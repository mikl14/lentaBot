package com.telegrambot.lentaBot.bot.service.rest;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public abstract class RestService {
    private final RestTemplate restTemplate;
    protected final ObjectMapper mapper;


    public RestService(RestTemplate restTemplate, ObjectMapper mapper) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }

    /**
     * <b>buildResponse</b> - собирает запрос
     *
     * @param url     ссылка
     * @param headers заголовки
     * @param body    тело
     * @return ResponseEntity<String>
     */
    private ResponseEntity<String> buildResponse(String url, Map<String, String> headers, String body) {
        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.setContentType(MediaType.APPLICATION_JSON);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            reqHeaders.add(entry.getKey(), entry.getValue());
        }

        HttpEntity<String> requestEntity = new HttpEntity<>(body, reqHeaders);
        try {
            ResponseEntity<String> req = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return req;
        } catch (HttpClientErrorException e) {
            return null;
        }
    }

    /**
     * <b>sendRequest</b> - отправляет запрос по передаваемому url
     *
     * @param url
     * @param headers
     * @param body
     * @param responseType
     * @param <T>
     * @return <T> T
     */
    public <T> T sendRequest(String url, Map<String, String> headers, String body, Class<T> responseType) {

        ResponseEntity<String> response = buildResponse(url, headers, body);

        if (response != null) {
            try {
                return mapper.readValue(response.getBody(), responseType);
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * <b>sendRequest</b> - отправляет запрос по указанному url возвращает не объект
     *
     * @param url
     * @param headers
     * @param body
     * @return ResponseEntity<String>
     */
    public ResponseEntity<String> sendRequest(String url, Map<String, String> headers, String body) {

        ResponseEntity<String> response = buildResponse(url, headers, body);

        if (response != null) {
            try {
                return response;
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * <b>sendEmptyRequest</b> -отправляет запрос и возвращает boolean результата
     *
     * @param url
     * @param headers
     * @param body
     * @return
     */
    public boolean sendEmptyRequest(String url, Map<String, String> headers, String body) {

        ResponseEntity<String> response = sendRequest(url, headers, body);

        assert response != null;
        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * <b>sendRequestWithList</b> - отправляет запрос по url возвращает список объектов
     *
     * @param url
     * @param headers
     * @param body
     * @param responseType
     * @param <T>
     * @return
     */
    public <T> List<T> sendRequestWithList(String url, Map<String, String> headers, String body, Class<T> responseType) {

        JavaType javaType = mapper.getTypeFactory().constructCollectionType(List.class, responseType);
        try {
            ResponseEntity<String> response = buildResponse(url, headers, body);
            if (response.getStatusCode().is2xxSuccessful()) {
                try {
                    return mapper.readValue(response.getBody(), javaType);
                } catch (Exception e) {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            return new ArrayList<T>();
        }
    }

}
