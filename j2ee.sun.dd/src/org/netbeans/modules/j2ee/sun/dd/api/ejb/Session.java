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
 * Session.java
 *
 * Created on November 17, 2004, 5:21 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface Session extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String CHECKPOINT_LOCATION = "CheckpointLocation";	// NOI18N
    public static final String QUICK_CHECKPOINT = "QuickCheckpoint";	// NOI18N
    public static final String CHECKPOINTED_METHODS = "CheckpointedMethods";	// NOI18N

    /** Setter for checkpoint-location property
     * @param value property value
     */
    public void setCheckpointLocation(java.lang.String value);
    /** Getter for checkpoint-location property.
     * @return property value
     */
    public java.lang.String getCheckpointLocation();
    /** Setter for quick-checkpoint property
     * @param value property value
     */
    public void setQuickCheckpoint(java.lang.String value);
    /** Getter for quick-checkpoint property.
     * @return property value
     */
    public java.lang.String getQuickCheckpoint();
    /** Setter for checkpointed-methods property
     * @param value property value
     */
    public void setCheckpointedMethods(CheckpointedMethods value);
    /** Getter for checkpointed-methods property.
     * @return property value
     */
    public CheckpointedMethods getCheckpointedMethods(); 
    
    public CheckpointedMethods newCheckpointedMethods();
    
}
