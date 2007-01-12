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


package org.netbeans.spi.jsp.lexer;

import java.util.Collections;
import java.util.Map;


/** Holds data relevant to the JSP coloring for one JSP page. 
 *
 * @author Marek Fukala
 */
public final class JspParseData {
    
    private Map<String, String> prefixMap;
    private boolean isELIgnored, isXMLSyntax;
    
    public JspParseData() {
        prefixMap = Collections.emptyMap();
        isELIgnored = false;
        isXMLSyntax = false;
    }
    
    /** Updates coloring data. The update is initiated by parser successfuly finished parsing. */
    public void updateParseData(Map<String,String> prefixMap, boolean isELIgnored, boolean isXMLSyntax) {
        this.prefixMap = prefixMap;
        this.isELIgnored = isELIgnored;
        this.isXMLSyntax = isXMLSyntax;
    }
    
    /** Returns true if the given tag library prefix is known in this page.
     */
    public boolean isTagLibRegistered(String prefix) {
        if (prefixMap == null) {
            return false;
        }
        return prefixMap.containsKey(prefix);
    }
    
    /** Returns true if the EL is ignored in this page.
     */
    public boolean isELIgnored() {
        return isELIgnored;
    }
    
    /** Returns true if the page is in xml syntax (JSP Documnet). 
     * If the page is in standard syntax, returns false.
     */
    public boolean isXMLSyntax(){
        return isXMLSyntax;
    }
    
}
