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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.spi.glassfish;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Future;
import javax.swing.event.ChangeListener;


/**
 *
 * @author Peter Williams
 */
public interface GlassfishModule {
    
    public static final String URL_ATTR = "url"; // NOI18N
    public static final String HOME_FOLDER_ATTR = "homefolder"; // NOI18N
    public static final String DISPLAY_NAME_ATTR = "displayName"; // NOI18N
    public static final String USERNAME_ATTR = "username"; // NOI18N
    public static final String PASSWORD_ATTR = "password"; // NOI18N
    public static final String ADMINPORT_ATTR = "adminPort"; // NOI18N
    public static final String HTTPPORT_ATTR = "httpportnumber"; // NOI18N
    public static final String HOSTNAME_ATTR = "host"; // NOI18N
    public static final String JRUBY_HOME = "jruby.home"; // NOI18N

    
    /**
     * Enum for the current state of the server (stopped, running, etc.)
     */
    public static enum ServerState {
        STARTING,
        RUNNING,
        RUNNING_JVM_DEBUG,
        STOPPING,
        STOPPED,
        STOPPED_JVM_BP
    }
    
    /**
     * Enum for the current state of a server operation (e.g start, stop, deploy)
     */
    public static enum OperationState {
        RUNNING,
        COMPLETED,
        FAILED
    }
    

    public Map<String, String> getInstanceProperties();
    
    public String setEnvironmentProperty(String name, String value);
    
    public Future<OperationState> startServer(OperationStateListener stateListener);

    public Future<OperationState> stopServer(OperationStateListener stateListener);

    public Future<OperationState> deploy(OperationStateListener stateListener, File application, String name);

    public Future<OperationState> undeploy(OperationStateListener stateListener, String name);
    
    public String [] getModuleList();
    
    public ServerState getServerState();
    
    public void addChangeListener(ChangeListener listener);
    
    public void removeChangeListener(ChangeListener listener);

}
