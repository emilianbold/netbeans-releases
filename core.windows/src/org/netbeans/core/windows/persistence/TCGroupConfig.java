/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.persistence;


/**
 * Class of config properties of reference of TopComponent in group for communication
 * with persistence management.
 * It keeps data which are read/written from/in .wstcgrp xml file.
 *
 * @author  Peter Zavadsky
 */
public class TCGroupConfig {
    
    /** Reference to TopComponent by its unique Id. */
    public String tc_id;
    
    /** Should TopComponent be opened when group is being opened. */
    public boolean open;
    
    /** Should TopComponent be closed when group is being closed. */
    public boolean close;
    
    /** Whether the TopComponent was opened at the time the group was opening.
     * It is relevant only in case group state opened is true. */
    public boolean wasOpened;
    
    
    /** Creates a new instance of TCGroupConfig */
    public TCGroupConfig() {
        tc_id = ""; // NOI18N
    }
    
    public boolean equals (Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TCGroupConfig) {
            TCGroupConfig tcGroupCfg = (TCGroupConfig) obj;
            return tc_id.equals(tcGroupCfg.tc_id) &&
                   (open == tcGroupCfg.open) &&
                   (close == tcGroupCfg.close) &&
                   (wasOpened == tcGroupCfg.wasOpened);
        }
        return false;
    }
    
    public int hashCode() {
        int hash = 17;
        hash = 37 * hash + tc_id.hashCode();
        hash = 37 * hash + (open ? 0 : 1);
        hash = 37 * hash + (close ? 0 : 1);
        hash = 37 * hash + (wasOpened ? 0 : 1);
        return hash;
    }
    
}
