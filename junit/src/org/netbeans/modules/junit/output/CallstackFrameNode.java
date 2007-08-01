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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 *
 * @author Marian Petras
 */
final class CallstackFrameNode extends AbstractNode {
    
    /** */
    private final String frameInfo;
    
    /** Creates a new instance of CallstackFrameNode */
    public CallstackFrameNode(final String frameInfo) {
        this(frameInfo, null);
    }
    
    /**
     * Creates a new instance of CallstackFrameNode
     *
     * @param  frameInfo  line of a callstack, e.g. <code>foo.bar.Baz:314</code>
     * @param  displayName  display name for the node, or <code>null</code>
     *                      to use the default display name for the given
     *                      callstack frame info
     */
    public CallstackFrameNode(final String frameInfo,
                              final String displayName) {
        super(Children.LEAF);
        setDisplayName(displayName != null
                       ? displayName
                       : "at " + frameInfo);                            //NOI18N
        setIconBaseWithExtension(
                "org/netbeans/modules/junit/output/res/empty.gif");     //NOI18N

        this.frameInfo = frameInfo;
    }
    
    /**
     */
    @Override
    public Action getPreferredAction() {
        return new JumpAction(this, frameInfo);
    }
    
}
