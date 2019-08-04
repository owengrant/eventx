package com.geoideas.eventx.service.consumer;

import io.vertx.core.AbstractVerticle;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.serviceproxy.ServiceBinder;

public class ConsumerVerticle extends AbstractVerticle {
    
    private static final String SERVICE_STOP = "EVENTX CONSUMER SERVICE DISCOVERY STOPPED";
    private static final String SERVICE_STOP_FAIL = "EVENTX CONSUMER SERVICE DISCOVERY STOPPED FAILED";
    private static final String RECORD_PUB = "EVENTX CONSUMER SERVICE RECORD PUBLISHED";
    private static final String RECORD_WITH = "EVENTX CONSUMER SERVICE RECORD WITHDRAWN";
    private ServiceDiscovery sd;
    private Record record;
    
    @Override
    public void start() {
        setup();
        publishService();
        System.out.println("EVENTX CONSUMER SERVICE RUNNING");
    }

    @Override
    public void stop() throws Exception {
        sd.unpublish(record.getRegistration(), hndlr -> {
            if(hndlr.succeeded()) System.out.println(RECORD_WITH);
            else hndlr.cause().printStackTrace();  
        });
        sd.close();
    }
    
    private void publishService() {
        sd = ServiceDiscovery.create(vertx);
        var sn = config().getString("serviceName");
        var address = config().getString("address");
        record = EventBusService.createRecord(sn, address, com.geoideas.eventx.shared.EventService.class);
        sd.publish(record, hndlr -> {
            if(hndlr.succeeded()) System.out.println(RECORD_PUB);
            else hndlr.cause().printStackTrace();
        });
    }
    
    private void setup(){
        var address = config().getString("address");
        var service = EventService.create(vertx, address, config().getJsonObject("permissions"));
        new ServiceBinder(vertx).setAddress(address).register(EventService.class, service);
    }
}
