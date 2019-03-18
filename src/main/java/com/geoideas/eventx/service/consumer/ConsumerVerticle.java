package com.geoideas.eventx.service.consumer;

import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

public class ConsumerVerticle extends AbstractVerticle {
    @Override
    public void start() {
        setup();
        System.out.println("EVENTX CONSUMER SERVICE RUNNING");
    }

    public void setup(){
        var address = config().getString("address");
        var service = EventService.create(vertx, address);
        new ServiceBinder(vertx).setAddress(address).register(EventService.class, service);
    }
}
