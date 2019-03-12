package com.geoideas.eventx.service.publisher.database.dao;

import com.geoideas.eventx.shared.EventDTO;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.reactivex.ext.sql.SQLClient;
import io.vertx.reactivex.ext.sql.SQLConnection;

import java.util.stream.Collectors;

public class EventDAO {
    public final String APPEND = "INSERT INTO EVENT(hash,event,eventType,entity,entityId,version,data) VALUES(?,?,?,?,?,?,?::json)";
    public final String APPEND_OCC = "INSERT INTO EVENT(hash,event,eventType,entity,entityId,version,data,revision) VALUES(?,?,?,?,?,?,?::json,?)";
    public final String POLL = "SELECT * FROM EVENT WHERE eventId >= ?";
    public final String POLL_EVENT = "SELECT * FROM EVENT WHERE eventId >= ? AND event = ?";
    public final String POLL_ENTITY = "SELECT * FROM EVENT WHERE eventId >= ? AND entity = ?";
    public final String POLL_ENTITY_EVENT = "SELECT * FROM EVENT WHERE eventId >= ? AND event = ? AND entity = ?";
    public final String POLL_ENTITY_BY_ID = "SELECT * FROM EVENT WHERE  entityId = ? AND entity = ?";
    public final String FIND_LAST_EVENT = "SELECT * FROM EVENT WHERE entityId = ? AND entity = ? ORDER BY eventId DESC LIMIT 1";
    public final String FIND = "SELECT * FROM EVENT WHERE entity = ? AND hash = ?";

    private  SQLClient client;

    public  EventDAO(SQLClient client){
        this.client = client;
    }


    public Single<EventDTO> publishOCC(EventDTO newEvent, EventDTO oldEvent){
        return findLastEvent(oldEvent)
               .flatMap(event -> {
                   var result = Single.just(new EventDTO());
                   if(event.isEmpty())
                       result = append(newEvent,false);
                   else{
                       var last = event.getJsonObject(0);
                       var lastRevision = last.getInteger("revision");
                       var oldHash = last.getString("hash");
                       if(oldHash.equals(oldEvent.getHash())) {
                           newEvent.setRevision(lastRevision+1);
                           result = append(newEvent,true);
                       }
                   }
                   return result;
               }) ;
    }

    public Single<EventDTO> publish(EventDTO event){
        return append(event, false);
    }

    public Single<EventDTO> append(EventDTO event, boolean occ){
        var query = APPEND;
        var params = new JsonArray()
                     .add(event.getHash())
                     .add(event.getEvent())
                     .add(event.getEventType())
                     .add(event.getEntity())
                     .add(event.getEntityId())
                     .add(event.getVersion())
                     .add(event.getData().encode());
        if(occ){
            query = APPEND_OCC;
            params.add(event.getRevision());
        }
        var query1 = query;
        return connect()
               .flatMap(con ->
                   con.rxUpdateWithParams(query1,params)
                   .doAfterTerminate(con::close)
               ).map(result -> result.getUpdated() == 0 ? new EventDTO() : event);
    }

    public Single<JsonArray> query(String sql, JsonArray params){
        return connect()
                .flatMap(con ->
                        con.rxQueryWithParams(sql, params)
                                .doAfterTerminate(con::close)
                )
                .map(ResultSet::getRows)
                .flatMap(list -> Single.just(
                                    list.stream()
                                        .map(d -> d.put("data",new JsonObject(d.getString("data"))))
                                        .collect(Collectors.toList()))
                                    )
                .map(JsonArray::new);
    }

    public Single<JsonArray> poll(EventDTO event){
        return query(POLL, new JsonArray().add(event.getEventId()));
    }

    public Single<JsonArray> pollEvent(EventDTO event){
        var params = new JsonArray()
                         .add(event.getEventId())
                         .add(event.getEvent());
        return query(POLL_EVENT,params);
    }

    public Single<JsonArray> pollEntity(EventDTO event){
        var params = new JsonArray()
                         .add(event.getEventId())
                         .add(event.getEntity());
        return query(POLL_ENTITY, params);
    }

    public Single<JsonArray> pollEntityEvent(EventDTO event){
        var params = new JsonArray()
                         .add(event.getEventId())
                         .add(event.getEvent())
                         .add(event.getEntity());
        return query(POLL_ENTITY_EVENT, params);
    }

    public Single<JsonArray> pollEntityById(EventDTO event){
        var params = new JsonArray()
                         .add(event.getEntityId())
                         .add(event.getEntity());
        return query(POLL_ENTITY_BY_ID, params);
    }

    public Single<JsonArray> findLastEvent(EventDTO event){
        var params = new JsonArray()
                         .add(event.getEntityId())
                         .add(event.getEntity());
        return query(FIND_LAST_EVENT,params);
    }

    public Single<JsonArray> find(EventDTO event){
        return query(FIND, new JsonArray().add(event.getEntity()).add(event.getHash()));
    }

    public Single<SQLConnection> connect(){
        return client.rxGetConnection();
    }
}
