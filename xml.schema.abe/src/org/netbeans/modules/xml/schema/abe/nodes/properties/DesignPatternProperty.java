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

package org.netbeans.modules.xml.schema.abe.nodes.properties;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.SchemaGenerator;
import org.netbeans.modules.xml.schema.abe.wizard.SchemaTransformWizard;

/**
 * @author Ayub Khan
 */
public class DesignPatternProperty extends BaseABENodeProperty {

    public DesignPatternProperty(AXIComponent component,
            String property, String dispName, String desc)
            throws NoSuchMethodException {
        super(component,SchemaGenerator.Pattern.class,property,dispName,desc,
				DesignPatternEditor.class);
    }

    /**
     * This method sets the value of the property.
     * Overridden to show transform wizard
     */
    @Override
    public void setValue(Object o) throws
            IllegalAccessException, InvocationTargetException{
		AXIModel am = getComponent().getModel();
		assert am != null;
		SchemaGenerator.Pattern previousDesignPattern = 
				am.getSchemaDesignPattern();
		SchemaTransformWizard wizard = 
				new SchemaTransformWizard(am.getSchemaModel());
		SchemaGenerator.Pattern selectedDesignPattern = wizard.show();
		if(!wizard.isCancelled())
			super.setValue(selectedDesignPattern);
		else
			super.setValue(previousDesignPattern);
    }
}
