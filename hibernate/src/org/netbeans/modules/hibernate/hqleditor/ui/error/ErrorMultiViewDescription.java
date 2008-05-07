package org.netbeans.modules.hibernate.hqleditor.ui.error;

import java.awt.Image;
import java.io.Serializable;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

public class ErrorMultiViewDescription implements MultiViewDescription, Serializable {

    static final String ICON_PATH = "org/netbeans/modules/hibernate/hqleditor/ui/resources/queryEditor.gif"; //NOI18N


    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    public String getDisplayName() {
        return "Error";
    }

    public Image getIcon() {
        return Utilities.loadImage(ICON_PATH, true);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public String preferredID() {
        return "HQLEditorOutput-error";
    }

    public MultiViewElement createElement() {
        return new ErrorMultiViewElement();
    }
}
