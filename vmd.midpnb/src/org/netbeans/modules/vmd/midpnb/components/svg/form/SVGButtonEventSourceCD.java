/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midpnb.components.svg.form;

import com.sun.source.util.TreePath;
import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.vmd.api.codegen.CodeGlobalLevelPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPath;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPresenter;
import org.netbeans.modules.vmd.api.inspector.common.DesignComponentInspectorFolder;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PresenterEvent;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.model.Versionable;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter.IconType;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter.NameType;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.midp.actions.GoToSourcePresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.general.ClassSupport;
import org.netbeans.modules.vmd.midp.components.handlers.EventHandlerSupport;
import org.netbeans.modules.vmd.midp.components.sources.EventSourceCD;
import org.netbeans.modules.vmd.midp.flow.FlowEventSourcePinPresenter;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGFormCD.SVGButtonEventSourceOrder;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Karol Harezlak
 */
public class SVGButtonEventSourceCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "#SVGButtonEventEventSource"); // NOI18
    public static final String PROP_SVGBUTTON = "svgButton"; //NOI18N
    
    private static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/button_16.png"; // NOI18N                                                
    private static final Image ICON_SVG_BUTTON = ImageUtilities.loadImage(ICON_PATH);

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(EventSourceCD.TYPEID, TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_SVGBUTTON, SVGButtonCD.TYPEID, PropertyValue.createNull(), false, false, Versionable.FOREVER));
    }

    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, ActionsPresenter.class);
        DocumentSupport.removePresentersOfClass(presenters, InspectorFolderPresenter.class);
        MidpActionsSupport.addCommonActionsPresenters(presenters, false, true, true, false, true);

        super.gatherPresenters(presenters);
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // info
                InfoPresenter.create(new SVGButtonEventSourceResolver()),
                //code
                new ImportCodePresenterSupport("org.netbeans.microedition.svg.SVGActionListener"), //NOI18N
                new ImportCodePresenterSupport("org.netbeans.microedition.svg.SVGComponent"), //NOI18N
                GoToSourcePresenter.createForwarder(PROP_SVGBUTTON),
                // flow
                new SVGButtonEventSourcePinPresenter(),
                // delete
                DeleteDependencyPresenter.createDependentOnParentComponentPresenter(),
                //inspector
                new SVGButtonInspectorFolderComponentPresneter()
                );


    }

    private class SVGButtonEventSourceResolver implements InfoPresenter.Resolver {

        public DesignEventFilter getEventFilter(DesignComponent component) {
            return new DesignEventFilter().addComponentFilter(component, false);
        }

        public String getDisplayName(DesignComponent component, NameType nameType) {
            switch (nameType) {
                case PRIMARY:
                    DesignComponent displayable = component.readProperty (PROP_SVGBUTTON).getComponent ();
                    if (displayable == null)
                        return NbBundle.getMessage(EventHandlerSupport.class, "DISP_Handler_Clear_Display"); // NOI18N

                    String displayableName = ClassSupport.resolveDisplayName (displayable);
                    return NbBundle.getMessage(EventHandlerSupport.class, "DISP_Handler_Go_to_displayable", displayableName); // NOI18N
                case SECONDARY:
                    return NbBundle.getMessage(EventHandlerSupport.class, "TYPE_Action"); // NOI18N
                case TERTIARY:
                    return null;
                default:
                    throw Debug.illegalState ();
            }
        }

        public boolean isEditable(DesignComponent component) {
            return true;
        }

        public String getEditableName(DesignComponent component) {
            return getButtonName(component);
        }

        public void setEditableName(DesignComponent component, String enteredName) {
            DesignComponent button = component.readProperty(SVGButtonEventSourceCD.PROP_SVGBUTTON).getComponent();
            button.writeProperty(ClassCD.PROP_INSTANCE_NAME, MidpTypes.createStringValue(enteredName));
        }

        public Image getIcon(DesignComponent component, IconType iconType) {
            return ICON_SVG_BUTTON;
        }
    }

    private class SVGButtonEventSourcePinPresenter extends FlowEventSourcePinPresenter {

        protected DesignComponent getComponentForAttachingPin() {
            return getComponent().getParentComponent();
        }

        protected String getDisplayName() {
            return getButtonName(getComponent());
        }

        protected String getOrder() {
            return SVGButtonEventSourceOrder.CATEGORY_ID;
        }

        @Override
        protected boolean canRename() {
            return true;
        }

        @Override
        protected String getRenameName() {
            return getButtonName(getComponent());
        }

        @Override
        protected void setRenameName(String name) {
            DesignComponent button = getComponent().readProperty(SVGButtonEventSourceCD.PROP_SVGBUTTON).getComponent();
            button.writeProperty(ClassCD.PROP_INSTANCE_NAME, MidpTypes.createStringValue(name));
        }



        @Override
        protected DesignEventFilter getEventFilter() {
            return new DesignEventFilter().setGlobal(true);
        }
    };

    private static String getButtonName(DesignComponent component) {
        DesignComponent svgButton = component.readProperty(PROP_SVGBUTTON).getComponent();
        if (svgButton == null) {
            throw new IllegalStateException("Design Component svg button is null"); //NOI18N
        }
        return (String) svgButton.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue();
    }

    private class ImportCodePresenterSupport extends CodeGlobalLevelPresenter {

        final private List<String> fullyNamesList;

        private ImportCodePresenterSupport(String... fullyNames) {
            this.fullyNamesList = new ArrayList(Arrays.asList(fullyNames));
        }

        @Override
        protected void performGlobalGeneration(StyledDocument styledDocument) {
            addImports(styledDocument);
        }

        private void addImports(final StyledDocument styledDocument) {
            if (getComponent().getComponents() == null || getComponent().getComponents().isEmpty()) {
                return;
            }
            try {
                JavaSource.forDocument(styledDocument).runModificationTask(new CancellableTask<WorkingCopy>() {

                    public void cancel() {
                    }

                    public void run(WorkingCopy parameter) throws Exception {
                        parameter.toPhase(JavaSource.Phase.PARSED);
                        for (String fqn : fullyNamesList) {
                            SourceUtils.resolveImport(parameter, new TreePath(parameter.getCompilationUnit()), fqn);
                        }
                    }
                }).commit();
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    private class SVGButtonInspectorFolder extends DesignComponentInspectorFolder {

        public SVGButtonInspectorFolder(boolean canRename, DesignComponent component) {
            super(canRename, component);
        }

        @Override
        public boolean isInside(InspectorFolderPath path, InspectorFolder folder, DesignComponent component) {
            if (path.getLastElement().getTypeID() == MidpInspectorSVGButtonSupport.TYPEID_CATEGORY_SVG_BUTTONS) {
                if (path.getLastElement().getComponentID().equals(component.getParentComponent().getComponentID())) {
                    return true;
                }
            }
            return false;
        }
    }

    private class SVGButtonInspectorFolderComponentPresneter extends InspectorFolderPresenter {

       
        @Override
        public InspectorFolder getFolder() {
           return new SVGButtonInspectorFolder(true, getComponent());
        }

        @Override
        protected void notifyAttached(DesignComponent component) {
            
        }

        @Override
        protected void notifyDetached(DesignComponent component) {
            
        }

        @Override
        protected DesignEventFilter getEventFilter() {
            return null;
        }

        @Override
        protected void designChanged(DesignEvent event) {
            
        }

        @Override
        protected void presenterChanged(PresenterEvent event) {
            
        }

       
        
        
        
    }
}
