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
package org.netbeans.modules.xslt.core;

import org.xml.sax.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;

import org.netbeans.api.xml.cookies.*;
import org.netbeans.spi.xml.cookies.*;
import org.openide.util.NbBundle;

/**
 * Validates XSL transformation
 * @author asgeir@dimonsoftware.com
 */
public class ValidateXSLSupport implements ValidateXMLCookie {
    
    // associated input source
    private final InputSource inputSource;

    // it will viasualize our results
    private CookieObserver console;

    // fatal error counter
    private int fatalErrors;
    
    // error counter
    private int errors;

    /** Creates a new instance of ValidateXSLSupport */
    public ValidateXSLSupport(InputSource inputSource) {
        this.inputSource = inputSource;
    }
    
    // inherit JavaDoc
    public boolean validateXML(CookieObserver l) {
       try {
            console = l;

            int fatalErrors = 0;
            int errors = 0;

            String checkedFile = inputSource.getSystemId();
             
         
    
            sendMessage(NbBundle.getMessage(ValidateXSLSupport.class, "MSG_checking" , checkedFile));//NOI18N

            ErrorListener errorListener = new XslErrorListener();
            try {
                SAXTransformerFactory factory = (SAXTransformerFactory)TransformerFactory.newInstance();
                factory.setErrorListener(errorListener);
                TransformerHandler transformerHandler = factory.newTransformerHandler(new SAXSource(inputSource));
            } catch (TransformerException ex) {
                CookieMessage message = new CookieMessage(
                    ex.getLocalizedMessage(), 
                    CookieMessage.FATAL_ERROR_LEVEL,
                    new DefaultXMLProcessorDetail(ex)
                );
                sendMessage (message);
            }
            
            return errors == 0 && fatalErrors == 0;
        } finally {
            console = null;
        }
    }

    private void sendMessage(String message) {
        if (console != null) {
            console.receive(new CookieMessage(message));
        }
    }

    private void sendMessage (CookieMessage message) {
        if (console != null) {
            console.receive (message);
        }
    }


    //
    // class XslErrorListener
    //
    private class XslErrorListener implements ErrorListener {
        public void error(TransformerException ex) throws TransformerException{
            if (errors++ == getMaxErrorCount()) {
                sendMessage(NbBundle.getMessage(ValidateXSLSupport.class, "MSG_too_many_errs" ));//NOI18N
                throw ex; // stop the parser                
            } else {
                CookieMessage message = new CookieMessage(
                    ex.getLocalizedMessage(), 
                    CookieMessage.ERROR_LEVEL,
                    new DefaultXMLProcessorDetail(ex)
                );
                sendMessage (message);
            }
        }
    
        public void fatalError(TransformerException ex) throws TransformerException{
            fatalErrors++;
            CookieMessage message = new CookieMessage(
                ex.getLocalizedMessage(), 
                CookieMessage.FATAL_ERROR_LEVEL,
                new DefaultXMLProcessorDetail(ex)
            );
            sendMessage (message);
        }
        
        public void warning(TransformerException ex) throws TransformerException{
            CookieMessage message = new CookieMessage(
                ex.getLocalizedMessage(), 
                CookieMessage.WARNING_LEVEL,
                new DefaultXMLProcessorDetail(ex)
            );
            sendMessage (message);
        }
    
        private int getMaxErrorCount() {
            return 20;  //??? load from option
        }    
    } // class XslErrorListener
  
}
