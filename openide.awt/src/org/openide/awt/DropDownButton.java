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

package org.openide.awt;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * JButton with a small arrow that displays popup menu when clicked.
 * 
 * @author S. Aubrecht
 * @since 6.11
 */
class DropDownButton extends JButton {
    
    private boolean mouseInArrowArea = false;
    
    private Map<String,Icon> regIcons = new HashMap<String,Icon>( 5 );
    private Map<String,IconWithArrow> arrowIcons = new HashMap<String,IconWithArrow>( 5 );
    
    private static final String ICON_NORMAL = "normal"; //NOI18N
    private static final String ICON_PRESSED = "pressed"; //NOI18N
    private static final String ICON_ROLLOVER = "rollover"; //NOI18N
    private static final String ICON_ROLLOVER_SELECTED = "rolloverSelected"; //NOI18N
    private static final String ICON_SELECTED = "selected"; //NOI18N
    private static final String ICON_DISABLED = "disabled"; //NOI18N
    private static final String ICON_DISABLED_SELECTED = "disabledSelected"; //NOI18N
    
    private static final String ICON_ROLLOVER_LINE = "rolloverLine"; //NOI18N
    private static final String ICON_ROLLOVER_SELECTED_LINE = "rolloverSelectedLine"; //NOI18N
    
    private PopupMenuListener menuListener;
    
    /** Creates a new instance of MenuToggleButton */
    public DropDownButton( Icon icon, JPopupMenu popup ) {
        assert null != icon;
        
        putClientProperty( DropDownButtonFactory.PROP_DROP_DOWN_MENU, popup );
        
        setIcon( icon );
        
        resetIcons();
        
        addPropertyChangeListener(  DropDownButtonFactory.PROP_DROP_DOWN_MENU,new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent e ) {
                resetIcons();
            }
        });
        
        addMouseMotionListener( new MouseMotionAdapter() {
            public void mouseMoved( MouseEvent e ) {
                if( null != getPopupMenu() ) {
                    mouseInArrowArea = isInArrowArea( e.getPoint() );
                    updateRollover( _getRolloverIcon(), _getRolloverSelectedIcon() );
                }
            }
        });
        
        addMouseListener( new MouseAdapter() {
            public void mousePressed( MouseEvent e ) {
                if( isInArrowArea( e.getPoint() ) && null != getPopupMenu() ) {
                    JPopupMenu menu = getPopupMenu();
                    if( getModel() instanceof Model ) {
                        ((Model)getModel())._press();
                        menu.addPopupMenuListener( getMenuListener() );
                    }
                    menu.show( DropDownButton.this, 0, getHeight()  );
                }
            }

            public void mouseEntered( MouseEvent e ) {
                if( hasPopupMenu() ) {
                    mouseInArrowArea = isInArrowArea( e.getPoint() );
                    updateRollover( _getRolloverIcon(), _getRolloverSelectedIcon() );
                }
            }

            public void mouseExited( MouseEvent e ) {
                mouseInArrowArea = false;
                if( hasPopupMenu() ) {
                    updateRollover( _getRolloverIcon(), _getRolloverSelectedIcon() );
                }
            }
        });
        
        setModel( new Model() );
    }
    
    private PopupMenuListener getMenuListener() {
        if( null == menuListener ) {
            menuListener = new PopupMenuListener() {
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                }

                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    if( getModel() instanceof Model ) {
                        ((Model)getModel())._release();
                    }
                    JPopupMenu menu = getPopupMenu();
                    if( null != menu )
                        menu.removePopupMenuListener( this );
                }

                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            };
        }
        return menuListener;
    }
        
    private void updateRollover( Icon rollover, Icon rolloverSelected ) {
        super.setRolloverIcon( rollover );
        super.setRolloverSelectedIcon( rolloverSelected );
    }
    
    private void resetIcons() {
        Icon icon = regIcons.get( ICON_NORMAL );
        if( null != icon )
            setIcon( icon );
        
        icon = regIcons.get( ICON_PRESSED );
        if( null != icon )
            setPressedIcon( icon );
        
        icon = regIcons.get( ICON_ROLLOVER );
        if( null != icon )
            setRolloverIcon( icon );
        
        icon = regIcons.get( ICON_ROLLOVER_SELECTED );
        if( null != icon )
            setRolloverSelectedIcon( icon );
        
        icon = regIcons.get( ICON_SELECTED );
        if( null != icon )
            setSelectedIcon( icon );
        
        icon = regIcons.get( ICON_DISABLED );
        if( null != icon )
            setDisabledIcon( icon );
        
        icon = regIcons.get( ICON_DISABLED_SELECTED );
        if( null != icon )
            setDisabledSelectedIcon( icon );
    }
    
    private Icon _getRolloverIcon() {
        IconWithArrow icon = null;
        icon = arrowIcons.get( mouseInArrowArea ? ICON_ROLLOVER : ICON_ROLLOVER_LINE );
        if( null == icon ) {
            Icon orig = regIcons.get( ICON_ROLLOVER );
            if( null == orig )
                orig = regIcons.get( ICON_NORMAL );
            icon = new IconWithArrow( orig, !mouseInArrowArea );
            arrowIcons.put( mouseInArrowArea ? ICON_ROLLOVER : ICON_ROLLOVER_LINE, icon );
        }
        return icon;
    }
    
    private Icon _getRolloverSelectedIcon() {
        IconWithArrow icon = null;
        icon = arrowIcons.get( mouseInArrowArea ? ICON_ROLLOVER_SELECTED : ICON_ROLLOVER_SELECTED_LINE );
        if( null == icon ) {
            Icon orig = regIcons.get( ICON_ROLLOVER_SELECTED );
            if( null == orig )
                orig = regIcons.get( ICON_ROLLOVER );
            if( null == orig )
                orig = regIcons.get( ICON_NORMAL );
            icon = new IconWithArrow( orig, !mouseInArrowArea );
            arrowIcons.put( mouseInArrowArea ? ICON_ROLLOVER_SELECTED : ICON_ROLLOVER_SELECTED_LINE, icon );
        }
        return icon;
    }
    
    JPopupMenu getPopupMenu() {
        Object menu = getClientProperty( DropDownButtonFactory.PROP_DROP_DOWN_MENU );
        if( menu instanceof JPopupMenu ) {
            return (JPopupMenu)menu;
        }
        return null;
    }
    
    boolean hasPopupMenu() {
        return null != getPopupMenu();
    }
    
    private boolean isInArrowArea( Point p ) {
        return p.getLocation().x >= getWidth() - IconWithArrow.getArrowAreaWidth() - getInsets().right;
    }

    @Override
    public void setIcon(Icon icon) {
        assert null != icon;
        regIcons.put( ICON_NORMAL, icon );
        IconWithArrow arrow = new IconWithArrow( icon, false );
        arrowIcons.put( ICON_NORMAL, arrow );
        arrowIcons.remove( ICON_ROLLOVER_LINE );
        arrowIcons.remove( ICON_ROLLOVER_SELECTED_LINE );
        arrowIcons.remove( ICON_ROLLOVER );
        arrowIcons.remove( ICON_ROLLOVER_SELECTED );
        super.setIcon( hasPopupMenu() ? arrow : icon );
    }

    private IconWithArrow updateIcons( Icon orig, String iconType ) {
        IconWithArrow arrow = null;
        if( null == orig ) {
            regIcons.remove( iconType );
            arrowIcons.remove( iconType );
        } else {
            regIcons.put( iconType, orig );
            arrow = new IconWithArrow( orig, false );
            arrowIcons.put( iconType, arrow );
        }
        return arrow;
    }
    
    @Override
    public void setPressedIcon(Icon icon) {
        IconWithArrow arrow = updateIcons( icon, ICON_PRESSED );
        super.setPressedIcon( hasPopupMenu() ? arrow : icon );
    }

    @Override
    public void setSelectedIcon(Icon icon) {
        IconWithArrow arrow = updateIcons( icon, ICON_SELECTED );
        super.setSelectedIcon( hasPopupMenu() ? arrow : icon );
    }

    @Override
    public void setRolloverIcon(Icon icon) {
        IconWithArrow arrow = updateIcons( icon, ICON_ROLLOVER );
        arrowIcons.remove( ICON_ROLLOVER_LINE );
        arrowIcons.remove( ICON_ROLLOVER_SELECTED_LINE );
        super.setRolloverIcon( hasPopupMenu() ? arrow : icon );
    }

    @Override
    public void setRolloverSelectedIcon(Icon icon) {
        IconWithArrow arrow = updateIcons( icon, ICON_ROLLOVER_SELECTED );
        arrowIcons.remove( ICON_ROLLOVER_SELECTED_LINE );
        super.setRolloverSelectedIcon( hasPopupMenu() ? arrow : icon );
    }

    @Override
    public void setDisabledIcon(Icon icon) {
        //TODO use 'disabled' arrow icon
        IconWithArrow arrow = updateIcons( icon, ICON_DISABLED );
        super.setDisabledIcon( hasPopupMenu() ? arrow : icon );
    }

    @Override
    public void setDisabledSelectedIcon(Icon icon) {
        //TODO use 'disabled' arrow icon
        IconWithArrow arrow = updateIcons( icon, ICON_DISABLED_SELECTED );
        super.setDisabledSelectedIcon( hasPopupMenu() ? arrow : icon );
    }
    
    
    private class Model extends DefaultButtonModel {
        private boolean _pressed = false;
        
        public void setPressed(boolean b) {
            if( mouseInArrowArea || _pressed )
                return;
            super.setPressed( b );
        }
    
        public void _press() {
            if((isPressed()) || !isEnabled()) {
                return;
            }

            stateMask |= PRESSED + ARMED;

            fireStateChanged();
            _pressed = true;
        }
        
        public void _release() {
            _pressed = false;
            mouseInArrowArea = false;
            setArmed( false );
            setPressed( false );
            setRollover( false );
            setSelected( false );
        }

        @Override
        protected void fireStateChanged() {
            if( _pressed )
                return;
            super.fireStateChanged();
        }

        @Override
        public void setArmed(boolean b) {
            if( _pressed )
                return;
            super.setArmed(b);
        }

        @Override
        public void setEnabled(boolean b) {
            if( _pressed )
                return;
            super.setEnabled(b);
        }

        @Override
        public void setSelected(boolean b) {
            if( _pressed )
                return;
            super.setSelected(b);
        }

        @Override
        public void setRollover(boolean b) {
            if( _pressed )
                return;
            super.setRollover(b);
        }
    }
}