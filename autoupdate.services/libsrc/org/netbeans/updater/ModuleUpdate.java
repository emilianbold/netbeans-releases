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

package org.netbeans.updater;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.*;
import java.util.jar.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

/** This class represents one module update available on the web
 *
 * @author  Ales Kemr
 * @version
 */
class ModuleUpdate extends Object {

    // Constants
    private static final String ATTR_CODENAMEBASE = "codenamebase"; // NOI18N

    /** Holds value of property codenamebase. */
    private String codenamebase = null;
    /** Holds value of sv */
    private String specification_version = null;
    
    private boolean pError = false;

    private boolean l10n = false;
    private boolean fromInstall = false;
    
    /** Creates new ModuleUpdate for downloaded .nbm file */
    ModuleUpdate( File nbmFile, boolean fromInstall ) {
        this.fromInstall = fromInstall;
        createFromDistribution( nbmFile );
    }

    /** Creates module from downloaded .nbm file */
    private void createFromDistribution( File nbmFile ) {

        Document document = null;
        Node node = null;
        Element documentElement = null;
        
        // Try to parse the info file
        JarFile jf = null;
        InputStream is = null;
        boolean exit = false;
        String errorMessage = null;
        try {
            jf = new JarFile(nbmFile);
            is = jf.getInputStream(jf.getEntry("Info/info.xml"));  // NOI18N
            
            InputSource xmlInputSource = new InputSource( is );
            document = XMLUtil.parse( xmlInputSource, false, false, new ErrorCatcher(), XMLUtil.createAUResolver() );
            
            documentElement = document.getDocumentElement();
            node = documentElement;
            if (is != null)
                is.close();
        }
        catch ( org.xml.sax.SAXException e ) {
            errorMessage = "Bad info : " + nbmFile.getAbsolutePath (); // NOI18N
            System.out.println(errorMessage);
            e.printStackTrace ();
            exit = true;
        }            
        catch ( java.io.IOException e ) {
            errorMessage = "Missing info : " + nbmFile.getAbsolutePath (); // NOI18N
            System.out.println(errorMessage);
            e.printStackTrace ();
            exit = true;
        }
        finally {
            try {
                if (is != null)
                    is.close();
                if (jf != null)
                    jf.close();
            } catch ( IOException ioe ) {
                ioe.printStackTrace();
                exit = true;
            }
        }
        
        if (exit) {
            throw new RuntimeException (errorMessage);
        }

        setCodenamebase( getAttribute( node, ATTR_CODENAMEBASE ) );
        NodeList nodeList = ((Element)node).getElementsByTagName( "l10n" ); // NOI18N

        if ( nodeList.getLength() > 0 ) {
            l10n = true;
            Node n = nodeList.item( 0 );            
            setSpecification_version( getAttribute( n, "module_spec_version" ) );
        } else {
            nodeList = ((Element)node).getElementsByTagName( "manifest" ); // NOI18N

            for( int i = 0; i < nodeList.getLength(); i++ ) {

                if ( nodeList.item( i ).getNodeType() != Node.ELEMENT_NODE ||
                        !( nodeList.item( i ) instanceof Element ) ) {
                    break;
                }

                // ((Element)nodeList.item( i )).normalize();
                NamedNodeMap attrList = nodeList.item( i ).getAttributes();            
                for( int j = 0; j < attrList.getLength(); j++ ) {
                    Attr attr = (Attr) attrList.item( j );
                    if (attr.getName().equals("OpenIDE-Module"))  // NOI18N
                        setCodenamebase(attr.getValue());
                    else if (attr.getName().equals("OpenIDE-Module-Specification-Version"))  // NOI18N
                        setSpecification_version(attr.getValue());
                }
            }
        }
    }
    
    private String getAttribute(Node n, String attribute) {
        Node attr = n.getAttributes().getNamedItem( attribute );
        return attr == null ? null : attr.getNodeValue();
    }

    /** Getter for property codeNameBase.
     *@return Value of property codeNameBase.
     */
    String getCodenamebase() {
        return codenamebase;
    }

    /** Setter for property Codenamebase.
     *@param manufacturer New value of property Codenamebase.
     */
    void setCodenamebase(String codenamebase) {
        this.codenamebase = codenamebase;
    }

    /** Getter for property specification_version.
     *@return Value of property specification_version.
     */
    String getSpecification_version() {
        return specification_version;
    }

    /** Setter for property specification_version.
     *@param notification New value of property specification_version.
     */
    void setSpecification_version(String specification_version) {        
        this.specification_version = specification_version;        
    }
    
    /** Getter for property l10n.
     * @return Value of property l10n.
     *
     */
    public boolean isL10n() {
        return l10n;
    }
    
    /** Getter for property fromInstall.
     * @return Value of property fromInstall.
     *
     */
    public boolean isFromInstall() {
        return fromInstall;
    }
    
    class ErrorCatcher implements org.xml.sax.ErrorHandler {
        private void message (String level, org.xml.sax.SAXParseException e) {
            pError = true;
        }

        public void error (org.xml.sax.SAXParseException e) {
            // normally a validity error
            pError = true;
        }

        public void warning (org.xml.sax.SAXParseException e) {
            //parseFailed = true;
        }

        public void fatalError (org.xml.sax.SAXParseException e) {
            pError = true;
        }
    } //end of inner class ErrorPrinter
    
}
