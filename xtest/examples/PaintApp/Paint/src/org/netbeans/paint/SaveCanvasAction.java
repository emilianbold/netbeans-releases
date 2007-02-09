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

package org.netbeans.paint;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;

public final class SaveCanvasAction extends CallableSystemAction implements PropertyChangeListener{

    public SaveCanvasAction() {
        TopComponent.getRegistry().addPropertyChangeListener(
                WeakListeners.propertyChange(this,
                TopComponent.getRegistry()));
        
        updateEnablement();
    }
    
    public void performAction() {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if (tc instanceof PaintTopComponent) {
            try {
                ((PaintTopComponent) tc).saveAs();
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        } else {
            //Theoretically the active component could have changed
            //between the time the menu item or toolbar button was
            //pressed and when the action was invoked.  Not likely,
            //but theoretically possible
            Toolkit.getDefaultToolkit().beep();
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(SaveCanvasAction.class, "CTL_SaveCanvasAction");
    }
    
    protected String iconResource() {
        return "org/netbeans/paint/save.PNG";
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())){
            updateEnablement();
        }
    }
    
    private void updateEnablement() {
        setEnabled(TopComponent.getRegistry().getActivated() instanceof PaintTopComponent);
    }
    
}
