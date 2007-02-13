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
package org.netbeans.modules.vmd.midp.components.general;

import org.netbeans.modules.vmd.api.codegen.CodeNamePresenter;
import org.netbeans.modules.vmd.api.flow.FlowScenePresenter;
import org.netbeans.modules.vmd.api.flow.visual.FlowDescriptor;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.inspector.common.DefaultOrderingController;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.AcceptSupport;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.codegen.InstanceNameResolver;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.categories.*;
import org.netbeans.modules.vmd.midp.inspector.controllers.RootPC;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorVersion;

import java.awt.datatransfer.Transferable;
import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */
public final class RootCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#Root");  // NOI18N 

    private static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/mobile_device_16.png";  // NOI18N 

    public static final String  PROP_VERSION = "version";  // NOI18N

    public static final String VALUE_MIDP_2_0 = "MIDP-2.0"; // NOI18N
    public static final String VALUE_MIDP_1_0 = "MIDP-1.0"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (null, TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.FOREVER;
    }

    public void postInitialize (DesignComponent component) {
        component.writeProperty (PROP_VERSION, MidpTypes.createStringValue (VALUE_MIDP_2_0));
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList (
            new PropertyDescriptor (PROP_VERSION, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), false, false, Versionable.FOREVER)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
                .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES)
                    .addProperty("Version", "MIDP Version",false, PropertyEditorVersion.createInstance(), null, PROP_VERSION);
    }

    private InspectorOrderingController[] creatOrderingControllers() {

       return new InspectorOrderingController[]{ new DefaultOrderingController(10, DisplayablesCategoryCD.TYPEID),
                                              new DefaultOrderingController(20, PointsCategoryCD.TYPEID),
                                              new DefaultOrderingController(30, CommandsCategoryCD.TYPEID),
                                              new DefaultOrderingController(40, ControllersCategoryCD.TYPEID),
                                              new DefaultOrderingController(50, ResourcesCategoryCD.TYPEID)
                                             };
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // general
            InfoPresenter.createStatic("MIDP Visual Design", null, ICON_PATH),
            // properties
            createPropertiesPresenter(),
            // validator
            InstanceNameResolver.createValidatorPresenter (),
            // inspector
            InspectorFolderPresenter.create(false),
            InspectorPositionPresenter.create(new RootPC()),
            InspectorOrderingPresenter.create(creatOrderingControllers()),
            // flow
            FlowScenePresenter.create (new RootAcceptBehavior ()),
            // code
            RootCode.createInitializePresenter (),
            CodeNamePresenter.fixed ("initialize", "exitMIDlet"),
            CodeNamePresenter.fixed (
                    "abstract", "assert", "boolean", "break", "break", "byte", "case", "catch", "char", "class", "const",
                    "continue", "default", "do", "double", "else" , "enum", "extends", "final", "finally", "float",
                    "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
                    "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
                    "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"
            ),
            // delete
            DeletePresenter.createIndeliblePresenter ()
        );
    }

    private static class RootAcceptBehavior implements FlowDescriptor.AcceptActionBehavior {

        public boolean isAcceptable (FlowDescriptor descriptor, Transferable transferable) {
            DesignComponent categoryComponent = MidpDocumentSupport.getCategoryComponent (descriptor.getRepresentedComponent ().getDocument (), DisplayablesCategoryCD.TYPEID);
            if (AcceptSupport.isAcceptable (categoryComponent, transferable))
                return true;
            categoryComponent = MidpDocumentSupport.getCategoryComponent (descriptor.getRepresentedComponent ().getDocument (), PointsCategoryCD.TYPEID);
            return AcceptSupport.isAcceptable (categoryComponent, transferable);
        }

        public void accept (FlowDescriptor descriptor, Transferable transferable) {
            DesignComponent categoryComponent = MidpDocumentSupport.getCategoryComponent (descriptor.getRepresentedComponent ().getDocument (), DisplayablesCategoryCD.TYPEID);
            if (AcceptSupport.isAcceptable (categoryComponent, transferable)) {
                ComponentProducer.Result result = AcceptSupport.accept (categoryComponent, transferable);
                MidpDocumentSupport.selectComponentProducerResult (result);
                return;
            }
            categoryComponent = MidpDocumentSupport.getCategoryComponent (descriptor.getRepresentedComponent ().getDocument (), PointsCategoryCD.TYPEID);
            if (AcceptSupport.isAcceptable (categoryComponent, transferable)) {
                ComponentProducer.Result result = AcceptSupport.accept (categoryComponent, transferable);
                MidpDocumentSupport.selectComponentProducerResult (result);
            }
        }

    }

}
