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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.netbeans.modules.websvc.rest.wadl.design.view.actions.RemoveMethodAction;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Ayub Khan
 */
public class MethodWidget extends WadlComponentWidget {

    private static final String IMAGE_METHOD =
            "org/netbeans/modules/websvc/rest/wadl/design/view/resources/method.png"; // NOI18N     
    private transient Widget buttons;
    private transient ImageLabelWidget headerLabelWidget;
    private transient TabbedPaneWidget tabbedWidget;
    private transient Widget listWidget;
    private transient ButtonWidget viewButton;
    private RequestWidget requestWidget;
    private ResponseWidget responseWidget;
    private Widget containerWidget;
    private boolean showServiceUrl;
    private Method methodRef;
    
    /**
     * Creates a new instance of MethodWidget
     * @param scene
     * @param operation
     */
    public MethodWidget(ObjectScene scene, Widget containerWidget, Method method, 
            WadlModel model) throws IOException {
        this(scene, containerWidget, method, model, true);
    }
    
    /**
     * Creates a new instance of MethodWidget
     * @param scene
     * @param operation
     */
    public MethodWidget(ObjectScene scene, Widget containerWidget, Method method, 
            WadlModel model, boolean showServiceUrl) throws IOException {
        super(scene, method, model);
        this.containerWidget = containerWidget;
        this.showServiceUrl = showServiceUrl;

        String href = method.getHref();
        if(href != null) {
            if(href.indexOf("#") != -1)
                href = href.substring(href.indexOf("#")+1);
            for(Method child:model.getApplication().getMethod()) {
                if(href.equals(child.getId())) {
                    this.methodRef = child;
                }
            }
        }

        initUI();
        if(ExpanderWidget.isExpanded(this, false))
            setExpanded(true);
    }
    
    public String getId() {
        return getMethod().getId() != null ? getMethod().getId() : "";
    }

    public String getDisplayName() {
        if (this.showServiceUrl) {
            StringBuffer sb = new StringBuffer();
            findPath(getMethod(), sb);
            if(getMethodRef() != null)
                sb.append("?"+getMethodRef().getId());
            else
                sb.append("?"+getId());
            return sb.toString();
        } else {
            String name = "";
            if (getMethod().getId() != null) {
                name += " " + getMethod().getId();
            }
            return name;
        }
    }

    public String getMethodName() {
        if(getMethodRef() != null)
            return getMethodRef().getName();
        else
            return getMethod().getName();
    }

    public void setMethodName(String name) {
        if(getMethodRef() != null)
            getMethodRef().setName(name);
        else
            getMethod().setName(name);
    }

    public Method getMethod() {
        return (Method) getWadlComponent();
    }

    public Method getMethodRef() {
        return this.methodRef;
    }

    @Override
    public void createHeader() throws IOException {
        Image image = ImageUtilities.loadImage(IMAGE_METHOD);
        ImageLabelWidget imageLabelWidget = new ImageLabelWidget(getScene(), image, "");
        
        //Display Methods combobox
        JComboBox cb = new JComboBox(MethodType.values(true));
        if (getMethodName() == null) {
            try {
                getModel().startTransaction();
                setMethodName(MethodType.GET.value().toUpperCase());
            } finally {
                getModel().endTransaction();
            }
        }
        cb.setSelectedItem(getMethodName());
        cb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox tf = (JComboBox) e.getSource();
                try {
                    getModel().startTransaction();
                    setMethodName((String) tf.getSelectedItem());
                } finally {
                    getModel().endTransaction();
                }
            }
        });
        ComponentWidget mNameWidget = new ComponentWidget(getScene(), cb);
        
        //Display service?method
        String serviceUrl = getDisplayName();
        final int skipLen = serviceUrl.length() - getId().length();
        final String serviceBase = serviceUrl.substring(0, skipLen);
        headerLabelWidget = new ImageLabelWidget(getScene(), null, serviceUrl);
        headerLabelWidget.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
        if(getMethodRef() == null) {
            headerLabelWidget.setLabelEditor(new TextFieldInplaceEditor() {

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
                        getMethod().setId(text);
                    } finally {
                        getModel().endTransaction();
                    }
                }
            });
        }
        headerLabelWidget.setToolTipText(getDisplayName());
        getHeaderWidget().addChild(imageLabelWidget);
        getHeaderWidget().addChild(mNameWidget);
        getHeaderWidget().addChild(headerLabelWidget);
        getHeaderWidget().addChild(new Widget(getScene()), 1);

        buttons = new Widget(getScene());
        buttons.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 8));
        viewButton = new ButtonWidget(getScene(),null,null);
        viewButton.setImage(new TabImageWidget(getScene(),16));
        viewButton.setSelectedImage(new ListImageWidget(getScene(),16));
        viewButton.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                setTabbedView(!viewButton.isSelected());
            }
        });
        buttons.addChild(viewButton);
        
        RemoveMethodAction removeMethod = new RemoveMethodAction(Collections.singleton(getMethod()), getModel());
        if(this.containerWidget instanceof ResourceWidget)
            removeMethod.addPropertyChangeListener((ResourceWidget)this.containerWidget);
        else if(this.containerWidget instanceof ListMethodsWidget)
            removeMethod.addPropertyChangeListener((ListMethodsWidget)this.containerWidget);
//        removeMethod.addPropertyChangeListener(DesignView.getListMethodsWidget());
        ButtonWidget removeMethodButton = new ButtonWidget(getScene(), removeMethod);
        removeMethodButton.setOpaque(true);
        removeMethodButton.setRoundedBorder(removeMethodButton.BORDER_RADIUS, 4, 0, null);

        buttons.addChild(removeMethodButton);
        buttons.addChild(getExpanderWidget());

        getHeaderWidget().addChild(buttons);
    }

    @Override
    public void createContent() throws IOException {
        super.createContent();
        listWidget = new Widget(getScene());
        listWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, RADIUS/2));
        requestWidget = new RequestWidget(getObjectScene(), getMethodRef()!=null?getMethodRef():getMethod(), getModel());
        responseWidget = new ResponseWidget(getObjectScene(), getMethodRef()!=null?getMethodRef():getMethod(), getModel());
        listWidget.addChild(requestWidget);
        listWidget.addChild(responseWidget);

        tabbedWidget = new TabbedPaneWidget(getScene());
        tabbedWidget.addTab(requestWidget);
        tabbedWidget.addTab(responseWidget);
        
        setTabbedView(!viewButton.isSelected());
    }

    public Object hashKey() {
        return getMethod();
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

}
