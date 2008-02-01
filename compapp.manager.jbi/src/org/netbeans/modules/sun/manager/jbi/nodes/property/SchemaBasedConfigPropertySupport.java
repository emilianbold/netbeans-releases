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

package org.netbeans.modules.sun.manager.jbi.nodes.property;

import com.sun.esb.management.common.ManagementRemoteException;
import java.util.List;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.swing.SwingUtilities;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentConfigurationMBeanAttributeInfo;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.RuntimeManagementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.nodes.AppserverJBIMgmtNode;
import org.netbeans.modules.sun.manager.jbi.util.DoNotShowAgainMessage;
import org.netbeans.modules.sun.manager.jbi.util.StackTraceUtil;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/**
 * Schema-based property support for JBI Component configuration.
 * 
 * @author jqian
 */
class SchemaBasedConfigPropertySupport<T> 
        extends PropertySupport.ReadWrite<T> {
    
    // These are not persistent across sessions.
    private static boolean promptForApplicationRestart = true;
    private static boolean promptForComponentRestart = true;
    private static boolean promptForServerRestart = true;

    protected Attribute attr;
    protected MBeanAttributeInfo info;
    protected AppserverJBIMgmtNode componentNode;
    protected PropertySheetOwner propertySheetOwner;

    SchemaBasedConfigPropertySupport(
            PropertySheetOwner propertySheetOwner, 
            Class<T> type, 
            Attribute attr, 
            MBeanAttributeInfo info) {

        super(attr.getName(), type, info.getName(), 
                Utils.getTooltip(info.getDescription()));
          
        // Doesn't work yet. #124256
//        // Use non-HTML version in the property sheet's description area.
//        setValue("nodeDescription", info.getDescription()); // NOI18N 
        
        this.attr = attr;
        this.info = info;
        
        this.propertySheetOwner = propertySheetOwner;
        
        if (propertySheetOwner instanceof AppserverJBIMgmtNode) {
            this.componentNode = (AppserverJBIMgmtNode) propertySheetOwner;
        }
    }

    @SuppressWarnings(value = "unchecked")
    public T getValue() /*throws IllegalAccessException*/ {
        if (info.isReadable()) {
            return (T) attr.getValue();
        } else {   
            // Mostly used for password handling
            // throw new IllegalAccessException("Cannod read from WriteOnly property"); // NOI18N 
            if (info.getType().equals("java.lang.String")) { // NOI18N
                return (T) "********"; // NOI18N
            } else {
                return null;
            }
        }
    }

    public void setValue(T newValue) {
        /*
        // There is a problem for password field because we don't know 
        // the actual old value.
        if (attr.getValue().equals(newValue)) {
            return;
        }*/
        
        if (StackTraceUtil.isCalledBy(
                "org.openide.explorer.propertysheet.PropertyDialogManager", // NOI18N
                "cancelValue")) { // NOI18N
            return;
        }           
        
        try {
            if (validate(newValue)) {                  
                attr = propertySheetOwner.setSheetProperty(getName(), newValue);
                
                if (componentNode != null &&
                        info instanceof JBIComponentConfigurationMBeanAttributeInfo) {
                    checkForPromptToRestart();
                }
            }
        } catch (Exception ex) {
            NotifyDescriptor d = new NotifyDescriptor.Message(
                    NbBundle.getMessage(getClass(), 
                    "MSG_SET_COMPONENT_CONFIG_PROPERTY_ERROR", // NOI18N
                    ex.getMessage()), 
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }

    protected boolean validate(T newValue) {
        return true;
    }
    
    protected void checkForPromptToRestart() {
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                JBIComponentConfigurationMBeanAttributeInfo myInfo =
                        (JBIComponentConfigurationMBeanAttributeInfo)info;

                DoNotShowAgainMessage d;

                if (myInfo.isApplicationRestartRequired() && 
                        promptForApplicationRestart) {
                    String compName = componentNode.getName();

                    try {
                        RuntimeManagementServiceWrapper adminService = 
                                componentNode.getAppserverJBIMgmtController().
                                getRuntimeManagementServiceWrapper();
                        List<String> saNames = 
                                adminService.getServiceAssemblyNames(
                                compName, AppserverJBIMgmtController.SERVER_TARGET);
                        if (saNames.size() > 0) {
                            d = promptForRestart("MSG_NEEDS_APPLICATION_RESTART", 
                                    saNames.toString());
                            if (d.getDoNotShowAgain()) {
                                promptForApplicationRestart = false;
                            }
                        }
                    } catch (ManagementRemoteException e) {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(
                                e.getMessage(),
                                NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                }

                if (myInfo.isComponentRestartRequired() &&
                        promptForComponentRestart) {
                    d = promptForRestart("MSG_NEEDS_COMPONENT_RESTART");
                    if (d.getDoNotShowAgain()) {
                        promptForComponentRestart = false;
                    }
                }

                if (myInfo.isServerRestartRequired() &&
                        promptForServerRestart) {
                    d = promptForRestart("MSG_NEEDS_SERVER_RESTART");
                    if (d.getDoNotShowAgain()) {
                        promptForServerRestart = false;
                    }
                }         
            }
        });
    }
    
    private DoNotShowAgainMessage promptForRestart(String msgBundleName) {
        DoNotShowAgainMessage d = new DoNotShowAgainMessage(
                NbBundle.getMessage(getClass(), msgBundleName),
                NotifyDescriptor.INFORMATION_MESSAGE);        
        DialogDisplayer.getDefault().notify(d);
        return d;
    }
    
    private DoNotShowAgainMessage promptForRestart(String msgBundleName, String param1) {
        DoNotShowAgainMessage d = new DoNotShowAgainMessage(
                NbBundle.getMessage(getClass(), msgBundleName, param1),
                NotifyDescriptor.INFORMATION_MESSAGE);        
        DialogDisplayer.getDefault().notify(d);
        return d;
    }
}
