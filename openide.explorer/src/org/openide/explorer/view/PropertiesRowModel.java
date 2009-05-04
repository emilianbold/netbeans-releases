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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.WeakHashMap;
import javax.swing.JLabel;
import javax.swing.event.TableModelEvent;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RowModel;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;

/**
 *
 * @author David Strupl
 */
class PropertiesRowModel implements RowModel {
   
    private Node.Property[] prop = new Node.Property[0];
    private Outline outline;
    private WeakHashMap<Node, PropertyChangeListener> nodesListenersCache = new WeakHashMap<Node, PropertyChangeListener> ();
    private String [] names = new String [prop.length];
    private String [] descs = new String [prop.length];

    
    /** listener on node properties changes, recreates displayed data */
    private PropertyChangeListener pcl = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            //fireTableDataChanged();
            int row = rowForNode((Node)evt.getSource());
            if (row == -1) {
                return;
            }

            int column = columnForProperty(evt.getPropertyName());
            if (column == -1) {
                outline.tableChanged(new TableModelEvent(outline.getModel(), row, row,
                             TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
            } else {
                outline.tableChanged(new TableModelEvent(outline.getModel(), row, row,
                             column+1, TableModelEvent.UPDATE));
            }
        }
    };
    
    /** Creates a new instance of PropertiesRowModel */
    public PropertiesRowModel() {
        
    }
    
    public void setOutline(Outline outline) {
        this.outline = outline;
    }
    
    private int rowForNode(Node n) {
        TreeNode tn = Visualizer.findVisualizer(n);
        if (tn != null) {
            ArrayList<TreeNode> al = new ArrayList<TreeNode> ();
            while (tn != null) {
                al.add(tn);
                tn = tn.getParent();
            }
            Collections.reverse(al);
            TreePath tp = new TreePath(al.toArray());
            int row = -1;
            try {
                row = outline.getLayoutCache().getRowForPath(tp);
            } catch (EmptyStackException ese) {
                // ignore; see issue 157888
            }
            return row;
        }
        return -1;
    }

    public Class getColumnClass(int column) {
        return Node.Property.class;
    }

    public int getColumnCount() {
        return prop.length;
    }

    public String getColumnName(int column) {
        assert column < prop.length : column + " must be bellow " + prop.length;
        if (names [column] == null) {
            String n = prop[column].getDisplayName ();
            JLabel l = new JLabel ();
            Mnemonics.setLocalizedText (l, n);
            names [column] = l.getText ();
        }
        return names [column];
    }

    public String getShortDescription (int column) {
        assert column < prop.length : column + " must be bellow " + prop.length;
        if (descs [column] == null) {
            String n = prop[column].getShortDescription ();
            JLabel l = new JLabel ();
            Mnemonics.setLocalizedText (l, n);
            descs [column] = l.getText ();
        }
        return descs [column];
    }

    public String getRawColumnName (int column) {
        return prop[column].getDisplayName();
    }

    public Object getValueFor(Object node, int column) {
        Node n = Visualizer.findNode(node);
        if (n == null) {
            throw new IllegalStateException("TreeNode must be VisualizerNode but was: " + node + " of class " + node.getClass().getName());
        }
        PropertyChangeListener cacheEntry = nodesListenersCache.get (n);
        if (cacheEntry == null) {
            PropertyChangeListener p = WeakListeners.propertyChange(pcl, n);
            nodesListenersCache.put(n, p);
            n.addPropertyChangeListener(p);
        }
        Node.Property theRealProperty = getPropertyFor(n, prop[column]);
        return theRealProperty;
    }

    public boolean isCellEditable(Object node, int column) {
        Node n = Visualizer.findNode(node);
        if (n == null) {
            throw new IllegalStateException("TreeNode must be VisualizerNode but was: " + node + " of class " + node.getClass().getName());
        }
        Node.Property theRealProperty = getPropertyFor(n, prop[column]);
        if (theRealProperty != null) {
            return theRealProperty.canWrite();
        } else {
            return false;
        }
    }
    
    protected Node.Property getPropertyFor(Node node, Node.Property prop) {
        Node.PropertySet[] propSets = node.getPropertySets();

        for (int i = 0; i < propSets.length; i++) {
            Node.Property[] props = propSets[i].getProperties();

            for (int j = 0; j < props.length; j++) {
                if (prop.equals(props[j])) {
                    return props[j];
                }
            }
        }

        return null;
    }


    public void setValueFor(Object node, int column, Object value) {
        // Intentionally left empty. The cell editor components are
        // PropertyPanels that will propagate the change into the target
        // property object - no need to do anything in this method.
    }
    
    public void setProperties(Node.Property[] newProperties) {
        prop = newProperties;
        names = new String [prop.length];
        descs = new String [prop.length];
    }
    
    /**
     * Of the parameter is of type Node.Property this methods
     * calls getValue on the property and returns the value.
     * If the parameter is something else <code>null</code>
     * is returned.
     */
    public static Object getValueFromProperty(Object property) {
        if (property instanceof Node.Property) {
            Node.Property prop = (Node.Property)property;
            try {
                return prop.getValue();
            } catch (Exception x) {
                ErrorManager.getDefault().getInstance(
                    PropertiesRowModel.class.getName()).notify(
                        ErrorManager.INFORMATIONAL, x);
            }
        }
        return null;
    }
    /**
     * Search the properties for given property name.
     * The returned value is the index of property: you
     * have to add 1 to make it the column index because the
     * column with index 0 is reserved for the tree!
     */
    private int columnForProperty(String propName) {
        for (int i = 0; i < prop.length; i++) {
            if (prop[i].getName().equals(propName))
                return i;
        }
        return -1;
    }

    /**
     * Changes the value of the boolean property.
     */
    public static void toggleBooleanProperty(Node.Property<Boolean> p) {
        if (p.getValueType() == Boolean.class || p.getValueType() == Boolean.TYPE) {
            if (!p.canWrite()) {
                return;
            }
            try {
                Boolean val = p.getValue ();
                if (Boolean.FALSE.equals(val)) {
                    p.setValue(Boolean.TRUE);
                } else {
                    //This covers null multi-selections too
                    p.setValue(Boolean.FALSE);
                }
            } catch (Exception e1) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e1);
            }
        }
    }    
}
