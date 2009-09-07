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
/*
 * AquaToolBarButtonUI.java
 *
 * Created on January 17, 2004, 1:54 PM
 */

package org.netbeans.swing.plaf.aqua;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicButtonListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.plaf.basic.BasicButtonUI;

/** A finder-style aqua toolbar button UI
 *
 * @author  Tim Boudreau
 */
class AquaToolBarButtonUI extends BasicButtonUI implements ChangeListener {
    private final boolean isMainToolbarButtonUI;
    private static BasicButtonListener listener = new BasicButtonListener(null);
    
    /** Creates a new instance of AquaToolBarButtonUI */
    public AquaToolBarButtonUI( boolean isMainToolbar ) {
        this.isMainToolbarButtonUI = isMainToolbar;
    }
    
    @Override
    public void installUI (JComponent c) {
        AbstractButton b = (AbstractButton) c;
        b.addMouseListener (listener);
        b.addChangeListener(this);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusable(false);
        b.setBorderPainted(true);
        b.setBorder( BorderFactory.createEmptyBorder(6,2,2,2) );
        b.setRolloverEnabled(isMainToolbarButtonUI);
   }

    @Override
    public void uninstallUI(JComponent c) {
        c.removeMouseListener (listener);
        if( c instanceof AbstractButton )
            ((AbstractButton)c).removeChangeListener(this);
    }
    
    public void stateChanged(ChangeEvent e) {
        ((AbstractButton) e.getSource()).repaint();
    }
    
    private final Rectangle scratch = new Rectangle();
    @Override
    public void paint (Graphics g, JComponent c) {
        Rectangle r = c.getBounds(scratch);
        AbstractButton b = (AbstractButton) c;
        r.x = 0;
        r.y = 0;
        Paint temp = ((Graphics2D) g).getPaint();
        paintBackground ((Graphics2D)g, b, r);
        r.height += b.getInsets().top;
        paintIcon (g, b, r);
        paintText (g, b, r);
        ((Graphics2D) g).setPaint(temp);
    }
    
    
    private FontMetrics fm = null; //We are not setting any custom fonts, can use one
    private void paintText (Graphics g, AbstractButton b, Rectangle r) {
        String s = b.getText();
        if (s == null || s.length() == 0) {
            return;
        }
        g.setColor (b.getForeground());
        Font f = b.getFont();
        if (b.isSelected()) {
            // don't use deriveFont() - see #49973 for details
            f = new Font(f.getName(), Font.BOLD, f.getSize());
        }
        g.setFont (f);
        FontMetrics fontMetrics = g.getFontMetrics();
        if (this.fm == null) {
            this.fm = fontMetrics;
        }        
        int x = 0;
        Icon ic = b.getIcon();
        if (ic != null) {
            x = ic.getIconWidth() + 2;
        } else {
            int w = fontMetrics.stringWidth (s);
            if (w <= r.width) {
                x = (r.width / 2) - (w / 2);
            }
        }
        int h = fontMetrics.getHeight();
        int y = fontMetrics.getMaxAscent();
        if (h <= r.height) {
            y += (r.height / 2) - (h / 2);
        }
        g.drawString (s, x, y);
    }
    
    private void paintBackground (Graphics2D g, AbstractButton b, Rectangle r) {
        if( !(b.isSelected() || b.getModel().isPressed()) )
            return;
        Color c = isMainToolbarButtonUI 
                ? UIManager.getColor("NbBrushedMetal.lightShadow")
                : b.getParent().getBackground();

        Color darker = makeDarker( c );
        Paint p = g.getPaint();
        g.setPaint( new GradientPaint(r.x, r.y, c, r.x, r.height/2, darker));
        g.fillRect(r.x, r.y, r.width, r.height/2);
        g.setPaint( new GradientPaint(r.x, r.y+r.height/2-1, darker, r.x, r.height, c));
        g.fillRect(r.x, r.y+r.height/2-1, r.width, r.height);

        Color evenDarker = makeDarker(darker);
        g.setPaint( new GradientPaint(r.x, r.y, darker, r.x, r.height/2, evenDarker));
        g.fillRect(r.x, r.y, 1, r.height/2);
        g.fillRect(r.x+r.width-1, r.y, 1, r.height/2);

        g.setPaint( new GradientPaint(r.x, r.y+r.height/2-1, evenDarker, r.x, r.height, darker));
        g.fillRect(r.x, r.y+r.height/2-1, 1, r.height);
        g.fillRect(r.x+r.width-1, r.y+r.height/2-1, 1, r.height);

        g.setPaint( new GradientPaint(r.x, r.y, c, r.x, r.height/2, evenDarker));
        g.fillRect(r.x+1, r.y, 1, r.height/2);
        g.fillRect(r.x+r.width-2, r.y, 1, r.height/2);

        g.setPaint( new GradientPaint(r.x, r.y+r.height/2-1, evenDarker, r.x, r.height, c));
        g.fillRect(r.x+1, r.y+r.height/2-1, 1, r.height);
        g.fillRect(r.x+r.width-2, r.y+r.height/2-1, 1, r.height);
    }

    private Color makeDarker( Color c ) {
        int factor = 30;
        return new Color( Math.max(c.getRed()-factor, 0),
                Math.max(c.getGreen()-factor, 0),
                Math.max(c.getRed()-factor, 0));
    }
    
    private void paintIcon (Graphics g, AbstractButton b, Rectangle r) {
        Icon ic = getIconForState (b);
        boolean noText = b.getText() == null || b.getText().length() == 0;
        if (ic != null) {
            int iconX = 0;
            int iconY = 0;
            int iconW = ic.getIconWidth();
            int iconH = ic.getIconHeight();
            
            if (iconW <= r.width && noText) {
                iconX = (r.width / 2) - (iconW / 2);
            }
            if (iconH <= r.height) {
                iconY = (r.height / 2) - (iconH / 2);
            }
            Graphics2D g2d = (Graphics2D) g;
            ic.paintIcon(b, g, iconX, iconY);
            if( isMainToolbarButtonUI && b.getModel().isRollover() && !b.getModel().isPressed()) {
                Composite composite = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT, 0.2f));
                ic.paintIcon(b, g, iconX, iconY);
                g2d.setComposite(composite);
            }
        }
    }
    
    private Icon getIconForState (AbstractButton b) {
        ButtonModel mdl = b.getModel();
        Icon result = null;
        boolean bigIcons = b.getClientProperty("PreferredIconSize") != null; //NOI18N
        Icon shadow = bigIcons ? bigEmptyShadow : smallEmptyShadow;
        if (!b.isEnabled()) {
            result = mdl.isSelected() ? b.getDisabledSelectedIcon() : b.getDisabledIcon();
            if (result == null && mdl.isSelected()) {
                result = b.getDisabledIcon();
            }
            shadow = bigIcons ? bigShadow : smallShadow;
        } else {
            if (mdl.isArmed() && !mdl.isPressed()) {
                result = mdl.isSelected() ? b.getRolloverSelectedIcon() : b.getRolloverIcon();
                if (result == null & mdl.isSelected()) {
                    result = b.getRolloverIcon();
                }
            }
            if (mdl.isPressed()) {
                result = b.getPressedIcon();
            } else if (mdl.isSelected()) {
                result = b.getSelectedIcon();
            } else {
                shadow = bigIcons ? bigShadow : smallShadow;
            }
        }
        if (result == null) {
            result = b.getIcon();
        }
        if( null != result && isMainToolbarButtonUI )
            result = new ShadowedIcon(result, shadow, b.getClientProperty("PreferredIconSize") != null);
        return result;
    }
    
    private static final int minButtonSize = 32;
    @Override
    public Dimension getPreferredSize(JComponent c) {
        AbstractButton b = (AbstractButton) c;
        
        boolean noText = 
            b.getText() == null || 
            b.getText().length() == 0;
            
        Icon ic = getIconForState((AbstractButton) c);
        int w = minButtonSize;
        Dimension result = ic == null ? new Dimension (noText ? 32 : 0, minButtonSize) :
                new Dimension(Math.max(w, ic.getIconWidth()+1), 
                Math.max(minButtonSize,ic.getIconHeight() + 1));
        
        if (!noText) {
            FontMetrics fm = this.fm;
            if (fm == null && c.getGraphicsConfiguration() != null) {
                fm = c.getGraphicsConfiguration().createCompatibleImage(1,1)
                     .getGraphics().getFontMetrics(c.getFont());
            }
            if (fm == null) {
                //init
                fm = new BufferedImage(1, 1, 
                BufferedImage.TYPE_INT_RGB).getGraphics().getFontMetrics(c.getFont());
            }
            result.width += fm.stringWidth(b.getText());
        }
        Insets in = b.getInsets();
        if( null != in ) {
            result.width += in.left + in.right;
            result.height += in.top + in.bottom;
        }
        return result;
    }    

    private static ImageIcon bigShadow = loadShadow("toolbar-icons-shadow-big.png"); //NOI18N
    private static ImageIcon smallShadow = loadShadow("toolbar-icons-shadow-small.png"); //NOI18N

    private static final Icon bigEmptyShadow = new Icon() {
        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        public int getIconWidth() {
            return 33;
        }

        public int getIconHeight() {
            return 13;
        }
    };

    private static final Icon smallEmptyShadow = new Icon() {
        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        public int getIconWidth() {
            return 22;
        }

        public int getIconHeight() {
            return 7;
        }
    };

    private static ImageIcon loadShadow( String name ) {
        try {
            BufferedImage img = ImageIO.read(
                    PlainAquaToolbarUI.class.getResourceAsStream(name));
            return new ImageIcon(img);
        } catch (Exception e) {
            Logger.getLogger(PlainAquaToolbarUI.class.getName()).log(Level.FINE, e.getLocalizedMessage(), e);
            return new ImageIcon( new BufferedImage (1, 1, BufferedImage.TYPE_INT_ARGB) );
        }
    }

    private static class ShadowedIcon implements Icon {

        private final Icon origIcon;
        private final Icon shadowIcon;
        private final boolean bigShadow;

        public ShadowedIcon( Icon origIcon, Icon shadow, boolean bigShadow ) {
            this.origIcon = origIcon;
            shadowIcon = shadow;
            this.bigShadow = bigShadow;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            int shift = (origIcon.getIconWidth() - shadowIcon.getIconWidth()) / 2;
            origIcon.paintIcon(c, g, x + (shift < 0 ? -shift : 0), y);
            shadowIcon.paintIcon(c, g, x + (shift > 0 ? shift : 0), y+origIcon.getIconHeight() - (bigShadow ? 2 : 1));
        }

        public int getIconWidth() {
            return Math.max(origIcon.getIconWidth(), shadowIcon.getIconWidth());
        }

        public int getIconHeight() {
            return origIcon.getIconHeight() + shadowIcon.getIconHeight() - (bigShadow ? 2 : 1);
        }
    }
}
