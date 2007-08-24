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

package org.netbeans.modules.web.jsf.api.facesmodel;

/**
 * The display-name type contains a short name that is intended
 * to be displayed by tools. It is used by display-name
 * elements.  The display name need not be unique.
 * 
 * @author Petr Pisl
 */
public interface DisplayName extends LangAttribute {
        
    /**
     * Gets the content of the display-name element.
     * @return the content of the element.
     */
    public String  getValue();
    
    /**
     * Sets the content of the display-name element.
     * @param displayName new content of the element
     */
    public void setValue(String displayName);
}
