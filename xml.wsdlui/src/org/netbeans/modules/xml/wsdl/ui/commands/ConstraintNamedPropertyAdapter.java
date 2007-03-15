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
 * Created on Jun 14, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.commands;

import org.netbeans.modules.xml.refactoring.spi.AnalysisUtilities;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.api.property.PropertyAdapter;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.NamedReferenceable;

/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class ConstraintNamedPropertyAdapter extends PropertyAdapter implements NamedPropertyAdapter {

    
    public ConstraintNamedPropertyAdapter(WSDLComponent delegate) {
        super(delegate);
    }
    
    public abstract boolean isNameExists(String name);
    
    public void setName(String name) {
        WSDLComponent comp = getDelegate();
        if (comp instanceof NamedReferenceable){
            NamedReferenceable ref = NamedReferenceable.class.cast(comp);
            if (ref != null) {
                // try rename silent and locally
                SharedUtils.locallyRenameRefactor((Nameable)ref, name);
            }
        } else if (comp instanceof Nameable) {
            getDelegate().getModel().startTransaction();
            Nameable.class.cast(comp).setName(name);
            
                getDelegate().getModel().endTransaction();
        }
        
    }

    public String getName() {
        WSDLComponent comp = getDelegate();
        String name = null;
        if (comp instanceof NamedReferenceable){
            name = NamedReferenceable.class.cast(comp).getName();
        } else if (comp instanceof Nameable) {
            name = Nameable.class.cast(comp).getName();
        }
        if (name == null) {
            return "";
        }
        return name;
    }

    
}
