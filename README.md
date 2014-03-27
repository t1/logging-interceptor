# logging-interceptor #

CDI interceptor for logging to slf4j.

Logging is the standard example for writing interceptors. Yet, I didn't find a good one, so I wrote my own (and it was fun :)

There are two main use-cases for logging interceptors:

* Log calls to existing methods: instead of repeating the method name and arguments within the method, simply annotate it. Often called tracing.
* Instead of using the generic logging api, you can define a pojo that does nothing but log (with the same annotation as above). Makes mocking your unit tests very easy, in case you want to make sure about what logs are written.

Note that interceptors are not triggered when you do local calls.

## Features ##

* Log to [slf4j](http://slf4j.org) (and you can go to any logging framework from there).
* Annotate methods as @Logged to log the call and eventually return value or exceptions thrown; or a class to have all methods within logged.
* Default log level is DEBUG; change it in the @Logged annotation.
* Default logger is the top level class containing the method being logged; change it in the @Logged annotation.
* Default log message is the name of the method, converted camel case to spaces (e.g. "getNextCustomer" -> "get next customer"); parameters appended; change it in the @Logged annotation.
* Parameters annotated as @LogContext are added to the [MDC](http://slf4j.org/manual.html#mdc) (and cleaned up thereafter). Very handy for adding the main business reference key to all logs written below.
* Define producers for LogContextVariables for other MDC variables; a producer for the version of the containing jar/ear/war is provided (requires the implementation or specification version in the manifest).
* Define converters, to e.g. extract the customer number from a customer object, by implementing LogConverter.
 
## Enable in Java EE 6 ##

If you'd just add the logging-interceptor.jar to your war or ear, it became a separate CDI module, and the interceptor was not useable in your application. So you'll have to overlay the jar contents into your application by adding this to your pom.xml:

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
			<version>1.1</version>
			<optional>true</optional>
		</dependency>
	</dependencies>

Then you can activate the interceptor in the application's breans.xml:

	<beans xmlns="http://java.sun.com/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/beans_1_0.xsd">
		<interceptors>
			<class>com.github.t1.log.LoggingInterceptor</class>
		</interceptors>
	</beans>

## Enable in Java EE 7 ##

The interceptor is annotated with a @Priority (see the last paragraph in [Java EE Tutorial](http://docs.oracle.com/javaee/7/tutorial/doc/cdi-adv006.htm)). So it is automatically activated if you add it as a library to you application.
