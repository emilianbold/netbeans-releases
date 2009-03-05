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

package org.netbeans.modules.hudson.util;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.openide.util.Exceptions;

/**
 * Helper utility class
 *
 * @author Michal Mocnak
 */
public class Utilities {
    
    private Utilities() {}
    
    public static boolean isSupportedVersion(HudsonVersion version) {
        // Check for null
        if (null == version)
            return false;
        
        // Version check
        if (version.compareTo(HudsonVersion.SUPPORTED_VERSION) < 0)
            return false;
        
        return true;
    }
    
    public static Graphics2D prepareGraphics(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Map rhints = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
        if( rhints == null && Boolean.getBoolean("swing.aatext") ) { //NOI18N
            g2.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        } else if( rhints != null ) {
            g2.addRenderingHints( rhints );
        }
        return g2;
    }
    
    public static int getDefaultFontSize() {
        Integer customFontSize = (Integer)UIManager.get("customFontSize"); // NOI18N
        if (customFontSize != null) {
            return customFontSize.intValue();
        } else {
            Font systemDefaultFont = UIManager.getFont("TextField.font"); // NOI18N
            return (systemDefaultFont != null)
                    ? systemDefaultFont.getSize()
                    : 12;
        }
    }
    
    public static void invokeInAWTThread(Runnable runnable, boolean wait) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            if (wait) {
                try {
                    SwingUtilities.invokeAndWait(runnable);
                } catch (InterruptedException e) {
                    Exceptions.printStackTrace(e);
                } catch (InvocationTargetException e) {
                    Exceptions.printStackTrace(e);
                }
            } else {
                SwingUtilities.invokeLater(runnable);
            }
        }
    }
    
    public static GridBagConstraints getGridBagConstraints(int gridx, int gridy,
            double weightx, double weighty, int anchor, int fill, int itop,
            int ileft, int ibottom, int iright) {
        // Create a new instance of GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Set constraints
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.anchor = anchor;
        gbc.fill = fill;
        gbc.insets = new Insets(itop, ileft, ibottom, iright);
        
        return gbc;
    }
    
    public static String getURLWithoutSpaces(String url) {
        return url.replaceAll(" ", "%20");
    }
}