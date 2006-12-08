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

package org.netbeans.modules.web.project;

import java.io.IOException;
import java.io.File;
import javax.swing.Action;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * Startup and shutdown hooks for web project module. It defines 
 * the project specific actions (e.g.compile/run/debug file) for the
 * nodes representing JSP and html files. These actions are registered
 * for their mime types in layer.
 *
 * @author Martin Grebac
 */
public class WebProjectModule extends ModuleInstall {
    public static final String JSPC_CLASSPATH = "jspc.classpath"; //NOI18N
    public static final String COPYFILES_CLASSPATH = "copyfiles.classpath"; //NOI18N
    
    public void restored() {
        
        ProjectManager.mutex().postWriteRequest(
                new Runnable () {
                    public void run () {
                        try {
                            EditableProperties ep = PropertyUtils.getGlobalProperties();
                            boolean changed = false;
                            // JSPC classpath
                            StringBuffer sb = new StringBuffer(450);
                            // Ant is needed in classpath if we are forking JspC into another process
                            sb.append(InstalledFileLocator.getDefault().locate("ant/lib/ant.jar", null, false)) //NOI18N
                            .append(':') // NOI18N
                             //XXX This is fix for issue #74250. This is a hack and should be solved in the glassfish's jasper 
                            // also it must be moved before J2EE_PLATFORM_CLASSPATH, because a server (JBoss) can expose
                            // old jsp api, and the compiler is not then able compile some jsp pages with tag lib declarations
                            .append(InstalledFileLocator.getDefault().locate("modules/ext/servlet2.5-jsp2.1-api.jar", null, false)) //NOI18N
                            .append(":${" + WebProjectProperties.J2EE_PLATFORM_CLASSPATH + "}:") // NOI18N
                            .append(InstalledFileLocator.getDefault().locate("modules/ext/glassfish-jspparser.jar", null, false)) //NOI18N
                            .append(':') // NOI18N
                            .append(InstalledFileLocator.getDefault().locate("modules/ext/glassfish-logging.jar", null, false)) //NOI18N
                            .append(':') // NOI18N
                            .append(InstalledFileLocator.getDefault().locate("modules/ext/commons-logging-1.0.4.jar", null, false)); //NOI18N
                            
                            String jspc_cp_old = ep.getProperty(JSPC_CLASSPATH);
                            String jspc_cp = sb.toString();
                            if (jspc_cp_old == null || !jspc_cp_old.equals (jspc_cp)) {
                                ep.setProperty(JSPC_CLASSPATH, jspc_cp);
                                changed = true;
                            }
                            File copy_files = InstalledFileLocator.getDefault().locate("ant/extra/copyfiles.jar", null, false); //NOI18N
                            if (copy_files == null) {
                                String msg = NbBundle.getMessage(WebProjectModule.class,"MSG_CopyFileMissing"); //NOI18N
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                            } else {
                                String copy_files_old = ep.getProperty(COPYFILES_CLASSPATH);
                                if (copy_files_old == null || !copy_files_old.equals(copy_files.toString())) {
                                    ep.setProperty(COPYFILES_CLASSPATH, copy_files.toString());
                                    changed = true;
                                }
                            }
                            if (changed) {
                                PropertyUtils.putGlobalProperties (ep);
                            }
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify (ioe);
                        }
                    }
                }
        );
    }
    
    public static Action compile() {
        return FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_COMPILE_SINGLE, 
                       NbBundle.getMessage(WebProjectModule.class, "LBL_CompileFile_Action"), // NOI18N
                       null );
    }
            
    public static Action run() {
        return FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_RUN_SINGLE, 
                       NbBundle.getMessage(WebProjectModule.class, "LBL_RunFile_Action"), // NOI18N
                       null );
    }
    
    public static Action debug() {
        return FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_DEBUG_SINGLE, 
                       NbBundle.getMessage(WebProjectModule.class, "LBL_DebugFile_Action"), // NOI18N
                       null );
    }

    public static Action htmlRun() {
        return FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_RUN_SINGLE, 
                       NbBundle.getMessage(WebProjectModule.class, "LBL_RunFile_Action"), // NOI18N
                       null );
    }

    public static Action htmlDebug() {
        return FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_DEBUG_SINGLE, 
                       NbBundle.getMessage(WebProjectModule.class, "LBL_DebugFile_Action"), // NOI18N
                       null );
    }
    
}
