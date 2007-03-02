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
package org.netbeans.modules.visualweb.propertyeditors.binding;

import com.sun.rave.designtime.markup.AttributePropertyEditor;

/**
 *
 *
 * @author Sandip Chitale
 */
public class ValueBindingAttributePropertyEditor extends
		ValueBindingPropertyEditor implements AttributePropertyEditor {

    private AttributePropertyEditor delegateAttributePropertyEditor;

	/**
	 *
	 */
	public ValueBindingAttributePropertyEditor() {
		super();
        delegateAttributePropertyEditor = null;
	}

    /**
     *
     */
    public ValueBindingAttributePropertyEditor(AttributePropertyEditor attributePropertyEditor) {
        super(attributePropertyEditor);
        delegateAttributePropertyEditor = attributePropertyEditor;
    }

    /* (non-Javadoc)
     * @see com.sun.rave.designtime.markup.AttributePropertyEditor#getMarkupInitializationString()
     */
    public String getMarkupInitializationString() {
        if (delegateAttributePropertyEditor == null) {
            return getJavaInitializationString();
        }
        if (isUseDelegatePropertyEditor()) {
            return ((AttributePropertyEditor) delegatePropertyEditor).getMarkupInitializationString();
        }

        if (isPropertyBound()) {
            delegateAttributePropertyEditor.setValue(getValueBindingValue());
        } else {
            delegateAttributePropertyEditor.setValue(getValueInternal());
        }
        return delegateAttributePropertyEditor.getMarkupInitializationString();
    }

    /* (non-Javadoc)
     * @see com.sun.rave.designtime.markup.AttributePropertyEditor#resolveMarkupInitializationString(java.lang.String)
     */
    public Object resolveMarkupInitializationString(String initString) {
        if (delegateAttributePropertyEditor == null) {
            // TODO Verify
            setAsText(initString);
            return getValue();
        }

        if (isUseDelegatePropertyEditor()) {
            return delegateAttributePropertyEditor.resolveMarkupInitializationString(initString);
        }

        if (isPropertyBound()) {
            delegateAttributePropertyEditor.setValue(getValueBindingValue());
        } else {
            delegateAttributePropertyEditor.setValue(getValueInternal());
        }
        return delegateAttributePropertyEditor.resolveMarkupInitializationString(initString);
    }
}
