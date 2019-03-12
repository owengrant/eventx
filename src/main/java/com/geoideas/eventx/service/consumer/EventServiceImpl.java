package com.geoideas.eventx.service.consumer;

import com.geoideas.eventx.service.publisher.PublisherVerticle;
import com.geoideas.eventx.shared.EventDTO;;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceException;

public class EventServiceImpl implements EventService{

    private Vertx vertx;
    private JsonObject config;
    private String address;

    public EventServiceImpl(Vertx vertx, String address){
        this.address = address;
        this.vertx = vertx;
    }

    public void handle(AsyncResult<Message<JsonArray>> result, JsonObject event,Handler<AsyncResult<JsonArray>> complete){
        if(result.failed()){
            var rEx = (ReplyException) result.cause();
            complete.handle(ServiceException.fail(rEx.failureCode(),rEx.getMessage(),event));
        }    
        else
          complete.handle(Future.future(h -> h.complete(result.result().body())));
    }

    public  void query(String dbAddress,EventDTO event, Handler<AsyncResult<JsonArray>> complete){
        var data = event.toJson();
        vertx.eventBus().<JsonArray>send(address+dbAddress, data , result -> handle(result,data,complete));
    }

    @Override
    public void publish(JsonObject event, Handler<AsyncResult<JsonObject>> complete) {
        //authentication and authorization
        vertx.eventBus().<JsonObject>send(address+PublisherVerticle.PUBLISH, event , result -> {
            if(result.failed())
                complete.handle(ServiceException.fail(EventService.PUBLISH_ERROR,result.cause().getMessage(), event));
            else
                complete.handle(Future.future(h -> h.complete(result.result().body())));
        });
    }

    @Override
    public void publishOCC(JsonObject newEvent, JsonObject oldEvent, Handler<AsyncResult<JsonObject>> complete) {
        //authentication and authorization
        var query = new JsonObject().put("newEvent",newEvent).put("oldEvent",oldEvent);
        vertx.eventBus().<JsonObject>send(address+PublisherVerticle.PUBLISH_OCC, query , result -> {
            if(result.failed())
                complete.handle(ServiceException.fail(EventService.PUBLISH_ERROR,result.cause().getMessage(), query));
            else
                complete.handle(Future.future(h -> h.complete(result.result().body())));
        });
    }


    @Override
    public void poll(int eventId, Handler<AsyncResult<JsonArray>> complete) {
        var event = new EventDTO();
        event.setEventId(eventId);
        query(PublisherVerticle.POLL, event, complete);
    }

    @Override
    public void pollEvent(int eventId, String event, Handler<AsyncResult<JsonArray>> complete) {
        var data = new EventDTO();
        data.setEventId(eventId);
        data.setEvent(event);
        query(PublisherVerticle.POLL_EVENT, data, complete);
    }

    @Override
    public void pollEntity(int eventId, String entity, Handler<AsyncResult<JsonArray>> complete) {
        var data = new EventDTO();
        data.setEventId(eventId);
        data.setEntity(entity);
        query(PublisherVerticle.POLL_ENTITY, data, complete);
    }

    @Override
    public void pollEntityEvent(int eventId, String entity, String event, Handler<AsyncResult<JsonArray>> complete) {
        var data = new EventDTO();
        data.setEventId(eventId);
        data.setEntity(entity);
        data.setEvent(event);
        query(PublisherVerticle.POLL_ENTITY_EVENT, data, complete);
    }

    @Override
    public void pollEntityById(int entityId, String entity, Handler<AsyncResult<JsonArray>> complete) {
        var data = new EventDTO();
        data.setEntityId(entityId);
        data.setEntity(entity);
        query(PublisherVerticle.POLL_ENTITY_BY_ID, data, complete);
    }

    @Override
    public void findLastEvent(int entityId, String entity, Handler<AsyncResult<JsonArray>> complete) {
        var data = new EventDTO();
        data.setEntityId(entityId);
        data.setEntity(entity);
        query(PublisherVerticle.FIND_LAST_EVENT, data, complete);
    }

    @Override
    public void find(String hash, Handler<AsyncResult<JsonArray>> complete) {
        var data = new EventDTO();
        data.setHash(hash);
        query(PublisherVerticle.FIND, data, complete);
    }


}
