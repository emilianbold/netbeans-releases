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

/** Node for section container
 *
 * @author mkuchtiak
 */
public class SectionContainerNode extends org.openide.nodes.AbstractNode {
    
    /** Creates a new instance of SectionContainerNode */
    public SectionContainerNode(org.openide.nodes.Children ch) {
        super(ch);
        setIconBase("org/netbeans/modules/xml/multiview/resources/folder");
    }
    
}
