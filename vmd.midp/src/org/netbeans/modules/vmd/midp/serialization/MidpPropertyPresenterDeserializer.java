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
 *
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
                presenter.addProperty (displayName, PropertyEditorNumber.createIntegerInstance(), propertyName);
            else if (EDITOR_FLOAT.equals (editor))
                presenter.addProperty (displayName, PropertyEditorNumber.createFloatInstance(), propertyName);
            else if (EDITOR_STRING.equals (editor))
                presenter.addProperty (displayName, PropertyEditorString.createInstance(NbBundle.getMessage(MidpPropertyPresenterDeserializer.class, "TEXT_UCLABEL")), propertyName); // NOI18N
            else if (EDITOR_JAVA_CODE.equals (editor))
                presenter.addProperty (displayName, PropertyEditorJavaString.createInstance (descriptor.getTypeDescriptor ().getThisType ()), propertyName);
            else if (EDITOR_CHAR.equals (editor))
                presenter.addProperty (displayName, PropertyEditorNumber.createCharInstance (), propertyName);
            else if (EDITOR_BYTE.equals (editor))
                presenter.addProperty (displayName, PropertyEditorNumber.createByteInstance(), propertyName);
            else if (EDITOR_SHORT.equals (editor))
                presenter.addProperty (displayName, PropertyEditorNumber.createShortInstance(), propertyName);
            else if (EDITOR_LONG.equals (editor))
                presenter.addProperty (displayName, PropertyEditorNumber.createLongInstance(), propertyName);
            else if (EDITOR_DOUBLE.equals (editor))
                presenter.addProperty (displayName, PropertyEditorNumber.createDoubleInstance(), propertyName);
            else
                return null;
            return Arrays.<Presenter>asList (presenter);
        }

    }

}
