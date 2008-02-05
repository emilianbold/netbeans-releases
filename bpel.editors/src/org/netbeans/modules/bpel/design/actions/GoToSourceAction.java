/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.bpel.design.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.nodes.actions.GoToAction;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Alexey
 */
public class GoToSourceAction extends DesignModeAction {
        private static final long serialVersionUID = 1L;
        public GoToSourceAction(DesignView view){
            super(view);
        }
        public void actionPerformed(ActionEvent e) {
            Pattern selected = getDesignView().getSelectionModel().getSelectedPattern();

            if (selected == null){
                return;
            }


            Node node = getDesignView().getNodeForPattern(selected);

            if (node == null){
                return;
            }

            Action[] actions = node.getActions(true);
            if (actions == null){
                return;
            }

            for (int i = actions.length - 1; i >= 0; i--) {
                Action action = actions[i];
                if (action instanceof GoToAction) {
                    org.netbeans.modules.bpel.nodes.actions.GoToSourceAction gotoAction
                            = SystemAction.get(org.netbeans.modules.bpel.nodes.actions.GoToSourceAction.class);
                    Node[] nodes = new Node[] {node};
                    if (gotoAction != null && gotoAction.enable(nodes)) {
                        gotoAction.performAction(nodes);
                    }
                    break;
                }
            }
        }
    }
