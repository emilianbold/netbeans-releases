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
import java.awt.Paint;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.rest.wadl.design.view.actions.AddBodyElementAction;
import org.netbeans.modules.websvc.rest.wadl.design.view.actions.RemoveBodyElementAction;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 * @author Ayub Khan
 */
public class BodyWidget<T extends WadlComponent> extends AbstractTitledWidget implements PropertyChangeListener {

    private transient T parent;
    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;
    private transient AddBodyElementAction addAction;
    private ObjectSceneListener paramSelectionListener;
    private boolean isReplaceInAction;
    private String title;
    private WadlModel wadlModel;
    private ParamStyle type;
    private Object key = new Object();

    /** 
     * Creates a new instance of OperationWidget 
     * @param scene 
     * @param method 
     */
    public BodyWidget(String title, ObjectScene scene, T parent, WadlModel wadlModel) throws IOException {
        super(scene, RADIUS, RADIUS, RADIUS / 2, BORDER_COLOR);
        this.title = title;
        this.parent = parent;
        this.wadlModel = wadlModel;
        addAction = new AddBodyElementAction(type, parent, wadlModel);
        addAction.addPropertyChangeListener(this);
        initUI();
        if(ExpanderWidget.isExpanded(this, false))
            setExpanded(true);
    }

    public WadlModel getModel() {
        return wadlModel;
    }

    protected Paint getTitlePaint(Rectangle bounds) {
        return TITLE_COLOR_PARAMETER;
    }

    public WadlComponent getWadlComponent() {
        return (WadlComponent) parent;
    }

    public void initUI() throws IOException {
        createHeader();
        createContent();
    }

    public void createHeader() {
        headerLabelWidget = new ImageLabelWidget(getScene(), getIcon(), getTitle());
        headerLabelWidget.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
        getHeaderWidget().addChild(headerLabelWidget);
        getHeaderWidget().addChild(new Widget(getScene()), 1);

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.CENTER, 8));

        ButtonWidget addChildButton = new ButtonWidget(getScene(), addAction);
        addChildButton.setOpaque(true);
        addChildButton.setRoundedBorder(addChildButton.BORDER_RADIUS, 4, 0, null);

        buttons.addChild(addChildButton);
        buttons.addChild(getExpanderWidget());
        buttons.setOpaque(true);
        buttons.setBackground(TITLE_COLOR_BRIGHT);

        getHeaderWidget().addChild(buttons);

    }

    private void createContent() throws IOException {
        if (getWadlComponent() instanceof Request) {
            for (Representation rep : ((Request) getWadlComponent()).getRepresentation()) {
                RepresentationWidget resourceWidget = new RepresentationWidget(getObjectScene(), this, rep, getModel());
                getContentWidget().addChild(resourceWidget);
            }
        } else if (getWadlComponent() instanceof Response) {
            for (Representation rep : ((Response) getWadlComponent()).getRepresentation()) {
                RepresentationWidget resourceWidget = new RepresentationWidget(getObjectScene(), this, rep, getModel());
                getContentWidget().addChild(resourceWidget);
            }
            for (Fault rep : ((Response) getWadlComponent()).getFault()) {
                FaultWidget resourceWidget = new FaultWidget(getObjectScene(), this, rep, getModel());
                getContentWidget().addChild(resourceWidget);
            }
        }
    }

    public Object hashKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Image getIcon() {
        return ImageUtilities.mergeImages(
                ImageUtilities.loadImage("org/netbeans/modules/websvc/rest/wadl/design/view/resources/folder.png"), 
                ImageUtilities.loadImage("org/netbeans/modules/websvc/rest/wadl/design/view/resources/representation.png"),
                3, 8);
    }

    protected void notifyAdded() {
        if (isReplaceInAction) {
            return;
        }
        super.notifyAdded();
        paramSelectionListener = new ObjectSceneAdapter() {

            public void selectionChanged(ObjectSceneEvent event,
                    Set<Object> previousSelection, Set<Object> newSelection) {
                Set<Param> params = new HashSet<Param>();
                if (newSelection != null) {
                    for (Object obj : newSelection) {
                        if (obj instanceof Param) {
                            params.add((Param) obj);
                        }
                    }
                }
            }
        };
        getObjectScene().addObjectSceneListener(paramSelectionListener,
                ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
    }

    protected void notifyRemoved() {
        if (isReplaceInAction) {
            return;
        }
        super.notifyRemoved();
        if (paramSelectionListener != null) {
            getObjectScene().removeObjectSceneListener(paramSelectionListener,
                    ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
            paramSelectionListener = null;
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        boolean expand = false;
        if (evt.getPropertyName().equals(AddBodyElementAction.ADD_BODYELEMENT)) {
            if (evt.getNewValue() instanceof Representation) {
                try {
                    Representation m = (Representation) evt.getNewValue();
                    RepresentationWidget repWidget = new RepresentationWidget(getObjectScene(), this, m, getModel());
                    getContentWidget().addChild(repWidget);
                    expand = true;
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else if (evt.getNewValue() instanceof Fault) {
                try {
                    Fault m = (Fault) evt.getNewValue();
                    FaultWidget faultWidget = new FaultWidget(getObjectScene(), this, m, getModel());
                    getContentWidget().addChild(faultWidget);
                    expand = true;
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else if (evt.getPropertyName().equals(RemoveBodyElementAction.REMOVE_BODYELEMENTS)) {
            List<Widget> childs = getContentWidget().getChildren();
            Set<RepresentationType> reps = (Set<RepresentationType>) evt.getOldValue();
            List<Widget> removeList = new ArrayList<Widget>();
            for (RepresentationType rep : reps) {
                for (Widget w : childs) {
                    if (w instanceof RepresentationTypeWidget &&
                            ((RepresentationTypeWidget) w).getWadlComponent() == rep) {
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
