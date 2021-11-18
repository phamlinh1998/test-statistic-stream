package com.example.testeventlog.utils;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.msgpack.jackson.dataformat.MessagePackFactory;

public class MessagePack {
  private static final ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper(new MessagePackFactory());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
  }

  public static byte[] objectToBytea(Object object) {
    try {
      return objectMapper.writeValueAsBytes(object);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T byteaToObject(byte[] bytea, Class<T> tClass) {
    try {
      return objectMapper.readValue(bytea, tClass);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
