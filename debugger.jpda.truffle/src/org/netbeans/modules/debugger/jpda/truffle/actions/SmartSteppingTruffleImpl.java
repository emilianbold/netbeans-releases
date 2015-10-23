/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.truffle.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.SmartSteppingCallback;

/**
 * Step out from com.oracle.truffle.*
 * 
 * @author Martin
 */
@SmartSteppingCallback.Registration(path="netbeans-JPDASession")
public class SmartSteppingTruffleImpl extends SmartSteppingCallback {
    
    private static final String TRUFFLE_PACKAGE = "com.oracle.truffle.";        // NOI18N
    
    private SmartSteppingFilter filter;

    @Override
    public void initFilter(SmartSteppingFilter f) {
        this.filter = f;
        f.addPropertyChangeListener(new FilterChangeListener());
    }

    @Override
    public boolean stopHere(ContextProvider lookupProvider, JPDAThread thread, SmartSteppingFilter f) {
        // We implement stopAt() instead.
        return true;
    }

    @Override
    public StopOrStep stopAt(ContextProvider lookupProvider, CallStackFrame frame, SmartSteppingFilter f) {
        String className = frame.getClassName();
        boolean isInTruffleClass = false;
        if (className.startsWith(TRUFFLE_PACKAGE)) {
            isInTruffleClass = true;
        } else {
            /* Should not be necessary after https://netbeans.org/bugzilla/show_bug.cgi?id=255974 was fixed:
            // Debugger can not stop in Proxy class implementation (native),
            // but we can check whether the previous frame happens to be one:
            if (frame.getFrameDepth() == 0) {
                try {
                    CallStackFrame[] callStack = frame.getThread().getCallStack(0, 2);
                    if (callStack.length == 2) {
                        CallStackFrame frame2 = callStack[1];
                        String className2 = frame2.getClassName();
                        if (className2.startsWith("com.sun.proxy.$Proxy")) {              // NOI18N
                            Field hField = frame2.getThisVariable().getField("h");               // NOI18N
                            if (hField instanceof ObjectVariable) {
                                String hClassName = ((ObjectVariable) hField).getClassType().getName();
                                if (hClassName.startsWith(TRUFFLE_PACKAGE)) {
                                    isInTruffleClass = true;
                                }
                            }
                        }
                    }
                } catch (AbsentInformationException aiex) {}
            }
            */
        }
        if (isInTruffleClass) {
            // Step out from Truffle code
            return StopOrStep.step(JPDAStep.STEP_LINE, JPDAStep.STEP_OUT);
        } else {
            // Can stop here
            return StopOrStep.stop();
        }
    }
    
    /**
     * Assures that truffle filter patterns are not in the stepping filter.
     * This is crucial for us to be able to say that we want to step out.
     */
    private static final class FilterChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {//if (SmartSteppingFilter.PROP_EXCLUSION_PATTERNS.equals(ev.getPropertyName())) {
            Set<String> newPatterns = (Set<String>) evt.getNewValue();
            Set<String> patternsToRemove = null;
            if (newPatterns != null) {
                for (String pattern : newPatterns) {
                    if (pattern.startsWith(TRUFFLE_PACKAGE)) {
                        if (patternsToRemove == null) {
                            patternsToRemove = Collections.singleton(pattern);
                        } else {
                            if (patternsToRemove.size() == 1) {
                                Set<String> pr = new HashSet<>();
                                pr.add(patternsToRemove.iterator().next());
                                patternsToRemove = pr;
                            }
                            patternsToRemove.add(pattern);
                        }
                    }
                }
            }
            if (patternsToRemove != null) {
                ((SmartSteppingFilter) evt.getSource()).removeExclusionPatterns(patternsToRemove);
            }
        }
        
    }
    
}
