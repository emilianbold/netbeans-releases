package org.netbeans.modules.etl.ui.view.graph.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.net.URL;
import javax.swing.Action;
import javax.swing.ImageIcon;

import javax.swing.KeyStroke;
import org.axiondb.ExternalConnectionProvider;
import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopPanel;
import org.netbeans.modules.sql.framework.ui.graph.actions.GraphAction;
import org.netbeans.modules.sql.framework.ui.utils.AxionExternalConnectionProvider;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


public final class TestRunAction extends GraphAction {

    private static final URL runIconUrl = TestRunAction.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/runCollaboration.png");
    private static transient final Logger mLogger = Logger.getLogger(TestRunAction.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public TestRunAction() {
        //action name
        String nbBundle1 = mLoc.t("BUND035: Run");
        this.putValue(Action.NAME,nbBundle1.substring(15));

        //action 
        this.putValue(Action.SMALL_ICON, new ImageIcon(runIconUrl));

        //action tooltip
        String nbBundle2 = mLoc.t("BUND036: Run Collaboration (Alt+Shift+N)");
        this.putValue(Action.SHORT_DESCRIPTION,nbBundle2.substring(15));

        // Acceleratot Shift-N
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('N', InputEvent.SHIFT_DOWN_MASK+InputEvent.ALT_MASK));
    }

    public void actionPerformed(ActionEvent ev) {
        ETLCollaborationTopPanel topComp = null;
        try {
            topComp = DataObjectProvider.getProvider().getActiveDataObject().getETLEditorTopPanel();
            Thread.currentThread().getContextClassLoader().loadClass(AxionExternalConnectionProvider.class.getName());
        } catch (Exception ex) {
            mLogger.errorNoloc(mLoc.t("EDIT026: Error loading class:{0}", TestRunAction.class.getName()), ex);
        }
        System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME, AxionExternalConnectionProvider.class.getName());

        if (topComp != null) {
            topComp.run();
        }
    }
}
