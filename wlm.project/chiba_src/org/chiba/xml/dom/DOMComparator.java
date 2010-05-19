// Copyright 2006 Chibacon
/*
 *
 *    Artistic License
 *
 *    Preamble
 *
 *    The intent of this document is to state the conditions under which a Package may be copied, such that
 *    the Copyright Holder maintains some semblance of artistic control over the development of the
 *    package, while giving the users of the package the right to use and distribute the Package in a
 *    more-or-less customary fashion, plus the right to make reasonable modifications.
 *
 *    Definitions:
 *
 *    "Package" refers to the collection of files distributed by the Copyright Holder, and derivatives
 *    of that collection of files created through textual modification.
 *
 *    "Standard Version" refers to such a Package if it has not been modified, or has been modified
 *    in accordance with the wishes of the Copyright Holder.
 *
 *    "Copyright Holder" is whoever is named in the copyright or copyrights for the package.
 *
 *    "You" is you, if you're thinking about copying or distributing this Package.
 *
 *    "Reasonable copying fee" is whatever you can justify on the basis of media cost, duplication
 *    charges, time of people involved, and so on. (You will not be required to justify it to the
 *    Copyright Holder, but only to the computing community at large as a market that must bear the
 *    fee.)
 *
 *    "Freely Available" means that no fee is charged for the item itself, though there may be fees
 *    involved in handling the item. It also means that recipients of the item may redistribute it under
 *    the same conditions they received it.
 *
 *    1. You may make and give away verbatim copies of the source form of the Standard Version of this
 *    Package without restriction, provided that you duplicate all of the original copyright notices and
 *    associated disclaimers.
 *
 *    2. You may apply bug fixes, portability fixes and other modifications derived from the Public Domain
 *    or from the Copyright Holder. A Package modified in such a way shall still be considered the
 *    Standard Version.
 *
 *    3. You may otherwise modify your copy of this Package in any way, provided that you insert a
 *    prominent notice in each changed file stating how and when you changed that file, and provided that
 *    you do at least ONE of the following:
 *
 *        a) place your modifications in the Public Domain or otherwise make them Freely
 *        Available, such as by posting said modifications to Usenet or an equivalent medium, or
 *        placing the modifications on a major archive site such as ftp.uu.net, or by allowing the
 *        Copyright Holder to include your modifications in the Standard Version of the Package.
 *
 *        b) use the modified Package only within your corporation or organization.
 *
 *        c) rename any non-standard executables so the names do not conflict with standard
 *        executables, which must also be provided, and provide a separate manual page for each
 *        non-standard executable that clearly documents how it differs from the Standard
 *        Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    4. You may distribute the programs of this Package in object code or executable form, provided that
 *    you do at least ONE of the following:
 *
 *        a) distribute a Standard Version of the executables and library files, together with
 *        instructions (in the manual page or equivalent) on where to get the Standard Version.
 *
 *        b) accompany the distribution with the machine-readable source of the Package with
 *        your modifications.
 *
 *        c) accompany any non-standard executables with their corresponding Standard Version
 *        executables, giving the non-standard executables non-standard names, and clearly
 *        documenting the differences in manual pages (or equivalent), together with instructions
 *        on where to get the Standard Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    5. You may charge a reasonable copying fee for any distribution of this Package. You may charge
 *    any fee you choose for support of this Package. You may not charge a fee for this Package itself.
 *    However, you may distribute this Package in aggregate with other (possibly commercial) programs as
 *    part of a larger (possibly commercial) software distribution provided that you do not advertise this
 *    Package as a product of your own.
 *
 *    6. The scripts and library files supplied as input to or produced as output from the programs of this
 *    Package do not automatically fall under the copyright of this Package, but belong to whomever
 *    generated them, and may be sold commercially, and may be aggregated with this Package.
 *
 *    7. C or perl subroutines supplied by you and linked into this Package shall not be considered part of
 *    this Package.
 *
 *    8. The name of the Copyright Holder may not be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 *    9. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED
 *    WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 *    MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package org.chiba.xml.dom;

import org.chiba.xml.ns.NamespaceConstants;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The DOM Comparator provides a set of comparison methods to check wether
 * two arbitrary DOM nodes may be considered equal. This may be helpful in
 * testing as well as in editing environments.
 * <P>
 * The comparison behaviour is controllable according to whitespace and
 * namespaces. By default, the comparator ignores any whitespace outside
 * element content and is aware of namespaces.
 * <P>
 * <EM>NOTE:</EM> It is strongly recommended to normalize the nodes to be
 * compared <I>before</I> comparison by calling the <CODE>Node.normalize()</CODE>.
 * Since that method may affect changes in the DOM tree, the comparator won't
 * do this to be non-intrusive.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id$
 */
public class DOMComparator {

    /**
     * Feature <CODE>ignore-comments</CODE>.
     */
    private boolean ignoreComments = false;

    /**
     * Feature <CODE>ignore-namespace-declarations</CODE>.
     */
    private boolean ignoreNamespaceDeclarations = false;

    /**
     * Feature <CODE>ignore-whitespace</CODE>.
     */
    private boolean ignoreWhitespace = true;

    /**
     * Feature <CODE>namespace-aware</CODE>.
     */
    private boolean namespaceAware = true;

    /**
     * The error handler.
     */
    private ErrorHandler errorHandler = new DefaultErrorHandler();

    /**
     * Constructs an empty DOM Comparator.
     */
    public DOMComparator() {
    }

    // Behaviour control.

    /**
     * Sets the state of comment ignoring.
     *
     * @param state the state of comment ignoring.
     */
    public void setIgnoreComments(boolean state) {
        this.ignoreComments = state;
    }

    /**
     * Sets the state of namespace declaration ignoring.
     *
     * @param state the state of namespace declaration ignoring.
     */
    public void setIgnoreNamespaceDeclarations(boolean state) {
        this.ignoreNamespaceDeclarations = state;
    }

    /**
     * Sets the state of whitespace ignoring.
     *
     * @param state the state of whitespace ignoring.
     */
    public void setIgnoreWhitespace(boolean state) {
        this.ignoreWhitespace = state;
    }

    /**
     * Sets the state of namespace awareness.
     *
     * @param state the state of namespace awareness.
     */
    public void setNamespaceAware(boolean state) {
        this.namespaceAware = state;
    }

    /**
     * Sets the error handler.
     *
     * @param errorHandler the error handler.
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;

		// Ensure error handler
		if (this.errorHandler == null) {
			this.errorHandler = new DefaultErrorHandler();
		}
	}

    // Generic DOM comparison methods.

	/**
     * Compares two nodes for equality.
     * <P>
     * Two nodes are considered to be equal if either both nodes
     * are <CODE>null</CODE> or
     * <UL>
     * <LI>both nodes are not <CODE>null</CODE>, and
     * <LI>both node's types are equal, and
     * <LI>both node's names are equal, and
     * <LI>both node's values are equal, and
     * <LI>both node's attributes are equal, and
     * <LI>both node's children are equal.
     * </UL>
     *
     * @param left  one node.
     * @param right another node.
     * @return <CODE>true</CODE> if both nodes are considered to be
     *         equal, otherwise <CODE>false</CODE>.
     */
    public boolean compare(Node left, Node right) {
        // Compare object references.
        if (left == right) {
            return true;
        }

        // Compare object references.
        if ((left == null) || (right == null)) {
            this.errorHandler.handleError("a node is null", null, left, right);
            return false;
        }

        // Compare node types.
        if (left.getNodeType() != right.getNodeType()) {
            this.errorHandler.handleError("different node types", left.getNodeType() + " vs. " + right.getNodeType(), left, right);
            return false;
        }

        if (this.namespaceAware &&
                ((left.getNodeType() == Node.ATTRIBUTE_NODE) || (left.getNodeType() == Node.ELEMENT_NODE))) {
            // Compare node URIs.
            if (!compare(left.getNamespaceURI(), right.getNamespaceURI())) {
                this.errorHandler.handleError("different namespaces", left.getNamespaceURI() + " vs. " + right.getNamespaceURI(), left, right);
                return false;
            }

            // It is insufficient to compare the local parts only, since
            // parsers handle this issue differently: While no URI is
            // given, a parser may choose to return no local part ...
            if (left.getNamespaceURI() != null) {
                // Compare node locals.
                if (!compare(left.getLocalName(), right.getLocalName())) {
                    this.errorHandler.handleError("different local names", left.getLocalName() + " vs. " + right.getLocalName(), left, right);
                    return false;
                }
            } else {
                // Compare node names.
                if (!compare(left.getNodeName(), right.getNodeName())) {
                    this.errorHandler.handleError("different node names", left.getNodeName() + " vs. " + right.getNodeName(), left, right);
                    return false;
                }
            }
        } else {
            // Compare node names.
            if (!compare(left.getNodeName(), right.getNodeName())) {
                this.errorHandler.handleError("different node names", left.getNodeName() + " vs. " + right.getNodeName(), left, right);
                return false;
            }
        }

        // Compare node values.
        if (!compare(left.getNodeValue(), right.getNodeValue())) {
            this.errorHandler.handleError("different node values", left.getNodeValue() + " vs. " + right.getNodeValue(), left, right);
            return false;
        }

        // Compare node attributes.
        if (!compare(left.getAttributes(), right.getAttributes())) {
            this.errorHandler.handleError("different node attributes", null, left, right);
            return false;
        }

        // Compare node children.
        if (!compare(left.getChildNodes(), right.getChildNodes())) {
            this.errorHandler.handleError("different node children", null, left, right);
            return false;
        }

        // All checks passed successfully.
        return true;
    }

    // Helper and concenience methods.

    /**
     * Compares two node lists for equality.
     * <P>
     * Two node lists are considered to be equal if either both node lists
     * are <CODE>null</CODE> or
     * <UL>
     * <LI>both node lists are not <CODE>null</CODE>, and
     * <LI>both node list's lengths are equal, and
     * <LI>both node list's items are equal, and
     * <LI>both node list's items appear in the same order.
     * </UL>
     *
     * @param left  one node list.
     * @param right another node list.
     * @return <CODE>true</CODE> if both node lists are considered to be
     *         equal, otherwise <CODE>false</CODE>.
     */
    protected boolean compare(NodeList left, NodeList right) {
        // Compare object references.
        if (left == right) {
            return true;
        }

        // Compare object references.
        if ((left == null) || (right == null)) {
            return false;
        }

        // Compare list lengths.
        if ((!this.ignoreComments) && (!this.ignoreWhitespace) && (left.getLength() != right.getLength())) {
            return false;
        }

        // Get next list items.
        int leftIndex = getNextIndex(left, 0);
        int rightIndex = getNextIndex(right, 0);

        while ((leftIndex < left.getLength()) || (rightIndex < right.getLength())) {
            // Compare list items.
            if (!compare(left.item(leftIndex), right.item(rightIndex))) {
                return false;
            }

            // Get next list items.
            leftIndex = getNextIndex(left, leftIndex + 1);
            rightIndex = getNextIndex(right, rightIndex + 1);
        }

        // All checks passed successfully.
        return true;
    }

    /**
     * Compares two node maps for equality.
     * <P>
     * Two node lists are considered to be equal if either both node maps
     * are <CODE>null</CODE> or
     * <UL>
     * <LI>both node maps are not <CODE>null</CODE>, and
     * <LI>both node map's lengths are equal, and
     * <LI>both node map's items are equal with no respect to the order
     * of their appearance.
     * </UL>
     *
     * @param left  one node map.
     * @param right another node map.
     * @return <CODE>true</CODE> if both node maps are considered to be
     *         equal, otherwise <CODE>false</CODE>.
     */
    protected boolean compare(NamedNodeMap left, NamedNodeMap right) {
        // Compare object references.
        if (left == right) {
            return true;
        }

        // Compare object references.
        if ((left == null) || (right == null)) {
            return false;
        }

        // Compare map lengths.
        if (getRealLength(left) != getRealLength(right)) {
            return false;
        }

        if (this.namespaceAware) {
            // Get next map item.
            int index = getNextIndex(left, 0);

            // Initialize empty item.
            Node item = null;

            while (index < left.getLength()) {
                // Get item.
                item = left.item(index);

                // It is insufficient to compare the local parts only, since
                // parsers handle this issue differently: While no URI is
                // given, a parser may choose to return no local part ...
                if (item.getNamespaceURI() != null) {
                    // Compare map items with no respect to the order of their appearance.
                    if (!compare(item, right.getNamedItemNS(item.getNamespaceURI(), item.getLocalName()))) {
                        return false;
                    }
                } else {
                    // Compare map items with no respect to the order of their appearance.
                    if (!compare(item, right.getNamedItem(item.getNodeName()))) {
                        return false;
                    }
                }

                // Get next map item.
                index = getNextIndex(left, index + 1);
            }

            // All checks passed successfully.
            return true;
        }

        for (int index = 0; index < left.getLength(); index++) {
            // Compare map items with no respect to the order of their appearance.
            if (!compare(left.item(index), right.getNamedItem(left.item(index).getNodeName()))) {
                return false;
            }
        }

        // All checks passed successfully.
        return true;
    }

    /**
     * Compares two strings for equality.
     * <P>
     * Two strings are considered to be equal if either both strings
     * are <CODE>null</CODE> or both strings are not <CODE>null</CODE>,
     * and depending on the specified comparison method
     * <UL>
     * <LI>both string's object references of the VM internal string pool
     * are equal, or
     * <LI>both string's hash codes are equal, or
     * <LI>both strings are character-wise equal.
     * </UL>
     *
     * @param left  one string.
     * @param right another string.
     * @return <CODE>true</CODE> if both strings are considered to be
     *         equal, otherwise <CODE>false</CODE>.
     */
    protected final boolean compare(String left, String right) {
        // Compare object references.
        if (left == right) {
            return true;
        }

        // Compare object references.
        if ((left == null) || (right == null)) {
            return false;
        }

        // Compare character by character.
        return left.equals(right);
    }

    /**
     * Returns the index of the next item in the node list to compare.
     * <P>
     * The item choosen depends on the comment and whitespace ignoring.
     *
     * @param list  the node list.
     * @param start the start index.
     * @return the index of the next item in the node list to compare.
     */
    private int getNextIndex(NodeList list, int start) {
        if (this.ignoreComments && this.ignoreWhitespace) {
            // Skip whitespace and comment list items.
            return DOMWhitespace.skipWhitespaceAndComments(list, start);
        }

        if (this.ignoreWhitespace) {
            // Skip whitespace list items.
            return DOMWhitespace.skipWhitespace(list, start);
        }

        if (this.ignoreComments) {
            // Skip comment list items.
            return DOMWhitespace.skipComments(list, start);
        }

        return start;
    }

    /**
     * Returns the index of the next item in the node map to compare.
     * <P>
     * The item choosen depends on the namespace awareness and
     * namespace declaration ignoring.
     *
     * @param map   the node map.
     * @param start the start index.
     * @return the index of the next item in the node map to compare.
     */
    private int getNextIndex(NamedNodeMap map, int start) {
        if (this.namespaceAware && this.ignoreNamespaceDeclarations) {
            for (int index = start; index < map.getLength(); index++) {
                if (!NamespaceConstants.XMLNS_NS.equals(map.item(index).getNamespaceURI())) {
                    // Deliver index.
                    return index;
                }
            }

            // Return original length.
            return map.getLength();
        }

        return start;
    }

    /**
     * Returns the real length of the map to compare.
     * <P>
     * The returned length depends on the namespace awareness and
     * namespace declaration ignoring.
     *
     * @param map the node map.
     * @return the real length of the map to compare.
     */
    private int getRealLength(NamedNodeMap map) {
        if (this.namespaceAware && this.ignoreNamespaceDeclarations) {
            // Initialize difference.
            int difference = 0;

            for (int index = 0; index < map.getLength(); index++) {
                if (NamespaceConstants.XMLNS_NS.equals(map.item(index).getNamespaceURI())) {
                    // Increment difference.
                    difference++;
                }
            }

            // Return computed length.
            return map.getLength() - difference;
        }

        // Return original length.
        return map.getLength();
    }

    // Static error handler interface

    public static interface ErrorHandler {
    	void handleError(String error, String detail, Node left, Node right);
    }

    public static class DefaultErrorHandler implements ErrorHandler {
        public void handleError(String error, String detail, Node left, Node right) {
            // NOP
        }
    }

    public static class SystemErrorHandler implements ErrorHandler {
        public void handleError(String error, String detail, Node left, Node right) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(error);
            if (detail != null) {
                buffer.append(" [");
                buffer.append(detail);
                buffer.append("]");
            }
            buffer.append(": left='");
            buffer.append(left == null ? "#null" : left.getNodeName());
            buffer.append("' right='");
            buffer.append(right == null ? "#null" : right.getNodeName());
            buffer.append("'");

            System.err.println(buffer);
        }
    }

}
