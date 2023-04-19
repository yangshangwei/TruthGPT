package com.tto.gpt;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tto.gpt.common.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Flux;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class TruthGPTServiceImpl implements TruthGPTService {

    @Value("${openAIChatGPTURL}")
    public  String openAIChatGPTURL;
    @Value("${openAIChatModel}")
    public String chatModel = "gpt-3.5-turbo";
    @Value("${openAICreateImageURL}")
    public String openAICreateImageURL;
    @Value("${checkProxy}")
    public boolean checkProxy;

    @Value("${maxClient}")
    public int MaxClient;
    public static GPTIGBean GPTIGBeanTemplate = new GPTIGBean();
    public static boolean useProxy = false;
    public static boolean netAvailable = false;

    public static Map<String, QuestionContainer> clientQuestionContainer = new HashMap<>();
    public static Map<String, String> clientKeys = new HashMap<>();
    public static Map<String, MultiValueMap<String, String>> clientAuth = new HashMap<>();

    public static Map<String,Map<String, AtomicBoolean>> questionCancel = new HashMap<>();
    Map<String, String> responseCache = new HashMap<>();

    public static String note;
    public static String version;

    static {
        GPTIGBeanTemplate.setN(2);
        GPTIGBeanTemplate.setSize("1024x1024");
        GPTIGBeanTemplate.setResponse_format("b64_json");
    }
    public TruthGPTServiceImpl() {

    }
    public static String getMacAddress() {
        try {
            String osName = System.getProperty("os.name");
            if (osName.toLowerCase().contains("windows")) {
                return getWindowsMotherboardId();
            } else if (osName.toLowerCase().contains("linux")) {
                return getLinuxMotherboardId();
            } else if (osName.toLowerCase().contains("mac")) {
                return getMacMotherboardId();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getNote() {
        return note;
    }

    @Override
    public String getOpenAIServiceUrl() {
        return openAIChatGPTURL;
    }

    @Override
    public boolean needCheckProxy() {
        return checkProxy;
    }

    @Override
    public void stopQuestion(String ip, String sn) {
        AtomicBoolean cancel = questionCancel.get(ip).get(sn);
        if(null != cancel){
            cancel.set(true);
        }
    }

    @Override
    public RDResult findBatchTitle(String ip) {
        QuestionContainer container = clientQuestionContainer.get(ip);
        ChatBatchInfo cbi = null;
        if (null != container) {
            cbi = container.findBatchInfo();
        }
        if (null == cbi) {
            return RDResult.instance(1, "");
        } else {
            return RDResult.instance(0, "", cbi);
        }
    }

    @Override
    public void removeLastQuestion(String ip) {
        QuestionContainer container = clientQuestionContainer.get(ip);
        if (null != container) {
            container.removeLastQuestion();
        }
    }

    @Override
    public RDResult updateChatBatch(ChatBatchInfo cbi, String ip) {
        QuestionContainer container = clientQuestionContainer.get(ip);
        if (null == container) {
            return RDResult.instance(1, "您当前的IP为非法访问！");
        }
        return container.updateChatBatchTitle(cbi);
    }

    @Override
    public RDResult deleteChatBatch(String batchId, String ip) {
        QuestionContainer container = clientQuestionContainer.get(ip);
        if (null == container) {
            return RDResult.instance(1, "您当前的IP为非法访问！");
        }
        return container.deleteChatBatch(batchId);
    }

    @Override
    public RDResult listChatRecordsByBatchId(String batchId, String ip) {
        QuestionContainer container = clientQuestionContainer.get(ip);
        if (null == container) {
            return RDResult.instance(1, "您当前的IP为非法访问！");
        }

        ChatBatchInfo cbi = container.getChatBatch(batchId);
        List<ChatRecord> recordList = container.listBatchChatRecords(batchId);
        if (!ObjectUtils.isEmpty(recordList) && null != cbi) {
            ChatHistory history = new ChatHistory();
            history.setChatRecordList(recordList);
            history.setBatchInfo(cbi);
            return RDResult.instance(0, "", history);
        } else {
            container.deleteChatBatch(batchId);
            return RDResult.instance(1, "非常抱歉，指定的聊天记录未找到或已丢失！");
        }
    }

    @Override
    public List<ChatBatchInfo> listChatHistory(String ip) {
        QuestionContainer container = clientQuestionContainer.get(ip);
        if (null != container) {
            return container.listChatHistory();
        }
        return Collections.emptyList();
    }

    @Override
    public RDResult checkClientIP(String ip) {
        if (ObjectUtils.isEmpty(ip)) {
            return RDResult.instance(1, "未检测到客户端的IP，禁止访问!");
        }
        if (clientQuestionContainer.containsKey(ip)) {
            return RDResult.instance(0, "");
        }

        if (clientQuestionContainer.size() >= MaxClient) {
            return RDResult.instance(1, "允许访问的客户量已达上限[" + MaxClient + "]");
        }
        QuestionContainer container = new QuestionContainer();
        container.setRecordManager(new TextChatRecordManager(ip));
        clientQuestionContainer.put(ip, container);
        questionCancel.put(ip,new HashMap<>());

        return RDResult.instance(0, "");
    }

    @Override
    public RDResult updateLocalSettings(String ip, GPTLocalSetting config) {
        try {
            if (null == config || ObjectUtils.isEmpty(config.getOpenAIKey())) {
                return RDResult.instance(1, "OpenAI | ApiKey不可以为空!");
            }

            clientKeys.put(ip, config.getOpenAIKey());
            if (clientAuth.containsKey(ip)) {
                clientAuth.get(ip).remove("Authorization");
                clientAuth.get(ip).add("Authorization", "Bearer " + config.getOpenAIKey());
            } else {
                MultiValueMap<String, String> ipAuth = new LinkedMultiValueMap<>();
                ipAuth.add("Authorization", "Bearer " + config.getOpenAIKey());
                clientAuth.put(ip, ipAuth);
            }

            String currentDirectory = System.getProperty("user.dir");
            String snFile = currentDirectory + File.separator + "apiKeys.json";

            Path path = Paths.get(snFile);
            Files.write(path, JSON.toJSONString(clientKeys).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            return RDResult.instance(0, "OpenAI | ApiKey更新成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return RDResult.instance(1, "OpenAI | ApiKey更新失败！无法访问授权服务！");
        }
    }


    @Override
    public RDResult getLocalSettings(String ip) {
        GPTLocalSetting ls = new GPTLocalSetting();
        ls.setOpenAIKey(clientKeys.get(ip));
        return RDResult.instance(0, "", ls);
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public RDResult generateImage(String ip, String sn, GPTIGBean bean) {

        if (!clientKeys.containsKey(ip)) {
            return RDResult.instance(1, "请先设置你的API-Key！");
        }
        if (ObjectUtils.isEmpty(bean.getPrompt())) {
            return RDResult.instance(1, "要生成图片请先输入提示语");
        }

        GPTIGBean param = new GPTIGBean();
        param.setPrompt(bean.getPrompt());
        param.setSize(GPTIGBeanTemplate.getSize());
        param.setN(GPTIGBeanTemplate.getN());
        param.setResponse_format(GPTIGBeanTemplate.getResponse_format());

        ResponseEntity<String> response = null;
        int httpCode = 0;
        try {
            MultiValueMap<String, String> headerAuth = clientAuth.get(ip);
            if (null == headerAuth) {
                return RDResult.instance(1, "当前IP没有设置AppKey！");
            }
            if (useProxy) {
                response = RemoteProxyInvoker.syncRemoteInvoke(HttpMethod.POST, openAICreateImageURL, param, null, headerAuth, String.class);
            } else {
                response = RemoteInvoker.syncRemoteInvoke(HttpMethod.POST, openAICreateImageURL, param, null, headerAuth, String.class);
            }
            httpCode = response.getStatusCodeValue();
        } catch (HttpClientErrorException e) {
            httpCode = e.getRawStatusCode();
        }
        if (HttpStatus.OK.value() == httpCode && null != response) {
            String images = response.getBody();
            try {
                JSONObject object = JSONObject.parseObject(images);
                JSONArray imageList = JSONArray.parseArray(object.getString("data"));
                List<String> urlList = new ArrayList<>();
                if (null != imageList && imageList.size() > 0) {
                    for (int i = 0; i < imageList.size(); i++) {
                        JSONObject obj = imageList.getJSONObject(i);
                        urlList.add(obj.getString("b64_json"));
                        if (i >= 2) {
                            break;
                        }
                    }
                }
                if (ObjectUtils.isEmpty(urlList)) {
                    return RDResult.instance(1, "AI这次挺笨，没有生成任何图片");
                } else {
                    return RDResult.instance(0, "", urlList);
                }
            } catch (Exception e) {
                return RDResult.instance(1, "图片生成失败...");
            }
        } else if (HttpStatus.UNAUTHORIZED.value() == httpCode) {
            return RDResult.instance(1, "【错误提示：】您的OpenAI API Key无效！");
        } else if (HttpStatus.TOO_MANY_REQUESTS.value() == httpCode) {
            return RDResult.instance(1, "【错误提示：】您的API Key已到期或您访问过快或当前服务器负载过高，请重启再尝试3次，若依然不行则表示您的API Key已过期，请联系技术支持");
        } else if (HttpStatus.INTERNAL_SERVER_ERROR.value() == httpCode) {
            return RDResult.instance(1, "【错误提示：】OpenAI的服务器内部出现错误，请稍后再试");
        } else {
            return RDResult.instance(1, "【错误提示：】未知错误，请稍后再试");
        }
    }

    @Override
    public void clearQuestions(String ip) {
        if (clientQuestionContainer.containsKey(ip)) {
            clientQuestionContainer.get(ip).clear();
        }
    }

    @Override
    public RDResult addRequestParameter(String ip, String sn, UIPrompt question) {
        if (!clientKeys.containsKey(ip)) {
            return RDResult.instance(1, "请先设置你的API-Key！");
        }
        GPT35Message message = new GPT35Message(GPTRole.USER_ROLE, question.getPrompt());
        if (clientQuestionContainer.containsKey(ip)) {
            QuestionContainer container = clientQuestionContainer.get(ip);
            RDResult result = container.addQuestion(message);
            container.setSystemScope(question.getSystemScope());
            questionCancel.get(ip).put(sn,new AtomicBoolean(false));
            return result;
        } else {
            return RDResult.instance(1, "非法访问！");
        }
    }

    @Override
    public String parseGPTResponse(String ip, String sn, String json, boolean append) {

        if(questionCancel.get(ip).get(sn).get()){
            String finalContent = responseCache.get(sn);
            if (append) {
                clientQuestionContainer.get(ip).logQAHistory(finalContent);
            }
            responseCache.remove(sn);
            questionCancel.get(ip).remove(sn);
            return "[_U_O_F_]";
        }
        try {
            String res = GeneralJSONParser.parseValue(json, "choices[0].delta.content");
            if (responseCache.containsKey(sn)) {
                responseCache.replace(sn, responseCache.get(sn) + res);
            } else {
                responseCache.put(sn, res);
            }

            if (append && clientQuestionContainer.containsKey(ip)) {
                clientQuestionContainer.get(ip).appendAnswers(res);
            }
            return res;
        } catch (Exception e) {
            try {
                String res = GeneralJSONParser.parseValue(json, "choices[0].finish_reason");
                if ("stop".equals(res)) {
                    String finalContent = responseCache.get(sn);
                    QuestionContainer container = clientQuestionContainer.get(ip);
                    if (null != container) {
                        if (append) {
                            container.logQAHistory(finalContent);
                            container.cacheAnswer(sn);
                        }
                    }
                    responseCache.remove(sn);
                    questionCancel.get(ip).remove(sn);
                    return "[_T_O_F_]" + finalContent;
                }
            } catch (Exception e1) {

            }
            return "";
        }
    }


    @Override
    public Flux shortCommand(String ip, String sn, String cmd) {
        if (!clientKeys.containsKey(ip)) {
            return Flux.error(new RuntimeException("ERR-NO-KEY"));
        }
        QuestionContainer questionContainer = clientQuestionContainer.get(ip);
        if (null == questionContainer) {
            return Flux.error(new RuntimeException("ERR-FK-NQ"));
        }
        String message = questionContainer.getCachedAnswer(sn);
        if (null == message || ObjectUtils.isEmpty(message)) {
            return Flux.error(new RuntimeException("ERR-FK-NQ"));
        }

        String command = "";
        if ("2en".equals(cmd)) {
            command = "Translate the text below to English:\n\n";
        } else if ("2cn".equals(cmd)) {
            command = "Translate the text below to Chinese:\n\n";
        } else if ("kws".equals(cmd)) {
            command = "Extract keywords from the text below :\n\n";
        }

        List<GPT35Message> messageList = new ArrayList<>();
        messageList.add(new GPT35Message(GPTRole.USER_ROLE, command + message));
        GPT35TGBean bean = new GPT35TGBean();
        bean.validation();
        bean.setStream(true);
        bean.setMessages(messageList);
        questionCancel.get(ip).put(sn,new AtomicBoolean(false));

        return communicateWithOpenAI(ip, bean);
    }

    @Override
    public Flux chatWithGPT(String ip, String sn, GPT35TGBean bean, int qaDeep) {
        if (!clientKeys.containsKey(ip)) {
            return Flux.error(new RuntimeException("ERR-NO-KEY"));
        }
        QuestionContainer questionContainer = clientQuestionContainer.get(ip);
        if (null == questionContainer) {
            return Flux.error(new RuntimeException("系统未接收到你的实际问题"));
        }
        List<GPT35Message> messageList;

        if (qaDeep < 1 || qaDeep > 10) {
            qaDeep = 1;
        }

        messageList = questionContainer.getQAStack(qaDeep);
        if (ObjectUtils.isEmpty(messageList)) {
            return Flux.error(new RuntimeException("系统未接收到你的实际问题"));
        }

        bean.validation();
        bean.setMessages(messageList);

        return communicateWithOpenAI(ip, bean);
    }

    protected Flux communicateWithOpenAI(String ip, GPT35TGBean bean) {

        MultiValueMap<String, String> headerAuth = clientAuth.get(ip);
        if (null == headerAuth) {
            return Flux.error(new RuntimeException("当前IP没有设置AppKey"));
        }

        if (useProxy) {
            return RemoteProxyInvoker.asyncRemotePostSSE(
                    openAIChatGPTURL,
                    headerAuth,
                    bean,
                    "data:");
        } else {
            return RemoteInvoker.asyncRemotePostSSE(
                    openAIChatGPTURL,
                    headerAuth,
                    bean,
                    "data:");
        }
    }

    public static int dynamicTokens(String str) {
        int alphaWords = 0;
        Matcher matcher = Pattern.compile("\\w+").matcher(str);
        while (matcher.find()) {
            alphaWords++;
        }

        int chineseCharCount = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                chineseCharCount++;
            }
        }
        int tokenSize = (int) ((chineseCharCount * 2 + alphaWords) * 1.1);
        return tokenSize;
    }

    public static void main(String args[]) throws Exception {
    }

    public static String getLinuxMotherboardId() throws IOException {
        String result = "";
        BufferedReader br = new BufferedReader(new FileReader("/sys/class/dmi/id/product_uuid"));
        result = br.readLine().trim();
        br.close();
        return "L" + result;
    }

    public static String getWindowsMotherboardId() throws IOException {
        String result = "";
        try {
            File file = File.createTempFile("realhowto",".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);

            String vbs1=
                    "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                            + "Set colItems = objWMIService.ExecQuery _ \n"
                            + "   (\"Select * from Win32_Processor\") \n"
                            + "For Each objItem in colItems \n"
                            + "    Wscript.Echo objItem.ProcessorId \n"
                            + "    exit for  ' do the first cpu only! \n"
                            + "Next \n";


            fw.write(vbs1);
            fw.close();

            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
        }
        catch(Exception E){
            System.err.println("Windows CPU Exp : "+E.getMessage());
        }
        return "W" + result.trim();
    }

    public static String getMacMotherboardId() throws IOException {
        String result = "";
        Process p = Runtime.getRuntime().exec("system_profiler SPHardwareDataType");
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = input.readLine()) != null) {
            result += line;
        }
        input.close();
        // 从结果中解析出主板序列号
        Pattern pattern = Pattern.compile("Serial Number \\(system\\):\\s+(\\S+)");
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()) {
            return "M" + matcher.group(1);
        }
        return null;
    }
}
