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


package org.netbeans.modules.visualweb.designer.jsf.action;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.api.designer.Designer.Box;
import org.netbeans.modules.visualweb.designer.jsf.JsfForm;
import org.netbeans.modules.visualweb.designer.jsf.JsfSupportUtilities;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;

/**
 * Action sending the comp to the back.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (old functionality implementation -> performAction impl)
 */
public class SendToBackAction  extends AbstractComponentAction {

    /** Creates a new instance of SendToBackAction */
    public SendToBackAction() {
    }

    protected String getDisplayName(Element[] componentRootElements) {
        return NbBundle.getMessage(SendToBackAction.class, "LBL_SendToBackAction");
    }

    protected String getIconBase(Element[] componentRootElements) {
        return null;
    }

    protected boolean isEnabled(Element[] componentRootElements) {
        if (componentRootElements.length == 0) {
            return false;
        }

        Element componentRootElement = componentRootElements[0];
//        WebForm webform = WebForm.findWebFormForElement(componentRootElement);
//        if (webform == null) {
//            return false;
//        }
        JsfForm jsfForm = JsfForm.findJsfForm(componentRootElement);
        if (jsfForm == null) {
            return false;
        }

//        return !LiveUnit.isTrayBean(designBean) && FacesSupport.isFormBean(webform, designBean.getBeanParent());
//        return !WebForm.getDomProviderService().isTrayComponent(componentRootElement)
//                && webform.isFormComponent(WebForm.getDomProviderService().getParentComponent(componentRootElement));
        return !JsfSupportUtilities.isTrayComponent(componentRootElement)
                && jsfForm.isFormComponent(JsfSupportUtilities.getParentComponent(componentRootElement));
    }

    protected void performAction(Element[] componentRootElements) {
        if (componentRootElements.length == 0) {
            return;
        }

        Element componentRootElement = componentRootElements[0];
//        WebForm webform = WebForm.findWebFormForElement(componentRootElement);
//        if (webform == null) {
//            return;
//        }
        JsfForm jsfForm = JsfForm.findJsfForm(componentRootElement);
        if (jsfForm == null) {
            return;
        }

//        sendToBack(webform);
        sendToBack(jsfForm);
    }


//    private static void sendToBack(WebForm webform) {
    private static void sendToBack(JsfForm jsfForm) {
        Designer[] designers = JsfForm.findDesigners(jsfForm);
        Designer designer = designers.length > 0 ? designers[0] : null;
        if (designer == null) {
            return;
        }
        
//        SelectionManager sm = webform.getSelection();
////        ModelViewMapper mapper = webform.getMapper();
//
//        if (!sm.isSelectionEmpty()) {
//            List<CssBox> list = new ArrayList<CssBox>(sm.getNumSelected());
////            Iterator it = sm.iterator();
////
////            while (it.hasNext()) {
////                DesignBean bean = (DesignBean)it.next();
//            for (Element componentRootElement : sm.getSelectedComponentRootElements()) {
////                DesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
////                CssBox box = mapper.findBox(bean);
//                CssBox box = ModelViewMapper.findBoxForComponentRootElement(webform.getPane().getPageBox(), componentRootElement);
//
//                if (box != null) {
//                    list.add(box);
//                }
//            }
//
////            GridHandler.getInstance().back(webform, list);
////            webform.getGridHandler().back(webform, list);
////            webform.getGridHandler().back(webform, list.toArray(new CssBox[list.size()]));
//            webform.getDomDocument().backComponents(list.toArray(new CssBox[list.size()]));
//        }
        Element[] selectedComponents = designer.getSelectedComponents();
        if (selectedComponents.length > 0) {
            List<Box> selectedBoxes = new ArrayList<Box>();
            for (Element selectedComponent : selectedComponents) {
                Box box = designer.findBoxForComponentRootElement(selectedComponent);
                if (box != null) {
                    selectedBoxes.add(box);
                }
            }
            jsfForm.getDomDocumentImpl().backComponents(selectedBoxes.toArray(new Box[selectedBoxes.size()]));
        }
    }

}
