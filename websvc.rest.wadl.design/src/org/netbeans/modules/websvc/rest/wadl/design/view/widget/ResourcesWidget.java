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
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.netbeans.modules.websvc.rest.wadl.design.view.DesignViewPopupProvider;
import org.netbeans.modules.websvc.rest.wadl.design.view.actions.AddResourceAction;
import org.netbeans.modules.websvc.rest.wadl.design.view.actions.RemoveResourceAction;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Ayub Khan
 */
public class ResourcesWidget extends WadlComponentWidget implements PropertyChangeListener {

    private static final String IMAGE_RESOURCES =
            "org/netbeans/modules/websvc/rest/wadl/design/view/resources/folder.png"; // NOI18N   
    private transient AddResourceAction addAction;
    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;
    private ObjectSceneListener resourceSelectionListener;

    /**
     * Creates a new instance of ResourcesWidget
     * @param scene
     * @param service
     * @param serviceModel
     */
    public ResourcesWidget(ObjectScene scene, WadlModel model) throws IOException {
        super(scene, model.getApplication() != null ? model.getApplication().getResources().iterator().next() : null, model);
        addAction = new AddResourceAction(model.getApplication(), model);
        addAction.addPropertyChangeListener(this);
        getActions().addAction(ActionFactory.createPopupMenuAction(
                new DesignViewPopupProvider(new Action[]{
                    addAction,
                })));
        initUI();
        setExpanded(true);
    }

    public Resources getResources() {
        return (Resources) getWadlComponent();
    }

    @Override
    public void createHeader() throws IOException {
        if (getResources() == null) {
            return;
        }
        Image image = ImageUtilities.loadImage(IMAGE_RESOURCES);
        headerLabelWidget = new ImageLabelWidget(getScene(), image,
                NbBundle.getMessage(ResourcesWidget.class, "LBL_Resources"));
        headerLabelWidget.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
        getHeaderWidget().addChild(headerLabelWidget);
        getHeaderWidget().addChild(new Widget(getScene()), 1);
        updateHeaderLabel();

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.CENTER, 8));

        ButtonWidget addButton = new ButtonWidget(getScene(), addAction);
        addButton.setOpaque(true);
        addButton.setRoundedBorder(addButton.BORDER_RADIUS, 4, 0, null);

        buttons.addChild(addButton);
        buttons.addChild(getExpanderWidget());

        getHeaderWidget().addChild(buttons);
    }

    @Override
    public void createContent() throws IOException {
        super.createContent();
        if (getResources() != null) {
            for (Resource resource : getResources().getResource()) {
                ResourceWidget resourceWidget = new ResourceWidget(getObjectScene(), this, resource, getModel());
                getContentWidget().addChild(resourceWidget);
            }
        }
    }

    private void updateHeaderLabel() {
        int noOfResources = getResources() == null ? 0 : getResources().getResource().size();
        headerLabelWidget.setComment("(" + noOfResources + ")");
    }

    public Object hashKey() {
        return getResources();
    }

    protected void notifyAdded() {
        super.notifyAdded();
    }

    protected void notifyRemoved() {
        super.notifyRemoved();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        boolean expand = false;
        if (evt.getPropertyName().equals(AddResourceAction.ADD_RESOURCE)) {
            try {
                Resource r = (Resource) evt.getNewValue();
                ResourceWidget methodWidget = new ResourceWidget(getObjectScene(), this,
                        r, getModel());
                getContentWidget().addChild(methodWidget);
                expand = true;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if (evt.getPropertyName().equals(RemoveResourceAction.REMOVE_RESOURCES)) {
            List<Widget> childs = getContentWidget().getChildren();
            Set<Resource> resourceSet = (Set<Resource>) evt.getOldValue();
            List<Widget> removeList = new ArrayList<Widget>();
            for (Resource r : resourceSet) {
                for (Widget w : childs) {
                    if (w instanceof ResourceWidget && ((ResourceWidget) w).getPath().equals(r.getPath())) {
                        removeList.add(w);
                    }
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
