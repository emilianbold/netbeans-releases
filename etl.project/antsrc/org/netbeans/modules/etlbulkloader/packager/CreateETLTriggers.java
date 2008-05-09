/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.etlbulkloader.packager;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.project.Localizer;


/**
 *
 * @author Manish
 */
public class CreateETLTriggers {

    private static transient final Logger mLogger = Logger.getLogger(CreateETLTriggers.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public CreateETLTriggers() {
        mLogger.infoNoloc(mLoc.t("Create eTL Bulk Loader Triggers ..."));
    }

    public void createBATTrigger() {
    }

    public void createSHTrigger() {
    }
}
