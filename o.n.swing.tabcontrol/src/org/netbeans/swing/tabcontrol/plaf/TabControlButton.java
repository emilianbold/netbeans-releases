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

import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;

/**
 * <p>A base class for control buttons placed within the tabs (view tabs) or 
 * next to the tab row (editor tabs). By default the button posts a TabActionEvent
 * to the TabDisplayerUI when pressed.</p>
 * <p>The button is painted using a set of icons only unless 'showBorder' is true.
 * The icons should include 'fake' button border then.</p>
 * 
 * @since 1.9
 * @author S. Aubrecht
 */
public abstract class TabControlButton extends JButton {
    
    public static final int ID_CLOSE_BUTTON = 1;
    public static final int ID_PIN_BUTTON = 2;
    public static final int ID_MAXIMIZE_BUTTON = 3;
    public static final int ID_RESTORE_BUTTON = 4;
    public static final int ID_SLIDE_LEFT_BUTTON = 5;
    public static final int ID_SLIDE_RIGHT_BUTTON = 6;
    public static final int ID_SLIDE_DOWN_BUTTON = 7;
    public static final int ID_DROP_DOWN_BUTTON = 8;
    public static final int ID_SCROLL_LEFT_BUTTON = 9;
    public static final int ID_SCROLL_RIGHT_BUTTON = 10;
    
    public static final int STATE_DEFAULT = 0;
    public static final int STATE_PRESSED = 1;
    public static final int STATE_DISABLED = 2;
    public static final int STATE_ROLLOVER = 3;
    
    private int buttonId;
    private TabDisplayer displayer;
    private boolean showBorder;
    private boolean superConstructorsCompleted = false;
            
    /**
     * @param displayer Tab displayer where this button is displayed.
     */
    TabControlButton( TabDisplayer displayer ) {
        this( -1, displayer, false );
    }
    
    /**
     * @param buttonId Button type (close button, slide button etc)
     * @param displayer Tab displayer where this button is displayed.
     */
    TabControlButton( int buttonId, TabDisplayer displayer ) {
        this( buttonId, displayer, false);
    }
    
    /**
     * @param buttonId Button type (close button, slide button etc)
     * @param displayer Tab displayer where this button is displayed.
     * @param showBorder if false then only icon will be make button overall look,
     *          true means regular button border
     */
    TabControlButton( int buttonId, TabDisplayer displayer, boolean showBorder ) {
        super();
        this.superConstructorsCompleted = true;
        this.buttonId = buttonId;
        this.displayer = displayer;
        this.showBorder = showBorder;
        configureButton();
    }
    
    /**
     * @param e 
     * @return Tab Action id that is posted to the TabDisplayerUI for processing
     * when the button is pressed.
     */
    protected abstract String getTabActionCommand( ActionEvent e );
    
    /**
     * @return Button type identification that is used by the TabDisplayerUI to select the correct
     * icons for this button.
     */
    protected int getButtonId() {
        return buttonId;
    }

    public Icon getIcon() {
        if( null != displayer )
            return displayer.getUI().getButtonIcon( getButtonId(), STATE_DEFAULT );
        return null;
    }

    public Icon getPressedIcon() {
        if( null != displayer )
            return displayer.getUI().getButtonIcon( getButtonId(), STATE_PRESSED );
        return null;
    }

    public Icon getRolloverIcon() {
        if( null != displayer )
            return displayer.getUI().getButtonIcon( getButtonId(), STATE_ROLLOVER );
        return null;
    }

    public Icon getRolloverSelectedIcon() {
        return getRolloverIcon();
    }

    public Icon getDisabledIcon() {
        if( null != displayer )
            return displayer.getUI().getButtonIcon( getButtonId(), STATE_DISABLED );
        return null;
    }

    public Icon getDisabledSelectedIcon() {
        return getDisabledIcon();
    }

    public void updateUI() {
        super.updateUI();
        // don't call configureButton() from super constructor
        if (superConstructorsCompleted) {
            configureButton();
        }
    }
    
    /**
     * Make sure that only button icon gets painted (turn off borders etc)
     */
    protected void configureButton() {
        setFocusable( false );
        setRolloverEnabled( getRolloverIcon() != null );
        if (showBorder) {
            setContentAreaFilled( true );
            setBorderPainted( true );
        } else {
            setContentAreaFilled( false );
            setBorderPainted( false );
            setBorder( BorderFactory.createEmptyBorder() );
        }
    }

    protected void fireActionPerformed(ActionEvent event) {
        super.fireActionPerformed(event);
        performAction( event );
        //clear the rollover flag because some operations (maximize/restore) do not send mouseExited event
        getModel().setRollover( false );
    }
    
    /**
     * Post an event to the TabDisplayerUI that this button was pressed.
     */
    void performAction( ActionEvent e ) {
        displayer.getUI().postTabAction( createTabActionEvent( e ) );
    }
    
    /**
     * @return Tab action event that is posted to the TabDisplayerUI when this button is pressed.
     */
    protected TabActionEvent createTabActionEvent( ActionEvent e ) {
        return new TabActionEvent( this, getTabActionCommand( e ), displayer.getSelectionModel().getSelectedIndex() );
    }
    
    protected TabDisplayer getTabDisplayer() {
        return displayer;
    }
}
