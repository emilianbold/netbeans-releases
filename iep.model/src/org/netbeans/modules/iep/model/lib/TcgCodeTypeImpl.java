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


package org.netbeans.modules.iep.model.lib;

/**
 * Concrete class implements interface TcgCodeType.
 *
 * @author Bing Lu
 *
 * @see TcgCodeType
 * @since April 30, 2002
 */
class TcgCodeTypeImpl implements TcgCodeType {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(TcgCodeTypeImpl.class.getName());

    private String mName = null;
    private String mTemplateName = null;

    /**
     * Constructor for the TcgCodeType object
     *
     * @param name String name of this TcgCodeType
     * @param templateName String Velocity template file name
     */
    TcgCodeTypeImpl(String name, String templateName) {
        mName = name;
        mTemplateName = templateName;
    }

    /**
     * Gets the name attribute of the TcgCodeType object
     *
     * @return The name value
     */
    public String getName() {
        return mName;
    }



    /**
     * Gets the Velocity template file name of this TcgCodeType object
     *
     * @return The templateName value
     */
    public String getTemplateName() {
        return mTemplateName;
    }

    /**
     * Override Object's
     *
     * @return DOCUMENT ME!
     */
    public String toString() {

        StringBuffer sb = new StringBuffer();

        sb.append(super.toString());
        sb.append("\t[");
        sb.append("mName: " + mName + ", ");
        sb.append("mTemplateName: " + mTemplateName);
        sb.append("]");

        return sb.toString();
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
