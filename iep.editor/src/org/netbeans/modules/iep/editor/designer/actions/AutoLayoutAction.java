package org.netbeans.modules.iep.editor.designer.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.netbeans.modules.iep.editor.designer.PdModel;
import org.netbeans.modules.iep.editor.designer.PlanCanvas;
import org.netbeans.modules.iep.editor.util.ImageUtil;
import org.netbeans.modules.iep.model.IEPModel;
import org.openide.util.NbBundle;

import com.nwoods.jgo.JGoLink;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoView;
import com.nwoods.jgo.layout.JGoLayeredDigraphAutoLayout;

public class AutoLayoutAction extends AbstractAction {

    public final static String AUTO_LAYOUT_NAME = "AutoLayout";

    public final static ImageIcon AUTO_LAYOUT_ICON = ImageUtil.getImageIcon("autoLayout.gif");

    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(AutoLayoutAction.class.getName());

    private PlanCanvas mView;
    private IEPModel mModel;
    
    public AutoLayoutAction(PlanCanvas view, IEPModel model) {
        this.mView = view;
        this.mModel = model;
        
        this.putValue(NAME, AUTO_LAYOUT_NAME);
        this.putValue(SMALL_ICON, AUTO_LAYOUT_ICON);
        
        String shortDesc = NbBundle.getMessage(AutoLayoutAction.class,"PlanDesigner.Autolayout");
        this.putValue(SHORT_DESCRIPTION , shortDesc);
    }
    
    public void actionPerformed(ActionEvent e) {
        layout();
        
    }
    
    private void layout() {
        PdModel doc = mView.getDoc();
        doc.startTransaction();
        JGoLayeredDigraphAutoLayout l = new JGoLayeredDigraphAutoLayout(doc,
            30, 30,  JGoLayeredDigraphAutoLayout.LD_DIRECTION_RIGHT,
            JGoLayeredDigraphAutoLayout.LD_CYCLEREMOVE_DFS,
            JGoLayeredDigraphAutoLayout.LD_LAYERING_OPTIMALLINKLENGTH,
            JGoLayeredDigraphAutoLayout.LD_INITIALIZE_DFSOUT, 4,
            JGoLayeredDigraphAutoLayout.LD_AGGRESSIVE_FALSE);
        l.performLayout();
        if (doc.isOrthogonalFlows()) {
            // now update all links
            JGoListPosition pos = doc.getFirstObjectPos();
            while (pos != null) {
                JGoObject obj = doc.getObjectAtPos(pos);
                // only consider top-level objects
                pos = doc.getNextObjectPosAtTop(pos);
                if (obj instanceof JGoLink) {
                    JGoLink link = (JGoLink)obj;
                    link.portChange(null, JGoLink.ChangedOrthogonal, 0, null);
                }
            }
        }
        doc.endTransaction("Layout");
    }
}
