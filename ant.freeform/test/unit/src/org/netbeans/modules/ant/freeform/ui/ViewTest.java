/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform.ui;

import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ant.freeform.FreeformProjectType;
import org.netbeans.modules.ant.freeform.TestBase;
import org.netbeans.modules.ant.freeform.Util;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// XXX testFindPath
// XXX testRootNodeDisplayNameChange

/**
 * Test {@link View}: changes in children etc.
 * @author Jesse Glick
 */
public class ViewTest extends TestBase {
    
    public ViewTest(String name) {
        super(name);
    }
    
    private LogicalViewProvider lpp;
    
    protected void setUp() throws Exception {
        super.setUp();
        lpp = (LogicalViewProvider) extsrcroot.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("found a LogicalViewProvider", lpp);
    }
    
    public void testViewItemBasic() throws Exception {
        Node root = lpp.createLogicalView();
        assertEquals("lookup has project", extsrcroot, root.getLookup().lookup(Project.class));
        Children ch = root.getChildren();
        Node[] kids = ch.getNodes(true);
        assertEquals("two child nodes", 2, kids.length);
        assertEquals("correct code name #1", "../src", kids[0].getName());
        assertEquals("correct display name #1", "External Sources", kids[0].getDisplayName());
        assertEquals("correct cookie #1",
            DataObject.find(egdirFO.getFileObject("extsrcroot/src")),
            kids[0].getLookup().lookup(DataObject.class));
        Node[] kids2 = kids[0].getChildren().getNodes(true);
        assertEquals("one child of ../src", 1, kids2.length);
        assertEquals("correct name of #1's kid", "org.foo", kids2[0].getName());
        assertEquals("correct code name #2", "nbproject/project.xml", kids[1].getName());
        assertEquals("correct display name #2", "project.xml", kids[1].getDisplayName());
        assertEquals("correct cookie #2",
            DataObject.find(egdirFO.getFileObject("extsrcroot/proj/nbproject/project.xml")),
            kids[1].getLookup().lookup(DataObject.class));
    }
    
    public void testViewItemChanges() throws Exception {
        Node root = lpp.createLogicalView();
        Children ch = root.getChildren();
        Node[] kids = ch.getNodes(true);
        assertEquals("two child nodes", 2, kids.length);
        assertEquals("correct code name #1", "../src", kids[0].getName());
        assertEquals("correct code name #2", "nbproject/project.xml", kids[1].getName());
        TestNL l = new TestNL();
        root.addNodeListener(l);
        Element data = extsrcroot.helper().getPrimaryConfigurationData(true);
        Element view = Util.findElement(data, "view", FreeformProjectType.NS_GENERAL);
        assertNotNull("have <view>", view);
        Element items = Util.findElement(view, "items", FreeformProjectType.NS_GENERAL);
        assertNotNull("have <items>", items);
        Element sourceFolder = Util.findElement(items, "source-folder", FreeformProjectType.NS_GENERAL);
        assertNotNull("have <source-folder>", sourceFolder);
        Element location = Util.findElement(sourceFolder, "location", FreeformProjectType.NS_GENERAL);
        assertNotNull("have <location>", location);
        NodeList nl = location.getChildNodes();
        assertEquals("one child", 1, nl.getLength());
        location.removeChild(nl.item(0));
        location.appendChild(location.getOwnerDocument().createTextNode("../src2"));
        Element sourceFile =  Util.findElement(items, "source-file", FreeformProjectType.NS_GENERAL);
        assertNotNull("have <source-file>", sourceFile);
        items.removeChild(sourceFile);
        extsrcroot.helper().putPrimaryConfigurationData(data, true);
        assertFalse("got some changes in children", l.probeChanges().isEmpty());
        kids = ch.getNodes(true);
        assertEquals("one child node", 1, kids.length);
        assertEquals("correct code name #1", "../src2", kids[0].getName());
        assertEquals("correct display name #1", "External Sources", kids[0].getDisplayName());
        assertEquals("correct cookie #1",
            DataObject.find(egdirFO.getFileObject("extsrcroot/src2")),
            kids[0].getLookup().lookup(DataObject.class));
    }
    
    private static final class TestNL implements NodeListener {
        private final Set/*<String>*/ changes = new HashSet();
        public TestNL() {}
        public synchronized void childrenRemoved(NodeMemberEvent ev) {
            changes.add("childrenRemoved");
        }
        public synchronized void childrenAdded(NodeMemberEvent ev) {
            changes.add("childrenAdded");
        }
        public synchronized void childrenReordered(NodeReorderEvent ev) {
            changes.add("childrenReordered");
        }
        public synchronized void nodeDestroyed(NodeEvent ev) {
            changes.add("nodeDestroyed");
        }
        public synchronized void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            changes.add(propertyChangeEvent.getPropertyName());
        }
        /** Get a set of all change event names since the last call. Clears set too. */
        public synchronized Set/*<String>*/ probeChanges() {
            Set/*<String>*/ _changes = new HashSet(changes);
            changes.clear();
            return _changes;
        }
    }

}
