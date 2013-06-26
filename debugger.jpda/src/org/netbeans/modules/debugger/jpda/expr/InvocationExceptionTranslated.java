/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import com.sun.jdi.VirtualMachine;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ArrayReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IntegerValueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StringReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ValueWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.omg.CORBA.portable.ApplicationException;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin
 */
public class InvocationExceptionTranslated extends ApplicationException {
    
    private static final Logger logger = Logger.getLogger(InvocationExceptionTranslated.class.getName());
    
    private ObjectReference exeption;
    private JPDADebuggerImpl debugger;
    private JPDAThreadImpl preferredThread;
    
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
        VirtualMachine evm = exeption.virtualMachine();
        VirtualMachine dvm = debugger.getVirtualMachine();
        logger.log(Level.CONFIG,
                   invocationMessage+
                   ", evm = "+evm+", dvm = "+dvm,                               // NOI18N
                   new IllegalStateException("Stack Trace Info"));              // NOI18N
    }
    
    public void setPreferredThread(JPDAThreadImpl preferredThread) {
        this.preferredThread = preferredThread;
    }

    @Override
    public synchronized String getMessage() {
        if (message == null) {
            try {
                Method getMessageMethod = ClassTypeWrapper.concreteMethodByName((ClassType) ValueWrapper.type(exeption),
                            "getMessage", "()Ljava/lang/String;");  // NOI18N
                if (getMessageMethod == null) {
                    message = "Unknown exception message";
                } else {
                    try {
                        StringReference sr = (StringReference) debugger.invokeMethod (
                                preferredThread,
                                exeption,
                                getMessageMethod,
                                new Value [0]
                            );
                        message = sr != null ? StringReferenceWrapper.value(sr) : ""; // NOI18N
                    } catch (InvalidExpressionException ex) {
                        return ex.getMessage();
                    }
                }
            } catch (InternalExceptionWrapper iex) {
                return iex.getMessage();
            } catch (VMDisconnectedExceptionWrapper vdex) {
                return vdex.getMessage();
            } catch (ObjectCollectedExceptionWrapper ocex) {
                Exceptions.printStackTrace(ocex);
                return ocex.getMessage();
            } catch (ClassNotPreparedExceptionWrapper cnpex) {
                return cnpex.getMessage();
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
            try {
                Method getMessageMethod = ClassTypeWrapper.concreteMethodByName((ClassType) ValueWrapper.type(exeption),
                            "getLocalizedMessage", "()Ljava/lang/String;");  // NOI18N
                if (getMessageMethod == null) {
                    localizedMessage = "Unknown exception message";
                } else {
                    try {
                        StringReference sr = (StringReference) debugger.invokeMethod (
                                preferredThread,
                                exeption,
                                getMessageMethod,
                                new Value [0]
                            );
                        localizedMessage = sr == null ? "" : StringReferenceWrapper.value(sr); // NOI18N
                    } catch (InvalidExpressionException ex) {
                        return ex.getLocalizedMessage();
                    }
                }
            } catch (InternalExceptionWrapper iex) {
                return iex.getMessage();
            } catch (VMDisconnectedExceptionWrapper vdex) {
                return vdex.getMessage();
            } catch (ObjectCollectedExceptionWrapper ocex) {
                Exceptions.printStackTrace(ocex);
                return ocex.getMessage();
            } catch (ClassNotPreparedExceptionWrapper cnpex) {
                return cnpex.getMessage();
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
            try {
                Method getCauseMethod = ClassTypeWrapper.concreteMethodByName((ClassType) ValueWrapper.type(exeption),
                            "getCause", "()Ljava/lang/Throwable;");  // NOI18N
                try {
                    ObjectReference or;
                    if (getCauseMethod == null) {
                        or = null;
                    } else {
                        or = (ObjectReference) debugger.invokeMethod (
                            preferredThread,
                            exeption,
                            getCauseMethod,
                            new Value [0]
                        );
                    }
                    if (or != null) {
                        cause = new InvocationExceptionTranslated(null, or, debugger);
                    } else {
                        cause = this;
                    }
                } catch (InvalidExpressionException ex) {
                    return null;
                }
            } catch (InternalExceptionWrapper iex) {
                return null;
            } catch (VMDisconnectedExceptionWrapper vdex) {
                return null;
            } catch (ObjectCollectedExceptionWrapper vdex) {
                return null;
            } catch (ClassNotPreparedExceptionWrapper cnpex) {
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
            try {
                Method getStackTraceMethod = ClassTypeWrapper.concreteMethodByName((ClassType) ValueWrapper.type(exeption),
                            "getStackTrace", "()[Ljava/lang/StackTraceElement;");  // NOI18N
                if (getStackTraceMethod == null) {
                    return new StackTraceElement[0];
                }
                ArrayReference ar = (ArrayReference) debugger.invokeMethod (
                        preferredThread,
                        exeption,
                        getStackTraceMethod,
                        new Value [0]
                    );
                int depth = ArrayReferenceWrapper.length(ar);
                stackTrace = new StackTraceElement[depth];
                for (int i=0; i < depth; i++) {
                    stackTrace[i] = getStackTraceElement((ObjectReference) ArrayReferenceWrapper.getValue(ar, i));
                }
            } catch (InvalidExpressionException ex) {
                // Leave stackTrace unset to reload next time
                return new StackTraceElement[0];
            } catch (ClassNotPreparedExceptionWrapper ex) {
                return new StackTraceElement[0];
            } catch (InternalExceptionWrapper ex) {
                return new StackTraceElement[0];
            } catch (VMDisconnectedExceptionWrapper ex) {
                return new StackTraceElement[0];
            } catch (ObjectCollectedExceptionWrapper ex) {
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

        try {
            Method getMethod;
            getMethod = ClassTypeWrapper.concreteMethodByName((ClassType) ValueWrapper.type(stElement),
                    "getClassName", "()Ljava/lang/String;");  // NOI18N
            if (getMethod == null) {
                declaringClass = "unknown";
            } else {
                try {
                    StringReference sr = (StringReference) debugger.invokeMethod (
                            preferredThread,
                            stElement,
                            getMethod,
                            new Value [0]
                        );
                    declaringClass = StringReferenceWrapper.value(sr);
                } catch (InvalidExpressionException ex) {
                    declaringClass = ex.getLocalizedMessage();
                }
                if (declaringClass == null) {
                    declaringClass = "unknown";
                }
            }
            getMethod = ClassTypeWrapper.concreteMethodByName((ClassType) ValueWrapper.type(stElement),
                    "getMethodName", "()Ljava/lang/String;");  // NOI18N
            if (getMethod == null) {
                methodName = "unknown";
            } else {
                try {
                    StringReference sr = (StringReference) debugger.invokeMethod (
                            preferredThread,
                            stElement,
                            getMethod,
                            new Value [0]
                        );
                    methodName = StringReferenceWrapper.value(sr);
                } catch (InvalidExpressionException ex) {
                    methodName = ex.getLocalizedMessage();
                }
                if (methodName == null) {
                    methodName = "unknown";
                }
            }
            getMethod = ClassTypeWrapper.concreteMethodByName((ClassType) ValueWrapper.type(stElement),
                    "getFileName", "()Ljava/lang/String;");  // NOI18N
            if (getMethod == null) {
                fileName = "unknown";
            } else {
                try {
                    StringReference sr = (StringReference) debugger.invokeMethod (
                            preferredThread,
                            stElement,
                            getMethod,
                            new Value [0]
                        );
                    if (sr == null) {
                        fileName = null;
                    } else {
                        fileName = StringReferenceWrapper.value(sr);
                    }
                } catch (InvalidExpressionException ex) {
                    fileName = ex.getLocalizedMessage();
                }
            }
            getMethod = ClassTypeWrapper.concreteMethodByName((ClassType) ValueWrapper.type(stElement),
                        "getLineNumber", "()I");  // NOI18N
            if (getMethod == null) {
                lineNumber = -1;
            } else {
                try {
                    IntegerValue iv = (IntegerValue) debugger.invokeMethod (
                            preferredThread,
                            stElement,
                            getMethod,
                            new Value [0]
                        );
                    lineNumber = IntegerValueWrapper.value(iv);
                } catch (InvalidExpressionException ex) {
                    lineNumber = -1;
                }
            }
            return new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
        } catch (InternalExceptionWrapper ex) {
            String msg = ex.getLocalizedMessage();
            return new StackTraceElement(msg, msg, msg, -1);
        } catch (VMDisconnectedExceptionWrapper ex) {
            String msg = ex.getLocalizedMessage();
            return new StackTraceElement(msg, msg, msg, -1);
        } catch (ObjectCollectedExceptionWrapper ex) {
            Exceptions.printStackTrace(ex);
            String msg = ex.getLocalizedMessage();
            return new StackTraceElement(msg, msg, msg, -1);
        } catch (ClassNotPreparedExceptionWrapper ex) {
            String msg = ex.getLocalizedMessage();
            return new StackTraceElement(msg, msg, msg, -1);
        }
    }

    
    @Override
    public String toString() {
        String s;
        try {
            s = TypeWrapper.name(ValueWrapper.type(exeption));
        } catch (ObjectCollectedExceptionWrapper ex) {
            return "Collected";
        } catch (InternalExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return "Disconnected";
        }
        String message = getOriginalLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }

}
