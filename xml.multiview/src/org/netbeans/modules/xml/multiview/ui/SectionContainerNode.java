/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
        setIconBase(childrenSize==0?"org/netbeans/modules/xml/multiview/resources/folder": //NOI18N
                                    "org/netbeans/modules/xml/multiview/resources/folderOpen"); //NOI18N
        addNodeListener(new NodeAdapter() {
            public void childrenAdded(org.openide.nodes.NodeMemberEvent ev) {
                if (SectionContainerNode.this.getChildren().getNodes().length==1) {
                    setIconBase("org/netbeans/modules/xml/multiview/resources/folderOpen"); //NOI18N
                }
            }
            public void childrenRemoved(org.openide.nodes.NodeMemberEvent ev) {
                if (SectionContainerNode.this.getChildren().getNodes().length==0) {
                    setIconBase("org/netbeans/modules/xml/multiview/resources/folder"); //NOI18N
                }
            }
        });
    }
    
}
