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
import org.netbeans.modules.vmd.api.inspector.InspectorFolderComponentPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.inspector.common.FolderPositionControllerFactory;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpCodePresenterSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpCodeSupport;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.resources.ResourcesSupport;
import org.netbeans.modules.vmd.midp.inspector.controllers.ResourcePC;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorJavaString;
import org.netbeans.modules.vmd.midpnb.propertyeditors.PropertyEditorSVGImage;
import org.netbeans.modules.vmd.midp.screen.ResourceSRItemPresenter;
import org.openide.util.NbBundle;

import java.util.*;


/**
 *
 * @author Karol Harezlak
 */
public class SVGImageCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.m2g.SVGImage"); // NOI18N
    
    public static final String PROP_RESOURCE_PATH = "resourcePath"; // NOI18N
    public static final String PROP_EXTERNAL_RESOURCE_HANDLER = "externalResourceHandler"; // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_resource_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_resource_32.png"; // NOI18N

    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }
    
    @Override
    public void postInitialize(DesignComponent component) {
        MidpProjectSupport.addLibraryToProject(component.getDocument(), SVGPlayerCD.MIDP_NB_SVG_LIBRARY);
    }
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ClassCD.TYPEID, TYPEID, true, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_RESOURCE_PATH, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), false, true, MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_EXTERNAL_RESOURCE_HANDLER, MidpTypes.TYPEID_JAVA_CODE, PropertyValue.createNull(), true, false, MidpVersionable.MIDP_2)
                );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
                .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty(NbBundle.getMessage(SVGImageCD.class, "DISP_SVGImage_ResourcePath"), PropertyEditorSVGImage.createInstance(), PROP_RESOURCE_PATH) // NOI18N
                .addProperty(NbBundle.getMessage(SVGImageCD.class, "DISP_SVGImage_ExternalResourceHandler"), PropertyEditorJavaString.createInstance(TYPEID), PROP_EXTERNAL_RESOURCE_HANDLER); // NOI18N
    }
    
    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addSetters(new Setter() {
            public boolean isConstructor() {
                return true;
            }
            public TypeID getConstructorRelatedTypeID() {
                return TYPEID;
            }
            public int getPriority() {
                return 0;
            }
            public String getSetterName() {
                return null;
            }
            public Versionable getVersionable() {
                return MidpVersionable.MIDP_2;
            }
            public void generateSetterCode(MultiGuardedSection section, DesignComponent component, Map<String, Parameter> name2parameter) {
                CodeWriter writer = section.getWriter();
                PropertyValue pathValue = component.readProperty(PROP_RESOURCE_PATH);
                String path = MidpTypes.getString(pathValue);
                if (path == null) {
                    writer.write(CodeReferencePresenter.generateDirectAccessCode(component)).write(" = SVGImage.createEmptyImage (null);\n"); // NOI18N
                } else {
                    writer.write("try {\n"); // NOI18N
                    writer.write(CodeReferencePresenter.generateDirectAccessCode(component)).write(" = "); // NOI18N
                    writer.write("(SVGImage) SVGImage.createImage (getClass ().getResourceAsStream("); // NOI18N
                    MidpCodeSupport.generateCodeForPropertyValue(writer, pathValue);
                    writer.write("), "); // NOI18N
                    MidpCodeSupport.generateCodeForPropertyValue(writer, component.readProperty (PROP_EXTERNAL_RESOURCE_HANDLER));
                    writer.write(");\n"); // NOI18N
                    
                    writer.write("} catch (java.io.IOException e) {\n").commit(); // NOI18N
                    section.switchToEditable(component.getComponentID() + "-@java.io.IOException"); // NOI18N
                    section.getWriter().write("e.printStackTrace ();\n").commit(); // NOI18N
                    section.switchToGuarded();
                    section.getWriter().write("}\n"); // NOI18N
                }
            }
            public List<String> getParameters() {
                return Collections.emptyList();
            }
        });
    }
    
    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, InfoPresenter.class);
        super.gatherPresenters(presenters);
    }
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                //info
                ResourcesSupport.createResourceInfoResolver(),
                // properties
                createPropertiesPresenter(),
                // code
                createSetterPresenter(),
                MidpCodePresenterSupport.createAddImportPresenter(),
                // inspector
                new InspectorFolderComponentPresenter(true),
                InspectorPositionPresenter.create(new ResourcePC(), FolderPositionControllerFactory.createHierarchical()),
                // screen
                new ResourceSRItemPresenter(InfoPresenter.NameType.TERTIARY)
                );
    }
    
}
