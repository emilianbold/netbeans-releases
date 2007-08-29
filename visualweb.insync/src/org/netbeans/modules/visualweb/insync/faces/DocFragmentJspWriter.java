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
package org.netbeans.modules.visualweb.insync.faces;

import com.sun.rave.designtime.markup.MarkupDesignBean;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.jsfsupport.container.FacesContainer;
import java.io.IOException;
import java.io.Writer;

import javax.faces.component.UIComponent;
import javax.faces.context.ResponseWriter;
import org.openide.ErrorManager;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.markup.MarkupDesignBean;

// XXX Originally in jsfsupport, which was wrong location.
/**
 * DocFragmentJspWriter provides a direct-to-DOM JSP writer for improved design-time DOM handling
 *
 * @author Carl Quinn
 * @author Tor Norbye
 * @version 1.1
 */
class DocFragmentJspWriter extends ResponseWriter {

    String encoding = "ISO-8859-1";

    private FacesContainer container;
    private Document doc;
    private DocumentFragment frag;

    // <markup_separation> moved to designer/markup
//    public interface ParsingDocument {
//        /** Given a string of xhtml, parse it and append it to the given
//         * parent node
//         * @param parent The parent node; cannot be null
//         * @param xhtml An xhtml fragment string; should be well formed but
//         *   may not be a complete xhtml document (e.g. no <body> tag;
//         *   may not be surrounded by an element, shouldn't have a DOCTYPE,
//         *   etc.)
//         * @param bean The bean for which this markup is generated
//         */
//        public void appendParsedString(Node parent, String xhtml, MarkupDesignBean bean);
//    }
    // </markup_separation>

    private Node current;

    /**
     * Flag which indicates that we have an open element tag
     */
    private boolean buildingStart;

    /** True when we shouldn't be escaping output (basically,
     * inside of <script> and <style> elements).
     */
    private boolean dontEscape;

    private char[] charHolder = new char[1];

    //--------------------------------------------------------------------------------- Construction

    /**
     * Construct the DocFragmentJspWriter
     */
    public DocFragmentJspWriter(FacesContainer container, DocumentFragment frag) {
        this.container = container;
        this.frag = frag;
        this.doc = frag.getOwnerDocument();
        current = frag;
        depth = 0;
        skipDepth = -1;
    }

    /**
     * Import a node into the written dom tree. If deep is true just clone it in and continue at the
     * same level. If close is false, then import one node & descend.
     * @param node Node to be copied
     * @param deep If true, copy recursively, e.g. include all children of node as well
     * @return The imported node
     */
    public Node importNode(Node node, boolean deep) {
        if (skipDepth != -1) {
            return null;
        }

        // Remove script tags (to lessen work that has to be done
        // by the designer), remove f:subview and other "metatags" like
        // that
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            String tag = ((Element)node).getLocalName();
            if (tag.charAt(0) == 's') {
                if (tag.equals("subview")) {
                    return null;
                    //} else if (tag.equals("script")) {
                    //    depth++;
                    //    skipDepth = depth;
                    //
                    //    return null;
                } // else TODO - yank f:verbatim too (but keep its children)
            }
        } // else: note that I cannot remove comments because they often are
        // interpreted by the browser: for example, a <style> element may
        // contain a comment and browsers know to look inside the comment
        // for the actual CSS styles
        
        Node newnode = doc.importNode(node, deep);
        current.appendChild(newnode);
        if (!deep) {
            current = newnode;
            depth++;
        }
        return newnode;
    }
    
    public Node appendTextNode(String text) {
        if (skipDepth != -1) {
            return null;
        }
        Node newnode = doc.createTextNode(text);
        current.appendChild(newnode);
        return newnode;
    }
    
    /**
     * Pop up a level after doing a non-deep import
     */
    public void popNode() {
        current = current.getParentNode();
    }
    
    //------------------------------------------------------------------------------- ResponseWriter
    
    /**
     * @return the content type, such as "text/html" for this
     * ResponseWriter.
     *
     */
    public String getContentType() {
        return "text/html";
    }
    
    /**
     * @return the character encoding, such as "ISO-8859-1" for this
     * ResponseWriter.  Please see <a
     * href="http://www.iana.org/assignments/character-sets">the
     * IANA</a> for a list of character encodings.
     *
     */
    public String getCharacterEncoding() {
        return encoding;
    }
    
    /**
     * <p>Write whatever text should begin a response.</p>
     *
     * @exception java.io.IOException if an input/output error occurs
     */
    public void startDocument() throws IOException {
    }
    
    /**
     * <p>Write whatever text should end a response.  If there is an open
     * element that has been created by a call to <code>startElement()</code>,
     * that element will be closed first.</p>
     *
     * @exception java.io.IOException if an input/output error occurs
     */
    public void endDocument() throws IOException {
        flush();
    }
    
    /**
     * Flush the stream.  If the stream has saved any characters from the
     * various write() methods in a buffer, write them immediately to their
     * intended destination.  Then, if that destination is another character or
     * byte stream, flush it.  Thus one flush() invocation will flush all the
     * buffers in a chain of Writers and OutputStreams.
     *
     * @exception  java.io.IOException  If an I/O error occurs
     */
    public void flush() throws IOException {
        closeStartIfNecessary();
    }
    
    /**
     * <p>Write the start of an element, up to and including the
     * element name.  Once this method has been called, clients can
     * call <code>writeAttribute()</code> or <code>writeURIAttribute()</code>
     * method to add attributes and corresponding values.  The starting
     * element will be closed (that is, the trailing '>' character added)
     * on any subsequent call to <code>startElement()</code>,
     * <code>writeComment()</code>,
     * <code>writeText()</code>, <code>endElement()</code>, or
     * <code>endDocument()</code>.</p>
     *
     * @param name Name of the element to be started
     *
     * @param componentForElement May be <code>null</code>.  If
     * non-<code>null</code>, must be the UIComponent instance to which
     * this element corresponds.
     *
     * @exception IOException if an input/output error occurs
     * @exception NullPointerException if <code>name</code>
     *  is <code>null</code>
     */
    public void startElement(String name, UIComponent componentForElement) throws IOException {
        if (componentForElement == null) {
            boolean assertionsEnabled = false;
            assert assertionsEnabled = true;
            if (assertionsEnabled) {
                Throwable t = new Throwable();
                t.fillInStackTrace();
                StackTraceElement stack[] = t.getStackTrace();
                StackTraceElement caller = stack[1];
                String className = caller.getClassName();
                className = className.substring(className.lastIndexOf('.')+1);
                String methodName = caller.getMethodName();
                if (!methodName.equals("renderHiddenField")) { // Known exception
                    System.err.println("Warning: tag <" + name + "> rendered with null component parameter! Caller: " + className + "." + methodName + "():" + caller.getLineNumber());
                }
            }
        }
        if (skipDepth != -1) {
            depth++;
            return;
        }
        if (componentForElement != null && componentForElement == preRendered) {
            assert preRenderedFragment != null;
            Node n = importNode(preRenderedFragment, true);
            //XhtmlText.markJspxSource(n);
            //XhtmlElement.setStyleParent(ec, elem);
            depth++;
            skipDepth = depth;
            preRenderedFragment.getChildNodes().getLength();
            return;
        }
        
        closeStartIfNecessary();
        
        // If I ever support HTML instead of XHTML gotta do case insensitive searching
        // here
        // if ((firstChar == 's' || firstChar == 'S') &&
        //        (name.equalsIgnoreCase("script") || name.equalsIgnoreCase("style"))) {
        //    dontEscape = true;
        //}
        char firstChar = name.charAt(0);
        if (firstChar == 's') {
            if (name.equals("script")) {
                //depth++;
                //skipDepth = depth;
                //return;
                dontEscape = true;
            } else if (name.equals("style")) {
                dontEscape = true;
            }
        }
        
        Element e = doc.createElement(name);
        current.appendChild(e);
        current = e;
        depth++;
        
        if (componentForElement != null) {
            DesignContext ctx = container.getFacesContext().getDesignContext();
            DesignBean bean = ctx.getBeanForInstance(componentForElement);
            if (bean == null) {
                // If there is no design bean for this component, it is most likely a component
                // created by the renderer itself. Search up the component tree for a suitable
                // bean.
                UIComponent ancestor = componentForElement.getParent();
                bean = ctx.getBeanForInstance(ancestor);
                while (bean == null && ancestor != null) {
                    ancestor = ancestor.getParent();
                    bean = ctx.getBeanForInstance(ancestor);
                }
            }
            if ((current instanceof Element) && (bean instanceof MarkupDesignBean)) {
//                InSyncService.getProvider().setMarkupDesignBeanForElement((Element)current, (MarkupDesignBean)bean);
                MarkupUnit.setMarkupDesignBeanForElement((Element)current, (MarkupDesignBean)bean);
            }
        }
        buildingStart = true;
    }
    
    /**
     * This method automatically closes a previous element (if not
     * already closed).
     */
    private void closeStartIfNecessary() throws IOException {
        if (buildingStart) // XXX add a skip check here too?
            buildingStart = false;
    }
    
    /**
     * <p>Write the end of an element, after closing any open element
     * created by a call to <code>startElement()</code>.
     *
     * @param name Name of the element to be ended
     *
     * @exception java.io.IOException if an input/output error occurs
     * @exception java.lang.NullPointerException if <code>name</code>
     *  is <code>null</code>
     */
    public void endElement(String name) throws IOException {
        if (skipDepth != -1) {
            if (depth == skipDepth) {
                depth--;
                skipDepth = -1;
                return;
            } else if (depth > skipDepth) {
                depth--;
                return;
            }
        }
        // always turn escaping back on once an element ends
        dontEscape = false;
        
        if (current instanceof Element) {
            boolean assertionsEnabled = false;
            assert assertionsEnabled = true;
            if (assertionsEnabled) {
                if (!current.getLocalName().equals(name)) {
                    String instanceName =
                            MarkupUnit.getMarkupDesignBeanForElement((Element) current).getInstanceName();
                    System.err.println("Renderer for " + instanceName + " attempting to close markup element '"
                            + name + "', closing '" + current.getLocalName() + "' instead");
                }
            }
            current = current.getParentNode();
            depth--;
        }
        
        if (buildingStart)
            buildingStart = false;
    }
    
    /** Ensure that we're done with the given node. Called to "rollback"
     * in case a child has aborted during render. */
    public void setCurrent(Node current, int depth) {
        this.current = current;
        this.depth = depth;
        buildingStart = false;
    }
    
    /** Return the current target node being rendered to by the jsp writer */
    public Node getCurrent() {
        return current;
    }
    
    /** Return the depth of the current target node being rendered by the
     * jsp writer. The document fragment starts out at depth 0. */
    public int getDepth() {
        return depth;
    }
    
    /**
     * <p>Write an attribute name and corresponding value (after converting
     * that text to a String if necessary), after escaping it properly.
     * This method may only be called after a call to
     * <code>startElement()</code>, and before the opened element has been
     * closed.</p>
     *
     * @param name Attribute name to be added
     *
     * @param value Attribute value to be added
     *
     * @param componentPropertyName May be <code>null</code>.  If
     * non-<code>null</code>, this must be the name of the property on
     * the {@link UIComponent} passed in to a previous call to {@link
     * #startElement} to which this attribute corresponds.
     *
     * @exception IllegalStateException if this method is called when there
     *  is no currently open element
     * @exception IOException if an input/output error occurs
     * @exception NullPointerException if <code>name</code> is
     * <code>null</code>
     */
    public void writeAttribute(String name, Object value, String componentPropertyName)
    throws IOException {
        if (skipDepth != -1) {
            return;
        }
        if (value == null) {
            ErrorManager.getDefault().log("ResponseWriter: writeAttribute " + name + " called with null value!");
            return;
        }
        
        name = name.trim(); //work around bug 5017976
        // assert current instanceof Element
        if (current instanceof Element)
            ((Element)current).setAttribute(name, value.toString());
    }
    
    /**
     * <p>Write a URI attribute name and corresponding value (after converting
     * that text to a String if necessary), after encoding it properly
     * (for example, '%' encoded for HTML).
     * This method may only be called after a call to
     * <code>startElement()</code>, and before the opened element has been
     * closed.</p>
     *
     * @param name Attribute name to be added
     *
     * @param value Attribute value to be added
     *
     * @param componentPropertyName May be <code>null</code>.  If
     * non-<code>null</code>, this must be the name of the property on
     * the {@link UIComponent} passed in to a previous call to {@link
     * #startElement} to which this attribute corresponds.
     *
     * @exception IllegalStateException if this method is called when there
     *  is no currently open element
     * @exception IOException if an input/output error occurs
     * @exception NullPointerException if <code>name</code> is
     * <code>null</code>
     */
    public void writeURIAttribute(String name, Object value, String componentPropertyName)
    throws IOException {
        if (skipDepth != -1) {
            return;
        }
        // assert current instanceof Element
        if (current instanceof Element)
            ((Element)current).setAttribute(name, value.toString());
    }
    
    /**
     * <p>Write a comment containing the specified text, after converting
     * that text to a String if necessary.  If there is an open element
     * that has been created by a call to <code>startElement()</code>,
     * that element will be closed first.</p>
     *
     * @param comment Text content of the comment
     *
     * @exception java.io.IOException if an input/output error occurs
     * @exception java.lang.NullPointerException if <code>comment</code>
     *  is <code>null</code>
     */
    public void writeComment(Object comment) throws IOException {
        if (skipDepth != -1) {
            return;
        }
        closeStartIfNecessary();
        current.appendChild(doc.createComment(comment.toString()));
    }
    
    /**
     * <p>Write an object (after converting it to a String, if necessary),
     * after escaping it properly.  If there is an open element
     * that has been created by a call to <code>startElement()</code>,
     * that element will be closed first.</p>
     *
     * <p>All angle bracket occurrences in the argument must be escaped
     * using the &amp;gt; &amp;lt; syntax.</p>
     *
     * @param text Text to be written
     *
     * @param componentPropertyName May be <code>null</code>.  If
     * non-<code>null</code>, this is the name of the property in the
     * associated component to which this piece of text applies.
     *
     * @exception IOException if an input/output error occurs
     * @exception NullPointerException if <code>text</code>
     *  is <code>null</code>
     */
    public void writeText(Object text, String componentPropertyName) throws IOException {
        if (skipDepth != -1) {
            return;
        }
        closeStartIfNecessary();
        if (text == null) {
            return;
        }
        String s = text.toString();
        /* This was necessary when we were rendering to JSPX. We now render to HTML.
        int n = s.length();
        StringBuffer sb = new StringBuffer(2*n);
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '"': sb.append("&quot;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                // apos missing, see BrowserPreview code
                default:  sb.append(c);
            }
        }
        current.appendChild(doc.createTextNode(sb.toString()));
         */
        current.appendChild(doc.createTextNode(s));
    }
    
    /**
     * <p>Write a single character, after escaping it properly.  If there
     * is an open element that has been created by a call to
     * <code>startElement()</code>, that element will be closed first.</p>
     *
     * @param text Text to be written
     *
     * @exception java.io.IOException if an input/output error occurs
     */
    public void writeText(char text) throws IOException {
        if (skipDepth != -1) {
            return;
        }
        charHolder[0] = text;
        writeText(charHolder, null);
    }
    
    /**
     * <p>Write text from a character array, after escaping it properly
     * for this method.  If there is an open element that has been
     * created by a call to <code>startElement()</code>, that element
     * will be closed first.</p>
     *
     * @param text Text to be written
     * @param off Starting offset (zero-relative)
     * @param len Number of characters to be written
     *
     * @exception java.lang.IndexOutOfBoundsException if the calculated starting or
     *  ending position is outside the bounds of the character array
     * @exception java.io.IOException if an input/output error occurs
     * @exception java.lang.NullPointerException if <code>text</code>
     *  is <code>null</code>
     */
    public void writeText(char[] text, int off, int len) throws IOException {
        if (skipDepth != -1) {
            return;
        }
        closeStartIfNecessary();
        current.appendChild(doc.createTextNode(new String(text, off, len)));
    }
    
    /**
     * Creates a new instance of this ResponseWriter, using a different Writer.
     */
    public ResponseWriter cloneWithWriter(Writer writer) {
        // How do we handle this? We need the writer to be resettable!
        throw new RuntimeException("cloneWithWriter not supported by the Creator container!");
        //return new PrettyJspWriter(writer);
    }
    
    //--------------------------------------------------------------------------------------- Writer
    
    /**
     * Close the stream, flushing it first. Once a stream has been closed, further write() or
     * flush() invocations will cause an IOException to be thrown. Closing a previously-closed
     * stream, however, has no effect.
     *
     * @exception java.io.IOException If an I/O error occurs
     */
    public void close() throws IOException {
        if (skipDepth != -1) {
            return;
        }
        closeStartIfNecessary();
    }
    
    public void write(char cbuf) throws IOException {
        charHolder[0] = cbuf;
        write(new String(charHolder));
    }
    
    public void write(char[] cbuf, int off, int len) throws IOException {
        write(new String(cbuf, off, len));
    }
    
    public void write(int c) throws IOException {
        write((char)c);
    }
    
    public void write(String str) throws IOException {
        if (skipDepth != -1 || str == null) {
            return;
        }
        closeStartIfNecessary();
        if (str.indexOf('<') != -1) {
            // The string contains unescaped markup! We've gotta parse the string instead
            MarkupDesignBean bean = null;
//            if (current instanceof RaveElement) {
//                bean = ((RaveElement)current).getDesignBean();
//            }
            if (current instanceof Element) {
//                bean = InSyncService.getProvider().getMarkupDesignBeanForElement((Element)current);
                bean = MarkupUnit.getMarkupDesignBeanForElement((Element)current);
            }
//            ((ParsingDocument)doc).appendParsedString(current, str, bean);
//            InSyncService.getProvider().appendParsedString(doc, current, str, bean);
            MarkupUnit unit = MarkupUnit.getMarkupUnitForDocument(doc);
            if (unit != null) {
                unit.appendParsedString(current, str, bean);
            }
        } else if (str.indexOf('&') != -1) { // contains entities
            // <markup_separation>
//            String expanded = MarkupServiceProvider.getDefault().expandHtmlEntities(str);
            // ====
            String expanded = Entities.expandHtmlEntities(str);
            // </markup_separation>
            current.appendChild(doc.createTextNode(expanded));
        } else {
            current.appendChild(doc.createTextNode(str));
        }
    }
    
    public void write(String str, int off, int len) throws IOException {
        write(str.substring(off, len));
    }
    
    /** Return the DocumentFragment being constructed by the writer */
    public DocumentFragment getFragment() {
        return frag;
    }
    
    /**
     * Set the "pre rendered" DocumentFragment for a particular bean.
     * Note: Only ONE bean can be pre-rendered at a time; this is not
     * a per-bean assignment. When set, this will cause the given
     * DocumentFragment to be inserted into the output fragment
     * rather than calling the bean's renderer.
     *
     * This is intended to be used for for example having the ability
     * to "inline edit" a particular component's value; in that case
     * since we're not updating the value attribute during editing,
     * we want to suppress the normal rendered portion from the component
     * and instead substitute the inline-edited document fragment
     * corresponding to the parsed text output of the component.
     */
    public void setPreRendered(UIComponent bean, DocumentFragment df) {
        preRendered = bean;
        preRenderedFragment = df;
    }
    private UIComponent preRendered;
    private DocumentFragment preRenderedFragment;
    private int depth;
    private int skipDepth;
}
