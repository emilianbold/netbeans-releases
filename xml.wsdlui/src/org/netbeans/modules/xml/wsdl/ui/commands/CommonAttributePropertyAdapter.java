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

/*
 * CommonAttributePropertyAdapter.java
 *
 * Created on April 17, 2006, 5:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.commands;

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.api.property.PropertyAdapter;

/**
 *
 * @author radval
 */
public abstract class CommonAttributePropertyAdapter extends PropertyAdapter {
    
    public static final String VALUE = "value"; //NOI18N
    
    public CommonAttributePropertyAdapter(WSDLComponent mWSDLConstruct) {
        super(mWSDLConstruct);
        
    }
    
    public abstract void setValue(String value);
    
    
    public abstract String getValue();
}
