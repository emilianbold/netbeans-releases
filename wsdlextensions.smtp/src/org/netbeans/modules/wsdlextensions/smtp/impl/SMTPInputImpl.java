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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.wsdlextensions.smtp.impl;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.smtp.SMTPInput;
import org.netbeans.modules.wsdlextensions.smtp.SMTPComponent;
import org.netbeans.modules.wsdlextensions.smtp.SMTPQName;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.w3c.dom.Element;

/**
 * @author Sainath Adiraju
 */
public class SMTPInputImpl extends SMTPComponentImpl implements SMTPInput {
    
    public SMTPInputImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SMTPInputImpl(WSDLModel model){
        this(model, createPrefixedElement(SMTPQName.INPUT.getQName(), model));
    }
    
    public void accept(SMTPComponent.Visitor visitor) {
        visitor.visit(this);
    }
    public String getMessageName() {
		 return getAttribute(SMTPAttribute.SMTP_MESSAGE_NAME);
	 }

     public void setMessageName() {
		 setAttribute(SMTPInput.SMTP_MESSAGE, SMTPAttribute.SMTP_MESSAGE_NAME, "message");
     }
 
     public String getSubjectName() {
		 return getAttribute(SMTPAttribute.SMTP_SUBJECT_NAME);
	 }

     public void setSubjectName() {
		 setAttribute(SMTPInput.SMTP_SUBJECT, SMTPAttribute.SMTP_SUBJECT_NAME, "subject");
     }

	 public String getFrom() {
		 return getAttribute(SMTPAttribute.SMTP_FROM_NAME);
	 }

     public void setFrom() {
		 setAttribute(SMTPInput.SMTP_FROM, SMTPAttribute.SMTP_FROM_NAME, "from");
     }

     public String getCharSet() {
		 return getAttribute(SMTPAttribute.SMTP_CHARSET_NAME);
	 }

     public void setCharSet() {
		 setAttribute(SMTPInput.SMTP_CHARSET, SMTPAttribute.SMTP_CHARSET_NAME, "charset");
     }
     
     public void setTo(){
                 setAttribute(SMTPInput.SMTP_TO, SMTPAttribute.SMTP_TO_NAME, "to");
     }

     public  String getTo(){
                 return getAttribute(SMTPAttribute.SMTP_TO_NAME);
     }
     
     public void setCc(){
                 setAttribute(SMTPInput.SMTP_CC, SMTPAttribute.SMTP_CC_NAME, "cc");
     }

     public  String getCc(){
                 return getAttribute(SMTPAttribute.SMTP_CC_NAME);
     }
     
     public void setBcc(){
                 setAttribute(SMTPInput.SMTP_BCC, SMTPAttribute.SMTP_BCC_NAME, "bcc");
     }

     public  String getBcc(){
                 return getAttribute(SMTPAttribute.SMTP_BCC_NAME);
     }
     
      public  String getUse(){
                 return getAttribute(SMTPAttribute.SMTP_USE_NAME);
     }
      
     public void setUse(){
                 setAttribute(SMTPInput.SMTP_USE, SMTPAttribute.SMTP_USE_NAME, "use");
     }
     
     public  String getEncodingStyle(){
                 return getAttribute(SMTPAttribute.SMTP_ENCODING_STYLE);
     }
      
     public void setEncodingStyle(){
                 setAttribute(SMTPInput.SMTP_ENCSTYLE, SMTPAttribute.SMTP_USE_NAME, "encodingStyle");
     }
     
}
