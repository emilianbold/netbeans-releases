/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.traceio;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;
import org.netbeans.lib.traceio.agent.TraceIO;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;
import org.openide.modules.Places;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Hurka
 */
public class Installer extends ModuleInstall {

    private static Logger LOG = Logger.getLogger(Installer.class.getName());
    private static String AGENT_PATH = "modules/ext/tioagent.jar";   // NOI18N
    private static String TRACE_ACTION_PROP_NAME = "org.netbeans.modules.traceio.action";   // NOI18N
    private static String TRACE_THREAD_AWT_PROP_NAME = "org.netbeans.modules.traceio.thread.awt";   // NOI18N
    private static String TRACE_SIZE = "org.netbeans.modules.traceio.size";     // NOI18N
    
    @Override
    public void restored() {
        if (testIOTrace()) {
            String pid = getSelfPID();
            String agentPath = getAgentPath();
            Properties agentProperties = new Properties();
            
            LOG.warning("Self PID "+pid);   // NOI18N
            LOG.warning("Agent "+agentPath);    // NOI18N
            setAgentProperties(agentProperties);
            try {
                VirtualMachine vm = VirtualMachine.attach(pid);
                LOG.warning(vm.toString());
                vm.loadAgent(agentPath, serializeProperties(agentProperties));
                vm.detach();
                LOG.warning("Agent loaded");    // NOI18N
            } catch (AttachNotSupportedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (AgentLoadException ex) {
                Exceptions.printStackTrace(ex);
            } catch (AgentInitializationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    boolean testIOTrace() {
        try {
            ClassLoader.getSystemClassLoader().loadClass(TraceIO.IO_TRACE);
        } catch (ClassNotFoundException ex) {
            LOG.warning(TraceIO.IO_TRACE+" not found; Trace IO module disabled");   // NOI18N
            return false;
        }
        return true;
    }
    
    String getSelfPID() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int index = name.indexOf('@');  // NOI18N
        return name.substring(0, index);
    }

    private String getAgentPath() {
        InstalledFileLocator loc = InstalledFileLocator.getDefault();
        File jar = loc.locate(AGENT_PATH, "org.netbeans.modules.traceio", false);   // NOI18N
        
        return jar.getAbsolutePath();
    }
    
    private String serializeProperties(Properties p) {
        StringBuilder buf = new StringBuilder();
        for (Entry<Object,Object> e : p.entrySet()) {
            Object key = e.getKey();
            Object val = e.getValue();
            if (key instanceof String && val instanceof String) {
                buf.append(key).append('\1').append(val).append('\1');  // NOI18N
            }
        }
        return buf.toString();
    }

    private void setAgentProperties(Properties agentProperties) {
        agentProperties.setProperty(TraceIO.USERDIR, Places.getUserDirectory().getAbsolutePath());
        agentProperties.setProperty(TraceIO.CACHEDIR, Places.getCacheDirectory().getAbsolutePath());
        agentProperties.setProperty(TraceIO.ACTION, System.getProperty(TRACE_ACTION_PROP_NAME, TraceIO.TRACE_ACTION.LOG.name()));
        agentProperties.setProperty(TraceIO.THREAD_AWT, System.getProperty(TRACE_THREAD_AWT_PROP_NAME, Boolean.TRUE.toString()));
        String size = System.getProperty(TRACE_SIZE);
        if (size != null) {
            char type = size.charAt(0);
            int s = Math.abs(Integer.valueOf(size));
            TraceIO.TRACE_EQ seq = TraceIO.TRACE_EQ.EQUAL;
            
            if (type == '+') {  // NOI18N
                seq = TraceIO.TRACE_EQ.GREATER;
            } else if (type == '-') {   // NOI18N
                seq = TraceIO.TRACE_EQ.LOWER;
            }
            agentProperties.setProperty(TraceIO.SIZE, String.valueOf(s));
            agentProperties.setProperty(TraceIO.SIZE_EQ, seq.toString());
        }
    }
}
