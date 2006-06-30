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

package org.netbeans.modules.options.colors;

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
public final class FontAndColorsPanelController extends OptionsPanelController {


    public void update () {
        getFontAndColorsPanel ().update ();
    }

    public void applyChanges () {
        getFontAndColorsPanel ().applyChanges ();
    }
    
    public void cancel () {
        getFontAndColorsPanel ().cancel ();
    }
    
    public boolean isValid () {
        return getFontAndColorsPanel ().dataValid ();
    }
    
    public boolean isChanged () {
        return getFontAndColorsPanel ().isChanged ();
    }
    
    public JComponent getComponent (Lookup masterLookup) {
        return getFontAndColorsPanel ();
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("netbeans.optionsDialog.fontAndColorsPanel");
    }

    public void addPropertyChangeListener (PropertyChangeListener l) {
        getFontAndColorsPanel ().addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        getFontAndColorsPanel ().removePropertyChangeListener (l);
    }

    private FontAndColorsPanel fontAndColorsPanel;
    
    private FontAndColorsPanel getFontAndColorsPanel () {
        if (fontAndColorsPanel == null)
            fontAndColorsPanel = new FontAndColorsPanel ();
        return fontAndColorsPanel;
    }
}
