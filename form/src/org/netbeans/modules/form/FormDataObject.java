/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.form;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.cookies.*;
import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.java.JavaEditor;
import org.netbeans.modules.form.*;

/** The DataObject for forms.
 *
 * @author Ian Formanek, Petr Hamernik
 */
public class FormDataObject extends JavaDataObject {
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
        init();
    }

    /** Initalizes the FormDataObject after deserialization */
    private void init() {
        getCookieSet().add(new Class[] { OpenCookie.class, EditCookie.class},
                           this);
    }

    //--------------------------------------------------------------------
    // Other methods

    // CookieSet.Factory implementation
    public Node.Cookie createCookie(Class klass) {
        if (OpenCookie.class.equals(klass)
            || EditCookie.class.equals(klass))
        {
            if (openEdit == null)
                openEdit = new OpenEdit();
            return openEdit;
        }

        return super.createCookie(klass);
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

    // from JavaDataObject
    protected JavaEditor createJavaEditor() {
        if (formEditor == null)
            formEditor = new FormEditorSupport(getPrimaryEntry(), this);
        return formEditor;
    }

    public FormEditorSupport getFormEditorSupport() {
        return (FormEditorSupport) getJavaEditor();
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
        init();
    }

}
