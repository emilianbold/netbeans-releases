/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.mappercore;

import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.MapperModel;

/**
 *
 * @author anjeleevich
 */
public class MapperPanel extends JPanel {
    
    private Mapper mapper;
    

    public MapperPanel(Mapper mapper) {
        this.mapper = mapper;
        setFocusable(true);
    }
    
    
    public Mapper getMapper() {
        return mapper;
    }
    
    
    LinkTool getLinkTool() {
        return mapper.getLinkTool();
    }
    
    
    public LeftTree getLeftTree() {
        return mapper.getLeftTree();
    }
    
    
    public Canvas getCanvas() {
        return mapper.getCanvas();
    }
    
    
    public RightTree getRightTree() {
        return mapper.getRightTree();
    }
    
    
    public MapperModel getMapperModel() {
        return mapper.getFilteredModel();
    }
    
    
    public MapperNode getRoot() {
        return mapper.getRoot();
    }
    
    
    public MapperNode getNodeAt(int y) {
        return mapper.getNodeAt(y);
    }
    
    
    public SelectionModel getSelectionModel() {
        return mapper.getSelectionModel();
    }
    
    
    public int yToMapper(int y) {
        for (Component c = this; c != mapper; c = c.getParent()) {
            y += c.getY();
        }
        
        return y;
    }
    
    
    public int yFromMapper(int y) {
        for (Component c = this; c != mapper; c = c.getParent()) {
            y -= c.getY();
        }
        
        return y;
    }
    
    
    public int getStep() {
        return mapper.getStepSize();
    }
    
    
    public TreePath getTreePath(int y) {
        MapperNode node = getNodeAt(y);
        return (node != null) ? node.getTreePath() : null;
    }
}
