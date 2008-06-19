/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import org.netbeans.modules.soa.mappercore.model.Constant;
import org.netbeans.modules.soa.mappercore.model.Function;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.Operation;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
/**
 *
 * @author alex
 */
public class MoveLeftCanvasAction extends MapperKeyboardAction {
        
    public MoveLeftCanvasAction(Canvas canvas) {
        super(canvas);
    }
    
    
    @Override
    public String getActionKey() {
        return "press-move-left-action";
    }

    @Override
    public KeyStroke[] getShortcuts() {
        return new KeyStroke[]{
            KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
            KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.SHIFT_MASK),
            KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.CTRL_MASK)
        };
    }

    public void actionPerformed(ActionEvent e) {
         // SHIFT
        if (isShiftPress(e)) {
            if (canvas.getScrollPane().getViewport() == null) {
                return;
            }

            Insets insets = canvas.getAutoscrollInsets();
            int y = canvas.getScrollPane().getViewport().getViewRect().y;
            Rectangle r = new Rectangle(0, y, 1, 1);
            r.x = insets.left - 16 -
                    2 * canvas.getScrollPane().getHorizontalScrollBar().getUnitIncrement();            
            canvas.scrollRectToVisible(r);
            
        }
        
        SelectionModel selectionModel = canvas.getSelectionModel();
       
        TreePath treePath = selectionModel.getSelectedPath();
        if (treePath == null) return;
            
        Graph graph = selectionModel.getSelectedGraph();
        if (graph == null || graph.isEmpty()) {
            canvas.getLeftTree().requestFocus();
            return;
        }

        List<Vertex> sVertexeces = selectionModel.getSelectedVerteces();
        List<Link> sLinks = selectionModel.getSelectedLinks();
        VertexItem sVertexItem = selectionModel.getSelectedVertexItem();
        //CONTROL    
        if (isControlPress(e)) {
            // vertex is select
            if (sVertexeces != null && sVertexeces.size() > 0) {
                Vertex vertex = sVertexeces.get(0);
                if (vertex.getItemCount() == 0) return;
                
                for (int i = 0; i < vertex.getItemCount(); i++) {
                    Link link = vertex.getItem(i).getIngoingLink();
                    if (link != null) {
                        selectionModel.setSelected(treePath, link);
                        return;
                    }
                }
                if (vertex.getGraph().getPrevVertex(vertex) == vertex) {
                    canvas.getLeftTree().requestFocus();
                } 
                return;
            }
            // Link is select
            if (sLinks != null && sLinks.size() > 0) {
                Link link = sLinks.get(0);
                
                if (link.getSource() instanceof TreeSourcePin) {
                    TreePath leftTreePath = ((TreeSourcePin) link.getSource()).getTreePath();
                    canvas.getLeftTree().setSelectionPath(leftTreePath);
                    canvas.getLeftTree().requestFocus();

                } else {
                    Vertex vertex = (Vertex) link.getSource();
                    if (vertex == null) { return; }

                    selectionModel.setSelected(treePath, vertex);
                }
                return;
            }
            // vertexItem is select
            if (sVertexItem != null) {
                Link link = sVertexItem.getIngoingLink();
                if (link == null) { 
                    selectionModel.setSelected(treePath, sVertexItem.getVertex());
                    return; 
                }
                
                selectionModel.setSelected(treePath, link);
                return;
            }
            // nothing is selected and graph have not vertex
        }
        // NOTHING
        if (isNothingPress(e)) {
            // Vertex is select
            if (sVertexeces != null && sVertexeces.size() > 0) {
                Vertex vertex = sVertexeces.get(0);

                Vertex prevVertex = vertex.getGraph().getPrevVertex(vertex);
                if (prevVertex == null)  return;

                selectionModel.setSelected(treePath, prevVertex);
                return;
            }
            // Link is select
            if (sLinks != null && sLinks.size() > 0) {
                Link link = sLinks.get(0);
                
                if (link.getSource() instanceof TreeSourcePin) {
                    TreePath leftTreePath = ((TreeSourcePin) link.getSource()).getTreePath();
                    canvas.getLeftTree().setSelectionPath(leftTreePath);
                    canvas.getLeftTree().requestFocus();

                } else {
                    Vertex vertex = (Vertex) link.getSource();
                    if (vertex == null) { return; }

                    selectionModel.setSelected(treePath, vertex);
                }
                return;
            }
            // VertexItem is select
            if (sVertexItem != null) {
                // v razrabotke
                return;
            }
            // nothing is selected            
            if (graph.getVerteces() != null && graph.getVerteces().size() > 0) {
                selectionModel.setSelected(treePath, graph.getPrevVertex(null));
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
