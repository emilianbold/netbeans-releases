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

package org.netbeans.spi.mobility.project.ui.customizer;
/**
 * Interface that should be implemented by component (usualy customizer panel)
 * that customizes group of propeties handled collectively by VisualPropertySupport.
 * @author Adam Sotona
 */
public interface VisualPropertyGroup {
    
    /**
     * Method used to register components of the group with the group properties using
     * VisualPropertySupport and current useDefault state.
     * @param useDefault boolean current state of "Use Defauls" switch
     */
    public void initGroupValues(boolean useDefault);
    
    /**
     * get list of property names in the group
     * @return String[] property names
     */
    public String[] getGroupPropertyNames();
    
}
