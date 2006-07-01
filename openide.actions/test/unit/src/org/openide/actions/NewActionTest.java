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
package org.openide.actions;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.Action;
import javax.swing.JMenuItem;
import junit.framework.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.actions.Presenter;

public class NewActionTest extends TestCase {

    public NewActionTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testTheNewTypesMethodIsCalledJustOnceIssue65534() throws Exception {
        class N extends AbstractNode {
            int cnt;
            StringWriter w = new StringWriter();
            PrintWriter p = new PrintWriter(w);
            
            public N() {
                super(Children.LEAF);
            }

            public org.openide.util.datatransfer.NewType[] getNewTypes() {
                cnt++;
                org.openide.util.datatransfer.NewType[] retValue;
                
                new Exception("Call " + cnt).printStackTrace(p);

                retValue = super.getNewTypes();
                return retValue;
            }
        }
        
        
        N node = new N();
        
        NewAction a = (NewAction)NewAction.get(NewAction.class);
        
        Action clone = a.createContextAwareInstance(node.getLookup());
        
        if (!(clone instanceof Presenter.Popup)) {
            fail("Does not implement popup: " + clone);
        }
        
        Presenter.Popup p = (Presenter.Popup)clone;
        
        JMenuItem m = p.getPopupPresenter();
        String name = m.getName();
        assertEquals("Just one call to getNewTypes\n" + node.w, 1, node.cnt);
    }
}


