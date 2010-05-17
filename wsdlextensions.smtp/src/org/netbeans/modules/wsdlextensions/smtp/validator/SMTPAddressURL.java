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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/***************************************************************************
 *
 *          Copyright (c) 2005, SeeBeyond Technology Corporation,
 *          All Rights Reserved
 *
 *          This program, and all the routines referenced herein,
 *          are the proprietary properties and trade secrets of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 *          Except as provided for by license agreement, this
 *          program shall not be duplicated, used, or disclosed
 *          without  written consent signed by an officer of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 ***************************************************************************/
package org.netbeans.modules.wsdlextensions.smtp.validator;

import java.io.UnsupportedEncodingException;
import javax.mail.internet.AddressException;
import org.netbeans.modules.wsdlextensions.smtp.SMTPAddress;
import org.netbeans.modules.wsdlextensions.smtp.SMTPInput;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.mail.internet.InternetAddress;

import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;

import org.netbeans.modules.wsdlextensions.smtp.validator.Mailbox;
/**
 * The MailTo class provides an interpretation of the mailto URI syntax as per
 * RFC 2368.  The mailto URI designates an Internet mailing address of
 * individuals.  It allows for multiple senders and the setting of mail header
 * fields.
 * <p>
 * Encoding is a very tricky part of the URI syntax.  The MailTo class only
 * accepts properly encoded strings.  The unmarshal() method will fail if
 * given an improper encoding.  The marshal() method only returns encoded
 * strings.  Once a mailto URI is unmarshalled, calls to methods getMailBox(),
 * addMailbox(), addHeader(), getHeader(), and getHeaders() will return
 * unencoded strings.  Using encoded strings with these methods will result
 * in unpredicatable behavior if the MailTo class is unmarshalled.  The
 * reasoning for this decision is that most
 * users of the class only have to deal with encodings at the unmarshal and
 * marshal level.  Most times they don't want to have to deal with it when
 * working with the components of the mailto URI
 * <p>
 * The encoding scheme is defined by RFC 2368 and RFC 1738, but it is detailed
 * below.
 * <ul>
 * <li>All unsafe characters as defined by RFC 1738 must be encoded.  For the
 * mailto URI this includes common characters such as the space, the quote, the
 * less-than-sign, and the greater-than-sign.</li>
 * <li>All alphanumeric characters do NOT have to be encoded.</li>
 * <li>All special characters $-_.+!*'(), do NOT have to be encoded.</li>
 * <li>The question mark (?), the equals sign (=), and the ampersand sign (&) are
 * reserved characters in the mailto URI scheme.  That means if they are used in
 * a mailbox or header, they must be encoded.
 * <p>
 * <blockquote>
 *       foo@domain.com?someHeader=blah&blah     // WRONG!
 *
 *       foo@domain.com?someHeader=blah%26blah   // RIGHT
 * </blockquote>
 * </li>
 * <li>All other URL reserved characters that are not being used for the mailto
 * URI scheme do NOT have to be encoded.  This includes semi-colon (;),
 * slash (/), colon (:), and the at-sign ("@").
 * </ul>
 *
 * @author       Sainath Adiraju
 *
 *
 */
public class SMTPAddressURL{
    
    public static final String MAILTO_SCHEME = "mailto";
	private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.smtp.validator.Bundle");
    
    private Map mHeaders;
    private Collection mMailboxes;
    private Collection mCCMailboxes;
    private Collection mBCCMailboxes;
private String subject;
    private String body;


    
    public SMTPAddressURL() {
        mHeaders = new HashMap();
        mMailboxes = new ArrayList();
        mCCMailboxes = new ArrayList();
        mBCCMailboxes = new ArrayList();
		subject = "";
        body = "";

    }
    
    public SMTPAddressURL(String mailToURI) throws URISyntaxException {
        this();
       // unmarshal(new URI(mailToURI));
    }
    
    public String getProtocol() {
        return MAILTO_SCHEME;
    }
    
    public Collection getMailbox() {
        return mMailboxes;
    }
    
     public Collection getCCMailbox() {
        return mCCMailboxes;
    }
     
      public Collection getBCCMailbox() {
        return mBCCMailboxes;
    }
    
    public void addMailbox(Mailbox mailbox) {
        mMailboxes.add(mailbox);
    }
    
    public void addHeader(String name, String value) {
        mHeaders.put(name, value);
    }
    
    public String getHeader(String name) {
        return (String)mHeaders.get(name);
    }
    
    public Map getHeaders() {
        return mHeaders;
    }
    
    public void unmarshal(Collection<Validator.ResultItem> results, Validator validator, SMTPAddress target) throws URISyntaxException ,InvalidMailboxException{
                if("".equals(target.getLocation())){
                    results.add(new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target,
                    mMessages.getString("SMTPAddress.NEED_VALUE_FOR_LOCATION")));
                }
                if("".equals(target.getSMTPServer())){
                    results.add(new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target,
                    mMessages.getString("SMTPAddress.NEED_VALUE_FOR_SMTPSERVER")));
                }
        String targetValidate = target.getLocation();
        int length = targetValidate.length();
        char charTarget[]= new char[length];
        targetValidate.getChars(0,length , charTarget, 0);
        int i=0;
        while(i<charTarget.length){
        if(charTarget[i]==' '){
          results.add(new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target,
                    mMessages.getString("SMTPAddress.SPACES_NOT_ALLOWED_IN_LOCATION")));
          break;
        }
        i++;
        }
           
        URI mailToURI = new URI(target.getLocation());
       	String scheme = mailToURI.getScheme();
		try {
            if (!scheme.equals(MAILTO_SCHEME )) {
                results.add(new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target,
                    mMessages.getString("SMTPAddress.MISSING_SMTP_URL")));
            }
            
            String data = mailToURI.getRawSchemeSpecificPart();
            
            // Check if there are headers
            int questionMark = data.indexOf('?');
            if (questionMark == -1) {
            	parseMailBoxes(data,results,"",validator,target);
		//validateAdressUrl(data,results,validator,target);
            } else {
                parseMailBoxes(data.substring(0, questionMark),results,"",validator,target);
                parseHeaders(data.substring(questionMark + 1,
                        data.length()),results,validator,target);
                
                Iterator it = mMailboxes.iterator();
                while (it.hasNext()) {
                	Mailbox mailBox = (Mailbox)it.next();
                	validateAdressUrl(mailBox.getNormalizedAddressSpec(),results,validator,target);
                }
                
                Iterator itforcc = mCCMailboxes.iterator();
                while (itforcc.hasNext()) {
                	Mailbox mailBox = (Mailbox)itforcc.next();
                	validateAdressUrl(mailBox.getNormalizedAddressSpec(),results,validator,target);
                }
                
                Iterator itforbcc = mBCCMailboxes.iterator();
                while (itforbcc.hasNext()) {
                	Mailbox mailBox = (Mailbox)itforbcc.next();
                	validateAdressUrl(mailBox.getNormalizedAddressSpec(),results,validator,target);
                }
                
            }
        } catch (URISyntaxException ex) {
            throw ex;
        } catch (Exception ex) {
            URISyntaxException mex =
                    new URISyntaxException(mailToURI.toString(), "");
            mex.initCause(ex);
            throw mex;
        }
    }
    
    protected void parseMailBoxes(String data,Collection<Validator.ResultItem> results, String mailBoxType,Validator validator ,SMTPAddress target ) throws InvalidMailboxException
    {
        
        if (data == null || data.equals("")) {
            return;
        }
        String decodedData = data;
        try {
             decodedData = URLDecoder.decode(data,"US-ASCII");
        } catch (UnsupportedEncodingException ex) {
            //do nothing
        }
        
        String[] mailboxes = data.split(",");
        for (int ii = 0; ii < mailboxes.length; ii++) {
            try {
            if (mailBoxType.compareToIgnoreCase("cc")==0)
                mCCMailboxes.add(new Mailbox(mailboxes[ii]));
            else if (mailBoxType.compareToIgnoreCase("bcc")==0 )
                mBCCMailboxes.add(new Mailbox(mailboxes[ii]));
            else
                mMailboxes.add(new Mailbox(mailboxes[ii]));
            } catch (InvalidMailboxException ex) {
                results.add(new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target,
                    mMessages.getString("SMTPHeader.MISSING_HEADER_VALUE")));
            }
        }
    }
    
    protected void parseHeaders(String data,Collection<Validator.ResultItem> results,Validator validator, SMTPAddress target )
    throws URISyntaxException {
        
        
        if (data == null || data.equals("")) {
            return;
        }
        
        String[] entries = data.split("&");
        for (int ii = 0; ii < entries.length; ii++) {
            String[] keyValue = entries[ii].split("=");
            if (keyValue.length != 2) {
                results.add(new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target,
                    mMessages.getString("SMTPHeader.MISSING_HEADER_VALUE")));
            }
            if("".equals(keyValue[0])){
                results.add(new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target,
                    mMessages.getString("SMTPHeader.MISSING_HEADER_KEY")));
            }
			try {
                keyValue[0] = URLDecoder.decode(keyValue[0],"US-ASCII");
            } catch (UnsupportedEncodingException ex) {
                //do nothing
            }
            try {
                keyValue[1] = URLDecoder.decode(keyValue[1],"US-ASCII");
            } catch (UnsupportedEncodingException ex) {
                //do nothing
            }

            mHeaders.put(keyValue[0], keyValue[1]);
            try{
                if(keyValue[0].compareToIgnoreCase("cc")== 0)
                    parseMailBoxes(keyValue[1],results, "cc",validator,target);
                else if (keyValue[0].compareToIgnoreCase("bcc")== 0)
                    parseMailBoxes(keyValue[1],results, "bcc",validator, target);
                else if (keyValue[0].compareToIgnoreCase("to")== 0)
                	parseMailBoxes(keyValue[1],results, "to",validator,target);
		else if (keyValue[0].compareToIgnoreCase("subject") == 0)
                    subject = keyValue[1];
                else if (keyValue[0].compareToIgnoreCase("body") == 0)
                    body = keyValue[1];

            } catch (Exception ex) {
                results.add(new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target,
                    mMessages.getString("SMTPMAILBOX.NOT_VALID_MAILBOX")));
            }
        }
        
    }
    protected void validateAdressUrl(String str,Collection<Validator.ResultItem> results,Validator validator, SMTPAddress target){
    	try{
         InternetAddress IA = new InternetAddress(str , false);
        }catch(AddressException ae){
             results.add(new Validator.ResultItem(validator,
	             Validator.ResultType.ERROR,
	             target,ae.getMessage()));	       
        }
    	
    }
    
protected void validateAdressUrl(String str,Collection<Validator.ResultItem> results,Validator validator, SMTPInput target){
    	
          try{
              InternetAddress IA = new InternetAddress(str , false);
          }catch(AddressException ae){
              results.add(new Validator.ResultItem(validator,
	            Validator.ResultType.ERROR,
	            target,ae.getMessage()));
            }
    	   
    }
}
