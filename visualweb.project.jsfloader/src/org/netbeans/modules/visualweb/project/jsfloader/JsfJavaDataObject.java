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


package org.netbeans.modules.visualweb.project.jsfloader;


import java.io.IOException;

import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.java.JavaEditor;

import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

import org.netbeans.modules.visualweb.api.insync.JsfJavaDataObjectMarker;
import org.netbeans.modules.visualweb.project.jsf.api.JsfDataObjectException;


/**
 * Object that represents one JSF java data object.
 *
 * @author Peter Zavadsky
 */
public class JsfJavaDataObject extends JavaDataObject implements JsfJavaDataObjectMarker {


    static final long serialVersionUID =8354927561693097159L;

    public JsfJavaDataObject(FileObject pf, JsfJavaDataLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        init();
    }


    private void init() {
        CookieSet set = getCookieSet();
        set.add(JsfJavaEditorSupport.class, this);
    }

    /** Gets the superclass cookie, without hacking save cookie. */
    Node.Cookie getPureCookie(Class clazz) {
        return super.getCookie(clazz);
    }

    /** Overrides behaviour to provide compound save cookie. */
    public Node.Cookie getCookie(Class clazz) {
        if(clazz == SaveCookie.class){
            FileObject primaryJsfFileObject = Utils.findJspForJava(getPrimaryFile());
            if( primaryJsfFileObject != null && primaryJsfFileObject.isValid()) {
                SaveCookie javaSaveCookie = (SaveCookie)super.getCookie(clazz);
                JsfJspDataObject jsfJspDataObject = (JsfJspDataObject) getCookie(JsfJspDataObject.class);
                SaveCookie jspSaveCookie;
                if(jsfJspDataObject == null) {
                    jspSaveCookie = null;
                } else {
                    jspSaveCookie = (SaveCookie)jsfJspDataObject.getPureCookie(clazz);
                }

                if(javaSaveCookie == null && jspSaveCookie == null) {
                    return null;
                } else {
                    return new CompoundSaveCookie(javaSaveCookie, jspSaveCookie);
                }
            }
        }
        return super.getCookie(clazz);
    }

    /** Hacking access to be able to add the save cookie, see the JsfJavaEditorSupport. */
    void addSaveCookie(SaveCookie save) {
        getCookieSet().add(save);
    }

    protected Node createNodeDelegate () {
        return new JsfJavaDataNode(this);
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx(JsfJavaDataLoader.class.getName () + ".Obj"); // NOI18N
    }

    /** Creates new Cookie */
    public Node.Cookie createCookie(Class klass) {
        if(klass.isAssignableFrom(JsfJavaEditorSupport.class)) {
            // XXX See above.
            return getJavaEditor();
        }

        return super.createCookie(klass);
    }

    private JsfJavaEditorSupport jsfJavaEditor;

    // from JavaDataObject
    protected JavaEditor createJavaEditor() {
        if(jsfJavaEditor == null) {
            jsfJavaEditor = new JsfJavaEditorSupport(this);
        }
        return jsfJavaEditor;
    }


    //--------------------------------------------------------------------
    // Serialization

    private void readObject(java.io.ObjectInputStream is)
    throws IOException, ClassNotFoundException {
        is.defaultReadObject();
        init();
    }



////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// <rave>
// XXX Hacking problems with cut/copy of .jsp and .java backing file
//... dont address to NB, resolve by creating new dataobject type.. possibly maintained with the help of project.
    
    private static final ThreadLocal pureCopy = new ThreadLocal();
    
    /** Copies only this object without touching the corresponding jsf jsp one.
     * Used when copying originated form corresponding file. */
    void pureCopy(DataFolder folder) throws IOException {
        try {
            pureCopy.set(Boolean.TRUE);
            copy(folder);
        } finally {
            pureCopy.set(Boolean.FALSE);
        }
    }
    
    /** Handles copy. Handles also copy of corresponding jsf jsp file. */
    protected DataObject handleCopy(DataFolder folder) throws IOException {
        if(pureCopy.get() == Boolean.TRUE) {
            return super.handleCopy(folder);
        } else {
            FileObject jspFile = Utils.findJspForJava(getPrimaryFile());
            if(jspFile == null) {
                throw new JsfDataObjectException("Can't find jsp file for " + this);
            }

            DataObject dataObject = super.handleCopy(folder);

            try {
                DataObject jspDataObject = DataObject.find(jspFile);
                if(jspDataObject instanceof JsfJspDataObject) {
                    FileObject jspFolder = Utils.findJspFolderForJava(dataObject.getPrimaryFile());
                    DataFolder jspDataFolder = DataFolder.findFolder(jspFolder);
                    ((JsfJspDataObject)jspDataObject).pureCopy(jspDataFolder);
                }
            } catch(DataObjectNotFoundException dnfe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dnfe);
            }

            return dataObject;
        }
    }
    
    /**
     * Capture the name the data object had, before doing the rename and update my editor title.
     * 
     */
    protected FileObject handleRename(String name) throws IOException {
        FileObject fo = super.handleRename(name);

        // XXX Also handle renaming of the multiview.
        final JsfJavaEditorSupport jsfJavaEditorSupport = (JsfJavaEditorSupport)getCookie(JsfJavaEditorSupport.class);
        if(jsfJavaEditorSupport != null) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                   jsfJavaEditorSupport.updateMultiViewDisplayName();
               } 
            });
        }
            
        return fo;
    }
    
    /** Hacking access to be able to remove the save cookie, see the JsfJavaEditorSupport. */
    protected void removeSaveCookie(SaveCookie save) {
        // This does not look quite right, but it should work.
        if (save instanceof CompoundSaveCookie) {
            CompoundSaveCookie compound = (CompoundSaveCookie) save;
            SaveCookie pureSave = (SaveCookie) getPureCookie(SaveCookie.class);
            if (compound.containsCookie(pureSave)) {
                getCookieSet().remove(pureSave);
            }
        }
        getCookieSet().remove(save);
    }
// </rave>
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
   
}
    
