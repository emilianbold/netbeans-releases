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

import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.visualweb.insync.beans.Bean;
import org.netbeans.modules.visualweb.insync.faces.FacesBean;
import org.netbeans.modules.visualweb.insync.faces.FacesBean.UsageInfo;
import org.netbeans.modules.visualweb.insync.live.FacesDesignBean;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.impl.BasicDisplayAction;

/**
 * Action which adds/removes binding attribute.
 * 
 * @author Sandip Chitale
 */
public class AddRemoveBindingAttributeAction extends AbstractDisplayActionAction {

    public AddRemoveBindingAttributeAction() {
    }
    
    @Override
    protected String getDefaultDisplayName() {
        return NbBundle.getMessage(AddRemoveBindingAttributeAction.class, "LBL_AddRemoveBindingAttributeActionName");
    }

    private static DisplayAction[] EMPTY_displayActions = new DisplayAction[0];
    @Override
    protected DisplayAction[] getDisplayActions(DesignBean[] designBeans) {
        if (designBeans.length == 1) {
            DesignBean designBean = designBeans.length == 0 ? null : designBeans[0];
            if (isBindingAttributeCapable(designBean)) {
                if (designBean != null && hasBindingAttribute(designBean)) {
                    return new DisplayAction[] {new RemoveBindingAttributeAction(designBean)};
                } else {
                    return new DisplayAction[] {new AddBindingAttributeAction(designBean)};
                }
            }
        }
        return EMPTY_displayActions;
    }
    
    static class AddBindingAttributeAction extends BasicDisplayAction {
        private DesignBean designBean;
        
        public AddBindingAttributeAction(DesignBean designBean) {
            super(NbBundle.getMessage(AddRemoveBindingAttributeAction.class, "LBL_AddBindingAttributeActionName"));
            this.designBean = designBean;
        }
        
        public Result invoke() {            
            if (designBean instanceof FacesDesignBean) {
                FacesDesignBean facesDesignBean = (FacesDesignBean) designBean;
                FacesModel facesModel = ((LiveUnit) designBean.getDesignContext()).getModel();
                UndoEvent undo = null;
                try {
                    undo = facesModel.writeLock(NbBundle.getMessage(AddRemoveBindingAttributeAction.class, "LBL_RemoveBindingAttributeActionName"));
                    facesDesignBean.addBinding();
                } finally {
                    facesModel.writeUnlock(undo);
                }
            }
            return Result.SUCCESS;
        }
    }

    static class RemoveBindingAttributeAction extends BasicDisplayAction {
        private DesignBean designBean;
        
        public RemoveBindingAttributeAction(DesignBean designBean) {
            super(NbBundle.getMessage(AddRemoveBindingAttributeAction.class, "LBL_RemoveBindingAttributeActionName"));
            this.designBean = designBean;
        }
        
        public Result invoke() {
            if (designBean instanceof FacesDesignBean) {
                FacesDesignBean facesDesignBean = (FacesDesignBean) designBean;
                UsageInfo usageInfo = facesDesignBean.getUsageInfo();
                NotifyDescriptor notifyDescriptor;
                switch (usageInfo.getUsageStatus()) {
                case USED :
                    notifyDescriptor = new NotifyDescriptor.Message(
                            NbBundle.getMessage(AddRemoveBindingAttributeAction.class, "ERROR_BINDING_IN_USE")); // NOI18N
                    DialogDisplayer.getDefault().notify(notifyDescriptor);
                    return Result.FAILURE;
                case INIT_USE_ONLY :
                    notifyDescriptor = new NotifyDescriptor.Confirmation(
                                            NbBundle.getMessage(AddRemoveBindingAttributeAction.class,
                                                                "WARNING_BINDING_IN_USE",  // NOI18N
                                                                usageInfo.getInitializedProperties().toString())                                     
                                            ,NbBundle.getMessage(AddRemoveBindingAttributeAction.class,
                                                                 "TITLE_REMOVE_BINDING_ATTRIBUTE") // NOI18N
                                            ,NotifyDescriptor.OK_CANCEL_OPTION);
                    if (DialogDisplayer.getDefault().notify(notifyDescriptor) == NotifyDescriptor.OK_OPTION) {
                        // fall through
                    } else {
                        return Result.FAILURE;
                    }
                case NOT_USED :
                    break;
                }
                FacesModel facesModel = ((LiveUnit) designBean.getDesignContext()).getModel();
                UndoEvent undo = null;
                try {
                    undo = facesModel.writeLock(NbBundle.getMessage(AddRemoveBindingAttributeAction.class, "LBL_RemoveBindingAttributeActionName"));
                    facesDesignBean.removeBinding();
                } finally {
                    facesModel.writeUnlock(undo);
                }
            }
            return Result.SUCCESS;
        }
    }
    
    private static boolean hasBindingAttribute(DesignBean designBean) {
        if (designBean instanceof FacesDesignBean) {
            FacesDesignBean facesDesignBean = (FacesDesignBean) designBean;
            Bean bean = facesDesignBean.getBean();
            if (bean instanceof FacesBean) {
                FacesBean facesBean = (FacesBean) bean;
                return facesBean.getAttr(facesBean.BINDING_ATTR) != null;
            }
        }
        return false;
    }
    
    private static boolean isBindingAttributeCapable(DesignBean designBean) {
        if (designBean instanceof FacesDesignBean) {
            FacesDesignBean facesDesignBean = (FacesDesignBean) designBean;
            if (facesDesignBean.getBean() instanceof FacesBean) {
                return true;
            }
        }
        return false;
    }
}
