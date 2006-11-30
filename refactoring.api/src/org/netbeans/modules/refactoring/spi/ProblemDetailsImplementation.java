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

import javax.swing.Action;
import org.openide.util.Cancellable;

/**
 * Typical implementation will invoke UI component on showDetails() call
 * This UI component will display ProblemDetails. There will be a button, or
 * similar UI control, which will be connected to rerunRefactoringAction to
 * invoke refactoring again once the Problem is fixed.
 * @author Jan Becicka
 * @since 1.5.0
 */
public interface ProblemDetailsImplementation {

    /**
     * This method will typically invoke component with ProblemDetails.
     * It is fully upon clients, how this component will be implemented.
     * @param rerunRefactoringAction this action is passed to client component
     * @param parent parent component, than can be closed by cancel method.
     * to allow clients to rerun refactoring once the Problem is fixed.
     */
    void showDetails(Action rerunRefactoringAction, Cancellable parent);

    /**
     * Message that will be displayed in parameters panel as a hint to suggest user,
     * that there are more details available.
     */
    String getDetailsHint();
    
}
