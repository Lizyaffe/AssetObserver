package com.masterface.nxt.ae;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A Java logging formatter that writes more compact output than the default
 */
public class BriefLogFormatter extends Formatter {

    /** Format used for log messages */
    private static final ThreadLocal<MessageFormat> messageFormat = new ThreadLocal<MessageFormat>() {
        @Override
        protected MessageFormat initialValue() {
            return new MessageFormat("{0,date,yyyy-MM-dd HH:mm:ss} {1}: {2}\n{3}");
        }
    };

    /**
     * Format the log record as follows:
     *
     *     Date Level Message ExceptionTrace
     *
     * @param       logRecord       The log record
     * @return                      The formatted string
     */
    @Override
    public String format(LogRecord logRecord) {
        Object[] arguments = new Object[4];
        arguments[0] = new Date(logRecord.getMillis());
        arguments[1] = logRecord.getLevel().getName();
        arguments[2] = logRecord.getMessage();
        //noinspection ThrowableResultOfMethodCallIgnored
        Throwable exc = logRecord.getThrown();
        if (exc != null) {
            Writer result = new StringWriter();
            exc.printStackTrace(new PrintWriter(result));
            arguments[3] = result.toString();
        } else {
            arguments[3] = "";
        }
        return messageFormat.get().format(arguments);
    }
}
