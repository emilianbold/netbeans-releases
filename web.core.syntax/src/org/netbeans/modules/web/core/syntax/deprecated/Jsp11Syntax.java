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

package org.netbeans.modules.web.core.syntax.deprecated;

import org.netbeans.modules.web.core.syntax.deprecated.JspMultiSyntax;
import org.netbeans.modules.web.core.syntax.*;
import org.netbeans.editor.Syntax;
import org.netbeans.modules.web.core.syntax.spi.JSPColoringData;

/** Handles syntax coloring for JSP 1.1. This involves handling custom tags.
 * This class relies on an external source of data, which provides information
 * about tag libraries. The information necessary is:
 * <ul>
 *   <li>Prefixes of tag libraries imported by the page (and its included pages !)</li>
 *   <li>For individual tags inside the tag libraries, it's <code>bodyContent</code> property</li>
 * </ul>
 * This class is able to deal with cases when this information is incomplete, 
 * i.e. if the information for individual tags is missing (for example in the case when the
 * .tld descriptor of the library was not found). In such a case the tags for which the information
 * is missing are treated as if they had bodycontent set to JSP.
 *
 * // PENDING - handle TAG_DEPENDENT tags correctly, change JspMultiSyntax and JspTagSyntax accordingly
 *
 * @author  petr.jiricka@netbeans.com
 * @deprecated Use {@link JspLexer} instead.
 *
 */
public class Jsp11Syntax extends JspMultiSyntax {

    /** Creates new Jsp11Syntax */
    public Jsp11Syntax() {
        super();
    }

    public Jsp11Syntax(Syntax contentSyntax, Syntax scriptingSyntax) {
        super(contentSyntax, scriptingSyntax);
    }

    /** Only keep reference to listener which listens on the JSP DataObject so 
     * it's not garbage collected. */
    public Object listenerReference;

    /** Data providing the information about tag libraries. */
    public JSPColoringData data;

    protected boolean isJspTag(String tagName) {
        // not calling super() for performance reasons
        if (tagName.startsWith("jsp:")) {   // NOI18N
            // standard JSP tag
            return true;
        }
        if (data == null)
            return false;
        
        int colonIndex = tagName.indexOf(':');
        if (colonIndex == -1) {
            // not a JSP tag
            return false;
        }

        // return true if there is information for a library with our prefix
        return data.isTagLibRegistered(tagName.substring(0, colonIndex));
    }

    
    /** Determines whether any EL expressions should be colored as expressions, 
     * or ignored. Returna the correct value per section  JSP.3.3.2
     * of the specification.
     * @param whether this expression is inside the JSP tag value, or just in template text
     * @return true if the expression should be ignored, false if it should be treated as an expression
     */
    protected boolean isELIgnored(boolean inJspTag) {
        if (data == null) {
            return false;
        }
        // PENDING: what we could do is the following:
        // for a 2.3 application, see if the page uses a tag library that hacks
        // EL support (JSTL or JSF) and if it does, enable EL expressions inside
        // JSP tag attribute values for this page.
        if (inJspTag) {
            return false;
        }
        return data.isELIgnored();
    }
  
    
    protected boolean isXMLSyntax(){
        if (data == null) {
            return false;
        }
        return data.isXMLSyntax();
    }
}