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

import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.JComponent;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author Ajit Bhate
 */
public interface Customizer extends Lookup.Provider, HelpCtx.Provider {
    public static final String PROP_ACTION_APPLY = "apply";
    public static final String PROP_ACTION_RESET = "reset";

    /**
     * This api indicates if customizer is editable
     */
    public boolean isEditable();

    /**
     * This api checks if the changes done in the customizer can be applied.
     */
    public boolean canApply();

    /**
     * This api applys the changes done in the customizer
     */
    public void apply() throws IOException;

    /**
     * This api discards any changes done in customizer
     * and resets it to its initial value.
     */
    public void reset();

    /**
     * This api returns the customizer ui component
     */
    public JComponent getComponent();

    /**
     * This api adds a property change listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * This api adds a property change listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);
}
