package org.netbeans.modules.iep.editor.designer.actions;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import org.netbeans.modules.iep.editor.designer.PdAction;
import org.netbeans.modules.iep.editor.designer.PlanCanvas;

import org.netbeans.modules.tbls.model.ImageUtil;
import org.openide.util.NbBundle;


public class PasteAction extends PdAction {

    public final static String PASTE_NAME = "Paste";
    
    public final static ImageIcon PASTE_ICON = ImageUtil.getImageIcon("paste.gif");

    private static final String PASTE_DESCRIPTION = NbBundle.getMessage(PasteAction.class,"PlanDesigner.Paste_to_canvas");

    public PasteAction(PlanCanvas canvas) {
        super(PASTE_NAME, PASTE_DESCRIPTION, PASTE_ICON, canvas);
        
    }
    
    public void actionPerformed(ActionEvent e) {
        getCanvas().paste();
        
    }
    
    public boolean canAct() { 
        return super.canAct() && getCanvas().getDoc().isModifiable() && getCanvas().canPaste(); 
    }
    
}
