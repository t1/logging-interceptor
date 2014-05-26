# CDI logging-interceptor #

CDI interceptor for logging to slf4j. [ ![Download](https://api.bintray.com/packages/t1/javaee-helpers/logging-interceptor/images/download.png) ](https://bintray.com/t1/javaee-helpers/logging-interceptor/_latestVersion)

Logging is the standard example for writing interceptors. Yet, I didn't find a good one, so I wrote my own (and it was fun :)

There are two main use-cases for logging interceptors:

* Log calls to existing methods: instead of repeating the method name and arguments within the method, simply annotate it. Often called tracing. (see [example](#basic-trace))
* Instead of using the generic logging api, you can define a pojo that does nothing but log (with the same annotation as above). Makes mocking your unit tests very easy, in case you want to make sure about what logs are written. (see [example](#classic-logging))

Note that interceptors are not triggered when you do local calls.

## Features ##

* Log to [slf4j](http://slf4j.org) (and you can go to any logging framework from there).
* Annotate methods as `@Logged` to log the call and eventually return value or exceptions thrown; or a class to have all methods within logged (see [examples](#examples)).
* Define the log level in the `@Logged` annotation; if you don't specify one, it's derived from the recursively containing type or finally `DEBUG`.
* The exception thrown is logged with a message like `failed with IllegalArgumentException` so the stack trace isn't repeated for every method the throw passes through.
* If the last parameter is a `Throwable`, it's passed to slf4j, so the stack trace is printed out; i.e. the logging-interceptor formats the message without the `Throwable` before it passes it on (that's an addition to the slf4j api). (see [example](#log-stack-trace))
* Default logger is the top level class containing the method being logged; you can explicitly set it in the `@Logged` annotation.
* Default log message is the name of the method, with camel case converted to spaces (e.g. "getNextCustomer" -> "get next customer") and parameters appended; you can change it in the `@Logged` annotation.
* Parameters annotated as `@DontLog` are not logged; very useful for, e.g., passwords.
* Parameters annotated as `@LogContext` are added to the [MDC](http://slf4j.org/manual.html#mdc) (and cleaned up thereafter). Very handy to add, e.g., the main business reference key to all logs written below.
* Define producers for `LogContextVariables` for other MDC variables; a producer for the `version` and `app` of the containing jar/ear/war is provided (requires the implementation or specification version in the manifest).
* Add a MDC variable `indent` to your pattern to visualize the call hierarchy of logged statements.
* Define converters, to e.g. extract the customer number from a customer object, by implementing `Converter`. Converters for `javax.ws.rs.core.UriInfo` and `javax.ws.rs.core.Response` are provided.

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
@ConverterType(Response.class)
public class ResponseLogConverter implements Converter {
    @Override
    public String convert(Object o) {
        Response response = (Response) o;
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

I'd like to have the `Converter` interface be generic, but I don't know how to inject those :-/

## Download ##

Add [Bintray](https://bintray.com/t1/javaee-helpers/logging-interceptor) to your `settings.xml` (see the `Set me up!` buton) and this Maven dependency to your `pom.xml`:

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
					<overlay>
						<groupId>com.github.t1</groupId>
						<artifactId>stereotype-helper</artifactId>
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
	<dependency>
		<groupId>com.github.t1</groupId>
		<artifactId>stereotype-helper</artifactId>
		<optional>true</optional>
	</dependency>
</dependencies>
```

Then you can activate the interceptor in the application's `breans.xml`:

```xml
<beans xmlns="http://java.sun.com/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/beans_1_0.xsd">
	<interceptors>
		<class>com.github.t1.log.LoggingInterceptor</class>
	</interceptors>
</beans>
```
