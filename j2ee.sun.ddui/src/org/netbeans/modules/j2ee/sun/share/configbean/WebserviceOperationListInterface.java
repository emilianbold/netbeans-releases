/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
