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
package org.netbeans.modules.xml.wsdl.ui.property.view;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.ui.api.property.ElementOrTypeOrMessagePartProvider;
import org.netbeans.modules.xml.wsdl.ui.api.property.DefaultElementOrTypeProvider;
import org.netbeans.modules.xml.wsdl.ui.api.property.ElementOrTypeAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.api.property.ElementOrTypeOrMessagePartAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.api.property.ElementOrTypeProvider;
import org.netbeans.modules.xml.wsdl.ui.property.model.BuiltInCustomizerGroupedProperty;
import org.netbeans.modules.xml.wsdl.ui.property.model.ElementOrTypeChooser;
import org.netbeans.modules.xml.wsdl.ui.property.model.ElementOrTypeOrMessagePartChooser;
import org.openide.nodes.Node;

public class GroupedBuiltInCustomizerFactory {

    public static Node.Property getProperty(ExtensibilityElement extensibilityElement, 
            BuiltInCustomizerGroupedProperty builtInCustomizer) {
        Node.Property property = null;
        try {
            ElementOrTypeChooser elementOrTypeChooser = builtInCustomizer.getElementOrTypeChooser();
            if (elementOrTypeChooser != null) {
                ElementOrTypeProvider prov = new DefaultElementOrTypeProvider(extensibilityElement, elementOrTypeChooser.getElementAttributeName(), elementOrTypeChooser.getTypeAttributeName());
                return new ElementOrTypeAttributeProperty(prov);
            }

            ElementOrTypeOrMessagePartChooser elementOrTypeOrMessagePartChooser = builtInCustomizer.getElementOrTypeOrMessagePartChooser();
            if (elementOrTypeOrMessagePartChooser != null) {
                ElementOrTypeOrMessagePartProvider prov = new ElementOrTypeOrMessagePartProvider(extensibilityElement,
                        elementOrTypeOrMessagePartChooser.getElementAttributeName(), 
                        elementOrTypeOrMessagePartChooser.getTypeAttributeName(),
                        elementOrTypeOrMessagePartChooser.getMessageAttributeName(),
                        elementOrTypeOrMessagePartChooser.getPartAttributeName());
                return new ElementOrTypeOrMessagePartAttributeProperty(prov);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Logger.getLogger(BuiltInCustomizerFactory.class.getName()).log(Level.INFO, "Not a recognized builtin in chooser");
        return property;
    }

    

}
