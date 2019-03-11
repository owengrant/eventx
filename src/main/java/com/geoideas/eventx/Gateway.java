package com.geoideas.eventx;


import com.geoideas.eventx.service.consumer.EventService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.serviceproxy.ServiceBinder;


public final class Gateway extends AbstractVerticle {

    @Override
    public void start() {
        setup();
        System.out.println("SPOOCH EVENT STORE SERVICE RUNNING");
    }

    public void setup(){
        var service = EventService.create(vertx,config().getString("address"));
        new ServiceBinder(vertx).setAddress("service:spooch-event").register(EventService.class, service);
        var publisher = "com.geoideas.eventx.service.publisher.PublisherVerticle";
        vertx.deployVerticle(publisher, new DeploymentOptions().setConfig(config()));
        var consumer = "com.geoideas.eventx.service.consumer.ConsumerVerticle";
        vertx.deployVerticle(consumer, new DeploymentOptions().setConfig(config()));
    }
}
