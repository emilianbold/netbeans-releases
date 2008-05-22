/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.bpel.design.actions;

import javax.swing.AbstractAction;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.model.DiagramModel;

/**
 *
 * @author Alexey
 */
public abstract class DesignViewAction extends AbstractAction {

    private DesignView designView;

    /**
         * Defines an <code>Action</code> object with a default
         * description string and default icon.
         */
    public DesignViewAction(DesignView designView) {
        super();
        this.designView = designView;
    }

    protected DesignView getDesignView(){
        return this.designView;
    }
    public boolean isEnabled() {
        DesignView view = getDesignView();
        DiagramModel model = view == null ? null : view.getModel();
        return super.isEnabled() && 
               view != null && 
               model != null;
        
    }
    /**
         * Defines an <code>Action</code> object with the specified
         * description string and a default icon.
  /*       
    public DesignModeAction(String name) {
        super(name);
    }
*/
    /**
         * Defines an <code>Action</code> object with the specified
         * description string and a the specified icon.
         */
    /*
    public DesignModeAction(String name, Icon icon) {
        super(name, icon);
    }
     */
}
