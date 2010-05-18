/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;



/**
 * @author Some Czech
 */
public class SheetTest extends NbTestCase {

    public SheetTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(SheetTest.class));
    }

    public void testSheetEvents() {

        AbstractNode node = new AbstractNode( Children.LEAF );
        
        Sheet sheet = node.getSheet();
        
        SheetListener sl = new SheetListener();
        TestNodeListener tnl = new TestNodeListener();
        
        node.addNodeListener( tnl );
        node.addPropertyChangeListener( sl );
                
        Sheet.Set ss = new Sheet.Set();
        ss.setName("Karel");
        sheet.put( ss );
        
        tnl.assertEvents( "NodePropertySets change", new PropertyChangeEvent[] {
            new PropertyChangeEvent( node, Node.PROP_PROPERTY_SETS, null, null )
        } );
        
        sl.assertEvents( "No events", new PropertyChangeEvent[] {} ); 
        
        PropertySupport.Name prop = new PropertySupport.Name (node);
        ss.put (prop);
        
        tnl.assertEvents( "NodePropertySets change again", new PropertyChangeEvent[] {
            new PropertyChangeEvent( node, Node.PROP_PROPERTY_SETS, null, null )
        } );
        
        sl.assertEvents( "No events fired", new PropertyChangeEvent[] {} ); 
        
        sheet.remove(ss.getName());
        
        tnl.assertEvents( "NodePropertySets change", new PropertyChangeEvent[] {
            new PropertyChangeEvent( node, Node.PROP_PROPERTY_SETS, null, null )
        } );
        
        sl.assertEvents( "No events", new PropertyChangeEvent[] {} ); 
        
        ss.remove (prop.getName());
        
        tnl.assertEvents( "No change in Node, as the set is removed", new PropertyChangeEvent[] {} );
        sl.assertEvents( "No events fired", new PropertyChangeEvent[] {} ); 
    }
    
    
    private static class SheetListener implements PropertyChangeListener {
        
        List events = new ArrayList();
        
        public void propertyChange(java.beans.PropertyChangeEvent evt) {            
            events.add( evt );            
        }
        
        public void assertEvents( String message, PropertyChangeEvent[] pevents ) {
            
            if ( events.size() != pevents.length ) {
                fail( message );
            }
            
            int i = 0;
            for( Iterator it = events.iterator(); it.hasNext(); i++ ) {
                PropertyChangeEvent pche = (PropertyChangeEvent)it.next();
                assertEquals( message + " [" + i + "] ", pevents[i].getSource(), pche.getSource ());
                assertEquals( message + " [" + i + "] ", pevents[i].getPropertyName(), pche.getPropertyName());
                assertEquals( message + " [" + i + "] ", pevents[i].getOldValue (), pche.getOldValue());
                assertEquals( message + " [" + i + "] ", pevents[i].getNewValue(), pche.getNewValue ());
                assertEquals( message + " [" + i + "] ", pevents[i].getPropagationId(), pche.getPropagationId());
            }
            
            events.clear();
        }
        
    }
    
    private static class TestNodeListener extends SheetListener implements NodeListener {
        
        public void childrenAdded(org.openide.nodes.NodeMemberEvent ev) {
        }
        
        public void childrenRemoved(org.openide.nodes.NodeMemberEvent ev) {
        }
        
        public void childrenReordered(org.openide.nodes.NodeReorderEvent ev) {
        }
        
        public void nodeDestroyed(org.openide.nodes.NodeEvent ev) {
        }
        
    }

}
