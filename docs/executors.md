# NyQL Executors

When you invoke the `execute` method from a _NyQLInstance_, framework uses the configured
executor to execute the script after the parsing. 
Note that only _**one**_ executor can active for an instance.

By default, NyQL provides two executors.
 * **QJDBCExecutor** : Executes scripts using the configured jdbc driver.
   * Associated Factory: `com.virtusa.gto.nyql.engine.impl.QJdbcExecutorFactory`
 * **QDummyExecutor** : Just a standing executor when no executor is configured and user
 has no intention of executing a script.
   * Associated Factory: `com.virtusa.gto.nyql.engine.impl.QDummyExecFactory`

**Note:** When you are specifying an executor in configuration file, you should 
specify the factory class, not the implementation of above executor class.

### Custom Executors

Sometimes you may not be happy with the default executors (along with pools) 
and their implementations, mainly due to application's non-functional 
requirements, such as, application may already be using another
JDBC connection pool like Spring, EBean, etc, or some customization issues. 
By default, NyQL provides two most common and matured jdbc pools 
out-of-the-box for your application. They are Hikari and C3P0. 
But sometimes these might not be the solutions for you.

In that case, NyQL provides an extension point to plug application-specific 
executors to the NyQL engine. Say that your application uses Hibernate for 
example. So, you want to reuse the connection pool used by Hibernate for 
executing you custom NyQL scripts. And this is easily provided by the NyQL, 
and all you have to do is to implement a executor factory and specify it in 
your NyQL configuration file as default executor.

### Defining a New Executor Factory
* Implement a class with interface `QExecutorFactory`

```java
public class HibernateExecutorFactory implements QExecutorFactory {
        // ... override all methods
}
```

* There are four methods to implement from above interface.
   * __init(Map)__ : Called only once in NyQL lifecycle to initialize the corresponding factory before being called by any other script. Input map contains all the configurations specified in the NyQL json configuration file. Your implementation may use these information accordingly.
  * __create()__ : This method will be called for each _query_, unless the query is being executed through a script. The executor will be discarded as soon as the query execution completed.
  * __createReusable()__ : This method will be called when a new script is being called for the first time. And here the executor will __not__ be discarded until all the script and its child scripts are completed. __Note:__ All the child scripts which runs through the main script will be used this same executor in its execution lifecycle. It means the executor returned by this method has a probability of having a longer duration than the executor returned by `create()` method.
  * __shutdown()__ : Called only once in the NyQL lifecycle to shutdown the execution factory and release all resources. It is very important to implement this method correctly as if you do not so, you may create whole lot of memory leaks in your application.

* Now, specify the full class name of the implemented factory in `nyql.json` file under the section `executors` with a unique name.

```json
"executors": [
    ...

    {
        "name": "hibernate",
        "factory": "<full-class-name-to-your-factory-class>",

        "config-1": "value-1",
        "config-2": "value-2",
        ...
    },

    ...
]
```

* Here in the above example, the values `config-1` and `config-2` or any other values will be forwarded to the factory's `init(Map)` method as contents of the input map.

* Set the default executor as the same name given to your custom executor (in this case `hibernate`). So, application will use it for query executions.
 
```json
    "defaultExecutor": "hibernate"
```
