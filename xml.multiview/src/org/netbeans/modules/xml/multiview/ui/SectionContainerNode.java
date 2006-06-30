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

package org.netbeans.modules.xml.multiview.ui;
import org.openide.nodes.NodeAdapter;

/** Node for section container
 *
 * @author mkuchtiak
 */
public class SectionContainerNode extends org.openide.nodes.AbstractNode {

    /** Creates a new instance of SectionContainerNode */
    public SectionContainerNode(org.openide.nodes.Children ch) {
        super(ch);
        int childrenSize = ch.getNodes().length;
        setIconBase("org/netbeans/modules/xml/multiview/resources/folder"); //NOI18N
        addNodeListener(new NodeAdapter() {
            public void childrenAdded(org.openide.nodes.NodeMemberEvent ev) {
                if (SectionContainerNode.this.getChildren().getNodes().length==1) {
                    firePropertyChange(org.openide.nodes.Node.PROP_LEAF,Boolean.TRUE, Boolean.FALSE);
                }
            }
            public void childrenRemoved(org.openide.nodes.NodeMemberEvent ev) {
                if (SectionContainerNode.this.getChildren().getNodes().length==0) {
                    firePropertyChange(org.openide.nodes.Node.PROP_LEAF,Boolean.FALSE, Boolean.TRUE);
                }
            }
        });
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(getName());
    }
}
