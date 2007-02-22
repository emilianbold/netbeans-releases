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


package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.awt.Image;

import org.netbeans.modules.xml.wsdl.model.Operation;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 *
 * @author Ritesh Adval
 *
 */
public class OneWayOperationNode extends OperationNode {
    
    
    /** Icon for the Ip msg button.    */
    private static Image ICON  = Utilities.loadImage
             ("org/netbeans/modules/xml/wsdl/ui/view/resources/oneway_operation.png");
    
    public OneWayOperationNode(Operation wsdlConstruct) {
        super(wsdlConstruct);
    }
    
    @Override
    public Image getIcon(int type) {
        return ICON;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }

    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(OneWayOperationNode.class, "LBL_OneWayNode_TypeDisplayName");
    }
}
