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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.projects.jbi.api.JbiBindingInfo;
import org.netbeans.modules.compapp.projects.jbi.api.JbiDefaultComponentInfo;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.ExtensibilityElementTemplateFactory;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplateGroup;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;


/**
 *
 * @author jsandusky
 */
public class AddWSDLPortsAction extends AbstractAction {
    
    private WeakReference<CasaDataObject> mReference;
    
    
    public AddWSDLPortsAction(CasaDataObject dataObject) {
        super(
                NbBundle.getMessage(AddWSDLPortsAction.class, "LBL_AddWSDLPortsAction_Name"), 
                null);
        mReference = new WeakReference<CasaDataObject>(dataObject);
    }
    
    
    public Action getAction() {
        return this;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof CanvasNodeProxyContext) {
            CanvasNodeProxyContext context = (CanvasNodeProxyContext) e.getSource();
            performAction(context.getLocalLocation());
        } else {
            performAction(new Point(-1, -1));
        }
    }

    private Map<String, LocalizedTemplateGroup> getWsdlTemplates() {
        ExtensibilityElementTemplateFactory factory = new ExtensibilityElementTemplateFactory();
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

    public void performAction(final Point location) {
        
        CasaDataObject dataObject = mReference.get();
        if (dataObject == null) {
            return;
        }
        
        final CasaWrapperModel model = dataObject.getEditorSupport().getModel();

        final Map<String, JbiBindingInfo> portMap = new HashMap<String, JbiBindingInfo>();

        JbiDefaultComponentInfo bcinfo = JbiDefaultComponentInfo.getJbiDefaultComponentInfo();
        if (bcinfo != null) {
            Map<String, LocalizedTemplateGroup> bcTemplates = getWsdlTemplates();
            List<JbiBindingInfo> bclist = bcinfo.getBindingInfoList();
            for (JbiBindingInfo bi : bclist) {
                String biName = bi.getBindingName().toUpperCase();
                if (bcTemplates.get(biName) != null) {
                    portMap.put(bi.getBindingName(), bi);
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
                    String selectedItem = panel.getSelectedItem();
                    JbiBindingInfo info = portMap.get(selectedItem);
                    model.addCasaPort(
                            info.getBindingName(),
                            info.getBcName(),
                            location.x,
                            location.y);
                }
            }
        });

        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setPreferredSize(new Dimension(400, 400));
        dlg.setVisible(true);
    }
    
}
