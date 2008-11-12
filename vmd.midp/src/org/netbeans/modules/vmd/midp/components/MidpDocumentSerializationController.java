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
 */package org.netbeans.modules.vmd.midp.components;

import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.io.serialization.ComponentElement;
import org.netbeans.modules.vmd.api.io.serialization.DocumentSerializationController;
import org.netbeans.modules.vmd.api.io.serialization.PropertyElement;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.categories.*;
import org.netbeans.modules.vmd.midp.components.general.RootCD;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.netbeans.modules.vmd.midp.palette.wizard.ComponentInstaller;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.modules.vmd.api.io.serialization.DocumentErrorHandler;
import org.netbeans.modules.vmd.midp.components.categories.DatabindingCategoryCD;


/**
 * @author David Kaspar
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.vmd.api.io.serialization.DocumentSerializationController.class)
public class MidpDocumentSerializationController extends DocumentSerializationController {

    public static final String VERSION_1 = "1"; // NOI18N
    private Collection<String> unresolved = new HashSet<String>();

    public void approveComponents(DataObjectContext context, DesignDocument loadingDocument, String documentVersion, final Collection<ComponentElement> componentElements, DocumentErrorHandler errorHandler) {
        final DescriptorRegistry registry = loadingDocument.getDescriptorRegistry();

        registry.readAccess(new Runnable() {

            public void run() {
                for (ComponentElement element : componentElements) {
                    String string = element.getTypeID().getString();
                    if (MidpTypes.isValidFQNClassName(string)) {
                        if (registry.getComponentDescriptor(new TypeID(TypeID.Kind.COMPONENT, string)) == null) {
                            unresolved.add(string);
                        }
                    }
                }
            }
        });
        if (!unresolved.isEmpty()) {
            Map<String, ComponentInstaller.Item> found = ComponentInstaller.search(ProjectUtils.getProject(context));
            ArrayList<ComponentInstaller.Item> install = new ArrayList<ComponentInstaller.Item>();
            for (String s : unresolved) {
                ComponentInstaller.Item item = found.get(s);
                if (item != null) {
                    install.add(item);
                }
            }
            ComponentInstaller.install(found, install);
        }
    }

    public void approveProperties(DataObjectContext context, DesignDocument loadingDocument, String documentVersion, DesignComponent component, Collection<PropertyElement> propertyElements, DocumentErrorHandler errorHandler) {
        if (!MidpDocumentSupport.PROJECT_TYPE_MIDP.equals(context.getProjectType()) || !VERSION_1.equals(documentVersion)) {
            return;
        }
        if (loadingDocument.getDescriptorRegistry().isInHierarchy(ItemCD.TYPEID, component.getType())) {
            ArrayList<PropertyElement> elementsToRemove = new ArrayList<PropertyElement>();
            ArrayList<PropertyElement> elementsToAdd = new ArrayList<PropertyElement>();
            for (PropertyElement propertyElement : propertyElements) {
                if (ItemCD.PROP_OLD_ITEM_COMMAND_LISTENER.equals(propertyElement.getPropertyName())) {
                    elementsToRemove.add(propertyElement);
                    elementsToAdd.add(PropertyElement.create(ItemCD.PROP_ITEM_COMMAND_LISTENER, propertyElement.getTypeID(), propertyElement.getSerialized()));
                    break;
                }
            }
            propertyElements.removeAll(elementsToRemove);
            propertyElements.addAll(elementsToAdd);
            for (PropertyElement pe : propertyElements) {
                pe.getSerialized();
            }
        }
    }

    public void postValidateDocument(DataObjectContext context, DesignDocument loadingDocument, String documentVersion, DocumentErrorHandler errorHandler) {
        if (!MidpDocumentSupport.PROJECT_TYPE_MIDP.equals(context.getProjectType()) || !VERSION_1.equals(documentVersion)) {
            //checkInstanceNames(DocumentSupport.gatherAllComponentsOfTypeID(loadingDocument.getRootComponent(), ClassCD.TYPEID), errorHandler);
            return;
        }
        DesignComponent rootComponent = loadingDocument.getRootComponent();
        if (rootComponent == null) {
            rootComponent = loadingDocument.createComponent(RootCD.TYPEID);
            loadingDocument.setRootComponent(rootComponent);
        }
        MidpDocumentSupport.getCategoryComponent(loadingDocument, CommandsCategoryCD.TYPEID);
        MidpDocumentSupport.getCategoryComponent(loadingDocument, ControllersCategoryCD.TYPEID);
        MidpDocumentSupport.getCategoryComponent(loadingDocument, DisplayablesCategoryCD.TYPEID);
        MidpDocumentSupport.getCategoryComponent(loadingDocument, PointsCategoryCD.TYPEID);
        MidpDocumentSupport.getCategoryComponent(loadingDocument, ResourcesCategoryCD.TYPEID);
        //checkInstanceNames(rootComponent.getComponents(), errorHandler);
    }
    
    /*
    private void checkInstanceNames(Collection<DesignComponent> components, DocumentErrorHandler errorHandler) {
        for (DesignComponent component : components) {
            PropertyDescriptor descriptor = component.getComponentDescriptor().getPropertyDescriptor(ClassCD.PROP_INSTANCE_NAME);
            if (descriptor == null) {
                return;
            }
            
            if (component.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue() == null) {
                PropertyValue value = InstanceNameResolver.createFromSuggested(component, ClassCode.getSuggestedMainName(component.getType()));
                component.writeProperty(ClassCD.PROP_INSTANCE_NAME, value);
                errorHandler.addWarning(NbBundle.getMessage(MidpDocumentSerializationController.class, "MSG_null_instance_name_1") //NOI18N
                        + " " + component + " " + NbBundle.getMessage(MidpDocumentSerializationController.class, "MSG_null_instance_name_2") //NOI18N 
                        + " " + InfoPresenter.getDisplayName(component)); //NOI18N
            }
        }
    }
     */
}