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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
import org.openide.modules.*;
import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.SharedClassObject;
import org.openide.windows.TopComponent;

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
    
    private static Collection floatingTopComponents;

    protected Object writeReplace(){
        return null;
    }
    
    public synchronized static void registerTopComponent(TopComponent tc) {
        if (floatingTopComponents == null)
            floatingTopComponents = new java.util.LinkedList();
        floatingTopComponents.add(tc);
    }
    
    public synchronized static void unregisterTopComponent(TopComponent tc) {
        if (floatingTopComponents == null)
            return;
        floatingTopComponents.remove(tc);
    }
    
    public void uninstalled() {
        Collection c;
        synchronized (JavadocModule.class) {
            c = new ArrayList(floatingTopComponents);
        }
        for (Iterator it = c.iterator(); it.hasNext();) {
            TopComponent tc = (TopComponent)it.next();
            tc.close();
        }
    }
    
    /** Exists only for the sake of its bean info.
     * @deprecated Exists only for compability reasons
     */    
    public static final class GlobalLocalFileSystem extends LocalFileSystem {        
        private static final long serialVersionUID = 3563912690225075761L;
        
        public GlobalLocalFileSystem(){
            super();
        }
        public GlobalLocalFileSystem(FileSystemCapability cap){
            super(cap);
        }
    }
    /** Marks it as global (not project-specific). 
     * @deprecated Exists only for compability reasons
     */
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
                return null;
            }
        }
        public Image getIcon (int kind) {
            try {
                return Introspector.getBeanInfo (LocalFileSystem.class).getIcon (kind);
            } catch (IntrospectionException ie) {
                return null;
            }
        }
    }
}
