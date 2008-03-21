/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.bpel.design.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.model.patterns.CollapsedPattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

/**
 *
 * @author Alexey
 */
public class CollapseCurrentPatternAction extends DesignModeAction {

    private static final long serialVersionUID = 1L;

    public CollapseCurrentPatternAction(DesignView view) {
        super(view);
    }

    public void actionPerformed(ActionEvent event) {
        //            System.out.println("CollapseCurrentPatternAction");
        Pattern pattern = getDesignView().getSelectionModel().getSelectedPattern();
        if (pattern == null) {
            return;
        }
        if (getDesignView().getModel().isCollapsed(pattern.getOMReference())) {
            return;
        }

        Action action = getDesignView().getCollapseExpandDecorationProvider().createCollapseExpandAction(pattern);

        if (action != null) {
            action.actionPerformed(event);
        }
    }
}
