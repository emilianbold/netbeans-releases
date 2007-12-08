package org.netbeans.modules.etl.ui.view.graph.actions;

import java.awt.event.ActionEvent;
import java.net.URL;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.axiondb.ExternalConnectionProvider;
import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopComponent;
import org.netbeans.modules.sql.framework.ui.graph.actions.GraphAction;
import org.netbeans.modules.sql.framework.ui.utils.AxionExternalConnectionProvider;
import org.openide.util.NbBundle;

import com.sun.sql.framework.utils.Logger;

public final class TestRunAction extends GraphAction {
    
    private static final URL runIconUrl = TestRunAction.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/runCollaboration.png");    
    
    public TestRunAction() {
        //action name
        this.putValue(Action.NAME, NbBundle.getMessage(TestRunAction.class, "ACTION_TESTRUN"));
        
        //action icon
        this.putValue(Action.SMALL_ICON, new ImageIcon(runIconUrl));
        
        //action tooltip
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(EditDbModelAction.class, "ACTION_TESTRUN_TOOLTIP"));
        
    }
    
    public void actionPerformed(ActionEvent ev) {
        ETLCollaborationTopComponent topComp = null;
        try {
            topComp = DataObjectProvider.getProvider().getActiveDataObject().getETLEditorTC();
            Thread.currentThread().getContextClassLoader().loadClass(AxionExternalConnectionProvider.class.getName());
        } catch (Exception ex) {
            Logger.printThrowable(Logger.ERROR, TestRunAction.class.getName(), null, "Error loading class:", ex);
        }
        System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME, AxionExternalConnectionProvider.class.getName());
        
        if (topComp != null) {
            topComp.run();
        }
    }
}