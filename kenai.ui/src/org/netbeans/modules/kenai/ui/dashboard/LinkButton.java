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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui.dashboard;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;

/**
 * Button with no border painted looking like a hypertext link.
 *
 * @author S. Aubrecht
 */
public class LinkButton extends JButton {

    /**
     * C'tor
     * @param img Image to create button's icon from
     * @param al Action to invoke when the button is pressed, can be null but
     * the button is disabled then.
     */
    public LinkButton( Image img, Action a ) {
        this( new ImageIcon(img), a );
    }

    /**
     * C'tor
     * @param icon
     * @param al Action to invoke when the button is pressed, can be null but
     * the button is disabled then.
     */
    public LinkButton( Icon icon, Action a ) {
        setIcon(icon);
        setPressedIcon(icon);

        init(a);
    }

    /**
     * C'tor
     * @param text 
     * @param icon
     * @param al Action to invoke when the button is pressed, can be null but
     * the button is disabled then.
     */
    public LinkButton( String text, Icon icon, Action a ) {
        super(text);
        setIcon(icon);
        setPressedIcon(icon);
        Object tooltip = a.getValue(Action.SHORT_DESCRIPTION);
        if( null != tooltip )
            setToolTipText(tooltip.toString());
        init(a);
    }


    /**
     * C'tor
     * @param text
     * @param al Action to invoke when the button is pressed, can be null but
     * the button is disabled then.
     */
    public LinkButton( String text, Action a ) {
        super( text );

        if( null != a ) {
            Icon icon = (Icon) a.getValue(Action.SMALL_ICON);
            if( null != icon ) {
                setIcon(icon);
                setPressedIcon(icon);
            }
            Object tooltip = a.getValue(Action.SHORT_DESCRIPTION);
            if( null != tooltip )
                setToolTipText(tooltip.toString());
        }
        init(a);
    }

    /**
     * Adjust foreground color
     * @param foreground Preferred color
     * @param isSelected True if the button is selected.
     */
    public void setForeground( Color foreground, boolean isSelected ) {
        if( isSelected ) {
            if( foreground instanceof UIResource )
                foreground = new Color(foreground.getRGB());
            setForeground(foreground);
        } else if( isEnabled() ) {
            setForeground(ColorManager.getDefault().getLinkColor());
        } else {
            setForeground(ColorManager.getDefault().getDisabledColor());
        }
    }

    private void init( Action al ) {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        setBorderPainted(false);
        setFocusPainted(false);
        setFocusable(false);
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setModel( new Model() );
        if( null != al ) {
            addActionListener(al);
            setForeground(ColorManager.getDefault().getLinkColor());
        } else {
            setEnabled(false);
            setForeground(ColorManager.getDefault().getDisabledColor());
        }
        setFont(UIManager.getFont("Tree.font"));//NOI18N
    }

    private static class Model extends DefaultButtonModel {

        @Override
        public boolean isPressed() {
            return false;
        }
    }
}
