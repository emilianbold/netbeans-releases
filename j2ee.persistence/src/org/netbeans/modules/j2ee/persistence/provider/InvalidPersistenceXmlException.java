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

package org.netbeans.modules.j2ee.persistence.provider;

/**
 * Thrown to indicate that a persistence.xml file is not valid, e.g. 
 * it could not be recognized correctly.
 * 
 * @author Erno Mononen
 */
public class InvalidPersistenceXmlException extends Exception{
    
    /**
     * The path to the invalid persistence.xml file. 
     */
    private final String path;
    
    /**
     * Creates a new instance of InvalidPersistenceXmlException
     * @parameter path the path to the invalid persistence.xml file
     */
    public InvalidPersistenceXmlException(String path) {
        this.path = path;
    }
    
    /**
     * Creates a new instance of InvalidPersistenceXmlException
     * @parameter message the detail message for the exception
     * @parameter path the path to the invalid persistence.xml file
     */
    public InvalidPersistenceXmlException(String message, String path) {
        super(message);
        this.path = path;
    }
    
    /**
     * @return the path to the invalid persistence.xml file.
     */ 
    public String getPath(){
        return path;
    }
    
    public String toString(){
        return getClass().getName() + "[path: " + getPath() + "]";
    }
    
}