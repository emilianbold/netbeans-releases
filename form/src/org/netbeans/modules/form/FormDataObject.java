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

/* $Id$ */

package org.netbeans.modules.form;

import java.io.IOException;
import java.text.MessageFormat;

import org.openide.*;
import org.openide.actions.OpenAction;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.Node;
import org.openide.nodes.CookieSet;
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

    /** If true, a postInit method is called after reparsing - used after createFromTemplate */
    transient private boolean templateInit;
    /** If true, the form is marked as modified after regeneration - used if created from template */
    transient private boolean modifiedInit;
    /** A flag to prevent multiple registration of ComponentRefListener */
    transient private boolean componentRefRegistered;

    transient private FormEditorSupport formEditor;

    /** The entry for the .form file */
    FileEntry formEntry;

    //--------------------------------------------------------------------
    // Constructors

    static final long serialVersionUID =-975322003627854168L;
    public FormDataObject(FileObject ffo, FileObject jfo, FormDataLoader loader) throws DataObjectExistsException {
        super(jfo, loader);
        formEntry =(FileEntry)registerEntry(ffo);
        init();
    }

    /** Initalizes the FormDataObject after deserialization */
    private void init() {
        templateInit = false;
        modifiedInit = false;
        componentRefRegistered = false;
    }

    //--------------------------------------------------------------------
    // Important interface

    //--------------------------------------------------------------------
    // Other methods

    public FileObject getFormFile() {
        return getFormEntry().getFile();
    }

    public boolean isReadOnly() {
        FileObject javaFO = getPrimaryFile();
        FileObject formFO = formEntry.getFile();
        return javaFO.isReadOnly() || formFO.isReadOnly();
    }

    public boolean formFileReadOnly() {
        return formEntry.getFile().isReadOnly();
    }

    protected JavaEditor createJavaEditor() {
        if (formEditor == null) {
            formEditor = new FormEditorSupport(getPrimaryEntry(), this);
        }
        return formEditor;
    }

    public FormEditorSupport getFormEditor() {
        return(FormEditorSupport)createJavaEditor();
    }

    FileEntry getFormEntry() {
        return formEntry;
    }

    /** Help context for this object.
     * @return help context
     */
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(FormDataObject.class);
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
        node.setDefaultAction(SystemAction.get(OpenAction.class));

        node.addPropertyChangeListener(new java.beans.PropertyChangeListener () {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (Node.PROP_NAME.equals(e.getPropertyName())) {
                    RADComponentNode rootNode =
                        (RADComponentNode)formEditor.getFormRootNode();
                    if (rootNode != null)
                        rootNode.updateName();
                    
                    FormTopComponent topComp = formEditor.getFormTopComponent();
                    if (topComp != null)
                        topComp.setName(java.text.MessageFormat.format(
                            FormEditor.getFormBundle ().getString("FMT_FormWindowTitle"),
                            new Object[] { (String)e.getNewValue() }));
                }
            }
        });
        
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
