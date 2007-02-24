/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.uml.propertysupport.nodes;

import org.openide.nodes.PropertySupport;

import java.lang.reflect.InvocationTargetException;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.awt.Component;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.ui.support.drawingproperties.FontColorDialogs.ApplicationColorsAndFonts;

public final class DefinitionColorsAndFontsProperty extends PropertySupport {
    
    private final IPropertyElement elm;
    private final PropertyEditor editor;

    public DefinitionColorsAndFontsProperty(IPropertyDefinition def, IPropertyElement elm) {
        super(def.getName(), IPropertyElement.class, def.getDisplayName(), def.getHelpDescription(), true, true);
        this.elm = elm;
        this.editor = new DefinitionColorsAndFontsEditor(def, elm);
        this.setValue("canEditAsText", false); // NOI18N
    }

    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return elm;
    }

    public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        // ignore since the custom editor does the job
    }

    public PropertyEditor getPropertyEditor() {
        return this.editor;
    }
    
    private static final class DefinitionColorsAndFontsEditor extends PropertyEditorSupport {
        private final IPropertyDefinition def;

        public DefinitionColorsAndFontsEditor(IPropertyDefinition def, IPropertyElement element) {
            super(element);
            this.def = def;
        }

        public boolean supportsCustomEditor() {
            return true;
        }

        public String getAsText() {
            return def.getDisplayName();
        }

        public Component getCustomEditor() {
            ApplicationColorsAndFonts pBasicColorsAndFontsDialog = new ApplicationColorsAndFonts();
//            pBasicColorsAndFontsDialog.center(this);
//            pBasicColorsAndFontsDialog.show();
            return pBasicColorsAndFontsDialog;
        }
    }
}
