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
package org.netbeans.modules.xsl.cookies;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.StreamSource;

import org.openide.filesystems.FileStateInvalidException;

import org.netbeans.api.xml.cookies.*;
import org.netbeans.spi.xml.cookies.*;

/**
 * Validates XSL transformation
 * @author Asgeir Orn Asgeirsson
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
            sendMessage(Util.THIS.getString("MSG_checking", checkedFile));

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
                String msg = Util.THIS.getString("MSG_too_many_errs");
                sendMessage(msg);
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
