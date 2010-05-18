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


package org.netbeans.modules.visualweb.insync.action;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesDesignContext;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.spi.designtime.idebridge.action.AbstractDesignBeanAction;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.openide.ErrorManager;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 * Action which sets/unsets initial focus.
 * XXX Mostly copies the old code from designer.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (old functionality implementation -> performAction impl)
 */
public class InitialFocusAction extends AbstractDesignBeanAction {

    /** Creates a new instance of InitialFocusAction */
    public InitialFocusAction() {
    }

    protected String getDisplayName(DesignBean[] designBeans) {
        DesignBean designBean = designBeans.length == 0 ? null : designBeans[0];

        if (designBean != null && hasFocus(designBean)) {
            return NbBundle.getMessage(InitialFocusAction.class, "LBL_InitialFocusAction_Clear");
        } else {
            return NbBundle.getMessage(InitialFocusAction.class, "LBL_InitialFocusAction_Set");
        }
    }

    protected String getIconBase(DesignBean[] designBeans) {
        return null;
    }

    protected boolean isEnabled(DesignBean[] designBeans) {
        if (designBeans.length == 0) {
            return false;
        }

        DesignBean designBean = designBeans[0];
        return designBean.getInstance() instanceof javax.faces.component.EditableValueHolder
                && !hasTableParent(designBean)
                && getFocusDesignProperty(designBean) != null;
    }

    private static boolean hasTableParent(DesignBean bean) {
        DesignBean parent = bean.getBeanParent();

        while (parent != null) {
            Object instance = parent.getInstance();

            if (instance instanceof javax.faces.component.UIData
            || instance instanceof com.sun.rave.web.ui.component.TableRowGroup // Braveheart
            || instance instanceof com.sun.webui.jsf.component.TableRowGroup) { // Woodstock
                return true;
            }

            parent = parent.getBeanParent();
        }

        return false;
    }

    protected void performAction(DesignBean[] designBeans) {
        if (designBeans.length == 0) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalArgumentException("The array shouldn't be empty."));
            return;
        }

        DesignBean designBean = designBeans[0];

        if (hasFocus(designBean)) {
            clearFocus(designBean);
        } else {
            setFocus(designBean);
        }
    }


    private static boolean hasFocus(DesignBean designBean) {
        DesignProperty focusProperty = getFocusDesignProperty(designBean);
        if (focusProperty == null) {
            return false;
        }

        Object value = focusProperty.getValue();
        if (value == null) {
            return false;
        }

        return value.equals(getDesignBeanClientId(designBean));
    }

    private static void clearFocus(DesignBean designBean) {
        DesignProperty focusProperty = getFocusDesignProperty(designBean);
        if (focusProperty == null) {
            return;
        }

        focusProperty.setValue(focusProperty.getUnsetValue());
    }
    
    private static void setFocus(DesignBean designBean) {
        DesignProperty focusProperty = getFocusDesignProperty(designBean);
        if (focusProperty == null) {
            return;
        }
        
        focusProperty.setValue(getDesignBeanClientId(designBean));
    }
    
    private static DesignProperty getFocusDesignProperty(DesignBean designBean) {
        DesignBean parent = designBean;
        while (parent != null) {
            Object instance = parent.getInstance();
            // XXX Why checking the webui body and not body in general?
            if (instance != null && isInstanceOfBody(instance.getClass())) {
                break;
            }

            parent = parent.getBeanParent();
        }
        
        return parent == null ? null : parent.getProperty("focus"); // NOI18N
    }
    
    private static String getDesignBeanClientId(DesignBean designBean) {
        Object instance = designBean.getInstance();

        if (!(instance instanceof UIComponent)) {
            return null;
        }

        DesignContext designContext = designBean.getDesignContext();
        FacesContext facesContext = ((FacesDesignContext)designContext).getFacesContext();
        UIComponent uiComponent = (UIComponent)instance;
        return uiComponent.getClientId(facesContext);
    }
    
    private static boolean isInstanceOfBody(Class c) {
        if (c == null) {
            return false;
        }
        
        // Insync depends on the webui and woodstock modules, so it is OK to use the compile time
        // dependency on them
    	return com.sun.rave.web.ui.component.Body.class.isAssignableFrom(c) ||
               com.sun.webui.jsf.component.Body.class.isAssignableFrom(c);
    }    
}
