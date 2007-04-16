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
package org.netbeans.modules.visualweb.designer.markup;

import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.xerces.dom.CoreDocumentImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * XXX Originally in insync.
 * 
 * <p>
 * Element for &lt;table&gt; elements. We need some extra state here to
 * be able to automatically handle CSS styling of tables missing tbody
 * elements.
 * The problem is that a html document like this:
 * <pre>
 *  &lt;style&gt;tbody {background: red}&lt;/style&gt;
 *  &lt;table&gt;&lt;tr&gt;&lt;td&gt;Hello&lt;/td&gt;&lt;/tr&gt;&lt;/table&gt;
 * </pre>
 * should have a red background. That's because a &lt;tbody&gt; element should
 * "automatically" be inserted in the table.  I first tried an approach
 * where I actually inserted &lt;tbody&gt; elements during the parse, but
 * this didn't work right -- doing insertBefore calls during Xerces parse
 * caused the structure of the document to be wrong because it doesn't
 * seem to look at the return value from insertBefore. So I then changed
 * the code to try to "patch up" the table during the box creation phase;
 * this had the problem that the screen would refresh multiple times since
 * layout would be invalidated while rendering the page (table has changed)
 * and besides, I would have to acquire a write lock since I was modifying
 * the source dom (except for JSF-rendered tables in DocumentFragments);
 * and each write unlock would cause a flush, so documents with multiple
 * tables would cause multiple flushes. I then switched to a write lock
 * for the entire phase -- but this was getting to be a big ugly solution.
 * </p>
 * <p>
 * So I instead went for the following scheme:
 * When a &lt;table&gt; element is created, create a special subclass of
 * RaveElement for it. This subclass tracks the "tbody" child, if any.
 * On insert, any &lt;tr&gt; elements inserted directly on the table are put
 * in a "secret" tbody element. The tbody element is secret in that it
 * is not added to the DOM itself, but it is however linked stylewise
 * via the setStyleParent, such that my modified Batik CSS parser will
 * follow the style parent link for the &lt;tr&gt; to jump to the &lt;tbody&gt; first,
 * then the &lt;table&gt;, instead of directly to the &lt;table&gt;. This will cause
 * the (unconnected) &lt;tbody&gt; element to be styled too, and as a result,
 * the proper CSS inheritance and treatment of the &lt;tbody&gt; tag works.
 * </p>
 *
 * @author Tor Norbye
 */
class RaveTableElement extends RaveElement {

    /**
     *
     */
    private static final long serialVersionUID = 3257003272006154552L;

//    private RaveElement tbody;
//    private RaveSourceElement tbody;
    private Element tbody;

    public RaveTableElement(CoreDocumentImpl ownerDocument,
                        String namespaceURI,
                        String qualifiedName) throws DOMException {
        super(ownerDocument, namespaceURI, qualifiedName);
    }

    public Node insertBefore(Node newChild, Node refChild) {
        if (newChild.getNodeType() == Node.ELEMENT_NODE) {
            // The table spec requires us to insert a tbody between
            // a table and a tr
//            RaveElement xel = (RaveElement)newChild;
//            RaveSourceElement xel = (RaveSourceElement)newChild;
            Element xel = (Element)newChild;
            String tag = xel.getTagName();
            // TODO -- should be using CSS instead here....
            //  Css.getComputedStyle(element, XhtmlCss.DISPLAY_INDEX) == CssValueConstants.TABLE_ROW_GROUP_VALUE) {
            if (tag.equals(HtmlTag.TBODY.name)) {
                tbody = xel;
            } else if (!(tag.equals(HtmlTag.TFOOT.name) ||
                         tag.equals(HtmlTag.THEAD.name) ||
                         tag.equals(HtmlTag.CAPTION.name))) {
                if (tbody == null) {
                    // Let's say the user has a table with multiple rows
                    // that aren't inside a tbody, but also contains a tbody
                    // further down. It would be nice if we could associate
                    // these tr's with the "existing" tbody instead; afterall,
                    // it could have custom-styles applied to it.
                    // However, we can't do that here, since insertBefore
                    // is called during the parse, and the tbody hasn't been
                    // seen yet. I'm going to punt on this issue for now.
                    // But I'm stashing the tbody element away when I see
                    // it such that I can at least associate later tr's with
                    // it.
//                    tbody = (RaveElement)getOwnerDocument().createElement(HtmlTag.TBODY.name);
                    tbody = getOwnerDocument().createElement(HtmlTag.TBODY.name);
                    ((CSSEngine.StyleElementLink)tbody).setStyleParent(this);
                }
                ((CSSEngine.StyleElementLink)xel).setStyleParent((CSSStylableElement)tbody);
            }
        }
        return super.insertBefore(newChild, refChild);
    }

    /** Return the tbody element for the table. This may be null
     * if the table has no rows. It may also be an element that is
     * not actually in the DOM, if the table didn't actually include a
     * tbody element.
     */
//    public RaveSourceElement getTbody() {
    public Element getTbody() {
        return tbody;
    }
}
