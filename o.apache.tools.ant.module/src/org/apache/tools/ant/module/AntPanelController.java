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

package org.apache.tools.ant.module;

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
public final class AntPanelController extends OptionsPanelController {

    @Override
    public void update () {
        getAntCustomizer ().update ();
    }

    @Override
    public void applyChanges () {
        getAntCustomizer ().applyChanges ();
    }
    
    @Override
    public void cancel () {
        getAntCustomizer ().cancel ();
    }
    
    @Override
    public boolean isValid () {
        return getAntCustomizer ().dataValid ();
    }
    
    @Override
    public boolean isChanged () {
        return getAntCustomizer ().isChanged ();
    }
    
    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("netbeans.optionsDialog.advanced.ant");
    }
    
    @Override
    public JComponent getComponent (Lookup lookup) {
        return getAntCustomizer ();
    }

    @Override
    public void addPropertyChangeListener (PropertyChangeListener l) {
        getAntCustomizer ().addPropertyChangeListener (l);
    }

    @Override
    public void removePropertyChangeListener (PropertyChangeListener l) {
        getAntCustomizer ().removePropertyChangeListener (l);
    }

    
    private AntCustomizer antCustomizer;
    
    private AntCustomizer getAntCustomizer () {
        if (antCustomizer == null)
            antCustomizer = new AntCustomizer ();
        return antCustomizer;
    }
}
