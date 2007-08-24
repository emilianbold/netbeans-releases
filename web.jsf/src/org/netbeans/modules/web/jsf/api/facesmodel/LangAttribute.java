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

import org.netbeans.modules.web.jsf.impl.facesmodel.FacesAttributes;

/**
 * The xml:lang attribute defines the language that the
 * elements are provided in. Its value is "en" (English)
 * by default
 * @author Petr Pisl
 */
public interface LangAttribute extends JSFConfigComponent {

    public static String LANG_ATTRIBUTE  = FacesAttributes.LANG.getName();
    
    /**
     * The xml:lang attribute defines the language that the
     * elements are provided in. Its value is "en" (English)
     * by default.
     * @return the lang attribute.
     */
    public String getLang();
    
    /**
     * The lang attribute defines the language that the
     * elements are provided in. Its value is "en" (English)
     * by default.
     * @param lang the value of lang attribute.
     */
    public void setLang(String lang);
}
