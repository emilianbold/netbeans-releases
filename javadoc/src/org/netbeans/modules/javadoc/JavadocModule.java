/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.javadoc;

import java.io.File;
import java.io.IOException;
import java.beans.*;
import java.awt.Image;

import java.util.Enumeration;
import java.lang.reflect.Method;
import javax.swing.event.*;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;

import org.openide.ErrorManager;
import org.openide.util.Utilities;
import org.openide.src.nodes.FilterFactory;
import org.openide.actions.CutAction;
import org.openide.util.actions.SystemAction;
import org.openide.modules.ModuleInstall;
import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.SharedClassObject;

import org.netbeans.modules.javadoc.settings.StdDocletSettingsService;
import org.netbeans.modules.javadoc.comments.JavaDocPropertySupportFactory;
import org.netbeans.modules.javadoc.comments.AutoCommentAction;
import org.netbeans.modules.javadoc.search.SearchDocAction;
import org.netbeans.modules.javadoc.search.DocFileSystem;

import org.openide.util.RequestProcessor;

/** Class for initializing Javadoc module on IDE startup.

 @author Petr Hrebejk
*/
public class JavadocModule extends ModuleInstall {

    /** serialVersionUID */
    private static final long serialVersionUID = 984124010415492146L;

    //private static final String PROP_INSTALL_COUNT = "installCount"; // NOI18N
    
    public static final ErrorManager err = TopManager.getDefault ().getErrorManager ().getInstance ("org.apache.tools.ant.module"); // NOI18N

    /** By first install of module in the IDE, check whether standard documentation folder
    * exists. If not creates it.
    */
    public void installed() {
        // Install Search Action 
        restored();
    }

    /** By uninstalling module from the IDE do nothing.
    */
    public void uninstalled () {
        // Unmount docs (AutoUpdate should handle actually removing the file):
    }

    /** Called on IDE startup. Registers actions for generating documentation
    * on DataFolder and JavaDataObject.
    */
    public void restored() {
       
        // Install the factory for adding JavaDoc property to nodes
        invokeDynamic( "org.netbeans.modules.java.JavaDataObject", // NOI18N
                       "addExplorerFilterFactory", // NOI18N
                       new JavaDocPropertySupportFactory() );
        invokeDynamic( "org.netbeans.modules.java.JavaDataObject", // NOI18N
                       "addBrowserFilterFactory", // NOI18N
                       new JavaDocPropertySupportFactory() );

    }

    /** Invoked on update */
    public void updated(int release, String specVersion) {
        restored();
    }

    // UTILITY METHODS ----------------------------------------------------------------------

    /** Dynamicaly invokes a method
     */
    private void invokeDynamic( String className, String methodName, FilterFactory factory ) {

        try {
            Class dataObject = TopManager.getDefault().systemClassLoader().loadClass( className );

            if ( dataObject == null )
                return;

            Method method = dataObject.getDeclaredMethod( methodName, new Class[] { FilterFactory.class }  );
            if ( method == null )
                return;

            method.invoke( null, new Object[] { factory } );
        }
        catch ( java.lang.ClassNotFoundException e ) {
            notify (e);
        }
        catch ( java.lang.NoSuchMethodException e ) {
            notify (e);
        }
        catch ( java.lang.IllegalAccessException e ) {
            notify (e);
        }
        catch ( java.lang.reflect.InvocationTargetException e ) {
            notify (e);
        }
    }
    
    // Implementation of java.io.Externalizable ------------------

    public void readExternal(final java.io.ObjectInput objectInput )
    throws java.io.IOException, java.lang.ClassNotFoundException {
        super.readExternal( objectInput );
        //putProperty (PROP_INSTALL_COUNT, new Integer(objectInput.readInt()));
        //numberOfStarts = objectInput.readInt();
    }

    public void writeExternal(final java.io.ObjectOutput objectOutput )
    throws java.io.IOException {
        super.writeExternal( objectOutput );
        //objectOutput.writeObject (getProperty (PROP_INSTALL_COUNT));
        //Integer i = (Integer)getProperty(PROP_INSTALL_COUNT);
        //objectOutput.writeInt(i != null ? i.intValue() : 0);
    }

    private static void notify (Exception e) {        
        TopManager.getDefault ().getErrorManager ().notify (ErrorManager.INFORMATIONAL, e);
    }

    private static void notify (String s) {
        if (Boolean.getBoolean ("netbeans.debug.javadoc")) // NOI18N
            TopManager.getDefault ().getErrorManager ().log (ErrorManager.INFORMATIONAL, s);
    }
  
    /** Exists only for the sake of its bean info. */
    public static final class GlobalLocalFileSystem extends LocalFileSystem {        
        private static final long serialVersionUID = 3563912690225075761L;
        
        public GlobalLocalFileSystem(){
            super();
        }
        public GlobalLocalFileSystem(FileSystemCapability cap){
            super(cap);
        }
    }
    /** Marks it as global (not project-specific). */
    public static final class GlobalLocalFileSystemBeanInfo extends SimpleBeanInfo {
        public BeanDescriptor getBeanDescriptor () {
            BeanDescriptor bd = new BeanDescriptor (GlobalLocalFileSystem.class);
            bd.setValue ("global", Boolean.TRUE); // NOI18N
            return bd;
        }
        public BeanInfo[] getAdditionalBeanInfo () {
            try {
                return new BeanInfo[] { Introspector.getBeanInfo (LocalFileSystem.class) };
            } catch (IntrospectionException ie) {
                err.notify (ie);
                return null;
            }
        }
        public Image getIcon (int kind) {
            try {
                return Introspector.getBeanInfo (LocalFileSystem.class).getIcon (kind);
            } catch (IntrospectionException ie) {
                err.notify (ie);
                return null;
            }
        }
    }
}
