/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Mail.java
 *
 * Created on June 18, 2002, 5:17 PM
 */

package org.netbeans.xtest.pes.xmlbeans;

import org.netbeans.xtest.pe.xmlbeans.XMLBean;
import org.netbeans.xtest.pes.PESMailer;

/**
 *
 * @author  mb115822
 */
public class Mail extends XMLBean {
    
    /** Creates a new instance of Mail */
    public Mail() {
    }
    
    public PESMailer mailer = null;
    
    // mail required values
    public String xmlat_smtpHost;
    public String xmlat_from;
    public String xmlat_to;
    
    // default logging level
    private static final String DEFAULT_LOGGING_LEVEL = "SEVERE";
    // which logging messages should be sent by email
    public String xmlat_loggingLevel = DEFAULT_LOGGING_LEVEL;
    
    
    
    // return true only if valid
    public void checkValidity() throws PESConfigurationException {
        if (xmlat_smtpHost == null) throw new PESConfigurationException("PESConfig: Mail: no smtpHost specified");
        if (xmlat_from == null) throw new PESConfigurationException("PESConfig: Mail: no from address specified");
        if (xmlat_to == null) throw new PESConfigurationException("PESConfig: Mail: no to address specified");
    }
    
    public void finishInitialization() {
        mailer = new PESMailer();
        try {
            mailer.setSMTPHost(xmlat_smtpHost);
            mailer.setFromAddress(xmlat_from);
            mailer.setToAddress(xmlat_to);
        } catch (IllegalArgumentException iae) {
            // mailer is not valid -- set it to null
            mailer = null;
        }
    }
    
    public PESMailer getPESMailer() {
        return mailer;
    }
    
    public String getLoggingLevel() {
        if (xmlat_loggingLevel != null) {
            return xmlat_loggingLevel;
        }
        return DEFAULT_LOGGING_LEVEL;
    }
    
}
