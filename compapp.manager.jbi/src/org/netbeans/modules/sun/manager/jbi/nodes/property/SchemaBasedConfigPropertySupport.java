/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.sun.manager.jbi.nodes.property;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
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
    public T getValue() {
        return (T) attr.getValue();
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
                        d = promptForRestart("MSG_NEEDS_APPLICATION_RESTART");
                        if (d.getDoNotShowAgain()) {
                            promptForApplicationRestart = false;
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
}
