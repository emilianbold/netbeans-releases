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

package org.netbeans.modules.bpel.mapper.tree.actions;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import javax.swing.AbstractAction;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.model.BpelMapperUtils;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.SelectionModel;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.MapperModel;

/**
 *
 * @author nk160297
 * @author AlexanderPermyakov
 */
public class DeleteGraphSelectionAction extends AbstractAction {

    private Mapper mMapper;
    
    public DeleteGraphSelectionAction(Mapper mapper) {
        super();
        mMapper = mapper;
    }
    
    public void actionPerformed(ActionEvent e) {
        SelectionModel selectionModel = mMapper.getSelectionModel();
        //
        List<Vertex> vertexList = selectionModel.getSelectedVerteces();
        HashSet<Link> linkSet = new HashSet<Link>(selectionModel.getSelectedLinks());
        //
        // Add to selection all links which connected to the selected verteces
        for (Vertex vertex : vertexList) {
            List<Link> connectedLinkList = 
                    BpelMapperUtils.getConnectedLinkList(vertex);
            linkSet.addAll(connectedLinkList);
        }
        // Save in Buffer deleted GraphSubset
        if (e.getModifiers() == ActionEvent.CTRL_MASK ||
                e.getModifiers() == ActionEvent.SHIFT_MASK) 
        {
             mMapper.getCanvas().setBufferCopyPaste(selectionModel.getSelectedSubset());
        }
        //
        // Remove the selected verteces and links
        Graph graph = selectionModel.getSelectedGraph();
        for (Link link : linkSet) {
            link.disconnect();
        }
        for (Vertex vertex : vertexList) {
            graph.removeVertex(vertex);
        }
        
        //
        // Initiate graph repaint
        MapperModel mapperModel = mMapper.getModel();
        assert mapperModel instanceof BpelMapperModel;
        //
        ((BpelMapperModel)mapperModel).fireGraphChanged(
                selectionModel.getSelectedPath());
    }
    
}
