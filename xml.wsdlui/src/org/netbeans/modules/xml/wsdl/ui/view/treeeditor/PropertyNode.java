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

import javax.swing.Action;

import org.openide.actions.PropertiesAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Ritesh Adval
 *
 *
 */
public class PropertyNode extends AbstractNode {

    private Image ICON  = Utilities.loadImage
         ("org/netbeans/modules/xml/wsdl/ui/view/resources/property.png");


    protected Property mWSDLConstruct;
    
    public PropertyNode(Property wsdlConstruct) {
        super(Children.LEAF);
        mWSDLConstruct = wsdlConstruct;
        this.setDisplayName(mWSDLConstruct.getName());
    }
    
    public Image getIcon(int type) {
        return ICON;
    }

    public Image getOpenedIcon(int type) {
        return ICON;
    }
    
    public Action[] getActions(boolean context) {
        SystemAction[] sysAction = new SystemAction[]{
                SystemAction.get(PropertiesAction.class)
            };

            return sysAction;
    }
    
    public Object getWSDLConstruct() {
        return mWSDLConstruct;
    }
    
}
