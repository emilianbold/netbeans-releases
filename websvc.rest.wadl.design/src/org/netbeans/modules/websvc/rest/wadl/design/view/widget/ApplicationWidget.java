/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.awt.Color;
import java.awt.Image;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.rest.wadl.model.WadlModel;
import org.netbeans.modules.websvc.rest.wadl.design.view.DesignViewPopupProvider;
import org.netbeans.modules.websvc.rest.wadl.design.view.actions.AddResourceAction;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Ayub Khan
 */
public class ApplicationWidget extends WadlComponentWidget {
    
    private static final String IMAGE_ONE_WAY  = 
            "org/netbeans/modules/websvc/rest/wadl/design/view/resources/oneway_operation.png"; // NOI18N   
    private static final String IMAGE_REQUEST_RESPONSE  = 
            "org/netbeans/modules/websvc/rest/wadl/design/view/resources/requestresponse_operation.png"; // NOI18N   
    private static final String IMAGE_NOTIFICATION  = 
            "org/netbeans/modules/websvc/rest/wadl/design/view/resources/notification_operation.png"; // NOI18N   

    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;

    private transient TabbedPaneWidget tabbedWidget;
    private transient Widget listWidget;
    private transient ButtonWidget viewButton;

    private transient AddResourceAction addResourceAction;

    
    /**
     * Creates a new instance of MethodWidget
     * @param scene
     * @param operation
     */
    public ApplicationWidget(ObjectScene scene, WadlModel model) throws IOException {
        super(scene, model.getApplication(), model);
        addResourceAction = new AddResourceAction(getWadlComponent(), model);
        getActions().addAction(ActionFactory.createPopupMenuAction(
                new DesignViewPopupProvider(new Action [] {
            addResourceAction
        })));
        initUI();
    }
    
    @Override
    public void createHeader() throws IOException {
        String typeOfOperation ="";
        Image image = ImageUtilities.loadImage(IMAGE_NOTIFICATION);
        headerLabelWidget = new ImageLabelWidget(getScene(), image, NbBundle.getMessage(
                ApplicationWidget.class, "LBL_Resources"));
        headerLabelWidget.setToolTipText(typeOfOperation);
        getHeaderWidget().addChild(headerLabelWidget);
        getHeaderWidget().addChild(new Widget(getScene()),1);

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.CENTER, 8));
        viewButton = new ButtonWidget(getScene(),null,null);
        viewButton.setImage(new TabImageWidget(getScene(),16));
        viewButton.setSelectedImage(new ListImageWidget(getScene(),16));
        viewButton.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                setTabbedView(!viewButton.isSelected());
            }
        });
        buttons.addChild(viewButton);

        buttons.addChild(getExpanderWidget());

        getHeaderWidget().addChild(buttons);
    }

    @Override
    public Object hashKey() {
        return getWadlComponent();
    }

    private void setTabbedView(boolean tabbedView) {
        if(viewButton.isSelected()!=tabbedView) {
            viewButton.setSelected(tabbedView);
            if(tabbedView) {
                if(listWidget.getParentWidget()==getContentWidget())
                    getContentWidget().removeChild(listWidget);
                getContentWidget().addChild(tabbedWidget);
            } else {
                if(tabbedWidget.getParentWidget()==getContentWidget())
                    getContentWidget().removeChild(tabbedWidget);
                getContentWidget().addChild(listWidget);
            }
        }
    }

    private static class ListImageWidget extends ImageLabelWidget.PaintableImageWidget {

        public ListImageWidget(Scene scene, int size) {
            super(scene, Color.LIGHT_GRAY, size, size);
            setBorder(BorderFactory.createLineBorder(0, Color.LIGHT_GRAY));
            setBackground(Color.WHITE);
            setOpaque(true);
            setToolTipText(NbBundle.getMessage(ApplicationWidget.class, "Hint_ListView"));
        }

        protected Shape createImage(int width, int height) {
            GeneralPath path = new GeneralPath();
            float gap = width/5;
            path.moveTo(gap, height/4);
            path.lineTo(width-gap, height/4);
            path.moveTo(gap, height/2);
            path.lineTo(width-gap, height/2);
            path.moveTo(gap, 3*height/4);
            path.lineTo(width-2*gap, 3*height/4);
            return path;
        }
    }

    private static class TabImageWidget extends ImageLabelWidget.PaintableImageWidget {

        public TabImageWidget(Scene scene, int size) {
            super(scene, Color.LIGHT_GRAY, size, size);
            setBorder(BorderFactory.createLineBorder(0, Color.LIGHT_GRAY));
            setBackground(Color.WHITE);
            setOpaque(true);
            setToolTipText(NbBundle.getMessage(ApplicationWidget.class, "Hint_TabbedView"));
        }

        protected Shape createImage(int width, int height) {
            GeneralPath path = new GeneralPath();
            path.moveTo(1, height/6);
            path.lineTo(2*width/3, height/6);
            path.moveTo(1, height/3+1);
            path.lineTo(width-1, height/3+1);
            path.moveTo(width/3, height/6+1);
            path.lineTo(width/3, height/3);
            path.moveTo(2*width/3, height/6+1);
            path.lineTo(2*width/3, height/3);
            return path;
        }
    }

}
