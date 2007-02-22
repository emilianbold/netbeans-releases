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

package org.netbeans.modules.xml.axi.datatype;

/**
 * This class represents DecimalType. This is one of those atomic types that can
 * be used to type an Attribute or leaf Elements in AXI Model
 *
 * MaxInclusive is Double.MAX_VALUE
 * MinInclusive is Double.MIN_VALUE
 *
 * @author Ayub Khan
 */
public class DecimalType extends NumberBase {
    
    /**
     * Creates a new instance of DecimalType
     */
    public DecimalType() {
        super(Datatype.Kind.DECIMAL);
    }
    
    /**
     * Creates a new instance of derived type of DecimalType
     */
    public DecimalType(Datatype.Kind kind) {
        super(kind);
    }
    
}
