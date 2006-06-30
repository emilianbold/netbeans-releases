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

package org.netbeans.spi.xml.cookies;

import javax.xml.transform.*;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.netbeans.api.xml.cookies.XMLProcessorDetail;

/**
 * Default XML processor observer message implementation.
 * It supports direct wrapping of {@link SAXParseException}s and
 * {@link TransformerException}s.
 *
 * @author      Petr Kuzel
 * @since       0.5
 */
public class DefaultXMLProcessorDetail extends XMLProcessorDetail {

    private int columnNumber;

    private int lineNumber;

    private String publicId;

    private String systemId;

    private Exception exception;

    
    /**
     * Create new DefaultXMLProcessorMessage based on SAXParseException.
     * @param spex SAX exception to be wrapped (never <code>null</code>).
     */
    public DefaultXMLProcessorDetail(SAXParseException spex) {
        if (spex == null) throw new NullPointerException();

        this.exception = spex;
        this.columnNumber = spex.getColumnNumber();
        this.lineNumber = spex.getLineNumber();
        this.publicId = spex.getPublicId();
        this.systemId = spex.getSystemId();

        if ( Util.THIS.isLoggable() ) /* then */ {
            Util.THIS.debug ("DefaultXMLProcessorDetail: SAXParseException");
            Util.THIS.debug ("    exception= " + exception);
            Util.THIS.debug ("    columnNumber= " + columnNumber);
            Util.THIS.debug ("    lineNumber= " + lineNumber);
            Util.THIS.debug ("    publicId= " + publicId);
            Util.THIS.debug ("    systemId= " + systemId);
        }
    }

    /**
     * Create new DefaultXMLProcessorMessage based on TransformerException.
     * @param trex TrAX exception to be wrapped (never <code>null</code>).
     */
    public DefaultXMLProcessorDetail(TransformerException trex) {
        if (trex == null) throw new NullPointerException();

        this.exception = trex;

        // some locators disapper immediately we must sample it now!
        
        SourceLocator locator = trex.getLocator();
        if (locator != null) {
            this.columnNumber = locator.getColumnNumber();
            this.lineNumber = locator.getLineNumber();
            this.publicId = locator.getPublicId();
            this.systemId = locator.getSystemId();

            // 
            if (lineNumber == -1) {
                tryWrappedLocator(trex);
            }
        } else {

            // default
            this.columnNumber = -1;
            this.lineNumber = -1;
            this.publicId = null;
            this.systemId = null;

            // may be better
            tryWrappedLocator(trex);
        }

        if ( Util.THIS.isLoggable() ) /* then */ {
            Util.THIS.debug ("DefaultXMLProcessorDetail: TransformerException");
            Util.THIS.debug ("    exception= " + exception);
            Util.THIS.debug ("    columnNumber= " + columnNumber);
            Util.THIS.debug ("    lineNumber= " + lineNumber);
            Util.THIS.debug ("    publicId= " + publicId);
            Util.THIS.debug ("    systemId= " + systemId);
        }
    }

    // use location information from wrapped exception or do nothing
    private void tryWrappedLocator(Exception ex) {
        if ( Util.THIS.isLoggable() ) /* then */ {
            Util.THIS.debug ("DefaultXMLProcessorDetail.tryWrappedLocator: " + ex);
        }

        // I saw SAXException wrapped in TransformerException and vice versa

        Throwable wrapped = null;
        if (ex instanceof TransformerException) {
            wrapped = ((TransformerException) ex).getException();
        } else if (ex instanceof SAXException) {
            wrapped = ((SAXException) ex).getException();
        } else {
            return;
        }

        // look if wrapped exception does not provide location info

        if (wrapped instanceof SAXParseException) {
            SAXParseException pex = (SAXParseException) wrapped;
            if (pex.getLineNumber() == -1) {
                tryWrappedLocator(pex);
            } else {
                this.columnNumber = pex.getColumnNumber();
                this.lineNumber = pex.getLineNumber();
                this.publicId = pex.getPublicId();
                this.systemId = pex.getSystemId();                    
            }
        } else if (wrapped instanceof TransformerException) {
            TransformerException wrappedTransformerEx = 
                (TransformerException) wrapped;
            SourceLocator locator = wrappedTransformerEx.getLocator();
            if (locator == null) {
                tryWrappedLocator(wrappedTransformerEx);
            } else {
                if (locator.getLineNumber() == -1) {
                    tryWrappedLocator(wrappedTransformerEx);
                } else {
                    this.columnNumber = locator.getColumnNumber();
                    this.lineNumber = locator.getLineNumber();
                    this.publicId = locator.getPublicId();
                    this.systemId = locator.getSystemId();                        
                }
            }
        } else if (wrapped instanceof SAXException) {
            tryWrappedLocator((SAXException)wrapped);
        }
    }
                                
    public int getColumnNumber() {
        return columnNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getPublicId() {
        return publicId;
    }

    public String getSystemId() {
        return systemId;
    }

    public Exception getException() {
        return exception;
    }

}
