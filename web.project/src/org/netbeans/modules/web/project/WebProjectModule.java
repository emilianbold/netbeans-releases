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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.swing.Action;
import org.netbeans.api.project.ProjectManager;
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

/**
 * Startup and shutdown hooks for web project module. It ensures that the
 * Nodes representing JSP files will have project specific actions (e.g.
 * compile/run/debug file) in the popup menu.
 * @author Martin Grebac
 */
public class WebProjectModule extends ModuleInstall {
    public static final String JSPC_CLASSPATH = "jspc.classpath"; //NOI18N
    public static final String COPYFILES_CLASSPATH = "copyfiles.classpath"; //NOI18N
    
    public void restored() {
        // Hack JspDataLoader actions - not very nice - but copied from JavaProjectModule
        
        DataLoaderPool dataLoaderPool = (DataLoaderPool) Lookup.getDefault().lookup(DataLoaderPool.class);
        
        try {
            Class jspDataObjectClass = Class.forName( "org.netbeans.modules.web.core.jsploader.JspDataObject", 
                                                        true, 
                                                        (ClassLoader)Lookup.getDefault().lookup( ClassLoader.class ) );
            
            DataLoader jspLoader = dataLoaderPool.firstProducerOf( jspDataObjectClass );
            
            ArrayList actions = new ArrayList(Arrays.asList(jspLoader.getActions()));
            ArrayList newActions = new ArrayList( actions.size() + 6 );

            for( Iterator it = actions.iterator(); it.hasNext(); ) {
                SystemAction a = (SystemAction)it.next();
                newActions.add( a );
                if ( a instanceof org.openide.actions.OpenAction ) {
                    newActions.add( null );
                    newActions.add( new CompileWrapper( ) );
                    newActions.add( null );
                    newActions.add( new RunWrapper(  ) );
                    newActions.add( new DebugWrapper(  ) );
                    newActions.add( null );
                }
            }


            jspLoader.setActions((SystemAction[])newActions.toArray(new SystemAction[newActions.size()]));        

            
        }
        catch( ClassNotFoundException e ) {
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
        }
        
        ProjectManager.mutex().postWriteRequest(
                new Runnable () {
                    public void run () {
                        try {
                            EditableProperties ep = PropertyUtils.getGlobalProperties();
                            boolean changed = false;
                            // JSPC classpath
                            StringBuffer sb = new StringBuffer();
                            // Ant is needed in classpath if we are forking JspC into another process
                            sb.append(InstalledFileLocator.getDefault().locate("ant/lib/ant.jar", null, false));
                            sb.append(":"); // NOI18N
                            sb.append(InstalledFileLocator.getDefault().locate("modules/autoload/ext/servlet-api-2.4.jar", null, false));
                            sb.append(":"); // NOI18N
                            sb.append(InstalledFileLocator.getDefault().locate("modules/autoload/ext/jsp-api-2.0.jar", null, false));
                            sb.append(":"); // NOI18N
                            sb.append(InstalledFileLocator.getDefault().locate("modules/autoload/ext/jasper-compiler-5.0.28.jar", null, false));
                            sb.append(":"); // NOI18N
                            sb.append(InstalledFileLocator.getDefault().locate("modules/autoload/ext/jasper-runtime-5.0.28.jar", null, false));
                            sb.append(":"); // NOI18N
                            sb.append(InstalledFileLocator.getDefault().locate("modules/autoload/ext/commons-el.jar", null, false));
                            sb.append(":"); // NOI18N
                            sb.append(InstalledFileLocator.getDefault().locate("modules/autoload/ext/commons-logging-api.jar", null, false));
                            String jspc_cp_old = ep.getProperty(JSPC_CLASSPATH);
                            String jspc_cp = sb.toString();
                            if (jspc_cp_old == null || !jspc_cp_old.equals (jspc_cp)) {
                                ep.setProperty(JSPC_CLASSPATH, jspc_cp);
                                changed = true;
                            }
                            String copy_files = InstalledFileLocator.getDefault().locate("ant/extra/copyfiles.jar", null, false).toString();
                            String copy_files_old = ep.getProperty(COPYFILES_CLASSPATH);
                            if (copy_files_old == null || !copy_files_old.equals(copy_files)) {
                                ep.setProperty(COPYFILES_CLASSPATH, copy_files);
                                changed = true;
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
            this.action.addPropertyChangeListener( this );
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
                       ActionProvider.COMMAND_COMPILE_SINGLE, "Compile File", null ) );
        }
        
    }
    
    public static class RunWrapper extends ActionWrapper {
        RunWrapper() {
            super( FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_RUN_SINGLE, "Run File", null ) );
            
        }
    }
    
    public static class DebugWrapper extends ActionWrapper {
        DebugWrapper() {
            super( FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_DEBUG_SINGLE, "Debug File", null ) );
        }
    }
    
}
