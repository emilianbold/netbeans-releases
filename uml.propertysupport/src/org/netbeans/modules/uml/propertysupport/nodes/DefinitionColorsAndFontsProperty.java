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
