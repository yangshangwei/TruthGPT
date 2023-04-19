package com.tto.gpt;


import com.github.markusbernhardt.proxy.ProxySearch;
import com.tto.gpt.common.RDResult;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import javax.net.ssl.SSLContext;
import java.net.*;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.List;


public class RemoteProxyInvoker {

    static CloseableHttpClient httpClient;
    static RequestConfig requestConfig;
    static RestTemplate restTemplate;
    static DefaultProxyRoutePlanner routePlanner;

    static ClientHttpConnector proxyConnector;
    static WebClient proxyWebClient;

    public static HttpHost getDefaultProxy(String requiredURL){
        ProxySearch proxySearch = new ProxySearch();
        proxySearch.addStrategy(ProxySearch.Strategy.OS_DEFAULT);
        proxySearch.addStrategy(ProxySearch.Strategy.JAVA);
        proxySearch.addStrategy(ProxySearch.Strategy.BROWSER);
        ProxySelector proxySelector = proxySearch.getProxySelector();

        ProxySelector.setDefault(proxySelector);
        URI home = URI.create(requiredURL);
        List<Proxy> proxyList = proxySelector.select(home);
        if (proxyList != null && !proxyList.isEmpty()) {
            for (Proxy proxy : proxyList) {
                System.out.println(proxy);
                SocketAddress address = proxy.address();
                if (address instanceof InetSocketAddress) {
                    return new HttpHost(((InetSocketAddress) address).getHostName(),((InetSocketAddress) address).getPort());
                }
            }
        }
        return null;
    }

    public static RDResult setProxy(HttpHost systemProxy){
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = null;
        try {
            sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        routePlanner = new DefaultProxyRoutePlanner(systemProxy);
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        int timeout = 120000;
        requestConfig = RequestConfig.custom().setProxy(systemProxy).setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).build();
        httpClient = HttpClients.custom().setSSLSocketFactory(csf).setRoutePlanner(routePlanner).setDefaultRequestConfig(requestConfig).build();

        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        restTemplate = new RestTemplate(requestFactory);

        proxyConnector = new ReactorClientHttpConnector(
                HttpClient.create()
                        .tcpConfiguration(tcpClient -> tcpClient.proxy(proxy ->
                                proxy.type(ProxyProvider.Proxy.HTTP)
                                        .host(systemProxy.getHostName())
                                        .port(systemProxy.getPort())
                        ))

        );
        proxyWebClient = WebClient.builder().clientConnector(proxyConnector).build();
        return RDResult.instance(0,"检测到本地的网络代理信息为[" + systemProxy.getHostName() + ":" + systemProxy.getPort() + "]");
    }

    public static ResponseEntity syncRemoteInvoke(
            HttpMethod method,
            String url,
            Object requestBody,
            MultiValueMap urlParams,
            MultiValueMap headers,
            Class cls) {

        HttpEntity requestEntity = new HttpEntity(requestBody, headers);
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url).queryParams(urlParams);

        ResponseEntity responseEntity = restTemplate.exchange(
                uriBuilder.toUriString(),
                method,
                requestEntity,
                cls);

        return responseEntity;
    }

    public static Flux<String> asyncRemotePostSSE(String url, MultiValueMap headers, Object requestBody, String prefix) {

        return proxyWebClient
                .post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(null == headers ? new LinkedMultiValueMap<>() : headers))
                .bodyValue(null == requestBody ? new Object() : requestBody)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .flatMapMany(response -> {
                    if (200 == response.rawStatusCode()) {
                        return response.bodyToFlux(String.class);
                    } else {
                        return Flux.error(new RuntimeException(String.valueOf(response.rawStatusCode())));
                    }
                }).timeout(Duration.ofSeconds(30));
    }
}
