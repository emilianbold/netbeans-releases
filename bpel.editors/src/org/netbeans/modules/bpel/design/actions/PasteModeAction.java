/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.bpel.design.actions;

import org.netbeans.modules.bpel.design.DesignView;

public abstract class PasteModeAction extends DesignViewAction {

    private static final long serialVersionUID = 1L;

    public PasteModeAction(DesignView view) {
        super(view);
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && 
               getDesignView().getCopyPasteHandler().isActive();
    }
}
