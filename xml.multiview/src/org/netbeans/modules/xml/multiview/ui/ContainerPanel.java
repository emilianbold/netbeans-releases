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
    
    /** Gets node corresponding to this container panel
     * @return Node corresponding node
     */  
    public org.openide.nodes.Node getRoot();
}
