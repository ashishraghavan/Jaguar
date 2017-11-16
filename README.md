Jaguar is a Java based e-commerce web application with Android to be used on the front-end. The following tools/technologies are being used to develop Jaguar.
Spring 
Hibernate (as an ORM tool)
Maven (for the build process)
Jersey (as the REST framework)
PostgresSQL (as the Database)
Mailgun (as the email provider)
PLivo (as the SMS provider)

The project consists of two modules : ObjectModel containing all the model classes (Account, User, Device, Product, Category, Filter, FilterValue etc.) and a client-api, which consists of all service classes and has a dependency on the ObjectModel. The spring-context.xml resides in the ObjectModel along with the Data Access Object layer implementation. Both the modules use TestNG as the unit testing framework. The client-api module consists of TestNG based integration tests.

The setup consists of a main pom.xml with the modules ObjectModel and client-api extending these pom's. The hibernate3-maven-plugin is used to generate ddl statements using the persistence.xml file, the surefire plugin is used for generating reports during the execution of the unit and integration tests. All model classes are annotated with @Entity so that hibernate recognizes them as a POJO to be mapped. Spring is used as the dependency injection and transaction management framework. A spring-config.xml is being used to specify all the required beans. c3p0 is used as a pooled data source, HibernateJpaVendorAdapter is used as the jpa adapter and spring JpaTransactionManager is used as the transaction manager. All model classes created consist of unit tests. The client-api consists of an OAuthFilter for implementing restricted access to all protected resources, the default jersey Servlet container handles all rest requests whereas a FileServlet routes all requests to static resources preserving the rest api path.

OAUTH mechanism is being used for authorization. A token is device based and authorization is required for each new device added to a user. All devices are mapped to a user and for each additional device added, the user will be notified via email or SMS with the option of revoking the (added) device.

Google guava is used as a timed storage mechanism for authentication tokens because of its simplicity and the fact that objects need not be serialized before storage. Redis is intended to be used in the future so that server restarts preserve all timed tokens.


The e-commerce part of this project is comprised of the model classes Product, Category, Filter, FilterValues, Image and Cart (to be implemented). All products belong to a specific category. Each category has child categories and a parent category. Categories with no parent category are supposed to be the top level categories. For example Phones is a top level category with Cell Phones and Landline phones as its child categories. Further Cell phone cases is sub-category of Cell Phones. So the relationship is Phones -> Cell Phones -> Cell phone cases. So, Cell Phones is one of the child categories of Cell Phones and Cell phone cases is a child category of Cell Phones. On the other hand, Cell Phones is the parent category of Cell phone cases and Phones is the parent category of Cell Phones. 

```
@JsonIgnore
    @OneToMany(targetEntity = Category.class,cascade = CascadeType.ALL,fetch = FetchType.EAGER,mappedBy = "parentCategory",orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ICategory> childCategories;

    @JsonIgnore
    @OneToMany(targetEntity = Filter.class,cascade = CascadeType.ALL,fetch = FetchType.LAZY,mappedBy = "category",orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<IFilter> filters;

    @JsonIgnore
    @ManyToOne(targetEntity = Category.class,cascade = CascadeType.ALL)
    @JoinColumn(name = "parent_category_id")
    @org.hibernate.annotations.ForeignKey(name = "fk_parent_category_id")
    private ICategory parentCategory;
