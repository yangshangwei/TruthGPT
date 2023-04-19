package com.tto.gpt;

import com.tto.gpt.common.RDResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;

@Controller
@RequestMapping("/")
public class TruthGPTWeb {

    @Value("${softwareVersion}")
    String softwareVersion;

    private final TruthGPTService service;

    public TruthGPTWeb(TruthGPTService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String index(ServerWebExchange exchange,Model model) {

        String ipAddress = getRequestIP(exchange);
        RDResult checkClient = service.checkClientIP(ipAddress);
        if(checkClient.beSuccess()) {

            model.addAttribute("INDEX_NOTE", service.getNote());
            model.addAttribute("INDEX_VERSION", service.getVersion());
            service.clearQuestions(ipAddress);
            model.addAttribute("CHAT_HISTORY",service.listChatHistory(ipAddress));
            model.addAttribute("NET_AVAILABLE", TruthGPTServiceImpl.netAvailable);

            model.addAttribute("_S_V_",softwareVersion);
            return "ttoIndex";
        }else {
            model.addAttribute("ERROR",checkClient.getMsg());
            return "ttoError";
        }
    }

    public static String getRequestIP(ServerWebExchange exchange){
        return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
    }
}
