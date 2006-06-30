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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
