EventX is a centralisd event sourcing microservice.

Features:

1) Optimistic Concurrency Control
2) Event publish and query permissions management
3) Flexible event searching

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