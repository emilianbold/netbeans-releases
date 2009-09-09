/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.expr.EvaluationContext;
import org.netbeans.modules.debugger.jpda.expr.EvaluationException;
import org.netbeans.modules.debugger.jpda.expr.JavaExpression;
import org.netbeans.modules.debugger.jpda.expr.TreeEvaluator;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.Evaluator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Entlicher
 */
@Evaluator.Registration(language="Java")
public class JavaEvaluator implements Evaluator<JavaExpression> {

    private final JPDADebuggerImpl debugger;

    public JavaEvaluator (ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.lookupFirst (null, JPDADebugger.class);
    }

    public Result evaluate(Expression<JavaExpression> expression, final Context context) throws InvalidExpressionException {
        JavaExpression expr = expression.getPreprocessedObject();
        if (expr == null) {
            expr = JavaExpression.parse(expression.getExpression(), JavaExpression.LANGUAGE_JAVA_1_5);
            expression.setPreprocessedObject(expr);
        }
        Value v = evaluateIn(expr, context.getStackFrame(), context.getStackDepth(),
                             context.getContextObject(), debugger.methodCallsUnsupportedExc == null,
                             new Runnable() { public void run() { context.notifyMethodToBeInvoked(); } });
        return new Result(v);
    }

    /*@Override
    public Value evaluate(String expression, StackFrame csf, int stackDepth,
                          ObjectReference var, boolean canInvokeMethods,
                          Runnable methodInvokePreprocessor) throws InvalidExpressionException {
        JavaExpression expr = JavaExpression.parse(expression, JavaExpression.LANGUAGE_JAVA_1_5);
        return evaluateIn(expr, csf, stackDepth, var, canInvokeMethods, methodInvokePreprocessor);
    }*/

    // * Might be changed to return a variable with disabled collection. When not used any more,
    // * it's collection must be enabled again.
    private Value evaluateIn (org.netbeans.modules.debugger.jpda.expr.JavaExpression expression,
                              final StackFrame frame, int frameDepth,
                              ObjectReference var, boolean canInvokeMethods,
                              Runnable methodInvokePreprocessor) throws InvalidExpressionException {
        // should be already synchronized on the frame's thread
        if (frame == null)
            throw new InvalidExpressionException
                    (NbBundle.getMessage(JPDADebuggerImpl.class, "MSG_NoCurrentContext"));

        // TODO: get imports from the source file
        List<String> imports = new ArrayList<String>();
        List<String> staticImports = new ArrayList<String>();
        imports.add ("java.lang.*");    // NOI18N
        try {
            imports.addAll (Arrays.asList (EditorContextBridge.getContext().getImports (
                debugger.getEngineContext ().getURL (frame, "Java") // NOI18N
            )));
            final ThreadReference tr = StackFrameWrapper.thread(frame);
            JPDAThreadImpl trImpl = debugger.getThread(tr);
            EvaluationContext context;
            TreeEvaluator evaluator =
                expression.evaluator(
                    context = new EvaluationContext(
                        trImpl,
                        frame,
                        frameDepth,
                        var,
                        imports,
                        staticImports,
                        canInvokeMethods,
                        methodInvokePreprocessor,
                        debugger
                    )
                );
            try {
                return evaluator.evaluate ();
            } finally {
                if (debugger.methodCallsUnsupportedExc == null && !context.canInvokeMethods()) {
                    debugger.methodCallsUnsupportedExc =
                            new InvalidExpressionException(new UnsupportedOperationException());
                }
            }
        } catch (InternalExceptionWrapper e) {
            throw new InvalidExpressionException(e.getLocalizedMessage());
        } catch (VMDisconnectedExceptionWrapper e) {
            throw new InvalidExpressionException(NbBundle.getMessage(
                TreeEvaluator.class, "CTL_EvalError_disconnected"));
        } catch (InvalidStackFrameExceptionWrapper e) {
            Exceptions.printStackTrace(e); // Should not occur
            throw new InvalidExpressionException (NbBundle.getMessage(
                    JPDAThreadImpl.class, "MSG_NoCurrentContext"));
        } catch (EvaluationException e) {
            InvalidExpressionException iee = new InvalidExpressionException (e);
            iee.initCause (e);
            Exceptions.attachMessage(iee, "Expression = '"+expression.getExpression()+"'");
            throw iee;
        } catch (IncompatibleThreadStateException itsex) {
            InvalidExpressionException isex = new InvalidExpressionException(itsex.getLocalizedMessage());
            isex.initCause(itsex);
            throw isex;
        }
    }

}
