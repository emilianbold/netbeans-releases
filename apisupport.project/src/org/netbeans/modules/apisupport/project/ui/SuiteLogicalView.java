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

package org.netbeans.modules.apisupport.project.ui;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.loaders.DataFolder;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.netbeans.modules.apisupport.project.suite.*;

/**
 * Explorer view of suites.
 * @author Jesse Glick
 */
public class SuiteLogicalView implements LogicalViewProvider {
    
    private final SuiteProject project;
    
    public SuiteLogicalView(SuiteProject project) {
        this.project = project;
    }

    public Node createLogicalView() {
        // XXX get fancier...
        Node basic = DataFolder.findFolder(project.getProjectDirectory()).getNodeDelegate();
        return new SuiteRootNode(basic);
    }
    
    public Node findPath(Node root, Object target) {
        // XXX
        return null;
    }
    
    private final class SuiteRootNode extends FilterNode {
        
        public SuiteRootNode(Node basic) {
            super(basic, new FilterNode.Children(basic),
                  new ProxyLookup(new Lookup[] {Lookups.singleton(project), basic.getLookup()}));
        }

        public Action[] getActions(boolean context) {
            return SuiteActions.getProjectActions(project);
        }

        public Image getIcon(int type) {
            return Utilities.loadImage("org/netbeans/modules/apisupport/project/suite/resources/suite.gif", true); // NOI18N
        }

        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

    }

}
