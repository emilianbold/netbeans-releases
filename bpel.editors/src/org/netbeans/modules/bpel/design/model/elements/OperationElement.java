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

package org.netbeans.modules.bpel.design.model.elements;

import org.netbeans.modules.bpel.design.geometry.FShape;
import org.netbeans.modules.bpel.design.model.elements.icons.Icon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.InvokeIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.ReceiveIcon2D;


/**
 *
 * @author anjeleevich
 */
public class OperationElement extends ContentElement {
    

    public OperationElement(FShape shape, Icon2D icon) {
        super(shape, icon);
    }
    
    
//    public static final OperationElement createReceive() {
//        return new OperationElement(EVENT_SHAPE, ReceiveIcon2D.INSTANCE);
//    }
//
//
//    public static final OperationElement createInvoke() {
//        return new OperationElement(TASK_SHAPE, InvokeIcon2D.INSTANCE);
//    }

}
