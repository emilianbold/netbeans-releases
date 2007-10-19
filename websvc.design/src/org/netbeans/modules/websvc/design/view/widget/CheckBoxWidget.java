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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;

/**
 *
 * @author Ajit
 */
public class CheckBoxWidget extends ButtonWidget {
    
    /** The expand button image. */
    private static final Image IMAGE_CHECKBOX = new BufferedImage(8, 8,
            BufferedImage.TYPE_INT_ARGB);
    /** The collapse button image. */
    private static final Image IMAGE_CHECKBOX_SELECTED = new BufferedImage(8, 8,
            BufferedImage.TYPE_INT_ARGB);
    private boolean isSelected;

    public static final String ACTION_COMMAND_SELECTED = "toggle-button-selected";
    public static final String ACTION_COMMAND_DESELECTED = "toggle-button-deselected";

    static {

        // Create the checkbox image.
        Graphics2D g2 = ((BufferedImage) IMAGE_CHECKBOX).createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        float w = IMAGE_CHECKBOX.getWidth(null);
        float h = IMAGE_CHECKBOX.getHeight(null);
        Rectangle2D gp = new Rectangle2D.Double(0,0,w,h);
        g2.setPaint(Color.WHITE);
        g2.fill(gp);
        g2.setPaint(Color.GRAY);
        g2.draw(gp);

        // Create the checkbox selected image.
        g2 = ((BufferedImage) IMAGE_CHECKBOX_SELECTED).createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        w = IMAGE_CHECKBOX_SELECTED.getWidth(null);
        h = IMAGE_CHECKBOX_SELECTED.getHeight(null);
        gp = new Rectangle2D.Double(0,0,w,h);
        g2.setPaint(Color.WHITE);
        g2.fill(gp);
        g2.setPaint(Color.GRAY);
        g2.draw(gp);
        gp = new Rectangle2D.Double(1.5,1.5,w-3,h-3);
        g2.fill(gp);
    }

    /**
     *
     * @param scene
     * @param text
     */
    public CheckBoxWidget(Scene scene, String text) {
        super(scene, IMAGE_CHECKBOX, text);
        setSelectedImage(IMAGE_CHECKBOX_SELECTED);
        setBorder(BorderFactory.createEmptyBorder(1));
    }

    public void performAction() {
        setSelected(!isSelected());
        super.performAction();
    }
    
    public String getActionCommand() {
        return isSelected()?ACTION_COMMAND_SELECTED:ACTION_COMMAND_DESELECTED;
    }

    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        if (previousState.isFocused() != state.isFocused()) {
            setBorder(state.isFocused()?BorderFactory.createDashedBorder
                    (BORDER_COLOR, 2, 2, true):BorderFactory.createEmptyBorder(1));
        }
        super.notifyStateChanged(previousState,state);
    }
    
}
