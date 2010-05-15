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
package org.netbeans.modules.wsdlextensions.email.validator;

import java.io.UnsupportedEncodingException;
import javax.mail.internet.AddressException;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPAddress;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPInput;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.mail.internet.InternetAddress;

import org.netbeans.modules.xml.xam.spi.Validator;
import org.openide.util.Exceptions;

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
public class SMTPMailto {

    public static final String MAILTO_SCHEME = "mailto";
    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.email.validator.Bundle");
    private Map<String, String> mHeaders;
    private List<Mailbox> mMailboxes;
    private List<Mailbox> mCCMailboxes;
    private List<Mailbox> mBCCMailboxes;
    private String subject;
    private String body;

    public SMTPMailto() {
        mHeaders = new HashMap<String, String>();
        mMailboxes = new ArrayList<Mailbox>();
        mCCMailboxes = new ArrayList<Mailbox>();
        mBCCMailboxes = new ArrayList<Mailbox>();
        subject = "";
        body = "";
    }

    public String getProtocol() {
        return MAILTO_SCHEME;
    }

    public Collection<Mailbox> getMailboxes() {
        return mMailboxes;
    }

    public Collection<Mailbox> getCCMailboxes() {
        return mCCMailboxes;
    }

    public Collection<Mailbox> getBCCMailboxes() {
        return mBCCMailboxes;
    }

    public void addMailbox(Mailbox mailbox) {
        mMailboxes.add(mailbox);
    }

    public void addHeader(String name, String value) {
        mHeaders.put(name, value);
    }

    public String getHeader(String name) {
        return (String) mHeaders.get(name);
    }

    public Map getHeaders() {
        return mHeaders;
    }

    public void unmarshal(String location) throws URISyntaxException, AddressException {
        if (location != null) {
            URI mailToURI = new URI(location);
            String scheme = mailToURI.getScheme();
            if (MAILTO_SCHEME.equals(scheme)) {
                String data = mailToURI.getRawSchemeSpecificPart();

                // Check if there are headers
                int questionMark = data.indexOf('?');
                if (questionMark == -1) {
                    parseMailBoxes(data, "");
                } else {
                    parseMailBoxes(data.substring(0, questionMark), "");
                    parseHeaders(data.substring(questionMark + 1, data.length()));

                    for (Mailbox mailBox : mMailboxes) {
                        InternetAddress IA = new InternetAddress(mailBox.getNormalizedAddressSpec(), false);
                    }

                    for (Mailbox mailBox : mCCMailboxes) {
                        InternetAddress IA = new InternetAddress(mailBox.getNormalizedAddressSpec(), false);
                    }

                    for (Mailbox mailBox : mBCCMailboxes) {
                        InternetAddress IA = new InternetAddress(mailBox.getNormalizedAddressSpec(), false);
                    }
                }
            }
        }
    }

    public void unmarshal(Collection<Validator.ResultItem> results, Validator validator, SMTPAddress target) throws URISyntaxException {

//        if ("".equals(target.getLocation())) {
//            results.add(new Validator.ResultItem(validator,
//                    Validator.ResultType.ERROR,
//                    target,
//                    mMessages.getString("SMTPAddress.NEED_VALUE_FOR_LOCATION")));
//        }
        if ("".equals(target.getEmailServer())) {
            results.add(new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target,
                    mMessages.getString("SMTPAddress.NEED_VALUE_FOR_SMTPSERVER")));
        }

        String location = target.getLocation();
        if (location != null) {
            if (location.contains(" ")) {
                results.add(new Validator.ResultItem(validator,
                        Validator.ResultType.ERROR,
                        target,
                        mMessages.getString("SMTPAddress.SPACES_NOT_ALLOWED_IN_LOCATION")));
            }

            URI mailToURI = new URI(location);
            String scheme = mailToURI.getScheme();
            try {
                if (!MAILTO_SCHEME.equals(scheme)) {
                    results.add(new Validator.ResultItem(validator,
                            Validator.ResultType.ERROR,
                            target,
                            MessageFormat.format(mMessages.getString("SMTPAddress.MISSING_SMTP_URL"), location)));
                }

                String data = mailToURI.getRawSchemeSpecificPart();

                // Check if there are headers
                int questionMark = data.indexOf('?');
                if (questionMark == -1) {
                    parseMailBoxes(data, results, "", validator, target);
                    //validateAdressUrl(data,results,validator,target);
                } else {
                    parseMailBoxes(data.substring(0, questionMark), results, "", validator, target);
                    parseHeaders(data.substring(questionMark + 1,
                            data.length()), results, validator, target);

                    for (Mailbox mailBox : mMailboxes) {
                        validateAdressUrl(mailBox.getNormalizedAddressSpec(), results, validator, target);
                    }

                    for (Mailbox mailBox : mCCMailboxes) {
                        validateAdressUrl(mailBox.getNormalizedAddressSpec(), results, validator, target);
                    }

                    for (Mailbox mailBox : mBCCMailboxes) {
                        validateAdressUrl(mailBox.getNormalizedAddressSpec(), results, validator, target);
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
    }

    protected void parseMailBoxes(String data, Collection<Validator.ResultItem> results, String mailBoxType, Validator validator, SMTPAddress target) {
        if (data == null || data.equals("")) {
            return;
        }
        String decodedData = data;
        try {
            decodedData = URLDecoder.decode(data, "US-ASCII");
        } catch (UnsupportedEncodingException ex) {
            //do nothing
        }

        String[] mailboxes = data.split(",");
        for (String mailbox : mailboxes) {
            try {
                if (mailBoxType.compareToIgnoreCase("cc") == 0) {
                    mCCMailboxes.add(new Mailbox(mailbox));
                } else if (mailBoxType.compareToIgnoreCase("bcc") == 0) {
                    mBCCMailboxes.add(new Mailbox(mailbox));
                } else {
                    mMailboxes.add(new Mailbox(mailbox));
                }
            } catch (AddressException ex) {
                results.add(new Validator.ResultItem(validator,
                        Validator.ResultType.ERROR,
                        target,
                        ex.getLocalizedMessage()));
            }
        }
    }

    protected void parseHeaders(String data, Collection<Validator.ResultItem> results, Validator validator, SMTPAddress target)
            throws URISyntaxException {

        if (data == null || data.equals("")) {
            return;
        }

        String[] entries = data.split("&");
        for (String entry : entries) {
            String[] keyValue = entry.split("=");
            if (keyValue.length != 2) {
                results.add(new Validator.ResultItem(validator,
                        Validator.ResultType.ERROR,
                        target,
                        MessageFormat.format(mMessages.getString("SMTPHeader.MISSING_HEADER_VALUE"), entry)));
            }
            if ("".equals(keyValue[0])) {
                results.add(new Validator.ResultItem(validator,
                        Validator.ResultType.ERROR,
                        target,
                        mMessages.getString("SMTPHeader.MISSING_HEADER_KEY")));
            }
            try {
                keyValue[0] = URLDecoder.decode(keyValue[0], "US-ASCII");
            } catch (UnsupportedEncodingException ex) {
                //do nothing
            }
            try {
                keyValue[1] = URLDecoder.decode(keyValue[1], "US-ASCII");
            } catch (UnsupportedEncodingException ex) {
                //do nothing
            }

            mHeaders.put(keyValue[0], keyValue[1]);
            try {
                if (keyValue[0].compareToIgnoreCase("cc") == 0) {
                    parseMailBoxes(keyValue[1], results, "cc", validator, target);
                } else if (keyValue[0].compareToIgnoreCase("bcc") == 0) {
                    parseMailBoxes(keyValue[1], results, "bcc", validator, target);
                } else if (keyValue[0].compareToIgnoreCase("to") == 0) {
                    parseMailBoxes(keyValue[1], results, "to", validator, target);
                } else if (keyValue[0].compareToIgnoreCase("subject") == 0) {
                    subject = keyValue[1];
                } else if (keyValue[0].compareToIgnoreCase("body") == 0) {
                    body = keyValue[1];
                }

            } catch (Exception ex) {
                results.add(new Validator.ResultItem(validator,
                        Validator.ResultType.ERROR,
                        target,
                        mMessages.getString("SMTPMAILBOX.NOT_VALID_MAILBOX")));
            }
        }

    }

    protected void validateAdressUrl(String str, Collection<Validator.ResultItem> results, Validator validator, SMTPAddress target) {
        try {
            InternetAddress IA = new InternetAddress(str, false);
        } catch (AddressException ae) {
            results.add(new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target, ae.getMessage()));
        }

    }

    protected void validateAdressUrl(String str, Collection<Validator.ResultItem> results, Validator validator, SMTPInput target) {

        try {
            InternetAddress IA = new InternetAddress(str, false);
        } catch (AddressException ae) {
            results.add(new Validator.ResultItem(validator,
                    Validator.ResultType.ERROR,
                    target, ae.getMessage()));
        }
    }

    private void parseMailBoxes(String data, String mailBoxType) throws AddressException {
        if (data == null || data.equals("")) {
            return;
        }
        String decodedData = data;
        try {
            decodedData = URLDecoder.decode(data, "US-ASCII");
        } catch (UnsupportedEncodingException ex) {
            //do nothing
        }

        String[] mailboxes = data.split(",");
        for (String mailbox : mailboxes) {
            if (mailBoxType.compareToIgnoreCase("cc") == 0) {
                mCCMailboxes.add(new Mailbox(mailbox));
            } else if (mailBoxType.compareToIgnoreCase("bcc") == 0) {
                mBCCMailboxes.add(new Mailbox(mailbox));
            } else {
                mMailboxes.add(new Mailbox(mailbox));
            }
        }
    }

    private void parseHeaders(String data) throws AddressException {
        if (data == null || data.equals("")) {
            return;
        }

        String[] entries = data.split("&");
        for (String entry : entries) {
            String[] keyValue = entry.split("=");
            if (keyValue.length != 2) {
                throw new AddressException(MessageFormat.format(mMessages.getString("SMTPHeader.MISSING_HEADER_VALUE"), entry), entry, -1);
            }
            if ("".equals(keyValue[0])) {
                throw new AddressException(mMessages.getString("SMTPHeader.MISSING_HEADER_KEY"), entry, -1);
            }
            try {
                keyValue[0] = URLDecoder.decode(keyValue[0], "US-ASCII");
            } catch (UnsupportedEncodingException ex) {
                //do nothing
            }
            try {
                keyValue[1] = URLDecoder.decode(keyValue[1], "US-ASCII");
            } catch (UnsupportedEncodingException ex) {
                //do nothing
            }

            mHeaders.put(keyValue[0], keyValue[1]);
//            try {
            if (keyValue[0].compareToIgnoreCase("cc") == 0) {
                parseMailBoxes(keyValue[1], "cc");
            } else if (keyValue[0].compareToIgnoreCase("bcc") == 0) {
                parseMailBoxes(keyValue[1], "bcc");
            } else if (keyValue[0].compareToIgnoreCase("to") == 0) {
                parseMailBoxes(keyValue[1], "to");
            } else if (keyValue[0].compareToIgnoreCase("subject") == 0) {
                subject = keyValue[1];
            } else if (keyValue[0].compareToIgnoreCase("body") == 0) {
                body = keyValue[1];
            }

//            } catch (Exception ex) {
//                throw new URISyntaxExceptionException(mMessages.getString("SMTPMAILBOX.NOT_VALID_MAILBOX"));
//            }
        }
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @return the body
     */
    public String getBody() {
        return body;
    }
}
