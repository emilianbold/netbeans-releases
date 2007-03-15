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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.apisupport.project.metainf;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.openide.filesystems.FileObject;

/**
 * Wrapper for testing purpose. I am too lazy to initialize NbModuleProject project in unittests
 * so in order to I created special usecase when the project is null
 * @author pzajac
 */
 class SUtil {
    static final String LOG_SET_KEYS = "setting keys"; 
    static final String LOG_COMPUTE_KEYS = "computing services keys";  
    static final String LOG_END_COMPUTE_KEYS = "compute keys finished";
    static final String LOG_SERVICE_NODE_HANDLER_ADD_NOTIFY = "ServiceNodeHandler.addNotify()";
    static Logger logger ;
    
    static FileObject projectDir;
    static Set platformJars;
    /** Creates a new instance of SUtil */
    public SUtil() {
    }

    static Logger getLogger() {
        if (logger == null) {
         logger = Logger.getLogger("apisupport.meta-inf"); // NOI18N
         logger.setLevel(Level.OFF);
        }
        return logger;
    }  
    
    static void log(String message) {
        getLogger().log(Level.INFO, message);
    }

//    static String getCodeNameBase(NbModuleProject nbModuleProject) {
//        return (nbModuleProject == null) ? 
//	    codeBaseName : 
//	    nbModuleProject.getCodeNameBase();
//    }

//    static FileObject getServicesFolder(NbModuleProject project) {
//        FileObject dir = (project != null) ? project.getProjectDirectory() : projectDir;
//	return (dir != null) ? 
//	    dir.getFileObject("src/" + Service.META_INF_SERVICES):
//	    null; 
//    }
    
   static FileObject getServicesFolder(Project project,boolean create) throws IOException {
        NbModuleProvider info = project.getLookup().lookup(NbModuleProvider.class);
        FileObject srcDir = project.getProjectDirectory().getFileObject(info.getResourceDirectoryPath(false));
        if (srcDir != null) {
            FileObject services = srcDir.getFileObject("META-INF/services"); //NOI18N
            if (services == null && create) {
                FileObject fo = srcDir.getFileObject("META-INF"); //NOI18N
                if (fo == null) {
                    fo = srcDir.createFolder("META-INF"); //NOI18N
                }
                services = fo.createFolder("services"); //NOI18N
            }
            return services;
        } else {
            Util.err.log(project.getProjectDirectory().getPath() + " doesn't contain source directory.");
            return null;
        }
    }

    static Set getPlatformJars() {
	return  (platformJars == null) ?
	    Collections.EMPTY_SET:       
	    platformJars;
    }

       /**
     * Centers the component <CODE>c</CODE> on the screen.  
     * 
     * @param  c  the component to center
     * @see  #centerComponent(Component, Component)
     */
	public static void centerComponent(Component c) {
		centerComponent(c, null);
	}
 
	/**
	 * Centers the component <CODE>c</CODE> on component <CODE>p</CODE>.  
	 * If <CODE>p</CODE> is <CODE>null</CODE>, the component <CODE>c</CODE> 
	 * will be centered on the screen.  
	 * 
	 * @param  c  the component to center
	 * @param  p  the parent component to center on or null for screen
	 * @see  #centerComponent(Component)
	 */
	public static void centerComponent(Component c, Component p) {
		if(c == null) {
			return;
		}
		Dimension d = (p != null ? p.getSize() : 
			Toolkit.getDefaultToolkit().getScreenSize()
		);
		c.setLocation(
			Math.max(0, (d.getSize().width/2)  - (c.getSize().width/2)), 
			Math.max(0, (d.getSize().height/2) - (c.getSize().height/2))
		);
	}

//    public static Project getProject(Node[] nodes) { 
//        ArrayList dobjs = new ArrayList();  
//        Project prj = null;
//        if (nodes.length == 1) {
//            prj = (Project) nodes[0].getLookup().lookup(Project.class);
//        }
//        if (prj == null) {
//            for(int n = 0 ; n < nodes.length ; n++) {
//                DataObject dobj = (DataObject) nodes[n].getCookie(DataObject.class);
//                if (dobj == null) {
//                    return null;
//                } 
//                dobjs.add(dobj);
//            }
//            prj = getProject(dobjs);
//        } 
//        return prj;
//    }
    
}
