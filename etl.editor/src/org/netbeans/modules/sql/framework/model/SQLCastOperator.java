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
package org.netbeans.modules.sql.framework.model;

/**
 * Definition of unique properties associated with a cast-ass operator
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public interface SQLCastOperator extends SQLGenericOperator {
    /**
     * Gets current precision/length value. Value may be meaningless if the current JDBC
     * type does not support a precision/length value.
     * 
     * @return current precision
     */
    public int getPrecision();

    /**
     * Gets current scale value. Value may be meaningless if the current JDBC type does
     * not support a scale value.
     * 
     * @return current scale
     */
    public int getScale();

    /**
     * Sets precision to given value.
     * 
     * @param newValue new precision
     */
    public void setPrecision(int newValue);

    /**
     * Sets scale to given value.
     * 
     * @param newValue new value
     */
    public void setScale(int newValue);
}
