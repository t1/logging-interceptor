package com.github.t1.log;

import java.util.List;

import javax.enterprise.inject.Instance;

import lombok.*;
import lombok.experimental.Accessors;

import org.slf4j.Logger;

@Data
@Accessors(fluent = true)
@RequiredArgsConstructor
class LogPointContext {
    private final Instance<LogContextVariable> logContextVariablesProducer;
    private final Converters converters;

    private Logger logger;
    private LogLevel level;
    private String message;

    private List<LogArgument> logArguments;
    private List<FieldLogVariableProducer> fieldLogContexts;

    private boolean logResult;
    private RepeatController repeatController;
}
