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

package org.netbeans.modules.xml.xam.ui.customizer;

import javax.swing.JComponent;
import javax.swing.JPanel;
import org.openide.util.Lookup;

/**
 *
 * @author Administrator
 */
public abstract class AbstractCustomizer extends JPanel implements Customizer {

    /**
     * can save? flag
     */
    private boolean saveFlag;

    /**
     * can reset? flag
     */
    private boolean resetFlag;

    /**
     * can save?
     */
    public boolean canApply() {
        return saveFlag;
    }

    /**
     * Fires events when save status is changed.
     * Subclasses must call this when save status is changed
     * due to changes on the customizer form.
     */
    protected  void setSaveEnabled(boolean flag) {
        firePropertyChange(PROP_ACTION_APPLY,saveFlag,flag);
        saveFlag = flag;
    }

    /**
     * Fires events when reset status is changed.
     * Subclasses must call this when reset status is changed
     * due to changes on the customizer form.
     */
    protected  void setResetEnabled(boolean flag) {
        firePropertyChange(PROP_ACTION_RESET,resetFlag,flag);
        resetFlag = flag;
    }

    /**
     * This is the customizer so return this object.
     */
    public JComponent getComponent() {
        return this;
    }

    /**
     * This api returns the Lookup
     */
    public Lookup getLookup(){
        return Lookup.EMPTY;
    }

}
