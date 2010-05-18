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

import java.util.*;
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
class PollAnswer
{
    String text = null;
    String id = null;
    int count = 0;
    boolean custom = false;

    PollAnswer(String id, String text) {
        this.id = id;
        this.text = text;
    }

    PollAnswer(String id) {
        this.id = id;
    }

    PollAnswer() {
    }
    
}


/**
 * Helper class for application using the poll functionality. 
 * This class can be used to 
 * parse the xml from the poll message and generate a poll response based on
 * user input.
 *
 */
public class Poll
{
    private LinkedList _answers = new LinkedList();
    private String _customId = null;
    private String _question;
    private String _pollID;
    private String _pollType = null;
    private String _access = null;
    private boolean _quote;
    private boolean _customAnswerAllowed = false;
    private PollAnswer _lastParsedAnswer;
    
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
        
    public static final String POLLTYPE_ANONYMOUS = "anon";
    public static final String POLLTYPE_OPEN = "open";

    public static final String POLLACCESS_PRIVATE = "private";
    public static final String POLLACCESS_PARTICIPANTS = "participants";
    public static final String POLLACCESS_PUBLIC = "public";
    public static final String POLLACCESS_NONE = "none";

    private JSOImplementation _jso;
    private StreamDataFactory _sdf;


    /** 
     * Constructor used by poll message receiver
     * @param in xml content's input stream.
     * The stream must be UTF-8 encoded.
     */
    public Poll(java.io.InputStream in) throws Exception {
        _jso = JSOImplementation.getInstance();
        _sdf = _jso.createDataFactory();
        SAX.parse(in, new PollMessageParser());
    }

    /**
     * Constructor used by poll message receiver
     * @param message content of the application/x-iim-poll message
     */
    public Poll(String message) throws Exception {
        _jso = JSOImplementation.getInstance();
        _sdf = _jso.createDataFactory();
        SAX.parse(message, new PollMessageParser());
    }

    /**
     * Constructor used by poll sender
     * @param question the question
     * @param answers valid answers
     * @param custom whether custom answers are allowed
     */
    public Poll(String question, java.util.List answers, boolean custom) 
    {
        _jso = JSOImplementation.getInstance();
        _sdf = _jso.createDataFactory();
	_question = question;
        _pollID = _jso.toString();
	loadAnswers(answers);
	_customAnswerAllowed = custom;
    }

    /**
     * Constructor used by polster
     * @param pollID unique poll identifier
     * @param question the question
     * @param answers valid answers
     * @param custom whether custom answers are allowed
     * @param pollType anonymous vs. open
     * @param access access list or type (private, public, participants).
     */
    public Poll(String pollID, String question,
		java.util.List answers, boolean custom,
		String pollType, String access) 
    {
        _jso = JSOImplementation.getInstance();
        _sdf = _jso.createDataFactory();
	_pollID = pollID;
	_question = question;
	_customAnswerAllowed = custom;
	_access = access;
	_pollType = pollType;
	loadAnswers(answers);
    }




    private void loadAnswers(java.util.List answers)
    {
        for (int i = 0 ; i < answers.size() ; i++) {
            _answers.add(new PollAnswer(Integer.toString(i),
					(String)answers.get(i)));
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
    
    public String getAnswer(int index) throws IndexOutOfBoundsException
    {
        PollAnswer a = (PollAnswer)_answers.get(index);
        if (a == null) {
            throw new IndexOutOfBoundsException();
        }
        return a.text;
    }
    
    public String createResponse(int index) throws CollaborationException {
        PollAnswer a = (PollAnswer)_answers.get(index);
        return createResponse(a.text);
    }

    public XDataForm createXDataResponse(String answer) throws CollaborationException {
        XDataForm xdf = (XDataForm)_sdf.createElementNode(new NSI("x", XDataForm.NAMESPACE), null);
        xdf.setType(XDataForm.SUBMIT);

        if (_pollID != null) {
            XDataField f1 = xdf.addField("id");
            f1.addValue(_pollID);
        }

        // compare to predefined answer
        for (int i = 0; i < _answers.size(); i++) {
            if (answer.equals((String)((PollAnswer)_answers.get(i)).text.toString())) {
                XDataField a = xdf.addField("question");
                a.addValue(answer);
                return xdf;
            }
        }

        // no match
        if (_customAnswerAllowed) {
            XDataField a = xdf.addField("custom");
            a.addValue(answer);
            return xdf;
        } else {
            throw new CollaborationException("Custom answer no allowed for this poll: " + answer);
        }
    }



    /**
     * returns a serialized pollr element based on the 
     * specified custom answer.  The returned string is suitable for use
     * in a poll response message.
     */
    public String createResponse(String answer) throws CollaborationException {
        return createXDataResponse(answer).toString();
    }


    public java.io.InputStream createResponseStream(String answer) throws CollaborationException {
	byte[] buf = createResponse(answer).getBytes();
	return new java.io.ByteArrayInputStream(buf, 0, buf.length);
    }

    public boolean isCustomAnswerAllowed() 
    {
	return _customAnswerAllowed;
    }

    public boolean isAnonynous()
    {
	return (_pollType != null &&
		_pollType.equalsIgnoreCase(POLLTYPE_ANONYMOUS));
    }

    public String getAccess()
    {
	return _access;
    }

    public String createResponse(String pollID, String answer) throws CollaborationException {
        _pollID = pollID;
        return createResponse(answer);
    }

    private PollAnswer findAnswer(String answer)
    {
	for (Iterator i = _answers.iterator(); i.hasNext(); ) {
	    PollAnswer a = (PollAnswer)i.next();
	    if (a.text != null && answer.equals(a.text)) return a;
	}
	if (_customAnswerAllowed) {
	    PollAnswer a = new PollAnswer();
	    a.text = answer;
	    return a;
	}
	return null;
    }
    
    private PollAnswer findAnswerFromId(String id)
    {
	for (Iterator i = _answers.iterator(); i.hasNext(); ) {
	    PollAnswer a = (PollAnswer)i.next();
	    if (a.id != null && id.equals(a.id)) return a;
	}
	return null;
    }
    
   /**
    * parse an answer received for this poll
    * @param in UTF-8 encoded input stream to the content of the 
    * application/x-iim-poll-reply message or message part 
    */
    public String parseAnswer(java.io.InputStream in) throws Exception
    {
        SAX.parse(in, new PollResponseParser());
	if (_lastParsedAnswer == null) {
	    throw new Exception("Answer does not match anything");
	}
	return _lastParsedAnswer.text;
    }

   /**
    * parse an answer received for this poll
    * @param str content of the
    * application/x-iim-poll-reply message or message part
    */
    public synchronized String parseAnswer(String str) throws Exception
    {
        SAX.parse(str, new PollResponseParser());
	if (_lastParsedAnswer == null) {
	    throw new Exception("Answer does not match anything");
	}
	return _lastParsedAnswer.text;
    }

    public int getCount(String answerID)
    {
	//PollAnswer a = _answers.getID()
	return 0; // todo
    }


    public XDataForm getXDataForm()
    { 
        XDataForm xdf = (XDataForm)_sdf.createElementNode(new NSI("x", XDataForm.NAMESPACE), null);
        xdf.setType(XDataForm.FORM);

	if (_pollID != null) {
            XDataField f1 = xdf.addField("id", XDataField.HIDDEN);
            f1.addValue(_pollID);
	}

        if (_pollType != null) {
            XDataField f2 = xdf.addField("polltype", XDataField.HIDDEN);
            f2.addValue(_pollType);
        }

        if (_access != null) {
            XDataField f3 = xdf.addField("access", XDataField.HIDDEN);
            f3.addValue(_access);
        }

        XDataField q = xdf.addField("question", XDataField.LIST_SINGLE);
        q.setLabel(_question);
        //q.setRequired(true);

        for (int i = 0 ; i < _answers.size() ; i++) {
	    PollAnswer a = (PollAnswer)_answers.get(i);
            q.addOption(a.text, a.id);
        }

        if (_customAnswerAllowed) {
            XDataField c = xdf.addField("custom", XDataField.TEXT_SINGLE);
            c.setLabel("Custom Reply");
        }

        return xdf;
    }

    /**
     * get the XML representation of this Poll
     * The return string can be used to generate the contents
     * of a poll message
     * @return poll message content String
     */
    public String toString()
    { 
        return getXDataForm().toString();
    }


    // ------------------------------------------------------
    // Poll Response Parser
    // ------------------------------------------------------

    class PollMessageParser extends org.xml.sax.helpers.DefaultHandler 
    {

	private int _state;
	private PollAnswer _answer;

	public void characters(char[] ch, int start, int length) throws org.xml.sax.SAXException {
	    switch (_state) {
		
            case IN_ID :
                _pollID = String.copyValueOf(ch, start, length);
                break;

            case IN_POLLTYPE :
                _pollType = String.copyValueOf(ch, start, length);
                break;

            case IN_ANSWER :
		_answer.id = String.copyValueOf(ch, start, length);
                _answers.add(_answer);
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
        
	public void endElement(java.lang.String nsuri, java.lang.String localName, java.lang.String fqName) throws org.xml.sax.SAXException {
            /*if (_state == IN_ANSWER) {
                _answers.add(_answer);
            }*/
	}
	
	public void startElement(java.lang.String nsuri, java.lang.String localName, java.lang.String fqName, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException 
	{
            if (fqName.equals(ELEMENT_FIELD)) {
                String attr = attributes.getValue(ATTRIBUTE_VAR);
                if (attr.equals(ATTRIBUTE_ID))  _state = IN_ID;
                else if (attr.equals(ATTRIBUTE_POLLTYPE)) _state = IN_POLLTYPE;
                else if (attr.equals(ATTRIBUTE_ACCESS)) _state = IN_ACCESS;
                else if (attr.equals(ATTRIBUTE_CUSTOM)) {
                    _state = IN_CUSTOM;
                    _customAnswerAllowed = true;
                } else if (attr.equals(ATTRIBUTE_QUESTION)) {
                    _state = IN_QUESTION;
                    _question = attributes.getValue(ATTRIBUTE_LABEL);
                } 
            } else if (fqName.equals(ELEMENT_VALUE)) {
            } else if (fqName.equals(ELEMENT_OPTION)) {
                _state = IN_ANSWER;
                _answer = new PollAnswer();
                _answer.text = attributes.getValue(ATTRIBUTE_LABEL);
            }
        }
    }



    // ------------------------------------------------------
    // Poll Response Parser
    // ------------------------------------------------------

    class PollResponseParser extends org.xml.sax.helpers.DefaultHandler
    {

	private int _state;
	private PollAnswer _currentAnswer = null;
	private StringBuffer _quotedAnswer = null;
	private StringBuffer _id = null;

	public void characters(char[] ch, int start, int length) throws org.xml.sax.SAXException {
            String s = String.copyValueOf(ch, start, length);
            if ("\n".equals(s)) return;
            switch (_state) {
                case IN_ANSWER :
                    _id.append(ch, start, length);
                    _currentAnswer = findAnswerFromId(_id.toString());
                    break;
                case IN_CUSTOM :
                    _quotedAnswer.append(ch, start, length);
                    break;
                case IN_ID :
                    _pollID = s;
                    break;
                case IN_POLLTYPE :
                    _pollType = s;
                    break;
                case IN_ACCESS :
                    _access = s;
                    break;
                default :
		    throw new org.xml.sax.SAXException("No characters at this stage");
            }
	} 
      
	public void startDocument() throws org.xml.sax.SAXException 
	{
	    _lastParsedAnswer = null;
	}
    
	public void endDocument() throws org.xml.sax.SAXException 
	{
	    if (_currentAnswer == null && _quotedAnswer != null) {
		_currentAnswer = findAnswer(_quotedAnswer.toString());
	    }
	    _lastParsedAnswer = _currentAnswer;
	}
    
	public void endElement(java.lang.String nsuri, java.lang.String localName, java.lang.String fqName) throws org.xml.sax.SAXException {
            if (_state == IN_ANSWER || _state == IN_CUSTOM) {
                if (_currentAnswer != null) _currentAnswer.count++;
            }
	}
    
        public void startElement(java.lang.String nsuri, java.lang.String localName, java.lang.String fqName, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException 
        {
            if (fqName.equals(ELEMENT_FIELD)) {
                String attr = attributes.getValue(ATTRIBUTE_VAR);
                if (attr.equals(ATTRIBUTE_ID)) _state = IN_ID;
                else if (attr.equals(ATTRIBUTE_ACCESS)) _state = IN_ACCESS;
                else if (attr.equals(ATTRIBUTE_POLLTYPE)) _state = IN_POLLTYPE;
                else if (attr.equals(ATTRIBUTE_QUESTION)) _state = IN_ANSWER;
                else if (attr.equals(ATTRIBUTE_CUSTOM)) _state = IN_CUSTOM;
            } else if (fqName.equals(ELEMENT_VALUE)) {
                _id = new StringBuffer();
                _quotedAnswer = new StringBuffer();
            }
	}
    }




    // ------------------------------------------------------
    // main for testing
    // ------------------------------------------------------

    public static void main(String[] arg)
    {
	
    }



}
