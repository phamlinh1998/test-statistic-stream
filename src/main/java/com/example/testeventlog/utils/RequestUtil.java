package com.example.testeventlog.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Log4j2
public class RequestUtil {

  /**
   * CURL
   *
   * @param method
   * @param requestUrl
   * @param mData
   * @param headerParam
   * @return
   */
  public static String postFormDataRequest(
      HttpMethod method,
      String requestUrl,
      Object mData,
      Map<String, String> headerParam,
      boolean snackMap) {
    try {
      RestTemplate restTemplate = new RestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      if (headerParam != null) {
        for (Map.Entry<String, String> entry : headerParam.entrySet()) {
          headers.add(entry.getKey(), entry.getValue());
        }
      }
      MultiValueMap<String, String> formData;
      if (snackMap == false) {
        formData = JsonParser.objectToMap(mData);
      } else {
        formData = JsonParser.objectToMapSnackCase(mData);
      }
      HttpEntity<MultiValueMap<String, String>> data = new HttpEntity<>(formData, headers);

      return restTemplate.exchange(requestUrl, method, data, String.class).getBody();
    } catch (HttpClientErrorException e) {
      return e.getResponseBodyAsString();
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  /**
   * CURL
   *
   * @param method
   * @param requestUrl
   * @param entity
   * @param headerParam
   * @return
   */
  public static String sendRequest(
      HttpMethod method, String requestUrl, Object entity, Map<String, String> headerParam) {
    try {
      RestTemplate restTemplate = new RestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      if (headerParam != null) {
        for (Map.Entry<String, String> entry : headerParam.entrySet()) {
          headers.add(entry.getKey(), entry.getValue());
        }
      }

      HttpEntity<Object> data = new HttpEntity<>(entity, headers);

      return restTemplate.exchange(requestUrl, method, data, String.class).getBody();
    } catch (HttpClientErrorException e) {
      e.printStackTrace();
      return e.getResponseBodyAsString();
    } catch (Exception e) {
      e.printStackTrace();
      return "Error: " + e.getMessage();
    }
  }

  /**
   * Create query string.
   *
   * @param clazz Entity class
   * @param object Entity object
   * @return
   */
  public static String getRequestString(Class clazz, Object object) {
    StringBuilder queryStringBuilder = new StringBuilder();
    Map<String, String> queryParams = new LinkedHashMap<>();

    try {
      for (Field f : clazz.getDeclaredFields()) {
        f.setAccessible(true);
        queryParams.put(f.getName(), String.valueOf(f.get(object)));
      }
      for (Map.Entry<String, String> entry : queryParams.entrySet()) {
        queryStringBuilder.append(CaseUtil.toSnack(entry.getKey()));
        queryStringBuilder.append("=");
        queryStringBuilder.append(entry.getValue());
        queryStringBuilder.append("&");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    String queryString = queryStringBuilder.toString();
    return "?" + queryString.substring(0, queryString.length() - 1);
  }
}
