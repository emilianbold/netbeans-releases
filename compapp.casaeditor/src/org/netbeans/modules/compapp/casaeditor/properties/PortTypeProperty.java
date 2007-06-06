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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.casaeditor.properties;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.openide.util.NbBundle;

/**
 *
 * @author rdara
 */
public class PortTypeProperty extends BaseCasaProperty {

    public PortTypeProperty(CasaNode node) {
                super(
                node, 
                (CasaComponent) node.getData(), 
                null, 
                String.class, 
                "portTypeDefinition", // NOI18N
                NbBundle.getMessage(PortTypeProperty.class, "PROP_PortTypeDefinition"),  // NOI18N
                NbBundle.getMessage(PortTypeProperty.class, "PROP_PortTypeDefinition")); // NOI18N
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return "Implement";
    }

    public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public PropertyEditor getPropertyEditor() {
        return new PortTypeEditor((CasaWrapperModel)getComponent().getModel(),
                                   null,
                                   NbBundle.getMessage(getClass(), "PROP_PortTypeDefinition")  // NOI18N
                                  );
    }
}
