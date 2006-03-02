/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * Form.java
 *
 * Created on September 30, 2005, 12:30 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.xml.schema.model;

/**
 *
 * @author ChrisWebster
 */
public enum Form {
    QUALIFIED("qualified"), UNQUALIFIED("unqualified");
    
    Form(String v) {
        value = v;
    }
    
    public String toString() {
        return value;
    }
    
    private String value;
}
