/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * DOMEntityDecl.java
 *
 * Created on March 29, 2001, 8:20 PM
 */

package org.netbeans.xtest.util;

/**
 *
 * @author  vs124454
 * @version 
 */
public class DOMEntityDecl {

    String name = "";
    String sysid = "";
    String pubid = "";
    
    /** Creates new DOMEntityDecl */
    public DOMEntityDecl() {
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setSystemId(String sysid) {
        this.sysid = sysid;
    }
    
    public void setPublicId(String pubid) {
        this.pubid = pubid;
    }
    
    public String getName() {
        return name;
    }
    
    public String getSystemId() {
        return sysid;
    }
    
    public String setPublicId() {
        return pubid;
    }
}
