/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.designview.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.accessibility.AccessibleContext;
import javax.swing.JComponent;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class ExUtils {
    
    // Class should not be instantiated
    private ExUtils() {
    }

    public static void setA11Y(JComponent component, String key) {
        setA11Y(component, component.getClass(), key);
    }

    public static void setA11Y(JComponent component, Class bundleClazz,
            String key)
    {
        String aName = NbBundle.getMessage(bundleClazz,
                A11Y_NAME_PREFIX + key);
        String aDescription = NbBundle.getMessage(bundleClazz,
                A11Y_DESCRIPTION_PREFIX + key);

        AccessibleContext aContext = component.getAccessibleContext();

        if (aContext != null) {
            aContext.setAccessibleName(aName);
            aContext.setAccessibleDescription(aDescription);
        }
    }
    
    public static int clip(int value, int minValue, int maxValue) {
        if (minValue == maxValue) {
            return minValue;
        }
        
        if (minValue > maxValue) {
            int t = minValue;
            minValue = maxValue;
            maxValue = t;
        }
        
        return Math.max(minValue, Math.min(value, maxValue));
    }
 
    public static int maxWidth(Dimension... sizes) {
        int result = 0;
        for (Dimension size : sizes) {
            result = Math.max(result, size.width);
        }
        return result;
    }

    public static int maxHeight(Dimension... sizes) {
        int result = 0;
        for (Dimension size : sizes) {
            result = Math.max(result, size.height);
        }
        return result;
    }
    
    public static Graphics2D prepareG2(Graphics g, 
            boolean translateToPixelCenter)
    {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        
        if (translateToPixelCenter) {
            g2.translate(0.5, 0.5);
        }
        
        return g2;
    }
    
    public static void disposeG2(Graphics2D g2, 
            boolean translatedToPixelCenterGraphics2D) 
    {
        if (translatedToPixelCenterGraphics2D) {
            g2.translate(-0.5, -0.5);
        }

        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    public static final String A11Y_NAME_PREFIX="ACSN_"; // NOI18N
    public static final String A11Y_DESCRIPTION_PREFIX="ACSD_"; // NOI18N
}
