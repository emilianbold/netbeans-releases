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

package org.openide.actions;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import javax.swing.Action;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;

/** Issue 68299.
 *
 * @author Jiri Rechtacek
 */
public class PropertiesActionTest extends NbTestCase {
    public PropertiesActionTest (String testName) {
        super (testName);
    }

    protected void setUp () throws Exception {
    }

    protected void tearDown () throws Exception {
    }

    public void testEnableOnEmptyProperties () throws Exception {
        testEnable (new PropertySet [0]);
    }

    public void testEnableOnNullProperties () throws Exception {
        testEnable (null);
    }
    
    public void testEnableOnNotNullProperties () throws Exception {
        PropertySet [] s = new PropertySet [] { new PropertySet () {
                        public Property[] getProperties () {
                            Property p = new Property (String.class) {
                                public boolean canRead () {
                                    return true;
                                }
                                public boolean canWrite () {
                                    return true;
                                }
                                public Object getValue () throws IllegalAccessException,InvocationTargetException {
                                    return null;
                                }
                                public void setValue (Object val) throws IllegalAccessException,IllegalArgumentException,InvocationTargetException {
                                }
                            };
                            return new Property [] { p };
                        }
                    } };

        testEnable (s);
    }
    
    private void testEnable (final PropertySet [] pros) throws Exception {
        Node n = new AbstractNode (Children.LEAF) {
            public PropertySet [] getPropertySets () {
                return pros;
            }
        };
        
        
        assertEquals ("Node has the given properties.", pros, n.getPropertySets ());
        
        Node[] activatedNodes = new Node [] { n };
        
        PropertiesAction pa = (PropertiesAction) PropertiesAction.get (PropertiesAction.class);
        Action a = pa.createContextAwareInstance (n.getLookup ());
        
        assertTrue ("PropertiesAction is enabled.", a.isEnabled ());
    }
    
}
