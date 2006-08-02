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
package org.openide.nodes;

import javax.swing.JPanel;


/** An implementation of a node that has children and
* supports reordering by providing Index implementor.
* Index implementor and children can be the same instance,
* allowing us to use either Index.ArrayChildren or Index.MapChildren
*
* @author Jaroslav Tulach, Dafe Simonek
*/
public class IndexedNode extends AbstractNode {
    /** Index implementation */
    private Index indexImpl;

    /** Create an indexed node. Uses {@link Index.ArrayChildren} to both
    * hold the children, and as an implementation of {@link Index}.
    */
    public IndexedNode() {
        super(new Index.ArrayChildren());
        indexImpl = (Index) getChildren();
    }

    /** Allows subclasses to provide their own children and
    * index handling.
    * @param children the children implementation
    * @param indexImpl the index implementation
    */
    protected IndexedNode(Children children, Index indexImpl) {
        super(children);
        this.indexImpl = indexImpl;
    }

    /*
    * @return false to signal that the customizer should not be used.
    *  Subclasses can override this method to enable customize action
    *  and use customizer provided by this class.
    */
    public boolean hasCustomizer() {
        return false;
    }

    /* Returns the customizer component.
    * @return the component
    */
    public java.awt.Component getCustomizer() {
        java.awt.Container c = new JPanel();
        @SuppressWarnings("deprecation")
        IndexedCustomizer customizer = new IndexedCustomizer(c, false);
        customizer.setObject(indexImpl);

        return c;
    }

    /** Get a cookie.
    * @param clazz representation class
    * @return the index implementation or children if these match the cookie class,
    * else using the superclass cookie lookup
    */
    public <T extends Node.Cookie> T getCookie(Class<T> clazz) {
        if (clazz.isInstance(indexImpl)) {
            // ok, Index implementor is enough
            return clazz.cast(indexImpl);
        }

        Children ch = getChildren();

        if (clazz.isInstance(ch)) {
            // ok, children are enough
            return clazz.cast(ch);
        }

        return super.getCookie(clazz);
    }
}
