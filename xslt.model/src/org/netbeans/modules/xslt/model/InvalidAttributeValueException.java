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
package org.netbeans.modules.xslt.model;


/**
 * @author ads
 *
 */
public class InvalidAttributeValueException extends Exception {

 
    private static final long serialVersionUID = 5990879376051438447L;

    /** {@inheritDoc} */
    public InvalidAttributeValueException( String message, Throwable cause ) {
        super(message, cause);
    }

    /** {@inheritDoc} */
    public InvalidAttributeValueException( String message ) {
        super(message);
    }

    /** {@inheritDoc} */
    public InvalidAttributeValueException( Throwable throwable ) {
        super( throwable );
    }

    /**
     * Constructor with description message, invalid attribute value and cause.  
     * @param message description
     * @param attributeValue invalid attribute value
     * @param cause original cause of exception 
     */
    public InvalidAttributeValueException( String message, String attributeValue,
            Throwable cause ) 
    {
        this( message , cause );
        myAttributeValue = attributeValue ;
    }
    
    /**
     * Constructor with description message and invalid attribute value.
     * @param message description
     * @param attributeValue  invalid attribute value
     */
    public InvalidAttributeValueException( String message, String attributeValue ) 
    {
        this( message );
        myAttributeValue = attributeValue ;
    }
    
    /**
     * @return invalid attribute value.
     */
    public String getAttributeValue() {
        return myAttributeValue;
    }
    
    private String myAttributeValue;
}
