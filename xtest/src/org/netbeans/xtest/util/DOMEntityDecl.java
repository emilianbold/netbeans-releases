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
