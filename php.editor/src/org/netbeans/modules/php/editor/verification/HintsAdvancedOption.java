/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */

package org.netbeans.modules.php.editor.verification;

import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.HintsProvider.HintsManager;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 */
public class HintsAdvancedOption extends AdvancedOption {

    OptionsPanelController panelController;

    public String getDisplayName() {
        return NbBundle.getMessage(HintsAdvancedOption.class, "CTL_Hints_DisplayName"); // NOI18N
    }

    public String getTooltip() {
        return NbBundle.getMessage(HintsAdvancedOption.class, "CTL_Hints_ToolTip"); // NOI18N
    }

    public synchronized OptionsPanelController create() {
        if ( panelController == null ) {
            HintsManager manager = HintsProvider.HintsManager.getManagerForMimeType(FileUtils.PHP_MIME_TYPE);
            assert manager != null;
            panelController = manager.getOptionsController();
        }
        
        return panelController;
    }

    //TODO: temporary solution, this should be solved on GSF level
    public static  OptionsPanelController createStatic(){
        return new HintsAdvancedOption().create();
    }
}
