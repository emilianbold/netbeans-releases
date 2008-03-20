/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.sql.framework.ui.graph.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.view.graph.actions.TestRunAction;
import org.openide.util.HelpCtx;

/**
 * Abstract action class to register accelerator keys with top component.
 * This is a wrapper around TestRunAction class.
 *
 * @author karthikeyan s
 */
public class RunAction extends AbstractAction {

    private static final String LOG_CATEGORY = RunAction.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(RunAction.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public String getName() {
        String nbBundle = mLoc.t("BUND035: Run");
        return nbBundle.substring(15);
    }

    protected String iconResource() {
        return "/org/netbeans/modules/sql/framework/ui/resources/images/runCollaboration.png";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }

    public void actionPerformed(ActionEvent e) {
        TestRunAction action = new TestRunAction();
        action.actionPerformed(e);
    }
}
