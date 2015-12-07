package com.github.t1.log;

import java.util.List;

import javax.enterprise.inject.Instance;

import org.slf4j.Logger;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@RequiredArgsConstructor
class LogPointContext {
    private final Instance<LogContextVariable> logContextVariables;
    private final Converters converters;

    private Logger logger;
    private LogLevel level;
    private String message;

    private List<LogArgument> logArguments;
    private List<FieldLogVariableProducer> fieldLogContexts;

    private boolean shouldLogResult;
    private boolean shouldLogResultValue;
    private RepeatController repeatController;
}
