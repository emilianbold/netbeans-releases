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
package org.netbeans.modules.bpel.mapper.tree.search;

import org.netbeans.modules.soa.ui.tree.impl.SimpleFinder;
import org.netbeans.modules.bpel.mapper.logging.tree.AlertItem;
import org.netbeans.modules.bpel.mapper.logging.tree.LogAlertType;
import org.netbeans.modules.bpel.mapper.logging.tree.LogItem;
import org.netbeans.modules.bpel.mapper.logging.tree.TraceItem;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.ext.logging.api.Location;

/**
 *
 * @author Vitaly Bychkov 
 * @version 1.0
 */
public class LoggingNodeFinder extends SimpleFinder {

    private LogAlertType myType;
    private Location myLocation;
    private Object myLevel;
    
    public LoggingNodeFinder(LogAlertType type, Location location, Object level) {
        myType = type;
        myLocation = location;
        myLevel = level;
    }
    
    protected boolean isFit(Object treeItem) {
        if (treeItem instanceof LogItem &&  LogAlertType.LOG.equals(myType)) {
            LogItem logI = (LogItem)treeItem;
             // found!!!
            return myLevel != null && myLevel.equals(logI.getLevel()) 
                    && myLocation != null && myLocation.equals(logI.getLocation());
            
        }
        if (treeItem instanceof AlertItem &&  LogAlertType.ALERT.equals(myType)) {
            AlertItem alertI = (AlertItem)treeItem;
             // found!!!
            return myLevel != null && myLevel.equals(alertI.getLevel()) 
                    && myLocation != null && myLocation.equals(alertI.getLocation());
            
        }
        return false;
    }

    protected boolean drillDeeper(Object treeItem) {
        if (treeItem instanceof TraceItem || treeItem instanceof BpelEntity) {
            return true;
        }
        return false;
    }

}
