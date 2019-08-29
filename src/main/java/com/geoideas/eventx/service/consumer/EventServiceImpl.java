package com.geoideas.eventx.service.consumer;

import com.geoideas.eventx.service.publisher.PublisherVerticle;
import com.geoideas.eventx.shared.Error;
import com.geoideas.eventx.shared.EventDTO;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.ReplyFailure;

public class EventServiceImpl implements EventService{

    private Vertx vertx;
    private JsonObject permissions;
    private String address;

    public EventServiceImpl(Vertx vertx, String address){
        this.address = address;
        this.vertx = vertx;
    }
    
    public EventServiceImpl(Vertx vertx, String address, JsonObject permissions){
        this.address = address;
        this.vertx = vertx;
        this.permissions = permissions;
    }

    public void handle(AsyncResult<Message<JsonArray>> result, JsonObject event,Handler<AsyncResult<JsonArray>> complete){
        if(result.failed()){
            var rEx = (ReplyException) result.cause();
            complete.handle(fail(rEx.failureCode(),rEx.getMessage()));
        }    
        else
          complete.handle(Future.future(h -> h.complete(result.result().body())));
    }

    public  void query(String dbAddress,EventDTO event, Handler<AsyncResult<JsonArray>> complete){
        var data = event.toJson();
        vertx.eventBus().<JsonArray>request(address+dbAddress, data , result -> handle(result,data,complete));
    }

    @Override
    public void publish(JsonObject event, Handler<AsyncResult<JsonObject>> complete) {
        //authentication and authorization
        if(!publishAuth(event)){
            complete.handle(fail(Error.UNAUTHORISED_ERROR,Error.UNAUTHORISED_ERROR_MESSAGE));
            return;
        }
        var eventDTO = new EventDTO().fromJson(event);
        vertx.eventBus().<JsonObject>request(address+PublisherVerticle.PUBLISH, event , result -> {
            if(result.failed())
                complete.handle(fail(EventService.PUBLISH_ERROR,result.cause().getMessage()));
            else
                complete.handle(Future.future(h -> h.complete(result.result().body())));
        });
    }

    @Override
    public void publishOCC(JsonObject newEvent, JsonObject oldEvent, Handler<AsyncResult<JsonObject>> complete) {
        //authentication and authorization
        if(!publishAuth(newEvent)){
            complete.handle(fail(Error.UNAUTHORISED_ERROR,Error.UNAUTHORISED_ERROR_MESSAGE));
            return;
        }
        var query = new JsonObject().put("newEvent",newEvent).put("oldEvent",oldEvent);
        vertx.eventBus().<JsonObject>request(address+PublisherVerticle.PUBLISH_OCC, query , result -> {
            if(result.failed())
                complete.handle(fail(EventService.PUBLISH_ERROR,result.cause().getMessage()));
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
    public void pollEntityById(String entityId, String entity, Handler<AsyncResult<JsonArray>> complete) {
        var data = new EventDTO();
        data.setEntityId(entityId);
        data.setEntity(entity);
        query(PublisherVerticle.POLL_ENTITY_BY_ID, data, complete);
    }

    @Override
    public void pollContext(int eventId, String context, Handler<AsyncResult<JsonArray>> complete) {
        query(PublisherVerticle.POLL_CONTEXT, new EventDTO().setEventId(eventId).setContext(context), complete);
    }

    @Override
    public void findLastEvent(String entityId, String entity, Handler<AsyncResult<JsonArray>> complete) {
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

    private boolean publishAuth(JsonObject request) {
        System.out.println(request.encodePrettily());
        var event = new EventDTO().fromJson(request);
        System.out.println(event.getEvent());
        var pass = false;
        var context = event.getContext();
        if(permissions.containsKey(context)) {
            var contextPerms = permissions.getJsonObject(context);
            var events = contextPerms.getJsonArray("events");
            pass = events.contains(event.getEvent());
        }
        return pass;
    }
    
    private <T> AsyncResult<T> fail(int code, String message) {
        return Future.future( h -> h.complete((T)new ReplyException(ReplyFailure.RECIPIENT_FAILURE, code, message)));
    }
}
