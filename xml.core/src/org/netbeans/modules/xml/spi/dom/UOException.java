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
 * Unsupported operation exception is thown by all methods required by
 * higher DOM level than 1.
 * In DOM level 1 is DOMException abstract.
 *
 * @author  Petr Kuzel
 */
public final class UOException extends DOMException {

    private static final long serialVersionUID = 6549456597318082770L;
    
    /**
     * Creates new <code>UOException</code> without detail message.
     */
    public UOException() {
        super(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");  //NOI18N
    }
}


