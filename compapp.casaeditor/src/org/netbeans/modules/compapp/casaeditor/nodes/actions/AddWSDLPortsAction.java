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

package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;

import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.CasaDataNode;
import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.compapp.casaeditor.CasaDataEditorSupport;
import org.netbeans.modules.compapp.projects.jbi.api.JbiBindingInfo;
import org.netbeans.modules.compapp.projects.jbi.api.JbiDefaultComponentInfo;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.ExtensibilityElementTemplateFactory;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplateGroup;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;


/**
 *
 * @author jsandusky
 */
public class AddWSDLPortsAction extends NodeAction {

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return NbBundle.getMessage(LoadWSDLPortsAction.class, "LBL_AddWSDLPortsAction_Name"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public Action getAction() {
        return this;
    }

    /*
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof CanvasNodeProxyContext) {
            CanvasNodeProxyContext context = (CanvasNodeProxyContext) e.getSource();
            performAction(context.getLocalLocation());
        } else {
            performAction(new Point(-1, -1));
        }
    }
    */

    private Map<String, LocalizedTemplateGroup> getWsdlTemplates() {
        ExtensibilityElementTemplateFactory factory = ExtensibilityElementTemplateFactory.getDefault();
        Collection<TemplateGroup> groups = factory.getExtensibilityElementTemplateGroups();
        Vector<LocalizedTemplateGroup> protocols = new Vector<LocalizedTemplateGroup>();
        LocalizedTemplateGroup ltg = null;
        Map<String, LocalizedTemplateGroup> temps = new HashMap<String, LocalizedTemplateGroup>();
        for (TemplateGroup group : groups) {
            ltg = factory.getLocalizedTemplateGroup(group);
            protocols.add(ltg);
            temps.put(ltg.getName(), ltg);
        }

        return temps;
    }

    public void performAction(Node[] activatedNodes) { // final Point location) {

        if (activatedNodes.length < 1) {
            return;
        }
        CasaWrapperModel cmodel = null;
        if (activatedNodes[0] instanceof CasaDataNode) {
            final CasaDataNode node = ((CasaDataNode) activatedNodes[0]);
            CasaDataObject obj = (CasaDataObject) node.getDataObject();
            CasaDataEditorSupport es = obj.getLookup().lookup(CasaDataEditorSupport.class);
            if (es != null) {
                cmodel = es.getModel();
            }
        } else if (activatedNodes[0] instanceof CasaNode) {
            final CasaNode node = ((CasaNode) activatedNodes[0]);
            cmodel = node.getModel();
        }

        if (cmodel == null) {
            return;
        }

        final Point location = new Point(-1, -1);
        final CasaWrapperModel model = cmodel;
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                showDialog(model, location);
            }
        });
    }

    private void showDialog(final CasaWrapperModel model, final Point location) {

        final Map<String, JbiBindingInfo> portMap = new HashMap<String, JbiBindingInfo>();

        JbiDefaultComponentInfo bcinfo = JbiDefaultComponentInfo.getJbiDefaultComponentInfo();
        if (bcinfo != null) {
            Map<String, LocalizedTemplateGroup> bcTemplates = getWsdlTemplates();
            List<JbiBindingInfo> bclist = bcinfo.getBindingInfoList();
            for (JbiBindingInfo bi : bclist) {
                String bindingType = bi.getBindingType().toUpperCase();
                if (bcTemplates.get(bindingType) != null) {
                    portMap.put(bi.getBindingType(), bi);
                }
            }
        }
        String[] ports = portMap.keySet().toArray(new String[0]);
        Arrays.sort(ports);

        final LoadWsdlPortPanel panel = new LoadWsdlPortPanel(
                NbBundle.getMessage(getClass(), "LBL_AllAvailableWSDLPorts"),
                ports);
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                NbBundle.getMessage(LoadWsdlPortPanel.class, "LBL_WsdlPort_Selection_Panel"),   // NOI18N
                true,
                new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                    int x = location.x;
                    int y = location.y;
                    for (String selectedItem : panel.getSelectedItems()) {
                        JbiBindingInfo info = portMap.get(selectedItem);
                        model.addCasaPort(
                                info.getBindingType(),
                                info.getBindingComponentName(),
                                x,
                                y);
                    }
                }
            }
        });

        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setPreferredSize(new Dimension(400, 400));
        dlg.setVisible(true);
    }

}
