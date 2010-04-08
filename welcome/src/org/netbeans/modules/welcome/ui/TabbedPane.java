/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import java.awt.Rectangle;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.util.Arrays;
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

    private final JComponent[] tabs;
    private final TabButton[] buttons;
    private final JComponent tabHeader;
    private final JPanel tabContent;
    private boolean[] tabAdded;
    private int selTabIndex = -1;
    
    public TabbedPane( JComponent ... tabs ) {
        super( new BorderLayout() );

        setOpaque(false);
        
        this.tabs = tabs;
        tabAdded = new boolean[tabs.length];
        Arrays.fill(tabAdded, false);

        // vlv: print
        for( JComponent c : tabs ) {
            c.putClientProperty("print.printable", Boolean.TRUE); // NOI18N
            c.putClientProperty("print.name", c.getName()); // NOI18N
        }
        
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TabButton btn = (TabButton) e.getSource();
                switchTab( btn.getTabIndex() );
                WelcomeOptions.getDefault().setLastActiveTab( btn.getTabIndex() );
            }
        };
        
        buttons = new TabButton[tabs.length];
        for( int i=0; i<buttons.length; i++ ) {
            buttons[i] = new TabButton(tabs[i].getName(), i);
            buttons[i].addActionListener(al);
        }

        
        tabHeader = new TabHeader(buttons);
        add( tabHeader, BorderLayout.NORTH );
        
        tabContent = new TabContentPane();//JPanel( new GridBagLayout() );
//        tabContent.setOpaque(false);
//        tabContent.setBorder(new ContentBorder());

        add( tabContent, BorderLayout.CENTER );
        int activeTabIndex = WelcomeOptions.getDefault().getLastActiveTab();
        if( WelcomeOptions.getDefault().isSecondStart() && activeTabIndex < 0 ) {
            activeTabIndex = 1;
            WelcomeOptions.getDefault().setLastActiveTab( 1 );
        }
        activeTabIndex = Math.max(0, activeTabIndex);
        activeTabIndex = Math.min(activeTabIndex, tabs.length-1);
//        buttons[activeTabIndex].setSelected(true);
        switchTab( activeTabIndex );
    }

    private void switchTab( int tabIndex ) {
        if( !tabAdded[tabIndex] ) {
            tabContent.add( tabs[tabIndex], new GridBagConstraints(tabIndex, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0) ); //NOI18N
            tabAdded[tabIndex] = true;
        }
        if( selTabIndex >= 0 ) {
            buttons[selTabIndex].setSelected(false);
        }
        JComponent compToShow = tabs[tabIndex];
        JComponent compToHide = selTabIndex >= 0 ? tabs[selTabIndex] : null;
        selTabIndex = tabIndex;
        buttons[selTabIndex].setSelected(true);

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
        private final int tabIndex;
        
        public TabButton( String title, int tabIndex ) {
            super( title );
            this.tabIndex = tabIndex;
            setOpaque( false );
            setFont( TAB_FONT );
            setForeground( Utils.getColor( isSelected
                    ? COLOR_TAB_SEL_FOREGROUND 
                    : COLOR_TAB_UNSEL_FOREGROUND ) );
            setHorizontalAlignment( JLabel.CENTER );
            setFocusable(true);
            
            addKeyListener( new KeyAdapter() {

                @Override
                public void keyPressed(KeyEvent e) {
                    if( e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER ) {
//                        setSelected( !isSelected );
                        if( null != actionListener ) {
                            actionListener.actionPerformed( new ActionEvent( TabButton.this, 0, "clicked") );
                        }
                    }
                }
            });

            addMouseListener( new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
//                    setSelected( !isSelected );
                    if( null != actionListener ) {
                        actionListener.actionPerformed( new ActionEvent( TabButton.this, 0, "clicked") );
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if( !isSelected ) {
                        setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
                        setForeground( Utils.getColor( MOUSE_OVER_TAB_COLOR  )  );
                    } else {
                        setCursor( Cursor.getDefaultCursor() );
//                        lbl.setForeground( Utils.getColor( COLOR_TAB_UNSEL_FOREGROUND ) );
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setCursor( Cursor.getDefaultCursor() );
                    setForeground( Utils.getColor( isSelected
                            ? COLOR_TAB_SEL_FOREGROUND 
                            : COLOR_TAB_UNSEL_FOREGROUND ) );
                }
            });
            
            addFocusListener( new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    setForeground( Utils.getColor( MOUSE_OVER_LINK_COLOR  )  );
                }
                @Override
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

        public int getTabIndex() {
            return tabIndex;
        }
    }

    private static final Color COLOR_UNSEL = new Color(173,175,176);
    private static final Color COLOR_SEL = new Color(255,255,255);

    private static final Insets insets = new Insets(23, 19, 6, 10);

    private class TabHeader extends JPanel {
        // ABBBBBBBBBBBBBBBCBBBBBBBBBBBBBBBBD
        // 5666666666666666788888888888888889
        // 4               5                6
        // 4 <fill color>  5  <fill color>  6
        // 4               5                6
        // 4               5                6
        // 0111111111111111211111111111111113

        private final Image[] sel;
        private final Image[] unsel;

        private final TabButton[] buttons;

        private final Image imgSelTopLeft1 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_sel_topleft1.png"); //NOI18N
        private final Image imgSelTopLeft2 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_sel_topleft2.png"); //NOI18N
        private final Image imgSelTop = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_sel_top.png"); //NOI18N
        private final Image imgSelLeft1 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_sel_left1.png"); //NOI18N
        private final Image imgSelLeft2 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_sel_left2.png"); //NOI18N
        private final Image imgSelTopRight1 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_sel_topright1.png"); //NOI18N
        private final Image imgSelTopRight2 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_sel_topright2.png"); //NOI18N
        private final Image imgSelRight1 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_sel_right1.png"); //NOI18N
        private final Image imgSelRight2 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_sel_right2.png"); //NOI18N

        private final Image imgUnselTopLeft1 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_topleft1.png"); //NOI18N
        private final Image imgUnselLeft1 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_left1.png"); //NOI18N
        private final Image imgUnselLeftTop1 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_lefttop1.png"); //NOI18N
        private final Image imgUnselBottomLeft1 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_bottomleft1.png"); //NOI18N

        private final Image imgUnselTopRight1 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_topright1.png"); //NOI18N
        private final Image imgUnselRight1 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_right1.png"); //NOI18N
        private final Image imgUnselRightTop1 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_righttop1.png"); //NOI18N
        private final Image imgUnselBottomRight1 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_bottomright1.png"); //NOI18N

        private final Image imgUnselTopLeft2 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_topleft2.png"); //NOI18N
        private final Image imgUnselLeft2 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_left2.png"); //NOI18N
        private final Image imgUnselBottomLeft2 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_bottomleft2.png"); //NOI18N

        private final Image imgUnselTopRight2 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_topright2.png"); //NOI18N
        private final Image imgUnselRight2 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_right2.png"); //NOI18N
        private final Image imgUnselBottomRight2 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_bottomright2.png"); //NOI18N

        private final Image imgUnselTopRight3 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_topright3.png"); //NOI18N
        private final Image imgUnselRight3 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_right3.png"); //NOI18N
        private final Image imgUnselBottomRight3 = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_bottomright3.png"); //NOI18N

        private final Image imgUnselTop = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_top.png"); //NOI18N
        private final Image imgUnselBottom = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_bottom.png"); //NOI18N
        private final Image imgUnselFiller = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_filler.png"); //NOI18N

        public TabHeader( TabButton ... buttons ) {
            super( new GridLayout(1,0) );
            setOpaque(false);
            this.buttons = buttons;
            Border b = BorderFactory.createEmptyBorder( 12, 19, 2, 24 );
            for( TabButton btn : buttons ) {
                btn.setBorder(b);
                add( btn );
            }

            sel = new Image[13];
            unsel = new Image[13];
            for( int i=0; i<sel.length; i++ ) {
                sel[i] = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_sel_"+i+".png");
                unsel[i] = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/tab_unsel_"+i+".png");
            }
        }

        private void paintTab( Graphics2D g, int tabIndex ) {
            int width = getWidth();
            int height = getHeight();
            boolean selected = tabIndex == selTabIndex;
            int tabWidth = width / buttons.length;
            int x = tabWidth * tabIndex;
            if( tabIndex == buttons.length-1 )
                tabWidth += width % buttons.length;

            if( selected ) {
                Image imgTopLeft = tabIndex == 0 ? imgSelTopLeft1 : imgSelTopLeft2;
                g.drawImage(imgTopLeft, x, 0, this);
                int topLeftWidth = imgTopLeft.getWidth(this);
                int topLeftHeight = imgTopLeft.getHeight(this);

                Image imgTopRight = tabIndex == buttons.length-1 ? imgSelTopRight1 : imgSelTopRight2;
                int topRightWidth = imgTopRight.getWidth(this);
                int topRightHeight = imgTopRight.getHeight(this);
                g.drawImage(imgTopRight, x+tabWidth-topRightWidth, 0, this);

                g.drawImage(imgSelTop, x+topLeftWidth, 0, tabWidth-topLeftWidth-topRightWidth, imgSelTop.getHeight(this), this);

                Image imgLeft = tabIndex == 0 ? imgSelLeft1 : imgSelLeft2;
                int leftWidth = imgLeft.getWidth(this);
                g.drawImage(imgLeft, x, topLeftHeight, leftWidth, height-topLeftHeight, this);

                Image imgRight = tabIndex == buttons.length-1 ? imgSelRight1 : imgSelRight2;
                int rightWidth = imgRight.getWidth(this);
                g.drawImage(imgRight, x+tabWidth-rightWidth, topRightHeight, rightWidth, height-topRightHeight, this);

                g.setColor(COLOR_SEL);
                g.fillRect(x+leftWidth, topLeftHeight, tabWidth-leftWidth-rightWidth, height-topLeftHeight);
            } else {
                Rectangle fillRect = new Rectangle(x, 0, tabWidth, height);
                //left arc
                if( tabIndex == 0 ) {
                    g.drawImage(imgUnselTopLeft1, x, 0, this);
                    int topHeight = imgUnselTopLeft1.getHeight(this);
                    int topWidth = imgUnselTopLeft1.getWidth(this);
                    fillRect.x += topWidth;
                    fillRect.width -= topWidth;
                    g.drawImage(imgUnselLeftTop1, x, topHeight, this);
                    topHeight += imgUnselLeftTop1.getHeight(this);
                    fillRect.y += topHeight;
                    fillRect.height -= topHeight;

                    int bottomHeight = imgUnselBottomLeft1.getHeight(this);
                    g.drawImage(imgUnselBottomLeft1, x, height-bottomHeight, this);

                    g.drawImage(imgUnselLeft1, x, topHeight, imgUnselLeft1.getWidth(this), height-topHeight-bottomHeight, this);

                } else {
                    int topHeight = imgUnselTopLeft1.getHeight(this);
                    topHeight += imgUnselLeftTop1.getHeight(this);
                    fillRect.y += topHeight;
                    fillRect.height -= topHeight;

                    g.drawImage(imgUnselTopLeft2, x, 0, this);
                    topHeight = imgUnselTopLeft2.getHeight(this);
                    int topWidth = imgUnselTopLeft2.getWidth(this);
                    fillRect.x += topWidth;
                    fillRect.width -= topWidth;

                    int bottomHeight = imgUnselBottomLeft2.getHeight(this);
                    g.drawImage(imgUnselBottomLeft2, x, height-bottomHeight, this);

                    g.drawImage(imgUnselLeft2, x, topHeight, imgUnselLeft2.getWidth(this), height-topHeight-bottomHeight, this);
                }

                //right arc
                if( tabIndex == buttons.length-1 ) {
                    int topHeight = imgUnselTopRight1.getHeight(this);
                    int topWidth = imgUnselTopRight1.getWidth(this);
                    g.drawImage(imgUnselTopRight1, x+tabWidth-topWidth, 0, this);
                    fillRect.width -= topWidth;
                    g.drawImage(imgUnselRightTop1, x+tabWidth-topWidth, topHeight, this);
                    topHeight += imgUnselRightTop1.getHeight(this);

                    int bottomHeight = imgUnselBottomRight1.getHeight(this);
                    g.drawImage(imgUnselBottomRight1, x+tabWidth-topWidth, height-bottomHeight, this);

                    g.drawImage(imgUnselRight1, x+tabWidth-topWidth, topHeight, imgUnselRight1.getWidth(this), height-topHeight-bottomHeight, this);

                } else if( tabIndex+1 == selTabIndex ) {
                    int topHeight = imgUnselTopRight2.getHeight(this);
                    int topWidth = imgUnselTopRight2.getWidth(this);
                    g.drawImage(imgUnselTopRight2, x+tabWidth-topWidth, 0, this);
                    fillRect.width -= topWidth;

                    int bottomHeight = imgUnselBottomRight2.getHeight(this);
                    g.drawImage(imgUnselBottomRight2, x+tabWidth-topWidth, height-bottomHeight, this);

                    g.drawImage(imgUnselRight2, x+tabWidth-topWidth, topHeight, imgUnselRight2.getWidth(this), height-topHeight-bottomHeight, this);
                } else {
                    int topHeight = imgUnselTopRight3.getHeight(this);
                    int topWidth = imgUnselTopRight3.getWidth(this);
                    g.drawImage(imgUnselTopRight3, x+tabWidth-topWidth, 0, this);
                    fillRect.width -= topWidth;

                    int bottomHeight = imgUnselBottomRight3.getHeight(this);
                    g.drawImage(imgUnselBottomRight3, x+tabWidth-topWidth, height-bottomHeight, this);

                    g.drawImage(imgUnselRight3, x+tabWidth-topWidth, topHeight, imgUnselRight3.getWidth(this), height-topHeight-bottomHeight, this);
                }
                
                fillRect.height -= imgUnselBottom.getHeight(this);
                g.drawImage(imgUnselFiller, fillRect.x, fillRect.y-imgUnselFiller.getHeight(this), fillRect.width, imgUnselFiller.getHeight(this), this);
                g.drawImage(imgUnselTop, fillRect.x, 0, fillRect.width, imgUnselTop.getHeight(this), this);
                g.drawImage(imgUnselBottom, fillRect.x, height-imgUnselBottom.getHeight(this), fillRect.width, imgUnselBottom.getHeight(this), this);

                g.setColor(COLOR_UNSEL);
                g.fill(fillRect);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;

            for( int i=0; i<buttons.length; i++ ) {
                paintTab( g2d, i );
            }
//            boolean isLeftSelection = false;//leftButton.isSelected;
//
//            int height = getHeight();
//            int width = getWidth();
//            Color background = isLeftSelection ? Color.white : new Color(193,193,193);
//            Image topGradient = isLeftSelection ? topGradientSel : topGradientUnsel;
//            //left tab background
//            g2d.setColor(background);
//            int backgroundWidth = width/2-4;
//            if( isLeftSelection )
//                backgroundWidth -= 4;
//            g2d.fillRect(4, 21, backgroundWidth, height-21);
//            //left tab top gradient
//            int gradientWidth = width/2-19;
//            if( isLeftSelection )
//                gradientWidth -= 22;
//            g2d.drawImage(topGradient, 19, 0, gradientWidth, 21, this);
//            //top left corner curve
//            g2d.drawImage(isLeftSelection ? topLeftCornerSel : topLeftCornerUnsel, 0, 0, this);
//            //left shadow
//            g2d.drawImage(leftShadow, 0, 21, 4, height-21, this);
//
//
//            if( isLeftSelection ) {
//                //middle section, left tab is selected
//                g2d.drawImage(topMiddleLeftSel, width/2-22, 0, this);
//                g2d.drawImage(middleShadowLeftSel, width/2-4, 24, 4, height-24, this );
//            } else {
//                //middle section, right tab is selected
//                g2d.drawImage(topMiddleRightSel, width/2, 0, this);
//                g2d.drawImage(middleShadowRightSel, width/2, 24, 4, height-24, this );
//            }
//
//            //top right corner curve
//            g2d.drawImage(isLeftSelection ? topRightCornerUnsel : topRightCornerSel, width-20, 0, this);
//            int rightCornerHeight = isLeftSelection ? 21 : 24;
//            //right shadow
//            g2d.drawImage(rightShadow, width-6, rightCornerHeight, 6, height-rightCornerHeight, this);
//
//            background = !isLeftSelection ? Color.white : new Color(193,193,193);
//            topGradient = !isLeftSelection ? topGradientSel : topGradientUnsel;
//            //right tab background
//            g2d.setColor(background);
//            int backgroundStart = width/2;
//            if( !isLeftSelection )
//                backgroundStart += 4;
//            g2d.fillRect(backgroundStart, 21, width-backgroundStart-6, height-21);
//            //right tab top gradient
//            int gradientStart = width/2;
//            if( !isLeftSelection )
//                gradientStart += 22;
//            g2d.drawImage(topGradient, gradientStart, 0, width-gradientStart-20, 21, this);
//
//            //bottom shadow of unselected tab
//            if( isLeftSelection ) {
//                g2d.drawImage(bottomShadowUnsel, width/2, height-4, width/2-6, 5, this);
//            } else {
//                g2d.drawImage(bottomShadowUnsel, 4, height-4, width/2, 5, this);
//            }
        }

        @Override
        protected void paintBorder(Graphics g) {
        }
    }
}
