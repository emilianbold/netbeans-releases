/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.collab;

import java.util.ArrayList;
import org.xml.sax.helpers.DefaultHandler;
import org.netbeans.lib.collab.util.SAX;


/**
 * Helper class for recipients of a poll message.  This class can be used to
 * parse the xml from the poll message and generate a poll response based on
 * user input.
 *
 *
 * @since version 0.1
 *
 */
public class ConferenceEventHelper extends org.xml.sax.helpers.DefaultHandler {

    private ArrayList _tuples = new ArrayList(2);
    private int _state;
    private ConferenceEventTuple _currentTuple;
    private StringBuffer _customMarkup;
    
    private final int IN_ROOT = 1;
    private final int IN_CEVENT = 2;
    private final int IN_SUBJECT = 3;
    private final int IN_NOTE = 4;

    private final String ELEMENT_CEVENT = "cevent";
    private final String ELEMENT_SUBJECT = "subject";
    private final String ELEMENT_NOTE = "note";
    private final String ATTRIBUTE_ACCESSLEVEL = "accesslevel";
    private final String ATTRIBUTE_STATUS = "status";
    private final String ATTRIBUTE_DESTINATION = "destination";
    private final String ATTRIBUTE_ID = "id";
    
    /** 
     * Constructor used to parse a conference event
     * @param in UTF-8 encoded input stream to the conference event
     * message content (XML document)
     */
    public ConferenceEventHelper(java.io.InputStream in) throws Exception {
        SAX.parse(in, this);
    }
    
    /**
     * Constructor used to parse a conference event
     * @param str conference event message content (XML document)
     */
    public ConferenceEventHelper(String str) throws Exception {
        SAX.parse(str, this);
    }

    /** 
     * Constructor used to generate conference event
     */
    public ConferenceEventHelper() {
        _tuples = new ArrayList();
    }

    public void addTuple(ConferenceEventTuple tuple) {
        _tuples.add(tuple);
    }
    
    public void characters(char[] ch, int start, int length) throws org.xml.sax.SAXException {
        switch (_state) {
                           
	case IN_NOTE :
	    _customMarkup.append(ch, start, length);
	    break;
                
	default :
	    throw new org.xml.sax.SAXException("No characters at this stage: ");
                    
        }
    } 
      
    public void startDocument() throws org.xml.sax.SAXException {
        _state = IN_ROOT;
    }
        
    public void endElement(java.lang.String nsuri, java.lang.String localName, java.lang.String fqName) throws org.xml.sax.SAXException {
        switch (_state) {
	case IN_CEVENT :
	    _state = IN_ROOT;
	    break;
                
	case IN_SUBJECT :
	    _state = IN_CEVENT;
	    _tuples.add(_currentTuple);
	    break;
                
	case IN_NOTE :
	    _currentTuple.note = _customMarkup.toString();
	    _state = IN_SUBJECT;
	    break;
                
	default :
	    throw new org.xml.sax.SAXException("Invalid element end at this stage: " + fqName);
        }
    }
    
    public void startElement(java.lang.String nsuri, java.lang.String localName, java.lang.String fqName, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {

        switch (_state) {
        
	case IN_ROOT :
	    if (fqName.equals(ELEMENT_CEVENT)) {
		_state = IN_CEVENT;
	    } else {
		throw new org.xml.sax.SAXException("Invalid element: " + fqName);
	    }
	    break;        
        
	case IN_CEVENT :
	    if (fqName.equals(ELEMENT_SUBJECT)) {
		_state = IN_SUBJECT;
		String status = attributes.getValue(ATTRIBUTE_STATUS);
		String accessLevel = attributes.getValue(ATTRIBUTE_STATUS);
		String destination = attributes.getValue(ATTRIBUTE_DESTINATION);
                String id = attributes.getValue(ATTRIBUTE_ID);
		if (destination == null) {
		    throw new org.xml.sax.SAXException("Missing destination");
		}
		_currentTuple = new ConferenceEventTuple(destination);
		_currentTuple.status = status;
		_currentTuple.accesslevel = accessLevel;
                _currentTuple.id = id;
	    } else {
		throw new org.xml.sax.SAXException("Unrecognized element: " + fqName);               
	    }
	    break;
        
	case IN_SUBJECT :
	    if (fqName.equals(ELEMENT_NOTE)) {
		_state = IN_NOTE;
		_customMarkup = new StringBuffer();
	    } else {
		throw new org.xml.sax.SAXException("Unrecognized element: " + fqName);               
	    }
	    break;

               
	default :
	    throw new org.xml.sax.SAXException("Invalid element at this stage: " + fqName);
            
	}
        
    }
    
    public java.util.Collection getTuples() {
        return _tuples;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("<?xml version='1.0'?>\n<cevent>");
        for (int i = 0 ; i < _tuples.size() ; i++) {
            ConferenceEventTuple ces = (ConferenceEventTuple)_tuples.get(i);
            buf.append("<subject destination='" + ces.destination + "'");
            if(ces.id != null) {
                buf.append(" id='" + ces.id + "'");
            }            
	    if (ces.status != null) {
		 buf.append(" status='" + ces.status + "'");
	    }
	    if (ces.accesslevel != null) {
		 buf.append(" accesslevel='" + ces.accesslevel + "'");
	    }
            if (ces.note != null && ces.note.length() > 0) {
                buf.append("><note>" + ces.note + "</note></subject>\n");
            } else {
                buf.append(" />");
            }
        }
        buf.append("</cevent>\n");
        return buf.toString();
    }
        
    public java.io.InputStream getInputStream() throws Exception {   
        byte[] bytes = this.toString().getBytes();
        return new java.io.ByteArrayInputStream(bytes, 0, bytes.length);
    }
    
}
