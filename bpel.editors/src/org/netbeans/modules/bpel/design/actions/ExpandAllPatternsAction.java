/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.bpel.design.actions;

import java.awt.event.ActionEvent;
import org.netbeans.modules.bpel.design.DesignView;

/**
 *
 * @author Alexey
 */
public class ExpandAllPatternsAction extends DesignModeAction {

    private static final long serialVersionUID = 1L;

    public ExpandAllPatternsAction(DesignView view) {
        super(view);
  /*FIXME
        super(NbBundle.getMessage(DesignView.class, "LBL_ExpandAll"),
                new ImageIcon(DesignView.class.getResource("resources/expand_all.png")));
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(DesignView.class,
                "LBL_ExpandAll_Description"));
    */
    }

    public void actionPerformed(ActionEvent event) {
        getDesignView().getModel().expandAll();
    }
}
