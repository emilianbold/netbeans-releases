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
 * WebserviceOperationListInterface.java
 *
 * Created on February 9, 2005, 11:26 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

/**
 *
 * @author Rajeshwar Patil
 */


//This interface is used by the panel used to implement Message Security Binding element.
//Implement by SessionEjb and ServiceRef
public interface WebserviceOperationListInterface {

    //provides list of operations or methods
    public java.util.List getOperations(String portInfoName);

    //used to enable save
    public void setDirty();

}
