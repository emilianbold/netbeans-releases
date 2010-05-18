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

package org.netbeans.modules.visualweb.designer.jsf.ui;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;

import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.designer.jsf.JsfForm;
import org.openide.awt.UndoRedo;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implemenation of JSF multiview element.
 *
 * @author Peter Zavadsky
 */
public class JsfMultiViewElement implements MultiViewElement {

//    private static final String PATH_TOOLBAR_FOLDER = "Designer/application/x-designer/Toolbars/Default"; // NOI18N
    
    private static final String TOP_COMPONENT_GROUP_JSF_DESIGNER = "jsfdesigner"; // NOI18N
    
//    private final Designer designer;
    private final JsfTopComponent jsfTopComponent;

    
    /** Creates a new instance of DesignerMultiViewElement */
    public JsfMultiViewElement(JsfForm jsfForm, Designer designer, DataObject jspDataObject) {
//        if (designer == null) {
//            throw new NullPointerException("The designer parameter is null!"); // NOI18N
//        }
//        this.designer = designer;
        jsfTopComponent = new JsfTopComponent(jsfForm, designer, jspDataObject);
    }

    
    public JsfTopComponent getJsfTopComponent() {
        return jsfTopComponent;
    }
    
    public JComponent getVisualRepresentation() {
//        return designer.getVisualRepresentation();
        return jsfTopComponent.getVisualRepresentation();
    }

    // XXX Moved from designer/../DesignerTopComp.
    // TODO Move it to JsfTopComponent.
    public JComponent getToolbarRepresentation() {
//        return designer.getToolbarRepresentation();
        return jsfTopComponent.getToolbarRepresentation();
    }

    public Action[] getActions() {
//        return designer.getActions();
        return jsfTopComponent.getActions();
    }

    public Lookup getLookup() {
//        return designer.getLookup();
        return jsfTopComponent.getLookup();
    }

    public void componentOpened() {
//        designer.componentOpened();
        jsfTopComponent.componentOpened();
    }

    public void componentClosed() {
//        designer.componentClosed();
        jsfTopComponent.componentClosed();
    }

    public void componentShowing() {
//        designer.componentShowing();
        jsfTopComponent.componentShowing();
        
        openJsfTopComponentGroupIfNeeded();
    }

    public void componentHidden() {
//        designer.componentHidden();
        jsfTopComponent.componentHidden();
        
        closeJsfTopComponentGroupIfNeeded();
    }

    public void componentActivated() {
//        designer.componentActivated();
        jsfTopComponent.componentActivated();
    }

    public void componentDeactivated() {
//        designer.componentDeactivated();
        jsfTopComponent.componentDeactivated();
    }

    public UndoRedo getUndoRedo() {
//        return designer.getUndoRedo();
        return jsfTopComponent.getUndoRedo();
    }

    public void setMultiViewCallback(MultiViewElementCallback multiViewElementCallback) {
//        designer.setMultiViewCallback(multiViewElementCallback);
        jsfTopComponent.setMultiViewCallback(multiViewElementCallback);
    }

    public CloseOperationState canCloseElement() {
//        return designer.canCloseElement();
        return jsfTopComponent.canCloseElement();
    }

    
    // JSF notifications >>>
    public void modelChanged() {
        jsfTopComponent.modelChanged();
    }
    
    public void modelRefreshed() {
        jsfTopComponent.modelRefreshed();
    }
    
    public void nodeChanged(Node node, Node parent, Element[] changedElements) {
        jsfTopComponent.nodeChanged(node, parent, changedElements);
    }
    
    public void nodeRemoved(Node node, Node parent) {
        jsfTopComponent.nodeRemoved(node, parent);
    }
    
    public void nodeInserted(Node node, Node parent) {
        jsfTopComponent.nodeInserted(node, parent);
    }
    
    public void updateErrors() {
        jsfTopComponent.updateErrors();
    }
    
    public void gridModeUpdated(boolean gridMode) {
        jsfTopComponent.gridModeUpdated(gridMode);
    }
    
    public void documentReplaced() {
        jsfTopComponent.documentReplaced();
    }
    
    public void showDropMatch(Element componentRootElement, Element regionElement, int dropType) {
        jsfTopComponent.showDropMatch(componentRootElement, regionElement, dropType);
    }
    
    public void clearDropMatch() {
        jsfTopComponent.clearDropMatch();
    }
    
    public void selectComponent(Element componentRootElement) {
        jsfTopComponent.selectComponent(componentRootElement);
    }
    
    public void inlineEditComponents(Element[] componentRootElements) {
        jsfTopComponent.inlineEditComponents(componentRootElements);
    }
    
    public void designContextGenerationChanged() {
        jsfTopComponent.designContextGenerationChanged();
    }
    
    public JsfForm getJsfForm() {
        return jsfTopComponent.getJsfForm();
    }
    
    public Designer getDesigner() {
        return jsfTopComponent.getDesigner();
    }
    // JSF notifications <<<
    
    
    public TopComponent getMultiViewTopComponent() {
        return jsfTopComponent.getMultiViewTopComponent();
    }
    
    public void closeMultiView() {
        jsfTopComponent.closeMultiView();
    }
    
    public boolean isSelectedElement() {
        return jsfTopComponent.isSelectedInMultiView();
    }

    public void modelLoaded() {
        jsfTopComponent.modelLoaded();
    }
    
    public void requestActive() {
        jsfTopComponent.requestActive();
    }
    
    
    private void openJsfTopComponentGroupIfNeeded() {
        if (isFirstVisibleJsfMultiViewElement()) {
            TopComponentGroup jsfTopComponentGroup = findJsfDesignerTopComponentGroup();
            if (jsfTopComponentGroup == null) {
                log("JSF TopComponentGroup not found, can not open helper windows."); // NOI18N
            } else {
                fine("Opening JSF Designer TopComponentGroup"); // NOI18N
                jsfTopComponentGroup.open();
            }
        }
    }
    
    private void closeJsfTopComponentGroupIfNeeded() {
        if (isLastVisibleJsfMultiViewElement()) {
            TopComponentGroup jsfTopComponentGroup = findJsfDesignerTopComponentGroup();
            if (jsfTopComponentGroup == null) {
                log("JSF TopComponentGroup not found, can not close helper windows."); // NOI18N
            } else {
                fine("Closing JSF Designer TopComponentGroup"); // NOI18N
                jsfTopComponentGroup.close();
            }
        }
    }
    
    private TopComponentGroup findJsfDesignerTopComponentGroup() {
        return WindowManager.getDefault().findTopComponentGroup(TOP_COMPONENT_GROUP_JSF_DESIGNER); // NOI18N
    }

    private boolean isFirstVisibleJsfMultiViewElement() {
        JsfMultiViewElement[] visibleJsfMultiViewElements = findAllVisibleJsfMultiViewElements();
        if (visibleJsfMultiViewElements.length == 0) {
            return true;
        } else if (visibleJsfMultiViewElements.length == 1 && visibleJsfMultiViewElements[0] == this) {
            return true;
        } else {
            return false;
        }
    }
    
    private boolean isLastVisibleJsfMultiViewElement() {
        JsfMultiViewElement[] visibleJsfMultiViewElements = findAllVisibleJsfMultiViewElements();
        if (visibleJsfMultiViewElements.length == 0) {
            return true;
        } else if (visibleJsfMultiViewElements.length == 1 && visibleJsfMultiViewElements[0] == this) {
            return true;
        } else {
            return false;
        }
    }
    
    
    private static JsfMultiViewElement[] findAllVisibleJsfMultiViewElements() {
        Set<JsfMultiViewElement> allVisibleJsfMultiViewElements = new HashSet<JsfMultiViewElement>();
        JsfMultiViewElement[] allJsfMultiViewElements = JsfForm.getJsfMultiViewElements();
        for (JsfMultiViewElement jsfMultiViewElement : allJsfMultiViewElements) {
            if (isVisibleJsfMultiViewElement(jsfMultiViewElement)) {
                allVisibleJsfMultiViewElements.add(jsfMultiViewElement);
            }
        }
        return allVisibleJsfMultiViewElements.toArray(new JsfMultiViewElement[allVisibleJsfMultiViewElements.size()]);
    }
    
    private static boolean isVisibleJsfMultiViewElement(JsfMultiViewElement jsfMultiViewElement) {
        TopComponent multiView = jsfMultiViewElement.getMultiViewTopComponent();
        if (multiView == null) {
            return false;
        }
        if (!multiView.isOpened()) {
            return false;
        }
        
        Mode mode = WindowManager.getDefault().findMode(multiView);
        if (mode == null) {
            return false;
        }
        if (mode.getSelectedTopComponent() != multiView) {
            return false;
        }
        
        return jsfMultiViewElement.isSelectedElement();
    }
    
    private static void log(String message) {
        Logger logger = getLogger();
        logger.log(Level.INFO, message);
    }
    
    private static void fine(String message) {
        Logger logger = getLogger();
        logger.fine(message);
    }
    
    private static Logger getLogger() {
        return Logger.getLogger(JsfMultiViewElement.class.getName());
    }
}
