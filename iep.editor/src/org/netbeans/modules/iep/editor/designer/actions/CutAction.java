package org.netbeans.modules.iep.editor.designer.actions;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import org.netbeans.modules.iep.editor.designer.PdAction;
import org.netbeans.modules.iep.editor.designer.PlanCanvas;

import org.netbeans.modules.tbls.model.ImageUtil;
import org.openide.util.NbBundle;


public class CutAction extends PdAction {

    public final static String CUT_NAME = "Cut";
    
    public final static ImageIcon CUT_ICON = ImageUtil.getImageIcon("x16.cut.gif");

    private static final String CUT_DESCRIPTION = NbBundle.getMessage(CutAction.class,"PlanDesigner.Cut_to_clipboard");

    public CutAction(PlanCanvas canvas) {
        super(CUT_NAME, CUT_DESCRIPTION, CUT_ICON, canvas);
    }


    public void actionPerformed(ActionEvent e) {
        getCanvas().cut();
    }

    public boolean canAct() { 
        return super.canAct() && !getCanvas().getSelection().isEmpty() 
            && getCanvas().getDoc().isModifiable(); 
    }
}
