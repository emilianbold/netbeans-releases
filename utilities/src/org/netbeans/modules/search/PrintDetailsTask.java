/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.nodes.Node;
import org.openidex.search.SearchGroup;
import org.openidex.search.SearchType;

/**
 *
 * @author  Marian Petras
 */
public class PrintDetailsTask implements Runnable {
    
    /** */
    private final ResultTreeChildren children;
    /** */
    private final SearchGroup searchGroup;
    /** */
    private final SearchDisplayer displayer;
    /** */
    private final boolean needsReset;
    
    /** Creates a new instance of PrintDetailsTask */
    public PrintDetailsTask(final ResultTreeChildren children,
                            final SearchGroup searchGroup,
                            final SearchDisplayer displayer,
                            final boolean needsReset) {
        this.children = children;
        this.searchGroup = searchGroup;
        this.displayer = displayer;
        this.needsReset = needsReset;
    }
    
    /** */
    public void run() {
        if (needsReset) {
            displayer.resetOutput();
        }
        
        Node[] nodes = children.getNodes();
        SearchType[] searchTypes = searchGroup.getSearchTypes();        
        List detailNodes = new ArrayList(nodes.length * searchTypes.length * 3);
        
        for (int i = 0; i < searchTypes.length; i++) {
            for (int j = 0; j < nodes.length; j++) {
                Node[] details = searchTypes[i].getDetails(nodes[j]);
                if (details != null) {
                    detailNodes.addAll(Arrays.asList(details));
                }
            }
        }
        displayer.acceptNodes((Node[])
                             detailNodes.toArray(new Node[detailNodes.size()]));
    }
    
}
