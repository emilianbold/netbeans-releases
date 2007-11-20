/*
 * Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.netbeans.paint;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.openide.util.Exceptions;
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
                Exceptions.printStackTrace(ioe);
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
