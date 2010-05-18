package org.netbeans.modules.soa.mappercore;

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



import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
/**
 *
 * @author alex
 */
public class MoveRightCanvasAction extends MapperKeyboardAction {
        
    public MoveRightCanvasAction(Canvas canvas) {
        super(canvas);
    }
    
    
    @Override
    public Object getActionKey() {
        return "press-move-right-action";
    }

    @Override
    public KeyStroke[] getShortcuts() {
        return new KeyStroke[]{
            KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
            KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.CTRL_MASK),
            KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.SHIFT_MASK)
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
            r.x = canvas.getWidth() - insets.right + 16 +
                    2 * canvas.getScrollPane().getHorizontalScrollBar().getUnitIncrement();            
            canvas.scrollRectToVisible(r);
        }
        
        SelectionModel selectionModel = canvas.getSelectionModel();
       
        TreePath treePath = selectionModel.getSelectedPath();
        if (treePath == null) {
            canvas.getRightTree().requestFocus();
            return;
        }
        
        Graph graph = selectionModel.getSelectedGraph();
        if (graph == null || graph.isEmpty()) {
            canvas.getRightTree().requestFocus();
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
                Link link = vertex.getOutgoingLink();
                if (link == null) {
                    if (vertex.getGraph().getNextVertex(vertex) == vertex) {
                        canvas.getRightTree().requestFocus();
                    }
                    return;
                }

                selectionModel.setSelected(treePath, link);
                return;
            }
            // Link is select
            if (sLinks != null && sLinks.size() > 0) {
                Link link = sLinks.get(0);

                if (link.getTarget() instanceof VertexItem) {
                    VertexItem vertexItem = (VertexItem) link.getTarget();
                    if (vertexItem == null) {
                        return;
                    }

                    selectionModel.setSelected(treePath, vertexItem);
                } else {
                    canvas.getRightTree().requestFocus();
                }
                return;
            }
            // vertexItem is select
            if (sVertexItem != null) {
                selectionModel.setSelected(treePath, sVertexItem.getVertex());
                return;
            }
            // nothing is selected and graph have not vertex
            canvas.getRightTree().requestFocus();
        }
        // NOTHING
        if (isNothingPress(e)) {
            // Vertex is select
            if (sVertexeces != null && sVertexeces.size() > 0) {
                int count = sVertexeces.size();
                Vertex vertex = sVertexeces.get(count - 1);
                Vertex nextVertex = vertex.getGraph().getNextVertex(vertex);
                if (nextVertex == null) return;  
                
                selectionModel.setSelected(treePath, nextVertex);
                return;
            }
            // Link is select
            if (sLinks != null && sLinks.size() > 0) {
                int count = sLinks.size();
                Link link = sLinks.get(0);
                
                if (link.getTarget().getClass() == VertexItem.class) {
                    VertexItem vertexItem = (VertexItem) link.getTarget();
                    if (vertexItem == null) { return;}
                   
                    Vertex vertex = vertexItem.getVertex();
                    selectionModel.setSelected(treePath, vertex);
                } else {
                    canvas.getRightTree().requestFocus();
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
                selectionModel.setSelected(treePath, graph.getNextVertex(null));
                return;
            }   
            // nothing is selected and graph have not vertex
            canvas.getRightTree().requestFocus();
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

