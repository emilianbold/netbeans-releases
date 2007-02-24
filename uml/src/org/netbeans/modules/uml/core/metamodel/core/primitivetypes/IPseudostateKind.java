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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * File       : IPseudostateKind.java
 * Created on : Sep 16, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.core.primitivetypes;

/**
 * @author Aztec
 */
public interface IPseudostateKind
{
    // Splits an incoming transition into several disjoint outgoing transition.
    public static final int PK_CHOICE = 0;

    // When reached as the target of a transition, restores the full state
    // configuration that was active just before the enclosing composite state
    // was last exited.
    public static final int PK_DEEPHISTORY = 1;

    // Splits an incoming transition into several concurrent outgoing 
    // transitions. All of the transitions fire together.
    public static final int PK_FORK = 2;

    // The default target of a transition to the enclosing composite state.
    public static final int PK_INITIAL = 3;

    // Merges transitions from concurrent regions into a single outgoing 
    // transition. All the transitions fire together.
    public static final int PK_JOIN = 4;
   
    // Chains together transitions into a single run-to-completion path. 
    // May have multiple input and/or output transitions. Each complete path 
    // involving a junction is logically independent and only one such path 
    // fires at one time.
    public static final int PK_JUNCTION = 5;
   
    // When reached as the target of a transition, restores the state within 
    // the enclosing composite state that was active just before the enclosing 
    // state was last exited.
    public static final int PK_SHALLOWHISTORY = 6;

    public static final int PK_ENTRYPOINT = 7;

    public static final int PK_STOP = 8;
}
