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

package org.netbeans.modules.welcome.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.Constants;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;
import org.netbeans.modules.welcome.WelcomeOptions;
import org.netbeans.modules.welcome.content.Utils;
import org.openide.util.ImageUtilities;

/**
 *
 * @author S. Aubrecht
 */
class TabbedPane extends JPanel implements Constants {// , Scrollable {

    private JComponent leftTab;
    private JComponent rightTab;
    private JComponent tabHeader;
    private JPanel tabContent;
    private boolean leftTabAdded = false;
    private boolean rightTabAdded = false;
    
    public TabbedPane( String leftTabTitle, JComponent leftTab,
            String rightTabTitle, final JComponent rightTab) {
        
        super( new BorderLayout() );

        setOpaque(false);
        
        this.leftTab = leftTab;
        this.rightTab = rightTab;

        // vlv: print
        leftTab.putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        leftTab.putClientProperty("print.name", leftTabTitle); // NOI18N
        rightTab.putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        rightTab.putClientProperty("print.name", rightTabTitle); // NOI18N
        
        final TabButton leftButton = new TabButton( leftTabTitle );
        final TabButton rightButton = new TabButton( rightTabTitle );


        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean isLeftTabSelected = e.getSource() == leftButton;
                leftButton.setSelected( isLeftTabSelected );
                rightButton.setSelected( !isLeftTabSelected );
                switchTab( isLeftTabSelected );
                WelcomeOptions.getDefault().setLastActiveTab( isLeftTabSelected ? 0 : 1 );
            }
        };
        
        leftButton.addActionListener( al );
        rightButton.addActionListener( al );
        
        tabHeader = new TabHeader(leftButton, rightButton);
        add( tabHeader, BorderLayout.NORTH );
        
        tabContent = new JPanel( new GridBagLayout() );
        tabContent.setOpaque(false);
        tabContent.setBorder(new ContentBorder());

        add( tabContent, BorderLayout.CENTER );
        int activeTabIndex = WelcomeOptions.getDefault().getLastActiveTab();
        boolean selectLeftTab = activeTabIndex <= 0;
        if( WelcomeOptions.getDefault().isSecondStart() && activeTabIndex < 0 ) {
            selectLeftTab = false;
            WelcomeOptions.getDefault().setLastActiveTab( 1 );
        }
        leftButton.setSelected( selectLeftTab );
        rightButton.setSelected( !selectLeftTab );
        switchTab( selectLeftTab );
    }

    private void switchTab( boolean showLeftTab ) {
        if( showLeftTab && !leftTabAdded ) {
            tabContent.add( leftTab, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0) ); //NOI18N
            leftTabAdded = true;
        } else if( !showLeftTab && !rightTabAdded ) {
            tabContent.add( rightTab, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0) ); //NOI18N
            rightTabAdded = true;
        }
        JComponent compToShow = showLeftTab ? leftTab : rightTab;
        JComponent compToHide = showLeftTab ? rightTab : leftTab;

        if( null != compToHide )
            compToHide.setVisible( false );
        
        compToShow.setVisible( true );
        compToShow.requestFocusInWindow();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if( null != getParent() && null != getParent().getParent() ) {
            Component scroll = getParent().getParent();
            if( scroll.getHeight() > 0 && scroll.getHeight() > d.height )
                d.height = scroll.getHeight();
            if( scroll.getWidth() > 0 ) {
                if( d.width > scroll.getWidth() ) {
                    d.width = Math.max(scroll.getWidth(), START_PAGE_MIN_WIDTH+(int)(((FONT_SIZE-11)/11.0)*START_PAGE_MIN_WIDTH));
                } else if( d.width < scroll.getWidth() ) {
                    d.width = scroll.getWidth();
                }
            }
        }
        d.width = Math.min( d.width, 1000 );
        return d;
    }
    
    private static class TabButton extends JLabel {
        private boolean isSelected = false;
        private ActionListener actionListener;
        
        
        public TabButton( String title ) {
            super( title );
            setOpaque( false );
            setFont( TAB_FONT );
            setForeground( Utils.getColor( isSelected
                    ? COLOR_TAB_SEL_FOREGROUND 
                    : COLOR_TAB_UNSEL_FOREGROUND ) );
            setHorizontalAlignment( JLabel.CENTER );
            setFocusable(true);
            
            addKeyListener( new KeyListener() {

                public void keyTyped(KeyEvent e) {
                }

                public void keyPressed(KeyEvent e) {
                    if( e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER ) {
                        setSelected( !isSelected );
                        if( null != actionListener ) {
                            actionListener.actionPerformed( new ActionEvent( TabButton.this, 0, "clicked") );
                        }
                    }
                }

                public void keyReleased(KeyEvent e) {
                }
            });

            addMouseListener( new MouseListener() {
                public void mouseClicked(MouseEvent e) {
                    setSelected( !isSelected );
                    if( null != actionListener ) {
                        actionListener.actionPerformed( new ActionEvent( TabButton.this, 0, "clicked") );
                    }
                }

                public void mousePressed(MouseEvent e) {
                }

                public void mouseReleased(MouseEvent e) {
                }

                public void mouseEntered(MouseEvent e) {
                    if( !isSelected ) {
                        setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
                        setForeground( Utils.getColor( MOUSE_OVER_TAB_COLOR  )  );
                    } else {
                        setCursor( Cursor.getDefaultCursor() );
//                        lbl.setForeground( Utils.getColor( COLOR_TAB_UNSEL_FOREGROUND ) );
                    }
                }

                public void mouseExited(MouseEvent e) {
                    setCursor( Cursor.getDefaultCursor() );
                    setForeground( Utils.getColor( isSelected
                            ? COLOR_TAB_SEL_FOREGROUND 
                            : COLOR_TAB_UNSEL_FOREGROUND ) );
                }
            });
            
            addFocusListener( new FocusListener() {

                public void focusGained(FocusEvent e) {
                    setForeground( Utils.getColor( MOUSE_OVER_LINK_COLOR  )  );
                }

                public void focusLost(FocusEvent e) {
                    setForeground( Utils.getColor( isSelected
                            ? COLOR_TAB_SEL_FOREGROUND 
                            : COLOR_TAB_UNSEL_FOREGROUND ) );
                }
            });
        }
        
        public void addActionListener( ActionListener l ) {
            assert null == actionListener;
            this.actionListener = l;
        }
        
        public void setSelected( boolean sel ) {
            this.isSelected = sel;
            setForeground( Utils.getColor( isFocusOwner() 
                    ? MOUSE_OVER_LINK_COLOR
                    : isSelected
                        ? COLOR_TAB_SEL_FOREGROUND 
                        : COLOR_TAB_UNSEL_FOREGROUND ) );
            
            setFocusable(!sel);
            if( null != getParent() )
                getParent().repaint();
        }
    }


    private static class TabHeader extends JPanel {
        private Image topLeftCornerSel;
        private Image topLeftCornerUnsel;
        private Image topRightCornerSel;
        private Image topRightCornerUnsel;
        private Image topGradientSel;
        private Image topGradientUnsel;
        private Image topMiddleLeftSel;
        private Image topMiddleRightSel;
        private Image middleShadowRightSel;
        private Image middleShadowLeftSel;
        private Image leftShadow;
        private Image rightShadow;
        private Image bottomShadowUnsel;

        private final TabButton leftButton;
        private final TabButton rightButton;

        public TabHeader( TabButton leftButton, TabButton rightButton ) {
            super( new GridLayout(1,0) );
            setOpaque(false);
            this.leftButton = leftButton;
            this.rightButton = rightButton;
            Border b = BorderFactory.createEmptyBorder( 8, 19, 2, 24 );
            leftButton.setBorder(b);
            rightButton.setBorder(b);
            add( leftButton );
            add( rightButton );

            topLeftCornerSel = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_top_left_corner_sel.png"); //NOI18N
            topLeftCornerUnsel = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_top_left_corner_unsel.png"); //NOI18N
            topRightCornerSel = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_top_right_corner_sel.png"); //NOI18N
            topRightCornerUnsel = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_top_right_corner_unsel.png"); //NOI18N
            topGradientUnsel = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_top_gradient_unsel.png"); //NOI18N
            topGradientSel = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_top_gradient_sel.png"); //NOI18N
            leftShadow = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/left_shadow.png"); //NOI18N
            rightShadow = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/right_shadow.png"); //NOI18N
            bottomShadowUnsel = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_bottom_shadow_unsel.png"); //NOI18N
            topMiddleRightSel = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_top_middle_right_sel.png"); //NOI18N
            topMiddleLeftSel = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_top_middle_left_sel.png"); //NOI18N
            middleShadowRightSel = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_middle_shadow_right_sel.png"); //NOI18N
            middleShadowLeftSel = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_middle_shadow_left_sel.png"); //NOI18N
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            boolean isLeftSelection = leftButton.isSelected;

            int height = getHeight();
            int width = getWidth();
            Color background = isLeftSelection ? Color.white : new Color(193,193,193);
            Image topGradient = isLeftSelection ? topGradientSel : topGradientUnsel;
            //left tab background
            g2d.setColor(background);
            int backgroundWidth = width/2-4;
            if( isLeftSelection )
                backgroundWidth -= 4;
            g2d.fillRect(4, 21, backgroundWidth, height-21);
            //left tab top gradient
            int gradientWidth = width/2-19;
            if( isLeftSelection )
                gradientWidth -= 22;
            g2d.drawImage(topGradient, 19, 0, gradientWidth, 21, this);
            //top left corner curve
            g2d.drawImage(isLeftSelection ? topLeftCornerSel : topLeftCornerUnsel, 0, 0, this);
            //left shadow
            g2d.drawImage(leftShadow, 0, 21, 4, height-21, this);


            if( isLeftSelection ) {
                //middle section, left tab is selected
                g2d.drawImage(topMiddleLeftSel, width/2-22, 0, this);
                g2d.drawImage(middleShadowLeftSel, width/2-4, 24, 4, height-24, this );
            } else {
                //middle section, right tab is selected
                g2d.drawImage(topMiddleRightSel, width/2, 0, this);
                g2d.drawImage(middleShadowRightSel, width/2, 24, 4, height-24, this );
            }

            //top right corner curve
            g2d.drawImage(isLeftSelection ? topRightCornerUnsel : topRightCornerSel, width-20, 0, this);
            int rightCornerHeight = isLeftSelection ? 21 : 24;
            //right shadow
            g2d.drawImage(rightShadow, width-6, rightCornerHeight, 6, height-rightCornerHeight, this);

            background = !isLeftSelection ? Color.white : new Color(193,193,193);
            topGradient = !isLeftSelection ? topGradientSel : topGradientUnsel;
            //right tab background
            g2d.setColor(background);
            int backgroundStart = width/2;
            if( !isLeftSelection )
                backgroundStart += 4;
            g2d.fillRect(backgroundStart, 21, width-backgroundStart-6, height-21);
            //right tab top gradient
            int gradientStart = width/2;
            if( !isLeftSelection )
                gradientStart += 22;
            g2d.drawImage(topGradient, gradientStart, 0, width-gradientStart-20, 21, this);

            //bottom shadow of unselected tab
            if( isLeftSelection ) {
                g2d.drawImage(bottomShadowUnsel, width/2, height-4, width/2-6, 5, this);
            } else {
                g2d.drawImage(bottomShadowUnsel, 4, height-4, width/2, 5, this);
            }
        }

        @Override
        protected void paintBorder(Graphics g) {
        }
    }
}
