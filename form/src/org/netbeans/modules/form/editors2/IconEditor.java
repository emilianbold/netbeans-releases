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

package org.netbeans.modules.form.editors2;

import java.beans.PropertyEditor;
import java.io.IOException;
import org.netbeans.modules.form.NamedPropertyEditor;
import org.netbeans.modules.form.ResourceValue;
import org.netbeans.modules.form.ResourceWrapperEditor;
import org.netbeans.modules.form.editors.IconEditor.NbImageIcon;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Resource wrapper for IconEditor from editors package.
 */
public class IconEditor extends ResourceWrapperEditor implements NamedPropertyEditor, XMLPropertyEditor {

    public IconEditor() {
        super(new org.netbeans.modules.form.editors.IconEditor());
    }

    public PropertyEditor getDelegatedPropertyEditor() {
        // hack for saving: for compatibility we want this editor to be used
        // for saving (its class name to be written to .form file);
        // so the delegate editor is not exposed
        return this;
    }

    protected void setValueToDelegate(Object value) {
        if (value instanceof ResourceValue) {
            ResourceValue resVal = (ResourceValue) value;
            value = resVal.getValue();
            if (value instanceof NbImageIcon)
                delegateEditor.setValue(value);
            else
                delegateEditor.setAsText(resVal.getClassPathResourceName());
        }
        else delegateEditor.setValue(value);
    }

    protected void setValueToResourcePanel() {
        Object value = delegateEditor.getValue();
        if (value instanceof NbImageIcon || value == null) {
            NbImageIcon nbIcon = (NbImageIcon) value;
            String stringValue = nbIcon != null ? nbIcon.getName() : "${null}"; // NOI18N
            String resName = (nbIcon != null && nbIcon.getType() == org.netbeans.modules.form.editors.IconEditor.TYPE_CLASSPATH)
                    ? nbIcon.getName() : null;
            resourcePanel.setValue(nbIcon, stringValue, resName);
        }
    }

    // NamedPropertyEditor implementation
    public String getDisplayName() {
        return NbBundle.getMessage(IconEditor.class, "IconEditor_DisplayName"); // NOI18N
    }

    // XMLPropertyEditor implementation
    public void readFromXML(Node element) throws IOException {
        ((org.netbeans.modules.form.editors.IconEditor)delegateEditor).readFromXML(element);
    }

    // XMLPropertyEditor implementation
    public Node storeToXML(Document doc) {
        return ((org.netbeans.modules.form.editors.IconEditor)delegateEditor).storeToXML(doc);
    }
}
