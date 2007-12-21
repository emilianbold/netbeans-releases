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

package org.netbeans.modules.bpel.debugger.spi.plugin.exec;

import org.netbeans.modules.bpel.debugger.spi.plugin.*;
import org.netbeans.modules.bpel.debugger.spi.plugin.def.ValueType;

/**
 * Represents a value of a variable or result of an expression.
 * Supposed to have more specific interfaces extending this one.
 * While there's no such interfaces yet, a value should be obtained using
 * asString() method.
 *
 * @author Alexander Zgursky
 */
public interface Value {
    ValueType getType();
    String asString();
}
