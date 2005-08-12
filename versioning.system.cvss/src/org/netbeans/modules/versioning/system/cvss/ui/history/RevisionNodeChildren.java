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

package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.netbeans.lib.cvsclient.command.log.LogInformation;

import java.util.*;

/**
 * Represents children of a Revision Node in Search history results table.
 * 
 * @author Maros Sandor
 */
class RevisionNodeChildren extends Children.Keys {
    
    private final SearchHistoryPanel.ResultsContainer    container;

    public RevisionNodeChildren(SearchHistoryPanel.ResultsContainer container) {
        this.container = container;
    }

    protected void addNotify() {
        refreshKeys();
    }

    protected void removeNotify() {
        setKeys (Collections.EMPTY_SET);
    }
    
    private void refreshKeys() {
        setKeys(container.getRevisions());
    }
    
    protected Node[] createNodes(Object key) {
        LogInformation.Revision fn = (LogInformation.Revision) key;
        RevisionNode node = new RevisionNode(fn);
        return new Node[] { node };
    }

    public void refreshChildren() {
        refreshKeys();
    }
}
