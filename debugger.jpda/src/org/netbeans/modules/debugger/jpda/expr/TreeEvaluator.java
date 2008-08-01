/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Micro//S ystems, Inc. Portions Copyright 1997-2007 Sun
 * Micro//S ystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.debugger.jpda.expr;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InternalException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.Mirror;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;

import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Entlicher
 */
public class TreeEvaluator {
    
    private Expression2 expression;
    private EvaluationContext evaluationContext;

    private StackFrame frame;
    private VirtualMachine vm;
    private ThreadReference frameThread;
    private int frameIndex;
    private String currentPackage;
    private Operators operators;
    
    private static final Logger loggerMethod = Logger.getLogger("org.netbeans.modules.debugger.jpda.invokeMethod"); // NOI18N
    
    TreeEvaluator(Expression2 expression, EvaluationContext context) {
        this.expression = expression;
        this.evaluationContext = context;
    }

    /**
     * Evaluates the expression for which it was created.
     *
     * @return the result of evaluating the expression as a JDI Value object.
     *         It returns null if the result itself is null.
     * @throws EvaluationException if the expression cannot be evaluated for whatever reason
     * @throws IncompatibleThreadStateException if the context thread is in an
     * incompatible state (running, dead)
     */
    public Value evaluate() throws EvaluationException2, IncompatibleThreadStateException, InvalidExpressionException
    {
        frame = evaluationContext.getFrame();
        vm = evaluationContext.getFrame().virtualMachine();
        frameThread = frame.thread();
        frameIndex = indexOf(frameThread.frames(), frame);
        if (frameIndex == -1) {
            throw new IncompatibleThreadStateException("Thread does not contain current frame");
        }
        currentPackage = evaluationContext.getFrame().location().declaringType().name();
        int idx = currentPackage.lastIndexOf('.');
        currentPackage = (idx > 0) ? currentPackage.substring(0, idx + 1) : "";
        operators = new Operators(vm);
        int line = frame.location().lineNumber();
        String url = evaluationContext.getDebugger().getEngineContext().getURL(frame, "Java");//evaluationContext.getDebugger().getSession().getCurrentLanguage());
        /*try {
            url = frame.location().sourcePath(expression.getLanguage());
        } catch (AbsentInformationException ex) {
            return null;
        }*/
        //Tree exprTree = EditorContextBridge.getExpressionTree(expression.getExpression(), url, line);
        //if (exprTree == null) return null;
        try {
            Mirror mirror = EditorContextBridge.parseExpression(expression.getExpression(), url, line,
                                                              new EvaluatorVisitor(expression), evaluationContext,
                                                              evaluationContext.getDebugger().getEngineContext().getContext());
            if (mirror instanceof Value || mirror == null) {
                return (Value) mirror;
            } else {
                throw new InvalidExpressionException(expression.getExpression());
            }
            //return exprTree.accept(new EvaluatorVisitor(), evaluationContext);
        } catch (IllegalStateException isex) {
            Throwable thr = isex.getCause();
            if (thr instanceof IncompatibleThreadStateException) {
                throw (IncompatibleThreadStateException) thr;
            }
            if (thr instanceof InvalidExpressionException) {
                throw (InvalidExpressionException) thr;
            }
            throw isex;
        } catch (InternalException e) {
            throw new InvalidExpressionException (e.getLocalizedMessage());
        }
        //return (Value) rootNode.jjtAccept(this, null);
        //return null;
    }

    private int indexOf(List<StackFrame> frames, StackFrame frame) {
        int n = frames.size();
        Location loc = frame.location();
        for (int i = 0; i < n; i++) {
            if (loc.equals(frames.get(i).location())) return i;
        }
        return -1;
    }

    public static Value invokeVirtual (
        ObjectReference objectReference, 
        Method method, 
        ThreadReference evaluationThread, 
        List<Value> args,
        JPDADebuggerImpl debugger
     ) throws InvalidExpressionException {
        
        try {
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.fine("STARTED : "+objectReference+"."+method+" ("+args+") in thread "+evaluationThread);
            }
            Value value =
                    objectReference.invokeMethod(evaluationThread, method,
                                                 args,
                                                 ObjectReference.INVOKE_SINGLE_THREADED);
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.fine("   return = "+value);
            }
            return value;
        } catch (InvalidTypeException itex) {
            throw new InvalidExpressionException (itex);
        } catch (ClassNotLoadedException cnlex) {
            throw new InvalidExpressionException (cnlex);
        } catch (IncompatibleThreadStateException itsex) {
            InvalidExpressionException ieex = new InvalidExpressionException (itsex);
            ieex.initCause(itsex);
            throw ieex;
        } catch (InvocationException iex) {
            Throwable ex = new InvocationExceptionTranslated(iex, debugger);
            InvalidExpressionException ieex = new InvalidExpressionException (ex);
            ieex.initCause(ex);
            throw ieex;
        } catch (UnsupportedOperationException uoex) {
            InvalidExpressionException ieex = new InvalidExpressionException (uoex);
            ieex.initCause(uoex);
            throw ieex;
        } catch (ObjectCollectedException ocex) {
            throw new InvalidExpressionException(NbBundle.getMessage(
                TreeEvaluator.class, "CTL_EvalError_collected"));
        } finally {
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.fine("FINISHED: "+objectReference+"."+method+" ("+args+") in thread "+evaluationThread);
            }
        }
    }
}
