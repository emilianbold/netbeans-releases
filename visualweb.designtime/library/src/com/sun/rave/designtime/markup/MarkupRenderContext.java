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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.sun.rave.designtime.markup;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * <P>A MarkupRenderContext represents the context in which a markup bean will be rendering its
 * markup output.  This is a call-back interface used in the MarkupDesignInfo.customizeRender(...)
 * method.  A component (bean) author can use the methods in this interface to alter the markup
 * stream that will be displayed on the design surface.  They can also associate custom behavior
 * with portions of markup using the associateMouseRegion(...) method.</P>
 *
 * <P><B>IMPLEMENTED BY CREATOR</B> - This interface is implemented by Creator for use by the
 * component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see MarkupDesignInfo#customizeRender(MarkupDesignBean, MarkupRenderContext)
 */
public interface MarkupRenderContext {

    /**
     * The 'rendered' DOM document fragment from the JSF component.
     */
    public DocumentFragment getDocumentFragment();

    /**
     * The starting position within the renderFragment from where nodes were rendered for this
     * component.
     */
    public MarkupPosition getBeginPosition();

    /**
     * The ending position within the renderFragment to which nodes were rendered for this
     * component. If this position is the same as begin, no nodes were rendered for the component.
     * Note that the end position is NOT inclusive, it points to the NEXT node in the node list.
     * Note also that the parent node for begin and end will always be the same.
     */
    public MarkupPosition getEndPosition();

    /**
     * Associates a mouse region with a particular element of rendered markup.
     *
     * @param element Element
     * @param region MarkupMouseRegion
     */
    public void associateMouseRegion(Element element, MarkupMouseRegion region);
}
