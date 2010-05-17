/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.visualweb.project.jsfloader;


import java.io.IOException;

import org.openide.ErrorManager;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.HelpCtx;

import org.netbeans.modules.visualweb.api.insync.JsfJavaDataObjectMarker;
import org.netbeans.modules.visualweb.project.jsf.api.JsfDataObjectException;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.CookieSet;
import org.openide.util.Lookup;


/**
 * Object that represents one JSF java data object.
 *
 * @author Peter Zavadsky
 */
public class JsfJavaDataObject extends MultiDataObject implements JsfJavaDataObjectMarker, CookieSet.Factory {


    static final long serialVersionUID =8354927561693097159L;
    static final String JSF_ATTRIBUTE = "jsfjava"; // NOI18N
    
    public JsfJavaDataObject(FileObject pf, JsfJavaDataLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        
        CookieSet set = getCookieSet();
        set.add(OpenCookie.class, this);
        set.add(EditCookie.class, this);
        set.add(EditorCookie.class, this);
        set.add(JsfJavaEditorSupport.class, this);
    }

    /** Gets the superclass cookie, without hacking save cookie. */
    <T extends Node.Cookie> T getPureCookie(Class<T> clazz) {
        return super.getCookie(clazz);
    }

    private OpenEdit openEdit = null;
    
    /** Overrides behaviour to provide compound save cookie. */
    @Override
    public <T extends Node.Cookie> T getCookie(Class<T> clazz) {
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
                    return clazz.cast(new CompoundSaveCookie(javaSaveCookie, jspSaveCookie));
                }
            }
        }else if (clazz.isAssignableFrom(JsfJavaEditorSupport.class)) {
            return clazz.cast(getJsfJavaEditorSupport());
        }

        return super.getCookie(clazz);
    }

    public <T extends Cookie> T createCookie(Class<T> klass) {
        if (OpenCookie.class.equals(klass) || EditCookie.class.equals(klass)) {
            if (openEdit == null) {
                openEdit = new OpenEdit();
            }
            return klass.cast(openEdit);
        }else if (EditorCookie.class.equals(klass) || JsfJavaEditorSupport.class.equals(klass)) {
            return klass.cast(getJsfJavaEditorSupport());
        }else {
            return null;
        }
    }
    
    
    /** Hacking access to be able to add the save cookie, see the JsfJavaEditorSupport. */
    void addSaveCookie(SaveCookie save) {
        getCookieSet().add(save);
    }

    @Override
    protected Node createNodeDelegate () {
        return new JsfJavaDataNode(this);
    }

    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx("org.netbeans.modules.visualweb.project.jsfloader.JsfJavaDataLoader" + ".Obj"); // NOI18N
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    private JsfJavaEditorSupport jsfJavaEditor;

    protected JsfJavaEditorSupport getJsfJavaEditorSupport() {
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
    }



////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// <rave>
// XXX Hacking problems with cut/copy of .jsp and .java backing file
//... dont address to NB, resolve by creating new dataobject type.. possibly maintained with the help of project.
    
    private static final ThreadLocal<Boolean> pureCopy = new ThreadLocal<Boolean>();
    
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
    @Override
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
    @Override
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
    
    private class OpenEdit implements OpenCookie, EditCookie {
        public void open() {
            getJsfJavaEditorSupport().open();
        }
        public void edit() {
            getJsfJavaEditorSupport().open();
        }
    }
// </rave>
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
    
