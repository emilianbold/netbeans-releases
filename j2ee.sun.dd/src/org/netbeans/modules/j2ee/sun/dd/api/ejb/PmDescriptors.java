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
