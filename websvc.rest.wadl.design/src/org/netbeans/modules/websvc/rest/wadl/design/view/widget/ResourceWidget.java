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

package org.netbeans.modules.websvc.rest.wadl.design.view.widget;

import java.awt.Font;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.netbeans.modules.websvc.rest.wadl.design.view.DesignViewPopupProvider;
import org.netbeans.modules.websvc.rest.wadl.design.view.actions.*;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Ayub Khan
 */
public class ResourceWidget extends WadlComponentWidget implements PropertyChangeListener {
    
    private static final String IMAGE_RESOURCE  = 
            "org/netbeans/modules/websvc/rest/wadl/design/view/resources/resource.png"; // NOI18N   

    private ImageLabelWidget headerLabelWidget;
    private Widget buttons;
    private AddMethodAction addMethod;
    private AddResourceAction addResource;
    private Widget containerWidget;
    ParametersWidget templateParamsWidget;
    ParametersWidget queryParamsWidget;
    private ResourceType resourceRef;

    
    /**
     * Creates a new instance of MethodWidget
     * @param scene
     * @param operation
     */
    public ResourceWidget(ObjectScene scene, Widget containerWidget, Resource resource, WadlModel model) throws IOException {
        super(scene, resource, model);
        this.containerWidget = containerWidget;

        String type = resource.getType();
        if(type != null) {
            if(type.indexOf("#") != -1)
                type = type.substring(type.indexOf("#")+1);
            for(ResourceType child:model.getApplication().getResourceType()) {
                if(type.equals(child.getId())) {
                    this.resourceRef = child;
                }
            }
        }

        addMethod = new AddMethodAction(getResourceRef()!=null?getResourceRef():getResource(),
                getResource().getPath(), model);
        addMethod.addPropertyChangeListener(this);
        addResource = new AddResourceAction(getResource(), model);
        addResource.addPropertyChangeListener(this);
        getActions().addAction(ActionFactory.createPopupMenuAction(
                new DesignViewPopupProvider(new Action [] {
            addMethod,
            addResource
        })));
        initUI();
        if(ExpanderWidget.isExpanded(this, false))
            setExpanded(true);
    }
    
    public Resource getResource() {
        return (Resource) getWadlComponent();
    }

    public ResourceType getResourceRef() {
        return this.resourceRef;
    }
    
    public String getPath() {
        return getResource().getPath();
    }
    
    @Override
    public void createHeader() throws IOException {
        Image image = null;
        image = ImageUtilities.loadImage(IMAGE_RESOURCE);
        String path = getResource().getPath();
        StringBuffer sb = new StringBuffer();
        findPath(getResource(), sb);
        String serviceUrl = sb.toString();
        final int skipLen = serviceUrl.length() - path.length();
        final String serviceBase = serviceUrl.substring(0, skipLen);
        headerLabelWidget = new ImageLabelWidget(getScene(), image, serviceUrl);
        headerLabelWidget.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
        headerLabelWidget.setLabelEditor(new TextFieldInplaceEditor(){
            public boolean isEnabled(Widget widget) {
                return true;
            }
            
            public String getText(Widget widget) {
                return headerLabelWidget.getLabel().substring(skipLen);
            }
            
            public void setText(Widget widget, String text) {
                headerLabelWidget.setLabel(serviceBase+text);
                try {
                    getModel().startTransaction();
                    getResource().setPath(text);
                } finally {
                    getModel().endTransaction();
                }
            }
        });
        headerLabelWidget.setToolTipText(serviceUrl);
        getHeaderWidget().addChild(headerLabelWidget);
        getHeaderWidget().addChild(new Widget(getScene()),1);
        
        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.CENTER, 8));

        ButtonWidget addMethodButton = new ButtonWidget(getScene(), addMethod);
        addMethodButton.setOpaque(true);
        addMethodButton.setRoundedBorder(addMethodButton.BORDER_RADIUS, 4, 0, null);

        ButtonWidget addResourceButton = new ButtonWidget(getScene(), addResource);
        addResourceButton.setOpaque(true);
        addResourceButton.setRoundedBorder(addResourceButton.BORDER_RADIUS, 4, 0, null);
        
        RemoveResourceAction removeMethod = new RemoveResourceAction(
                Collections.singleton(getResource()), getModel());
        removeMethod.addPropertyChangeListener((PropertyChangeListener) this.containerWidget);
        ButtonWidget removeResourceButton = new ButtonWidget(getScene(), removeMethod);
        removeResourceButton.setOpaque(true);
        removeResourceButton.setRoundedBorder(removeResourceButton.BORDER_RADIUS, 4, 0, null);

        buttons.addChild(addMethodButton);
        buttons.addChild(addResourceButton);
        buttons.addChild(removeResourceButton);
        buttons.addChild(getExpanderWidget());

        getHeaderWidget().addChild(buttons);
    }
        
    @Override
    public void createContent() throws IOException {
        super.createContent();
        getContentWidget().setBorder(BorderFactory.createEmptyBorder(RADIUS));

        ResourceType ref = getResourceRef()!=null?getResourceRef():getResource();
        templateParamsWidget = new ParametersWidget(
                NbBundle.getMessage(ParametersWidget.class, "LBL_Param", "Template"),
                ParamStyle.TEMPLATE, getObjectScene(), ref, ref.getParent(),
                ParametersWidget.getParameters(ref.getParam(), ParamStyle.HEADER), getModel());
        getContentWidget().addChild(templateParamsWidget);

        queryParamsWidget = new ParametersWidget(
                NbBundle.getMessage(ParametersWidget.class, "LBL_Param", "Query"),
                ParamStyle.QUERY, getObjectScene(), ref, ref.getParent(),
                ParametersWidget.getParameters(ref.getParam(), ParamStyle.QUERY), getModel());
        templateParamsWidget.setTitle(NbBundle.getMessage(ParametersWidget.class, "LBL_Param", "Query"));
        getContentWidget().addChild(queryParamsWidget);

        for (Method m : ref.getMethod()) {
            MethodWidget methodWidget = new MethodWidget(getObjectScene(), this, m, getModel());
            getContentWidget().addChild(methodWidget);
        }

        //then display the sub-resources
        if(getResourceRef() == null) {
            for (Resource r : getResource().getResource()) {
                ResourceWidget resourceWidget = new ResourceWidget(getObjectScene(), this, r, getModel());
                getContentWidget().addChild(resourceWidget);
            }
        }
    }

    public Object hashKey() {
        return getResource();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        boolean expand = false;
        if(evt.getPropertyName().equals(AddMethodAction.ADD_METHOD)) {
            try {
                Method m = (Method) evt.getNewValue();
                MethodWidget methodWidget = new MethodWidget(getObjectScene(), this, m, getModel());
                getContentWidget().addChild(methodWidget);
                expand = true;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if(evt.getPropertyName().equals(AddResourceAction.ADD_RESOURCE)) {
            try {
                Resource r = (Resource) evt.getNewValue();
                ResourceWidget resourceWidget = new ResourceWidget(getObjectScene(), this, r, getModel());
                getContentWidget().addChild(resourceWidget);
                expand = true;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if(evt.getPropertyName().equals(RemoveMethodAction.REMOVE_METHODS)) {
            List<Widget> childs = getContentWidget().getChildren();
            Set<Method> methods = (Set<Method>) evt.getOldValue();
            List<Widget> removeList = new ArrayList<Widget>();
            for(Method m:methods){
                for(Widget w:childs) {
                    if(w instanceof MethodWidget && ((MethodWidget)w).getMethodName().equals(m.getName()) &&
                            (m.getId() == null || (m.getId() != null && ((MethodWidget)w).getMethod().getId().equals(m.getId())))) {
                        removeList.add(w);
                    }
                }
            }
            getContentWidget().removeChildren(removeList);
            expand = true;
        } else if(evt.getPropertyName().equals(RemoveResourceAction.REMOVE_RESOURCES)) {
            List<Widget> childs = getContentWidget().getChildren();
            Set<Resource> resources = (Set<Resource>) evt.getOldValue();
            List<Widget> removeList = new ArrayList<Widget>();
            for(Resource r:resources){
                for(Widget w:childs) {
                    if(w instanceof ResourceWidget && ((ResourceWidget)w).getPath().equals(r.getPath()))
                        removeList.add(w);
                }
            }
            getContentWidget().removeChildren(removeList);
            expand = true;
        }
        getScene().validate();
        if(expand) {
            expandWidget();
        }
    }

}
