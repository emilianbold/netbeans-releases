/*
 * The contents of this file are subject to the terms of the Common
 * Development
The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
particular file as subject to the "Classpath" exception as provided
by Sun in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved.

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
 *
 */
package org.netbeans.modules.sql.framework.ui.graph.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.net.URL;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.DataObjectProvider;



import org.netbeans.modules.etl.ui.view.ETLCollaborationTopPanel;

/**
 *
 * @author karthikeyan s
 */
public class ZoomInAction extends GraphAction {

    private static final URL zoomInImgUrl = ZoomInAction.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/zoom_in_edm.png");
    private static transient final Logger mLogger = Logger.getLogger(ZoomInAction.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public ZoomInAction() {
        //action name
        String nbBundle = mLoc.t("BUND321: Zoom In");
        this.putValue(Action.NAME, nbBundle.substring(15));

        //action icon
        this.putValue(Action.SMALL_ICON, new ImageIcon(zoomInImgUrl));

        //action tooltip
        String nbBundle1 = mLoc.t("BUND321: Zoom In");
        this.putValue(Action.SHORT_DESCRIPTION, nbBundle1.substring(15));
        // Acceleratot Shift-Q
    }

    public void actionPerformed(ActionEvent e) {
        ETLCollaborationTopPanel etlEditor = null;
        try {
            etlEditor = DataObjectProvider.getProvider().getActiveDataObject().getETLEditorTopPanel();
        } catch (Exception ex) {
            // ignore
        }
        if (etlEditor != null) {
            etlEditor.setZoomFactor(etlEditor.getZoomFactor() * 1.1);
        }
    }
}
