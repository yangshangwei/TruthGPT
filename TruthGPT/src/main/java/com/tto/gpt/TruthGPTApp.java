package com.tto.gpt;

import org.apache.http.HttpHost;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
public class TruthGPTApp {

    public static void main(String[] args) {

        SpringApplication app = new SpringApplication(TruthGPTApp.class);
        ConfigurableApplicationContext context = app.run(args);
        System.out.println("============================================================================");
        System.out.println("......TruthGPT启动成功！！开始检测本网络的连通性....");

        TruthGPTService service = context.getBean(TruthGPTService.class);
        if(service.needCheckProxy()) {
            System.out.println("....配置中指定了要检测本地代理，开始检测....");
            try {
                HttpHost proxy = RemoteProxyInvoker.getDefaultProxy(service.getOpenAIServiceUrl());
                if (null != proxy) {
                    TruthGPTServiceImpl.useProxy = true;
                    RemoteProxyInvoker.setProxy(proxy);
                    System.out.println("【恭喜】!!!您的网络现在是可以代理服务访问OpenAI的服务的！");
                    System.out.println("请在浏览器地址中输入  http://127.0.0.1:8888   尽情享用你的TruthGPT吧");
                    TruthGPTServiceImpl.netAvailable = true;
                } else {
                    System.out.println("【抱歉】!!!您的网络暂时无法访问OpenAI的相关服务！");
                }

            } catch (Exception e) {
                System.out.println("【抱歉】!!!您的网络暂时无法访问OpenAI的相关服务！");
            }
        }else {
            System.out.println("....配置中未指定了要检测本地代理，软件将以直连方式访问OpenAI的相关服务....");
        }
        System.out.println("============================================================================");
    }
}
