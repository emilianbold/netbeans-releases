/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.wsdl.ui.wsdl.util;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.ui.view.ElementOrType;
import org.netbeans.modules.xml.wsdl.ui.view.ElementOrTypeChooserPanel;
import org.netbeans.modules.xml.wsdl.ui.wizard.PartnerLinkTypeGenerator;
import org.netbeans.modules.xml.wsdl.ui.wizard.common.PortTypeGenerator;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author jalmero
 */
public class BindingUtils {

    private static GlobalType mGlobalType = null;
    private static GlobalElement mGlobalElement = null;
    private static Map<String, String> mNamespaceToPrefixMap = null;
    private static String mPartTypeStr = "";
    private static boolean mStatus = true;
    private static final QName mPartnerLinkTypeQName = BPELQName.PARTNER_LINK_TYPE.getQName();
    private static final QName mPartnerLinkTypeRoleQName = BPELQName.ROLE.getQName();
    public static final String PROJECT_INSTANCE = "BC_PROJECT_INSTANCE";
    //This constant can be set in template wizard for retrieving either INBOUND or OUTBOUND templates.
    public static final String BINDING_EDITOR_MODE = "BINDING_EDITOR_MODE";//NOI18N
    //Enumeration used to set BINDING_EDITOR_MODE in template wizard. 
    public enum Type {
        INBOUND,
        OUTBOUND,
        INBOUND_OUTBOUND
    }
    
    public static boolean launchElementOrType(Project project, 
            WSDLModel wsdlModel, SchemaComponent schemaComponent) {
        mStatus = true;

        if (project != null) {   
            // fill in the namespace->prefix map
            mNamespaceToPrefixMap = new HashMap<String, String>();
            Map<String, String> map = ((AbstractDocumentComponent) wsdlModel.
                    getDefinitions()).getPrefixes();
            for (String prefix : map.keySet()) {
                mNamespaceToPrefixMap.put(map.get(prefix), prefix);
            }

            final ElementOrTypeChooserPanel panel =
                    new ElementOrTypeChooserPanel(project, mNamespaceToPrefixMap,
                    wsdlModel, schemaComponent);
            final DialogDescriptor descriptor = new DialogDescriptor(panel,
                    NbBundle.getMessage(BindingUtils.class,
                    "BindingComponentUtils.Dialog.title"), true, null);
            descriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.xml.wsdl.ui.api.property.ElementOrTypePropertyEditor"));

            final PropertyChangeListener pcl = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getSource() == panel && evt.getPropertyName().
                            equals(ElementOrTypeChooserPanel.PROP_ACTION_APPLY)) {
                        descriptor.setValid(((Boolean) evt.getNewValue()).booleanValue());
                    }
                }
            };
            panel.addPropertyChangeListener(pcl);

            // dialog's action listener
            ActionListener al = new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource().equals(DialogDescriptor.OK_OPTION) ||
                            evt.getSource().equals(DialogDescriptor.CANCEL_OPTION) ||
                            evt.getSource().equals(DialogDescriptor.CLOSED_OPTION)) {
                        panel.removePropertyChangeListener(pcl);
                    }

                    if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                        panel.apply();
                        SchemaComponent comp1 = panel.getSelectedSchemaComponent();
                        ElementOrType elementOrType = panel.getSelectedComponent();
                        if (comp1 == null) {
                            mGlobalType = null;
                            mGlobalElement = null;
                            return;
                        }                         
                        
                        if (comp1 instanceof GlobalType) {
                            mPartTypeStr = elementOrType.toString();
                            mGlobalElement = null;
                            mGlobalType = (GlobalType) comp1;
                        } else if (comp1 instanceof GlobalElement) {
                            mPartTypeStr = elementOrType.toString();
                            mGlobalType = null;
                            mGlobalElement = (GlobalElement) comp1;
                        }
                    } else {
                        mStatus = false;
                    }
                }
            };
            descriptor.setButtonListener(al);
            descriptor.setValid(false);
            Dialog dialog = DialogDisplayer.getDefault().
                    createDialog(descriptor);
            dialog.getAccessibleContext().setAccessibleDescription(
                    descriptor.getTitle());
            dialog.setVisible(true);
            dialog.toFront();
        }
        return mStatus;
    }

    /**
     * Return the selected schema component
     * @return
     */
    public static GlobalElement getSchemaComponent() {
        return mGlobalElement;
    }

    /**
     * Returnt the global type selected
     * @return
     */
    public static GlobalType getElementOrType() {
        return mGlobalType;
    }
    
    public static String getPrefixNameSpace() {
        return mPartTypeStr;
    }

    public static QName getPartnerLinkTypeQName() {
        return mPartnerLinkTypeQName;
    }
    
    public static QName getPartnerLinkTypeRoleQName() {
        return mPartnerLinkTypeRoleQName;
    }

    public static String getPartnerLinkTypeComment() {
        return NbBundle.getMessage(PartnerLinkTypeGenerator.class,
                                "LBL_partnerLinkType_comment");
    }
}
