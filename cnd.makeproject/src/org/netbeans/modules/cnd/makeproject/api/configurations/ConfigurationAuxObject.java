/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;

public interface ConfigurationAuxObject {
    /**
     * Initializes the object to default values
     */
    public void initialize();

    public XMLDecoder getXMLDecoder();
    public XMLEncoder getXMLEncoder();

    /**
     * Returns a unique String id (key) used to retrive this object from the
     * pool of aux objects
     OLD: use getXMLDecoder.tag() for below
     * and for storing the object in xml form and
     * parsing the xml code to restore the object.
     * Debugger should use the id "dbxdebugger", for instance.
     */

    public String getId();

    /**
     * Responsible for saving the object in xml format.
     * It should save the object in the following format using the id string from getId():
     * <id-string>
     *     <...
     *     <...
     * </id-string>
     */
    /* OLD
    public void writeElement(PrintWriter pw, int indent, Object object);
    */

    /**
     * Responsible for parsing the xml code created from above and for restoring the state of
     * the object (but not the object itself).
     * Refer to the Sax parser documentation for details.
     */
    /* OLD
    public void startElement(String namespaceURI, String localName, String element, Attributes atts);
    public void endElement(String uri, String localName, String qName, String currentText);
    */

    /**
     * Returns true if object has changed and needs to be saved.
     */
    public boolean hasChanged();
    public void clearChanged();

    /**
     * Returns true if object should be stored in shared (public) part of configuration
     */
    public boolean shared();

    /**
     * Assign all values from a profileAuxObject to this object (reverse of clone)
     */
    public void assign(ConfigurationAuxObject profileAuxObject);

    /**
     * Clone itself to an identical (deep) copy.
     */
    public Object clone();
}
