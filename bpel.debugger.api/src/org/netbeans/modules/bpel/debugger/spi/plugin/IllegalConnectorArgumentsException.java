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

package org.netbeans.modules.bpel.debugger.spi.plugin;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Alexander Zgursky
 */
public class IllegalConnectorArgumentsException extends Exception {
    public static final long serialVersionUID = 0L;

    private List<String> argumentNames;

    /**
     * Creates a new instance of IllegalConnectorArgumentsException.
     */
    public IllegalConnectorArgumentsException(
            String message, List<String> argNames)
    {
        super(message);
        argumentNames = argNames;
    }
    
    /**
     * Creates a new instance of IllegalConnectorArgumentsException.
     */
    public IllegalConnectorArgumentsException(
            String message, String argName)
    {
        super(message);
        argumentNames = Arrays.asList(argName);
    }
    
    public List<String> getArgumentNames() {
        return argumentNames;
    }
}
