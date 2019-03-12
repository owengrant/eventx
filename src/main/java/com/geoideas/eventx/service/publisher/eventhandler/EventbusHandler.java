package com.geoideas.eventx.service.publisher.eventhandler;

import com.geoideas.eventx.service.publisher.database.dao.EventDAO;
import com.geoideas.eventx.shared.Error;
import com.geoideas.eventx.shared.EventDTO;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventbusHandler {

    private Logger log = LoggerFactory.getLogger(EventbusHandler.class);
    
    private EventDAO eDao;

    public EventbusHandler(EventDAO eDao){
        this.eDao = eDao;
    }
    
    private void handleError(Message<JsonObject> message, Throwable e){
        handleError(message,e.getMessage());
    }
    
    private void handleError(Message<JsonObject> message, String err){
        var errCode = Error.UNKNOWN_ERROR;
        var errMessage = Error.UNKNOWN_ERROR_MESSAGE;
        if(err.contains("hash") || err.contains(Error.DUPLICATE_HASH_MESSAGE)){
            errCode = Error.DUPLICATE_HASH;
            errMessage = Error.DUPLICATE_HASH_MESSAGE;
        }
        else if(err.contains("primary") || err.contains(Error.DUPLICATE_EVENT_MESSAGE)){
            errCode = Error.DUPLICATE_EVENT;
            errMessage = Error.DUPLICATE_EVENT_MESSAGE;
        }
        message.fail(errCode, errMessage); 
        log.error(err);
    }
    
    private void handlePublish(Message<JsonObject> message, EventDTO event){
        if(event.getEventId() != -1)
            message.reply(event.toJson());
        else
            handleError(message, "Failed possibly because: "+Error.DUPLICATE_EVENT_MESSAGE);
    }
    
    public EventDTO getEvent(Message<JsonObject> message){
        return new EventDTO().fromJson(message.body());
    }

    public void handlePoll(Single<JsonArray> action, Message<JsonObject> message){
        action.subscribe(events -> message.reply(events), e -> handleError(message,e));
    }

    public void publish(Message<JsonObject> message){
        if(message.body() == null){
            message.fail(Error.EVENT_CONTENT_ERROR, Error.EVENT_CONTENT_ERROR_MESSAGE);
            return;
        }    
        var event = getEvent(message);
        eDao.publish(event)
            .subscribe(v -> handlePublish(message,v), e -> handleError(message,e));
    }

    public void publishOCC(Message<JsonObject> message){
        var body = message.body();
        if(body == null || body.getJsonObject("newEvent") == null || body.getJsonObject("oldEvent") == null){
            message.fail(Error.EVENT_CONTENT_ERROR, Error.EVENT_CONTENT_ERROR_MESSAGE);
            return;
        }   

        var newEvent = new EventDTO().fromJson(body.getJsonObject("newEvent"));
        var oldEvent = new EventDTO().fromJson(body.getJsonObject("oldEvent"));
        eDao.publishOCC(newEvent, oldEvent)
            .subscribe(v -> handlePublish(message,v), e -> handleError(message,e));
    }

    public void poll(Message<JsonObject> message){
        if(message.body() == null){
            message.fail(Error.EVENT_CONTENT_ERROR, Error.EVENT_CONTENT_ERROR_MESSAGE);
            return;
        }   
        handlePoll(eDao.poll(getEvent(message)), message);
    }

    public void pollEvent(Message<JsonObject> message){
        if(message.body() == null){
            message.fail(Error.EVENT_CONTENT_ERROR, Error.EVENT_CONTENT_ERROR_MESSAGE);
            return;
        }   
        handlePoll(eDao.pollEvent(getEvent(message)), message);
    }

    public void pollEntity(Message<JsonObject> message){
        if(message.body() == null){
            message.fail(Error.EVENT_CONTENT_ERROR, Error.EVENT_CONTENT_ERROR_MESSAGE);
            return;
        }   
        handlePoll(eDao.pollEntity(getEvent(message)), message);
    }

    public void pollEntityEvent(Message<JsonObject> message){ 
        if(message.body() == null){
            message.fail(Error.EVENT_CONTENT_ERROR, Error.EVENT_CONTENT_ERROR_MESSAGE);
            return;
        }   
        handlePoll(eDao.pollEntityEvent(getEvent(message)), message);
    }

    public void pollEntityById(Message<JsonObject> message) { 
        if(message.body() == null){
            message.fail(Error.EVENT_CONTENT_ERROR, Error.EVENT_CONTENT_ERROR_MESSAGE);
            return;
        }   
        handlePoll(eDao.pollEntityById(getEvent(message)), message); 
    }

    public void findLastEvent(Message<JsonObject> message) { 
        if(message.body() == null){
            message.fail(Error.EVENT_CONTENT_ERROR, Error.EVENT_CONTENT_ERROR_MESSAGE);
            return;
        }   
        handlePoll(eDao.findLastEvent(getEvent(message)), message);
    }

    public void find(Message<JsonObject> message) { 
        if(message.body() == null){
            message.fail(Error.EVENT_CONTENT_ERROR, Error.EVENT_CONTENT_ERROR_MESSAGE);
            return;
        }   
        handlePoll(eDao.find(getEvent(message)), message); 
    }
}
