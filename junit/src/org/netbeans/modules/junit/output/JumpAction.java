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
