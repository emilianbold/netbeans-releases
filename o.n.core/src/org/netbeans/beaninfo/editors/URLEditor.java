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

package org.netbeans.beaninfo.editors;

import java.beans.PropertyEditorSupport;
import java.net.URL;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import org.netbeans.core.UIExceptions;
import org.openide.util.NbBundle;

/** A property editor for java.net.URL class.
*
* @author   Ian Formanek
*/
public class URLEditor extends PropertyEditorSupport implements org.openide.explorer.propertysheet.editors.XMLPropertyEditor  {

    /** sets new value */
    public void setAsText(String s) {
        if ("null".equals(s)) { // NOI18N
            setValue(null);
            return;
        }

        try {
            URL url = new URL (s);
            setValue(url);
        } catch (MalformedURLException e) {
            IllegalArgumentException iae = new IllegalArgumentException (e.getMessage());
            String msg = MessageFormat.format(
                NbBundle.getMessage(
                    URLEditor.class, "FMT_EXC_BAD_URL"), new Object[] {s}); //NOI18N
             UIExceptions.annotateUser(iae, e.getMessage(), msg, e,
                                      new java.util.Date());
            throw iae;
        }
    }

    /** @return the current value as String */
    public String getAsText() {
        URL url = (URL)getValue();
        return url != null ? url.toString() : "null"; // NOI18N
    }

    public String getJavaInitializationString () {
        URL url = (URL) getValue ();
        return "new java.net.URL(\""+url.toString ()+"\")"; // NOI18N
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
