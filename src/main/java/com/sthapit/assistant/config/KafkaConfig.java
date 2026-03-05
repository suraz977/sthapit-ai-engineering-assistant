package com.sthapit.assistant.config;

import com.sthapit.assistant.model.AgentResult;
import com.sthapit.assistant.model.AgentTask;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {

  @Bean
  public ProducerFactory<String, Object> producerFactory(KafkaProperties props) {
    Map<String, Object> config = new HashMap<>(props.buildProducerProperties());
    config.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return new DefaultKafkaProducerFactory<>(config);
  }

  @Bean
  public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> pf) {
    return new KafkaTemplate<>(pf);
  }

  @Bean
  public ConsumerFactory<String, AgentTask> taskConsumerFactory(KafkaProperties props) {
    JsonDeserializer<AgentTask> deserializer = new JsonDeserializer<>(AgentTask.class);
    deserializer.addTrustedPackages("*");

    Map<String, Object> config = new HashMap<>(props.buildConsumerProperties());

    // Ensure group.id is never null
    String groupId = props.getConsumer().getGroupId();
    if (groupId == null || groupId.isBlank()) {
      groupId = "assistant";
    }
    config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

    return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, AgentTask> taskListenerFactory(ConsumerFactory<String, AgentTask> cf) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, AgentTask>();
    factory.setConsumerFactory(cf);
    return factory;
  }

  @Bean
  public ConsumerFactory<String, AgentResult> resultConsumerFactory(KafkaProperties props) {
    JsonDeserializer<AgentResult> deserializer = new JsonDeserializer<>(AgentResult.class);
    deserializer.addTrustedPackages("*");

    Map<String, Object> config = new HashMap<>(props.buildConsumerProperties());

    // Ensure group.id is never null
    String groupId = props.getConsumer().getGroupId();
    if (groupId == null || groupId.isBlank()) {
      groupId = "assistant";
    }
    config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

    return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, AgentResult> resultListenerFactory(ConsumerFactory<String, AgentResult> cf) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, AgentResult>();
    factory.setConsumerFactory(cf);
    return factory;
  }
}