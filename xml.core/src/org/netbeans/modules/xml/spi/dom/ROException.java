/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.spi.dom;

import org.w3c.dom.*;

/**
 * Read-only eception is thrown from all modification methods.
 * In DOM level 1 is DOMException abstract.
 *
 * @author  Petr Kuzel
 */
public final class ROException extends DOMException {

    private static final long serialVersionUID = -2953952370813340306L;
    
    /**
     * Creates new <code>ROException</code> without detail message.
     */
    public ROException() {
        super(DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
    }
}


