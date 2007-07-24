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
package org.netbeans.modules.refactoring.spi.ui;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.openide.util.HelpCtx;

/** Interface representing UI for a refactoring.
 *
 * @author Martin Matula
 */
public interface RefactoringUI {
    /** Returns name of the refactoring.
     * @return Refactoring name.
     */
    String getName();
    
    /** Returns description of the refactoring.
     * @return Refactoring description.
     */
    String getDescription();
    
    /** Indicates whether this class represents a real refactoring that changes
     * code or whether it is just a query (e.g. all usages for a class).
     * @return <code>true</code> if the class represents only a query,
     * <code>false</code> if the class represents a real refactoring.
     */
    boolean isQuery();
    
    /** Returns refactoring-specific panel containing input fields for 
     * refactoring parameters. 
     * Name of the panel returned from this method will be used as the dialog
     * name. 
     * this method might return null if hasParameters return false.
     * @param parent dialog in which that the returned panel will be displayed in.
     * @see #hasParameters
     * @return Refactoring-specific parameters panel.
     */
    CustomRefactoringPanel getPanel(ChangeListener parent);

    /** Implementation of this method should set the refactoring parameters entered
     * by user into the refactoring-specific parameters panel (returned from getPanel
     * method) into the underlying refactoring object.
     * @return Chain of problems returned from the underlying refactoring object
     * when trying to set its parameters.
     */
    Problem setParameters();
    
    /**
     * check parameters of refactoring
     * @return Chain of problems returned from the underlying refactoring object
     * when trying to check its parameters.
     */
    Problem checkParameters();
    
    /**
     * true, if refactoring has parameters
     * false otherwise. In this case {@link #getPanel} method can return null
     * @return false if this UI does not require any parameters. True otherwise.
     */
    boolean hasParameters();
    
    /** Returns underlying refactoring object.
     * @return Underlying refactoring object.
     */
    AbstractRefactoring getRefactoring();
    
    /**
     * @return helpcontext
     */
    public HelpCtx getHelpCtx();
}
