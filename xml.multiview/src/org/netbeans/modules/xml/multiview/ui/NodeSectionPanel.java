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
 *
 * Created on October 17, 2004, 8:57 PM
 * @author mkuchtiak
 */
public interface NodeSectionPanel {
    public org.openide.nodes.Node getNode();
    public void setActive(boolean active);
    public void open();
    public void scroll(javax.swing.JScrollPane scrollPane);
}
