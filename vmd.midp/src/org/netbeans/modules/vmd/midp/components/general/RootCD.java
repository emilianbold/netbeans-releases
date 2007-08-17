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

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.modules.vmd.api.codegen.CodeNamePresenter;
import org.netbeans.modules.vmd.api.flow.FlowScenePresenter;
import org.netbeans.modules.vmd.api.flow.visual.FlowDescriptor;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderComponentPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.inspector.common.DefaultOrderingController;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.AcceptPresenter;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.api.model.common.AcceptSupport;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.api.palette.PaletteSupport;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfoPresenter;
import org.netbeans.modules.vmd.midp.actions.ExportFlowAsImageAction;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.codegen.InstanceNameResolver;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.categories.*;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.points.PointCD;
import org.netbeans.modules.vmd.midp.inspector.controllers.RootPC;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorVersion;
import org.netbeans.modules.vmd.midp.screen.MidpScreenDeviceInfo;
import org.netbeans.spi.palette.PaletteController;
import org.openide.actions.RedoAction;
import org.openide.actions.UndoAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author David Kaspar
 */
public final class RootCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#Root");  // NOI18N 

    private static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/mobile_device_16.png";  // NOI18N 

    public static final String  PROP_VERSION = "version";  // NOI18N

    public static final String VALUE_MIDP_PREFIX = "MIDP"; // NOI18N
    public static final String VALUE_MIDP_2_0 = "MIDP-2.0"; // NOI18N
    public static final String VALUE_MIDP_1_0 = "MIDP-1.0"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (null, TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.FOREVER;
    }

    @Override
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
                .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                    .addProperty(NbBundle.getMessage(RootCD.class, "DISP_Root_Version"), NbBundle.getMessage(RootCD.class, "TTIP_Root_Version"), PropertyEditorVersion.createInstance(), PROP_VERSION); // NOI18N
    }

    private InspectorOrderingController[] creatOrderingControllers() {

       return new InspectorOrderingController[]{ new DefaultOrderingController(10, DisplayablesCategoryCD.TYPEID),
                                              new DefaultOrderingController(20, PointsCategoryCD.TYPEID),
                                              new DefaultOrderingController(30, CommandsCategoryCD.TYPEID),
                                              new DefaultOrderingController(40, ControllersCategoryCD.TYPEID),
                                              new DefaultOrderingController(50, ResourcesCategoryCD.TYPEID)
                                             };
    }


    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        MidpActionsSupport.addCommonActionsPresenters (presenters, false, false, false, false, true);
        MidpActionsSupport.addNewActionPresenter(presenters, DisplayableCD.TYPEID, PointCD.TYPEID);
        presenters.add(ActionsPresenter.create(53, SystemAction.get(ExportFlowAsImageAction.class)));
        presenters.add(ActionsPresenter.create(57, SystemAction.get(UndoAction.class), SystemAction.get(RedoAction.class)));
        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // general
            InfoPresenter.createStatic(NbBundle.getMessage(RootCD.class, "NAME_Root"), null, ICON_PATH), // NOI18N
            // properties
            createPropertiesPresenter(),
            // validator
            InstanceNameResolver.createValidatorPresenter (),
            // inspector
            new InspectorFolderComponentPresenter(false),
            InspectorPositionPresenter.create(new RootPC()),
            InspectorOrderingPresenter.create(creatOrderingControllers()),
            // flow
            FlowScenePresenter.create (new RootActionBehavior ()),
            // code
            RootCode.createInitializePresenter (),
            CodeNamePresenter.fixed ("initialize", "exitMIDlet"), // NOI18N
            CodeNamePresenter.fixed (
                    "abstract", "assert", "boolean", "break", "break", "byte", "case", "catch", "char", "class", "const", // NOI18N
                    "continue", "default", "do", "double", "else" , "enum", "extends", "final", "finally", "float", // NOI18N
                    "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", // NOI18N
                    "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", // NOI18N
                    "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while" // NOI18N
            ),
            // delete
            DeletePresenter.createIndeliblePresenter (),
            // screen
            ScreenDeviceInfoPresenter.create(new MidpScreenDeviceInfo()),
            // accept
            new AcceptPresenter(AcceptPresenter.Kind.COMPONENT_PRODUCER) {
                public boolean isAcceptable (ComponentProducer producer, AcceptSuggestion suggestion) {
                    DesignComponent categoryComponent = MidpDocumentSupport.getCategoryComponent (getComponent ().getDocument (), DisplayablesCategoryCD.TYPEID);
                    if (AcceptSupport.isAcceptable (categoryComponent, producer, null))
                        return true;
                    categoryComponent = MidpDocumentSupport.getCategoryComponent (getComponent ().getDocument (), PointsCategoryCD.TYPEID);
                    return AcceptSupport.isAcceptable (categoryComponent, producer, null);
               }

                public ComponentProducer.Result accept (ComponentProducer producer, AcceptSuggestion suggestion) {
                    DesignComponent categoryComponent = MidpDocumentSupport.getCategoryComponent (getComponent ().getDocument (), DisplayablesCategoryCD.TYPEID);
                    if (AcceptSupport.isAcceptable (categoryComponent, producer, null)) {
                        ComponentProducer.Result result = AcceptSupport.accept (categoryComponent, producer, null);
                        AcceptSupport.selectComponentProducerResult (result);
                        return result;
                    }

                    categoryComponent = MidpDocumentSupport.getCategoryComponent (getComponent ().getDocument (), PointsCategoryCD.TYPEID);
                    if (AcceptSupport.isAcceptable (categoryComponent, producer, null)) {
                        ComponentProducer.Result result = AcceptSupport.accept (categoryComponent, producer, null);
                        AcceptSupport.selectComponentProducerResult (result);
                        return result;
                    }

                    return null;
                }
            }
        );
    }

    private static class RootActionBehavior implements FlowDescriptor.AcceptActionBehaviour, FlowDescriptor.SelectActionBehaviour, FlowDescriptor.KeyActionBehaviour {

        public boolean isAcceptable (FlowDescriptor descriptor, Transferable transferable) {
            DesignComponent categoryComponent = MidpDocumentSupport.getCategoryComponent (descriptor.getRepresentedComponent ().getDocument (), DisplayablesCategoryCD.TYPEID);
            if (AcceptSupport.isAcceptable (categoryComponent, transferable, null))
                return true;
            categoryComponent = MidpDocumentSupport.getCategoryComponent (descriptor.getRepresentedComponent ().getDocument (), PointsCategoryCD.TYPEID);
            return AcceptSupport.isAcceptable (categoryComponent, transferable, null);
        }

        public void accept (FlowDescriptor descriptor, Transferable transferable) {
            DesignComponent categoryComponent = MidpDocumentSupport.getCategoryComponent (descriptor.getRepresentedComponent ().getDocument (), DisplayablesCategoryCD.TYPEID);
            if (AcceptSupport.isAcceptable (categoryComponent, transferable, null)) {
                ComponentProducer.Result result = AcceptSupport.accept (categoryComponent, transferable, null);
                AcceptSupport.selectComponentProducerResult (result);
                return;
            }
            categoryComponent = MidpDocumentSupport.getCategoryComponent (descriptor.getRepresentedComponent ().getDocument (), PointsCategoryCD.TYPEID);
            if (AcceptSupport.isAcceptable (categoryComponent, transferable, null)) {
                ComponentProducer.Result result = AcceptSupport.accept (categoryComponent, transferable, null);
                AcceptSupport.selectComponentProducerResult (result);
            }
        }

        public boolean select (FlowDescriptor descriptor, int modifiers) {
            DesignDocument document = descriptor.getRepresentedComponent ().getDocument ();
            PaletteController controller = PaletteSupport.getPaletteController (document);
            Lookup category = controller.getSelectedCategory ();
            Lookup item = controller.getSelectedItem ();
            boolean ret = false;

            if (item != null) {
                Transferable transferable = PaletteSupport.createTransferable (document, item);
                if (isAcceptable (descriptor, transferable)) {
                    accept (descriptor, transferable);
                    ret = true;
                }
            }

            if ((modifiers & InputEvent.SHIFT_MASK) != InputEvent.SHIFT_MASK)
                controller.clearSelection ();
            return ret;
        }

        public boolean keyPressed (WidgetAction.WidgetKeyEvent e) {
            if (e.getKeyCode () == KeyEvent.VK_DELETE) {
                SystemAction.findObject (org.netbeans.modules.vmd.api.model.presenters.actions.DeleteAction.class).actionPerformed (null);
                return true;
            }
            return false;
        }
    }

}
