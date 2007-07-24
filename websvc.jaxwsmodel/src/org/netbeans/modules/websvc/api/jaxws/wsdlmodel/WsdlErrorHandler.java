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
package org.netbeans.modules.websvc.api.jaxws.wsdlmodel;

import org.xml.sax.SAXParseException;

/**
 *
 * @author mkuchtiak
 */
public interface WsdlErrorHandler {
    
    /** callbac method : warning
     * @param ex exception describing a warning
     * @throws AbortException to enable abort the parsing process
    */ 
    public void warning(SAXParseException ex) throws AbortException ;
  
    /** callbac method : info
     * @param ex exception describing an information
    */ 
    public void info(SAXParseException ex);
    
    /** callbac method : fatalError
     * @param ex exception describing a fatal error
     * @throws AbortException to enable abort the parsing process
    */ 
    public void fatalError(SAXParseException ex) throws AbortException ;
    
    /** callbac method : error
     * @param ex exception describing an error
     * @throws AbortException to enable abort the parsing process
    */ 
    public void error(SAXParseException ex) throws AbortException ;
    
    /** Exception to enable abort the parse process
     */ 
    public static class AbortException extends Exception {
        public AbortException() {
            super();
        }
        public AbortException(String message) {
            super(message);
        }
    }

}
