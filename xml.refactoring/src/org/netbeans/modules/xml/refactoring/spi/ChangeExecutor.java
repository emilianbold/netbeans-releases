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
package org.netbeans.modules.xml.refactoring.spi;

import java.io.IOException;
import org.netbeans.modules.xml.refactoring.RefactorRequest;
import org.netbeans.modules.xml.refactoring.impl.RefactoringUtil;
import org.netbeans.modules.xml.xam.Referenceable;

/**
 * Executor of a refactor target change.
 * A refactoring request processing consist of 2 steps:
 * (1) making the change on the target component,
 * (2) refactoring selected the usage components.
 * The implementation of this class is reponsible for the first step.
 *
 * @author Nam Nguyen
 */
public abstract class ChangeExecutor {

    public abstract <T extends RefactorRequest> boolean canChange(Class<T> changeType, Referenceable target);

    /**
     * Perform a pre-change checking on the refactor request.
     * Implementation should quietly ignore unsupported refactoring type.
     */
    public void precheck(RefactorRequest request) {
    }

    /**
     * Perform the change specified by the refactor request.  Any errors that would
     * fail the overall refactoring should be reported throught #RefactoringRequest.addError
     * Implementation should quietly ignore unsupported refactoring type.
     */
    public void doChange(RefactorRequest request) throws IOException {
    }

    /**
     * Returns UI helper in displaying the usages.  Implementation could override
     * the default UI to help display usages in a more intuitive way than the 
     * generic helper.
     */
    public UIHelper getUIHelper() {
        return new UIHelper();
    }

}

