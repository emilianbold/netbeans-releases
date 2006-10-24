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


package org.netbeans.modules.form;

import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;

/** The DataObject for forms.
 *
 * @author Ian Formanek, Petr Hamernik
 */
public class FormDataObject extends MultiDataObject {
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 7952143476761137063L;

    //--------------------------------------------------------------------
    // Private variables

//    /** If true, a postInit method is called after reparsing - used after createFromTemplate */
//    transient private boolean templateInit;
//    /** If true, the form is marked as modified after regeneration - used if created from template */
//    transient private boolean modifiedInit;
//    /** A flag to prevent multiple registration of ComponentRefListener */
//    transient private boolean componentRefRegistered;

    transient private FormEditorSupport formEditor;

    transient private OpenEdit openEdit;

    /** The entry for the .form file */
    FileEntry formEntry;

    //--------------------------------------------------------------------
    // Constructors

    static final long serialVersionUID =-975322003627854168L;

    public FormDataObject(FileObject ffo, FileObject jfo, FormDataLoader loader)
        throws DataObjectExistsException
    {
        super(jfo, loader);
        formEntry = (FileEntry)registerEntry(ffo);
    }

    //--------------------------------------------------------------------
    // Other methods

    @Override
    public <T extends Cookie> T getCookie(Class<T> type) {
        T retValue;
        
        if (OpenCookie.class.equals(type) || EditCookie.class.equals(type)) {
            if (openEdit == null)
                openEdit = new OpenEdit();
            retValue = type.cast(openEdit);
        } else if (type.isAssignableFrom(FormEditorSupport.class)) {
            retValue = (T) getFormEditorSupport();
        } else {
            retValue = super.getCookie(type);
        }
        return retValue;
    }

    private class OpenEdit implements OpenCookie, EditCookie {
        public void open() {
            // open form editor with form designer selected
            getFormEditorSupport().openFormEditor(true);
        }
        public void edit() {
            // open form editor with java editor selected (form not loaded)
            getFormEditorSupport().open();
        }
    }

    public FileObject getFormFile() {
        return formEntry.getFile();
    }

    public boolean isReadOnly() {
        FileObject javaFO = getPrimaryFile();
        FileObject formFO = formEntry.getFile();
        return !javaFO.canWrite() || !formFO.canWrite();
    }

    public boolean formFileReadOnly() {
        return !formEntry.getFile().canWrite();
    }

    public synchronized FormEditorSupport getFormEditorSupport() {
        if (formEditor == null) {
            formEditor = new FormEditorSupport(getPrimaryEntry(), this, getCookieSet());
        }
        return formEditor;
    }

    // PENDING remove when form_new_layout is merged to trunk
    public FormEditorSupport getFormEditor() {
        return getFormEditorSupport();
    }
    // END of PENDING

    FileEntry getFormEntry() {
        return formEntry;
    }

    /** Provides node that should represent this data object. When a node for
     * representation in a parent is requested by a call to getNode(parent) it
     * is the exact copy of this node with only parent changed. This
     * implementation creates instance <CODE>DataNode</CODE>.  <P> This method
     * is called only once.
     *
     * @return the node representation for this data object
     * @see DataNode
     */
    protected Node createNodeDelegate() {
        FormDataNode node = new FormDataNode(this);
        return node;
    }

    //--------------------------------------------------------------------
    // Serialization

    private void readObject(java.io.ObjectInputStream is)
        throws java.io.IOException, ClassNotFoundException {
        is.defaultReadObject();
    }

}
