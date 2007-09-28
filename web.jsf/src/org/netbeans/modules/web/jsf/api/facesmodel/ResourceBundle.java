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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jsf.api.facesmodel;

import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 *
 * @author Petr Pisl
 */
public interface ResourceBundle extends JSFConfigComponent, DescriptionGroup {
    /**
     * Property name of &lt;base-name&gt; element.
     * The fully qualified class name of the
     * java.util.ResourceBundle instance.
     */ 
    public static final String BASE_NAME = JSFConfigQNames.BASE_NAME.getLocalName();
    /**
     * Property name of &lt;var&gt; element.
     * The name by which this ResourceBundle instance is retrieved by a call to
     * Application.getResourceBundle().
     */ 
    public static final String VAR = JSFConfigQNames.VAR.getLocalName();
    
    String getBaseName();
    void setBaseName(String baseName);
    
    String getVar();
    void setVar(String var);
}
