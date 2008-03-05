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

package org.netbeans.modules.xml.xpath.ext.metadata;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathModel;

/**
 * Represents an unknown extension XPath function. 
 * It doesn't have metadata (the NULL_METADATA_STUB is used), but 
 * the name is known and it is returned by the getName() method. 
 * 
 * @author Enrico Lelina
 * @version 
 */
public class UnknownExtensionFunction extends XPathExtensionFunction {
        
    /** The function name. */
    QName mName;
    
    /**
     * Constructor.
     * Instantiates a new XPathExtensionFunction with the given name.
     * @param name the function name
     */
    public UnknownExtensionFunction(XPathModel model, QName name) {
        super(model);
        mName = name;
    }
    
    /**
     * Gets the name of the function.
     * @return the function name
     */
    @Override
    public QName getName() {
        return mName;
    }
    
    @Override
    public String toString() {
        return XPathUtils.qNameObjectToString(mName);
    }

}
