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

package org.netbeans.modules.websvc.wsdl.config.api;

import org.netbeans.modules.schema2beans.BaseBean;

/**
 * Interface representing the root of interfaces bean tree structure.  Inspired
 * by (and heavily copied from) CommonDDBean in web/ddapi
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface!</em></font></b>
 *</p>
 *
 * @author Peter Williams
 */
public interface RootInterface extends CommonDDBean {
    /**
     * Used in {@link #merge} method.<br>
     * The result of merge operation is the intersection of graphs.
     */
    public static final int MERGE_INTERSECT = BaseBean.MERGE_INTERSECT;
    /**
     * Used in {@link #merge} method.<br>
     * The result of merge operation is the union of graphs.
     */
    public static final int MERGE_UNION = BaseBean.MERGE_UNION;
    /**
     * Used in {@link #merge} method.<br>
     * The result of merge operation is the first graph updated with the changes in another graph.
     */
    public static final int MERGE_UPDATE = BaseBean.MERGE_UPDATE;

    /**
     * Writes the deployment descriptor data from deployment descriptor bean graph to file object.<br>
     * This is more convenient method than {@link org.netbeans.modules.j2ee.dd.api.common.CommonDDBean#write} method.<br>
     * The locking problems are solved for the user in this method.
     *
     * @param fo File Object where to write the content of depl.descriptor holding in bean tree structure
     */
    public void write(org.openide.filesystems.FileObject fo) throws java.io.IOException ;

    /**
     * Merging two bean tree structures together.<pre>
     *
     *There are several ways odf merging :
     *
     *    Let's define:
     *        G1 the current graph and G2 the new graph we want to merge
     *        E1 the set of element of G1 that don't exist anymore in G2.
     *        E2 the set of new elements of G2 that don't exist in G1.
     *
     *    Then,
     *        MERGE_UPDATE is     G1 - E1 + E2    (G1 becomes G2)
     *        MERGE_UNION is     G1 U G2 <=> G1 + E2
     *        MERGE_INTERSECT is    G1 n G2 <=> (G1 U G2) - E1 - E2
     *</pre>
     * @param bean root of the bean graph that is merged with actual bean graph
     * @param mode type of merging (INTERSECT, UNION, UPDATE)
     */
    public void merge(RootInterface root, int mode);
}
