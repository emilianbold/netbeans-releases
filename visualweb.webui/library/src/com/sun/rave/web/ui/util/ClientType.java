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
package com.sun.rave.web.ui.util;

/**
 * This class provides a typesafe enumeration of value types (see also
 * ClientTypeEvaluator). The ClientTypeEvaluator and the
 * ClientTypes are helper classes for UIComponents which accept
 * value bindings that can be either single objects or a collection of
 * objects (for example, an array). Typically, these components have
 * to process input differently depending on the type of the value
 * object.
 *@see com.sun.rave.web.ui.util.ClientSniffer
 *
 */
public class ClientType {

    private String type;

    /** Client type is Mozilla 6 or higher */
    public static final ClientType GECKO = new ClientType("gecko") ;
    /** Client type is IE6 or higher */
    public static final ClientType IE6 = new ClientType("ie6");
     /** Client type is IE 5, version 5.5 or higher */
    public static final ClientType IE5_5 = new ClientType("ie5.5"); 
    /** Client type is not IE 5.5+ or gecko. */
    public static final ClientType OTHER = new ClientType("default");

    private ClientType(String s) { 
	type = s; 
    } 
       
    /**
     * Get a String representation of the action
     * @return A String representation of the value type.
     */
    public String toString() {
	return type;
    }
}
