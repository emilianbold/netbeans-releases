/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.openide.explorer.view;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import javax.swing.JPanel;
import org.netbeans.junit.NbTestCase;
import org.netbeans.swing.etable.ETableColumn;
import org.netbeans.swing.etable.ETableColumnModel;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;

/**
 *
 * @author  Jiri Rechtacek
 */
public final class OutlineViewTest extends NbTestCase {

    private OutlineViewComponent component;
    private OutlineView view;
    private Node toExpand200_299,  toExpand0_99;

    public OutlineViewTest (String testName) {
        super (testName);
    }

    @Override
    protected boolean runInEQ () {
        return true;
    }

    @Override
    public void setUp () {
        TestNode[] childrenNodes = new TestNode[3];
        for (int i = 0; i < childrenNodes.length; i ++) {
            TestNode[] leafNodes = new TestNode[100];
            for (int j = 0; j < leafNodes.length; j ++) {
                leafNodes[j] = new TestNode ("[" + (100 * i + j) + "]");
            }
            Children.Array leafs = new Children.Array ();
            leafs.add (leafNodes);
            //childrenNodes [i] = new TestNode (leafs, "[" + (i * 100) + "-" + ((i + 1) *100 - 1) + "]");
            switch (i) {
                case 0:
                    childrenNodes[i] = new TestNode (leafs, "[1-index from 0 to 99]");
                    break;
                case 1:
                    childrenNodes[i] = new TestNode (leafs, "[10-index from 100 to 199]");
                    break;
                case 2:
                    childrenNodes[i] = new TestNode (leafs, "[2-index from 200 to 299]");
                    break;
                default:
                    fail ("Unexcepted value " + i);
            }
            if (toExpand0_99 == null) {
                toExpand0_99 = childrenNodes[i];
            }
            toExpand200_299 = childrenNodes[i];
        }

        Children.Array children = new Children.Array ();
        children.add (childrenNodes);

        Node rootNode = new TestNode (children, "[0 - 1000]");

        component = new OutlineViewComponent (rootNode);
        view = component.getOutlineView ();
    }

    public void testNaturallySortingTree () throws InterruptedException, IllegalAccessException, InvocationTargetException {
        view.expandNode (toExpand0_99);

        // should look like
        // - [1-index from 0 to 99]
        //   [0]
        //   [1]
        //   [2]
        //   ....
        // + [10-index from 100 to 199]
        // + [2-index from 200 to 299]
        assertEquals ("[1-index from 0 to 99]", view.getOutline ().getValueAt (0, 0).toString ());
        assertEquals ("[0]", view.getOutline ().getValueAt (1, 0).toString ());
        assertEquals ("[1]", view.getOutline ().getValueAt (2, 0).toString ());
        assertEquals ("[2]", view.getOutline ().getValueAt (3, 0).toString ());
        assertEquals ("[10-index from 100 to 199]", view.getOutline ().getValueAt (101, 0).toString ());
        assertEquals ("[2-index from 200 to 299]", view.getOutline ().getValueAt (102, 0).toString ());
    }

    public void testDescendingSortingTreeWithNaturallyStringOrdering () throws InterruptedException, IllegalAccessException, InvocationTargetException {
        ETableColumnModel etcm = (ETableColumnModel) view.getOutline ().getColumnModel ();
        ETableColumn etc = (ETableColumn) etcm.getColumn (0); // tree column
        etcm.setColumnSorted (etc, false, 1); // descending order
        etc.setNestedComparator (testComarator);
        view.expandNode (toExpand200_299);

//        org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor (component, "", true, null);
//        java.awt.Dialog d = org.openide.DialogDisplayer.getDefault ().createDialog (dd);
//        d.setVisible (true);

        // should look like
        // - [2-index from 200 to 299]
        //   [299]
        //   [298]
        // + [10-index from 100 to 199]
        //   ....
        // + [1-index from 0 to 99]
        assertEquals ("[2-index from 200 to 299]", view.getOutline ().getValueAt (1, 0).toString ());
        assertEquals ("[10-index from 100 to 199]", view.getOutline ().getValueAt (0, 0).toString ());
        assertEquals ("[299]", view.getOutline ().getValueAt (2, 0).toString ());
        assertEquals ("[298]", view.getOutline ().getValueAt (3, 0).toString ());
    }

    public void testAscendingSortingTreeWithNaturallyStringOrdering () throws InterruptedException, IllegalAccessException, InvocationTargetException {
        ETableColumnModel etcm = (ETableColumnModel) view.getOutline ().getColumnModel ();
        ETableColumn etc = (ETableColumn) etcm.getColumn (0); // tree column
        etcm.setColumnSorted (etc, true, 1); // ascending order
        view.expandNode (toExpand0_99);

//        org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor (component, "", true, null);
//        java.awt.Dialog d = org.openide.DialogDisplayer.getDefault ().createDialog (dd);
//        d.setVisible (true);

        // should look like
        // - [1-index from 0 to 99]
        //   [0]
        //   [10]
        //   [11]
        //   ....
        // + [10-index from 100 to 199]
        // + [2-index from 200 to 299]
        assertEquals ("[1-index from 0 to 99]", view.getOutline ().getValueAt (0, 0).toString ());
        assertEquals ("[0]", view.getOutline ().getValueAt (1, 0).toString ());
        assertEquals ("[10]", view.getOutline ().getValueAt (2, 0).toString ());
        assertEquals ("[11]", view.getOutline ().getValueAt (3, 0).toString ());
        assertEquals ("[10-index from 100 to 199]", view.getOutline ().getValueAt (101, 0).toString ());
        assertEquals ("[2-index from 200 to 299]", view.getOutline ().getValueAt (102, 0).toString ());
    }

    public void testDescendingSortingTreeWithCustomComparator () throws InterruptedException, IllegalAccessException, InvocationTargetException {
        ETableColumnModel etcm = (ETableColumnModel) view.getOutline ().getColumnModel ();
        ETableColumn etc = (ETableColumn) etcm.getColumn (0); // tree column
        etc.setNestedComparator (testComarator);
        etcm.setColumnSorted (etc, false, 1); // descending order
        view.expandNode (toExpand200_299);

//        org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor (component, "", true, null);
//        java.awt.Dialog d = org.openide.DialogDisplayer.getDefault ().createDialog (dd);
//        d.setVisible (true);

        // should look like
        // + [10-index from 100 to 199]
        // - [2-index from 200 to 299]
        //   [299]
        //   [298]
        //   ....
        // + [1-index from 0 to 99]
        assertEquals ("[10-index from 100 to 199]", view.getOutline ().getValueAt (0, 0).toString ());
        assertEquals ("[2-index from 200 to 299]", view.getOutline ().getValueAt (1, 0).toString ());
        assertEquals ("[299]", view.getOutline ().getValueAt (2, 0).toString ());
        assertEquals ("[298]", view.getOutline ().getValueAt (3, 0).toString ());
    }

    public void testAscendingSortingTreeWithCustomComparator () throws InterruptedException, IllegalAccessException, InvocationTargetException {
        ETableColumnModel etcm = (ETableColumnModel) view.getOutline ().getColumnModel ();
        ETableColumn etc = (ETableColumn) etcm.getColumn (0); // tree column
        etc.setNestedComparator (testComarator);
        etcm.setColumnSorted (etc, true, 1); // ascending order

//        org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor (component, "", true, null);
//        java.awt.Dialog d = org.openide.DialogDisplayer.getDefault ().createDialog (dd);
//        d.setVisible (true);

        view.expandNode (toExpand0_99);

        // should look like
        // - [1-index from 0 to 99]
        //   [0]
        //   [1]
        //   [2]
        //   ....
        // + [2-index from 200 to 299]
        // + [10-index from 100 to 199]
        assertEquals ("[1-index from 0 to 99]", view.getOutline ().getValueAt (0, 0).toString ());
        assertEquals ("[0]", view.getOutline ().getValueAt (1, 0).toString ());
        assertEquals ("[1]", view.getOutline ().getValueAt (2, 0).toString ());
        assertEquals ("[2]", view.getOutline ().getValueAt (3, 0).toString ());
        assertEquals ("[2-index from 200 to 299]", view.getOutline ().getValueAt (101, 0).toString ());
    }

    private class OutlineViewComponent extends JPanel implements ExplorerManager.Provider {

        private final ExplorerManager manager = new ExplorerManager ();
        private OutlineView view;

        private OutlineViewComponent (Node rootNode) {
            setLayout (new BorderLayout ());
            manager.setRootContext (rootNode);

            Node.Property[] props = rootNode.getPropertySets ()[0].getProperties ();
            view = new OutlineView ("test-outline-view-component");
            view.setProperties (props);

            view.getOutline ().setRootVisible (false);

            add (view, BorderLayout.CENTER);
        }

        public ExplorerManager getExplorerManager () {
            return manager;
        }

        public OutlineView getOutlineView () {
            return view;
        }
    }

    static class TestNode extends AbstractNode {

        public TestNode (String name) {
            super (Children.LEAF);
            setName (name);
        }

        public TestNode (Children children, String name) {
            super (children);
            setName (name);
        }

        @Override
        protected Sheet createSheet () {
            Sheet s = super.createSheet ();
            Sheet.Set ss = s.get (Sheet.PROPERTIES);
            if (ss == null) {
                ss = Sheet.createPropertiesSet ();
                s.put (ss);
            }
            ss.put (new DummyProperty (getName ()));
            return s;
        }

        void forcePropertyChangeEvent () {
            firePropertyChange ("unitTestPropName", null, new Object ());
        }

        class DummyProperty extends Property<String> {

            public DummyProperty (String val) {
                super (String.class);
                setName ("unitTestPropName");
                try {
                    setValue (val);
                } catch (IllegalAccessException ex) {
                    Exceptions.printStackTrace (ex);
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace (ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace (ex);
                }
            }

            public boolean canRead () {
                return true;
            }

            public String getValue () throws IllegalAccessException,
                    InvocationTargetException {
                return (String) getValue ("unitTestPropName");
            }

            public boolean canWrite () {
                return true;
            }

            public void setValue (String val) throws IllegalAccessException,
                    IllegalArgumentException,
                    InvocationTargetException {
                setValue ("unitTestPropName", val);
            }
        }
    }

    private Comparator testComarator = new Comparator () {

        public int compare (Object o1, Object o2) {
            assertTrue (o1 + " instanceof String", o1 instanceof Node);
            assertTrue (o2 + " instanceof String", o2 instanceof Node);
            Node n1 = (Node) o1;
            Node n2 = (Node) o2;

            // my comparator
            return getInteger (n1.getDisplayName ()).compareTo (getInteger (n2.getDisplayName ()));
        }

        private Integer getInteger (Object o) {
            String s = o.toString ();
            assertTrue (s + "startsWith (\"[\") && s.endsWith (\"]\")", s.startsWith ("[") && s.endsWith ("]"));
            int end = s.indexOf ("-");
            if (end != -1) {
                s = s.substring (1, end);
            } else {
                s = s.substring (1, s.length () - 1);
            }
            //System.out.println ("###: " + o.toString () + " => " + Integer.parseInt (s));
            return Integer.parseInt (s);
        }
    };
}
