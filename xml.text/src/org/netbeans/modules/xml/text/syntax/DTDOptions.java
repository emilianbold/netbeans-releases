/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.text.syntax;

/**
 * Options for the dtd editor kit
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class DTDOptions extends AbstractBaseOptions {
    /** Serial Version UID */
    private static final long serialVersionUID = 2347735706857337892L;

    
    //
    // init
    //

    /** */
    public DTDOptions () {
        super (DTDKit.class, "dtd"); // NOI18N
    }

}
