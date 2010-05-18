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

import org.jabberstudio.jso.*;
import org.jabberstudio.jso.util.*;
import org.jabberstudio.jso.x.xdata.*;
import org.netbeans.lib.collab.util.SAX;

/**
 *
 * @since version 0.1
 *
 */
class PollHelperAnswer {
    StringBuffer text;
    String id;

    PollHelperAnswer(String str) {
        id = str;
        text = new StringBuffer();
    }

    PollHelperAnswer() {
        text = new StringBuffer();
    }
}


/**
 * Helper class for recipients of a poll message.  This class can be used to 
 * parse the xml from the poll message and generate a poll response based on
 * user input.
 *
 */
public class PollHelper extends org.xml.sax.helpers.DefaultHandler {

    private ArrayList _answers = new ArrayList(3);
    private String _customId = null;
    private String _question;
    private String _pollID = null;
    private String _pollType = null;
    private String _access = null;
    private int _state;
    private PollHelperAnswer _currentAnswer;
    
    private static final int IN_ID = 1;
    private static final int IN_POLLTYPE = 2;
    private static final int IN_ACCESS = 3;
    private static final int IN_QUESTION = 4;
    private static final int IN_ANSWER = 5;
    private static final int IN_CUSTOM = 6;

    public static final String ELEMENT_FIELD = "field";
    public static final String ELEMENT_VALUE = "value";
    public static final String ELEMENT_OPTION = "option";

    public static final String ATTRIBUTE_VAR = "var";
    public static final String ATTRIBUTE_LABEL = "label";

    public static final String ATTRIBUTE_ID = "id";
    public static final String ATTRIBUTE_QUESTION = "question";
    public static final String ATTRIBUTE_POLLTYPE = "polltype";
    public static final String ATTRIBUTE_ACCESS = "access";
    public static final String ATTRIBUTE_CUSTOM = "custom";
    
    private JSOImplementation _jso;
    private StreamDataFactory _sdf;
    
    /** 
     * Constructor used by poll message receiver
     * @param in xml content's input stream.  The stream must be UTF-8 encoded.
     */
    public PollHelper(java.io.InputStream in) throws Exception {
        _jso = JSOImplementation.getInstance();
        _sdf = _jso.createDataFactory();
        SAX.parse(in, this);
    }

    /**
     * Constructor used by poll message receiver
     * @param message content of the application/x-iim-poll message
     */
    public PollHelper(String message) throws Exception {
        _jso = JSOImplementation.getInstance();
        _sdf = _jso.createDataFactory();
        SAX.parse(message, this);
    }


    public void characters(char[] ch, int start, int length) throws org.xml.sax.SAXException {
        switch (_state) {

            case IN_ID :
                _pollID = String.copyValueOf(ch, start, length);
                break;
            case IN_POLLTYPE :
                _pollType = String.copyValueOf(ch, start, length);
                break;
            case IN_ANSWER :
               _currentAnswer.id = String.copyValueOf(ch, start, length);
                _answers.add(_currentAnswer);
               break;
            case IN_QUESTION :
               break;
            case IN_ACCESS :
                _access = String.copyValueOf(ch, start, length);
               break;
            default :
               throw new org.xml.sax.SAXException("No characters at this stage: ");
        }
    } 
      
    public void startDocument() throws org.xml.sax.SAXException {
    }
        
    public void endDocument() throws org.xml.sax.SAXException {
    }
        
    public void endElement(java.lang.String nsuri, java.lang.String localName, java.lang.String fqName) throws org.xml.sax.SAXException {
    }
    
    public void startElement(java.lang.String nsuri, java.lang.String localName, java.lang.String fqName, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {

        if (fqName.equals(ELEMENT_FIELD)) {
            String attr = attributes.getValue(ATTRIBUTE_VAR);
            if (attr.equals(ATTRIBUTE_ID)) _state = IN_ID;
            else if (attr.equals(ATTRIBUTE_POLLTYPE)) _state = IN_POLLTYPE;
            else if (attr.equals(ATTRIBUTE_ACCESS)) _state = IN_ACCESS;
            else if (attr.equals(ATTRIBUTE_CUSTOM)) {
                _state = IN_CUSTOM;
                _customId = "custom";
            } else if (attr.equals(ATTRIBUTE_QUESTION)) {
                _state = IN_QUESTION;
                _question = attributes.getValue(ATTRIBUTE_LABEL);
            } 
        } else if (fqName.equals(ELEMENT_VALUE)) {
        } else if (fqName.equals(ELEMENT_OPTION)) {
            _state = IN_ANSWER;
            _currentAnswer = new PollHelperAnswer();
            _currentAnswer.text = new StringBuffer();
            _currentAnswer.text.append(attributes.getValue("label"));
        }
    }
       
    
    public String getPollID() {
        return _pollID;
    }

    public String getQuestion() {
        return _question;
    }

    public int countAnswers() {
        return _answers.size();
    }
    
    public String getAnswer(int index) throws IndexOutOfBoundsException {
        PollHelperAnswer a = (PollHelperAnswer)_answers.get(index);
        if (a == null) {
            throw new IndexOutOfBoundsException();
        }
        return a.text.toString();
    }
    
    public String createResponse(int index) {
        XDataForm xdf = (XDataForm)_sdf.createElementNode(new NSI("x", XDataForm.NAMESPACE), null);
        xdf.setType(XDataForm.SUBMIT);

        if (_pollID != null) {
            XDataField f1 = xdf.addField("id");
            f1.addValue(_pollID);
        }

        PollHelperAnswer a = (PollHelperAnswer)_answers.get(index);
        XDataField f = xdf.addField("question");
        f.addValue(a.id);

        return xdf.toString();
    }


    /**
     * returns a serialized pollr element based on the 
     * specified custom answer.  The returned string is suitable for use
     * in a poll response message.
     */
    public String createResponse(String answer) throws CollaborationException {
        // compare with predefined answers
        for (int i = 0 ; i < _answers.size() ; i++) {
            if (answer.equals((String)((PollHelperAnswer)_answers.get(i)).text.toString())) {
                return createResponse(i);
            }
        }

        if (_customId != null) {
            XDataForm xdf = (XDataForm)_sdf.createElementNode(new NSI("x", XDataForm.NAMESPACE), null);
            xdf.setType(XDataForm.SUBMIT);

            if (_pollID != null) {
                XDataField f1 = xdf.addField("id");
                f1.addValue(_pollID);
            }

            XDataField a = xdf.addField("custom");
            a.addValue(answer);
            return xdf.toString();
        } else {
            throw new CollaborationException("Custom answer not allowed for this poll: " + answer);
        }
    }

    public java.io.InputStream createResponseStream(String answer) throws CollaborationException {
	byte[] buf = createResponse(answer).getBytes();
	return new java.io.ByteArrayInputStream(buf, 0, buf.length);
    }

    public boolean isCustomAnswerAllowed() {
        if (_customId != null) {
            return true;
        } else {
            return false;
        }
    }
    
}
