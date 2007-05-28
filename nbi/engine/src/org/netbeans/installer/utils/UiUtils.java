/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.utils;

import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.NO_OPTION;

/**
 *
 * @author Kirill Sorokin
 */
public final class UiUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static boolean showYesNoDialog(String title, String message) {
        final int result = JOptionPane.showConfirmDialog(
                null,
                message,
                title,
                YES_NO_OPTION);
        
        return result == YES_OPTION;
    }
    
    public static CertificateAcceptanceStatus showCertificateAcceptanceDialog(
            final Certificate[] certificates,
            final int chainStart,
            final int chainEnd,
            final boolean rootCaIsNotValid,
            final boolean timeIsNotValid,
            final Date timestamp,
            final String description) {
        if (certificates[chainStart] instanceof X509Certificate
                && certificates[chainEnd-1] instanceof X509Certificate) {
            final X509Certificate firstCert =
                    (X509Certificate) certificates[chainStart];
            final X509Certificate lastCert =
                    (X509Certificate) certificates[chainEnd-1];
            
            final Principal subject = firstCert.getSubjectDN();
            final Principal issuer = lastCert.getIssuerDN();
            
            // extract subject & issuer's name
            final String subjectName = extractName(
                    subject.getName(),
                    "CN=",
                    "Unknown Subject");
            final String issuerName = extractName(
                    issuer.getName(),
                    "O=",
                    "Unknown Issuer");
            
            // dialog caption
            String caption = null;
            String body = "";
            
            // check if this is the case when both - the root CA and time of
            // signing is valid:
            if ((!rootCaIsNotValid) && (!timeIsNotValid)) {
                caption = StringUtils.format(
                        "The digital signature of {0} has been verified.", 
                        description);
                
                body += 
                        "The digital signature has been validated by a trusted source. " +
                        "The security certificate was issued by a company that is trusted";
                
                // for timestamp info, add a message saying that certificate was
                // valid at the time of signing. And display date of signing.
                if (timestamp != null) {
                    // get the right date format for timestamp
                    final DateFormat df = DateFormat.getDateTimeInstance(
                            DateFormat.LONG,
                            DateFormat.LONG);
                    body += StringUtils.format(
                            " and was valid at the time of signing on {0}.",
                            df.format(timestamp));
                } else {
                    // add message about valid time of signing:
                    body += 
                            ", has not expired and is still valid.";
                }
                
                // we should add one more message here - disclaimer we used
                // to have.  This is to be displayed in the "All trusted"
                // case in the More Information dialog.
                body += StringUtils.format(
                        "Caution: \"{0}\" asserts that this content is safe.  You should only accept this content if you trust \"{1}\" to make that assertion.",
                        subjectName,
                        subjectName);
            } else {
                // this is the case when either publisher or time of signing
                // is invalid - check and add corresponding messages to
                // appropriate message arrays.
                
                // If root CA is not valid, add a caption and a message to the
                // securityAlerts array.
                if (rootCaIsNotValid){
                    // Use different caption text for https and signed content
                    caption = StringUtils.format(
                            "The digital signature of {0} cannot be verified.", 
                            description);
                    
                    body += "The digital signature cannot be verified by a trusted source. " +
                            "Only continue if you trust the origin of the file. " +
                            "The security certificate was issued by a company that is not trusted.";
                } else {
                    caption = StringUtils.format(
                            "The digital signature of {0} has been verified.", 
                            description);
                    
                    // Same details for both
                    body += "The security certificate was issued by a company that is trusted.";
                }
                
                // now check if time of signing is valid.
                if (timeIsNotValid) {
                    // if no warnings yet, add the one that will show with the
                    // bullet in security warning dialog:
                    body += "The digital signature was generated with a trusted certificate but has expired or is not yet valid";
                } else {
                    // for timestamp info, add a message saying that certificate
                    // was valid at the time of signing
                    if (timestamp != null) {
                        // get the right date format for timestamp
                        final DateFormat df = DateFormat.getDateTimeInstance(
                                DateFormat.LONG,
                                DateFormat.LONG);
                        body += StringUtils.format(
                                "The security certificate was valid at the time of signing on {0}.",
                                df.format(timestamp));
                    } else {
                        body += "The security certificate has not expired and is still valid.";
                    }
                }
            }
            
            
            String message = StringUtils.format("<html><b>{0}</b><br>Subject: {1}<br>Issuer: {2}<br><br>{3}<br><br>Click OK to accept the certificate permanently, No to accept it temporary for this session, Cancel to reject the certificate.", caption, subjectName, issuerName, body);
            
            int option = JOptionPane.showConfirmDialog(null, message);
            if (option == JOptionPane.OK_OPTION) {
                return CertificateAcceptanceStatus.ACCEPT_PERMANENTLY;
            } else {
                return CertificateAcceptanceStatus.DENY;
            }
        }
        
        return CertificateAcceptanceStatus.DENY;
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private static String extractName(String nameString, String prefix, String defaultValue) {
        int i = nameString.indexOf(prefix);
        int j = 0;
        
        if (i < 0) {
            return defaultValue;
        } else {
            try {
                // shift to the beginning of the prefix text
                i = i + prefix.length();
                
                // check if it begins with a quote
                if (nameString.charAt(i) == '\"') {
                    // skip the quote
                    i = i + 1;
                    
                    // search for another quote
                    j = nameString.indexOf('\"', i);
                } else {
                    
                    // no quote, so search for comma
                    j = nameString.indexOf(',', i);
                }
                
                if (j < 0) {
                    return nameString.substring(i);
                } else {
                    return nameString.substring(i, j);
                }
            } catch (IndexOutOfBoundsException e) {
                return defaultValue;
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private UiUtils() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static enum CertificateAcceptanceStatus {
        ACCEPT_PERMANENTLY,
        ACCEPT_FOR_THIS_SESSION,
        DENY
    }
}
