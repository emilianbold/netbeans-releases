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

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.Node;


/** Superclass for Elf objects in the Repository.
 *
 */
public class OrphanedElfObject extends ExeObject {

    /** Serial version number */
    static final long serialVersionUID = 4984421756480377983L;

    public OrphanedElfObject(FileObject pf, ExeLoader loader)
	throws DataObjectExistsException {
	super(pf, loader);
    }

    protected void init() {
    }

    protected Node createNodeDelegate() {
	return new OrphanedElfNode(this);
    }
}

