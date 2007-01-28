/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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
