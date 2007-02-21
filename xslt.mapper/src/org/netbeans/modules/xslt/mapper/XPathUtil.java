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
package org.netbeans.modules.xslt.mapper;

import org.netbeans.modules.xml.xpath.AbstractXPathModelHelper;
import org.netbeans.modules.xml.xpath.XPathException;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathModel;

/**
 *
 * @author Alexey
 */
public class XPathUtil {
    
    public static XPathExpression createExpression(String s){
        XPathModel xpImpl = AbstractXPathModelHelper.getInstance().newXPathModel();
        try {
            return xpImpl.parseExpression(s);
        } catch (XPathException ex) {
            
        };
        return null;
    }
    
    public static String removeQuotes(String expression) {
        String result = expression;
        if (expression != null) {            
            if ((expression.length() > 1) && 
                    ((expression.startsWith("'")) && (expression.endsWith("'")) ||
                    (expression.startsWith("\"")) && (expression.endsWith("\"")))) {
                StringBuffer buf = new StringBuffer(expression);
                buf.deleteCharAt(0);
                buf.deleteCharAt(buf.length() - 1);
                result = buf.toString();
            }
        }
        return result;
    }            
}
