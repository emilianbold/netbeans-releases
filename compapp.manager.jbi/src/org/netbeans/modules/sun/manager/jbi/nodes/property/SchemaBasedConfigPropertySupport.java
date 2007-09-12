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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.sun.manager.jbi.nodes.property;

import java.util.List;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import org.netbeans.modules.sun.manager.jbi.management.AdministrationService;
import org.netbeans.modules.sun.manager.jbi.nodes.JBIComponentNode;
import org.netbeans.modules.sun.manager.jbi.util.MyMBeanAttributeInfo;
import org.netbeans.modules.sun.manager.jbi.util.DoNotShowAgainMessage;
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

    private Attribute attr;
    private MBeanAttributeInfo info;
    private JBIComponentNode componentNode;

    SchemaBasedConfigPropertySupport(
            JBIComponentNode componentNode, 
            Class<T> type, 
            Attribute attr, 
            MBeanAttributeInfo info) {

        super(attr.getName(), type, info.getName(), info.getDescription());

        this.attr = attr;
        this.info = info;
        this.componentNode = componentNode;
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

    public void setValue(T val) {
        String name = getName();
        try {
            if (validate(val)) {
                attr = componentNode.setSheetProperty(name, val);

                if (info instanceof MyMBeanAttributeInfo) {
                    
                    MyMBeanAttributeInfo myInfo = (MyMBeanAttributeInfo)info;
                    
                    DoNotShowAgainMessage d;
                            
                    if (myInfo.isApplicationRestartRequired() && 
                            promptForApplicationRestart) {
                        String compName = componentNode.getName();
                        AdministrationService adminService = 
                                componentNode.getAppserverJBIMgmtController().
                                getJBIAdministrationService();
                        List<String> saNames = 
                                adminService.getServiceAssemblyNames(compName);
                        if (saNames.size() > 0) {
                            d = promptForRestart("MSG_NEEDS_APPLICATION_RESTART", 
                                    saNames.toString());
                            if (d.getDoNotShowAgain()) {
                                promptForApplicationRestart = false;
                            }
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
            }
        } catch (Exception ex) {
            NotifyDescriptor d = new NotifyDescriptor.Message(
                    NbBundle.getMessage(getClass(), 
                    "MSG_SET_COMPONENT_CONFIG_PROPERTY_ERROR", 
                    ex.getMessage()), 
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }

    protected boolean validate(T value) {
        return true;
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
