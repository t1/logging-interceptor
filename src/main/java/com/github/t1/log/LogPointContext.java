package com.github.t1.log;

import lombok.*;
import lombok.experimental.Accessors;
import org.slf4j.Logger;

import javax.enterprise.inject.Instance;
import java.util.List;

@Data
@Accessors(fluent = true)
@RequiredArgsConstructor
class LogPointContext {
    private final Instance<LogContextVariable> logContextVariables;
    private final Converters converters;

    private Logger logger;
    private LogLevel level;
    private String messageFormat;

    private List<LogArgument> logArguments;
    private List<FieldLogVariableProducer> fieldLogContexts;

    private boolean voidMethod;
    private String returnFormat;
    private RepeatController repeatController;
}
