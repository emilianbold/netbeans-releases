/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
