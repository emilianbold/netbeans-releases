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
package org.netbeans.modules.xslt.tmap.model.api;

import org.netbeans.modules.xml.xam.dom.ComponentFactory;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface TMapComponentFactory extends ComponentFactory<TMapComponent> {
//<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
//<xsltmap xmlns:ns0="http://sun.com/XsltFilterTest" xmlns:ns1="http://sun.com/FileFilterTest">
//    <service partnerLinkType="ns0:pltFilterTest1" roleName="server">
//        <operation opName="copyEmpl" file="Empl-Input2Output.xsl">
//            <invokes partnerLinkType="ns1:pltFilterTest2" roleName="client"
//                     opName="writeEmpl" />
//        </operation>
//    </service>

//    <filterOneWay>
//        <input  partnerLink="{http://sun.com/XsltFilterTest}pltFilterTest1"       
//                roleName="server"            
//                portType="{http://sun.com/XsltFilterTest}xsltFilterPort"
//                operation="copyEmpl"
//                messageType="{http://sun.com/FileFilterTest}output-msg"
//                file="Empl-Input2Output.xsl" />
//        <output partnerLink="{http://sun.com/FileFilterTest}pltFilterTest2"
//                roleName="client"
//                portType="{http://sun.com/FileFilterTest}fileFilterPort"
//                operation="writeEmpl" />
//    </filterOneWay>
//</xsltmap>
    
    TransformMap createTransformMap();
    
    Service createService();
    
    Operation createOperation();
    
    Invokes createInvokes();
}
