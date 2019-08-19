# EventX

###### EventX is a microservice which provides event sourcing services. EventX is written using Vertx and exposes services via the Vertx eventbus. Easy integration is provided via Service Discovery and Service Proxies.
#### EventX is NOT does not provide event streaming services.

## Features

* ###### Optimistic Concurrent Control - EventX can gaurantee the order in which events (transformations) on a specified entity are stored. This is done using by presisting an event about an entity only if the last event on that entity has a given hash signature. 
* ###### Multitenancy -  Each event must have a context (source application anme), entity name and event name. This allows EventX to be used to store events from multiple applications within on database while maintaining separation of events across applications. 
* ###### Publishing Permissions - Each application publishing events to EventX must be registered in the EventX configuration. This allows EventX to restrict and isolate which events authorized application can publish. This improves data integrity in the multitenancy environment by preventing multiple services from having the same name and publishing the same events.
* ###### Read Ordering - Events are always read in the same order they were stored.

## Operations


* ###### Publish - Publishes event to EventX
* ###### PublishOCC - Publishes event to EventX using Optimistic Concurrency
* ###### Poll - Reads all events from a given event id
* ###### Poll Event - Reads all events of a given type from a givent event id
* ###### Poll Entity - Reads all events of a given
* ###### Poll Entity By Id - Reads all events of a specified instance of an entity
* ###### Poll Context - Reads all events from a given context
* ###### Find Last Event - Finds the last event of a given entity

## Examples

#### Getting Service Proxy via Service Discovery

	var sd = ServiceDiscovery.create(vertx)
    sd.getRecord(rec -> rec.getName().equals("com.geoideas.eventx"), hndlr -> {
      if(hndlr.succeeded()) {
        EventService eventx = sd.getReference(hndlr.result()).get();
      }
    });

#### Getting Service From Eventx-Shared Library

	EventService eventx = EventService.create(vertx, "service:com.geoideas.eventx");

#### Publishing Events

	var event = new EventDTO()
    	.setContext("tester.test")
        .setEvent("add_grade")
        .setEventType("insert")
        .setEntity("test_1")
        .setEntityId("1234456789qwertyuiplkjhgfdsazxcv") // at least 32 characters
        .setData(new JsonObject())
        .setVersion(1)
        .setRevision(1);
    eventx.publish(event.toJson(), hndlr -> {
      if(hndlr.succeeded())
      	System.out.println(hndlr.result().encode());
    });
	
### Polling Events

	eventx.poll(0, hndlr -> {
    	if(hndlr.succeeded())
        	hndlr.result().stream()
            	.map(e -> (JsonObject))
            	.map(EventDTO::fromJson)
                .forEach(System.out::println)
    });

### Publishing Permissions
##### Permissions are added to the verticle configuration

	{
  		"database": {"host": "localhost","port": 5432,"database": 	"eventx","username": 	"postgres","password": "postgres"},
	  	"serviceName": "com.geoideas.eventx",
  		"address": "service:com.geoideas.eventx",
  		"permissions": {
        	"com.geoideas.eventx.tester": {
            	"events": ["com.geoideas.eventx.tester:TEST"]
       		}
  		}
	}
* Keys of the "permissions" object represent the names of applications allowed to publish events.
	* the "events" arrays holds the names of events which can be published by the current application.
    
    
   ##### To ensure consistency and isolation of event across multiple clients the full name of the application owning the event must be prepended to the event name
