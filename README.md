Jaguar is a Java based e-commerce web application with Android to be used on the front-end. The following tools/technologies are being used to develop Jaguar.
Spring 
Hibernate (as an ORM tool)
Maven (for the build process)
Jersey (as the REST framework)
PostgresSQL (as the Database)
Mailgun (as the email provider)
PLivo (as the SMS provider)

The project consists of two modules : ObjectModel containing all the model classes (Account, User, Device, Product, Category, Filter, FilterValue etc.) and a client-api, which consists of all service classes and has a dependency on the ObjectModel. The spring-context.xml resides in the ObjectModel along with the Data Access Object layer implementation.
OAUTH mechanism is being used for authorization. A token is device based and authorization is required for each new device added to a user. All devices are mapped to a user and for each additional device added, the user will be notified of this via email or SMS with the option of revoking the device.
