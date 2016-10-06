# NyQL

A common query DSL for popular relational databases.

#### Terminology:
* Query Repository: A folder containing all your query scripts.
* Mapper: An instance responsible of mapping an identifier to a valid source which is consumable by any repository.
* QResultProxy: Contains a database specific generated query with parameters in correct order
* QScript: Executable entity contains a QResultProxy instance with relevant data
* Executor: Entity which executes script(s)

#### How to Use Through Java
* Add the dependency `nyql-engine` to the maven project, and also corresponding nyql database translator.

```xml
<dependency>
    <groupId>com.virtusa.gto.insight.nyql</groupId>
    <artifactId>nyql-engine</artifactId>
    <version>${nyql.version}</version>
</dependency>
```

If you are going to use `mysql` then add `nyql-impl-mysql` dependency to the classpath.

```xml
<dependency>
    <groupId>com.virtusa.gto.insight.nyql</groupId>
    <artifactId>nyql-impl-mysql</artifactId>
    <version>${nyql.version}</version>
</dependency>
```

* Make sure to have correct driver classes in your classpath at the runtime through maven dependencies. For eg, if you are using mysql database use mysql jdbc driver.
 ```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.36</version>
</dependency>
```

* NyQL uses a configuration json file in the classpath to configure automatically. See a sample [nyql.json](nyql.json). It has below properties.
    * **translators**: an array of fully qualified class names of db factories. These will be loaded at the beginning and will throw exception if not found. So make sure you specify only you needed.
    * **activate**: the active database implementation name. This must be equal to a name returned by any translator.
    * **caching**: 
      * **compiledScripts**: Whether to cache the groovy compiled scripts or not. Recommended to set this `true`.
      * **generatedQueries**: Whether to cache generated queries by NyQL. Then you can have fine tune by specifying a cache status for each script using `do_cache=true` declaration in very top of scripts you want to cache. Recommended to set this `true`.
    * **executors**: List of executors for query execution. Each executor should declare below properties.
      * **name**: name of the executor. Should be unique.
      * **factory**: factory class which creates executors at runtime for each session.
      * **url**: JDBC url
      * **username**: database username
      * **password**: database password
      * **pooling**: NyQL uses HikariCP for JDBC connection pooling. And you can specify those configurations here as a JSON object. See their [site](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby) for available configurations


3. If you want to see the db specific query, then use below code piece.

```java 
public class Main {
    
    public static void main(String[] args) throws Exception {
        // configure NyQL somewhere in starting point
        // of your application lifecycle.
        NyQL.configure(jsonFile);
        
        try {
            // your session variables or parameter values...
            Map<String, Object> data = new HashMap<>();
            
            // to parse and see the query
            NyQL.parse("<script-name>", data);
            
            // to execute and get results
            NyQL.execute("<script-name>", data);
            
        } finally {
            // call shutdown at the end of your application.
            NyQL.shutdown();
        }
    }
}
```
