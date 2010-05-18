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

import java.util.HashMap;
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
class PolsterHelperAnswer {
    String text;
    String id;
    int count;
    boolean custom;

    PolsterHelperAnswer(String index, String str) {
        id = index;
        text = str;
        count = 0;
        custom = false;
    }

    PolsterHelperAnswer() {
        id = "custom";
        text = null;
        count = 0;
        custom = true;
    }
    
}


/**
 * Helps Collaboration client create poll messages and collect poll responses
 */
public class PolsterHelper extends org.xml.sax.helpers.DefaultHandler {

    private HashMap _answers;
    private String _customId = null;
    private String _question;
    private int _state;
    private PolsterHelperAnswer _currentAnswer;
    private StringBuffer _quotedAnswer;
    private StringBuffer _pollMessage;

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
     * Constructor used by poller
     * @param question the question
     * @param answers valid answers
     * @param custom whether custom answers are allowed
     */
    public PolsterHelper(String question, java.util.List answers, boolean custom) {
        _jso = JSOImplementation.getInstance();
        _sdf = _jso.createDataFactory();
        _answers = new HashMap();

        XDataForm xdf = (XDataForm)_sdf.createElementNode(new NSI("x", XDataForm.NAMESPACE), null);
        xdf.setType(XDataForm.FORM);
        
        _pollMessage = new StringBuffer();
        if (custom) {
            XDataField c = xdf.addField("custom", XDataField.TEXT_SINGLE);
            c.setLabel("Custom Reply");
            _answers.put("custom", new PolsterHelperAnswer());
        }

        XDataField q = xdf.addField("question", XDataField.LIST_SINGLE);
        q.setLabel(question);
        for (int i = 0 ; i < answers.size() ; i++) {
            String id = (new Integer(i)).toString();
            q.addOption((String)answers.get(i), id);
            _answers.put(id, new PolsterHelperAnswer(id, (String)answers.get(i)));
        }
        _pollMessage.append(xdf.toString());
    }

    public void characters(char[] ch, int start, int length) throws org.xml.sax.SAXException {
        switch (_state) {
            case IN_ANSWER :
                String id = String.copyValueOf(ch, start, length);
                _currentAnswer = (PolsterHelperAnswer)_answers.get(id);
                if (_currentAnswer == null) {
                    throw new org.xml.sax.SAXException("No answer matching id " + id);
                }
                break;
            case IN_CUSTOM :
                _quotedAnswer.append(ch, start, length);
                break;
            default :
                throw new org.xml.sax.SAXException("No characters at this stage");
        }
    } 
      
    public void startDocument() throws org.xml.sax.SAXException {
    }
    
    public void endElement(java.lang.String nsuri, java.lang.String localName, java.lang.String fqName) throws org.xml.sax.SAXException {
        if (_state == IN_ANSWER || _state == IN_CUSTOM) {
            _currentAnswer.count++;
        }
    }
    
    public void startElement(java.lang.String nsuri, java.lang.String localName, java.lang.String fqName, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
        if (fqName.equals(ELEMENT_FIELD)) {
            String attr = attributes.getValue(ATTRIBUTE_VAR);
            if (attr.equals(ATTRIBUTE_QUESTION)) {
                _state = IN_ANSWER;
            } else if (attr.equals(ATTRIBUTE_CUSTOM)) {
                _state = IN_CUSTOM;
            }
        } else if (fqName.equals(ELEMENT_VALUE)) {
            _quotedAnswer = new StringBuffer();
        } 
    }
    
   /**
    * parse an answer received for this poll
    * @param in UTF-8 encoded input stream to the content of the 
    * application/x-iim-poll-reply message or message part 
    */
    public String parseAnswer(java.io.InputStream in) throws Exception {
        SAX.parse(in, this);
        if (_currentAnswer.custom) {
            return _quotedAnswer.toString();
        } else {
            return _currentAnswer.text;
        }
    }

   /**
    * parse an answer received for this poll
    * @param str content of the
    * application/x-iim-poll-reply message or message part
    */
    public String parseAnswer(String str) throws Exception {
        SAX.parse(str, this);
        if (_currentAnswer.custom) {
            return _quotedAnswer.toString();
        } else {
            return _currentAnswer.text;
        }
    }


    /**
     * get the XML representation of this Poll
     * The return string can be used to generate the contents of a poll message
     * @return poll message content String
     */
    public String toString() {
        return _pollMessage.toString();
    }
    
}
