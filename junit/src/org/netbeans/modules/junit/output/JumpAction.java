/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.nodes.Node;

/**
 *
 * @author Marian Petras
 */
final class JumpAction extends AbstractAction {
    
    /** */
    private final Node node;
    /** */
    private final String callstackFrameInfo;
    
    /** Creates a new instance of JumpAction */
    public JumpAction(Node node, String callstackFrameInfo) {
        this.node = node;
        this.callstackFrameInfo = callstackFrameInfo;
    }
    
    /**
     * If the <code>callstackFrameInfo</code> is not <code>null</code>,
     * tries to jump to the callstack frame source code. Otherwise does nothing.
     */
    public void actionPerformed(ActionEvent e) {
        if (callstackFrameInfo == null) {
            return;
        }
        
        OutputUtils.openCallstackFrame(node, callstackFrameInfo);
    }

}
