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


package org.netbeans.core.windows.persistence;


/**
 * Interface which defines observer of persistence changes.
 *
 * @author  Peter Zavadsky
 */
public interface PersistenceObserver {


    /** Handles adding mode to model.
     * @param modeConfig configuration data of added mode
     */
    public void modeConfigAdded(ModeConfig modeConfig);

    /** Handles removing mode from model.
     * @param modeName unique name of removed mode
     */
    public void modeConfigRemoved(String modeName);

    /** Handles adding tcRef to model. 
     * @param modeName unique name of parent mode.
     * @param tcRefConfig configuration data of added tcRef
     * @param tcRefNames array of tcIds to pass ordering of new tcRef,
     * if there is no ordering defined tcRef is appended to end of array
     */
    public void topComponentRefConfigAdded(
    String modeName, TCRefConfig tcRefConfig, String [] tcRefNames);
    
    /** Handles removing tcRef from model. 
     * @param tc_id unique id of removed tcRef
     */
    public void topComponentRefConfigRemoved(String tc_id);
    
    /** Handles adding group to model.
     * @param groupConfig configuration data of added group
     */
    public void groupConfigAdded(GroupConfig groupConfig);
    
    /** Handles removing group from model.
     * @param groupName unique name of removed group
     */
    public void groupConfigRemoved(String groupName);
    
    /** Handles adding tcGroup to model. 
     * @param groupName unique name of parent group
     * @param tcGroupConfig configuration data of added tcGroup
     */
    public void topComponentGroupConfigAdded(String groupName, TCGroupConfig tcGroupConfig);
    
    /** Handles removing tcGroup from model. 
     * @param groupName unique name of parent group.
     * @param tc_id unique id of removed tcGroup
     */
    public void topComponentGroupConfigRemoved(String groupName, String tc_id);
}

