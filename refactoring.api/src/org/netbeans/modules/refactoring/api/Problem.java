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


/** Class used to represent problems encountered when performing
 * various refactoring calls. Problems can be chained (using setNext method)
 * - every problem can point to the following problem.
 *
 * @author Martin Matula
 */
public final class Problem {
    private final boolean fatal;
    private final String message;
    private Problem next = null;
    private ProblemDetails details;

    /** Creates new instance of Problem class.
     * @param fatal Indicates whether the problem is fatal.
     * @param message Textual description of the problem.
     */
    public Problem(boolean fatal, String message) {
        this.fatal = fatal;
        this.message = message;
    }
    
    public Problem(boolean fatal, String message, ProblemDetails details) {
        this(fatal, message);
        this.details = details;
    }
    
    /** Indicates whether the problem is fatal.
     * @return <code>true</code> if the problem is fatal, otherwise returns <code>false</code>.
     */
    public boolean isFatal() {
        return fatal;
    }
    
    /** Returns textual description of the problem.
     * @return Textual description of the problem.
     */
    public String getMessage() {
        return message;
    }
    
    /** Returns the following problem (or <code>null</code> if there none).
     * @return The following problem.
     */
    public Problem getNext() {
        return next;
    }
    
    /**
     * Sets the following problem. The problem can be set only once - subsequent
     * attempts to call this method will result in IllegalStateException.
     * @param next The following problem.
     * @throws java.lang.IllegalStateException subsequent attempts to call this method will result in IllegalStateException.
     */
    public void setNext(Problem next) throws IllegalStateException {
        if (this.next != null) {
            throw new IllegalStateException("Cannot change \"next\" property of Problem."); //NOI18N
        }
        this.next = next;
    }

    /**
     * Getter for ProblemDetails
     * @return instance of ProblemDetails or null
     */
    public ProblemDetails getDetails() {
        return details;
    }
}
