package com.geoideas.eventx.service.publisher.eventhandler;

import com.geoideas.eventx.service.publisher.database.dao.EventDAO;
import io.pet.spooch.event.shared.dto.EventDTO;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;

public class EventbusHandler {

    private EventDAO eDao;

    public EventbusHandler(EventDAO eDao){
        this.eDao = eDao;
    }

    public EventDTO getEvent(Message<JsonObject> message){
        return new EventDTO().fromJson(message.body());
    }

    public void handlePoll(Single<JsonArray> action, Message<JsonObject> message){
        action.doOnError(e -> { message.fail(0,""); e.printStackTrace(); })
              .subscribe(events -> message.reply(events));
    }

    public void publish(Message<JsonObject> message){
        var event = getEvent(message);
        eDao.publish(event)
            .doOnError(e -> { message.fail(0,""); e.printStackTrace(); })
            .subscribe(v -> message.reply(v.toJson()), e -> { message.fail(0,""); e.printStackTrace(); });
    }

    public void publishOCC(Message<JsonObject> message){
        var body = message.body();
        var newEvent = new EventDTO().fromJson(body.getJsonObject("newEvent"));
        var oldEvent = new EventDTO().fromJson(body.getJsonObject("oldEvent"));
        eDao.publishOCC(newEvent, oldEvent)
            .doOnError(e -> { message.fail(0,""); e.printStackTrace(); })
            .subscribe(v -> message.reply(v.toJson()));
    }

    public void poll(Message<JsonObject> message){
        handlePoll(eDao.poll(getEvent(message)), message);
    }

    public void pollEvent(Message<JsonObject> message){
        handlePoll(eDao.pollEvent(getEvent(message)), message);
    }

    public void pollEntity(Message<JsonObject> message){
        handlePoll(eDao.pollEntity(getEvent(message)), message);
    }

    public void pollEntityEvent(Message<JsonObject> message){ handlePoll(eDao.pollEntityEvent(getEvent(message)), message); }

    public void pollEntityById(Message<JsonObject> message) { handlePoll(eDao.pollEntityById(getEvent(message)), message); }

    public void findLastEvent(Message<JsonObject> message) { handlePoll(eDao.findLastEvent(getEvent(message)), message); }

    public void find(Message<JsonObject> message) { handlePoll(eDao.find(getEvent(message)), message); }
}
