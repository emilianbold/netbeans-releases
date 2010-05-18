/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.GraphItem;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;

/**
 *
 * @author AlexanderPermyakov
 */
public class ShowPopapMenuAction extends MapperKeyboardAction {

    public ShowPopapMenuAction(Canvas canvas) {
        super(canvas);
    }
    
    @Override
    public Object getActionKey() {
        return "ShowPopapMenu";
    }

    @Override
    public KeyStroke[] getShortcuts() {
        return new KeyStroke[] {
              KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK),
              KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0),
        };
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MapperContext context = canvas.getMapper().getContext();
        MapperModel model = canvas.getMapper().getModel();
        if (context == null || model == null) { return; }
        
        SelectionModel selectionModel = canvas.getSelectionModel();
        TreePath treePath = selectionModel.getSelectedPath();
        if (treePath == null) {return; }
        
        int x = canvas.getRendererContext().getCanvasVisibleMinX();
        int y = canvas.getMapper().getNode(treePath, true).yToView(0) + 1;
        GraphItem item = null;
        
        List<Link> links = selectionModel.getSelectedLinks();
        if (links != null && !links.isEmpty()) {
            Link link = links.get(0);
            item = link;
            Point p = link.getSourcePoint(canvas.getRendererContext(),
                    canvas.getMapper().getNode(treePath, true).yToView(0));
            
            x = p.x;
            y = p.y;
            if (p.x == Integer.MAX_VALUE) {
                x = canvas.getRendererContext().getCanvasVisibleMaxX();
            }
            if (p.x == Integer.MIN_VALUE) {
                x = canvas.getRendererContext().getCanvasVisibleMinX();
            }

        }
        
        List<Vertex> vertexes = selectionModel.getSelectedVerteces();
        if (vertexes != null && !vertexes.isEmpty()) {
            Vertex vertex = vertexes.get(0);
            int step = canvas.getStep();
            
            item = vertex;
            x = canvas.toCanvas(vertex.getX() * step) + step;
            y = canvas.getMapper().getNode(treePath, true).
                    yToView(vertex.getY() * step) + step;
        }
        
        if (item == null) {
            item = selectionModel.getSelectedVertexItem();
            if (item != null) {
                int step = canvas.getStep();
                VertexItem vertexItem = (VertexItem) item;
                x = canvas.toCanvas(vertexItem.getVertex().getX() * step) + step;
                y = canvas.getMapper().getNode(treePath, true).
                        yToView(vertexItem.getY() * step) + step;
            }
        }

        JPopupMenu mapperMenu = context.getCanvasPopupMenu(model, item, canvas.getMapper());

        if (mapperMenu != null) {
            mapperMenu.show(canvas, x, y);
        }
        
        
    }

}
