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
 * Used to check MBean attribute wizard values.
 */
public class Attribute {

    private String attrName = "";
    private String attrType = "";
    private String attrAccess = "";
    private String attrDescription = "";

    /** Creates a new instance of Attribute */
    public Attribute(String attrName, String attrType, String attrAccess,
            String attrDescription) {
        this.attrName = attrName;
        this.attrType = attrType;
        this.attrAccess = attrAccess;
        this.attrDescription = attrDescription;
    }
    
    /**
     * Returns the name of the attribute
     * @return attrName the name of the attribute
     */
    public String getName() {
        return attrName;
    }
    
    /**
     * Sets the name of the attribute
     * @param attrName the name of the attribute
     */
    public void setName(String attrName) {
        this.attrName = attrName;
    }
    
    /**
     * Returns the type of the attribute
     * @return attrType the type of the attribute
     */
    public String getType() {
        return attrType;
    }
    
    /**
     * Sets the type of the attribute
     * @param attrType the type of the attribute
     */
    public void setType(String attrType) {
        this.attrType = attrType;
    }
    
    /**
     * Returns the access permission for the attribute
     * @return attrAccess the access permission for the attribute
     */
    public String getAccess() {
        return attrAccess;
    }
    
    /**
     * Sets the access permission for the attribute
     * @param attrAccess the access permission for the attribute
     */
    public void setAccess(String attrAccess) {
        this.attrAccess = attrAccess;
    }
    
    /**
     * Returns the description for the attribute
     * @return attrDescription the description for the attribute
     */
    public String getDescription() {
        return attrDescription;
    }

    /**
     * Sets the description for the attribute
     * @param attrDescription the description for the attribute
     */
    public void setDescription(String attrDescription) {
        this.attrDescription = attrDescription;
    }
}
