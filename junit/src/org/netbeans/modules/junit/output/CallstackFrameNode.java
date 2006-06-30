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
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.junit.wizards.Utils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.Line;

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
    public Action getPreferredAction() {
        return new JumpAction(this, frameInfo);
    }
    
}
