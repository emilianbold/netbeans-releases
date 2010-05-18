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
package org.netbeans.modules.soa.ui.form;

import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * This interface describe subsidiary methods for a Custom Editor which
 * is intended to show property of a Node.
 *
 * @author nk160297
 */
public interface CustomNodeEditor<T>
        extends EditorLifeCycle, Lookup.Provider, ValidStateManager.Provider, 
        HelpCtx.Provider {

    /**
     * This string is used as a key to set property to different UI controls
     * which should be bound to a Node.Property.
     */
    String PROPERTY_BINDER = "PropertyBinder"; // NOI18N
    
    /**
     * Returns the Node which properties the editor shows. 
     */
    Node getEditedNode();

    /**
     * Returns the original object which is edited
     */
    T getEditedObject();
    
    /**
     * This method does part of standard steps to process Ok button. 
     * It is intended to be used internally by the NodeEditorDescriptor only.
     * It's recommend to avoid using the method. 
     * Returns the success flag.
     */
    boolean doValidateAndSave();
    
    /**
     * Indicates the current editting mode of the editor. 
     */ 
    EditingMode getEditingMode();
    
    /**
     * This method change the current editing mode. 
     * It should be used carefully!
     * Usually the editing mode can be change at initialization stage.
     */ 
    void setEditingMode(EditingMode newValue);
    
    enum EditingMode {
        NOT_SPECIFIED, 
        CREATE_NEW_INSTANCE, // The editor shows an object which is just created.
        EDIT_INSTANCE // The editor shows an old object.
    };
    
    interface Owner {
        void setEditor(CustomNodeEditor editor);
        CustomNodeEditor getEditor();
    }
}
