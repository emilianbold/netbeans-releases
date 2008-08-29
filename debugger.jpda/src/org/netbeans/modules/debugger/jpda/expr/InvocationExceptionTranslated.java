/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.expr;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
import java.io.PrintStream;
import java.io.PrintWriter;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.omg.CORBA.portable.ApplicationException;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin
 */
public class InvocationExceptionTranslated extends ApplicationException {
    
    private ObjectReference exeption;
    private JPDADebuggerImpl debugger;
    
    private String invocationMessage;
    private String message;
    private String localizedMessage;
    private Throwable cause;
    private StackTraceElement[] stackTrace;

    public InvocationExceptionTranslated(InvocationException iex, JPDADebuggerImpl debugger) {
        this(iex.getMessage(), iex.exception(), debugger);
    }
    
    private InvocationExceptionTranslated(String invocationMessage, ObjectReference exeption, JPDADebuggerImpl debugger) {
        super(InvocationException.class.getName(), null);
        this.invocationMessage = invocationMessage;
        this.exeption = exeption;
        this.debugger = debugger;
    }

    @Override
    public synchronized String getMessage() {
        if (message == null) {
            Method getMessageMethod = ((ClassType) exeption.type ()).
                        concreteMethodByName ("getMessage", "()Ljava/lang/String;");  // NOI18N
            try {
                StringReference sr = (StringReference) debugger.invokeMethod (
                        exeption,
                        getMessageMethod,
                        new Value [0]
                    );
                message = sr != null ? sr.value() : ""; // NOI18N
            } catch (InvalidExpressionException ex) {
                return ex.getMessage();
            }
        }
        if (invocationMessage != null) {
            return invocationMessage + ": " + message;
        } else {
            return message;
        }
    }

    @Override
    public String getLocalizedMessage() {
        if (localizedMessage == null) {
            Method getMessageMethod = ((ClassType) exeption.type ()).
                        concreteMethodByName ("getLocalizedMessage", "()Ljava/lang/String;");  // NOI18N
            try {
                StringReference sr = (StringReference) debugger.invokeMethod (
                        exeption,
                        getMessageMethod,
                        new Value [0]
                    );
                localizedMessage = sr == null ? "" : sr.value(); // NOI18N
            } catch (InvalidExpressionException ex) {
                return ex.getLocalizedMessage();
            }
        }
        if (invocationMessage != null) {
            return invocationMessage + ": " + localizedMessage;
        } else {
            return localizedMessage;
        }
    }
    
    
    
    private String getOriginalLocalizedMessage() {
        getLocalizedMessage();
        return localizedMessage;
    }

    @Override
    public synchronized Throwable getCause() {
        if (cause == null) {
            Method getCauseMethod = ((ClassType) exeption.type ()).
                        concreteMethodByName ("getCause", "()Ljava/lang/Throwable;");  // NOI18N
            try {
                ObjectReference or = (ObjectReference) debugger.invokeMethod (
                        exeption,
                        getCauseMethod,
                        new Value [0]
                    );
                if (or != null) {
                    cause = new InvocationExceptionTranslated(null, or, debugger);
                } else {
                    cause = this;
                }
            } catch (InvalidExpressionException ex) {
                return null;
            }
        }
        return (cause == this ? null : cause);
    }

    /**
     * Hard copy from Throwable, our implementation of getOurStackTrace() is used.
     */
    @Override
    public void printStackTrace(PrintStream s) {
        synchronized (s) {
            s.println(this);
            StackTraceElement[] trace = getOurStackTrace();
            for (int i=0; i < trace.length; i++)
                s.println("\tat " + trace[i]);

            InvocationExceptionTranslated ourCause = (InvocationExceptionTranslated) getCause();
            if (ourCause != null)
                ourCause.printStackTraceAsCause(s, trace);
        }
    }

    /**
     * Print our stack trace as a cause for the specified stack trace.
     * 
     * Hard copy from Throwable, our implementation of getOurStackTrace() is used.
     */
    private void printStackTraceAsCause(PrintStream s,
                                        StackTraceElement[] causedTrace)
    {
        // assert Thread.holdsLock(s);

        // Compute number of frames in common between this and caused
        StackTraceElement[] trace = getOurStackTrace();
        int m = trace.length-1, n = causedTrace.length-1;
        while (m >= 0 && n >=0 && trace[m].equals(causedTrace[n])) {
            m--; n--;
        }
        int framesInCommon = trace.length - 1 - m;

        s.println("Caused by: " + this);
        for (int i=0; i <= m; i++)
            s.println("\tat " + trace[i]);
        if (framesInCommon != 0)
            s.println("\t... " + framesInCommon + " more");

        // Recurse if we have a cause
        InvocationExceptionTranslated ourCause = (InvocationExceptionTranslated) getCause();
        if (ourCause != null)
            ourCause.printStackTraceAsCause(s, trace);
    }

    /**
     * Hard copy from Throwable, our implementation of getOurStackTrace() is used.
     */
    @Override
    public void printStackTrace(PrintWriter s) {
        synchronized (s) {
            s.println(this);
            StackTraceElement[] trace = getOurStackTrace();
            for (int i=0; i < trace.length; i++)
                s.println("\tat " + trace[i]);

            InvocationExceptionTranslated ourCause = (InvocationExceptionTranslated) getCause();
            if (ourCause != null)
                ourCause.printStackTraceAsCause(s, trace);
        }
    }
    
    /**
     * Print our stack trace as a cause for the specified stack trace.
     * 
     * Hard copy from Throwable, our implementation of getOurStackTrace() is used.
     */
    private void printStackTraceAsCause(PrintWriter s,
                                        StackTraceElement[] causedTrace)
    {
        // assert Thread.holdsLock(s);

        // Compute number of frames in common between this and caused
        StackTraceElement[] trace = getOurStackTrace();
        int m = trace.length-1, n = causedTrace.length-1;
        while (m >= 0 && n >=0 && trace[m].equals(causedTrace[n])) {
            m--; n--;
        }
        int framesInCommon = trace.length - 1 - m;

        s.println("Caused by: " + this);
        for (int i=0; i <= m; i++)
            s.println("\tat " + trace[i]);
        if (framesInCommon != 0)
            s.println("\t... " + framesInCommon + " more");

        // Recurse if we have a cause
        InvocationExceptionTranslated ourCause = (InvocationExceptionTranslated) getCause();
        if (ourCause != null)
            ourCause.printStackTraceAsCause(s, trace);
    }

    /**
     * Hard copy from Throwable, our implementation of getOurStackTrace() is used.
     */
    @Override
    public StackTraceElement[] getStackTrace() {
        return (StackTraceElement[]) getOurStackTrace().clone();
    }

    
    private synchronized StackTraceElement[] getOurStackTrace() {
        // Initialize stack trace if this is the first call to this method
        if (stackTrace == null) {
            Method getStackTraceMethod = ((ClassType) exeption.type ()).
                        concreteMethodByName ("getStackTrace", "()[Ljava/lang/StackTraceElement;");  // NOI18N
            try {
                ArrayReference ar = (ArrayReference) debugger.invokeMethod (
                        exeption,
                        getStackTraceMethod,
                        new Value [0]
                    );
                int depth = ar.length();
                stackTrace = new StackTraceElement[depth];
                for (int i=0; i < depth; i++) {
                    stackTrace[i] = getStackTraceElement((ObjectReference) ar.getValue(i));
                }
            } catch (InvalidExpressionException ex) {
                // Leave stackTrace unset to reload next time
                return new StackTraceElement[0];
            }
        }
        return stackTrace;
    }

    private StackTraceElement getStackTraceElement(ObjectReference stElement) {
        String declaringClass;
        String methodName;
        String fileName;
        int    lineNumber;
        
        Method getMethod = ((ClassType) stElement.type ()).
                    concreteMethodByName ("getClassName", "()Ljava/lang/String;");  // NOI18N
        try {
            StringReference sr = (StringReference) debugger.invokeMethod (
                    stElement,
                    getMethod,
                    new Value [0]
                );
            declaringClass = sr.value();
        } catch (InvalidExpressionException ex) {
            declaringClass = ex.getLocalizedMessage();
        }
        getMethod = ((ClassType) stElement.type ()).
                    concreteMethodByName ("getMethodName", "()Ljava/lang/String;");  // NOI18N
        try {
            StringReference sr = (StringReference) debugger.invokeMethod (
                    stElement,
                    getMethod,
                    new Value [0]
                );
            methodName = sr.value();
        } catch (InvalidExpressionException ex) {
            methodName = ex.getLocalizedMessage();
        }
        getMethod = ((ClassType) stElement.type ()).
                    concreteMethodByName ("getFileName", "()Ljava/lang/String;");  // NOI18N
        try {
            StringReference sr = (StringReference) debugger.invokeMethod (
                    stElement,
                    getMethod,
                    new Value [0]
                );
            if (sr == null) {
                fileName = null;
            } else {
                fileName = sr.value();
            }
        } catch (InvalidExpressionException ex) {
            fileName = ex.getLocalizedMessage();
        }
        getMethod = ((ClassType) stElement.type ()).
                    concreteMethodByName ("getLineNumber", "()I");  // NOI18N
        try {
            IntegerValue iv = (IntegerValue) debugger.invokeMethod (
                    stElement,
                    getMethod,
                    new Value [0]
                );
            lineNumber = iv.value();
        } catch (InvalidExpressionException ex) {
            lineNumber = -1;
        }
        return new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
    }

    
    @Override
    public String toString() {
        String s = exeption.type().name();
        String message = getOriginalLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }

}
