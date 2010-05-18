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

import org.netbeans.modules.visualweb.api.designer.Designer;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import org.netbeans.modules.visualweb.designer.jsf.JsfSupportUtilities;
import org.netbeans.modules.visualweb.spi.designtime.idebridge.action.AbstractDesignBeanAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.visualweb.designer.jsf.JsfForm;
import org.netbeans.modules.visualweb.designer.jsf.ui.JsfMultiViewElement;
import org.openide.ErrorManager;
import org.openide.awt.Actions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;

/**
 * Action allowing inline editing.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (old functionality implementation -> performActionAt impl)
 */
public class InlineEditAction extends AbstractDesignBeanAction {

    /** Creates a new instance of InlineEditAction. */
    public InlineEditAction() {
    }

    protected String getDisplayName(DesignBean[] designBeans) {
        if (designBeans != null && designBeans.length == 1) {
            String[] propertyNames = getInlineEditablePropertyNames(designBeans[0]);
            if (propertyNames.length == 1) {
                return NbBundle.getMessage(
                        InlineEditAction.class,
                        "LBL_InlineEditActionProperty",
//                        processDisplayName(propertyNames[0]));
                        findPropertyDisplayName(designBeans[0], propertyNames[0]));
            }
        }

        return NbBundle.getMessage(InlineEditAction.class, "LBL_InlineEditAction");
    }

    protected String getIconBase(DesignBean[] designBeans) {
        return null;
    }

    protected boolean isEnabled(DesignBean[] designBeans) {
        if (designBeans.length == 0) {
            return false;
        }
        return getInlineEditablePropertyNames(designBeans[0]).length > 0;
    }

    protected void performAction(DesignBean[] designBeans) {
        // XXX Strange impl of the Actions.SubMenu(action, model, isPopup). If the model provides one item,
        // it doesn't call the performAt(0), but this method.
        new InlineEditMenuModel(designBeans).performActionAt(0);
    }

    protected JMenuItem getMenuPresenter(Action contextAwareAction, Lookup.Result result) {
        return new Actions.SubMenu(contextAwareAction, new InlineEditMenuModel(getDesignBeans(result)), false);
    }

    protected JMenuItem getPopupPresenter(Action contextAwareAction, Lookup.Result result) {
        return new Actions.SubMenu(contextAwareAction, new InlineEditMenuModel(getDesignBeans(result)), true);
    }

    private static String[] getInlineEditablePropertyNames(DesignBean designBean) {
        if (!(designBean instanceof MarkupDesignBean)) {
            return new String[0];
        }

//        return InlineEditor.getEditablePropertyNames(designBean);
//        return WebForm.getDomProviderService().getEditablePropertyNames(
//                WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean)designBean));
        return JsfSupportUtilities.getEditableProperties(designBean);
    }

//    // XXX Copied from DesignActions.
//    private static String processDisplayName(String displayName) {
//        // XXX Copied from DesignerActions.
//        // PENDING: If I look up the DesignProperty (to get its value and
//        // check valuebinding), then I might as well call its getDisplayName
//        // method too.
//        // Capitalize the property names such that we get proper camel casing
//        if (displayName.length() > 1 && Character.isLowerCase(displayName.charAt(0))) {
//            displayName = Character.toUpperCase(displayName.charAt(0)) +
//                    displayName.substring(1);
//        }
//        return displayName;
//    }
    private static String findPropertyDisplayName(DesignBean designBean, String propertyName) {
        if (designBean == null) {
            return propertyName;
        }
        DesignProperty designProperty = designBean.getProperty(propertyName);
        if (designProperty == null) {
            return propertyName;
        }
        return designProperty.getPropertyDescriptor().getDisplayName();
    }
    

    /** Implementation of the actions submenu model. */
    private static class InlineEditMenuModel implements Actions.SubMenuModel {

        private final DesignBean designBean;
        private final String[] propertyNames;
        
        public InlineEditMenuModel(DesignBean[] designBeans) {
            this.designBean = designBeans.length == 0 ? null : designBeans[0];
            this.propertyNames = getInlineEditablePropertyNames(designBean);
        }
        
        
        public int getCount() {
            return propertyNames.length;
        }

        public String getLabel(int i) {
            String displayName = propertyNames[i];
            
//            return processDisplayName(displayName);
            return findPropertyDisplayName(designBean, displayName);
        }

        public HelpCtx getHelpCtx(int i) {
            // XXX Implement?
            return null;
        }

        public void performActionAt(int i) {
            if (designBean == null) {
                return;
            }
            
            // XXX Is it only the first in the selection?
            if (!(designBean instanceof MarkupDesignBean)) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                        new IllegalStateException("Bean should be MarkupDesignBean bean=" + designBean)); // NOI18N
                return;
            }
            
            MarkupDesignBean bean = (MarkupDesignBean)designBean;
            
//            WebForm webform = WebForm.findWebFormForDesignContext(bean.getDesignContext());
//            
//            if (webform == null) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
//                        new NullPointerException("Can't find webform for design context=" // NOI18N
//                        + bean.getDesignContext()));
//                return;
//            }
            Designer designer = JsfSupportUtilities.findDesignerForDesignContext(bean.getDesignContext());
            if (designer == null) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                        new NullPointerException("Can't find designer for design context=" // NOI18N
                        + bean.getDesignContext()));
                return;
            }

            String propertyName = propertyNames[i];
            
//            if (webform.getManager().isInlineEditing()
////            && webform.getManager().getInlineEditor().isEditing(bean, propertyName)) {
//            && webform.getManager().getInlineEditor().isEditing(
//                    WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean(bean), propertyName)) {
//                return;
//            } else {
//                webform.getManager().finishInlineEditing(false);
//            }
//
////            ModelViewMapper mapper = webform.getMapper();
////            CssBox box = mapper.findBox(bean);
//            Element componentRootElement = WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean(bean);
//            CssBox box = ModelViewMapper.findBoxForComponentRootElement(webform.getPane().getPageBox(), componentRootElement);
//            webform.getTopComponent().requestActive();
//            webform.getManager().startInlineEditing(componentRootElement, propertyName, box, true, true, null, false);
            JsfMultiViewElement jsfMultiViewElement = JsfForm.findJsfMultiViewElementForDesigner(designer);
            if (jsfMultiViewElement != null) {
                jsfMultiViewElement.requestActive();
            }
            Element componentRootElement = JsfSupportUtilities.getComponentRootElementForDesignBean(bean);
            designer.startInlineEditing(componentRootElement, propertyName);
         }

        public void addChangeListener(ChangeListener changeListener) {
            // dummy, this model is not mutable.
        }

        public void removeChangeListener(ChangeListener changeListener) {
            // dummy, this model is not mutable.
        }
        
    } // End of InlineEditMenuModel.
}
