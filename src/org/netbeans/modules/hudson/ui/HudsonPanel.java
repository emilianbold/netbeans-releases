/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.hudson.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * Decorated panel
 *
 * @author S. Aubrecht (original)
 * @authoe Michal Mocnak
 */
public class HudsonPanel extends JPanel implements PropertyChangeListener, MouseListener {
    
    private static final String SEL_HEADER_TOP_LEFT_IMAGE = "org/netbeans/modules/hudson/ui/resources/t_topleft.png"; // NOI18N
    private static final String SEL_HEADER_TOP_RIGHT_IMAGE = "org/netbeans/modules/hudson/ui/resources/t_topright.png"; // NOI18N
    private static final String SEL_HEADER_BOTTOM_LEFT_IMAGE = "org/netbeans/modules/hudson/ui/resources/t_bottomleft.png"; // NOI18N
    private static final String SEL_HEADER_BOTTOM_RIGHT_IMAGE = "org/netbeans/modules/hudson/ui/resources/t_bottomright.png"; // NOI18N
    private static final String SEL_HEADER_TOP_IMAGE = "org/netbeans/modules/hudson/ui/resources/t_top.png"; // NOI18N
    private static final String SEL_HEADER_BOTTOM_IMAGE = "org/netbeans/modules/hudson/ui/resources/t_bottom.png"; // NOI18N
    private static final String SEL_HEADER_LEFT_IMAGE = "org/netbeans/modules/hudson/ui/resources/t_left.png"; // NOI18N
    private static final String SEL_HEADER_RIGHT_IMAGE = "org/netbeans/modules/hudson/ui/resources/t_right.png"; // NOI18N
    
    private static final String SEL_FOOTER_TOP_LEFT_IMAGE = "org/netbeans/modules/hudson/ui/resources/b_topleft.png"; // NOI18N
    private static final String SEL_FOOTER_TOP_RIGHT_IMAGE = "org/netbeans/modules/hudson/ui/resources/b_topright.png"; // NOI18N
    private static final String SEL_FOOTER_BOTTOM_LEFT_IMAGE = "org/netbeans/modules/hudson/ui/resources/b_bottomleft.png"; // NOI18N
    private static final String SEL_FOOTER_BOTTOM_RIGHT_IMAGE = "org/netbeans/modules/hudson/ui/resources/b_bottomright.png"; // NOI18N
    private static final String SEL_FOOTER_TOP_IMAGE = "org/netbeans/modules/hudson/ui/resources/b_top.png"; // NOI18N
    private static final String SEL_FOOTER_BOTTOM_IMAGE = "org/netbeans/modules/hudson/ui/resources/b_bottom.png"; // NOI18N
    private static final String SEL_FOOTER_LEFT_IMAGE = "org/netbeans/modules/hudson/ui/resources/b_left.png"; // NOI18N
    private static final String SEL_FOOTER_RIGHT_IMAGE = "org/netbeans/modules/hudson/ui/resources/b_right.png"; // NOI18N
    
    private static final String SEL_LEFT_SIDE_IMAGE = "org/netbeans/modules/hudson/ui/resources/leftside.png"; // NOI18N
    private static final String SEL_RIGHT_SIDE_IMAGE = "org/netbeans/modules/hudson/ui/resources/rightside.png"; // NOI18N
    
    private static final String DESEL_HEADER_TOP_LEFT_IMAGE = "org/netbeans/modules/hudson/ui/resources/desel_t_topleft.png"; // NOI18N
    private static final String DESEL_HEADER_TOP_RIGHT_IMAGE = "org/netbeans/modules/hudson/ui/resources/desel_t_topright.png"; // NOI18N
    private static final String DESEL_HEADER_BOTTOM_LEFT_IMAGE = "org/netbeans/modules/hudson/ui/resources/desel_t_bottomleft.png"; // NOI18N
    private static final String DESEL_HEADER_BOTTOM_RIGHT_IMAGE = "org/netbeans/modules/hudson/ui/resources/desel_t_bottomright.png"; // NOI18N
    private static final String DESEL_HEADER_TOP_IMAGE = "org/netbeans/modules/hudson/ui/resources/desel_t_top.png"; // NOI18N
    private static final String DESEL_HEADER_BOTTOM_IMAGE = "org/netbeans/modules/hudson/ui/resources/desel_t_bottom.png"; // NOI18N
    private static final String DESEL_HEADER_LEFT_IMAGE = "org/netbeans/modules/hudson/ui/resources/desel_t_left.png"; // NOI18N
    private static final String DESEL_HEADER_RIGHT_IMAGE = "org/netbeans/modules/hudson/ui/resources/desel_t_right.png"; // NOI18N
    
    private static final String DESEL_FOOTER_TOP_LEFT_IMAGE = "org/netbeans/modules/hudson/ui/resources/desel_b_topleft.png"; // NOI18N
    private static final String DESEL_FOOTER_TOP_RIGHT_IMAGE = "org/netbeans/modules/hudson/ui/resources/desel_b_topright.png"; // NOI18N
    private static final String DESEL_FOOTER_BOTTOM_LEFT_IMAGE = "org/netbeans/modules/hudson/ui/resources/desel_b_bottomleft.png"; // NOI18N
    private static final String DESEL_FOOTER_BOTTOM_RIGHT_IMAGE = "org/netbeans/modules/hudson/ui/resources/desel_b_bottomright.png"; // NOI18N
    private static final String DESEL_FOOTER_TOP_IMAGE = "org/netbeans/modules/hudson/ui/resources/desel_b_top.png"; // NOI18N
    private static final String DESEL_FOOTER_BOTTOM_IMAGE = "org/netbeans/modules/hudson/ui/resources/desel_b_bottom.png"; // NOI18N
    private static final String DESEL_FOOTER_LEFT_IMAGE = "org/netbeans/modules/hudson/ui/resources/desel_b_left.png"; // NOI18N
    private static final String DESEL_FOOTER_RIGHT_IMAGE = "org/netbeans/modules/hudson/ui/resources/desel_b_right.png"; // NOI18N
    
    private static final String DESEL_LEFT_SIDE_IMAGE = "org/netbeans/modules/hudson/ui/resources/desel_leftside.png"; // NOI18N
    private static final String DESEL_RIGHT_SIDE_IMAGE = "org/netbeans/modules/hudson/ui/resources/desel_rightside.png"; // NOI18N
    
    private JLabel lblTitle;
    private JComponent content;
    private JComponent bottomContent;
    
    protected final ImageIcon hTopLeftInFocus = new ImageIcon( Utilities.loadImage( SEL_HEADER_TOP_LEFT_IMAGE ) );
    protected final ImageIcon hTopRightInFocus = new ImageIcon( Utilities.loadImage( SEL_HEADER_TOP_RIGHT_IMAGE ) );
    protected final ImageIcon hBottomLeftInFocus = new ImageIcon( Utilities.loadImage( SEL_HEADER_BOTTOM_LEFT_IMAGE ) );
    protected final ImageIcon hBottomRightInFocus = new ImageIcon( Utilities.loadImage( SEL_HEADER_BOTTOM_RIGHT_IMAGE ) );
    protected final ImageIcon hTopInFocus = new ImageIcon( Utilities.loadImage( SEL_HEADER_TOP_IMAGE ) );
    protected final ImageIcon hBottomInFocus = new ImageIcon( Utilities.loadImage( SEL_HEADER_BOTTOM_IMAGE ) );
    protected final ImageIcon hLeftInFocus = new ImageIcon( Utilities.loadImage( SEL_HEADER_LEFT_IMAGE ) );
    protected final ImageIcon hRightInFocus = new ImageIcon( Utilities.loadImage( SEL_HEADER_RIGHT_IMAGE ) );
    
    protected final ImageIcon leftSideInFocus = new ImageIcon( Utilities.loadImage( SEL_LEFT_SIDE_IMAGE ) );
    protected final ImageIcon rightSideInFocus = new ImageIcon( Utilities.loadImage( SEL_RIGHT_SIDE_IMAGE ) );
    
    protected final ImageIcon fTopLeftInFocus = new ImageIcon( Utilities.loadImage( SEL_FOOTER_TOP_LEFT_IMAGE ) );
    protected final ImageIcon fTopRightInFocus = new ImageIcon( Utilities.loadImage( SEL_FOOTER_TOP_RIGHT_IMAGE ) );
    protected final ImageIcon fBottomLeftInFocus = new ImageIcon( Utilities.loadImage( SEL_FOOTER_BOTTOM_LEFT_IMAGE ) );
    protected final ImageIcon fBottomRightInFocus = new ImageIcon( Utilities.loadImage( SEL_FOOTER_BOTTOM_RIGHT_IMAGE ) );
    protected final ImageIcon fTopInFocus = new ImageIcon( Utilities.loadImage( SEL_FOOTER_TOP_IMAGE ) );
    protected final ImageIcon fBottomInFocus = new ImageIcon( Utilities.loadImage( SEL_FOOTER_BOTTOM_IMAGE ) );
    protected final ImageIcon fLeftInFocus = new ImageIcon( Utilities.loadImage( SEL_FOOTER_LEFT_IMAGE ) );
    protected final ImageIcon fRightInFocus = new ImageIcon( Utilities.loadImage( SEL_FOOTER_RIGHT_IMAGE ) );
    
    protected final ImageIcon hTopLeftDeselect = new ImageIcon( Utilities.loadImage( DESEL_HEADER_TOP_LEFT_IMAGE ) );
    protected final ImageIcon hTopRightDeselect = new ImageIcon( Utilities.loadImage( DESEL_HEADER_TOP_RIGHT_IMAGE ) );
    protected final ImageIcon hBottomLeftDeselect = new ImageIcon( Utilities.loadImage( DESEL_HEADER_BOTTOM_LEFT_IMAGE ) );
    protected final ImageIcon hBottomRightDeselect = new ImageIcon( Utilities.loadImage( DESEL_HEADER_BOTTOM_RIGHT_IMAGE ) );
    protected final ImageIcon hTopDeselect = new ImageIcon( Utilities.loadImage( DESEL_HEADER_TOP_IMAGE ) );
    protected final ImageIcon hBottomDeselect = new ImageIcon( Utilities.loadImage( DESEL_HEADER_BOTTOM_IMAGE ) );
    protected final ImageIcon hLeftDeselect = new ImageIcon( Utilities.loadImage( DESEL_HEADER_LEFT_IMAGE ) );
    protected final ImageIcon hRightDeselect = new ImageIcon( Utilities.loadImage( DESEL_HEADER_RIGHT_IMAGE ) );
    
    protected final ImageIcon leftSideDeselect = new ImageIcon( Utilities.loadImage( DESEL_LEFT_SIDE_IMAGE ) );
    protected final ImageIcon rightSideDeselect = new ImageIcon( Utilities.loadImage( DESEL_RIGHT_SIDE_IMAGE ) );
    
    protected final ImageIcon fTopLeftDeselect = new ImageIcon( Utilities.loadImage( DESEL_FOOTER_TOP_LEFT_IMAGE ) );
    protected final ImageIcon fTopRightDeselect = new ImageIcon( Utilities.loadImage( DESEL_FOOTER_TOP_RIGHT_IMAGE ) );
    protected final ImageIcon fBottomLeftDeselect = new ImageIcon( Utilities.loadImage( DESEL_FOOTER_BOTTOM_LEFT_IMAGE ) );
    protected final ImageIcon fBottomRightDeselect = new ImageIcon( Utilities.loadImage( DESEL_FOOTER_BOTTOM_RIGHT_IMAGE ) );
    protected final ImageIcon fTopDeselect = new ImageIcon( Utilities.loadImage( DESEL_FOOTER_TOP_IMAGE ) );
    protected final ImageIcon fBottomDeselect = new ImageIcon( Utilities.loadImage( DESEL_FOOTER_BOTTOM_IMAGE ) );
    protected final ImageIcon fLeftDeselect = new ImageIcon( Utilities.loadImage( DESEL_FOOTER_LEFT_IMAGE ) );
    protected final ImageIcon fRightDeselect = new ImageIcon( Utilities.loadImage( DESEL_FOOTER_RIGHT_IMAGE ) );
    
    private boolean focusedBorder = false;
    
    public HudsonPanel(String title) {
        super(new GridBagLayout());
        
        lblTitle = new JLabel( title );
        lblTitle.setFont(new Font(null, Font.BOLD, org.netbeans.modules.hudson.util.Utilities.getDefaultFontSize()));
        lblTitle.setForeground(new Color(Integer.decode("0x0E1B55")));
        lblTitle.setHorizontalAlignment(JLabel.LEFT);
        lblTitle.setOpaque( false );
        
        int vertFill = 3;
        
        if( lblTitle.getPreferredSize().height+2*vertFill < hTopInFocus.getIconHeight()+hBottomInFocus.getIconHeight() ) {
            vertFill = (hTopInFocus.getIconHeight()+hBottomInFocus.getIconHeight()-lblTitle.getPreferredSize().height)/2;
        }
        
        add(lblTitle, new GridBagConstraints( 0,0,1,1,1.0,0.0,
                GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,
                new Insets(vertFill, 10, vertFill, 10),0,0));
        
        setBackground(new Color(Integer.decode("0xE8DDC2")));
        setOpaque(false);
        
        KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        kfm.addPropertyChangeListener( WeakListeners.propertyChange(this, kfm));
        
        addMouseListener(this);
    }
    
    public void setContent( JComponent c ) {
        if( null != content ) {
            remove( content );
            content.removeMouseListener( this );
        }
        this.content = c;
        if( null != content ) {
            add( content, new GridBagConstraints( 0,1,1,1,1.0,1.0,
                    GridBagConstraints.CENTER,GridBagConstraints.BOTH,
                    new Insets(0,3,0,3),0,0) );
            content.addMouseListener( this );
        }
    }
    
    public void setBottomContent( JComponent c ) {
        bottomContent = c;
        
        if(null != bottomContent) {
            int topFill = 5;
            int bottomFill = 10;
            int imageHeight = fTopInFocus.getIconHeight()+fBottomInFocus.getIconHeight();
            int prefHeight = bottomContent.getPreferredSize().height;
            
            if( prefHeight+topFill+bottomFill < imageHeight ) {
                topFill = (imageHeight-prefHeight)/2;
                bottomFill = (imageHeight-prefHeight)/2 + (imageHeight-prefHeight)%2;
            }
            
            add( bottomContent, new GridBagConstraints( 0,2,1,1,1.0,0.0,
                    GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,
                    new Insets(topFill, 10, bottomFill, 10), 0, 0));
            
            bottomContent.addMouseListener( this );
        }
    }
    
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = org.netbeans.modules.hudson.util.Utilities.prepareGraphics(g);
        
        g2.setColor(new Color(Integer.decode("0xFFFFFF")));
        
        int header = getHeaderContentHeight();
        int footer = getFooterHeight();
        
        g2.fillRect(0, header, getWidth(), getHeight()-header-footer);
        
        paintTop(g2);
        paintBottom(g2);
        paintSides(g2);
    }
    
    private void paintTop(Graphics2D g2) {
        int width = getWidth();
        int contentHeight = getHeaderContentHeight();
        
        ImageIcon hTop = focusedBorder ? hTopInFocus : hTopDeselect;
        ImageIcon hBottom = focusedBorder ? hBottomInFocus : hBottomDeselect;
        ImageIcon hTopLeft = focusedBorder ? hTopLeftInFocus : hTopLeftDeselect;
        ImageIcon hBottomLeft = focusedBorder ? hBottomLeftInFocus : hBottomLeftDeselect;
        ImageIcon hTopRight = focusedBorder ? hTopRightInFocus : hTopRightDeselect;
        ImageIcon hBottomRight = focusedBorder ? hBottomRightInFocus : hBottomRightDeselect;
        ImageIcon hRight = focusedBorder ? hRightInFocus : hRightDeselect;
        ImageIcon hLeft = focusedBorder ? hLeftInFocus : hLeftDeselect;
        
        int imageHeight = hTop.getIconHeight()+hBottom.getIconHeight();
        int headerHeight = getHeaderContentHeight();
        int fillHeight = 0;
        if( imageHeight > contentHeight ) {
            headerHeight = imageHeight;
        } else {
            fillHeight = headerHeight - imageHeight;
        }
        //left side
        g2.drawImage( hTopLeft.getImage(), 0, 0, null );
        g2.drawImage( hBottomLeft.getImage(), 0, headerHeight-hBottomLeft.getIconHeight(), null );
        if( fillHeight > 0 ) {
            g2.drawImage( hLeft.getImage(), 0, hTopLeft.getIconHeight(), hLeft.getIconWidth(), fillHeight, null );
        }
        
        //right side
        g2.drawImage( hTopRight.getImage(), width-hTopRight.getIconWidth(), 0, null );
        g2.drawImage( hBottomRight.getImage(), width-hBottomRight.getIconWidth(), headerHeight-hBottomRight.getIconHeight(), null );
        if( fillHeight > 0 ) {
            g2.drawImage( hRight.getImage(), width-hBottomRight.getIconWidth(), hTopRight.getIconHeight(), hRight.getIconWidth(), fillHeight, null );
        }
        
        //top
        g2.drawImage( hTop.getImage(), hTopLeft.getIconWidth(), 0, width-hTopLeft.getIconWidth()-hTopRight.getIconWidth(), hTop.getIconHeight(), null );
        
        //bottom
        g2.drawImage( hBottom.getImage(), hBottomLeft.getIconWidth(), headerHeight-hBottom.getIconHeight(), width-hBottomLeft.getIconWidth()-hBottomRight.getIconWidth(), hBottom.getIconHeight(), null );
        
        //fill
        if( fillHeight > 0 ) {
            g2.setColor(new Color(Integer.decode("0xFBFCFD")));
            g2.fillRect( hLeft.getIconWidth(), hTopLeft.getIconHeight(), width-hLeft.getIconWidth()-hRight.getIconWidth(), fillHeight );
        }
    }
    
    private void paintBottom(Graphics2D g2) {
        int width = getWidth();
        int height = getHeight();
        
        ImageIcon fTop = focusedBorder ? fTopInFocus : fTopDeselect;
        ImageIcon fBottom = focusedBorder ? fBottomInFocus : fBottomDeselect;
        ImageIcon fTopLeft = focusedBorder ? fTopLeftInFocus : fTopLeftDeselect;
        ImageIcon fBottomLeft = focusedBorder ? fBottomLeftInFocus : fBottomLeftDeselect;
        ImageIcon fTopRight = focusedBorder ? fTopRightInFocus : fTopRightDeselect;
        ImageIcon fBottomRight = focusedBorder ? fBottomRightInFocus : fBottomRightDeselect;
        ImageIcon fRight = focusedBorder ? fRightInFocus : fRightDeselect;
        ImageIcon fLeft = focusedBorder ? fLeftInFocus : fLeftDeselect;
        
        int contentHeight = getFooterContentHeight();
        int imageHeight = null == bottomContent ? fBottom.getIconHeight() : fTop.getIconHeight()+fBottom.getIconHeight();
        int footerHeight = getFooterContentHeight();
        int fillHeight = null == bottomContent ? 0 : 1;
        if( imageHeight > contentHeight ) {
            footerHeight = imageHeight;
        } else {
            fillHeight = footerHeight - imageHeight;
        }
        //left side
        g2.drawImage( fBottomLeft.getImage(), 0, height-fBottomLeft.getIconHeight(), null );
        if( fillHeight > 0 ) {
            g2.drawImage( fTopLeft.getImage(), 0, height-footerHeight, null );
            g2.drawImage( fLeft.getImage(), 0, height-footerHeight+fTopLeft.getIconHeight(), fLeft.getIconWidth(), fillHeight, null );
        }
        
        //right side
        g2.drawImage( fBottomRight.getImage(), width-fTopRight.getIconWidth(), height-fBottomRight.getIconHeight(), null );
        if( fillHeight > 0 ) {
            g2.drawImage( fTopRight.getImage(), width-fTopRight.getIconWidth(), height-footerHeight, null );
            g2.drawImage( fRight.getImage(), width-fRight.getIconWidth(), height-footerHeight+fTopRight.getIconHeight(), fRight.getIconWidth(), fillHeight, null );
        }
        
        //bottom
        g2.drawImage( fBottom.getImage(), fBottomLeft.getIconWidth(), height-fBottom.getIconHeight(), width-fBottomLeft.getIconWidth()-fBottomRight.getIconWidth(), fBottom.getIconHeight(), null );
        
        //fill
        if( fillHeight > 0 ) {
            g2.setColor(new Color(Integer.decode("0xFFFFFF")));
            g2.fillRect( fLeft.getIconWidth(), height-footerHeight+fTop.getIconHeight(), width-fLeft.getIconWidth()-fRight.getIconWidth(), fillHeight );
            
            //top
            g2.drawImage( fTop.getImage(), fTopLeft.getIconWidth(), height-footerHeight, width-fTopLeft.getIconWidth()-fTopRight.getIconWidth(), fTop.getIconHeight(), null );
        }
    }
    
    private void paintSides(Graphics2D g2) {
        ImageIcon leftSide = focusedBorder ? leftSideInFocus : leftSideDeselect;
        ImageIcon rightSide = focusedBorder ? rightSideInFocus : rightSideDeselect;
        
        g2.drawImage( leftSide.getImage(), 0, getHeaderHeight(),
                leftSide.getIconWidth(), getHeight()-getHeaderHeight()-getFooterHeight(), null );
        g2.drawImage( rightSide.getImage(), getWidth()-rightSide.getIconWidth(), getHeaderHeight(),
                rightSide.getIconWidth(), getHeight()-getHeaderHeight()-getFooterHeight(), null );
    }
    
    private int getHeaderContentHeight() {
        Insets insets = lblTitle.getInsets();
        return lblTitle.getHeight()+insets.top+insets.bottom;
    }
    
    private int getHeaderHeight() {
        return Math.max( getHeaderContentHeight(), hTopInFocus.getIconHeight()+hBottomInFocus.getIconHeight() );
    }
    
    private int getFooterContentHeight() {
        if( null == bottomContent )
            return 0;
        GridBagConstraints constr = ((GridBagLayout)getLayout()).getConstraints( bottomContent );
        return bottomContent.getHeight()+constr.insets.top+constr.insets.bottom;
    }
    
    private int getFooterHeight() {
        if( null == bottomContent )
            return fBottomInFocus.getIconHeight();
        return Math.max( getFooterContentHeight(), fTopInFocus.getIconHeight()+fBottomInFocus.getIconHeight() );
    }
    
    private static Component lastFocusOwner = null;
    private boolean isFocusOwner = false;
    
    public void propertyChange( PropertyChangeEvent evt ) {
        if( "focusOwner".equals( evt.getPropertyName() ) && evt.getNewValue() instanceof Component ) { //NOI18N
            Component focusOwner = (Component)evt.getNewValue();
            HudsonPanel focusAncestor = (HudsonPanel)SwingUtilities.getAncestorOfClass( HudsonPanel.class, focusOwner  );
            
            
            isFocusOwner = null != focusAncestor && HudsonPanel.this == focusAncestor;
            
            if( isFocusOwner )
                lastFocusOwner = focusOwner;
            
            if( isFocusOwner != focusedBorder ) {
                focusedBorder = isFocusOwner;
                lblTitle.setForeground(new Color(Integer.decode(focusedBorder ? "0xD96702" : "0x0E1B55")));
                getParent().repaint();
            }
        }
    }
    
    public void switchFocus() {
        if( isFocusOwner )
            return;
        if( null == lastFocusOwner || !lastFocusOwner.isShowing() ) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent( this );
        } else {
            lastFocusOwner.requestFocusInWindow();
        }
    }
    
    protected void requestAttention() {
        focusedBorder = true;
        lblTitle.setForeground(new Color(Integer.decode("0xD96702")));
        getParent().repaint();
    }
    
    public void mouseClicked(MouseEvent e) {}
    
    public void mousePressed(MouseEvent e) {
        lastFocusOwner = null;
        switchFocus();
    }
    
    public void mouseReleased(MouseEvent e) {}
    
    public void mouseEntered(MouseEvent e) {}
    
    public void mouseExited(MouseEvent e) {}
}