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

package org.netbeans.api.diff;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import javax.swing.JToolBar;

/**
 * Controller interface that allows external code to control the DIFF component.
 *
 * @author Maros Sandor, Martin Entlicher
 */
public interface DiffView {

    /**
     * This property is fired when the difference count has changed.
     */
    public static final String PROP_DIFF_COUNT = "diffCount"; // NOI18N

    /**
     * Gets the visual DIFF component that modules can include in their GUI.
     *
     * @return Component
     */ 
    public Component getComponent();
    
    /**
     * Gets the number of differences found in sources.
     * 
     * @return int
     */ 
    public int getDifferenceCount();
    
    /**
     * Test whether this view can change the current difference.
     * This is expected to be true when the view has a visual indication
     * of the current difference.
     */
    public boolean canSetCurrentDifference();

    /**
     * Instructs the DIFF view to navigate to the n-th difference.
     * 
     * @param diffNo The difference number (-1 means hide current difference visualization)
     * @throws UnsupportedOperationException iff {@link #canSetCurrentDifference}
     *         returns <code>false</code>.
     */ 
    public void setCurrentDifference(int diffNo) throws UnsupportedOperationException;
    
    /**
     * Get the current difference that is displayed in the view.
     *
     * @return The current difference number
     * @throws UnsupportedOperationException iff {@link #canSetCurrentDifference}
     *         returns <code>false</code>.
     */
    public int getCurrentDifference() throws UnsupportedOperationException;
    
    /**
     * Get a toolbar, that is adviced to be displayed together with the component
     * obtained from {@link #getComponent}.
     *
     * @return the toolbar or <code>null</code> when no toolbar is provided by
     *         this view.
     */
    public JToolBar getToolBar();
    
    /**
     * Add a property change listener.
     * @param l The property change listener
     */
    public void addPropertyChangeListener(PropertyChangeListener l);
    
    /**
     * Remove a property change listener.
     * @param l The property change listener
     */
    public void removePropertyChangeListener(PropertyChangeListener l);
    
}
