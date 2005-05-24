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
