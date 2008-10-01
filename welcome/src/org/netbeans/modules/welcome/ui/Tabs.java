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
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.Constants;
import java.awt.Image;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import org.netbeans.modules.welcome.WelcomeOptions;
import org.netbeans.modules.welcome.content.BackgroundPanel;
import org.netbeans.modules.welcome.content.Utils;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author S. Aubrecht
 */
class Tabs extends BackgroundPanel implements Constants {

    private JScrollPane leftComp;
    private JScrollPane rightComp;
    private JComponent leftTab;
    private JComponent rightTab;
    private JPanel tabContent;
    
    public Tabs( String leftTabTitle, JComponent leftTab, 
            String rightTabTitle, final JComponent rightTab) {
        
        super( new BorderLayout() );
        
        this.leftTab = leftTab;
        this.rightTab = rightTab;

        // vlv: print
        leftTab.putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        leftTab.putClientProperty("print.name", leftTabTitle); // NOI18N
        rightTab.putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        rightTab.putClientProperty("print.name", rightTabTitle); // NOI18N
        
        final Tab leftButton = new Tab( leftTabTitle, true );
        final Tab rightButton = new Tab( rightTabTitle, false );
        
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
        
        JPanel buttons = new JPanel( new GridLayout(1,2) );
        buttons.setOpaque(true);
        buttons.add( leftButton );
        buttons.add( rightButton );
        buttons.setBackground( Utils.getColor(COLOR_TAB_UNSEL_BACKGROUND) );
        
        add( buttons, BorderLayout.NORTH );
        
        tabContent = new BackgroundPanel( new CardLayout() );

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
        JScrollPane compToShow = showLeftTab ? leftComp : rightComp;
        JScrollPane compToHide = showLeftTab ? rightComp : leftComp;

        if( null == compToShow ) {
            compToShow = new JScrollPane( showLeftTab ? leftTab : rightTab );
            compToShow.setOpaque( true );
            compToShow.getViewport().setOpaque( true );
            compToShow.getViewport().setBackground(Utils.getColor(COLOR_SCREEN_BACKGROUND));
            compToShow.setBorder( BorderFactory.createEmptyBorder() );

            if( showLeftTab ) {
                leftComp = compToShow;
                tabContent.add( leftComp, "left" ); //NOI18N
            } else {
                rightComp = compToShow;
                tabContent.add( rightComp, "right" ); //NOI18N
            }
        }

        if( null != compToHide )
            compToHide.setVisible( false );
        
        compToShow.setVisible( true );
        compToShow.requestFocusInWindow();
        
        invalidate();
        revalidate();
        repaint();
    }
    
    private static class Tab extends JPanel {
        private boolean isLeftTab;
        private Image imgUnselBottom;
        private Image imgSelLeft;
        private Image imgSelUpperLeft;
        private Image imgSelLowerLeft;
        private Image imgSelRight;
        private Image imgSelUpperRight;
        private Image imgSelLowerRight;
        private boolean isSelected = false;
        private ActionListener actionListener;
        private JLabel lbl;
        
        
        public Tab( String title, boolean isLeftTab ) {
            super( new GridBagLayout() );
            this.isLeftTab = isLeftTab;
            imgUnselBottom = ImageUtilities.loadImage( IMAGE_TAB_UNSEL );
            if( isLeftTab ) {
                imgSelLeft = ImageUtilities.loadImage( IMAGE_TAB_SEL_RIGHT );
                imgSelUpperLeft = ImageUtilities.loadImage( IMAGE_TAB_SEL_UPPER_RIGHT );
                imgSelLowerLeft = ImageUtilities.loadImage( IMAGE_TAB_SEL_LOWER_RIGHT );
            } else {
                imgSelRight = ImageUtilities.loadImage( IMAGE_TAB_SEL_LEFT );
                imgSelUpperRight = ImageUtilities.loadImage( IMAGE_TAB_SEL_UPPER_LEFT );
                imgSelLowerRight = ImageUtilities.loadImage( IMAGE_TAB_SEL_LOWER_LEFT );
            }
            lbl = new JLabel(title);
            lbl.setOpaque( false );
            add( lbl, new GridBagConstraints(0,0,1,1,0.0,0.0,
                    GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(5,5,4,0),0,0) );
            lbl.setFont( TAB_FONT );
            lbl.setForeground( Utils.getColor( isSelected
                    ? COLOR_TAB_SEL_FOREGROUND 
                    : COLOR_TAB_UNSEL_FOREGROUND ) );
            lbl.setHorizontalAlignment( JLabel.CENTER );
            lbl.setFocusable(true);
            
            lbl.addKeyListener( new KeyListener() {

                public void keyTyped(KeyEvent e) {
                }

                public void keyPressed(KeyEvent e) {
                    if( e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER ) {
                        setSelected( !isSelected );
                        if( null != actionListener ) {
                            actionListener.actionPerformed( new ActionEvent( Tab.this, 0, "clicked") );
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
                        actionListener.actionPerformed( new ActionEvent( Tab.this, 0, "clicked") );
                    }
                }

                public void mousePressed(MouseEvent e) {
                }

                public void mouseReleased(MouseEvent e) {
                }

                public void mouseEntered(MouseEvent e) {
                    if( !isSelected ) {
                        setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
                        lbl.setForeground( Utils.getColor( MOUSE_OVER_TAB_COLOR  )  );
                    } else {
                        setCursor( Cursor.getDefaultCursor() );
//                        lbl.setForeground( Utils.getColor( COLOR_TAB_UNSEL_FOREGROUND ) );
                    }
                }

                public void mouseExited(MouseEvent e) {
                    setCursor( Cursor.getDefaultCursor() );
                    lbl.setForeground( Utils.getColor( isSelected
                            ? COLOR_TAB_SEL_FOREGROUND 
                            : COLOR_TAB_UNSEL_FOREGROUND ) );
                }
            });
            
            lbl.addFocusListener( new FocusListener() {

                public void focusGained(FocusEvent e) {
                    lbl.setForeground( Utils.getColor( MOUSE_OVER_LINK_COLOR  )  );
                }

                public void focusLost(FocusEvent e) {
                    lbl.setForeground( Utils.getColor( isSelected
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
            lbl.setForeground( Utils.getColor( lbl.isFocusOwner() 
                    ? MOUSE_OVER_LINK_COLOR
                    : isSelected
                        ? COLOR_TAB_SEL_FOREGROUND 
                        : COLOR_TAB_UNSEL_FOREGROUND ) );
            
            lbl.setFocusable(!sel);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor( Utils.getColor( isSelected ? COLOR_TAB_SEL_BACKGROUND : COLOR_TAB_UNSEL_BACKGROUND ) );
            int width = getWidth();
            int height = getHeight();
            
            g.fillRect( 0, 0, width, height );
            
            if( isSelected ) {
                if( isLeftTab ) {
                    
                    g.setColor( Utils.getColor( COLOR_TAB_UNSEL_BACKGROUND ) );
                    g.fillRect( width-imgSelUpperLeft.getWidth(null), 0, width, height );
                    
                    g.setColor( Utils.getColor( COLOR_TAB_SEL_BACKGROUND ) );
                    int rightImageWidth = imgSelLeft.getWidth(null);
                    g.drawImage(imgSelUpperLeft, width-imgSelUpperLeft.getWidth(null), 0, null);
                    for( int i=0; i<(height-imgSelUpperLeft.getHeight(null)/*-imgSelLowerLeft.getHeight(null)*/); i++ ) {
                        g.drawImage( imgSelLeft, width-rightImageWidth-1, imgSelUpperLeft.getHeight(null)+i, null);
                    }
                    g.fillRect(width-imgSelUpperLeft.getWidth(null), imgSelUpperLeft.getHeight(null), imgSelUpperLeft.getWidth(null)-rightImageWidth, height-imgSelUpperLeft.getHeight(null));
//                    g.drawImage(imgSelLowerLeft, width-imgSelLowerLeft.getWidth(null)-1, height-imgSelLowerLeft.getHeight(null), null);
                    
                } else {
                    
                    g.setColor( Utils.getColor( COLOR_TAB_UNSEL_BACKGROUND ) );
                    g.fillRect( 0, 0, imgSelUpperRight.getWidth(null), height );
                    
                    g.setColor( Utils.getColor( COLOR_TAB_SEL_BACKGROUND ) );
                    g.drawImage(imgSelUpperRight, 0, 0, null);
                    for( int i=0; i<(height-imgSelUpperRight.getHeight(null)/*-imgSelLowerRight.getHeight(null)*/); i++ ) {
                        g.drawImage( imgSelRight, 1, imgSelUpperRight.getHeight(null)+i, null);
                    }
                    g.fillRect(imgSelRight.getWidth(null), imgSelUpperRight.getHeight(null), 
                            imgSelUpperRight.getWidth(null)-imgSelRight.getWidth(null), height-imgSelUpperRight.getHeight(null));
//                    g.drawImage(imgSelLowerRight, 0, height-imgSelLowerRight.getHeight(null), null);
//                    g.fillRect(imgSelLowerRight.getWidth(null), height-imgSelLowerRight.getHeight(null), 
//                            imgSelUpperRight.getWidth(null)-imgSelLowerRight.getWidth(null), imgSelLowerRight.getHeight(null));
                }
            } else {
                int imgWidth = imgUnselBottom.getWidth(null);
                int imgHeight = imgUnselBottom.getHeight(null);
                for( int i=0; i<width/imgWidth+1; i++ ) {
                    g.drawImage( imgUnselBottom, i*imgWidth, height-imgHeight, null );
                }
            }
        }

        @Override
        protected void paintBorder(Graphics g) {
        }
    }
}
