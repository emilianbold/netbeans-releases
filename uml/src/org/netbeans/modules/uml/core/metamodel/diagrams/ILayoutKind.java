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


package org.netbeans.modules.uml.core.metamodel.diagrams;


public interface ILayoutKind
{
    public final int LK_NO_LAYOUT = 0;
    public final int LK_HIERARCHICAL_LAYOUT = 1;
    public final int LK_ORTHOGONAL_LAYOUT = 2;
    public final int LK_SYMMETRIC_LAYOUT = 3;
    public final int LK_TREE_LAYOUT = 4;
    public final int LK_CIRCULAR_LAYOUT = 5;
    public final int LK_SEQUENCEDIAGRAM_LAYOUT = 6;
    public final int LK_GLOBAL_LAYOUT = 7;
    public final int LK_INCREMENTAL_LAYOUT = 8;
    public final int LK_UNKNOWN_LAYOUT = 255;
}
