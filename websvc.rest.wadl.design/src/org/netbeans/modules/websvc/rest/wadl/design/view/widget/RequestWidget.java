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
import java.io.IOException;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Ayub Khan
 */
public class RequestWidget extends WadlComponentWidget implements TabWidget {

    private static final String IMAGE_REQUEST =
            "org/netbeans/modules/websvc/rest/wadl/design/view/resources/request.png"; // NOI18N     
    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;
    private transient Widget tabComponent;
    private Method method;

    /**
     * Creates a new instance of MethodWidget
     * @param scene
     * @param method
     */
    public RequestWidget(ObjectScene scene, Method method, WadlModel model) throws IOException {
        super(scene, 
              !method.getRequest().isEmpty()?method.getRequest().iterator().next():
                  model.getFactory().createRequest(), model);
        this.method = method;
        initUI();
        if(ExpanderWidget.isExpanded(this, false))
            setExpanded(true);
    }

    @Override
    protected Paint getTitlePaint(Rectangle bounds) {
        return TITLE_COLOR_METHOD_BODY;
    }

    public Request getRequest() {
        return (Request) getWadlComponent();
    }

    @Override
    public void createHeader() throws IOException {
        Image image = ImageUtilities.loadImage(IMAGE_REQUEST);
        headerLabelWidget = new ImageLabelWidget(getScene(), image, getTitle());
        headerLabelWidget.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
        getHeaderWidget().addChild(headerLabelWidget);
        getHeaderWidget().addChild(new Widget(getScene()), 1);

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.CENTER, 8));
        buttons.addChild(getExpanderWidget());
        getHeaderWidget().addChild(buttons);
    }

    @Override
    public void createContent() throws IOException {
        super.createContent();
        createContentChildren(getContentWidget());

    }

    public String getTitle() {
        return NbBundle.getMessage(MethodWidget.class, "LBL_Request");
    }

    public Image getIcon() {
        return Utilities.loadImage("org/netbeans/modules/websvc/rest/wadl/design/view/resources/request.png"); // NOI18N
    }

    @Override
    public Object hashKey() {
        return getRequest();
    }

    public Widget getComponentWidget() {
        if (tabComponent == null) {
            tabComponent = createContentWidget();
            createContentChildren(tabComponent);
        }
        return tabComponent;
    }

    private void createContentChildren(Widget containerWidget) {
        try {
            ParametersWidget headerParamsWidget = new ParametersWidget(
                    NbBundle.getMessage(ParametersWidget.class, "LBL_Param", "Header"),
                        ParamStyle.HEADER, getObjectScene(), getRequest(),
                        this.method,
                        ParametersWidget.getParameters(getRequest().getParam(),
                        ParamStyle.HEADER), getModel());
            containerWidget.addChild(headerParamsWidget);

            ParametersWidget queryParamsWidget = new ParametersWidget(
                    NbBundle.getMessage(ParametersWidget.class, "LBL_Param", "Query"),
                        ParamStyle.QUERY, getObjectScene(), getRequest(),
                        this.method,
                        ParametersWidget.getParameters(getRequest().getParam(),
                        ParamStyle.QUERY), getModel());
            containerWidget.addChild(queryParamsWidget);

            BodyWidget bodyWidget = new BodyWidget(
                    NbBundle.getMessage(BodyWidget.class, "LBL_Body", "Request"),
                    getObjectScene(), getRequest(), getModel());
            containerWidget.addChild(bodyWidget);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
