/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.truffle.actions;

import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.actions.JPDADebuggerActionProvider;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.truffle.TruffleDebugManager;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleStrataProvider;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.Exceptions;

/**
 *
 * @author martin
 */
@ActionsProvider.Registration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM,
                              actions={"stepInto", "stepOver", "stepOut", "continue"})
public class StepActionProvider extends JPDADebuggerActionProvider {
    
    private static final Logger LOG = Logger.getLogger(StepActionProvider.class.getName());
    
    private static final Set actions = new HashSet(Arrays.asList (new Object[] {
            ActionsManager.ACTION_STEP_INTO,
            ActionsManager.ACTION_STEP_OUT,
            ActionsManager.ACTION_STEP_OVER,
            ActionsManager.ACTION_CONTINUE
    }));

    private final ContextProvider lookupProvider;

    public StepActionProvider (ContextProvider lookupProvider) {
        super (
            (JPDADebuggerImpl) lookupProvider.lookupFirst 
                (null, JPDADebugger.class)
        );
        this.lookupProvider = lookupProvider;
    }
    
    @Override
    protected void checkEnabled(int debuggerState) {
        Iterator i = getActions ().iterator ();
        while (i.hasNext ()) {
            setEnabled (
                i.next (),
                (debuggerState == JPDADebugger.STATE_STOPPED) &&
                (getDebuggerImpl ().getCurrentThread () != null) &&
                (TruffleAccess.getCurrentPCInfo(getDebuggerImpl()) != null)
            );
        }
    }

    @Override
    public void doAction(Object action) {
        LOG.fine("doAction("+action+")");
        JPDADebuggerImpl debugger = getDebuggerImpl();
        CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(debugger);
        int stepCmd = 0;
        final String stepInto = (String) ActionsManager.ACTION_STEP_INTO;
        if (ActionsManager.ACTION_CONTINUE.equals(action)) {
            stepCmd = 0;
        } else if (ActionsManager.ACTION_STEP_INTO.equals(action)) {
            stepCmd = 1;
        } else if (ActionsManager.ACTION_STEP_OVER.equals(action)) {
            stepCmd = 2;
        } else if (ActionsManager.ACTION_STEP_OUT.equals(action)) {
            stepCmd = 3;
        }
        JPDAClassType accessorClass = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
        try {
            Variable[] arguments = new Variable[] { currentPCInfo.getSuspendedInfo(), debugger.createMirrorVar((Integer) stepCmd, true) };
            accessorClass.invokeMethod("setStep", "(Ljava/lang/Object;I)V", arguments);
        } catch (InvalidExpressionException | InvalidObjectException | NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        /*
        ClassType accessorClass = TruffleDebugManager.getDebugAccessorClass(debugger);
        try {
            Field stepCmdField = ReferenceTypeWrapper.fieldByName(accessorClass, "stepCmd");
            ClassTypeWrapper.setValue(accessorClass, stepCmdField,
                                      VirtualMachineWrapper.mirrorOf(accessorClass.virtualMachine(), stepCmd));
        } catch (VMDisconnectedExceptionWrapper ex) {
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        */
        killJavaStep(debugger);
        if (stepCmd > 0) {
            debugger.resumeCurrentThread();
        } else {
            debugger.resume();
        }
    }
    
    /**
     * Kill any pending Java step.
     * @param debugger 
     */
    public static void killJavaStep(JPDADebugger debugger) {
        killJavaStep((JPDADebuggerImpl) debugger);
    }
    
    // Kill any pending Java step...
    private static void killJavaStep(JPDADebuggerImpl debugger) {
        VirtualMachine vm = debugger.getVirtualMachine();
        if (vm == null) {
            return ;
        }
        EventRequestManager erm = vm.eventRequestManager();
        List<StepRequest> stepRequests;
        try {
            stepRequests = EventRequestManagerWrapper.stepRequests(erm);
        } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
            return ;
        }
        if (stepRequests.isEmpty()) {
            return ;
        }
        stepRequests = new ArrayList<>(stepRequests);
        for (StepRequest sr : stepRequests) {
            try {
                EventRequestManagerWrapper.deleteEventRequest(erm, sr);
                debugger.getOperator().unregister(sr);
            } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                     InvalidRequestStateExceptionWrapper ex) {
            }
        }
    }

    @Override
    public Set getActions() {
        return actions;
    }
    
}
