/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.mbeanwizard;

import java.util.List;
import org.netbeans.modules.jmx.MBeanOperation;
import org.netbeans.modules.jmx.MBeanOperationParameter;
import org.netbeans.modules.jmx.MBeanOperationException;


/**
 *
 * @author an156382
 */
public class MBeanWrapperOperation extends MBeanOperation{
    
    private boolean selected;
    
    /** Creates a new instance of MBeanWrapperOperation */
    public MBeanWrapperOperation(boolean selected, String operationName, 
            String operationReturnType,
            List<MBeanOperationParameter> operationParameters,
            List<MBeanOperationException> operationExceptions,
            String operationDescription) {
        super(operationName,operationReturnType,operationParameters,
                operationExceptions,operationDescription);
        this.selected = selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    public boolean isSelected() {
        return this.selected;
    }
    
}
