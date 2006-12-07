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

package org.netbeans.modules.options.advanced;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class AdvancedPanelController extends OptionsPanelController {


    public void update () {
        getAdvancedPanel ().update ();
    }

    public void applyChanges () {
        getAdvancedPanel ().applyChanges ();
    }
    
    public void cancel () {
        getAdvancedPanel ().cancel ();
    }
    
    public boolean isValid () {
        return getAdvancedPanel ().dataValid ();
    }
    
    public boolean isChanged () {
        return getAdvancedPanel ().isChanged ();
    }
        
    public Lookup getLookup () {
        return getAdvancedPanel ().getLookup ();
    }
    
    public JComponent getComponent (Lookup masterLookup) {
        getAdvancedPanel ().init (masterLookup);
        return getAdvancedPanel ();
    }
    
    public HelpCtx getHelpCtx () {
        return getAdvancedPanel ().getHelpCtx ();
    }
    
    public void addPropertyChangeListener (PropertyChangeListener l) {
        getAdvancedPanel ().addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        getAdvancedPanel ().removePropertyChangeListener (l);
    }

    private AdvancedPanel advancedPanel;
    
    private AdvancedPanel getAdvancedPanel () {
        if (advancedPanel == null)
            advancedPanel = new AdvancedPanel ();
        return advancedPanel;
    }
}
