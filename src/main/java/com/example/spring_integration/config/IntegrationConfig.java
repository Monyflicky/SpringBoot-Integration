package com.example.spring_integration.config;

import com.example.spring_integration.model.Address;
import com.example.spring_integration.model.Student;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.filter.MessageFilter;
import org.springframework.integration.json.JsonToObjectTransformer;
import org.springframework.integration.json.ObjectToJsonTransformer;
import org.springframework.integration.router.HeaderValueRouter;
import org.springframework.integration.router.PayloadTypeRouter;
import org.springframework.integration.router.RecipientListRouter;
import org.springframework.integration.support.json.Jackson2JsonObjectMapper;
import org.springframework.integration.transformer.HeaderEnricher;
import org.springframework.integration.transformer.support.HeaderValueMessageProcessor;
import org.springframework.integration.transformer.support.StaticHeaderValueMessageProcessor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class IntegrationConfig
{
    //defines the message channels
    @Bean
    public MessageChannel recieverChannel(){
        return new DirectChannel();
    }

    @Bean
    public MessageChannel replyChannel(){
        return new DirectChannel();
    }

    @Bean
    @Transformer(inputChannel = "integration.student.gateway.channel", outputChannel = "integration.student.toConvertObject.channel")
    public HeaderEnricher enrichHeader()
    {
        Map<String, HeaderValueMessageProcessor<String>> headersToAdd = new HashMap();
        headersToAdd.put("header1", new StaticHeaderValueMessageProcessor<String>("Test Header 1"));
        headersToAdd.put("header2", new StaticHeaderValueMessageProcessor<String>("Test Header 2"));
        HeaderEnricher enrich = new HeaderEnricher(headersToAdd);

        return enrich;
    }
    @Bean
    @Transformer(inputChannel = "integration.student.toConvertObject.channel", outputChannel = "integration.student.objectToJson.channel")
    public ObjectToJsonTransformer objectToJsonTransformer(){
        return new ObjectToJsonTransformer();
    }

    @Bean
    public Jackson2JsonObjectMapper getMapper()
    {
        ObjectMapper mapper = new ObjectMapper();
        return new Jackson2JsonObjectMapper(mapper);
    }

    @Bean
    @Transformer(inputChannel = "integration.student.jsonToObject.channel", outputChannel = "integration.student.jsonToObject.fromTransformer.channel")
    public JsonToObjectTransformer jsonToObjectTransformer(){
        return new JsonToObjectTransformer();
    }

    @Bean
    @ServiceActivator(inputChannel = "router.channel")
    public PayloadTypeRouter router(){
        PayloadTypeRouter router = new PayloadTypeRouter();
        router.setChannelMapping(Student.class.getName(), "student.enrich.header.channel");
        router.setChannelMapping(Address.class.getName(), "address.enrich.header.channel");

        return router;
    }
    @Bean
    @Transformer(inputChannel = "student.enrich.header.channel", outputChannel = "header.payload.router.channel")
    public HeaderEnricher enrichHeaderForStudent()
    {
        Map<String, HeaderValueMessageProcessor<String>> headersToAdd = new HashMap();
        headersToAdd.put("testHeader", new StaticHeaderValueMessageProcessor<String>("student"));
        HeaderEnricher enrich = new HeaderEnricher(headersToAdd);

        return enrich;
    }

    @Bean
    @Transformer(inputChannel = "address.enrich.header.channel", outputChannel = "header.payload.router.channel")
    public HeaderEnricher enrichHeaderForAddress()
    {
        Map<String, HeaderValueMessageProcessor<String>> headersToAdd = new HashMap();
        headersToAdd.put("testHeader", new StaticHeaderValueMessageProcessor<String>("address"));
        HeaderEnricher enrich = new HeaderEnricher(headersToAdd);

        return enrich;
    }

    @Bean
    @ServiceActivator(inputChannel = "header.payload.router.channel")
    public HeaderValueRouter headerRouter(){
        HeaderValueRouter router = new HeaderValueRouter("testHeader");
        router.setChannelMapping("student", "student.channel");
        router.setChannelMapping("address", "address.channel");

        return router;
    }

    @Bean
    @Filter(inputChannel = "router.channel")
    public MessageFilter filter(){
        return new MessageFilter(new MessageSelector() {
            @Override
            public boolean accept(Message<?> message) {
                return message.getPayload() instanceof Student;
            }
        });
    }
//    @Bean
//    @ServiceActivator(inputChannel = "router.channel")
//    public RecipientListRouter router(){
//        RecipientListRouter router = new RecipientListRouter();
//        router.addRecipient("student.channel.1");
//        router.addRecipient("student.channel.2");
//
//        return router;
//    }
}
