/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * SmallSwatchChooserPanel.java
 *
 * Created on February 18, 2004, 3:36 PM
 */
package org.netbeans.modules.mobility.project.ui.customizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;

/**
 *
 * @author  gc149856
 */
public class SmallSwatchChooserPanel extends AbstractColorChooserPanel {
    
    SwatchPanel swatchPanel;
    MouseListener mainSwatchListener;

    public String getDisplayName() {
        return UIManager.getString("ColorChooser.swatchesNameText"); //NOI18N
    }
    
    /**
     * Provides a hint to the look and feel as to the
     * <code>KeyEvent.VK</code> constant that can be used as a mnemonic to
     * access the panel. A return value <= 0 indicates there is no mnemonic.
     * <p>
     * The return value here is a hint, it is ultimately up to the look
     * and feel to honor the return value in some meaningful way.
     * <p>
     * This implementation looks up the value from the default
     * <code>ColorChooser.swatchesMnemonic</code>, or if it
     * isn't available (or not an <code>Integer</code>) returns -1.
     * The lookup for the default is done through the <code>UIManager</code>:
     * <code>UIManager.get("ColorChooser.swatchesMnemonic");</code>.
     *
     * @return KeyEvent.VK constant identifying the mnemonic; <= 0 for no
     *         mnemonic
     * @see #getDisplayedMnemonicIndex
     * @since 1.4
     */
    public int getMnemonic() {
        return 0;
    }
    
    /**
     * Provides a hint to the look and feel as to the index of the character in
     * <code>getDisplayName</code> that should be visually identified as the
     * mnemonic. The look and feel should only use this if
     * <code>getMnemonic</code> returns a value > 0.
     * <p>
     * The return value here is a hint, it is ultimately up to the look
     * and feel to honor the return value in some meaningful way. For example,
     * a look and feel may wish to render each
     * <code>AbstractColorChooserPanel</code> in a <code>JTabbedPane</code>,
     * and further use this return value to underline a character in
     * the <code>getDisplayName</code>.
     * <p>
     * This implementation looks up the value from the default
     * <code>ColorChooser.rgbDisplayedMnemonicIndex</code>, or if it
     * isn't available (or not an <code>Integer</code>) returns -1.
     * The lookup for the default is done through the <code>UIManager</code>:
     * <code>UIManager.get("ColorChooser.swatchesDisplayedMnemonicIndex");</code>.
     *
     * @return Character index to render mnemonic for; -1 to provide no
     *                   visual identifier for this panel.
     * @see #getMnemonic
     * @since 1.4
     */
    public int getDisplayedMnemonicIndex() {
        return 0;
    }
    
    public Icon getSmallDisplayIcon() {
        return null;
    }
    
    public Icon getLargeDisplayIcon() {
        return null;
    }
        
    protected void buildChooser() {
        
        final JPanel superHolder = new JPanel(new BorderLayout());
        
        swatchPanel =  new MainSwatchPanel();
        swatchPanel.getAccessibleContext().setAccessibleName(getDisplayName());
        
        mainSwatchListener = new MainSwatchListener();
        swatchPanel.addMouseListener(mainSwatchListener);
        
        
        final JPanel mainHolder = new JPanel(new BorderLayout());
        final Border border = new CompoundBorder( new LineBorder(Color.black),
                new LineBorder(Color.white) );
        mainHolder.setBorder(border);
        mainHolder.add(swatchPanel, BorderLayout.CENTER);
        superHolder.add( mainHolder, BorderLayout.CENTER );
        
        add(superHolder);
        
    }
    
    public void uninstallChooserPanel(final JColorChooser enclosingChooser) {
        super.uninstallChooserPanel(enclosingChooser);
        swatchPanel.removeMouseListener(mainSwatchListener);
        swatchPanel = null;
        mainSwatchListener = null;
        removeAll();  // strip out all the sub-components
    }
    
    public void updateChooser() {
        
    }
    
    
    class MainSwatchListener extends MouseAdapter {
        public void mousePressed(final MouseEvent e) {
            final Color color = swatchPanel.getColorForLocation(e.getX(), e.getY());
            getColorSelectionModel().setSelectedColor(color);
            
        }
    }
    
    
    class SwatchPanel extends JPanel {
        
        protected Color[] colors;
        protected Dimension swatchSize;
        protected Dimension numSwatches;
        protected Dimension gap;
        
        public SwatchPanel() {
            initValues();
            initColors();
            setToolTipText(""); // register for events // NOI18N
            setOpaque(true);
            setBackground(Color.white);
            setRequestFocusEnabled(false);
        }
        
        public boolean isFocusable() {
            return false;
        }
        
        protected void initValues() {
            
        }
        
        public void paintComponent(final Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0,0,getWidth(), getHeight());
            for (int row = 0; row < numSwatches.height; row++) {
                for (int column = 0; column < numSwatches.width; column++) {
                    
                    g.setColor( getColorForCell(column, row) );
                    final int x = column * (swatchSize.width + gap.width);
                    final int y = row * (swatchSize.height + gap.height);
                    g.fillRect( x, y, swatchSize.width, swatchSize.height);
                    g.setColor(Color.black);
                    g.drawLine( x+swatchSize.width-1, y, x+swatchSize.width-1, y+swatchSize.height-1);
                    g.drawLine( x, y+swatchSize.height-1, x+swatchSize.width-1, y+swatchSize.height-1);
                }
            }
        }
        
        public Dimension getPreferredSize() {
            final int x = numSwatches.width * (swatchSize.width + gap.width) -1;
            final int y = numSwatches.height * (swatchSize.height + gap.height) -1;
            return new Dimension( x, y );
        }
        
        protected void initColors() {
            
            
        }
        
        public String getToolTipText(final MouseEvent e) {
            final Color color = getColorForLocation(e.getX(), e.getY());
            return color.getRed()+", "+ color.getGreen() + ", " + color.getBlue(); //NOI18N
        }
        
        public Color getColorForLocation( final int x, final int y ) {
            final int column = x / (swatchSize.width + gap.width);
            
            final int row = y / (swatchSize.height + gap.height);
            return getColorForCell(column, row);
        }
        
        private Color getColorForCell( final int column, final int row) {
            
            return colors[ (row * numSwatches.width) + column ];
        }
    }
    
    class MainSwatchPanel extends SwatchPanel {
        
        static final int width=5;
        static final int height=3;
        
        protected void initValues() {
            swatchSize = UIManager.getDimension("ColorChooser.swatchesSwatchSize"); //NOI18N
            numSwatches = new Dimension( width, height );
            gap = new Dimension(1, 1);
        }
        
        protected void initColors() {
            final int[] rawValues = initRawValues();
            final int numColors = rawValues.length / 3;
            
            colors = new Color[numColors];
            for (int i = 0; i < numColors ; i++) {
                colors[i] = new Color( rawValues[(i*3)], rawValues[(i*3)+1], rawValues[(i*3)+2] );
            }
        }
        
        private int[] initRawValues() {
            
            final int[] rawValues = {
                255,255,255,// first row
                204,255,255,
                204,204,255,
                255,204,204,
                204,255,204,
                204,204,204, //second row
                153,153,255,
                255,153,153,
                255,153,153,
                153,255,153,
                102,102,255,//third row
                255,102,204,
                255,255,102,
                102,255,102,
                102,255,204};
            return rawValues;
        }
        
    }
    
}
