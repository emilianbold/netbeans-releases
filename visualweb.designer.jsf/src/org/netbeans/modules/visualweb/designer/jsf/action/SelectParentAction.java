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
import com.sun.rave.designtime.markup.MarkupDesignBean;
import org.netbeans.modules.visualweb.designer.jsf.JsfSupportUtilities;
import org.netbeans.modules.visualweb.insync.Util;
import org.netbeans.modules.visualweb.spi.designtime.idebridge.action.AbstractDesignBeanAction;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;

/**
 * Action selecting the parent bean.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (old functionality implementation -> isEnabled, performAction impl)
 */
public class SelectParentAction  extends AbstractDesignBeanAction {

    /** Creates a new instance of SelectParentAction */
    public SelectParentAction() {
    }

    protected String getDisplayName(DesignBean[] designBeans) {
        return NbBundle.getMessage(SelectParentAction.class, "LBL_SelectParentAction");
    }

    protected String getIconBase(com.sun.rave.designtime.DesignBean[] designBeans) {
        return null;
    }

    protected boolean isEnabled(DesignBean[] designBeans) {
        if (designBeans.length == 0) {
            return false;
        }

        DesignBean designBean = designBeans[0];
        return canSelectParent(designBean);
    }

    protected void performAction(DesignBean[] designBeans) {
        if (designBeans.length == 0) {
            return;
        }

        DesignBean designBean = designBeans[0];
        selectParent(designBean);
    }

    private static boolean canSelectParent(DesignBean designBean) {
        if (designBean == null) {
            return false;
        }
        
        DesignBean parent = findSelectableParent(designBean);
        if (parent == null) {
            return false;
        }
        
        if (parent == parent.getDesignContext().getRootContainer()) {
            return false;
        }
        
        if (Util.isSpecialBean(parent)) {
            return false;
        }
        
        return true;
    }

    private static void selectParent(DesignBean designBean) {
        if (designBean == null) {
            return;
        }

        DesignBean parent = findSelectableParent(designBean);
        if (parent == null) {
            return;
        }

        Element componentRootElement = JsfSupportUtilities.getComponentRootElementForDesignBean(parent);
        if (componentRootElement == null) {
            return;
        }
        
        Designer designer = JsfSupportUtilities.findDesignerForDesignContext(designBean.getDesignContext());
        if (designer == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                    new NullPointerException("Can't find designer for design context=" // NOI18N
                    + designBean.getDesignContext()));
            return;
        }
        designer.selectComponent(componentRootElement);
    }
    
    // XXX #94766 There are component root elements not mapped to boxes (?!)
    private static DesignBean findSelectableParent(DesignBean designBean) {
        if (designBean == null) {
            return null;
        }
        
        Designer designer = JsfSupportUtilities.findDesignerForDesignContext(designBean.getDesignContext());
        if (designer == null) {
            // XXX
            return null;
        }
        
        DesignBean parent = designBean.getBeanParent();
        while (parent != null) {
            Element componentRootElement = JsfSupportUtilities.getComponentRootElementForDesignBean(parent);
            if (componentRootElement != null) {
                if (designer.findBoxForComponentRootElement(componentRootElement) != null) {
                    return parent;
                }
            }
            
            parent = parent.getBeanParent();
        }
        return null;
    }
    
}
