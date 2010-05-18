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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.io.IOException;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.SwingScrollWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Ayub Khan
 */
public class DocWidget<T extends WadlComponent> extends AbstractTitledWidget {
    
    private static final String IMAGE_DOC  = 
            "org/netbeans/modules/websvc/rest/wadl/design/view/resources/doc.png"; // NOI18N     

    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;

    private transient TabbedPaneWidget tabbedWidget;
    private transient Widget listWidget;
    private transient ButtonWidget viewButton;

    private WadlComponentWidget parentWidget;
    private Doc doc;
    private WadlModel model;

    
    /**
     * Creates a new instance of MethodWidget
     * @param scene
     * @param operation
     */
    public DocWidget(ObjectScene scene, WadlComponentWidget parentWidget, Doc doc, WadlModel model) throws IOException {
        super(scene,RADIUS,RADIUS,RADIUS/2,BORDER_COLOR);
        this.parentWidget = parentWidget;
        this.doc = doc;
        this.model = model;
        initUI();
        setExpanded(false);
    }
    
    private void initUI() {
        String typeOfOperation = "";
        Image image = ImageUtilities.loadImage(IMAGE_DOC);

        String title = "Double click to enter doc title.";
        if(doc != null && doc.getTitle() != null)
            title = doc.getTitle();
        headerLabelWidget = new ImageLabelWidget(getScene(), image, title);
        headerLabelWidget.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
        headerLabelWidget.setLabelEditor(new TextFieldInplaceEditor() {

            public boolean isEnabled(Widget widget) {
                return true;
            }

            public String getText(Widget widget) {
                return headerLabelWidget.getLabel();
            }

            public void setText(Widget widget, String text) {
                headerLabelWidget.setLabel(text);
                try {
                    doc = parentWidget.createDoc();
                    model.startTransaction();
                    doc.setTitle(text);
                } finally {
                    model.endTransaction();
                }
            }
        });
        headerLabelWidget.setToolTipText(typeOfOperation);
        getHeaderWidget().addChild(headerLabelWidget);
        getHeaderWidget().addChild(new Widget(getScene()), 1);

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 8));
        buttons.addChild(getExpanderWidget());

        getHeaderWidget().addChild(buttons);

        Widget repWidget = new Widget(getScene());
        repWidget.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.LEFT_TOP, 8));
        
        SwingScrollWidget scroll = new SwingScrollWidget (getScene());
        scroll.setMinimumSize (new Dimension (100, 50));
        scroll.setMaximumSize (new Dimension (600, 150));
        getContentWidget().addChild (scroll);
        
        String content = "Double click to add content.";
        if(doc != null && doc.getContent() != null)
            content = doc.getContent();
        final LabelWidget contentWidget = new LabelWidget(getScene(), content);
        contentWidget.getActions().addAction(ActionFactory.createInplaceEditorAction(
                new TextFieldInplaceEditor(){
            public boolean isEnabled(Widget widget) {
                return true;
            }

            public String getText(Widget widget) {
                return contentWidget.getLabel();
            }

            public void setText(Widget widget, String text) {
                contentWidget.setLabel(text);
                try {
                    doc = parentWidget.createDoc();
                    model.startTransaction();
                    doc.setContent(text);
                } finally {
                    model.endTransaction();
                }
            }
        }));
        repWidget.addChild(contentWidget);
        scroll.setView (repWidget);
        scroll.getActions ().addAction (ActionFactory.createResizeAction ());
    }

    public Object hashKey() {
        return doc;
    }

}
