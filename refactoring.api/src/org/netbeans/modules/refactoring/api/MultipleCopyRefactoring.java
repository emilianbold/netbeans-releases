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

package org.netbeans.modules.refactoring.api;

import org.openide.util.Lookup;

/**
 * This class is just holder for parameters of Multiple Copy Refactoring. 
 * Refactoring itself is implemented in plugins
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see AbstractRefactoring
 * @see RefactoringSession
 * @author Jan Becicka
 */
public final class MultipleCopyRefactoring extends AbstractRefactoring {

    private Lookup target;

    /**
     * Public constructor takes Lookup containing objects to refactor as parameter.
     * Multiple Copy Refactoring Refactoring currently does not have any implementation.
     * @param objectsToCopy store your objects into Lookup
     */
    public MultipleCopyRefactoring (Lookup objectsToCopy) {
        super(objectsToCopy);
    }

    /**
     * Target where copy should be created
     * Multiple Copy Refactoring Refactoring currently does not have any implementation.
     * @param target
     */
    public void setTarget(Lookup target) {
        this.target = target;
    }
    
    /**
     * Target where copy should be created
     * Multiple Copy Refactoring Refactoring currently does not have any implementation.
     * @return target
     */
    public Lookup getTarget() {
        return target;
    }
}


