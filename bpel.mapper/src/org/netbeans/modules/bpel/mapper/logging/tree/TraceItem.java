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

import java.util.List;
import javax.swing.Icon;
import org.netbeans.modules.bpel.model.ext.logging.api.Location;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TraceItem implements LoggingTreeItem {

    private Location myLocation;
    private LogAlertType myType;
    private List<LoggingTreeItem> myChildren;
    
    public static final TraceItem[] TRACE_ITEMS = new TraceItem[4];
    
    static {
        TRACE_ITEMS[0] = new TraceItem(Location.ON_START, LogAlertType.LOG);
        TRACE_ITEMS[1] = new TraceItem(Location.ON_START, LogAlertType.ALERT);
        TRACE_ITEMS[2] = new TraceItem(Location.ON_COMPLETE, LogAlertType.LOG);
        TRACE_ITEMS[3] = new TraceItem(Location.ON_COMPLETE, LogAlertType.ALERT);
    }
    
    public TraceItem(Location location, LogAlertType type) {
        assert location != null && type != null;
        myLocation = location;
        myType = type;
    }
    
    public boolean isLeaf() {
        return false;
    }

    public Icon getIcon() {
        return null;
    }

    public String getDisplayName() {
        return getName();
    }

    public String getName() {
        return NbBundle.getMessage(LoggingTreeItem.class, "LBL_TraceItem", 
                myType.toString(), myLocation.toString());
    }

    public List<LoggingTreeItem> getChildren() {
        if (myChildren == null) {
            myChildren = myType.getChildren(myLocation);
        }
        
        return myChildren;
    }
}
