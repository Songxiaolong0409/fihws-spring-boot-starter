package com.fih.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 交换机有四种类型,分别为Direct,topic,headers,Fanout.
 */
@Configuration
@ConfigurationProperties(prefix = "push.config")
@Data
@Slf4j
public class RabbitMQConfig {

    private String queue;

    public static final String EXCHANGE_PUSH="exchange_push";

    public static final String TOPIC_PUSH_MSG="topic.push.msg";

    public static final String TOPIC_PUSH_CONN="topic.push.conn";

    public static final String TOPIC_PUSH_MSG_CONTENT=TOPIC_PUSH_MSG+".content";

    public static final String TOPIC_PUSH_CONN_ADD=TOPIC_PUSH_CONN+".add";

    public static final String TOPIC_PUSH_CONN_DEL=TOPIC_PUSH_CONN+".del";

    public String getQueueConnName(){
        return TOPIC_PUSH_CONN+"."+queue;
    }

    public String getQueueName(){
        return TOPIC_PUSH_MSG+"."+queue;
    }

    /**
     * 定义队列
     * @return
     */
    @Bean(TOPIC_PUSH_MSG+".${push.config.queue}")
    public Queue queueInstance(){
        log.info("创建queue : {}",getQueueName());
        return new Queue(getQueueName(),true);
    }


    @Bean(TOPIC_PUSH_CONN+".${push.config.queue}")
    public Queue queueConnInstance(){
        log.info("创建queue : {}",getQueueConnName());
        return new Queue(getQueueConnName(),true);
    }


    /**
     * 定义交换机
     * @return
     */
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_PUSH);
    }


    /**
     * 义绑定关系，通过交换机 将名称为queue_push 的队列绑定到交换机上
     * @param queue
     * @param exchange
     * @return
     */
    @Bean
    public Binding bindingExchangeMessagePush() {
        return BindingBuilder.bind(queueInstance()).to(exchange()).with(TOPIC_PUSH_MSG+".*");
    }

    @Bean
    public Binding bindingExchangeConnMessagePush() {
        return BindingBuilder.bind(queueConnInstance()).to(exchange()).with(TOPIC_PUSH_CONN+".*");
    }

    /*@Bean
    public Binding bindingExchangeMessagePush(@Qualifier(QUERY_PUSH2) Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queueConnInstance()).to(exchange).with(TOPIC_PUSH);
    }*/
}
