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

package org.netbeans.modules.xml.multiview.cookies;

import org.netbeans.modules.xml.multiview.ui.NodeSectionPanel;

public interface SectionFocusCookie  {
    /**
     * Request to set the focus for the specified section panel.
     *
     * @param NodeSectionPanel panel Section panel that need to be focused on
     * @return boolean return true if the focus was able to be set on the identified Object
     */
    public boolean focusSection(NodeSectionPanel panel);
}
