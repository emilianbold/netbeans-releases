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
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.vmd.api.codegen.CodeGlobalLevelPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderComponentPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorUIManagerSupport;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.DynamicPresenter;
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
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.sources.EventSourceCD;
import org.netbeans.modules.vmd.midp.flow.FlowEventSourcePinPresenter;
import org.netbeans.modules.vmd.midp.flow.FlowListElementPinOrderPresenter;
import org.openide.util.Exceptions;

/**
 *
 * @author karolharezlak
 */
public class SVGButtonEventSourceCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "#SVGButtonEventEventSource"); // NOI18
    public static final String PROP_SVGBUTTON = "svgButton"; //NOI18N

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
        MidpActionsSupport.addCommonActionsPresenters(presenters, false, true, false, false, true);
        
        super.gatherPresenters(presenters);
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // info
                InfoPresenter.create(new SVGButtonEventSourceResolver()),
                // inspector
                //InspectorPositionPresenter.create(new ComponentsCategoryPC(MidpInspectorSupport.TYPEID_ELEMENTS)), new ScreenMoveArrayAcceptPresenter(ListCD.PROP_ELEMENTS, ListElementEventSourceCD.TYPEID),
                //code
                new ImportCodePresenterSupport("org.netbeans.microedition.svg.SVGActionListener", "org.netbeans.microedition.svg.SVGComponent;"), //NOI18N
                GoToSourcePresenter.createForwarder(PROP_SVGBUTTON),
                // flow
                new SVGButtonEventSourcePinPresenter(),
                // delete
                DeleteDependencyPresenter.createDependentOnParentComponentPresenter(),
                //inspector
                new InspectorSelectionPresneter()
                );
                
                
    }

    private class SVGButtonEventSourceResolver implements InfoPresenter.Resolver {

        public DesignEventFilter getEventFilter(DesignComponent component) {
            return null;
        }

        public String getDisplayName(DesignComponent component, NameType nameType) {
            return getButtonName(component);
        }

        public boolean isEditable(DesignComponent component) {
            return false;
        }

        public String getEditableName(DesignComponent component) {
            return null;
        }

        public void setEditableName(DesignComponent component, String enteredName) {
        }

        public Image getIcon(DesignComponent component, IconType iconType) {
            return null;
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
            return FlowListElementPinOrderPresenter.CATEGORY_ID;
        }

        @Override
        protected boolean canRename() {
            return false;
        }

        @Override
        protected String getRenameName() {
            return getButtonName(getComponent());
        }

        @Override
        protected DesignEventFilter getEventFilter() {
            return super.getEventFilter().addParentFilter(getComponent(), 1, false);
        }
    };

    private static String getButtonName(DesignComponent component) {
        DesignComponent svgButton = component.readProperty(PROP_SVGBUTTON).getComponent();
        if (svgButton == null) {
            return "ERROR";
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
    
    private class InspectorSelectionPresneter extends DynamicPresenter {

        @Override
        protected void notifyAttached(DesignComponent component) {
            
        }

        @Override
        protected void notifyDetached(DesignComponent component) {
           
        }

        @Override
        protected DesignEventFilter getEventFilter() {
            return new DesignEventFilter().setSelection(true);
        }

        @Override
        protected void designChanged(DesignEvent event) {
            if (getComponent().getDocument().getSelectedComponents().contains(getComponent())) {
                String nameToSelect = InfoPresenter.getDisplayName(getComponent().getParentComponent());
                InspectorUIManagerSupport.selectNodesInInspector(getComponent().getDocument(), nameToSelect);
            }
        }

        @Override
        protected void presenterChanged(PresenterEvent event) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
}
