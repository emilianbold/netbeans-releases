/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visualweb.designer.markup;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.apache.xerces.dom.CoreDocumentImpl;
import org.apache.xerces.dom.TextImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.Text;

// CVS note: This file used to be called XhtmlText (same directory)
// if you need to look at older CVS history

/**
 * XXX Originally in insync.
 * This class represents an individual TEXT_NODE in an RaveDocument DOM.
 *
 * @author Tor Norbye
 */
class RaveText extends TextImpl {
    private static final long serialVersionUID = 4122818088460105010L;
//    private boolean jspx;
    // XXX This seems to be a dummy unused field.
//    RaveText source;

//    /** Flag which indicates whether this node lives in the "source" DOM (typically the JSP file)
//     * or the "rendered" DOM (typically the HTML file). I chose to use "render"/"source" terms
//     * instead of HTML/JSP terms since the JSP can itself contain HTML markup which might muddy the
//     * issue. */
//    private boolean rendered;
//
//    /** If this is a "rendered" node (see {@link isRendered} then alternate points to its JSP
//     * source node, and if this is a node in the source DOM (the JSP itself), then alternate
//     * will point to the first, top level HTML node rendered from this node.   HTML markup
//     * in the JSP will have a 1-1 mapping to HTML markup in the rendered DOM. For JSF components,
//     * only the top level HTML rendered elements will point back to the source JSP node; children
//     * will be null indicating that they have no direct alternate node.
//     */
//    private RaveText alternate;

    
    public RaveText() {
    }

    /** Factory constructor. */
    public RaveText(CoreDocumentImpl ownerDoc, String data) {
        super(ownerDoc, data);
    }

    // Ensure that split text nodes "inherit" the jspx attribute.
    public Text splitText(int offset) throws DOMException {
//        RaveTextImpl t = (RaveTextImpl)super.splitText(offset);
        RaveText t = (RaveText)super.splitText(offset);
        t.copyFrom(this);
//        t.source = null; // source should NOT be duplicated here - that's the source for the pre-split node

        return t;
    }

    // Ensure that cloned text nodes "inherit" the jspx attribute.
    public Text replaceWholeText(String content) throws DOMException {
//        RaveTextImpl t = (RaveTextImpl)super.replaceWholeText(content);
        RaveText t = (RaveText)super.replaceWholeText(content);
        t.copyFrom(this);
//        t.source = null; // source should NOT be duplicated here

        return t;
    }

    // Moved to MarkupServiceImpl.
//    /** Recursively mark all text nodes in the given node subtree as
//     * being jspx nodes
//     */
//    public static void markJspxSource(Node n) {
//        if (n instanceof RenderNode) {
//            ((RenderNode)n).setJspx(true);
//        }
//
//        NodeList list = n.getChildNodes();
//        int len = list.getLength();
//
//        for (int i = 0; i < len; i++) {
//            markJspxSource(list.item(i));
//        }
//    }

    // XXX Get rid of this.
    /** During a clone operation, copy relevant info from the given
     * source element to this target element
     */
//    void copyFrom(RaveTextImpl from) {
    void copyFrom(RaveText from) {
//        jspx = from.jspx;
//        setJspx(from.isJspx());
        MarkupServiceImpl.setJspxNode(this, MarkupServiceImpl.isJspxNode(from));
//        this.source = (from.source != null) ? from.source : from;
    }

//    public boolean isRendered() {
//        return rendered;
//    }
//    public abstract boolean isRendered();

//    public RaveText getRendered() {
//        if (rendered) {
//            // TODO - should anyone ask for this if they already have HTML ? The code
//            // might be confused so perhaps I should assert on this instead to pinpoint
//            // questionable code...
//            return this;
//        }
//
//        return alternate;
//    }
//
//    public RaveText getSource() {
//        if (!rendered) {
//            // TODO - should anyone ask for this if they already have JSP ? The code
//            // might be confused so perhaps I should assert on this instead to pinpoint
//            // questionable code...
//            return this;
//        }
//
//        return alternate;
//    }
//
//    public void setSource(RaveText source) {
//        rendered = true;
//        alternate = source;
//
//        if (alternate != null) {
//            ((RaveTextImpl) alternate).rendered = false;
//            ((RaveTextImpl) alternate).alternate = this;
//        }
//    }
//
//    public void setRendered(RaveText rendered) {
//        this.rendered = false;
//        alternate = rendered;
//
//        if (alternate != null) {
//            ((RaveTextImpl) alternate).rendered = true;
//            ((RaveTextImpl) alternate).alternate = this;
//        }
//    }

//    public boolean isJspx() {
////        return jspx;
//    }
//
//    public void setJspx(boolean jspx) {
////        this.jspx = jspx;
//    }

//    public /*RaveRenderNode*/ Node getSourceNode() {
//        return getSource();
//    }
//
//    public /*RaveRenderNode*/ Node getRenderedNode() {
//        return getRendered();
//    }

//    public void markRendered() {
//        this.rendered = true;
//    }

    public String toString() {
        return "RaveText[" + getNodeValue() + "]";
    }
}
