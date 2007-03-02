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
package org.netbeans.modules.visualweb.propertyeditors.css;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * Super class for all Style editors
 * @author  Winston Prakash
 */
public class StyleEditor extends JPanel{
    /**
     * Holds value of property displayName.
     */
    private String displayName;
    
    /**
     * Holds value of property icon.
     */
    private Icon icon;
    
    /**
     * Getter for property displayName.
     * @return Value of property displayName.
     */
    public String getDisplayName() {
        return this.displayName;
    }
    
    /**
     * Setter for property displayName.
     * @param displayName New value of property displayName.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Getter for property icon.
     * @return Value of property icon.
     */
    public Icon getIcon() {
        return this.icon;
    }
    
    /**
     * Setter for property icon.
     * @param icon New value of property icon.
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
    }
    
}
