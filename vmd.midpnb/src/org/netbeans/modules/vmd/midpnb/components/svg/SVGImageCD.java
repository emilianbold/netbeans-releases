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

package org.netbeans.modules.vmd.midpnb.components.svg;

import org.netbeans.modules.vmd.api.codegen.*;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpCodeSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.imagechooser.PropertyEditorImageChooser;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 *
 * @author Karol Harezlak
 */
public class SVGImageCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.m2g.SVGImage"); // NOI18N

    public static final String PROP_RESOURCE_PATH = "resourcePath";  // NOI18N

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ClassCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList (
            new PropertyDescriptor(PROP_RESOURCE_PATH, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), false, true, MidpVersionable.MIDP_2)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
            .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty("Resource Path", PropertyEditorImageChooser.create(), PROP_RESOURCE_PATH);
    }

    private static Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
            .addSetters (new Setter () {
                public boolean isConstructor () {
                    return true;
                }
                public TypeID getConstructorRelatedTypeID () {
                    return TYPEID;
                }
                public String getSetterName () {
                    return null;
                }
                public Versionable getVersionable () {
                    return MidpVersionable.MIDP_2;
                }
                public void generateSetterCode (MultiGuardedSection section, DesignComponent component, Map<String, Parameter> name2parameter) {
                    CodeWriter writer = section.getWriter ();
                    PropertyValue pathValue = component.readProperty (PROP_RESOURCE_PATH);
                    String path = MidpTypes.getString (pathValue);
                    if (path == null) {
                        writer.write (CodeReferencePresenter.generateDirectAccessCode (component)).write (" = SVGImage.createEmptyImage (null);\n"); // NOI18N
                    } else {
                        writer.write ("try {\n"); // NOI18N
                        writer.write (CodeReferencePresenter.generateDirectAccessCode (component)).write (" = "); // NOI18N
                        writer.write ("(SVGImage) ScalableImage.createImage (");
                        MidpCodeSupport.generateCodeForPropertyValue (writer, pathValue);
                        writer.write (", null);\n"); // NOI18N

                        writer.write ("} catch (java.io.IOException e) {\n").commit (); // NOI18N
                        section.switchToEditable (component.getComponentID () + "-@java.io.IOException"); // NOI18N
                        section.getWriter ().write ("e.printStackTrace ();\n").commit (); // NOI18N
                        section.switchToGuarded ();
                        writer.write ("}\n"); // NOI18N
                    }
                }
                public List<String> getParameters () {
                    return Collections.emptyList ();
                }
            });
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            // properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter (),
            MidpCustomCodePresenterSupport.createAddImportPresenter ()
        );
    }

}
