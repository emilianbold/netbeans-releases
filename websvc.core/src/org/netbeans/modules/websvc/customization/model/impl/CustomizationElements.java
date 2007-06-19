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
 * CustomizationElements.java
 *
 * Created on February 6, 2006, 10:25 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.model.impl;

/**
 *
 * @author Roderico Cruz
 */
public enum CustomizationElements {
    BINDINGS("bindings"),
    PACKAGE("package"),
    CLASS("class"),
    ENABLEWRAPPERSTYLE("enableWrapperStyle"),
    ENABLEASYNCMAPPING("enableAsyncMapping"),
    ENABLEMIMECONTENT("enableMIMEContent"),
    JAVAEXCEPTION("exception"),
    METHOD("method"),
    PARAMETER("parameter"),
    JAVADOC("javadoc"),
    PROVIDER("provider");
    
    CustomizationElements(String docName) {
        this.docName = docName;
    }
    
    public String getName() {
        return docName;
    }
    
    
    private final String docName;
} 
