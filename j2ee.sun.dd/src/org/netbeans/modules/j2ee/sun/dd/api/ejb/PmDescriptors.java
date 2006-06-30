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
 * PmDescriptors.java
 *
 * Created on November 17, 2004, 4:48 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface PmDescriptors extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String PM_DESCRIPTOR = "PmDescriptor";	// NOI18N
    public static final String PM_INUSE = "PmInuse";	// NOI18N

    public PmDescriptor[] getPmDescriptor();
    public PmDescriptor getPmDescriptor(int index);
    public void setPmDescriptor(PmDescriptor[] value);
    public void setPmDescriptor(int index, PmDescriptor value);
    public int addPmDescriptor(PmDescriptor value);
    public int removePmDescriptor(PmDescriptor value);
    public int sizePmDescriptor(); 
    public PmDescriptor newPmDescriptor(); 
    
    /** Setter for pm-inuse property
     * @param value property value
     */
    public void setPmInuse(PmInuse value);
    /** Getter for pm-inuse property.
     * @return property value
     */
    public PmInuse getPmInuse(); 
    public PmInuse newPmInuse(); 
}
