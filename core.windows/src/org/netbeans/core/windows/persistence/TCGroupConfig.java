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
