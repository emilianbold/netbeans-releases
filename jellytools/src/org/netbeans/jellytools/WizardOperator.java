/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools;

import java.lang.reflect.Field;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;

/**
 * Enable to manipulate NetBeans wizards. You can access list of steps on the
 * left side, all buttons at the bottom (Back, Next, Finish, Cancel, Help).
 * Each step of a particular wizard is represented by an ancestor of 
 * WizardOperator, i.e. all components are described there.
 */
public class WizardOperator extends NbDialogOperator {
    
    private JButtonOperator _btNext;
    private JButtonOperator _btBack;
    private JButtonOperator _btFinish;
    private JListOperator _lstSteps;
    
    
    /** Creates a new instance of WizardOperator.
     * It waits for a dialog with given title.
     * @param title  title of a wizard window
     */
    public WizardOperator(String title) {
        super(title);
    }
    
    /** Returns operator of "Next >" button.
     * @return  JButtonOperator instance of "Next >" button
     */
    public JButtonOperator btNext() {
        if (_btNext == null) {
            String nextCaption = Bundle.getString("org.openide.Bundle", "CTL_NEXT");
            _btNext = new JButtonOperator(this, nextCaption);
        }
        return _btNext;
    }
    
    /** Returns operator of "< Back" button.
     * @return  JButtonOperator instance of "< Back" button
     */
    public JButtonOperator btBack() {
        if (_btBack == null) {
            String backCaption = Bundle.getString("org.openide.Bundle", "CTL_PREVIOUS");
            _btBack = new JButtonOperator(this, backCaption);
        }
        return _btBack;
    }
    
    /** Returns operator of "Finish" button.
     * @return  JButtonOperator instance of "Finish" button
     */
    public JButtonOperator btFinish() {
        if (_btFinish == null) {
            String finishCaption = Bundle.getString("org.openide.Bundle", "CTL_FINISH");
            _btFinish = new JButtonOperator(this, finishCaption);
        }
        return _btFinish;
    }
    
    /** Returns operator of the list of steps on the left side in wizard dialog.
     * @return  JListOperator instance of list of steps
     */
    public JListOperator lstSteps() {
        if (_lstSteps == null) {
            _lstSteps = new JListOperator(this);
        }
        return _lstSteps;
    }
    
    /** Pushes "Next >" button. */
    public void next() {
        btNext().push();
    }
    
    /** Pushes "< Back" button. */
    public void back() {
        btBack().push();
    }
    
    /** Pushes "Finish" button. */
    public void finish() {
        btFinish().push();
    }
    
    /** Returns index of currently selected step which is bold faced.
     * @return  index of currently selected step (starts at 0)
     */
    public int stepsGetSelectedIndex() {
        int selectedIndex = -1;
        try {
            Field field = lstSteps().getCellRenderer().getClass().getDeclaredField("selected");
            field.setAccessible(true);
            selectedIndex = field.getInt(lstSteps().getCellRenderer());
        } catch (NoSuchFieldException e1) {
            throw new JemmyException("Field selected not found in CellRenderer.", e1);
        } catch (IllegalAccessException e2) {
            throw new JemmyException("Illegal access to field selected.", e2);
        }
        return selectedIndex;
    }
    
    /** Returns currently selected step which is bold faced.
     * @return  value of currently selected step without leading number
     */
    public String stepsGetSelectedValue() {
        return lstSteps().getModel().getElementAt(stepsGetSelectedIndex()).toString();
    }
    
    /** Checks if given panel name is currently selected/shown in wizard. It
     * compares name for exact match.
     * @param panelName  name of panel
     */
    protected void checkPanel(String panelName) {
        if(!stepsGetSelectedValue().equals(panelName)) {
            throw new JemmyException("Wrong panel! Found \""+stepsGetSelectedValue()+"\" instead of \""+panelName+"\".");
        }
    }
    
}
