package com.tto.gpt;


import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;
import java.time.Duration;


public class RemoteInvoker {

    final static CloseableHttpClient httpClient;
    final static RequestConfig requestConfig;
    final static RestTemplate restTemplate;

    static {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = null;
        try {
            sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        int timeout = 10000;
        requestConfig = RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).build();

        httpClient = HttpClients.custom().setSSLSocketFactory(csf).setDefaultRequestConfig(requestConfig).build();

        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        restTemplate = new RestTemplate(requestFactory);
    }

    public static boolean isReachAble(String url){
        try {
            restTemplate.headForHeaders(url);
            return true;
        } catch (Exception e) {
            return false;
        }
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

        return WebClient.create()
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
                }).timeout(Duration.ofSeconds(20));
    }
}
