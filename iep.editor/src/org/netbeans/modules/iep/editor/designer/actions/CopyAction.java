package org.netbeans.modules.iep.editor.designer.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import org.netbeans.modules.iep.editor.designer.PdAction;
import org.netbeans.modules.iep.editor.designer.PdModel;
import org.netbeans.modules.iep.editor.designer.PlanCanvas;
import org.netbeans.modules.tbls.model.ImageUtil;
import org.openide.util.NbBundle;

import com.nwoods.jgo.JGoGlobal;


public class CopyAction extends PdAction {

    public final static String COPY_NAME = "Copy";
    
    public final static ImageIcon COPY_ICON = ImageUtil.getImageIcon("copy.gif");

    private static String COPY_DESCRIPTION = NbBundle.getMessage(CopyAction.class,"PlanDesigner.Copy_to_clipboard");
    
    private PlanCanvas mCanvas;
    
    public CopyAction(PlanCanvas canvas) {
        super(COPY_NAME, COPY_DESCRIPTION, COPY_ICON, canvas);
        this.mCanvas = canvas;
    }

    public void actionPerformed(ActionEvent e) {
        getCanvas().copy();
        
        Toolkit toolkit = mCanvas.getToolkit();
        if (toolkit == null)
            toolkit = JGoGlobal.getToolkit();
        
        Clipboard clipboard = toolkit.getSystemClipboard();
        
        PdModel model = new PdModel();
        model.copyFromCollection(mCanvas.getSelection());
        
        IEPComponentTransferable trax = new IEPComponentTransferable(model);
        
        // and set up the clipboard
        clipboard.setContents(trax, mCanvas);
        
    }
    
    public boolean canAct() { 
        return super.canAct() && !getCanvas().getSelection().isEmpty(); 
    }

}
