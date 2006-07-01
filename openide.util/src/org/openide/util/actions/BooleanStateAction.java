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
package org.openide.util.actions;


/** An action that can be toggled on or off.
* The actual "performing" of the action is the toggle itself, so
* this action should be used by listening to the {@link #PROP_BOOLEAN_STATE} property.
* <p>The default value of the state is <code>true</code> (on).
*
*
* @author   Ian Formanek, Petr Hamernik
*/
public abstract class BooleanStateAction extends SystemAction implements Presenter.Menu, Presenter.Popup,
    Presenter.Toolbar {
    /** serialVersionUID */
    static final long serialVersionUID = 6394800019181426199L;

    /** Name of property hold the state of the action. */
    public static final String PROP_BOOLEAN_STATE = "booleanState"; // NOI18N

    /* Returns a JMenuItem that presents the Action, that implements this
    * interface, in a MenuBar.
    * @return the JMenuItem representation for the Action
    */
    public javax.swing.JMenuItem getMenuPresenter() {
        return org.netbeans.modules.openide.util.AWTBridge.getDefault().createMenuPresenter(this);
    }

    /* Returns a JMenuItem that presents the Action, that implements this
    * interface, in a Popup Menu.
    * The default implmentation returns the same JMenuItem as the getMenuPresenter.
    * @return the JMenuItem representation for the Action
    */
    public javax.swing.JMenuItem getPopupPresenter() {
        return org.netbeans.modules.openide.util.AWTBridge.getDefault().createPopupPresenter(this);
    }

    /* Returns a Component that presents the Action, that implements this
    * interface, in a ToolBar.
    * @return the Component representation for the Action
    */
    public java.awt.Component getToolbarPresenter() {
        return org.netbeans.modules.openide.util.AWTBridge.getDefault().createToolbarPresenter(this);
    }

    /** Get the current state.
    * @return <code>true</code> if on
    */
    public boolean getBooleanState() {
        return getProperty(PROP_BOOLEAN_STATE).equals(Boolean.TRUE);
    }

    /** Set the current state.
    * Fires a change event, which should be used to affect other components when
    * its state is toggled.
    * @param value <code>true</code> to turn on, <code>false</code> to turn off
    */
    public void setBooleanState(boolean value) {
        Boolean newValue = value ? Boolean.TRUE : Boolean.FALSE;
        Boolean oldValue = (Boolean) putProperty(PROP_BOOLEAN_STATE, newValue);

        firePropertyChange(PROP_BOOLEAN_STATE, oldValue, newValue);
    }

    /* Initializes its own properties (and let superclass initialize
    * too).
    */
    protected void initialize() {
        putProperty(PROP_BOOLEAN_STATE, Boolean.TRUE);
        super.initialize();
    }

    /* Implementation of method of javax.swing.Action interface.
    * Changes the boolean state.
    *
    * @param ev ignored
    */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        setBooleanState(!getBooleanState());
    }
}
