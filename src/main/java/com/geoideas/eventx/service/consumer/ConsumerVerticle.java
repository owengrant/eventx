package com.geoideas.eventx.service.consumer;

import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

public class ConsumerVerticle extends AbstractVerticle {
    @Override
    public void start() {
        setup();
        System.out.println("SPOOCH EVENT STORE CONSUMER SERVICE RUNNING");
    }

    public void setup(){
        var service = EventService.create(vertx,config().getString("address"));
        new ServiceBinder(vertx).setAddress("service:spooch-event").register(EventService.class, service);
    }
}
