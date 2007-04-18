/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
