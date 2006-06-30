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
 * ActivationConfig.java
 *
 * Created on November 18, 2004, 10:24 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface ActivationConfig extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String DESCRIPTION = "Description";	// NOI18N
    public static final String ACTIVATION_CONFIG_PROPERTY = "ActivationConfigProperty";	// NOI18N

    /** Setter for description property
     * @param value property value
     */
    public void setDescription(java.lang.String value);
    /** Getter for description property.
     * @return property value
     */
    public java.lang.String getDescription();
    
    public ActivationConfigProperty[] getActivationConfigProperty();
    public ActivationConfigProperty getActivationConfigProperty(int index);
    public void setActivationConfigProperty(ActivationConfigProperty[] value);
    public void setActivationConfigProperty(int index, ActivationConfigProperty value);
    public int addActivationConfigProperty(ActivationConfigProperty value);
    public int removeActivationConfigProperty(ActivationConfigProperty value);
    public int sizeActivationConfigProperty();
    public ActivationConfigProperty newActivationConfigProperty();
    
}
