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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** Tests behaviour of GlobalContextProviderImpl
 * and its cooperation with activated and current nodes.
 *
 * @author Jaroslav Tulach
 */
public class ContextAwareActionInTopComponentTest extends NbTestCase {

    private TopComponent tc;
    private MyContextAwareAction myGlobalAction = new MyContextAwareAction();
    private KeyStroke KEY_STROKE = KeyStroke.getKeyStroke( KeyEvent.VK_W, KeyEvent.ALT_DOWN_MASK+KeyEvent.CTRL_DOWN_MASK+KeyEvent.SHIFT_DOWN_MASK );

    public ContextAwareActionInTopComponentTest(java.lang.String testName) {
        super(testName);
    }
    
    protected void setUp () throws Exception {
        tc = new TopComponent ();
        tc.requestActive();
        
        MockServices.setServices( MyKeymap.class );
        Keymap km = Lookup.getDefault().lookup(Keymap.class);
        km.addActionForKeyStroke( KEY_STROKE, myGlobalAction );
    }
    
    public void testGlobalActionDisabled () throws Exception {
        myGlobalAction.setEnabled( false );
        
        final org.openide.nodes.Node n = new org.openide.nodes.AbstractNode (org.openide.nodes.Children.LEAF);
        tc.setActivatedNodes(new Node[] { n });
        
        KeyEvent e = new KeyEvent( tc, KeyEvent.KEY_TYPED, 0, 0, 0 );
        assertTrue( tc.processKeyBinding( KEY_STROKE, e, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, true ) );
        assertTrue( myGlobalAction.actionWasPerformed );
    }
    
    public void testGlobalActionSurvivedFocusChange() throws Exception {
        myGlobalAction.setEnabled( true );
        
        final org.openide.nodes.Node n = new org.openide.nodes.AbstractNode (org.openide.nodes.Children.LEAF);
        tc.setActivatedNodes(null);
        
        KeyEvent e = new KeyEvent( tc, KeyEvent.KEY_TYPED, 0, 0, 0 );
        assertTrue( tc.processKeyBinding( KEY_STROKE, e, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, true ) );
        assertTrue( myGlobalAction.actionWasPerformed );
    }
    
    public void testGlobalActionDoesNotSurviveFocusChange() throws Exception {
        myGlobalAction.setEnabled( true );
        
        final org.openide.nodes.Node n = new org.openide.nodes.AbstractNode (org.openide.nodes.Children.LEAF);
        tc.setActivatedNodes(new Node[0]);
        
        KeyEvent e = new KeyEvent( tc, KeyEvent.KEY_TYPED, 0, 0, 0 );
        assertTrue( tc.processKeyBinding( KEY_STROKE, e, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, true ) );
        assertFalse( myGlobalAction.actionWasPerformed );
    }
    
    /**
     * Context-aware action that is enabled only if there are any activated nodes.
     * 
     */
    private static class MyContextAwareAction extends AbstractAction implements ContextAwareAction {
        
        private static boolean actionWasPerformed = false;
        
        public MyContextAwareAction() {
            actionWasPerformed = false;
        }
    
        public void actionPerformed(ActionEvent arg0) {
            actionWasPerformed = true;
        }

        public Action createContextAwareInstance(Lookup actionContext) {
            MyContextAwareAction action = new MyContextAwareAction();
            action.setEnabled( null != actionContext.lookup( Node.class ) );
            return action;
        }
    }
    
    public static class MyKeymap implements Keymap {
        
        private Map<KeyStroke, Action> ks2a = new HashMap<KeyStroke, Action>();
        
        public String getName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Action getDefaultAction() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setDefaultAction(Action arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Action getAction(KeyStroke arg0) {
            return ks2a.get( arg0 );
        }

        public KeyStroke[] getBoundKeyStrokes() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Action[] getBoundActions() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public KeyStroke[] getKeyStrokesForAction(Action arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isLocallyDefined(KeyStroke arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addActionForKeyStroke(KeyStroke arg0, Action arg1) {
            ks2a.put( arg0, arg1 );
        }

        public void removeKeyStrokeBinding(KeyStroke arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeBindings() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Keymap getResolveParent() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setResolveParent(Keymap arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
}
}
