package com.geoideas.eventx.service.publisher;

import com.geoideas.eventx.service.publisher.database.dao.EventDAO;
import com.geoideas.eventx.service.publisher.eventhandler.EventbusHandler;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.asyncsql.PostgreSQLClient;

public class PublisherVerticle extends AbstractVerticle {

    public static final String PUBLISH = "/db/publish";
    public static final String PUBLISH_OCC = "/db/publish/occ";
    public static final String POLL = "/db/poll";
    public static final String POLL_EVENT = "/db/poll/event";
    public static final String POLL_ENTITY = "/db/poll/entity";
    public static final String POLL_ENTITY_EVENT = "/db/poll/entity/event";
    public static final String POLL_ENTITY_BY_ID = "/db/poll/entity/id";
    public static final String POLL_CONTEXT = "/db/poll/context";
    public static final String FIND_LAST_EVENT = "/db/poll/entity/event/last";
    public static final String FIND = "/db/poll/entity/hash";

    @Override
    public void start() {
        setup();
        System.out.println("EVENTX PUBLISHER SERVICE RUNNING");
    }

    public void setup(){
        var db = PostgreSQLClient.createShared(vertx, config().getJsonObject("database"));
        var eDao = new EventDAO(db);

        var busHandler = new EventbusHandler(eDao);
        var bus = vertx.eventBus();
        var root = config().getString("address");
        bus.localConsumer(root+PUBLISH, busHandler::publish);
        bus.localConsumer(root+PUBLISH_OCC, busHandler::publishOCC);
        bus.localConsumer(root+POLL, busHandler::poll);
        bus.localConsumer(root+POLL_EVENT, busHandler::pollEvent);
        bus.localConsumer(root+POLL_ENTITY, busHandler::pollEntity);
        bus.localConsumer(root+POLL_ENTITY_EVENT, busHandler::pollEntityEvent);
        bus.localConsumer(root+POLL_ENTITY_BY_ID, busHandler::pollEntityById);
        bus.localConsumer(root+POLL_CONTEXT, busHandler::pollContext);
        bus.localConsumer(root+FIND_LAST_EVENT, busHandler::findLastEvent);
        bus.localConsumer(root+FIND, busHandler::find);
    }
}
