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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.mappercore;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.TargetPin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
/**
 *
 * @author alex
 */
public class MoveDownCanvasAction extends MapperKeyboardAction {
        
    public MoveDownCanvasAction(Canvas canvas) {
        super(canvas);
    }
    
    @Override
    public String getActionKey() {
        return "press-move-down-action";
    }

    @Override
    public KeyStroke[] getShortcuts() {
         return new KeyStroke[] {
         KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), 
         KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK),
         KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK),
         KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK + 
                KeyEvent.SHIFT_DOWN_MASK) 
         };
    }

    public void actionPerformed(ActionEvent e) {        
        SelectionModel selectionModel = canvas.getSelectionModel();
       
        TreePath treePath = selectionModel.getSelectedPath();
        if (treePath == null) return;
        
        Mapper mapper = canvas.getMapper();
        Graph graph = selectionModel.getSelectedGraph();
        if (graph == null || graph.isEmpty()) {
            MapperNode currentNode = mapper.getNode(treePath, true);
            MapperNode nextNode = currentNode.getNextVisibleNode();
            if (nextNode == null) return;
                
            mapper.setSelectedNode(nextNode);
            return;
        }

        List<Vertex> sVertexeces = selectionModel.getSelectedVerteces();
        List<Link> sLinks = selectionModel.getSelectedLinks();
        VertexItem sVertexItem = selectionModel.getSelectedVertexItem();
        // CONTROL+SHIFT
        if (isControlPress(e) && isShiftPress(e)) {
            if (sLinks != null && sLinks.size() > 0) {
                    Link link = sLinks.get(0);
                    List<Link> links = graph.getIngoingLinks();
                    if (!links.contains(link)) return;
                
                    int index = links.indexOf(link);
                    if (index == links.size() - 1) return;
                
                    selectionModel.setSelected(treePath, links.get(index + 1));
                }
                return;
        }
        // SHIFT
        if (isShiftPress(e)) {
           if (canvas.getScrollPane().getViewport() == null) {
                return;
            }

            Insets insets = canvas.getAutoscrollInsets();
            int x = canvas.getScrollPane().getViewport().getViewRect().x;
            Rectangle r = new Rectangle(x, 0, 1, 1);
            r.y = canvas.getHeight() - insets.bottom + 16 +
                    2 * canvas.getScrollPane().getVerticalScrollBar().getUnitIncrement();
            canvas.scrollRectToVisible(r);
        }
        //CONTROL    
        if (isControlPress(e)) {
            // veretex is select
            if (sVertexeces != null && sVertexeces.size() > 0) {
                Vertex vertex = sVertexeces.get(0);
                if (vertex.getItemCount() < 1) { return; }
                
                selectionModel.setSelected(treePath, vertex.getItem(0));
                return;
            }
            // link is select
            if (sLinks != null && sLinks.size() > 0) {
                Link link = sLinks.get(0);
                TargetPin targetPin = link.getTarget();
                if (targetPin instanceof Graph) return;
                
                VertexItem vertexItem = (VertexItem) targetPin;
                Vertex vertex = vertexItem.getVertex();
                for (int i = vertex.getItemIndex(vertexItem) + 1;
                        i < vertex.getItemCount(); i++) {
                    link = vertex.getItem(i).getIngoingLink();
                    if (link != null) {
                        selectionModel.setSelected(treePath, link);
                        break;
                    }
                }
                return;
            }
            // vertexItem is select
            if (sVertexItem != null) {
                Vertex vertex = sVertexItem.getVertex();
                int index = vertex.getItemIndex(sVertexItem);
                if (index >= vertex.getItemCount() - 1) {
                    selectionModel.setSelected(treePath, vertex);
                    return;
                }
                
                VertexItem nextVertexItem = vertex.getItem(index + 1);
                selectionModel.setSelected(treePath, nextVertexItem);
                return;
            }
            // nothing is selected and graph have not vertex
            MapperNode currentNode = mapper.getNode(treePath, true);
            MapperNode nextNode = currentNode.getNextVisibleNode();
            if (nextNode == null) return;
                
            mapper.setSelectedNode(nextNode);
        }
        // NOTHING
        if (isNothingPress(e)) {
            TreePath currentTreePath = selectionModel.getSelectedPath();
            if (currentTreePath != null) {
                MapperNode currentNode = mapper.getNode(currentTreePath, true);
                MapperNode nextNode = currentNode.getNextVisibleNode();
                if (nextNode != null) {
                    selectionModel.setSelected(nextNode.getTreePath());
                }
            } else if (mapper.getRoot() != null 
                    && mapper.getRoot().getChildCount() > 0) 
            {
                mapper.setSelectedNode(mapper.getRoot().getChild(0));
            }   
        }
    }
    
    private boolean isControlPress(ActionEvent e) {
        return (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
    }
    
    private boolean isShiftPress(ActionEvent e) {
        return (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
    }
    
    private boolean isNothingPress(ActionEvent e) {
        return e.getModifiers() == 0;
    }
}
