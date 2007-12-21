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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.mapper.logging.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.bpel.model.api.support.EnumValue;
import org.netbeans.modules.bpel.model.ext.logging.api.AlertLevel;
import org.netbeans.modules.bpel.model.ext.logging.api.Location;
import org.netbeans.modules.bpel.model.ext.logging.api.LogLevel;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public enum LogAlertType {
    LOG,
    ALERT;
    
    private Map<Location, List<LoggingTreeItem>>  myChildren = new HashMap<Location, List<LoggingTreeItem>>();
    
    
    private LogAlertType() {
    }
    
    public List<LoggingTreeItem> getChildren(Location location) {
        assert location != null;
        if (myChildren.get(location) == null) {
            List<LoggingTreeItem> children = new ArrayList<LoggingTreeItem>();
            switch (this) {
                case LOG:
                    children = getLogLevelItems(location);
                    break;
                case ALERT:
                    children = getAlertLevelItems(location);
                    break;
            }
            myChildren.put(location, children);
        }
        return myChildren.get(location);
    }


    private List<LoggingTreeItem> getLogLevelItems(Location location) {
        List<LoggingTreeItem> items = new ArrayList<LoggingTreeItem>();
        
        LogLevel[] logLevels = LogLevel.values();
        for (LogLevel logLevel : logLevels) {
            if (LogLevel.INVALID.equals(logLevel)) {
                continue;
            }
            items.add(new LogItem(logLevel, location));
        }
        return items;
    }
    
    private List<LoggingTreeItem> getAlertLevelItems(Location location) {
        List<LoggingTreeItem> items = new ArrayList<LoggingTreeItem>();
        
        AlertLevel[] alertLevels = AlertLevel.values();
        for (AlertLevel alertLevel : alertLevels) {
            if (AlertLevel.INVALID.equals(alertLevel)) {
                continue;
            }
            items.add(new AlertItem(alertLevel, location));
        }
        return items;
    }
}
