/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.modules.mobility.svgcore.composer.actions;

import java.awt.event.ActionEvent;
import org.netbeans.modules.mobility.svgcore.composer.AbstractComposerActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.view.svg.AbstractSVGAction;

/**
 *
 * @author Pavel Benes
 */
public class MoveBackwardActionFactory extends AbstractComposerActionFactory {
    private final AbstractSVGAction  m_moveBackwardAction = 
        new AbstractSVGAction("animate_start.png", "HINT_MoveBackward", "LBL_MoveBackward") {
            public void actionPerformed(ActionEvent e) {
                SVGObject [] selected = m_sceneMgr.getSelectedObjects();
                if (selected != null) {
                    assert selected.length > 0;
                    assert selected[0] != null;
                    selected[0].moveBackward();
                } else {
                    System.err.println("No selection, button should be disabled");
                }
            }
    };            
    
    public MoveBackwardActionFactory(SceneManager sceneMgr) {
        super(sceneMgr);
    }

    public AbstractSVGAction getMenuAction() {
        return m_moveBackwardAction;
    }
}
