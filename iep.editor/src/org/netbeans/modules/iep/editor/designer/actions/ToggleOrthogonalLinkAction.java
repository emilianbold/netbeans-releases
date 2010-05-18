package org.netbeans.modules.iep.editor.designer.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JDialog;

import org.netbeans.modules.iep.editor.designer.PlanCanvas;
import org.netbeans.modules.tbls.model.ImageUtil;
import org.netbeans.modules.iep.model.IEPModel;
import org.openide.util.NbBundle;


import com.nwoods.jgo.JGoOverview;

public class ToggleOrthogonalLinkAction extends AbstractAction {

    public final static ImageIcon ORTHOGONAL_LINK_ICON = ImageUtil.getImageIcon("orthoLink.gif");
    
    public final static String ORTHOGONAL_LINK_NAME = "OrthogonalLink";
    
    private PlanCanvas mView;
    private IEPModel mModel;
    
    private JGoOverview mOverview;
    private JDialog mOverviewDialog;
    
    public ToggleOrthogonalLinkAction(PlanCanvas view, IEPModel model) {
        this.mView = view;
        this.mModel = model;
        
        this.putValue(NAME, ORTHOGONAL_LINK_NAME);
        this.putValue(SMALL_ICON, ORTHOGONAL_LINK_ICON);
        
        String shortDesc = NbBundle.getMessage(PlanCanvas.class,"PlanDesigner.Toggle_orthogonal_flows");
        this.putValue(SHORT_DESCRIPTION , shortDesc);
        
    }
    
    public void actionPerformed(ActionEvent e) {
            this.mView.getDoc().toggleOrthogonalFlows();
    }

}
