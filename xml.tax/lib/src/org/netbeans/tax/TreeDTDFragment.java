/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
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
