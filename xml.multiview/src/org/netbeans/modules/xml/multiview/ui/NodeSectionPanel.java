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

/**
 * NodeSectionPanel.java 
 * This is the interface to section panels that correspond to specific node in explorer view.
 *
 * Created on October 17, 2004, 8:57 PM
 * @author mkuchtiak
 */
public interface NodeSectionPanel {
    
    /** Gets the corresponding node for the panel
     * @return Node Node that coresponds to this Section Panel
     */
    public org.openide.nodes.Node getNode();
    
    /** Sets this panel as the active panel in the section view
     * @param boolean active`tells if the panel should be active or passive
     * @param boolean active`tells if the panel should be active or passive
     */    
    public void setActive(boolean active);
    
    /** Tells whether the panel is active or not.
     * @return boolean true or false
     */    
    public boolean isActive();
    
    /** Opens (extends) the panel for editing.
     */       
    public void open();
    
    /** Scrolls the panel to be visibel in scrollPane.
     */        
    public void scroll(javax.swing.JScrollPane scrollPane);

}
