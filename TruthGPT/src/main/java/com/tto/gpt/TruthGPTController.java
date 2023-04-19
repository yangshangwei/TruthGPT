package com.tto.gpt;

import com.tto.gpt.common.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/gpt")
public class TruthGPTController {
    private final TruthGPTService service;

    public TruthGPTController(TruthGPTService gptService) {
        this.service = gptService;
    }

    @GetMapping("/tg/stop/{sn}")
    public void stopQuestion(ServerWebExchange exchange, @PathVariable String sn) {
        String ip = TruthGPTWeb.getRequestIP(exchange);
        RDResult result = service.checkClientIP(ip);
        if (result.beSuccess()) {
            service.stopQuestion(ip,sn);
        }
    }

    @PostMapping("/tg/{sn}")
    public RDResult submitCodeQuestion(ServerWebExchange exchange, @RequestBody UIPrompt question, @PathVariable String sn) {

        String ip = TruthGPTWeb.getRequestIP(exchange);
        RDResult result = service.checkClientIP(ip);
        if (result.beSuccess()) {
            result = service.addRequestParameter(ip, sn, question);
        }
        return result;
    }

    @GetMapping(value = "/asyn/chat/{sn}/{token}/{temperature}/{qaDeep}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> generatingTexts(ServerWebExchange exchange,
                                                         @PathVariable String sn,
                                                         @PathVariable int token,
                                                         @PathVariable float temperature,
                                                         @PathVariable int qaDeep) {

        String ip = TruthGPTWeb.getRequestIP(exchange);
        RDResult result = service.checkClientIP(ip);
        if (result.beSuccess()) {
            GPT35TGBean bean = new GPT35TGBean();
            bean.setTemperature(temperature);
            bean.setMax_tokens(token);
            bean.setStream(true);

            return service.chatWithGPT(ip,sn, bean,qaDeep).map(data -> {
                try {
                    byte[] bytes = service.parseGPTResponse(ip,sn, (String) data, true).getBytes("UTF-8");
                    return Base64.getEncoder().encodeToString(bytes);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }).onErrorResume(e -> errorHandler(e,ip));
        } else {
            String errMsg = "【软件不可用：】客户端规模已达上限！！";
            try {
                return Flux.just(ServerSentEvent.builder(Base64.getEncoder().encodeToString(errMsg.getBytes("UTF-8"))).build());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @GetMapping("/authValidate")
    public RDResult authValidate(){
        String macAddress = TruthGPTServiceImpl.getMacAddress();
        if(ObjectUtils.isEmpty(macAddress)) {
            return RDResult.instance(1, "您的电脑貌似没有配置网卡，无法购买本机授权");
        }else {
            return RDResult.instance(0,"",macAddress);
        }
    }

    @GetMapping("/findBatchTitle")
    public RDResult findBatchTitle(ServerWebExchange exchange){

        String ip = TruthGPTWeb.getRequestIP(exchange);
        RDResult result = service.checkClientIP(ip);
        if (result.beSuccess()) {
            return service.findBatchTitle(ip);
        }else {
            String errMsg = "【软件不可用：】客户端规模已达上限！！";
            return RDResult.instance(1, errMsg);
        }
    }


    @GetMapping("/chatHis/{batchId}")
    public RDResult listChatHistoricDetail(@PathVariable String batchId,ServerWebExchange exchange){

        String ip = TruthGPTWeb.getRequestIP(exchange);
        RDResult result = service.checkClientIP(ip);
        if (result.beSuccess()) {
            return service.listChatRecordsByBatchId(batchId,ip);
        }else {
            String errMsg = "【软件不可用：】客户端规模已达上限！！";
            return RDResult.instance(1, errMsg);
        }
    }

    @GetMapping("/delChatHis/{batchId}")
    public RDResult deleteChatBatch(@PathVariable String batchId,ServerWebExchange exchange){

        String ip = TruthGPTWeb.getRequestIP(exchange);
        RDResult result = service.checkClientIP(ip);
        if (result.beSuccess()) {
            return service.deleteChatBatch(batchId,ip);
        }else {
            String errMsg = "【软件不可用：】客户端规模已达上限！！";
            return RDResult.instance(1, errMsg);
        }
    }

    @PostMapping("/updateChatHis")
    public RDResult updateChatHis(@RequestBody ChatBatchInfo cbi, ServerWebExchange exchange){

        if(null == cbi || ObjectUtils.isEmpty(cbi.getBatchTitle()) || ObjectUtils.isEmpty(cbi.getBatchId())){
            return RDResult.instance(1, "新的历史会话标题不可以为空!");
        }

        String ip = TruthGPTWeb.getRequestIP(exchange);
        RDResult result = service.checkClientIP(ip);
        if (result.beSuccess()) {
            return service.updateChatBatch(cbi,ip);
        }else {
            String errMsg = "【软件不可用：】客户端规模已达上限！！";
            return RDResult.instance(1, errMsg);
        }
    }


    @PostMapping("/ig/{sn}")
    public RDResult generateImage(ServerWebExchange exchange,@PathVariable String sn, @RequestBody GPTIGBean bean) {

        String ip = TruthGPTWeb.getRequestIP(exchange);
        RDResult result = service.checkClientIP(ip);
        if (result.beSuccess()) {
            return service.generateImage(ip,sn, bean);
        }else {
            String errMsg = "【软件不可用：】客户端规模已达上限！！";
            return RDResult.instance(1, errMsg);
        }
    }

    @GetMapping(value = "/asyn/sc/{sn}/{command}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> shortCommand(ServerWebExchange exchange,@PathVariable String sn, @PathVariable String command) {

        String ip = TruthGPTWeb.getRequestIP(exchange);
        RDResult result = service.checkClientIP(ip);
        if (result.beSuccess()) {
            return service.shortCommand(ip,sn, command).map(data -> {
                try {
                    byte[] bytes = service.parseGPTResponse(ip,sn, (String) data, false).getBytes("UTF-8");
                    return Base64.getEncoder().encodeToString(bytes);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }).onErrorResume(e -> errorHandler(e,null));
        }else {
            String errMsg = "【软件不可用：】客户端规模已达上限！！";
            try {
                return Flux.just(ServerSentEvent.builder(Base64.getEncoder().encodeToString(errMsg.getBytes("UTF-8"))).build());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }


    protected Flux<ServerSentEvent<String>> errorHandler(Object e,String ip) {

        if(!ObjectUtils.isEmpty(ip)){
            service.removeLastQuestion(ip);
        }

        try {
            if (e instanceof TimeoutException || e instanceof WebClientRequestException) {
                String errMsg = "【错误提示：】经过20秒的长久等待，OpenAI的API依然没有响应，大概网络是不通了，尝试配置代理服务器吧~~！";
                return Flux.just(ServerSentEvent.builder(Base64.getEncoder().encodeToString(errMsg.getBytes("UTF-8"))).build());
            }

            RuntimeException re = (RuntimeException) e;
            String errMsg = "";
            if (String.valueOf(HttpStatus.BAD_REQUEST.value()).equals(re.getMessage())) {
                errMsg = "【错误提示：】此次请求出现异常，可能是潜在的答案太长了！[错误码：" + re.getMessage() + "]";
            } else if (String.valueOf(HttpStatus.UNAUTHORIZED.value()).equals(re.getMessage())) {
                errMsg = "【错误提示：】您的OpenAI API Key无效！[错误码：" + re.getMessage() + "]";
            } else if (String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()).equals(re.getMessage())) {
                errMsg = "【错误提示：】您的API Key已到期或您访问过快或当前服务器负载过高，请重启再尝试3次，若依然不行则表示您的API Key已过期！[错误码：" + re.getMessage() + "]";
            } else if (String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()).equals(re.getMessage())) {
                errMsg = "【错误提示：】OpenAI的服务器内部出现错误，请稍后再试！[错误码：" + re.getMessage() + "]";
            }else if(String.valueOf(HttpStatus.NOT_FOUND.value()).equals(re.getMessage())){
                errMsg = "【错误提示：】请求失败！未找到相关的资源，请尝试更换您的APIKey！[错误码：" + re.getMessage() + "]";
            }else {
                errMsg = "【未知错误!错误码为：" + re.getMessage() + "】";
            }
            return Flux.just(ServerSentEvent.builder(Base64.getEncoder().encodeToString(errMsg.getBytes("UTF-8"))).build());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @GetMapping("/ls")
    public RDResult getLocalSetting(ServerWebExchange exchange) {
        String ip = TruthGPTWeb.getRequestIP(exchange);
        RDResult result = service.checkClientIP(ip);
        if (result.beSuccess()) {
            result = service.getLocalSettings(ip);
        }
        return result;
    }

    @PostMapping("/ls/update")
    public RDResult updateLocalSetting(ServerWebExchange exchange,@RequestBody GPTLocalSetting ls) {
        String ip = TruthGPTWeb.getRequestIP(exchange);
        RDResult result = service.checkClientIP(ip);
        if (result.beSuccess()) {
            result = service.updateLocalSettings(ip,ls);
        }
        return result;
    }

}
