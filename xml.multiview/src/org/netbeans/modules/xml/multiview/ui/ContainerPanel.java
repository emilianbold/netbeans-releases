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

/*
 * ContainerPanel.java
 *
 * Created on November 26, 2004, 6:35 PM
 */

package org.netbeans.modules.xml.multiview.ui;

/** ContainerPanel.java
 *  Interface for panel containing sections.
 *
 * Created on November 26, 2004, 6:35 PM
 * @author mkuchtiak
 */
public interface ContainerPanel {
    /** Gets section for specific explorer node
     * @param Node key explorer node
     * @return NodeSectionPanel JPanel corresponding to given node
     */  
    public NodeSectionPanel getSection(org.openide.nodes.Node key);
    
    /** Adds new section
     * @param section component(JPanel) to be added to container
     */ 
    public void addSection(NodeSectionPanel section);
    
    /** Removes section
     * @param section component(JPanel) to be removed from container
     */ 
    public void removeSection(NodeSectionPanel section);
}
