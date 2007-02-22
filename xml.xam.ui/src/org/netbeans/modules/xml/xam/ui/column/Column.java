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

package org.netbeans.modules.xml.xam.ui.column;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;

/**
 * A Column repesents a visual component in a ColumnView.
 *
 * @author  Todd Fast, todd.fast@sun.com
 * @author  Nathan Fiedler
 */
public interface Column {
    /** Property name for the title property. */
    public static final String PROP_TITLE = "title";

    /**
     * Adds the given property change listener to this column.
     *
     * @param  listener  PropertyChangeListener to add.
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Returns the visual component for this column.
     *
     * @return  visual component.
     */
    JComponent getComponent();

    /**
     * Returns the description of this column. This may be used for setting
     * the accessible description of related user interface components.
     *
     * @return  column description.
     */
    String getDescription();

    /**
     * Returns the title of this column.
     *
     * @return  column title.
     */
    String getTitle();

    /**
     * Removes the given property change listener from this column.
     *
     * @param  listener  PropertyChangeListener to remove.
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
}
