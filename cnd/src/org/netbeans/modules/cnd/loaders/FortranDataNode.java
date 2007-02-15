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

package org.netbeans.modules.cnd.loaders;

import org.openide.nodes.Children;

/**
 *  A node representing this Fortran object. The Fortran file could be F77,
 *  F90, or F95.
 */
public class FortranDataNode extends CndDataNode {

    /** The base name of the Fortran source icon */
    private static final String FortranSrcIcon =
		"org/netbeans/modules/cnd/loaders/FortranSrcIcon.gif"; // NOI18N

    public FortranDataNode(CndDataObject obj) {
	super(obj, Children.LEAF, FortranSrcIcon);
    }

    public FortranDataNode(CndDataObject obj, Children ch) {
	super(obj, ch, FortranSrcIcon);
    }
}
