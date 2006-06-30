/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.spi;

import java.io.File;
import java.net.URL;
import org.apache.tools.ant.module.run.LoggerTrampoline;
import org.openide.windows.OutputListener;

/**
 * Represents one Ant build session, possibly consisting of multiple targets,
 * subprojects, and so on.
 * A session may be shared by several {@link AntLogger}s.
 * @author Jesse Glick
 * @since org.apache.tools.ant.module/3 3.12
 */
public final class AntSession {

    static {
        LoggerTrampoline.ANT_SESSION_CREATOR = new LoggerTrampoline.Creator() {
            public AntSession makeAntSession(LoggerTrampoline.AntSessionImpl impl) {
                return new AntSession(impl);
            }
            public AntEvent makeAntEvent(LoggerTrampoline.AntEventImpl impl) {
                throw new AssertionError();
            }
            public TaskStructure makeTaskStructure(LoggerTrampoline.TaskStructureImpl impl) {
                throw new AssertionError();
            }
        };
    }
    
    private final LoggerTrampoline.AntSessionImpl impl;
    private AntSession(LoggerTrampoline.AntSessionImpl impl) {
        this.impl = impl;
    }
    
    /**
     * Get the Ant script originally invoked.
     * Note that due to subproject support some events may come from other scripts.
     * @return the Ant script which was run to start with
     */
    public File getOriginatingScript() {
        return impl.getOriginatingScript();
    }
    
    /**
     * Get the Ant targets originally run.
     * @return a list of one or more targets (but may be empty during {@link AntLogger#buildInitializationFailed})
     */
    public String[] getOriginatingTargets() {
        return impl.getOriginatingTargets();
    }
    
    /**
     * Get optional data stored by the logger in this session.
     * @param logger the logger which wishes to retrieve data
     * @return any optional data, or null initially
     */
    public Object getCustomData(AntLogger logger) {
        return impl.getCustomData(logger);
    }
    
    /**
     * Store custom data associated with this session.
     * May be used by the logger to keep some information that will persist
     * for the lifetime of the session.
     * @param logger the logger which wishes to store data
     * @param data some custom data to retain
     */
    public void putCustomData(AntLogger logger, Object data) {
        impl.putCustomData(logger, data);
    }
    
    /**
     * Print a line of text to the Ant output.
     * @param message a message to print (newline will be appended automatically)
     * @param err true to send to the error stream, false for regular output
     * @param listener an output listener suitable for hyperlinks, or null for a plain print
     * @see #createStandardHyperlink
     */
    public void println(String message, boolean err, OutputListener listener) {
        impl.println(message, err, listener);
    }
    
    /**
     * Deliver a message logged event to all matching loggers.
     * <p>
     * Loggers will receive {@link AntLogger#messageLogged} with an event
     * similar to the original event except for the message and log level;
     * also the exception will always be null and the event will initially
     * be unconsumed.
     * </p>
     * <p>
     * This call blocks until all loggers have processed the nested event.
     * Note that this logger may also receive the event so it must be reentrant.
     * </p>
     * <p class="nonnormative">
     * Loggers are discouraged from using this facility merely to create hyperlinks
     * for which the target is known. Use {@link #println} instead. This method
     * is primarily intended for use from the standard logger to deliver stack
     * trace lines to other loggers which may be able to hyperlink them.
     * </p>
     * @param originalEvent the original event received by the calling logger
     * @param message a message to log (see {@link AntEvent#getMessage})
     * @param level the level to log it at (see {@link AntEvent#getLogLevel})
     */
    public void deliverMessageLogged(AntEvent originalEvent, String message, int level) {
        impl.deliverMessageLogged(originalEvent, message, level);
    }
    
    /**
     * Marks an exception as having been processed by a logger.
     * <p>
     * A single build-halting exception can traverse any number of Ant events
     * as it progresses outwards, typically from the failing task to the failing
     * target (possibly several times due to subprojects) and finally to the build failure.
     * </p>
     * <p class="nonnormative">
     * Consuming the exception permits a logger to indicate to other loggers that it
     * has already handled the problem in an appropriate manner. Since the standard
     * logger may print an exception (possibly with stack trace) that is part of
     * a {@link AntLogger#buildFinished} event, loggers which deal with the exception
     * in some other way should consume it before returning from the callback.
     * </p>
     * @param t an exception to mark as consumed
     * @throws IllegalStateException if it was already consumed
     */
    public void consumeException(Throwable t) throws IllegalStateException {
        impl.consumeException(t);
    }
    
    /**
     * Tests whether a given exception has already been consumed by some logger.
     * <p>
     * Note that if an exception is consumed, any exception with that exception
     * as its {@link Throwable#getCause} (possibly recursively) is also considered
     * consumed. This is useful because Ant's <code>ProjectHelper.addLocationToBuildException</code>
     * will annotate <code>BuildException</code>s with location information by constructing
     * wrapper exceptions.
     * </p>
     * @param t an exception
     * @return true if it (or a nested exception) has already been consumed by {@link #consumeException}
     */
    public boolean isExceptionConsumed(Throwable t) {
        return impl.isExceptionConsumed(t);
    }

    /**
     * Get the (user-requested) verbosity level for this session.
     * Generally only messages logged at this or lesser level (higher priority) should be displayed.
     * @return the verbosity, e.g. {@link AntEvent#LOG_INFO}
     */
    public int getVerbosity() {
        return impl.getVerbosity();
    }
    
    /**
     * Get a display name used for the session as a whole.
     * @return a user-presentable display name appropriate for session-scope messaging
     */
    public String getDisplayName() {
        return impl.getDisplayName();
    }
    
    /**
     * Convenience method to create a standard hyperlink implementation.
     * The GUI of the hyperlink will be oriented toward error messages and
     * may involve editor annotations.
     * Line and column numbers start at 1.
     * @param file a file to link to (may or may not exist, but hyperlink will not work if it does not)
     * @param message a message to use e.g. for the status bar when clicking on the hyperlink,
     *                or for annotation tool tips
     * @param line1 the starting line number, or -1 if there is no associated line number
     * @param column1 the starting column number, or -1 if there is no associated column number
     *                (must be -1 if line1 is -1)
     * @param line2 the ending line number, or -1 for a single-line link
     *              (must be -1 if line1 is -1)
     * @param column2 the ending column number, or -1 if not applicable
     *                (must be -1 if either line2 or column1 is -1)
     * @return a standard hyperlink suitable for {@link #println}
     */
    public OutputListener createStandardHyperlink(URL file, String message, int line1, int column1, int line2, int column2) {
        return impl.createStandardHyperlink(file, message, line1, column1, line2, column2);
    }
    
    @Override
    public String toString() {
        return impl.toString();
    }
    
}
