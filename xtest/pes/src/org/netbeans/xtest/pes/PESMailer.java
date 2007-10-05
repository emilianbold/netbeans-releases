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
 * PESMailer.java
 *
 * Created on June 18, 2002, 4:38 PM
 */

package org.netbeans.xtest.pes;


import org.apache.tools.mail.*;
import java.io.*;

/**
 *
 * @author  mb115822
 */
public class PESMailer {

    private String smtpHost = "localhost";
    private String fromAddress;
    private String toAddress;



    public PESMailer() {
    }




    public void setSMTPHost(String host) {
        if (host == null) {
            smtpHost = "localhost";
        } else {
            smtpHost = host;
        }
    }
    
    public String getSMTPHost() {
        return smtpHost;
    }
    
    public void setFromAddress(String addrs) {        
        fromAddress = addrs;
    }
    
    public  String getFromAddress() {
        return fromAddress;
    }
    
    public  void setToAddress(String addrs) {        
        toAddress = addrs;
    }
    
    public  String getToAddress() {
        return toAddress;
    }
    
    /*
    public static boolean readyToMail() {
        if ((smtpHost == null)|(fromAddress == null)|(toAddress == null)) {
            return false;
        } else {
            return true;
        }
    }
    */
    
    public void send(Message aMessage) throws IOException {
        String fromAddress = getFromAddress();
        String toAddress = getToAddress();        
        String subject = "no subject";
        String message = "";
        
        if (aMessage.getFromAddress() != null) {
            fromAddress = aMessage.getFromAddress();
        }
        
        if (aMessage.getToAddress() != null) {
            toAddress = aMessage.getToAddress();
        }
        
        if (aMessage.getSubject() != null) {
            subject = aMessage.getSubject();
        }
        
        if (aMessage.getMessage() != null) {
            message = aMessage.getMessage();
        }
        
        // send the email
        MailMessage mail = new MailMessage(getSMTPHost());
        mail.from(fromAddress);
        mail.to(toAddress);
        mail.setSubject(subject);
        PrintStream out = mail.getPrintStream();
        out.print(message);
        PESLogger.logger.fine("Sending message with subject "+subject+" to "+toAddress);
        mail.sendAndClose();        
    }
    

    
    public static class Message {
        private String fromAddress;
        private String toAddress;
        private String subject;
        private String message;
        
        /** Getter for property fromAddress.
         * @return Value of property fromAddress.
         *
         */
        public java.lang.String getFromAddress() {
            return fromAddress;
        }
        
        /** Setter for property fromAddress.
         * @param fromAddress New value of property fromAddress.
         *
         */
        public void setFromAddress(java.lang.String fromAddress) {
            this.fromAddress = fromAddress;
        }
        
        /** Getter for property toAddress.
         * @return Value of property toAddress.
         *
         */
        public java.lang.String getToAddress() {
            return toAddress;
        }
        
        /** Setter for property toAddress.
         * @param toAddress New value of property toAddress.
         *
         */
        public void setToAddress(java.lang.String toAddress) {
            this.toAddress = toAddress;
        }
        
        /** Getter for property subject.
         * @return Value of property subject.
         *
         */
        public java.lang.String getSubject() {
            return subject;
        }
        
        /** Setter for property subject.
         * @param subject New value of property subject.
         *
         */
        public void setSubject(java.lang.String subject) {
            this.subject = subject;
        }
        
        /** Getter for property message.
         * @return Value of property message.
         *
         */
        public java.lang.String getMessage() {
            return message;
        }
        
        /** Setter for property message.
         * @param message New value of property message.
         *
         */
        public void setMessage(java.lang.String message) {
            this.message = message;
        }
        
    }
    
}
