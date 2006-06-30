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

package org.netbeans.modules.xsl.grammar;

import org.w3c.dom.*;
/**
 *
 * @author  asgeir@dimonsoftware.com
 */
public class ResultAttr extends ResultNode implements Attr {

    private Attr attr;

    /** Creates a new instance of ResultAttr */
    public ResultAttr(Attr peer, String ignorePrefix, String onlyUsePrefix) {
        super(peer, ignorePrefix, onlyUsePrefix);
        attr = peer;
    }

    public String getName() {
        return attr.getName();
    }

    public Element getOwnerElement() {
        return new ResultElement(attr.getOwnerElement(), ignorePrefix, onlyUsePrefix);
    }
    
    public boolean getSpecified() {
        return attr.getSpecified();
    }
    
    public String getValue() {
        return attr.getValue();
    }
    
    public void setValue(String value) throws DOMException {
        attr.setValue(value);
    }
}
