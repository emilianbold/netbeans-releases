/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.core.compatibility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.MalformedURLException;

import java.security.CodeSource;
import java.security.ProtectionDomain;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.api.xml.cookies.CookieMessage;
import org.netbeans.api.xml.cookies.CookieObserver;
import org.netbeans.api.xml.cookies.TransformableCookie;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.spi.xml.cookies.DefaultXMLProcessorDetail;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * Transformation utilities.
 * The class is mainly taken from the XSL Support module.
 *
 * @author  Nikita Krjukov
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TransformUtil {

    private static final String SAX_FEATURES_NAMESPACES = "http://xml.org/sax/features/namespaces"; // NOI18N

    public static final String DEFAULT_OUTPUT_EXT = "html";
    private static TransformerFactory transformerFactory;
    private static SAXParserFactory saxParserFactory;


    /**
     * Applies specified XSL transformation to the specified file.
     * A backup file will be created in case the backupExt parameter
     * is specified (not null and not empty).
     * The specified extension will be appended to the original file name
     * in order to create the backup file name.
     * For example if backupExt is "old" then test.bpel --> test.bpel.old
     * If a file with the same name exists as backup is going to have,
     * then a counter number is added to the end: old1, old2, ...
     *
     * @param sourceFo
     * @param xslFile
     * @param backupExt
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     * @throws javax.xml.transform.TransformerException
     */
    public static void applyXslTransform(FileObject sourceFo, String xslFile, String backupExt)
            throws FileNotFoundException, IOException, TransformerException {
        //
        boolean keepBackup = (backupExt != null) && (backupExt.length() != 0);
        if (!keepBackup) {
            // specifies temporary file extension
            backupExt = "temp"; // NOI18N
        }
        //
        // Check if exists a file with the same name as the backup file is going to have.
        FileObject parentFolder = sourceFo.getParent();
        String backupCandidate = backupExt;
        int counter = 1;
        while (parentFolder.getFileObject(sourceFo.getNameExt(), backupCandidate) != null) {
            backupCandidate = backupExt + counter;
            counter++;
        }
        //
        // Make copy of the existing bpel file
        FileObject backUpFo = sourceFo.copy(
                parentFolder, sourceFo.getNameExt(), backupCandidate);
        //
        // Prepare to transformation
        InputStream xslInStream = CheckCompatibilityAction.class.
                getResourceAsStream(xslFile);
        InputStream sourceIS = backUpFo.getInputStream();
        OutputStream resultOS = sourceFo.getOutputStream();
        //
        try {
            StreamSource xslSource = new StreamSource(xslInStream);
            StreamSource xmlSource = new StreamSource(sourceIS);
            StreamResult output = new StreamResult(resultOS);
            //
            // Do transformation
            TransformUtil.transform(xmlSource, null, xslSource, output, null);
        } finally {
            xslInStream.close();
            sourceIS.close();
            resultOS.close();
        }
        //
        // Delete temporary file if it is not necessary
        if (!keepBackup) {
            backUpFo.delete();
        }
    }

    public static String getURLName (FileObject fileObject) throws MalformedURLException, FileStateInvalidException {
        URL fileURL = null;
        File file = FileUtil.toFile (fileObject);

        if ( file != null ) {
//            if ( Util.THIS.isLoggable() ) /* then */ {
//                try {
//                    Util.THIS.debug ("[TransformUtil.getURLName]");
//                    Util.THIS.debug ("    file = " + file);
//                    Util.THIS.debug ("    file.getCanonicalPath = " + file.getCanonicalPath());
//                    Util.THIS.debug ("    file.getAbsolutePath  = " + file.getAbsolutePath());
//                    Util.THIS.debug ("    file.toString  = " + file.toString());
//                    Util.THIS.debug ("    file.toURL  = " + file.toURL());
//                } catch (Exception exc) {
//                    Util.THIS.debug ("DEBUG Exception", exc);
//                }
//            }

            fileURL = file.toURL();
        } else {
            fileURL = fileObject.getURL();
        }

        return fileURL.toExternalForm();
    }

    public static URL createURL (URL baseURL, String fileName) throws MalformedURLException, FileStateInvalidException {
//        if ( Util.THIS.isLoggable() ) /* then */ {
//            Util.THIS.debug ("TransformUtil.createURL:");
//            Util.THIS.debug ("    baseURL = " + baseURL);
//            Util.THIS.debug ("    fileName = " + fileName);
//        }

        URL url = new URL (baseURL, fileName);

//        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    return URL = " + url);

        return url;
    }

    public static Source createSource (URL baseURL, String fileName) throws IOException, MalformedURLException, FileStateInvalidException, ParserConfigurationException, SAXException {
        URL url = createURL (baseURL, fileName);
        // test right url
        InputStream is = url.openStream();
        is.close();

        XMLReader reader = TransformUtil.newXMLReader();

//        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TransformUtil.createSource: XMLReader (http://xml.org/sax/features/namespaces) : "
//                                                                  +  reader.getFeature (SAX_FEATURES_NAMESPACES));

        reader.setEntityResolver (TransformUtil.getEntityResolver());
        Source source = new SAXSource (reader, new InputSource (url.toExternalForm()));

        return source;
    }

    public static URIResolver getURIResolver () {
        UserCatalog catalog = UserCatalog.getDefault();
        URIResolver res = (catalog == null ? null : catalog.getURIResolver());
        return res;
    }

    public static EntityResolver getEntityResolver () {
        UserCatalog catalog = UserCatalog.getDefault();
        EntityResolver res = (catalog == null ? null : catalog.getEntityResolver());
        return res;
    }


    private static TransformerFactory getTransformerFactory () {
        if ( transformerFactory == null ) {
            transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setURIResolver (getURIResolver()); //!!! maybe that it should be set every call if UsersCatalog instances are dynamic
        }
        return transformerFactory;
    }

    private static SAXParserFactory getSAXParserFactory () throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        if ( saxParserFactory == null ) {
            saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setFeature (SAX_FEATURES_NAMESPACES, true);
        }
        return saxParserFactory;
    }


    public static Transformer newTransformer (Source xsl) throws TransformerConfigurationException {
        Transformer transformer = getTransformerFactory().newTransformer (xsl);

//        if ( Util.THIS.isLoggable() ) /* then */ transformer.setParameter ("transformer", xsl); // debug

        return transformer;
    }

    public static XMLReader newXMLReader () throws ParserConfigurationException, SAXException {
        SAXParser parser = getSAXParserFactory().newSAXParser();
        return parser.getXMLReader();
    }


    /*  @return associated stylesheet or <code>null</code>.
     */
    public static Source getAssociatedStylesheet (URL baseURL) {
//        if ( Util.THIS.isLoggable() ) /* then */ {
//            Util.THIS.debug ("TransformUtil.getAssociatedStylesheet:");
//            Util.THIS.debug ("    baseURL = " + baseURL);
//        }

        Source xml_stylesheet = null;

        try {
            XMLReader reader = newXMLReader();
            reader.setEntityResolver (getEntityResolver());
            SAXSource source = new SAXSource (reader, new InputSource (baseURL.toExternalForm()));

            xml_stylesheet = getTransformerFactory().getAssociatedStylesheet (source, null, null, null);

//            if ( Util.THIS.isLoggable() ) /* then */ {
//                Util.THIS.debug ("    source = " + source.getSystemId());
//                Util.THIS.debug ("    xml_stylesheet = " + xml_stylesheet);
//            }
        } catch (Exception exc) { // ParserConfigurationException, SAXException, TransformerConfigurationException
            // ignore it
//            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TransformUtil.getAssociatedStylesheet: !!!", exc); // debug
        }

        return xml_stylesheet;
    }

    public static String guessOutputExt (Source source) {
        String ext = DEFAULT_OUTPUT_EXT;

        try {
            Transformer transformer = newTransformer (source);
            String method = transformer.getOutputProperty (OutputKeys.METHOD);

//            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[TransformUtil] guessOutputExt: method = " + method);

            if ( "text".equals (method) ) { // NOI18N
                ext = "txt"; // NOI18N
            } else if ( method != null ) {
                ext = method;
            }
        } catch (Exception exc) {
            // ignore it

//            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug (exc);
        }

//        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[TransformUtil] guessOutputExt: extension = " + ext);

        return ext;
    }

    /**
     * @throws TransformerException it indicates
     */
    public static void transform (Source xml, TransformableCookie transformable, Source xsl, Result output, CookieObserver notifier) throws TransformerException {
//        if ( Util.THIS.isLoggable() ) /* then */ {
//            Util.THIS.debug ("TransformUtil.transform");
//            Util.THIS.debug ("    XML source = " + xml.getSystemId());
//            Util.THIS.debug ("    TransformableCookie = " + transformable);
//            Util.THIS.debug ("    XSL source = " + xsl.getSystemId());
//            Util.THIS.debug ("    Output Result = " + output.getSystemId());
//            Util.THIS.debug ("    CookieObserver = " + notifier);
//        }

        if ( transformable != null ) {

            transformable.transform (xsl, output, notifier);

        } else {

            try {
                Transformer transformer = TransformUtil.newTransformer (xsl);

                if (notifier != null) {

                    // inform user about used implementation

                    ProtectionDomain domain = transformer.getClass().getProtectionDomain();
                    CodeSource codeSource = domain.getCodeSource();
                    if (codeSource == null) {
                        notifier.receive(new CookieMessage(NbBundle.getMessage(TransformUtil.class, "BK000", transformer.getClass().getName())));
                    } else {
                        URL location = codeSource.getLocation();
                        notifier.receive(new CookieMessage(NbBundle.getMessage(TransformUtil.class, "BK001", location, transformer.getClass().getName())));
                    }

                    Proxy proxy = new Proxy (notifier);
                    transformer.setErrorListener (proxy);
                }

                //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\n==> transform: param [transformer] = " + transformer.getParameter ("transformer")); // debug

                transformer.transform (xml, output);

            } catch (Exception exc) { // TransformerException, ParserConfigurationException, SAXException, FileStateInvalidException
//                if ( Util.THIS.isLoggable() ) /* then */ {
//                    Util.THIS.debug ("    EXCEPTION during transformation: " + exc.getClass().getName(), exc);
//                    Util.THIS.debug ("    exception's message = " + exc.getLocalizedMessage());
//
//                    Throwable tempExc = unwrapException (exc);
//                    Util.THIS.debug ("    wrapped exception = " + tempExc.getLocalizedMessage());
//                }

                TransformerException transExcept = null;
                Object detail = null;

                if ( exc instanceof TransformerException ) {
                    transExcept = (TransformerException)exc;
                    if ( ( notifier != null ) &&
                         ( exc instanceof TransformerConfigurationException ) ) {
                        detail = new DefaultXMLProcessorDetail (transExcept);
                    }
                } else if ( exc instanceof SAXParseException ) {
                    transExcept = new TransformerException (exc);
                    if ( notifier != null ) {
                        detail = new DefaultXMLProcessorDetail ((SAXParseException)exc);
                    }
                } else {
                    transExcept = new TransformerException (exc);
                    if ( notifier != null ) {
                        detail = new DefaultXMLProcessorDetail (transExcept);
                    }
                }

                if ( ( notifier != null ) &&
                     ( detail != null ) ) {
                    CookieMessage message = new CookieMessage
                        (unwrapException(exc).getLocalizedMessage(),
                         CookieMessage.FATAL_ERROR_LEVEL,
                         detail);
                    notifier.receive (message);
                }

//                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("--> throw transExcept: " + transExcept);

                throw transExcept;
            }
        }
    }

    /** Unwrap wrapped cause exception.
     */
    public static Throwable unwrapException (Throwable exc) {
        Throwable wrapped = null;
        if (exc instanceof TransformerException) {
            wrapped = ((TransformerException) exc).getException();
        } else if (exc instanceof SAXException) {
            wrapped = ((SAXException) exc).getException();
        } else {
            return exc;
        }

        if ( wrapped == null ) {
            return exc;
        }

        return unwrapException (wrapped);
    }


    //
    // class Proxy
    //

    private static class Proxy implements ErrorListener {

        private final CookieObserver peer;

        public Proxy (CookieObserver peer) {
            if (peer == null) {
                throw new NullPointerException();
            }
            this.peer = peer;
        }

        public void error (TransformerException tex) throws TransformerException {
            report (CookieMessage.ERROR_LEVEL, tex);
        }

        public void fatalError (TransformerException tex) throws TransformerException {
            report (CookieMessage.FATAL_ERROR_LEVEL, tex);

            throw tex;
        }

        public void warning (TransformerException tex) throws TransformerException {
            report (CookieMessage.WARNING_LEVEL, tex);
        }

        private void report (int level, TransformerException tex) throws TransformerException {
//            if ( Util.THIS.isLoggable() ) /* then */ {
//                Util.THIS.debug ("[TransformableSupport::Proxy]: report [" + level + "]: ", tex);
//                Util.THIS.debug ("    exception's message = " + tex.getLocalizedMessage());
//
//                Throwable tempExc = unwrapException (tex);
//                Util.THIS.debug ("    wrapped exception = " + tempExc.getLocalizedMessage());
//            }

            Throwable unwrappedExc = unwrapException (tex);
            CookieMessage message = new CookieMessage (
                unwrappedExc.getLocalizedMessage(),
                level,
                new DefaultXMLProcessorDetail (tex)
            );
            peer.receive (message);
        }

    } // class Proxy

}
