
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    CharSequence getUnit();

    /** return a unit id which serves as a sign of global set of keys, e.g., 
     *  projects in the IDE
     * @return the unit
     */
    int getUnitId();
    
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
    CharSequence getAt(int level);
    
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
