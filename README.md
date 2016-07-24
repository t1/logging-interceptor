# CDI logging-interceptor [![Download](https://api.bintray.com/packages/t1/javaee-helpers/logging-interceptor/images/download.svg) ](https://bintray.com/t1/javaee-helpers/logging-interceptor/_latestVersion) [![Join the chat at https://gitter.im/t1/logging-interceptor](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/t1/logging-interceptor?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

CDI interceptor for logging to slf4j.

Logging is the standard example for writing interceptors. Yet, I didn't find a good one, so I wrote my own (and it was a lot of fun :)

There are two main use-cases for logging interceptors:

* Log calls to existing methods: instead of repeating the method name and arguments within the method, simply annotate it. Often called tracing (see [example](#basic-trace)). Note that interceptors are not triggered when you do local calls, so you may need to resort to the other, more classical logging use-case:
* Instead of using the generic logging api, you can define a pojo that does nothing but log (with the same annotation as above). Makes mocking your unit tests very easy, in case you want to make sure about what logs are written. (see [example](#classic-logging))

## News ##

### Version 3.0.0 ###

We use [semantic versioning](http://semver.org). tl;dr: versions consist of three parts with a semantic: The Bad (major = breaking changes), the Good (minor = new features), and the Ugly (micro/patch = bugfixes).

So going to 3.0.0 is Bad, as it may break existing applications. But sometimes Bad things are necessary. Here we need it to get to Java 8 and replace esp. Joda-Date (BTW: big cudos to Stephen for that project).


## Features ##

* Logs to [slf4j](http://slf4j.org) (and you can go to any logging framework from there).
* Annotate methods as `@Logged` to log the call and eventually return value or exceptions thrown (both including time).
* Annotate a class to have all managed methods within logged (see [examples](#examples)).
* Annotate a CDI `@Stereotype` and have all managed methods logged.
* Define the log level in the `@Logged` annotation; if you don't specify one, it's derived from the recursively containing type or package (i.e. you can annotate your `package-info.java` and inherit the log level from there; this doesn't work for the interception itself, as CDI doesn't support that) or finally `DEBUG`.
* The exception thrown is logged with a message like `failed with IllegalArgumentException` so the stack trace isn't repeated for every method the throw passes through.
* If the last parameter is a `Throwable`, it's passed to slf4j, so the stack trace is printed out; i.e. the logging-interceptor formats the message without the `Throwable` before it passes it on (that's an addition to the slf4j api). (see [example](#log-stack-trace))
* The default logger is the top level class containing the method being logged; you can explicitly set it in the `@Logged` annotation.
* The default log message is the name of the method, with camel case converted to spaces (e.g. "getNextCustomer" -> "get next customer") and parameters appended; you can explicitly set it in the `@Logged` annotation.
* In addition to the slf4j log message format placeholders, you can use positional indexes (e.g. `{0}`) or parameter names (e.g. `{firstName}`; requires jdk8 parameter meta data or debug info). And you can use simple expressions, like `person.address.zip`.
* Parameters annotated as `@DontLog` are not logged; very useful for, e.g., passwords.
* Parameters annotated as `@LogContext` are added to the [MDC](http://slf4j.org/manual.html#mdc) (and cleaned up thereafter). Very handy to add, e.g., the main business reference key to all logs written below.
* Define producers for `LogContextVariable`s for other MDC variables; a producer for the `version` and `app` of the containing jar/ear/war is provided (requires the implementation or specification version in the manifest). As a convenience, you also can just annotate a field as `@LogContext`, so you don't have to package the field into a `LogContextVariable`.
* Add a MDC variable `indent` to your pattern to visualize the call hierarchy of logged statements.
* Define converters, to e.g. extract the customer number from a customer object, by implementing `Converter` (see [example](#converter)). Converters for `javax.ws.rs.core.UriInfo` and `javax.ws.rs.core.Response` are provided.
* Set `@Logged#json` to have some information put into an MDC variable `json`. It's a JSON map without the outer curlies; you'll have to add those to your pattern.
  * `EVENT`: the `timestamp`, `event` (the method name, _not_ converted to spaces), `logger`, and `level`.
  * `PARAMETERS`: the parameters of the method.
  * `CONTEXT`: all MDC variables (like `%X`, but with colons instead of `=` between keys and values).
  * `ALL`: for all of the above, so you can log using json with the log pattern `{%X{json}}`.
* Set `@Logged#repeat` to limit the number of log repeats, e.g. `ONCE_PER_DAY` will not repeat any calls until 24 hours have passed since the previous call.

## Examples ##

### Basic Trace ###

```java
@Path("/customers")
@Logged(level = INFO)
public class CustomersResource {
	@GET
	@Path("/{customer-id}")
	public Customer getCustomer(@PathParam("customer-id") String customerId) {
		return ...
	}
}
```

would log calls to all methods in `CustomersResource` at `INFO` level, e.g.:

```
get customer 1234
return Customer(id=1234, firstName="Joe", ...)
```

### Classic Logger ###

```java
static class CustomersResourceLogger {
	@Logged("found {} for id {}")
	void foundCustomerForId(Customer customer, String customerId) {}
}

@Inject CustomersResourceLogger log;

...
log.foundCustomerForId(customer, "1234");
...
```

would log:

```
found Customer(id=1234, firstName="Joe", ...) for id 1234
```

### Log Stack Trace ###

```java
static class ExceptionLogger {
	@Logged(level = ERROR)
	void failed(String operation, RuntimeException e) {}
}

@Inject
ExceptionLogger exceptionLogger;

...
try {
	...
} catch (RuntimeException e) {
	exceptionLogger.failed("my operation", e);
}
...
```

would log the message `failed my operation` with the exception and stack trace.

### Converter ###

```java
public class ResponseLogConverter implements Converter {
    public String convert(Response response) {
        StatusType statusInfo = response.getStatusInfo();
        return statusInfo.getStatusCode() + " " + statusInfo.getReasonPhrase() + entityInfo(response);
    }

    private String entityInfo(Response response) {
        Object entity = response.getEntity();
        if (entity == null)
            return "";
        return ": " + entity.getClass().getSimpleName();
    }
}
```

## Download ##

Add [Bintray](https://bintray.com/t1/javaee-helpers/logging-interceptor) to your `settings.xml` (see the `Set me up!` button) and this Maven dependency to your `pom.xml`:

```
<dependency>
  <groupId>com.github.t1</groupId>
  <artifactId>logging-interceptor</artifactId>
  <version>${logging-interceptor.version}</version>
</dependency>
```

## Enable in Java EE 7 ##

The interceptor is annotated with a @Priority (see the last paragraph in [Java EE Tutorial](http://docs.oracle.com/javaee/7/tutorial/doc/cdi-adv006.htm)). So it is automatically activated if you add it as a library to you application.

## Enable in Java EE 6 ##

Enabling interceptors from a library jar is a bit tricky in CDI 1.0. If you'd just add the logging-interceptor.jar to your war or ear, it was a separate CDI module, and the interceptor was not useable in your application. So you'll have to overlay the jar contents into your application by adding this to your pom.xml:

```xml
<build>
	<plugins>
		<plugin>
			<groupId>org.apache.maven.plugins</groupId>
			<artifactId>maven-war-plugin</artifactId>
			<version>2.4</version>
			<configuration>
				<overlays>
					<overlay>
						<groupId>com.github.t1</groupId>
						<artifactId>logging-interceptor</artifactId>
						<type>jar</type>
						<targetPath>WEB-INF/classes</targetPath>
					</overlay>
				</overlays>
			</configuration>
		</plugin>
	</plugins>
</build>

<dependencies>
	<dependency>
		<groupId>com.github.t1</groupId>
		<artifactId>logging-interceptor</artifactId>
		<optional>true</optional>
	</dependency>
</dependencies>
```

Then you can activate the interceptor in the application's `beans.xml`:

```xml
<beans xmlns="http://java.sun.com/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/beans_1_0.xsd">
	<interceptors>
		<class>com.github.t1.log.LoggingInterceptor</class>
	</interceptors>
</beans>
```

# License

Licensed under [Apache License 2.0](http://www.spdx.org/licenses/Apache-2.0)
