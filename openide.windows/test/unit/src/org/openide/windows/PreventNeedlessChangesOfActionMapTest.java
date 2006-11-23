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
package org.openide.windows;

import javax.swing.ActionMap;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Jaroslav Tulach
 */
public class PreventNeedlessChangesOfActionMapTest extends NbTestCase
implements LookupListener {
    private TopComponent tc;
    private Lookup.Result res;
    private int cnt;
    
    /** Creates a new instance of PreventNeedlessChangesOfActionMapTest */
    public PreventNeedlessChangesOfActionMapTest(String s) {
        super(s);
    }
    
    protected void setUp() throws Exception {
        tc = new TopComponent();
        res = tc.getLookup().lookup(new Lookup.Template<ActionMap> (ActionMap.class));
        assertEquals("One instance", 1, res.allItems().size());
        
        res.addLookupListener(this);
    }
    
    public void testChangeOfNodeDoesNotFireChangeInActionMap() {
        ActionMap am = (ActionMap)tc.getLookup().lookup(ActionMap.class);
        assertNotNull(am);
        
        Node m1 = new AbstractNode(Children.LEAF);
        m1.setName("old m1");
        Node m2 = new AbstractNode(Children.LEAF);
        m2.setName("new m2");
        
        tc.setActivatedNodes(new Node[] { m1 });
        assertEquals("No change in ActionMap 1", 0, cnt);
        tc.setActivatedNodes(new Node[] { m2 });
        assertEquals("No change in ActionMap 2", 0, cnt);
        tc.setActivatedNodes(new Node[0]);
        assertEquals("No change in ActionMap 3", 0, cnt);
        tc.setActivatedNodes(null);
        assertEquals("No change in ActionMap 4", 0, cnt);
        
        ActionMap am2 = (ActionMap)tc.getLookup().lookup(ActionMap.class);
        assertEquals("Still the same action map", am, am2);
    }

    public void resultChanged(LookupEvent ev) {
        cnt++;
    }
    
}
