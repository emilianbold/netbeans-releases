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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.common;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo;
import org.netbeans.modules.j2ee.sun.dd.api.common.WsdlPort;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice.PortInfoPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;


/**
 * @author Peter Williams
 */
public class PortInfoNode extends NamedBeanNode {

    public PortInfoNode(SectionNodeView sectionNodeView, final DDBinding binding, final ASDDVersion version) {
        super(sectionNodeView, binding, null, generateTitle((PortInfo) binding.getSunBean()), ICON_BASE_PORT_INFO_NODE, version);
        enableRemoveAction();
    }

    protected SectionNodeInnerPanel createNodeInnerPanel() {
        return new PortInfoPanel(getSectionNodeView(), this, version);
    }
    
    @Override
    protected String generateTitle() {
        return generateTitle((PortInfo) key);
    }
    
    static String generateTitle(PortInfo portInfo) {
        StringBuilder builder = new StringBuilder(128);
        
        String sei = portInfo.getServiceEndpointInterface();
        if(Utils.notEmpty(sei)) {
            builder.append(sei);
        } else {
            WsdlPort wsdlPort = portInfo.getWsdlPort();
            if(wsdlPort != null) {
                String nsuri = wsdlPort.getNamespaceURI();
                String localPart = wsdlPort.getLocalpart();

                if(Utils.notEmpty(nsuri)) {
//                    if(builder.length() > 0) {
//                        builder.append(", ");
//                    }
                    builder.append(nsuri);
                }

                if(Utils.notEmpty(localPart)) {
                    if(builder.length() > 0) {
                        builder.append(", ");
                    }
                    builder.append(localPart);
                }
            }
        }
        
        if(builder.length() == 0) {
            builder.append("Unbound port-info");
        }
        
        return builder.toString();
    }
    
}
