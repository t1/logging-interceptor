# logging-interceptor #

CDI interceptor for logging to slf4j

Logging is the standard example for writing interceptors. Yet, I didn't find a good one, so I wrote my own.

There are two main use-cases for logging interceptors:

* Log calls to existing methods: instead of repeating the method name and arguments within the method, simply annotate it. Often called tracing.
* Instead of using the generic logging api, define a pojo that does nothing but log. Makes mocking your unit tests very easy, in case you want to make sure about what logs are written.

## Features ##

* Log to [slf4j](http://slf4j.org) (and you can go anywhere from there).
* Annotate methods, a type, or the package (package-info.java) for wider scopes.
* Default log level is DEBUG, change in the annotation.
* Default logger is the top level class containing the method being logged; change in the annotation.
* Default log message is the name of the method, converted camel case to spaces (e.g. "getNextCustomer" -> "get next customer"); parameters appended.
* Log return value, if not void.
* Parameters annotated as @LogContext are added to the [MDC](http://slf4j.org/manual.html#mdc) (and cleaned up thereafter). Very handy for adding the main business reference key to all sub-log-statements. You can also define a converter, if necessary to e.g. extract the customer number from a customer object.
* Define producers for LogContextVariables for other MDC variables; a producer for the version is provided.

