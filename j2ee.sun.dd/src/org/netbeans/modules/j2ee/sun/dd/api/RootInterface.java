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
package org.netbeans.modules.j2ee.sun.dd.api;

import java.io.IOException;
import org.openide.filesystems.FileObject;

/**
 * Interface representing the root of interfaces bean tree structure.
 *
 *
 */
public interface RootInterface extends CommonDDBean {    
    
    public static final String PROPERTY_STATUS = "dd_status";
    public static final String PROPERTY_VERSION = "dd_version";
    public static final int STATE_INVALID_PARSABLE = 1;
    public static final int STATE_INVALID_UNPARSABLE = 2;
    public static final int STATE_VALID = 0;
    
 
    /** 
     * Changes current DOCTYPE to match version specified.
     * Warning: Only the upgrade from lower to higher version is supported.
     * 
     * @param version 
     */
    public void setVersion(java.math.BigDecimal version);
    
    /** 
     * Version property as defined by the DOCTYPE, if known.
     * 
     * @return current version
     */
    public java.math.BigDecimal getVersion();
    
    /** 
     * Current parsing status
     * 
     * @return status value
     */
    public int getStatus();

    /**
     * Confirms that the DD passed in is the proxied DD owned by this interface
     */
    public boolean isEventSource(RootInterface rootDD);
    
    /** 
     * Writes the deployment descriptor data from deployment descriptor bean graph to file object.<br>
     * This is more convenient method than {@link org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean#write} method.<br>
     * The locking problems are solved for the user in this method.
     *
     * @param fo FileObject for where to write the content of deployment descriptor 
     *   holding in bean tree structure
     * @throws java.io.IOException 
     */
    public void write(FileObject fo) throws IOException;
    
}
