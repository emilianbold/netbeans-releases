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

package org.netbeans.modules.cnd.mixeddev.debugger;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.SessionBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeSession;
import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.DebugTarget;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DebuggerOption;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.cnd.debugger.common2.utils.PsProvider;
import org.netbeans.modules.cnd.mixeddev.java.JNISupport;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Nikolay Koldunov
 */

@DebuggerServiceRegistration(types = SessionBridge.SessionChanger.class)
public class CndSessionChanger implements SessionBridge.SessionChanger{

    @Override
    public Set<String> getActions() {
        return Collections.singleton((String) ActionsManager.ACTION_STEP_INTO);
    }

    @Override
    public Session changeSuggested(Session origin, String action, Map<Object, Object> properties) {
        checkAutostartOption();
        PsProvider.PsData psData = PsProvider.getDefault(Host.getLocal()).getData(false);
        final Vector<Vector<String>> processes = psData.processes(
                Pattern.compile(".*java.+:" + properties.get("conn_port") + ".*") // NOI18N
        );
        if (processes.size() != 1) {
            NativeDebuggerManager.warning(NbBundle.getMessage(this.getClass(), "MSG_ProcessDetectionError")); // NOI18N
            return null;
        }
        final String funcName = MethodMapper.getNativeName(
                "" + properties.get("javaClass") + "." + properties.get("javaMethod")); // NOI18N
        final String stringPid = processes.firstElement().get(psData.pidColumnIdx());
        final long longPid = Long.parseLong(stringPid);
        Session ret = recognizeSessionByPid(longPid);
        if (ret == null) {
            final DebugTarget target = new DebugTarget();
            target.setPid(longPid);
            target.setHostName("localhost"); // NOI18N

            final CountDownLatch latch = new CountDownLatch(1);
            NativeDebuggerManager.get().addDebuggerStateListener(new NativeDebuggerManager.DebuggerStateListener() {
                @Override
                public void notifyAttached(NativeDebugger debugger, long pid) {
                    if (pid == longPid) {
                        NativeDebuggerManager.get().removeDebuggerStateListener(this);
                        debugger.stepTo(funcName);
                        latch.countDown();
                    }
                }
            });
            NativeDebuggerManager.get().attach(target);
            try {
                latch.await(100, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            NativeDebugger currentDebugger = NativeDebuggerManager.get().currentDebugger();
            if (currentDebugger != null) {
                NativeSession nativeSession = currentDebugger.session();
                if (nativeSession != null) {
                    ret = nativeSession.coreSession();
                }
            }
        } else {
            NativeSession.map(ret).getDebugger().pause();
            NativeSession.map(ret).getDebugger().stepTo(funcName);
        }

        return ret;
    }
    
    private static Session recognizeSessionByPid(long pid) {
        for (NativeSession nativeSession : NativeDebuggerManager.get().getSessions()) {
            if (nativeSession.getPid() == pid) {
                return nativeSession.coreSession();
            }
        }
        return null;
    }

//    private static void insertBreakpoint(NativeDebugger debugger, String functionName) {
//        NativeBreakpoint bpt = NativeBreakpoint.newFunctionBreakpoint(functionName);
//        if (bpt != null) {
//            int routingToken = RoutingToken.BREAKPOINTS.getUniqueRoutingTokenInt();
//            bpt.setTemp(true);
//            Handler.postNewHandler(debugger, bpt, routingToken);
//        }
////        debugger.stepTo(functionName);
//    }
    
    private static void checkAutostartOption() {
        boolean isAutostart = DebuggerOption.RUN_AUTOSTART.isEnabled(NativeDebuggerManager.get().globalOptions());
        if (isAutostart) {
//            NativeDebuggerManager.warning(NbBundle.getMessage(SessionChangerImpl.class, "MSG_UnsetAutostart")); // NOI18N
            DebuggerOption.RUN_AUTOSTART.setCurrValue(NativeDebuggerManager.get().globalOptions(), "false"); // NOI18N
        }
    }

    private static final class MethodMapper {
        /*package*/ static String getNativeName(String javaName) {
            return JNISupport.getCppMethodSignature(javaName.replaceAll("[.]", "/")); // NOI18N
        }
    }

//    /*package*/ String resolveSymbol(CharSequence funcName) {
//        Project[] projects = OpenProjects.getDefault().getOpenProjects();
//        for (Project prj : projects) {
//            NativeProject nativeProject = prj.getLookup().lookup(NativeProject.class);
//            if (nativeProject != null) {
//                Collection<CsmOffsetable> candidates = CsmSymbolResolver.resolveSymbol(nativeProject, funcName);
//                if (!candidates.isEmpty()) {
//                    CsmOffsetable candidate = candidates.iterator().next();
////                    CsmUtilities.openSource(candidate);
//                    if (CsmKindUtilities.isFunction(candidate)) {
//                        return ((CsmFunction) candidate).getQualifiedName().toString();
//                    }
//                    break;
//                }
//            }
//        }
//        return null;
//    }
}
