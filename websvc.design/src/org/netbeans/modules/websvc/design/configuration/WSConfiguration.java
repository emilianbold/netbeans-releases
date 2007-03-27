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

package org.netbeans.modules.websvc.design.configuration;

import javax.swing.Icon;

/**
 *
 * @author Ajit Bhate
 */
public interface WSConfiguration {
    
    /**
     * Returns the user interface component for this WSConfiguration.
     *
     * @return  the user interface component.
     */
    java.awt.Component getComponent();

    /**
     * Returns the user-oriented description of this WSConfiguration, for use in
     * tooltips in the usre interface.
     *
     * @return  the human-readable description of this WSConfiguration.
     */
    String getDescription();

    /**
     * Returns the display icon of this WSConfiguration.
     *
     * @return  icon for this WSConfiguration.
     */
    Icon getIcon();

    /**
     * Returns the display name of this WSConfiguration.
     *
     * @return  title for this WSConfiguration.
     */
    String getDisplayName();

}
