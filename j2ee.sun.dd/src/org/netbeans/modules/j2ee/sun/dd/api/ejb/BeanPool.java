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
 * BeanPool.java
 *
 * Created on November 17, 2004, 5:18 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface BeanPool extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String STEADY_POOL_SIZE = "SteadyPoolSize";	// NOI18N
    public static final String RESIZE_QUANTITY = "ResizeQuantity";	// NOI18N
    public static final String MAX_POOL_SIZE = "MaxPoolSize";	// NOI18N
    public static final String POOL_IDLE_TIMEOUT_IN_SECONDS = "PoolIdleTimeoutInSeconds";	// NOI18N
    public static final String MAX_WAIT_TIME_IN_MILLIS = "MaxWaitTimeInMillis";	// NOI18N
        
    /** Setter for steady-pool-size property
     * @param value property value
     */
    public void setSteadyPoolSize(java.lang.String value);
    /** Getter for steady-pool-size property.
     * @return property value
     */
    public java.lang.String getSteadyPoolSize();
    
    /** Setter for resize-quantity property
     * @param value property value
     */
    public void setResizeQuantity(java.lang.String value);
    /** Getter for resize-quantity property.
     * @return property value
     */
    public java.lang.String getResizeQuantity();
    
    
    /** Setter for max-pool-size property
     * @param value property value
     */
    public void setMaxPoolSize(java.lang.String value);
    /** Getter for max-pool-size property.
     * @return property value
     */
    public java.lang.String getMaxPoolSize();
    
    
    /** Setter for pool-idle-timeout-in-seconds property
     * @param value property value
     */
    public void setPoolIdleTimeoutInSeconds(java.lang.String value);
    /** Getter for pool-idle-timeout-in-seconds property.
     * @return property value
     */
    public java.lang.String getPoolIdleTimeoutInSeconds();
    
    
    /** Setter for max-wait-time-in-millis property
     * @param value property value
     */
    public void setMaxWaitTimeInMillis(java.lang.String value);
    /** Getter for max-wait-time-in-millis property.
     * @return property value
     */
    public java.lang.String getMaxWaitTimeInMillis();
}
