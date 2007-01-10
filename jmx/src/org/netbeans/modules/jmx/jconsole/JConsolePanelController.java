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

package org.netbeans.modules.jmx.jconsole;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * @author jfdenise
 */
public final class JConsolePanelController extends OptionsPanelController {

    @Override
    public void update() {
        getJConsoleCustomizer().update();
    }

    @Override
    public void applyChanges() {
        getJConsoleCustomizer().applyChanges();
    }
    
    @Override
    public void cancel () {
        getJConsoleCustomizer().cancel();
    }
    
    @Override
    public boolean isValid () {
        return getJConsoleCustomizer().dataValid();
    }
    
    @Override
    public boolean isChanged () {
        return getJConsoleCustomizer().isChanged();
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    @Override
    public JComponent getComponent(Lookup lookup) {
        return getJConsoleCustomizer();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        getJConsoleCustomizer().addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        getJConsoleCustomizer().removePropertyChangeListener(l);
    }

    private JConsoleCustomizer jconsoleCustomizer;
    
    private JConsoleCustomizer getJConsoleCustomizer () {
        if (jconsoleCustomizer == null)
            jconsoleCustomizer = new JConsoleCustomizer ();
        return jconsoleCustomizer;
    }
}
