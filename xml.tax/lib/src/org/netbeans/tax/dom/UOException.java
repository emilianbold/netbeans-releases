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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.tax.dom;

import org.w3c.dom.*;

/**
 * Unsupported operation exception is thown by all methods required by
 * higher DOM level than 1.
 * In DOM level 1 is DOMException abstract.
 *
 * @author  Petr Kuzel
 */
class UOException extends DOMException {

    private static final long serialVersionUID = 6549456597318082770L;

    /**
     * Creates new <code>UOException</code> without detail message.
     */
    public UOException() {
        super(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");  //NOI18N
    }
}


