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

package org.netbeans.modules.mobility.project.ui.customizer.regex;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import org.openide.filesystems.FileObject;

/**
 * User: suchys
 * Date: Dec 20, 2003
 * Time: 12:48:08 PM
 */
public class CheckedNodeEditor extends AbstractCellEditor implements TreeCellEditor{
    static final long serialVersionUID = -5087358518052291490L;
    CheckedNodeRenderer customRenderer = null;
    final JTree jtree;
    ItemListener itemListener;
    protected CheckedTreeBeanView storage;
    
    public CheckedNodeEditor(JTree jtree) {
        this.jtree = jtree;
        customRenderer = new CheckedNodeRenderer();
    }
    
    public void setContentStorage(final CheckedTreeBeanView storage) {
        this.storage = storage;
        customRenderer.setContentStorage(storage);
    }
    
    public Object getCellEditorValue() {
        return customRenderer.getRenderer();
    }
    
    public boolean isCellEditable(final EventObject e) {
        final boolean returnValue = false;
        if (e instanceof MouseEvent){
            if (!canEditImmediately(e)) return false;
            final MouseEvent mouseEvent = (MouseEvent) e;
            final TreePath path = jtree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
            if (path != null){
                final Object o = path.getLastPathComponent();
                final Node node = Visualizer.findNode(o);
                if (node.getCookie(FileObjectCookie.class) != null){
                    return true;
                }
            }
        }
        return returnValue;
    }
    
    /**
     * Returns true if <code>event</code> is <code>null</code>,
     * or it is a <code>MouseEvent</code> with a click count > 2
     * and <code>inHitRegion</code> returns true.
     * @param event the event being studied
     */
    protected boolean canEditImmediately(final EventObject event) {
        if ((event instanceof MouseEvent) && SwingUtilities.isLeftMouseButton((MouseEvent) event)) {
            final MouseEvent me = (MouseEvent) event;
            return inHitRegion(me.getX(), me.getY());
        }
        return false;
    }
    
    /**
     * Returns true if the passed in location is a valid mouse location
     * to start editing from. This is implemented to return false if
     * <code>x</code> is <= the width of the icon and icon gap displayed
     * by the renderer. In other words this returns true if the user
     * clicks over the text part displayed by the renderer, and false
     * otherwise.
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @return true if the passed in location is a valid mouse location
     */
    protected boolean inHitRegion(final int x, final int y) {
        final int lastRow = jtree.getRowForLocation(x, y);
        if (lastRow != -1 && jtree != null) {
            final Rectangle bounds = jtree.getRowBounds(lastRow);
            //int offset = (int)customRenderer.getRenderer().jCheckBox1.getSize().getWidth();
            String osName = (String) System.getProperties().get("os.name"); //NOI18N
            if (bounds != null && (x - bounds.x ) < 10 && (x - bounds.x) >= 0) {
                return true;
            //fix fo Mac OS X Issue 158557
            } else if (osName.contains("Mac") && bounds != null) { //NOI18N
                return true;
            }
        }
        return false;
    }
    
    public Component getTreeCellEditorComponent(final JTree tree, final Object value,
            final boolean isSelected, final boolean expanded,
            final boolean leaf, final int row) {
        final Component editor = customRenderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, true);
        itemListener = new ItemListener() {
            public void itemStateChanged(@SuppressWarnings("unused") ItemEvent e) {
                if (stopCellEditing()){
                    if (editor instanceof CheckedNodeRenderer.RendererComponent){
                        ((CheckedNodeRenderer.RendererComponent)editor).removeItemListener(itemListener);
                    }
                    final Node node = Visualizer.findNode(value);
                    final FileObjectCookie doj = (FileObjectCookie) node.getCookie(FileObjectCookie.class);
                    if (doj != null){
                        final FileObject fo = doj.getFileObject();
                        storage.setState(fo, CheckedTreeBeanView.UNSELECTED == storage.getState(fo));
                        final TreePath path = tree.getAnchorSelectionPath();
                        ((DefaultTreeModel)tree.getModel()).reload();
                        tree.setAnchorSelectionPath(path);
                    }
                }
            }
        };
        
        if (editor instanceof CheckedNodeRenderer.RendererComponent){
            ((CheckedNodeRenderer.RendererComponent)editor).addItemListener(itemListener);
        }
        return editor;
    }
    
}
