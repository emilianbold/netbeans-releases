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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.model;


import java.util.ArrayList;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeStructureSnapshot;
import org.netbeans.core.windows.SplitConstraint;


/**
 * Model whidh represents sub model of split modes. It adds special notion
 * of positioning of editor area. The editor area itself is represented
 * by exact instance of SplitSubModel.
 *
 * @author  Peter Zavadsky
 */
final class EditorSplitSubModel extends SplitSubModel {

    /** Only instance of EditorNode representing position
     * of editor area in this sub model. */
    private final EditorNode editorNode;


    public EditorSplitSubModel(Model parentModel, SplitSubModel editorArea) {
        super(parentModel);
        
        this.editorNode = new EditorNode(editorArea);
        
        // XXX The editor node has to be always present.
        addNodeToTree(editorNode, new SplitConstraint[0]);
    }
    

    /** Overrides superclass method to prevent removing of editor node. */
    protected boolean removeNodeFromTree(Node node) {
        if(node == editorNode) {
            // XXX Prevents removing of editor node.
            return false;
        }
        
        return super.removeNodeFromTree(node);
    }

    public boolean setEditorNodeConstraints(SplitConstraint[] editorNodeConstraints) {
        super.removeNodeFromTree(editorNode);
        return addNodeToTree(editorNode, editorNodeConstraints);
    }
    
    public SplitConstraint[] getEditorNodeConstraints() {
        return editorNode.getNodeConstraints();
    }
    
    public SplitSubModel getEditorArea() {
        return editorNode.getEditorArea();
    }

    public boolean setSplitWeights( ModelElement[] snapshots, double[] splitWeights) {
        if( super.setSplitWeights( snapshots, splitWeights ) ) {
            return true;
        }
        
        return getEditorArea().setSplitWeights( snapshots, splitWeights );
    }
    
    
    /** Class which represents editor area position in EditorSplitSubModel. */
    static class EditorNode extends SplitSubModel.Node {
        /** Ref to editor area. */
        private final SplitSubModel editorArea;
        
        /** Creates a new instance of EditorNode */
        public EditorNode(SplitSubModel editorArea) {
            this.editorArea = editorArea;
        }

        public boolean isVisibleInSplit() {
            return true;
        }
        
        public SplitSubModel getEditorArea() {
            return editorArea;
        }
        
        public double getResizeWeight() {
            return 1D;
        }
        
        public ModeStructureSnapshot.ElementSnapshot createSnapshot() {
            return new ModeStructureSnapshot.EditorSnapshot(this, null,
                editorArea.createSplitSnapshot(), getResizeWeight());
        }
    } // End of nested EditorNode class.
    
    
    // XXX
    protected boolean addNodeToTreeAroundEditor(Node addingNode, String side) {
        // Update
        Node attachNode = editorNode;
        if(attachNode == root) {
            int addingIndex = (side == Constants.TOP || side == Constants.LEFT) ? 0 : -1;
            int oldIndex = addingIndex == 0 ? -1 : 0;
            // Create new branch.
            int orientation = (side == Constants.TOP || side == Constants.BOTTOM) ? Constants.VERTICAL : Constants.HORIZONTAL;
            SplitNode newSplit = new SplitNode(orientation);
            newSplit.setChildAt(addingIndex, Constants.DROP_TO_SIDE_RATIO, addingNode);
            newSplit.setChildAt(oldIndex, 1D - Constants.DROP_TO_SIDE_RATIO, attachNode);
            root = newSplit;
        } else {
            SplitNode parent = attachNode.getParent();
            if(parent == null) {
                return false;
            }

            int attachIndex = parent.getChildIndex(attachNode);
            double attachWeight = parent.getChildSplitWeight(attachNode);
            // Create new branch.
            int orientation = (side == Constants.TOP || side == Constants.BOTTOM) ? Constants.VERTICAL : Constants.HORIZONTAL;
            SplitNode newSplit = new SplitNode(orientation);
            parent.removeChild(attachNode);
            int addingIndex = (side == Constants.TOP || side == Constants.LEFT) ? 0 : -1;
            int oldIndex = addingIndex == 0 ? -1 : 0;
            newSplit.setChildAt(addingIndex, Constants.DROP_AROUND_RATIO, addingNode);
            newSplit.setChildAt(oldIndex, 1D - Constants.DROP_AROUND_RATIO, attachNode);
            parent.setChildAt(attachIndex, attachWeight, newSplit);
        }
        
        return true;
    }

}

