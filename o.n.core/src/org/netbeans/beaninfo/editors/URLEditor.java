/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.beans.PropertyEditorSupport;
import java.net.URL;
import java.net.MalformedURLException;

/** A property editor for java.net.URL class.
*
* @author   Ian Formanek
*/
public class URLEditor extends PropertyEditorSupport implements org.openide.explorer.propertysheet.editors.XMLPropertyEditor  {

    /** sets new value */
    public void setAsText(String s) {
        try {
            URL url = new URL (s);
            setValue(url);
        } catch (MalformedURLException e) {
            // cannot change -> ignore
        }
    }

    /** @return the current value as String */
    public String getAsText() {
        URL url = (URL)getValue ();
        return url.toString ();
    }

    public String getJavaInitializationString () {
        URL url = (URL) getValue ();
        return "\""+url.toString ()+"\""; // NOI18N
    }

    public boolean supportsCustomEditor () {
        return false;
    }

    //--------------------------------------------------------------------------
    // XMLPropertyEditor implementation

    public static final String XML_URL = "Url"; // NOI18N

    public static final String ATTR_VALUE = "value"; // NOI18N

    /** Called to load property value from specified XML subtree. If succesfully loaded,
    * the value should be available via the getValue method.
    * An IOException should be thrown when the value cannot be restored from the specified XML element
    * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
    * @exception IOException thrown when the value cannot be restored from the specified XML element
    */
    public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_URL.equals (element.getNodeName ())) {
            throw new java.io.IOException ();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
        try {
            String value = attributes.getNamedItem (ATTR_VALUE).getNodeValue ();
            setAsText (value);
        } catch (Exception e) {
            throw new java.io.IOException ();
        }
    }

    /** Called to store current property value into XML subtree. The property value should be set using the
    * setValue method prior to calling this method.
    * @param doc The XML document to store the XML in - should be used for creating nodes only
    * @return the XML DOM element representing a subtree of XML from which the value should be loaded
    */
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        org.w3c.dom.Element el = doc.createElement (XML_URL);
        el.setAttribute (ATTR_VALUE, getAsText ());
        return el;
    }
}

/*
 * Log
 *  5    Gandalf   1.4         1/13/00  Petr Jiricka    i18n
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         7/19/99  Ian Formanek    XML Serialization
 *  2    Gandalf   1.1         5/8/99   Ian Formanek    Fixed to compile
 *  1    Gandalf   1.0         5/8/99   Ian Formanek    
 * $
 */
