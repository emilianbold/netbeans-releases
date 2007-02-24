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
 * File       : IInteractionOperator.java
 * Created on : Sep 16, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.core.primitivetypes;

/**
 * @author Aztec
 */
public interface IInteractionOperator
{
    // Designates that the CombinedFragment represents a choice of behavior.
    public static final int IO_ALT = 0;

    // Designates a guard that is the negation of the conjunction of all other
    // guards in the enclosing CombinedFragment.
    public static final int IO_ELSE = 1;

    // Designates that the CombinedFragment represents a choice of behavior 
    // where either the (sole) operand happens or nothing happens.
    public static final int IO_OPT = 2;

    // Designates that the CombinedFragment represents a parallel merge between
    // the behaviors of the operands.
    public static final int IO_PAR = 3;

    // Designates that the CombinedFragment represents a loop.
    public static final int IO_LOOP = 4;

    // Designates that the CombinedFragment represents a critical region.
    public static final int IO_REGION = 5;

    // Designates that the CombinedFragment represents traces that are defined 
    // to be impossible.
    public static final int IO_NEG = 6;

    // Designates that the CombinedFragment represents an assertion.
    public static final int IO_ASSERT = 7;

    // Designates that the CombinedFragment represents a weak sequencing between
    // the behaviors of the operands.
    public static final int IO_SEQ = 8;

    // Designates that the CombinedFragment represents a strict sequencing 
    // between the behaviors of the operands.
    public static final int IO_STRICT = 9;

    // Designates that there are some message types that are not shown within 
    // this combined fragment.
    public static final int IO_FILTER = 10;
}
