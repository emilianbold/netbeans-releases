package org.netbeans.modules.iep.editor.designer.actions;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JDialog;

import org.netbeans.modules.iep.editor.designer.PlanCanvas;
import org.netbeans.modules.iep.editor.util.ImageUtil;
import org.netbeans.modules.iep.model.IEPModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

import com.nwoods.jgo.JGoOverview;
import com.nwoods.jgo.JGoView;

public class OverviewAction extends AbstractAction {

    public final static String OVERVIEW_NAME = "Overview";
    
    public final static ImageIcon OVERVIEW_ICON = ImageUtil.getImageIcon("overview.gif");

    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(OverviewAction.class.getName());

    private JGoView mView;
    private IEPModel mModel;
    
    private JGoOverview mOverview;
    private JDialog mOverviewDialog;
    
    public OverviewAction(JGoView view, IEPModel model) {
        this.mView = view;
        this.mModel = model;
        
        this.putValue(NAME, OVERVIEW_NAME);
        this.putValue(SMALL_ICON, OVERVIEW_ICON);
        
        String shortDesc = NbBundle.getMessage(PlanCanvas.class,"PlanDesigner.Overview");
        this.putValue(SHORT_DESCRIPTION , shortDesc);
    }
    
    public void actionPerformed(ActionEvent e) {
        overviewAction();
    }
    
    private void overviewAction() {
        if (mOverview == null) {
            mOverview = new JGoOverview();
            mOverview.setObserved(this.mView);
            String title = NbBundle.getMessage(PlanCanvas.class,"PlanDesigner.Overview");
            try {
                title = mModel.getIEPFileName() + " " +NbBundle.getMessage(PlanCanvas.class,"PlanDesigner.Overview");
            } catch (Exception e) {
                e.printStackTrace();
                mLog.warning(e.getMessage());
            }
            mOverviewDialog = new JDialog(WindowManager.getDefault().getMainWindow(), title, false);
            mOverviewDialog.getContentPane().setLayout(new BorderLayout());
            mOverviewDialog.getContentPane().add(mOverview, BorderLayout.CENTER);
        }
        mOverviewDialog.pack();
        mOverviewDialog.setVisible(true);
    }


}
