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
 * This class is just holder for parameters of Move Refactoring. 
 * Refactoring itself is implemented in plugins.
 * 
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see AbstractRefactoring
 * @see RefactoringSession
 * @author Jan Becicka
 */
public final class MoveRefactoring extends AbstractRefactoring {

    private Lookup target;

    /**
     * Public constructor takes Lookup containing objects to refactor as parameter.
     * Move Refactoring implementations currently understand following types:
     * <table border="1">
     *   <tr><th>Module</th><th>Types the Module Understands</th><th>Implementation</th></tr>
     *   <tr><td>Refactoring API (Default impl.)</td><td>{@link org.openide.filsuystems.FileObject}(s)</td><td>Does file(s) move</td></tr>
     *   <tr><td>Java Refactoring</td><td>{@link org.openide.filsuystems.FileObject}(s) with content type text/x-java</td><td>Does refactoring inside .java files</td></tr>
     * </table>
     * @param objectsToMove store your objects into Lookup
     */
    public MoveRefactoring (Lookup objectsToMove) {
        super(objectsToMove);
    }

    /**
     * Target for moving.
     * Move Refactoring implementations currently understand following types:
     * <table border="1">
     *   <tr><th>Module</th><th>Types the Module Understands</th><th>Implementation</th></tr>
     *   <tr><td>Refactoring API (Default impl.)</td><td>{@link java.net.URL}</td>
     *        <td>Creates direstory corresponding to specified {@link java.net.URL} if does not 
     *            exist and moves all FileObjects into this folder.</td></tr>
     *   <tr><td>Java Refactoring</td><td>{@link java.net.URL}</td><td>Does move refactoring inside .java files</td></tr>
     * </table>
     * @param target
     */
    public void setTarget(Lookup target) {
        this.target = target;
    }

    /**
     * Target for moving
     * @see #setTarget
     * @return target
     */
    public Lookup getTarget() {
        return this.target;
    }
}
