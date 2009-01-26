/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package javax.help;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.help.plaf.basic.BasicSearchCellRenderer;
import org.openide.util.Exceptions;

public class ModifiedBasicSearchCellRenderer extends BasicSearchCellRenderer {

    /**
     * Returns a new instance of BasicSearchCellRender.  Left alignment is
     * set. Icons and text color are determined from the
     * UIManager.
     */
    public ModifiedBasicSearchCellRenderer(Map map) {
        super(map);
    }

    private boolean isTagged(SearchTOCItem item) {
        Enumeration searchHits = item.getSearchHits();
        while (searchHits.hasMoreElements()) {
            SearchHit hit = (SearchHit) searchHits.nextElement();
            if (hit.getBegin() >= Integer.MAX_VALUE / 4) {
                return true;
            }
        }
        return false;
    }
    /**
     * Configures the renderer based on the components passed in.
     * Sets the value from messaging value with toString().
     * The foreground color is set based on the selection and the icon
     * is set based on on leaf and expanded.
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel,
            boolean expanded,
            boolean leaf, int row,
            boolean hasFocus) {

        Component ret = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        SearchTOCItem item = (SearchTOCItem) ((DefaultMutableTreeNode) value).getUserObject();
        if (item != null) {
            boolean tagged = isTagged(item);
            if (tagged) {
                try {
                    Method setIconMethod = JLabel.class.getDeclaredMethod("setIcon", new Class[]{Icon.class});
                    setIconMethod.invoke(quality, new Object[] {veryhigh});
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return ret;
    }

    // icons used for the ModifiedBasicSearchCellRenderer
    private static Icon veryhigh = SwingHelpUtilities.getImageIcon(javax.help.plaf.basic.BasicHelpUI.class, "images/SearchVeryHigh.gif");
}
