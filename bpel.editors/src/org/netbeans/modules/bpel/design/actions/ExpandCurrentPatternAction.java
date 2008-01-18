/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.bpel.design.actions;

import java.awt.event.ActionEvent;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.model.patterns.CollapsedPattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

/**
 *
 * @author Alexey
 */
public class ExpandCurrentPatternAction extends DesignModeAction {

    private static final long serialVersionUID = 1L;

    public ExpandCurrentPatternAction(DesignView view) {
        super(view);
    }

    public void actionPerformed(ActionEvent event) {
//            System.out.println("ExpandCurrentPatternAction");
        Pattern pattern = getDesignView().getSelectionModel().getSelectedPattern();
        if (pattern == null) {
            return;
        }
        if (!(pattern instanceof CollapsedPattern)) {
            return;
        }

        getDesignView().getCollapseExpandDecorationProvider().createCollapseExpandAction(pattern).actionPerformed(event);
    }
}
