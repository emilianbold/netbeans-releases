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

package org.netbeans.modules.wsdlextensions.email.impl.imap;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.email.imap.IMAPComponent;
import org.netbeans.modules.wsdlextensions.email.imap.IMAPInput;
import org.netbeans.modules.wsdlextensions.email.imap.IMAPQName;
import org.netbeans.modules.wsdlextensions.email.impl.EMAILAttribute;
import org.w3c.dom.Element;

/**
 * @author Sainath Adiraju
 */
public class IMAPInputImpl extends IMAPComponentImpl implements IMAPInput {
    
    public IMAPInputImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public IMAPInputImpl(WSDLModel model){
        this(model, createPrefixedElement(IMAPQName.INPUT.getQName(), model));
    }
    
    public void accept(IMAPComponent.Visitor visitor) {
        visitor.visit(this);
    }
  
    public String getMessage() {
		 return getAttribute(EMAILAttribute.EMAIL_MESSAGE_NAME);
	 }

     public void setMessage(String message) {
		 setAttribute(IMAPInput.EMAIL_MESSAGE, EMAILAttribute.EMAIL_MESSAGE_NAME, message);
     }
 
     public String getSubject() {
		 return getAttribute(EMAILAttribute.EMAIL_SUBJECT_NAME);
	 }

     public void setSubject(String subject) {
		 setAttribute(IMAPInput.EMAIL_SUBJECT, EMAILAttribute.EMAIL_SUBJECT_NAME, subject);
     }

	 public String getFrom() {
		 return getAttribute(EMAILAttribute.EMAIL_FROM_NAME);
	 }

     public void setFrom(String from) {
		 setAttribute(IMAPInput.EMAIL_FROM, EMAILAttribute.EMAIL_FROM_NAME, from);
     }

     public String getCharset() {
		 return getAttribute(EMAILAttribute.EMAIL_CHARSET_NAME);
	 }

     public void setCharset(String val) {
		 setAttribute(IMAPInput.EMAIL_CHAR_SET, EMAILAttribute.EMAIL_CHARSET_NAME, val);
     }
     
     public void setTo(String to){
        setAttribute(IMAPInput.EMAIL_TO, EMAILAttribute.EMAIL_TO_NAME, to);
     }

     public String getTo(){
        return getAttribute(EMAILAttribute.EMAIL_TO_NAME);
     }
     
     public void setCc(String cc){
        setAttribute(IMAPInput.EMAIL_CC, EMAILAttribute.EMAIL_CC_NAME, cc);
     }

     public String getCc(){
        return getAttribute(EMAILAttribute.EMAIL_CC_NAME);
     }
     
     public void setBcc(String bcc){
        setAttribute(IMAPInput.EMAIL_BCC, EMAILAttribute.EMAIL_BCC_NAME, bcc);
     }

     public String getBcc(){
        return getAttribute(EMAILAttribute.EMAIL_BCC_NAME);
     }
     
      public String getUse(){
        return getAttribute(EMAILAttribute.EMAIL_USE);
     }
      
     public void setUse(String val){
        setAttribute(IMAPInput.EMAIL_USE, EMAILAttribute.EMAIL_USE, val);
     }
     
     public String getEncodingStyle(){
        return getAttribute(EMAILAttribute.EMAIL_ENCODING_STYLE);
     }
      
     public void setEncodingStyle(String val){
        setAttribute(IMAPInput.EMAIL_ENCODINGSTYLE, EMAILAttribute.EMAIL_ENCODING_STYLE, val);
     }

     public String getNewsgroups(){
        return getAttribute(EMAILAttribute.EMAIL_NEWSGROUP);
     }

     public void setNewsgroups(String newsGroup){
        setAttribute(IMAPInput.EMAIL_NEWSGROUP, EMAILAttribute.EMAIL_NEWSGROUP, newsGroup);
     }

     public boolean getHandleNMAttachments(){
        String s = getAttribute(EMAILAttribute.EMAIL_HANDLE_NM_ATT);
        return s!=null && Boolean.parseBoolean(s);
     }

     public void setHandleNMAttachments(boolean val){
        setAttribute(IMAPInput.EMAIL_HANDLENMATT, EMAILAttribute.EMAIL_HANDLE_NM_ATT, val);
     }

     public String getSaveAttachmentsToDir(){
        return getAttribute(EMAILAttribute.EMAIL_SAVE_ATT_DIR);
     }

     public void setSaveAttachmentsToDir(String val){
        setAttribute(IMAPInput.EMAIL_SAVEATTDIR, EMAILAttribute.EMAIL_SAVE_ATT_DIR, val);
     }
     
}
