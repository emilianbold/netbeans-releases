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
package org.netbeans.modules.visualweb.propertyeditors;

import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;
import java.awt.Component;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * An editor for properties that take CSS style classes. The editor will accept
 * one or more of the style classes defined by all CSS style sheets in the
 * current project. Inline editing of style classes is allowed, however, the
 * style class names entered must correspond to style classes defined by style
 * sheets in project scope.
 *
 * @author gjmurphy
 */
public class StyleClassPropertyEditor extends PropertyEditorBase implements
        com.sun.rave.propertyeditors.StyleClassPropertyEditor {

    List styleClassList;

    String[] getStyleClasses() {
        if (this.styleClassList == null)
            return new String[0];
        return (String[]) this.styleClassList.toArray(new String[this.styleClassList.size()]);
    }

    void setStyleClasses(String[] styleClasses) {
        this.styleClassList = Arrays.asList(styleClasses);
    }

    public Object getValue() {
        if (styleClassList == null || styleClassList.size() == 0)
            return null;
        return getAsText();
    }

    public void setValue(Object value) {
        String text = (String) value;
        if (text == null || text.trim().length() == 0) {
            this.styleClassList = null;
        } else {
            this.styleClassList = Arrays.asList(text.trim().split("\\s"));
        }
    }

    public String getAsText() {
        if (styleClassList == null || styleClassList.size() == 0)
            return "";
        StringBuffer buffer = new StringBuffer();
        buffer.append(styleClassList.get(0));
        for (int i = 1; i < styleClassList.size(); i++) {
            buffer.append(" ");
            buffer.append(styleClassList.get(i));
        }
        return buffer.toString();
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.trim().length() == 0) {
            this.styleClassList = null;
        } else {
            String[] styleClasses = text.trim().split("\\s");
            String[] styleClassesInContext = getAvailableStyleClasses();
            this.styleClassList = new ArrayList();
            List notFoundStyleClassList = new ArrayList();
            for (int i = 0; i < styleClasses.length; i++) {
                int index = Arrays.binarySearch(styleClassesInContext, styleClasses[i]);
                if (index >= 0)
                    this.styleClassList.add(styleClassesInContext[index]);
                else
                    notFoundStyleClassList.add(styleClasses[i]);
            }
            if (notFoundStyleClassList.size() > 0) {
                throw new IllegalTextArgumentException(
                        bundle.getMessage("StyleClassPropertyEditor.classNotFound", notFoundStyleClassList.toString()));
            }
        }
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    public Component getCustomEditor() {
        return new StyleClassPropertyPanel(this);
    }

    String[] getAvailableStyleClasses() {
        DesignProperty designProperty = this.getDesignProperty();
        if (designProperty == null)
            return new String[0];
        DesignContext designContext = designProperty.getDesignBean().getDesignContext();
        // According to the API documentation, this should return an array of
        // StyleClassDescriptor objects, but this does not appear to have been
        // implemented yet.
        Object[] styleClasses = (Object[]) designContext.getContextData(
                Constants.ContextData.CSS_STYLE_CLASS_DESCRIPTORS);
        String[] styleClassNames = new String[styleClasses.length];
        for (int i = 0; i < styleClasses.length; i++)
            styleClassNames[i] = styleClasses[i].toString();
        Collator collator = Collator.getInstance(Locale.US);
        collator.setStrength(Collator.PRIMARY);
        Arrays.sort(styleClassNames, collator);
        return styleClassNames;
    }
}
