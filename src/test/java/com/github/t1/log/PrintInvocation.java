package com.github.t1.log;

import static org.mockito.Mockito.*;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.*;

public class PrintInvocation implements Answer<Void> {
    public static Stubber doPrint() {
        return doAnswer(new PrintInvocation());
    }

    @Override
    public Void answer(InvocationOnMock invocation) {
        StringBuilder out = new StringBuilder();
        out.append(invocation.getMethod().getName());
        out.append("(");
        boolean first = true;
        for (Object argument : invocation.getArguments()) {
            if (first)
                first = false;
            else
                out.append(", ");
            out.append(argument);
        }
        out.append(")");
        System.out.println(out);
        return null;
    }
}
