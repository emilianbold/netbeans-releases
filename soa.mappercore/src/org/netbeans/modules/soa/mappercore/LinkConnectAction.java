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

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.event.MapperSelectionEvent;
import org.netbeans.modules.soa.mappercore.event.MapperSelectionListener;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TargetPin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.mappercore.utils.Utils;

/**
 *
 * @author AlexanderPermyakov
 */
public class LinkConnectAction extends MapperKeyboardAction implements 
        MapperSelectionListener, TreeExpansionListener, FocusListener,
        TreeSelectionListener
{
    private TreePath treePath;
        
    public LinkConnectAction(Canvas canvas) {
        super(canvas);
        treePath = null;

        MapperKeyboardAction action = new LinkConnectDone(canvas);
        
        canvas.getMapper().addRightTreeExpansionListener(this);
        canvas.getSelectionModel().addSelectionListener(this);
        canvas.registerAction(action);
        
        canvas.getRightTree().addFocusListener(this);
        canvas.getRightTree().registrAction(this);
        canvas.getRightTree().registrAction(action);
        
        canvas.getLeftTree().addFocusListener(this);
        canvas.getLeftTree().addTreeSelectionListener(this);
        canvas.getLeftTree().registrAction(this);
        canvas.getLeftTree().registrAction(action);
    }
    
    @Override
    public Object getActionKey() {
        return "press-link-connect";
    }

    @Override
    public KeyStroke[] getShortcuts() {
        return new KeyStroke[] {
            KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK),
            KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_DOWN_MASK), 
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)
        };
    }

    public void actionPerformed(ActionEvent e) {
        LinkTool linkTool = canvas.getLinkTool();
        if (linkTool == null) {return;}
        
        if (e.getSource() == canvas.getLeftTree() && !linkTool.isActive()) {
            TreePath leftPath = canvas.getLeftTree().getSelectionPath();
            if (leftPath == null) { return; }

            TreeSourcePin treeSource = new TreeSourcePin(leftPath);
            linkTool.activateOutgoing(treeSource, null, null);
            canvas.getLeftTree().repaint();
        }
        
        SelectionModel selectionModel = canvas.getSelectionModel();
        treePath = selectionModel.getSelectedPath();
        if (treePath == null) return;
        
        SourcePin source = linkTool.getSourcePin();
        TargetPin target = linkTool.getTargetPin();
        
        if (!linkTool.isActive()) {
            if (e.getSource() == canvas) {
                List<Vertex> vertexes = selectionModel.getSelectedVerteces();
                if (vertexes != null && vertexes.size() != 0) {
                    if (source != null) {
                        linkTool.activateIngoing(treePath, vertexes.get(0).getItem(0));
                        setSource(source, canvas);
                    } else {
                        linkTool.activateOutgoing(treePath, vertexes.get(0));
                        setTarget(null, canvas);
                    }
                }
                VertexItem vertexItem = selectionModel.getSelectedVertexItem();
                if (vertexItem != null) {
                    linkTool.activateIngoing(treePath, vertexItem);
                    setSource(source, canvas);
                }
                canvas.repaint();
            }
            
            if (e.getSource() == canvas.getRightTree()) {
                Graph graph = canvas.getMapper().getNode(treePath, true).getGraph();
                linkTool.activateIngoing(treePath, graph, null);
                setSource(null, canvas);
                canvas.getRightTree().repaint();
            }
        }
        
        if (e.getModifiers() != 0) return;
        if (target != null && source != null) { 
            if (linkTool.getMapperModel().canConnect(treePath, source, target, null, null)) {
                linkTool.getMapperModel().connect(treePath, source,
                        target, null, null);
            }
        }
        linkTool.done();
    }

    public void mapperSelectionChanged(MapperSelectionEvent event) {
        LinkTool linkTool = canvas.getLinkTool();
        if (linkTool == null || !linkTool.isActive()) { return; }
       
        SelectionModel selectionModel = linkTool.getSelectionModel();
        if (treePath == null) { treePath = selectionModel.getSelectedPath(); }
        if (treePath == null) { return; }
        if (selectionModel.getSelectedPath() != treePath) {
            if (!(linkTool.getSourcePin() instanceof TreeSourcePin)) 
 //                   || canvas.hasFocus())
            {
                linkTool.done();
                return;
            }
            treePath = selectionModel.getSelectedPath();
            if (canvas.getRightTree().hasFocus()) {
                MapperNode node = canvas.getMapper().getNode(treePath, true);
                Graph graph = node.getGraph();
                setTarget(graph, canvas.getRightTree());
                canvas.getMapper().repaint();
                return;
            }
        }
        
        Vertex vertex = null;
        List<Vertex> vertexes = selectionModel.getSelectedVerteces();
        if (vertexes != null && !vertexes.isEmpty()) {
            vertex = vertexes.get(0);
        }
        
        if (linkTool.isOutgoing()) {
            VertexItem vertexItem = selectionModel.getSelectedVertexItem();
            if (vertexItem != null) {
                setTarget(vertexItem, canvas);
                return;
            }
            
            if (vertex == null || vertex.getItemCount() < 1) { return; }

            vertexItem = vertex.getItem(0);
            setTarget(vertexItem, canvas);
            return;
        }

        if (linkTool.isIngoing()) {
            if (vertex == null) { return; }
            
            setSource(vertex, canvas);
        }
        canvas.repaint();
    }

    public void treeExpanded(TreeExpansionEvent event) {
    }

    public void treeCollapsed(TreeExpansionEvent event) {
        LinkTool linkTool = canvas.getLinkTool();
        if (linkTool == null || !linkTool.isActive()) { return; }
        if (treePath == null) {
            linkTool.done();
            return;
        }
        MapperNode node = linkTool.getMapper().getNode(treePath, true);
        if (node == null) { return; } 
                
        if (!node.isVisibleGraph()) {
            this.treePath = null;
            linkTool.done();
        }
    }

    public void focusGained(FocusEvent e) {
        LinkTool linkTool = canvas.getLinkTool();
        if (linkTool == null || !linkTool.isActive()) { return; }
        
        Component component = e.getComponent();
        if (component == linkTool.getRightTree()) {
            if (linkTool.isOutgoing()) {
                SelectionModel selectionModel = linkTool.getSelectionModel();
                TreePath treePath = selectionModel.getSelectedPath();
                if (treePath == null) { return; }
                
                Graph graph = linkTool.getMapper().getNode(treePath, true).getGraph();
                setTarget(graph, linkTool.getRightTree());  
            }
            if (linkTool.isIngoing()) {
                 setSource(null, null);
            }
        }
        
        if (component == linkTool.getLeftTree()){
            if (linkTool.isIngoing()) {
                TreePath leftPath = canvas.getLeftTree().getSelectionPath();
                if (leftPath == null) { return; }
                SourcePin source = new TreeSourcePin(leftPath);
                setSource(source, canvas.getLeftTree());
            }
            if (linkTool.isOutgoing()) {
                setTarget(null, null);
            }
        }
        canvas.getMapper().repaint();
    }

    public void focusLost(FocusEvent e) {
        LinkTool linkTool = canvas.getLinkTool();
         if (linkTool == null || !linkTool.isActive()) { return; }
        
        List<Vertex> sVertexes = linkTool.getSelectionModel().getSelectedVerteces();
        if (sVertexes == null || sVertexes.isEmpty()) { return; }
        Vertex vertex = sVertexes.get(0); 
         
        JComponent component = (JComponent) e.getComponent();
        if (component == canvas.getRightTree()) {
            if (linkTool.isIngoing()) {
                setSource(vertex, canvas);
                return;
            }
            if (linkTool.isOutgoing()) {
                if (vertex.getItemCount() < 1) { return; }
                
                VertexItem vertexItem = vertex.getItem(0);
                setTarget(vertexItem, canvas);
                return;
            }
        }
        
        if (component == linkTool.getLeftTree()){
            
        }
        canvas.getMapper().repaint();
    }
    
    private void setSource(SourcePin source, JComponent c) {
        LinkTool linkTool = canvas.getLinkTool();
        TargetPin target = linkTool.getTargetPin();
        MapperModel mapperModel = canvas.getMapperModel();
        
        if (source != null && mapperModel.canConnect(treePath, source, target, 
                null, null)) 
        {
            linkTool.setSource(source, c, new Point());
        } else if (target instanceof Graph || source instanceof TreeSourcePin) {
            linkTool.setSource(null, null, new Point());
        } else {
            Point p = linkTool.getTargetPoint();
            p = Utils.toScrollPane(canvas, p, null);
            linkTool.setSource(null, canvas, p);
        }
        
    }
    
    private void setTarget(TargetPin target, JComponent c) {
        LinkTool linkTool = canvas.getLinkTool();
        SourcePin source = linkTool.getSourcePin();
        MapperModel mapperModel = canvas.getMapperModel();
        TreePath treePath = canvas.getSelectionModel().getSelectedPath();
        
        if (target != null && mapperModel.canConnect(treePath, source, target, 
                null, null)) 
        {
            linkTool.setTarget(treePath, target, c, new Point());
        } else if (source instanceof TreeSourcePin) {
            linkTool.setTarget(treePath, null, null, new Point());
        } else {
            Point p = linkTool.getSourcePoint();
            p = Utils.toScrollPane(canvas, p, null);
            linkTool.setTarget(treePath, null, canvas, p);
        }
    }

    public void valueChanged(TreeSelectionEvent e) {
        LinkTool linkTool = canvas.getLinkTool();
        if (linkTool == null || !linkTool.isActive()) { return; }
        
        if (linkTool.isIngoing() ) { 
            TreePath leftPath = e.getNewLeadSelectionPath();
            SourcePin source = new TreeSourcePin(leftPath);
            setSource(source, canvas.getLeftTree());
            canvas.getLeftTree().repaint();
            canvas.repaint();
        } 
        
        if (linkTool.isOutgoing() && 
                linkTool.getSourcePin() instanceof TreeSourcePin) 
        {
            linkTool.done();
            canvas.getMapper().repaint();
        }
    }
}
