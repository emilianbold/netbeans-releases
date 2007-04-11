/*
 *	The contents of this file are subject to the terms of the Common Development
 *	and Distribution License (the License). You may not use this file except in
 *	compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 *	or http://www.netbeans.org/cddl.txt.
 *	
 *	When distributing Covered Code, include this CDDL Header Notice in each file
 *	and include the License file at http://www.netbeans.org/cddl.txt.
 *	If applicable, add the following below the CDDL Header, with the fields
 *	enclosed by brackets [] replaced by your own identifying information:
 *	"Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is Terminal Emulator.
 * The Initial Developer of the Original Software is Sun Microsystems, Inc..
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2001.
 * All Rights Reserved.
 *
 * Contributor(s): Ivan Soleimanipour.
 */

/*
 * "InterpKit.java"
 * InterpKit.java 1.3 01/07/23
 * The abstract operations the terminal can perform.
 */

package org.netbeans.lib.terminalemulator;


/*
 * Registry and locator for various built-in Interp's
 */

abstract class InterpKit {
    static Interp forName(String name, Ops ops) {
	if (name.equals("dumb"))	// NOI18N
	    return new InterpDumb(ops);
	else if (name.equals("ansi"))	// NOI18N
	    return new InterpANSI(ops);	
        else if (name.equals("dtterm"))	// NOI18N
	    return new InterpDtTerm(ops);      
	else
	    return null;
    } 
}
