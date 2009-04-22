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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.spi.debugger.jpda.EditorContext;


/**
 * Implements non visual part of stepping through code in JPDA debugger.
 * It supports standart debugging actions StepInto, Over, Out, RunToCursor, 
 * and Go. And advanced "smart tracing" action.
 *
 * @author  Jan Jancura
 */
public class StepIntoActionProvider extends JPDADebuggerActionProvider {
    
    public static final String SS_STEP_OUT = "SS_ACTION_STEPOUT";
    public static final String ACTION_SMART_STEP_INTO = "smartStepInto";

    private StepIntoNextMethod stepInto;
    private MethodChooser currentMethodChooser;

    public StepIntoActionProvider (ContextProvider contextProvider) {
        super (
            (JPDADebuggerImpl) contextProvider.lookupFirst 
                (null, JPDADebugger.class)
        );
        stepInto = new StepIntoNextMethod(contextProvider);
        setProviderToDisableOnLazyAction(this);
    }


    // ActionProviderSupport ...................................................
    
    public Set getActions () {
        return new HashSet<Object>(Arrays.asList (new Object[] {
            ActionsManager.ACTION_STEP_INTO,
        }));
    }
    
    public void doAction (Object action) {
        runAction(action, true);
    }
    
    @Override
    public void postAction(final Object action, final Runnable actionPerformedNotifier) {
        doLazyAction(new Runnable() {
            public void run() {
                try {
                    runAction(action, true);
                } finally {
                    actionPerformedNotifier.run();
                }
            }
        });
    }
    
    public void runAction(Object action, boolean doResume) {
        if (ActionsManager.ACTION_STEP_INTO.equals(action) && doMethodSelection()) {
            return; // action performed
        }
        stepInto.runAction(action, doResume);
    }
    
    protected void checkEnabled (int debuggerState) {
        Iterator i = getActions ().iterator ();
        while (i.hasNext ())
            setEnabled (
                i.next (),
                (debuggerState == JPDADebugger.STATE_STOPPED) &&
                (getDebuggerImpl ().getCurrentThread () != null)
            );
    }
    
    // other methods ...........................................................
    
    public boolean doMethodSelection () {
        synchronized (this) {
            if (currentMethodChooser != null) {
                currentMethodChooser.doStepIntoCurrentSelection();
                return true;
            }
        }
        final String[] methodPtr = new String[1];
        final String[] urlPtr = new String[1];
        final int[] linePtr = new int[1];
        final int[] offsetPtr = new int[1];
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    EditorContext context = EditorContextBridge.getContext();
                    methodPtr[0] = context.getSelectedMethodName ();
                    linePtr[0] = context.getCurrentLineNumber();
                    offsetPtr[0] = EditorContextBridge.getCurrentOffset();
                    urlPtr[0] = context.getCurrentURL();
                }
            });
        } catch (InvocationTargetException ex) {
            return false;
        } catch (InterruptedException ex) {
            return false;
        }
        final int methodLine = linePtr[0];
        final int methodOffset = offsetPtr[0];
        final String url = urlPtr[0];
        if (methodLine < 0 || url == null || !url.endsWith (".java")) {
            return false;
        }
        String className = debugger.getCurrentThread().getClassName();
        VirtualMachine vm = debugger.getVirtualMachine();
        if (vm == null) return false;
        final List<ReferenceType> classes = VirtualMachineWrapper.classesByName0(vm, className);
        if (!classes.isEmpty()) {
            MethodChooser chooser = new MethodChooser(debugger, url, classes.get(0), methodLine, methodOffset);
            boolean success = chooser.run();
            if (success && chooser.isInSelectMode()) {
                synchronized (this) {
                    currentMethodChooser = chooser;
                    chooser.setReleaseListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            synchronized (this) {
                                currentMethodChooser = null;
                            }
                        }
                    });
                }
            }
            return success;
        } else {
            return false;
        }
    }
    
}
