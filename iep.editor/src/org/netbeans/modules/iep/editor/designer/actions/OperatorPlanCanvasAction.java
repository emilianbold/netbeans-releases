package org.netbeans.modules.iep.editor.designer.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.modules.iep.editor.designer.PlanCanvas;
import org.netbeans.modules.tbls.editor.palette.TcgActiveEditorDrop;
import org.netbeans.spi.palette.PaletteController;
import org.openide.nodes.Node;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Lookup;

public class OperatorPlanCanvasAction extends AbstractAction  {

    private PlanCanvas mCanvas;
    
    private Node mOperatorPaletteNode;
    
    public OperatorPlanCanvasAction(Node operatorPaletteNode, PlanCanvas canvas) {
        this.mOperatorPaletteNode = operatorPaletteNode;
        this.mCanvas = canvas;
        
        this.putValue(Action.NAME, this.mOperatorPaletteNode.getDisplayName());
        
    }
    
    public void actionPerformed(ActionEvent e) {
        Lookup itemLookup = mOperatorPaletteNode.getLookup();
        TcgActiveEditorDrop iaed = (TcgActiveEditorDrop)itemLookup.lookup(ActiveEditorDrop.class);
        this.mCanvas.handleAddNewOperatorByPopup(iaed);
    }

}
