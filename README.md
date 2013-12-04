# logging-interceptor #

CDI interceptor for logging to slf4j.

Logging is the standard example for writing interceptors. Yet, I didn't find a good one, so I wrote my own (and it was fun :)

There are two main use-cases for logging interceptors:

* Log calls to existing methods: instead of repeating the method name and arguments within the method, simply annotate it. Often called tracing.
* Instead of using the generic logging api, you can define a pojo that does nothing but log (with the same annotation as above). Makes mocking your unit tests very easy, in case you want to make sure about what logs are written.

Note that interceptors are not triggered when you do local calls.

## Features ##

* Log to [slf4j](http://slf4j.org) (and you can go to any logging framework from there).
* Annotate methods as @Logged to log the call and eventually return value or exceptions thrown; or a type or a complete package (package-info.java) for wider scopes.
* Default log level is DEBUG; change it in the @Logged annotation.
* Default logger is the top level class containing the method being logged; change it in the @Logged annotation.
* Default log message is the name of the method, converted camel case to spaces (e.g. "getNextCustomer" -> "get next customer"); parameters appended; change it in the @Logged annotation.
* Parameters annotated as @LogContext are added to the [MDC](http://slf4j.org/manual.html#mdc) (and cleaned up thereafter). Very handy for adding the main business reference key to all logs written below. You can also define a converter, if necessary to e.g. extract the customer number from a customer object.
* Define producers for LogContextVariables for other MDC variables; a producer for the version of the containing jar/ear/war is provided (requires the implementation or specification version in the manifest).

## Enable ##

You'll have to activate the interceptor in the breans.xml:

	<beans xmlns="http://java.sun.com/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/beans_1_0.xsd">
		<interceptors>
			<class>com.github.t1.log.LoggingInterceptor</class>
		</interceptors>
	</beans>

