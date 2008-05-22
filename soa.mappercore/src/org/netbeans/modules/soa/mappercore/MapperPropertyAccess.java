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

import org.netbeans.modules.soa.mappercore.model.MapperModel;

/**
 *
 * @author anjeleevich
 */
public class MapperPropertyAccess {
    
    
    private Mapper mapper;
    
    
    public MapperPropertyAccess(Mapper mapper) {
        this.mapper = mapper;
    }
    
    
    public final Mapper getMapper() {
        return mapper;
    }
    
    
    public final Canvas getCanvas() {
        return mapper.getCanvas();
    }
    
    
    public final LeftTree getLeftTree() {
        return mapper.getLeftTree();
    }
    
    
    public final RightTree getRightTree() {
        return mapper.getRightTree();
    }
    
    
    public final MapperModel getMapperModel() {
        return mapper.getFilteredModel();
    }
    
    
    public final MapperNode getRoot() {
        return mapper.getRoot();
    }
    
    
    public final MapperNode getNodeAt(int y) {
        return mapper.getNodeAt(y);
    }
    
    
    public final LinkTool getLinkTool() {
        return mapper.getLinkTool();
    }
    
    
    public final MoveTool getMoveTool() {
        return mapper.getMoveTool();
    }
    
    
    public final SelectionModel getSelectionModel() {
        return mapper.getSelectionModel();
    }
}
