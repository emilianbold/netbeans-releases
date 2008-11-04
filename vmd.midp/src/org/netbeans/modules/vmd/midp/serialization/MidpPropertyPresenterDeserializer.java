/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.midp.serialization;

import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PresenterDeserializer;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.propertyeditors.*;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * @author David Kaspar
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.vmd.api.model.PresenterDeserializer.class)
public class MidpPropertyPresenterDeserializer extends PresenterDeserializer {

    public static final String PROPERTY_NODE = "MidpProperty"; // NOI18N
    public static final String DISPLAY_NAME_ATTR = "displayName"; // NOI18N
    public static final String EDITOR_ATTR = "editor"; // NOI18N
    public static final String PROPERTY_NAME_ATTR = "propertyName"; // NOI18N

    public static final String EDITOR_BOOLEAN = "boolean"; // NOI18N
    public static final String EDITOR_CHAR = "char"; // NOI18N
    public static final String EDITOR_BYTE = "byte"; // NOI18N
    public static final String EDITOR_SHORT = "short"; // NOI18N
    public static final String EDITOR_INT = "int"; // NOI18N
    public static final String EDITOR_LONG = "long"; // NOI18N
    public static final String EDITOR_FLOAT = "float"; // NOI18N
    public static final String EDITOR_DOUBLE = "double"; // NOI18N
    public static final String EDITOR_STRING = "java.lang.String"; // NOI18N
// TODO -    public static final String EDITOR_IMAGE = "image"; // NOI18N
    public static final String EDITOR_JAVA_CODE = "#javaCode"; // NOI18N

    public MidpPropertyPresenterDeserializer () {
        super (MidpDocumentSupport.PROJECT_TYPE_MIDP);
    }

    public PresenterFactory deserialize (Node node) {
        if (! PROPERTY_NODE.equalsIgnoreCase (node.getNodeName ()))
            return null;
        String displayName = XMLUtils.getAttributeValue (node, DISPLAY_NAME_ATTR);
        String editor = XMLUtils.getAttributeValue (node, EDITOR_ATTR);
        String propertyName = XMLUtils.getAttributeValue (node, PROPERTY_NAME_ATTR);
        return new MidpPropertyPresenterFactory (displayName, editor, propertyName);
    }

    private static class MidpPropertyPresenterFactory extends PresenterFactory {

        private String displayName;
        private String editor;
        private String propertyName;

        public MidpPropertyPresenterFactory (String displayName, String editor, String propertyName) {
            this.displayName = displayName;
            this.editor = editor;
            this.propertyName = propertyName;
        }

        public List<Presenter> createPresenters (ComponentDescriptor descriptor) {
            DefaultPropertiesPresenter presenter = new DefaultPropertiesPresenter ()
                .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_PROPERTIES);
            if (EDITOR_BOOLEAN.equals (editor))
                presenter.addProperty (displayName, PropertyEditorBooleanUC.createInstance (), propertyName);
            else if (EDITOR_INT.equals (editor))
                presenter.addProperty (displayName, PropertyEditorNumber.createIntegerInstance(false, NbBundle.getMessage(MidpPropertyPresenterDeserializer.class, "LBL_INTEGER_STR")), propertyName); // NOI18N
            else if (EDITOR_FLOAT.equals (editor))
                presenter.addProperty (displayName, PropertyEditorNumber.createFloatInstance(NbBundle.getMessage(MidpPropertyPresenterDeserializer.class, "LBL_FLOAT_STR")), propertyName); // NOI18N
            else if (EDITOR_STRING.equals (editor))
                presenter.addProperty (displayName, PropertyEditorString.createInstance(NbBundle.getMessage(MidpPropertyPresenterDeserializer.class, "LBL_STRING")), propertyName); // NOI18N
            else if (EDITOR_JAVA_CODE.equals (editor))
                presenter.addProperty (displayName, PropertyEditorJavaString.createInstance (descriptor.getTypeDescriptor ().getThisType ()), propertyName);
            else if (EDITOR_CHAR.equals (editor))
                presenter.addProperty (displayName, PropertyEditorNumber.createCharInstance (false, NbBundle.getMessage(MidpPropertyPresenterDeserializer.class, "LBL_CHAR_STR")), propertyName); // NOI18N
            else if (EDITOR_BYTE.equals (editor))
                presenter.addProperty (displayName, PropertyEditorNumber.createByteInstance(false, NbBundle.getMessage(MidpPropertyPresenterDeserializer.class, "LBL_BYTE_STR")), propertyName); // NOI18N
            else if (EDITOR_SHORT.equals (editor))
                presenter.addProperty (displayName, PropertyEditorNumber.createShortInstance(false, NbBundle.getMessage(MidpPropertyPresenterDeserializer.class, "LBL_SHORT_STR")), propertyName); // NOI18N
            else if (EDITOR_LONG.equals (editor))
                presenter.addProperty (displayName, PropertyEditorNumber.createLongInstance(false, NbBundle.getMessage(MidpPropertyPresenterDeserializer.class, "LBL_LONG_STR")), propertyName); // NOI18N
            else if (EDITOR_DOUBLE.equals (editor))
                presenter.addProperty (displayName, PropertyEditorNumber.createDoubleInstance(NbBundle.getMessage(MidpPropertyPresenterDeserializer.class, "LBL_DOUBLE_STR")), propertyName); // NOI18N
            else
                return null;
            return Arrays.<Presenter>asList (presenter);
        }

    }

}
