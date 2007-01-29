/*
 * @(#)DOMAttrImpl.java   1.11 2000/08/16
 *
 */

package org.w3c.tidy;

import org.w3c.dom.DOMException;

/**
 *
 * DOMAttrImpl
 *
 * (c) 1998-2000 (W3C) MIT, INRIA, Keio University
 * See Tidy.java for the copyright notice.
 * Derived from <a href="http://www.w3.org/People/Raggett/tidy">
 * HTML Tidy Release 4 Aug 2000</a>
 *
 * @author  Dave Raggett <dsr@w3.org>
 * @author  Andy Quick <ac.quick@sympatico.ca> (translation to Java)
 * @version 1.4, 1999/09/04 DOM Support
 * @version 1.5, 1999/10/23 Tidy Release 27 Sep 1999
 * @version 1.6, 1999/11/01 Tidy Release 22 Oct 1999
 * @version 1.7, 1999/12/06 Tidy Release 30 Nov 1999
 * @version 1.8, 2000/01/22 Tidy Release 13 Jan 2000
 * @version 1.9, 2000/06/03 Tidy Release 30 Apr 2000
 * @version 1.10, 2000/07/22 Tidy Release 8 Jul 2000
 * @version 1.11, 2000/08/16 Tidy Release 4 Aug 2000
 */

public class DOMAttrImpl extends DOMNodeImpl implements org.w3c.dom.Attr {

    protected AttVal avAdaptee;

    protected DOMAttrImpl(AttVal adaptee)
    {
        super(null); // must override all methods of DOMNodeImpl
        this.avAdaptee = adaptee;
    }


    /* --------------------- DOM ---------------------------- */

    public String getNodeValue() throws DOMException
    {
        return getValue();
    }

    public void setNodeValue(String nodeValue) throws DOMException
    {
        setValue(nodeValue);
    }

    public String getNodeName()
    {
        return getName();
    }

    public short getNodeType()
    {
        return org.w3c.dom.Node.ATTRIBUTE_NODE;
    }

    public org.w3c.dom.Node getParentNode()
    {
        return null;
    }

    public org.w3c.dom.NodeList getChildNodes()
    {
        // NOT SUPPORTED
        return null;
    }

    public org.w3c.dom.Node getFirstChild()
    {
        // NOT SUPPORTED
        return null;
    }

    public org.w3c.dom.Node getLastChild()
    {
        // NOT SUPPORTED
        return null;
    }

    public org.w3c.dom.Node getPreviousSibling()
    {
        return null;
    }

    public org.w3c.dom.Node getNextSibling()
    {
        return null;
    }

    public org.w3c.dom.NamedNodeMap getAttributes()
    {
        return null;
    }

    public org.w3c.dom.Document getOwnerDocument()
    {
        return null;
    }

    public org.w3c.dom.Node insertBefore(org.w3c.dom.Node newChild, 
                                         org.w3c.dom.Node refChild)
                                             throws DOMException
    {
        throw new DOMExceptionImpl(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   "Not supported");
    }

    public org.w3c.dom.Node replaceChild(org.w3c.dom.Node newChild, 
                                         org.w3c.dom.Node oldChild)
                                             throws DOMException
    {
        throw new DOMExceptionImpl(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   "Not supported");
    }

    public org.w3c.dom.Node removeChild(org.w3c.dom.Node oldChild)
                                            throws DOMException
    {
        throw new DOMExceptionImpl(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   "Not supported");
    }

    public org.w3c.dom.Node appendChild(org.w3c.dom.Node newChild)
                                            throws DOMException
    {
        throw new DOMExceptionImpl(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   "Not supported");
    }

    public boolean hasChildNodes()
    {
        return false;
    }

    public org.w3c.dom.Node cloneNode(boolean deep)
    {
        return null;
    }

    /**
     * @see org.w3c.dom.Attr#getName
     */
    public String getName()
    {
        return avAdaptee.attribute;
    }

    /**
     * @see org.w3c.dom.Attr#getSpecified
     */
    public boolean getSpecified()
    {
        return true;
    }

    /**
     * Returns value of this attribute.  If this attribute has a null value,
     * then the attribute name is returned instead.
     * Thanks to Brett Knights <brett@knightsofthenet.com> for this fix.
     * @see org.w3c.dom.Attr#getValue
     * 
     */
    public String getValue()
    {
        return (avAdaptee.value == null) ? avAdaptee.attribute : avAdaptee.value ;
    }

    /**
     * @see org.w3c.dom.Attr#setValue
     */
    public void setValue(String value)
    {
        avAdaptee.value = value;
    }

    /**
     * DOM2 - not implemented.
     */
    public org.w3c.dom.Element getOwnerElement() {
	return null;
    }

}
