/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.beans.*;
import java.awt.Component;
import java.util.ArrayList;

import org.openide.TopManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
/**
 * Defines editor for choosing of Web browser.
 *
 * @author  Radim Kubacki
 */
public class HtmlBrowser extends Object {

    public static class FactoryEditor extends PropertyEditorSupport {
        
        /** extended attribute that signals that this object should not be visible to the user */
        private static final String EA_HIDDEN = "hidden"; // NOI18N

        private static final String BROWSER_FOLDER = "Services/Browsers"; // NOI18N
        
        /** Creates new FactoryEditor */
        public FactoryEditor () {
        }
        
        public String getAsText () {
            try {
                org.openide.awt.HtmlBrowser.Factory f = (org.openide.awt.HtmlBrowser.Factory)getValue ();
                if (f == null)
                    return NbBundle.getBundle (FactoryEditor.class).getString ("CTL_UnspecifiedBrowser");
                
                FileObject fo = TopManager.getDefault ().getRepository ()
                .getDefaultFileSystem ().findResource (BROWSER_FOLDER);
                DataFolder folder = DataFolder.findFolder (fo);
                DataObject [] dobjs = folder.getChildren ();
                for (int i = 0; i<dobjs.length; i++) {
                    Object o = ((InstanceCookie)dobjs[i].getCookie (InstanceCookie.class)).instanceCreate ();
                    if (f.equals (o))
                        return dobjs[i].getNodeDelegate ().getDisplayName ();
                }
            }
            catch (Exception ex) {
                TopManager.getDefault ().notifyException (ex);
            }
            return NbBundle.getBundle (FactoryEditor.class).getString ("CTL_UnspecifiedBrowser");
        }
        
        public boolean supportsCustomEditor () {
            return false;
        }
        
        public void setAsText (java.lang.String str) throws java.lang.IllegalArgumentException {
            try {
                if (NbBundle.getBundle (FactoryEditor.class).getString ("CTL_UnspecifiedBrowser").equals (str)) {
                    setValue (null);
                    return;
                }
                FileObject fo = TopManager.getDefault ().getRepository ()
                .getDefaultFileSystem ().findResource (BROWSER_FOLDER);
                DataFolder folder = DataFolder.findFolder (fo);
                DataObject [] dobjs = folder.getChildren ();
                for (int i = 0; i<dobjs.length; i++) {
                    if (str.equals (dobjs[i].getNodeDelegate ().getDisplayName ())) {
                        Object o = ((InstanceCookie)dobjs[i].getCookie (InstanceCookie.class)).instanceCreate ();
                        setValue (o);
                        return;
                    }
                }
            }
            catch (Exception ex) {
                TopManager.getDefault ().notifyException (ex);
                return;
            }
        }
        
        public java.lang.String[] getTags () {
            FileObject fo = TopManager.getDefault ().getRepository ()
            .getDefaultFileSystem ().findResource (BROWSER_FOLDER);
            DataFolder folder = DataFolder.findFolder (fo);
            DataObject [] dobjs = folder.getChildren ();
            ArrayList list = new ArrayList (dobjs.length);
            for (int i = 0; i<dobjs.length; i++) {
                if (!Boolean.TRUE.equals (dobjs[i].getPrimaryFile ().getAttribute (EA_HIDDEN)))
                    list.add (dobjs[i].getNodeDelegate ().getDisplayName ());
            }
            String[] retValue = new String[list.size ()];
            
            list.toArray (retValue);
            return retValue;
        }
        
    }
                
}
