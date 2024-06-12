package com.zerobase.customboard.infra.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

  @InjectMocks
  private RedisService redisService;

  @Mock
  private RedisTemplate<String, Object> redisTemplate;

  @Mock
  private ValueOperations<String, Object> valueOperations;

  @Test
  @DisplayName("Redis 데이터 가져오기")
  void testGetData() throws Exception{
    // given
    String key = "testKey";
    String expectedValue = "testValue";

    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get(key)).willReturn(expectedValue);

    // when
    String actualValue = redisService.getData(key);

    //then
    assertEquals(expectedValue, actualValue);
    verify(redisTemplate.opsForValue(), times(1)).get(key);
  }

  @Test
  @DisplayName("Redis 데이터 삭제")
  void testDeleteData() throws Exception{
    // given
    String key = "testKey";

    // when
    redisService.deleteData(key);

    //then
    verify(redisTemplate, times(1)).delete(key);
  }

  @Test
  @DisplayName("Redis 데이터 저장")
  void testSetDataExpire() throws Exception{
    // given
    String key = "testKey";
    String value = "testValue";
    long expired = 1000L;

    given(redisTemplate.opsForValue()).willReturn(valueOperations);

    // when
    redisService.setDataExpire(key,value,expired);

    //then
    verify(valueOperations, times(1)).set(key, value, expired, TimeUnit.MILLISECONDS);
  }

  @Test
  @DisplayName("Redis 데이터 존재여부 확인")
  void testExistData() throws Exception{
    // given
    String key = "testKey";

    given(redisTemplate.hasKey(key)).willReturn(true);

    // when
    boolean exists = redisService.existData(key);

    //then
    assertTrue(exists);
    verify(redisTemplate, times(1)).hasKey(key);
  }

}