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

package org.netbeans.spi.editor.hints;

import java.beans.PropertyChangeListener;
import java.util.List;

/**A list of fixes that allows lazy computation of the fixes for an error.
 *
 * @author Jan Lahoda
 */
public interface LazyFixList {
    
    public static final String PROP_FIXES = "fixes";
    public static final String PROP_COMPUTED = "computed";
    
    public void addPropertyChangeListener(PropertyChangeListener l);
    
    public void removePropertyChangeListener(PropertyChangeListener l);
    
    /**Should return false if there will not be any fixes in the list for sure.
     * Should return true otherwise.
     * Should run very fast - should not try to actualy compute the fixes.
     * 
     * @return false if this list will never contain any fixes, true otherwise.
     */
    public boolean probablyContainsFixes();
    
    public List<Fix> getFixes();
    
    /**Returns true if the list of fixes will not changed anymore (it is computed).
     *
     * @return true if the list of fixes is computed.
     */
    public boolean isComputed();
    
}
