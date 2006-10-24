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
package org.netbeans.modules.refactoring.spi;

import java.util.Collection;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;

/** Interface implemented by refactoring plugins. Contains callback methods which a particular refactoring
 * calls to check pre-conditions, validity of parameters and collect refactoring elements.
 * It is expected that the refactoring that this plugin operates on is passed to the plugin
 * in its constructor by a corresponding implementation of {@link RefactoringPluginFactory}.
 *
 * @author Martin Matula
 */
public interface RefactoringPlugin {
    /** Checks pre-conditions of the refactoring and returns problems.
     * @return Problems found or null (if no problems were identified)
     */
    Problem preCheck();
    
    /** Checks parameters of the refactoring.
     * @return Problems found or null (if no problems were identified)
     */
    Problem checkParameters();
    
    /** Fast checks parameters of the refactoring. This method will be used for 
     * online error checking.
     * @return Problems found or null (if no problems were identified)
     */
    Problem fastCheckParameters();
    
    
    /**
     * Asynchronous request to cancel ongoing long-term request (such as preCheck(), checkParameters() or prepare())
     */
    void cancelRequest();
    
    /** Collects refactoring elements for a given refactoring.
     * @param refactoringElements RefactoringElementsBag of refactoring elements - the implementation of this method
     * should add refactoring elements to this collections. It should make no assumptions about the collection
     * content.
     * @return Problems found or null (if no problems were identified)
     */
    Problem prepare(RefactoringElementsBag refactoringElements);
}
