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
 * Software is Sun Microsystems, Inc. Portions Copyright 2005-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.api.xml;

import java.util.HashMap;

import org.xml.sax.Attributes;

/**
 * Receive notification of the content of an XML element named via {@link #tag}.
 * <p>
 * XMLDocReader will call {@link #start} upon encountering an element named
 * via {@link #tag}, and will call {@link #end} when the closing tag is
 * encountered.
 * <p>
 * Any elements encountered between those will cause {@link #startElement}
 * and {@link #endElement} to get called, <i>unless</i> a sub-decoder has
 * been registered using {@link #registerXMLDecoder} in which case 
 * all of this recursively applies to the nested element.
 * <p>
 * For example, if <code>FamilyXMLDecoder</code> has <code>tag()</code>
 * return <code>"family"</code> and calls
 * <pre>registerXMLDecoder(new PersonXMLDecoder(...))</pre>
 * then the following trace of XML elements and corresponding callbacks
 * will occur:
 * <pre>
 &lt;family&gt;					start(null)
    &lt;person firstName="X" lastName="Y"/&gt;	PersonXMLDecoder.start(...)
    &lt;heritage&gt;Algebra&lt;/heritage&gt;		startElement("heritage", ...)
						endElement("heritage", "Algebra");
 &lt;/family&gt;					end();
 * </pre>
 * <p>
 * An XMLDecoder should be extended by a subclass, which would typically also
 * implement {@link XMLEncoder} to create a <b>codec</b>.
 */

public abstract class XMLDecoder {

    abstract protected String tag();

    abstract protected void start(Attributes atts) throws VersionException;

    abstract protected void end();

    abstract protected void startElement(String name, Attributes atts);

    abstract protected void endElement(String name, String currentText);

    protected void registerXMLDecoder(XMLDecoder decoder) {
	tagMap.put(decoder.tag(), decoder);
    }

    protected void deregisterXMLDecoder(XMLDecoder decoder) {
	tagMap.remove(decoder.tag());
    }


    private HashMap/*<String,XMLDecoder>*/ tagMap = new HashMap();
    private XMLDecoder currentDecoder;
    private String currentElement;

    public XMLDecoder() {
    } 

    void _startElement(String name, Attributes atts) throws VersionException {
	if (checkStartRecursion(name, atts))
	    return;
	else
	    startElement(name, atts);
    }

    void _endElement(String name, String currentText) {
	// see if need to terminate the current decoder
	if (checkEndRecursion(name, currentText)) {
	    return;
	} else {
	    // pass on to current decoder
	    endElement(name, currentText);
    }
    }

    private boolean checkStartRecursion(String name, Attributes atts)
	throws VersionException {

	if (currentDecoder != null) {
	    currentDecoder._startElement(name, atts);
	    return true;
	}

	XMLDecoder tentativeDecoder = (XMLDecoder) tagMap.get(name);
	if (tentativeDecoder != null) {
	    /* DEBUG
	    System.out.println("Switching to decoder for " + name);
	    */
	    tentativeDecoder.start(atts);	// throws VersionException
	    // everything went fine, commit to it
	    currentDecoder = tentativeDecoder;
	    currentElement = name;
	    return true;
	}
	return false;
    }

    private boolean checkEndRecursion(String name, String currentText) {
	if (currentDecoder != null) {
	    if (currentDecoder.checkEndRecursion(name, currentText)) {
		return true;
	    } else if (name.equals(currentElement)) {
		// ending this decoder
		currentDecoder.end();
		currentDecoder = null;
		currentElement = null;
		return true;
	    } else {
		currentDecoder.endElement(name, currentText);
	    return true;
	}
	}
	return false;
    }

    private void registerXMLDecoder(String tag, XMLDecoder decoder) {
	tagMap.put(tag, decoder);
    }

    protected void checkVersion(Attributes atts, String what, int maxVersion)
        throws VersionException {

        int version = 0;
        String versionString = atts.getValue("version");        // NOI18N
        if (versionString != null)
            version = new Integer(versionString).intValue();
        if (version > maxVersion) {
            throw new VersionException(what, maxVersion, version);
        }
    }
}

