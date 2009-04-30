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

import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import javax.swing.JComboBox;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.rest.wadl.design.MediaType;
import org.netbeans.modules.websvc.rest.wadl.design.view.actions.RemoveBodyElementAction;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.openide.util.NbBundle;

/**
 * @author Ayub Khan
 */
public abstract class RepresentationTypeWidget extends WadlComponentWidget {

    private transient Widget buttons;
    ParametersWidget queryParamsWidget;
    RemoveBodyElementAction removeAction;
    private Widget parent;
    private RepresentationType repRef;
    
    /**
     * Creates a new instance of MethodWidget
     * @param scene
     * @param method
     */
    public RepresentationTypeWidget(ObjectScene scene, Widget parent, RepresentationType rep,
            WadlModel model) throws IOException {
        super(scene, rep, model);
        this.parent = parent;

        String href = rep.getHref();
        if(href != null) {
            if(href.indexOf("#") != -1)
                href = href.substring(href.indexOf("#")+1);
            for(RepresentationType child:model.getApplication().getRepresentationType()) {
                if(href.equals(child.getId())) {
                    this.repRef = child;
                }
            }
        }

        removeAction = new RemoveBodyElementAction(Collections.singleton(rep), model);
        removeAction.addPropertyChangeListener((PropertyChangeListener) parent);
        initUI();
        if(ExpanderWidget.isExpanded(this, false))
            setExpanded(true);
    }

    @Override
    protected abstract Paint getTitlePaint(Rectangle bounds);

    public RepresentationType getRepresentationType() {
        if(this.repRef == null)
            return (RepresentationType) getWadlComponent();
        else
            return this.repRef;
    }

    @Override
    public void createHeader() throws IOException {
        ImageLabelWidget imageLabelWidget = new ImageLabelWidget(getScene(), getIcon(),
                getTitle());

        getHeaderWidget().addChild(imageLabelWidget);
        getHeaderWidget().addChild(new Widget(getScene()), 1);

        ButtonWidget removeChildButton = new ButtonWidget(getScene(), removeAction);
        removeChildButton.setOpaque(true);
        removeChildButton.setRoundedBorder(removeChildButton.BORDER_RADIUS, 4, 0, null);
        
        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 8));
        buttons.addChild(removeChildButton);
        buttons.addChild(getExpanderWidget());
        
        getHeaderWidget().addChild(buttons);
    }

    @Override
    public void createContent() throws IOException {
        super.createContent();
        if (getWadlComponent() != null) {

            //Display Methods combobox
            JComboBox cb = new JComboBox(MediaType.values(false));
            String mediaType = getRepresentationType().getMediaType();
            if(mediaType == null)
                mediaType = MediaType.PLAIN.value();
            if(!mediaType.equals(MediaType.fromValue(mediaType).value()))
                cb.addItem(mediaType);
            cb.setSelectedItem(mediaType.toLowerCase());
            cb.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    JComboBox tf = (JComboBox) e.getSource();
                    try {
                        getModel().startTransaction();
                        getRepresentationType().setMediaType((String) tf.getSelectedItem());
                    } finally {
                        getModel().endTransaction();
                    }
                }
            });
            ComponentWidget mediaTypeWidget = new ComponentWidget(getScene(), cb);
            getContentWidget().addChild(mediaTypeWidget);

            queryParamsWidget = new ParametersWidget(
                    NbBundle.getMessage(ParametersWidget.class, "LBL_Param", "Query"),
                    ParamStyle.QUERY, getObjectScene(), getRepresentationType(),
                    getRepresentationType().getParent(),
                    ParametersWidget.getParameters(getRepresentationType().getParam(), ParamStyle.QUERY),
                    getModel());
            getContentWidget().addChild(queryParamsWidget);
        }
    }

    public abstract String getTitle();

    public abstract Image getIcon();

    @Override
    public Object hashKey() {
        return getWadlComponent();
    }

}
