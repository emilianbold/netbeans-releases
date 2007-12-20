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

package org.netbeans.modules.xml.xpath.ext;

/**
 * XPathModel helper class.
 *
 * @author Enrico Lelina
 * @version
 */
public abstract class XPathModelHelper {

    /** Singleton Castor support */
    private static XPathModelHelper mXPathModelHelper = null;

    /**
     * Get Castor support from Enterprise Designer context.
     * @return AbstractXPathModelHelper    The castor support object.
     */
    public static synchronized XPathModelHelper getInstance() {
        if (mXPathModelHelper == null) {
            mXPathModelHelper = loadImpl(null);
        }

        return mXPathModelHelper;
    }

    /**
     * Get the Castor support using the given Class Loader.
     * @param loader Class Loader that can find the XPathModelHelper
     * implementation class.
     * @return AbstractCastorSuppport The castor support object.
     */
    public static synchronized XPathModelHelper getInstance(ClassLoader loader) {
        if (null == mXPathModelHelper) {
            mXPathModelHelper = loadImpl(loader);
        }
        return mXPathModelHelper;
    }

    /** Loads the XPathModelHelper implementation class.
     * @param   loader  ClassLoader to use.
     * @return  XPathModelHelper implementing class.
     */
    private static XPathModelHelper loadImpl(ClassLoader loader) {
        String implClassName = null;
        XPathModelHelper axmh = null;
        try {
            implClassName = System.getProperty(
                    "org.netbeans.modules.xml.xpath.ext.XPathModelHelper", 
                    "org.netbeans.modules.xml.xpath.ext.impl.XPathModelHelperImpl");
            Class implClass = null;
            if (loader != null) {
                implClass = Class.forName(implClassName, true, loader);
            } else {
                implClass = Class.forName(implClassName);
            }
            axmh = (XPathModelHelper) implClass.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot find/load " + implClassName, e);
        }
        return axmh;
    }

    /**
     * Instantiates a new XPathModel object.
     * @return a new XPathModel object instance
     */
    public abstract XPathModel newXPathModel();
    
}
