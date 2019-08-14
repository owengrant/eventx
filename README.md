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

Permissions Management

To add permissions use the following structure in the verticle configuration:

{
    permissions: {
        service_name: {
            events: [event1, event2] //events that a particular service can publish
            services: { //coming soon
                service_name: [event1, event2] //service and events a particular service and query
                service_name: [event1, event2]
            }
        }
    }
}