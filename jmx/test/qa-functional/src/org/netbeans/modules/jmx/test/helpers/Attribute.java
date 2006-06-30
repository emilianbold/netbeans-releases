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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.test.helpers;

/**
 *
 * @author an156382
 */
public class Attribute {

    private String attrName = "";
    private String attrType = "";
    private String attrAccess = "";
    private String attrComment = "";

    /** Creates a new instance of Attribute */
    public Attribute(String attrName, String attrType, String attrAccess,
            String attrComment) {
        this.attrName = attrName;
        this.attrType = attrType;
        this.attrAccess = attrAccess;
        this.attrComment = attrComment;
    }
    
    /**
     * Returns the name of the MBean
     * @return attrName the name of the MBean
     */
    public String getName() {
        return attrName;
    }
    
    /**
     * Returns the type of the MBean
     * @return attrType the type of the MBean
     */
    public String getType() {
        return attrType;
    }
    
    /**
     * Returns the access permission for the MBean
     * @return attrAccess the access permission for the MBean
     */
    public String getAccess() {
        return attrAccess;
    }
    
    /**
     * Returns the comment for the MBean
     * @return attrComment the comment for the MBean
     */
    public String getComment() {
        return attrComment;
    }
}
