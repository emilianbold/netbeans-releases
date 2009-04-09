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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.rest.wadl.design.view.actions.*;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * @author Ayub Khan
 */
public class ParametersWidget<T extends WadlComponent> extends AbstractTitledWidget implements PropertyChangeListener {

    private static final String IMAGE_PARAMS =
            "org/netbeans/modules/websvc/rest/wadl/design/view/resources/param.png"; // NOI18N 
    private transient T parent;
    private transient T ancestor;
    private transient Collection<Param> params;
    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;
    private transient ParametersTableModel model;
    private transient TableWidget parameterTable;
    private transient AddParamAction addParam;
    private ObjectSceneListener paramSelectionListener;
    RemoveParamAction removeAction;
    private boolean isReplaceInAction;
    private String title;
    private WadlModel wadlModel;
    private ParamStyle type;
    LabelWidget noParamsWidget;

    /** 
     * Creates a new instance of OperationWidget 
     * @param scene 
     * @param method 
     */
    public ParametersWidget(String title, ParamStyle type, ObjectScene scene, 
            T parent, T ancestor, Collection<Param> params, WadlModel wadlModel) throws IOException {
        super(scene, RADIUS, RADIUS, RADIUS / 2, BORDER_COLOR);
        this.title = title;
        this.type = type;
        this.parent = parent;
        this.ancestor = ancestor;
        this.params = params;
        this.wadlModel = wadlModel;
        addParam = new AddParamAction(type, parent, ancestor, wadlModel);
        addParam.addPropertyChangeListener(this);

        removeAction = new RemoveParamAction(parent, wadlModel);
        removeAction.addPropertyChangeListener(this);
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

    public void initUI() {
        model = new ParametersTableModel(params, parent.getValidParamStyles(false), getModel());
        populateContentWidget(getContentWidget());
        getContentWidget().setBorder(BorderFactory.createEmptyBorder(0, 1, 1, 1));
        Image image = ImageUtilities.loadImage(IMAGE_PARAMS);
        headerLabelWidget = new ImageLabelWidget(getScene(), image, getTitle(),
                "(" + params.size() + ")");
        headerLabelWidget.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
        getHeaderWidget().addChild(headerLabelWidget);
        getHeaderWidget().addChild(new Widget(getScene()), 1);

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.CENTER, 8));

        ButtonWidget addParamButton = new ButtonWidget(getScene(), addParam);
        addParamButton.setOpaque(true);
        addParamButton.setRoundedBorder(addParamButton.BORDER_RADIUS, 4, 0, null);

        ButtonWidget removeParamButton = new ButtonWidget(getScene(), removeAction);
        removeParamButton.setOpaque(true);
        removeParamButton.setRoundedBorder(removeParamButton.BORDER_RADIUS, 4, 0, null);

        buttons.addChild(addParamButton);
        buttons.addChild(removeParamButton);
        buttons.addChild(getExpanderWidget());
        buttons.setOpaque(true);
        buttons.setBackground(TITLE_COLOR_BRIGHT);

        getHeaderWidget().addChild(buttons);

    }

    private void populateContentWidget(Widget parentWidget) {
        if (parameterTable != null && parameterTable.getParentWidget() == parentWidget) {
            isReplaceInAction = true;
            parentWidget.removeChild(parameterTable);
            isReplaceInAction = false;
        }
        if (noParamsWidget != null && noParamsWidget.getParentWidget() == parentWidget) {
            parentWidget.removeChild(noParamsWidget);
        }
        if (model.getRowCount() > 0) {
            parameterTable = new TableWidget(getScene(), model);
            parentWidget.addChild(parameterTable);
        } else {
            noParamsWidget = new LabelWidget(getScene(),
                    NbBundle.getMessage(ParametersWidget.class, "LBL_ParamNone"));
            noParamsWidget.setAlignment(LabelWidget.Alignment.CENTER);
            parentWidget.addChild(noParamsWidget);
        }
    }

    public Object hashKey() {
        return model;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Image getIcon() {
        return ImageUtilities.loadImage("org/netbeans/modules/websvc/rest/wadl/design/view/resources/input.png");
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
                removeAction.setWorkingSet(params);
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
        if (evt.getPropertyName().equals(AddParamAction.ADD_PARAM)) {
            Param param = (Param) evt.getNewValue();
            model.addParameter(param);
            populateContentWidget(getContentWidget());
            expand = true;
        } else if (evt.getPropertyName().equals(RemoveParamAction.REMOVE_PARAMS)) {
            Set<Param> resources = (Set<Param>) evt.getOldValue();
            for (Param p : resources) {
                model.removeParameter(p);
            }
            if (!resources.isEmpty()) {
                populateContentWidget(getContentWidget());
            }
            expand = true;
        }
        getScene().validate();
        if(expand) {
            expandWidget();
        }
    }

    static Collection getParameters(Collection<Param> params, ParamStyle style) {
        List<Param> filteredParams = new ArrayList<Param>();
        for (Param p : params) {
            if (style.value().equals(p.getStyle())) {
                filteredParams.add(p);
            }
        }
        return Collections.unmodifiableList(filteredParams);
    }
}
