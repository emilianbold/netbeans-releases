
/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.repository.spi;

/**
 * interface for repository keys, must be implemented in client of repository
 * @author Sergey Grinev, Vladimir Voskresensky
 */
public interface Key {
    /** returns associated factory
     * @return associated factory
     */
    PersistentFactory getPersistentFactory();

    /** return a unit which serves as a sign of global set of keys, e.g., 
     *  projects in the IDE
     * @return the unit
     */
    String getUnit();
    
    /** Behaviors allow repository to optimize
     *  storaging files
     */
    enum Behavior { 
        Default, // default behavior
        LargeAndMutable   // mutable object; tends to change it's state *a lot*
    }

    /** returns behavior of the object 
     * @return key Behavior
     */
    Behavior getBehavior();
    
    /** returns depth of primary key's hierarchy *
     * @return depth of primary key's hierarchy
     */
    int getDepth();
    
    /** returns n-th element of primary hierarchy *
     * @param level n
     * @return n-th primary element
     */
    String getAt(int level);
    
    /** returns depth of secondary key's hierarchy *
     * @return secondary depth
     */
    int getSecondaryDepth();
    
    /** returns n-th element of secondary hierarchy *
     * @param level n
     * @return n-th secondary element
     */
    int getSecondaryAt(int level);
}
