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

package org.netbeans.modules.xml.xam.ui.category;

import java.awt.Component;

/**
 * A SearchComponent presents the interface for the search feature.
 *
 * @author Nathan Fiedler
 */
public interface SearchComponent {

    /**
     * Return the interface component for adding this to a container.
     *
     * @return  component for adding to interface.
     */
    Component getComponent();

    /**
     * Hide the search component from the user.
     */
    void hideComponent();

    /**
     * Make the search component visible to the user, possibly putting the
     * focus in the input field.
     */
    void showComponent();
}
