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
package org.openide.filesystems;

import java.util.EventObject;


/** Fired when a filesystem pool is reordered.
 * @see Repository#reorder
 */
public class RepositoryReorderedEvent extends EventObject {
    static final long serialVersionUID = -5473107156345392581L;

    /** permutation */
    private int[] perm;

    /** Create a new reorder event.
     * @param fsp the filesystem pool being reordered
     * @param perm the permutation of filesystems in the pool
     */
    public RepositoryReorderedEvent(Repository fsp, int[] perm) {
        super(fsp);
        this.perm = perm;
    }

    /** Get the affected filesystem pool.
     * @return the pool
     */
    public Repository getRepository() {
        return (Repository) getSource();
    }

    /** Get the permutation of filesystems.
     * @return the permutation
     */
    public int[] getPermutation() {
        int[] nperm = new int[perm.length];
        System.arraycopy(perm, 0, nperm, 0, perm.length);

        return nperm;
    }
}
