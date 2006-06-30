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

package org.netbeans.tax;

import org.netbeans.tax.spec.DTD;


/**
 * Basically parameter entity treated as fragment. It's used
 * to model external DTDs and external parameter entities.
 */
public class TreeDTDFragment extends TreeDocumentFragment {
    /**
     * Creates new TreeDocumentFragment.
     * @throws InvalidArgumentException
     */
    public TreeDTDFragment() throws InvalidArgumentException {
        super();
    }

    /** Creates new TreeDocumentFragment -- copy constructor. */
    protected TreeDTDFragment (TreeDTDFragment documentFragment, boolean deep) {
        super (documentFragment, deep);
    }


    //
    // from TreeObject
    //

    /**
     */
    public Object clone (boolean deep) {
        return new TreeDTDFragment (this, deep);
    }

    /**
     */
    protected TreeObjectList.ContentManager createChildListContentManager() {
        return new ExternalDTDContentManager();
    }

    /**
     * External DTD content manager (assigned to externalDTDList).
     * All kids use as parent node wrapping TreeDocumentType.
     * All kids must be DTD.Child instances.
     */
    protected class ExternalDTDContentManager extends TreeParentNode.ChildListContentManager {

        /**
         */
        public TreeNode getOwnerNode () {
            return TreeDTDFragment.this;
        }

        /**
         */
        public void checkAssignableObject (Object obj) {
            super.checkAssignableObject (obj);
            checkAssignableClass (DTD.Child.class, obj);
        }

    }

}
