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

package org.openide.loaders;

import java.io.*;
import java.util.HashSet;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;

import org.openide.filesystems.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.actions.SystemAction;

/** An implementation of a data object which consumes file objects not recognized by any other loaders.
*
* @author Ian Formanek
*/
final class DefaultDataObject extends MultiDataObject 
implements org.openide.cookies.OpenCookie {
    static final long serialVersionUID =-4936309935667095746L;
    
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 6305590675982925167L;

    /** Constructs new data shadow for given primary file and referenced original.
    * @param fo the primary file
    * @param original original data object
    */
    DefaultDataObject (FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super (fo, loader);
    }
 
    /* Creates node delegate.
    */
    protected Node createNodeDelegate () {
        DataNode dn = new DataNode (this, org.openide.nodes.Children.LEAF);
        
        // netbeans.core.nodes.description    
        dn.setShortDescription (NbBundle.getMessage (DefaultDataObject.class, 
                                "HINT_DefaultDataObject")); // NOI18N
        return dn;
    }
   
    /** Get the name of the data object.
    * <p>The implementation uses the name of the primary file and its exten.
    * @return the name
    */
    
    public String getName() {
        return getPrimaryFile ().getNameExt ();
    }

    /* Help context for this object.
    * @return help context
    */
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }

    
    /* Handles renaming of the object.
    * Must be overriden in children.
    *
    * @param name name to rename the object to
    * @return new primary file of the object
    * @exception IOException if an error occures
    */
    protected FileObject handleRename (String name) throws IOException {
        FileLock lock = getPrimaryFile ().lock ();
        int pos = name.lastIndexOf('.');
        
        try {
            if (pos < 0){
                // file without separator
                getPrimaryFile ().rename (lock, name, null);
            } else if (pos == 0){
                getPrimaryFile ().rename (lock, name, getPrimaryFile ().getExt ());
            } else {
                if (!name.equals(getPrimaryFile ().getNameExt())){
                    getPrimaryFile ().rename (lock, name.substring(0, pos), 
                        name.substring(pos+1, name.length()));
                    DataObjectPool.getPOOL().revalidate(
                        new HashSet (java.util.Collections.singleton(getPrimaryFile ()))
                    );
                }
            }
        } finally {
            lock.releaseLock ();
        }
        return getPrimaryFile ();
    }
    
    /* Creates new object from template.
    * @exception IOException
    */
    protected DataObject handleCreateFromTemplate (
        DataFolder df, String name
    ) throws IOException {
        // avoid doubling of extension
        if (name != null && name.endsWith("." + getPrimaryFile ().getExt ())) // NOI18N
            name = name.substring(0, name.lastIndexOf("." + getPrimaryFile ().getExt ())); // NOI18N
        
        return super.handleCreateFromTemplate (df, name);
    }
    
    /** Either opens the in text editor or asks user questions.
     */
    public void open() {
        EditorCookie ic = (EditorCookie)getCookie (EditorCookie.class);
        if (ic != null) {
            ic.open();
        } else {
            // ask a query 
            java.util.ArrayList options = new java.util.ArrayList ();
            options.add (NotifyDescriptor.OK_OPTION);
            options.add (NotifyDescriptor.CANCEL_OPTION);
            NotifyDescriptor nd = new NotifyDescriptor (
                NbBundle.getMessage (DefaultDataObject.class, "MSG_BinaryFileQuestion"),
                NbBundle.getMessage (DefaultDataObject.class, "MSG_BinaryFileWarning"),
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                options.toArray(), null
            );
            Object ret = DialogDisplayer.getDefault().notify (nd);
            if (ret != NotifyDescriptor.OK_OPTION) {
                return;
            }
            
            EditorCookie c = (EditorCookie)getCookie (EditorCookie.class, true);
            c.open ();
        }
    }

    /** We implement OpenCookie and sometimes we also have cloneable
     * editor cookie */
    public org.openide.nodes.Node.Cookie getCookie(Class c) {
        return getCookie (c, false);
    }
    
    /** Getter for cookie.
     * @param force if true, there are no checks for content of the file
     */
    final org.openide.nodes.Node.Cookie getCookie (Class c, boolean force) {
        if (c == org.openide.cookies.OpenCookie.class) {
            return this;
        }

        org.openide.nodes.Node.Cookie cook = super.getCookie (c);
        if (cook != null) {
            return cook;
        }
            
        if (
            c.isAssignableFrom(org.openide.cookies.EditCookie.class)
            ||
            c.isAssignableFrom(org.openide.cookies.EditorCookie.Observable.class)
            ||
            c.isAssignableFrom (org.openide.cookies.PrintCookie.class)
            ||
            c.isAssignableFrom (org.openide.cookies.CloseCookie.class)
            ||
            c == DefaultES.class
        ) {
            try {
                if (!force) {
                    // try to initialize the editor cookie set if the file 
                    // seems editable
                    byte[] arr = new byte[2048];
                    InputStream is = getPrimaryFile().getInputStream();
                    try {
                        int len = is.read (arr);
                        for (int i = 0; i < len; i++) {
                            if (arr[i] >= 0 && arr[i] <= 31 && arr[i] != '\n' && arr[i] != '\r' && arr[i] != '\t') {
                                return null;
                            }
                        }
                    } finally {
                        is.close ();
                    }
                }
                DefaultES support = new DefaultES (
                    this, getPrimaryEntry(), getCookieSet ()
                );
                getCookieSet().add ((Node.Cookie)support);
                return getCookieSet ().getCookie(c);
            } catch (IOException ex) {
                // XXX
                ex.printStackTrace();
            }
        }
        return null;
    }
}
