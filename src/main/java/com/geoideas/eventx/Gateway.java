package com.geoideas.eventx;


import com.geoideas.eventx.service.consumer.EventService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.serviceproxy.ServiceBinder;


public final class Gateway extends AbstractVerticle {

    @Override
    public void start() {
        setup();
        bridgeEventbus();
        System.out.println("EVENTX SERVICE RUNNING");
    }
    
    public void setup(){
        var service = EventService.create(vertx,config().getString("address"));
        new ServiceBinder(vertx).setAddress("service:spooch-event").register(EventService.class, service);
        var publisher = "com.geoideas.eventx.service.publisher.PublisherVerticle";
        vertx.deployVerticle(publisher, new DeploymentOptions().setConfig(config()));
        var consumer = "com.geoideas.eventx.service.consumer.ConsumerVerticle";
        vertx.deployVerticle(consumer, new DeploymentOptions().setConfig(config()));
    }
    
    public void bridgeEventbus(){
        var bridge = SockJSHandler.create(vertx);
        var inbound = new PermittedOptions().setAddressRegex(config().getString("address"));
        var ops = new BridgeOptions().addInboundPermitted(inbound);
        bridge.bridge(ops);
        var router = Router.router(vertx);
        router.route("/eventbus/*").handler(bridge);
        var serverConfig = config().getJsonObject("bridge");
        vertx.createHttpServer()
             .requestHandler(router)
             .listen(
                     serverConfig.getInteger("port"),
                     serverConfig.getString("host"),
                     hndlr -> System.out.println(hndlr.succeeded() ? "EVENTX EVENTBUS BRIDGE INTEGRATION RUNNING" : "FAIL TO OPEN EVENTBUS BRIDGE")
             );
    }
}
