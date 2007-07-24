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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.TabListPopupAction;
import org.netbeans.swing.tabcontrol.WinsysInfoForTabbed;

/**
 * A factory to create tab control buttons.
 * 
 * @since 1.9
 * @author S. Aubrecht
 */
public class TabControlButtonFactory {
    
    private static IconLoader iconCache;
    
    private TabControlButtonFactory() {
    }
    
    public static Icon getIcon( String iconPath ) {
        if( null == iconCache )
            iconCache = new IconLoader();
        return iconCache.obtainIcon( iconPath );
    }
    
    /**
     * Create default close button.
     */
    public static TabControlButton createCloseButton( TabDisplayer displayer ) {
        return new CloseButton( displayer );
    }
    
    /**
     * Create default auto-hide/pin button. The button changes icons depending
     * on the state of tab component.
     */
    public static TabControlButton createSlidePinButton( TabDisplayer displayer ) {
        return new SlidePinButton( displayer );
    }
    
    /**
     * Create default maximize/restore button. The button changes icons depending
     * on the state of tab component.
     */
    public static TabControlButton createMaximizeRestoreButton( TabDisplayer displayer, boolean showBorder ) {
        return new MaximizeRestoreButton( displayer, showBorder );
    }
    
    public static TabControlButton createScrollLeftButton( TabDisplayer displayer, Action scrollAction, boolean showBorder ) {
        TabControlButton button = new TimerButton( TabControlButton.ID_SCROLL_LEFT_BUTTON, displayer, scrollAction, showBorder );
        button.setToolTipText( java.util.ResourceBundle.getBundle("org/netbeans/swing/tabcontrol/plaf/Bundle").getString("Tip_Scroll_Documents_Left") );
        return button;
    }
    
    public static TabControlButton createScrollRightButton( TabDisplayer displayer, Action scrollAction, boolean showBorder ) {
        TabControlButton button = new TimerButton( TabControlButton.ID_SCROLL_RIGHT_BUTTON, displayer, scrollAction, showBorder );
        button.setToolTipText( java.util.ResourceBundle.getBundle("org/netbeans/swing/tabcontrol/plaf/Bundle").getString("Tip_Scroll_Documents_Right") );
        return button;
    }
    
    public static TabControlButton createDropDownButton( TabDisplayer displayer, boolean showBorder ) {
        return new DropDownButton( displayer, showBorder );
    }
    
    private static class CloseButton extends TabControlButton {
        
        public CloseButton( TabDisplayer displayer ) {
            super( TabControlButton.ID_CLOSE_BUTTON, displayer );
            setToolTipText( java.util.ResourceBundle.getBundle("org/netbeans/swing/tabcontrol/plaf/Bundle").getString("Tip_Close_Window") );
        }
        
        protected String getTabActionCommand( ActionEvent e ) {
//            if( (e.getModifiers() & ActionEvent.SHIFT_MASK) > 0 )
//                return TabDisplayer.COMMAND_CLOSE_ALL;
//            else if( (e.getModifiers() & ActionEvent.ALT_MASK) > 0 )
//                return TabDisplayer.COMMAND_CLOSE_ALL_BUT_THIS;
            return TabDisplayer.COMMAND_CLOSE;
        }
    }
    
    private static class SlidePinButton extends TabControlButton {
        
        public SlidePinButton( TabDisplayer displayer ) {
            super( displayer );
            ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
            toolTipManager.registerComponent( this );
        }
        
        protected String getTabActionCommand( ActionEvent e ) {
            if( getButtonId() == TabControlButton.ID_PIN_BUTTON )
                return TabDisplayer.COMMAND_DISABLE_AUTO_HIDE;
            return TabDisplayer.COMMAND_ENABLE_AUTO_HIDE;
        }

        protected int getButtonId() {
            int retValue = TabControlButton.ID_PIN_BUTTON;
            Component currentTab = getActiveTab( getTabDisplayer() );
            if( null != currentTab ) {
                WinsysInfoForTabbed winsysInfo = getTabDisplayer().getWinsysInfo();
                if( null != winsysInfo ) {
                    Object orientation = winsysInfo.getOrientation( currentTab );
                    if( TabDisplayer.ORIENTATION_EAST.equals( orientation ) ) 
                        retValue = TabControlButton.ID_SLIDE_RIGHT_BUTTON;
                    else if( TabDisplayer.ORIENTATION_WEST.equals( orientation ) ) 
                        retValue = TabControlButton.ID_SLIDE_LEFT_BUTTON;
                    else if( TabDisplayer.ORIENTATION_SOUTH.equals( orientation ) ) 
                        retValue = TabControlButton.ID_SLIDE_DOWN_BUTTON;
                }
            }
            
            return retValue;
        }

        public String getToolTipText() {
            if( getButtonId() == TabControlButton.ID_PIN_BUTTON )
                return java.util.ResourceBundle.getBundle("org/netbeans/swing/tabcontrol/plaf/Bundle").getString("Tip_Pin");
            return java.util.ResourceBundle.getBundle("org/netbeans/swing/tabcontrol/plaf/Bundle").getString("Tip_Minimize_Window");
        }
    }
    
    private static class MaximizeRestoreButton extends TabControlButton {
        
        public MaximizeRestoreButton( TabDisplayer displayer, boolean showBorder ) {
            super( -1, displayer, showBorder );
            ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
            toolTipManager.registerComponent( this );
        }
        
        protected String getTabActionCommand( ActionEvent e ) {
            return TabDisplayer.COMMAND_MAXIMIZE;
        }

        protected int getButtonId() {
            int retValue = TabControlButton.ID_MAXIMIZE_BUTTON;
            Component currentTab = getActiveTab( getTabDisplayer() );
            if( null != currentTab ) {
                WinsysInfoForTabbed winsysInfo = getTabDisplayer().getWinsysInfo();
                if( null != winsysInfo ) {
                    if( winsysInfo.inMaximizedMode( currentTab ) ) {
                        retValue = TabControlButton.ID_RESTORE_BUTTON;
                    }
                }
            }
            
            return retValue;
        }

        public String getToolTipText() {
            if( getButtonId() == TabControlButton.ID_MAXIMIZE_BUTTON )
                return java.util.ResourceBundle.getBundle("org/netbeans/swing/tabcontrol/plaf/Bundle").getString("Tip_Maximize_Window");
            return java.util.ResourceBundle.getBundle("org/netbeans/swing/tabcontrol/plaf/Bundle").getString("Tip_Restore_Window");
        }
    }

    private static Component getActiveTab( TabDisplayer displayer ) {
        Component res = null;
        int selIndex = displayer.getSelectionModel().getSelectedIndex();
        if( selIndex >= 0 ) {
            TabData tab = displayer.getModel().getTab( selIndex );
            res = tab.getComponent();
        }
        return res;
    }


    /** 
     * A convenience button class which will continue re-firing its action
     * on a timer for as long as the button is depressed.  Used for left-right scroll
     * buttons.
     */
    private static class TimerButton extends TabControlButton implements ActionListener {
        Timer timer = null;

        public TimerButton( int buttonId, TabDisplayer displayer, Action a, boolean showBorder ) {
            super( buttonId, displayer, showBorder );
            setAction( a );
        }

        private Timer getTimer() {
            if (timer == null) {
                timer = new Timer(400, this);
                timer.setRepeats(true);
            }
            return timer;
        }

        int count = 0;

        public void actionPerformed( ActionEvent e ) {
            count++;
            if (count > 2) {
                if (count > 5) {
                    timer.setDelay(75);
                } else {
                    timer.setDelay(200);
                }
            }
            performAction();
        }

        private void performAction() {
            if (!isEnabled()) {
                stopTimer();
                return;
            }
            getAction().actionPerformed(new ActionEvent(this,
                                                        ActionEvent.ACTION_PERFORMED,
                                                        getActionCommand()));
        }

        private void startTimer() {
            Timer t = getTimer();
            if (t.isRunning()) {
                return;
            }
            repaint();
            t.setDelay(400);
            t.start();
        }

        private void stopTimer() {
            if (timer != null) {
                timer.stop();
            }
            repaint();
            count = 0;
        }

        protected void processMouseEvent(MouseEvent me) {
            if (isEnabled() && me.getID() == me.MOUSE_PRESSED) {
                startTimer();
            } else if (me.getID() == me.MOUSE_RELEASED) {
                stopTimer();
            }
            super.processMouseEvent(me);
        }

        protected void processFocusEvent(FocusEvent fe) {
            super.processFocusEvent(fe);
            if (fe.getID() == fe.FOCUS_LOST) {
                stopTimer();
            }
        }

        protected String getTabActionCommand(ActionEvent e) {
            return null;
        }
    }

    /**
     * A button for editor tab control to show a list of opened documents.
     */
    private static class DropDownButton extends TabControlButton {
        
        private boolean forcePressedIcon = false;
        
        public DropDownButton( TabDisplayer displayer, boolean showBorder ) {
            super( TabControlButton.ID_DROP_DOWN_BUTTON, displayer, showBorder );
            setAction( new TabListPopupAction( displayer ) );
            setToolTipText( java.util.ResourceBundle.getBundle("org/netbeans/swing/tabcontrol/plaf/Bundle").getString("Tip_Show_Opened_Documents_List") );
        }

        protected void processMouseEvent(MouseEvent me) {
            super.processMouseEvent(me);
            if (isEnabled() && me.getID() == me.MOUSE_PRESSED) {
                forcePressedIcon = true;
                repaint();
                getAction().actionPerformed(new ActionEvent(this,
                                                            ActionEvent.ACTION_PERFORMED,
                                                            "pressed"));
            } else if (isEnabled() && me.getID() == me.MOUSE_RELEASED) {
                forcePressedIcon = false;
                repaint();
            }
        }

        protected String getTabActionCommand(ActionEvent e) {
            return null;
        }
        
        void performAction( ActionEvent e ) {
        }

        public Icon getRolloverIcon() {
            if( forcePressedIcon )
                return getPressedIcon();
            
            return super.getRolloverIcon();
        }

        public Icon getIcon() {
            if( forcePressedIcon )
                return getPressedIcon();
            
            return super.getIcon();
        }
    }


    /**
     * Loader for icons. Caches loaded icons using hash map.
     */
    final private static class IconLoader {
        /* mapping <String, Icon> from resource paths to icon objects, used as cache */
        private Map<String,Icon> paths2Icons;

        /**
         * Finds and returns icon instance from cache, if present. Otherwise
         * loads icon using given resource path and stores icon into cache for
         * next access.
         *
         * @return icon image
         */
        public Icon obtainIcon(String iconPath) {
            if (paths2Icons == null) {
                paths2Icons = new HashMap<String,Icon>(6);
            }
            Icon icon = paths2Icons.get(iconPath);
            if (icon == null) {
                // not yet in cache, load and store
                Image image = loadImage(iconPath);
                if (image == null) {
                    throw new IllegalArgumentException("Icon with resource path: "
                                                       + iconPath
                                                       + " can't be loaded, probably wrong path.");
                }
                icon = new ImageIcon(image);
                paths2Icons.put(iconPath, icon);
            }
            return icon;
        }

    } // end of IconLoader

    private static Image loadImage(String path) {
        try {
            URL url = TabControlButtonFactory.class.getResource("/"+path);
            return ImageIO.read(url);
        } catch (Exception e) {
            Logger.getLogger(TabControlButtonFactory.class.getName()).
                    log(Level.WARNING, "Cannot load image", e);
            return null;
        }
    }
}
