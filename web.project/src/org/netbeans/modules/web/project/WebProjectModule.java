/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.swing.Action;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.loaders.DataLoaderPool;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.openide.loaders.DataLoader;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.SystemAction;
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
                            sb.append(InstalledFileLocator.getDefault().locate("ant/lib/ant.jar", null, false))
                            .append(":${" + WebProjectProperties.J2EE_PLATFORM_CLASSPATH + "}:") // NOI18N
                            .append(InstalledFileLocator.getDefault().locate("modules/autoload/ext/jasper-compiler-5.5.7.jar", null, false))
                            .append(":") // NOI18N
                            .append(InstalledFileLocator.getDefault().locate("modules/autoload/ext/jasper-runtime-5.5.7.jar", null, false))
                            .append(":") // NOI18N
                            .append(InstalledFileLocator.getDefault().locate("modules/autoload/ext/commons-el.jar", null, false))
                            .append(":") // NOI18N
                            .append(InstalledFileLocator.getDefault().locate("modules/ext/commons-logging-1.0.4.jar", null, false));
                            String jspc_cp_old = ep.getProperty(JSPC_CLASSPATH);
                            String jspc_cp = sb.toString();
                            if (jspc_cp_old == null || !jspc_cp_old.equals (jspc_cp)) {
                                ep.setProperty(JSPC_CLASSPATH, jspc_cp);
                                changed = true;
                            }
                            File copy_files = InstalledFileLocator.getDefault().locate("ant/extra/copyfiles.jar", null, false);
                            if (copy_files == null) {
                                String msg = NbBundle.getMessage(ProjectWebModule.class,"MSG_CopyFileMissing"); //NOI18N
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
    
            
    public static class ActionWrapper extends CallableSystemAction implements ContextAwareAction, PropertyChangeListener {
        
        private Action action;
        
        public ActionWrapper( Action action ) {
            this.action = action;
        }
            
        public String getName() {
            return (String)action.getValue( Action.NAME );
        }

        public String iconResource() {
            return null;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        protected boolean asynchronous() {
            return false;
        }

        public void actionPerformed( ActionEvent ev ) {
            action.actionPerformed(ev);
        }
        
        public boolean isEnabled() {
            return action.isEnabled();            
        }
        
        protected void addNotify() {
            this.action.addPropertyChangeListener( this );
            super.addNotify();
        }
        
        protected void removeNotify() {
            this.action.removePropertyChangeListener( this );
            super.removeNotify();
        }
        
        public void performAction() {
            actionPerformed( new ActionEvent( this, 0, "" ) ); // NOI18N
        }
        
        public Action createContextAwareInstance( Lookup actionContext ) {
            return ((ContextAwareAction)action).createContextAwareInstance( actionContext );
        }
        
        public void propertyChange( PropertyChangeEvent evt ) {
            firePropertyChange( evt.getPropertyName(), evt.getOldValue(), evt.getNewValue() );
        }
        
    }
    
    public static class CompileWrapper extends ActionWrapper {
        
        CompileWrapper() {
            super( FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_COMPILE_SINGLE,
                       NbBundle.getMessage(WebProjectModule.class, "LBL_CompileFile_Action"), // NOI18N
                       null ) );
        }
        
    }
    
    public static class RunWrapper extends ActionWrapper {
        RunWrapper() {
            super( FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_RUN_SINGLE, 
                       NbBundle.getMessage(WebProjectModule.class, "LBL_RunFile_Action"), // NOI18N
                       null ) );
            
        }
    }
    
    public static class DebugWrapper extends ActionWrapper {
        DebugWrapper() {
            super( FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_DEBUG_SINGLE, 
                       NbBundle.getMessage(WebProjectModule.class, "LBL_DebugFile_Action"), // NOI18N
                       null ) );
        }
    }
    
    public static class HtmlRunWrapper extends ActionWrapper {
        HtmlRunWrapper() {
            super( FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_RUN_SINGLE, 
                       NbBundle.getMessage(WebProjectModule.class, "LBL_RunFile_Action"), // NOI18N
                       null ) );
            
        }
    }
    
    public static class HtmlDebugWrapper extends ActionWrapper {
        HtmlDebugWrapper() {
            super( FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_DEBUG_SINGLE, 
                       NbBundle.getMessage(WebProjectModule.class, "LBL_DebugFile_Action"), // NOI18N
                       null ) );
        }
    }
}
