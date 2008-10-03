/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

// TODO: copied from o.n.m.j2ee.common.DatasourceCustomizer. Make it API of j2ee.common and reuse this API here.

package org.netbeans.modules.web.core.palette;

import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.UIManager;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Helper class to simplify setting of error/warning/info messages
 * @author  Petr Slechta
 */
public class MsgHelper {

    private JLabel label;
    private Class<?> clazz;
    private Color nbErrorForeground;
    private Color nbWarningForeground;
    private Color nbInfoForeground;
    private ImageIcon errorIcon;
    private ImageIcon warningIcon;
    private ImageIcon infoIcon;

    /**
     * Creates new instance of MsgHelper
     * @param label JLabel component that is used for presentation of messages
     * @param clazz class that is used to localize message bundle used to get localized
     * version of messages
     */
    public MsgHelper(JLabel label, Class<?> clazz) {
        this.label = label;
        this.clazz = clazz;

        nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null)
            nbErrorForeground = new Color(255, 0, 0);

        nbWarningForeground = UIManager.getColor("nb.warningForeground"); //NOI18N
        if (nbWarningForeground == null)
            nbWarningForeground = new Color(0, 0, 0);

        nbInfoForeground = nbWarningForeground;

        errorIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/j2ee/common/resources/errorIcon.png"));  //NOI18N
        warningIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/j2ee/common/resources/warningIcon.png"));  //NOI18N
        infoIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/j2ee/common/resources/infoIcon.png"));  //NOI18N
    }

    /**
     * Set an error message
     * @param msgKey key from bundle that contains text of error message
     */
    public void setErrorMsg(String msgKey) {
        label.setForeground(nbErrorForeground);
        if (msgKey != null) {
            label.setText(NbBundle.getMessage(clazz, msgKey));
            label.setIcon(errorIcon);
        }
        else {
            label.setText("");  //NOI18N
            label.setIcon(null);
        }
    }

    /**
     * Set an warning message
     * @param msgKey key from bundle that contains text of warning message
     */
    public void setWarningMsg(String msgKey) {
        label.setForeground(nbWarningForeground);
        if (msgKey != null) {
            label.setText(NbBundle.getMessage(clazz, msgKey));
            label.setIcon(warningIcon);
        }
        else {
            label.setText("");  //NOI18N
            label.setIcon(null);
        }
    }

    /**
     * Set an informational message
     * @param msgKey key from bundle that contains text of info message
     */
    public void setInfoMsg(String msgKey) {
        label.setForeground(nbInfoForeground);
        if (msgKey != null) {
            label.setText(NbBundle.getMessage(clazz, msgKey));
            label.setIcon(infoIcon);
        }
        else {
            label.setText("");  //NOI18N
            label.setIcon(null);
        }
    }
}
