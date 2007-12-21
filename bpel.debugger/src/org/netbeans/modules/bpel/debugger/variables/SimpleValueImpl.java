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

package org.netbeans.modules.bpel.debugger.variables;

import org.netbeans.modules.bpel.debugger.api.variables.NamedValueHost;
import org.netbeans.modules.bpel.debugger.api.variables.SimpleValue;

/**
 *
 * @author Alexander Zgursky
 */
public class SimpleValueImpl implements SimpleValue {
    private String myValueAsString;
    private NamedValueHost myValueHost;
    
    /** Creates a new instance of SimpleTypeValueImpl */
    public SimpleValueImpl(String valueAsString) {
        myValueAsString = valueAsString;
    }

    /** Creates a new instance of SimpleTypeValueImpl */
    public SimpleValueImpl(String valueAsString, NamedValueHost valueHost) {
        myValueAsString = valueAsString;
        myValueHost = valueHost;
    }
    
    public String getValueAsString() {
        return myValueAsString;
    }

    public NamedValueHost getValueHost() {
        return myValueHost;
    }
}
