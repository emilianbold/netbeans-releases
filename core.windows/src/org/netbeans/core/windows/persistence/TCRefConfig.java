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
 * Class of reference of TopComponent in mode config properties for communication 
 * with persistence management.
 * It keeps data which are read/written from/in .wstcref xml file.
 *
 * @author  Peter Zavadsky
 */
public class TCRefConfig {
    
    /** Reference to TopComponent by its unique Id. */
    public String tc_id;
    
    /** Is TopComponent opened. */
    public boolean opened;
    
    public String previousMode;
    
    /** Creates a new instance of TCRefConfig */
    public TCRefConfig() {
        tc_id = ""; // NOI18N
    }
    
    public boolean equals (Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TCRefConfig) {
            TCRefConfig tcRefCfg = (TCRefConfig) obj;
            return (tc_id.equals(tcRefCfg.tc_id) &&
                   (opened == tcRefCfg.opened));
        }
        return false;
    }
    
    public int hashCode() {
        int hash = 17;
        hash = 37 * hash + tc_id.hashCode();
        hash = 37 * hash + (opened ? 0 : 1);
        return hash;
    }
    
}
