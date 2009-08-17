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


package org.netbeans.modules.compapp.debugger;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.SessionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;


/**
 * Session provider for attaching to a remote BPEL service engine.
 *
 * @author Sun Microsystems
 * @author Sun Microsystems
 */
public class CompAppSessionProvider extends SessionProvider {
    /**
     * Public ID used for registration in Meta-inf/debugger.
     * In reality, it is used only by the CompApp module itself to register a debug
     * session with the ID of "CompAppDebugSession".
     */
    public static final String DEBUGGER_INFO_ID = "CompAppDebuggerInfo"; //NOI18N
    
    /**
     * Public ID used for registration in Meta-inf/debugger.
     * Various modules would use this ID to provide their Debugger Engines
     */
    public static final String SESSION_ID = "CompAppDebugSession"; // NOI18N
    
    private ContextProvider mContextProvider;
    private Map mParams;
    
    
    public CompAppSessionProvider(ContextProvider contextProvider) {
        this.mContextProvider = contextProvider;
        mParams = (Map)contextProvider.lookupFirst(null, Map.class);
    };
    
    
    public String getSessionName() {
        String projectBaseDir = (String)mParams.get("projectBaseDir"); //NOI18N

        //return the compapp project name plus "(BPEL)" as the debug session name.
        File projFolder = new File(projectBaseDir);
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(projFolder));
        Project proj = null;
        try {
            proj = ProjectManager.getDefault().findProject(fo);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        ProjectInformation info = (ProjectInformation)proj.getLookup().lookup(ProjectInformation.class);
        String projectName = info.getDisplayName();
        //As we know that we support BPEL debug only and we don't notify users with some
        //message that other modules are not debuggable (specifically, users might expect
        //that their J2EE modules are debuggable),
        //we need to add "(BPEL)" to the session name.
        return projectName + " (BPEL)"; //NOI18N
    }
    
    public String getLocationName() {
        String j2eeServerInstance = (String)mParams.get("j2eeServerInstance");
        return getServerInstanceHost(j2eeServerInstance);
    }
    
    public String getTypeID() {
        return SESSION_ID;
    }
    
    public Object[] getServices() {
        return new Object[0];
    }
    
    private static String getServerInstanceHost(String j2eeServerInstance) {
        //the j2eeServerInstance has the following format:
        //"[C:\Sun\glassfish_b32]deployer:Sun:AppServer::localhost:4848"
        //so we can obtain a host from the string itself
        //TODO:is there a more reliable way to obtain a host?
        int to = j2eeServerInstance.lastIndexOf(":");
        int from = j2eeServerInstance.lastIndexOf(":", to - 1);
        return j2eeServerInstance.substring(from + 1, to);
    }
}
