/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.websvc.wsdl.config.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.*;

import org.openide.util.NbBundle;
import org.openide.filesystems.*;

import org.netbeans.modules.websvc.wsdl.config.ConfigurationProxy;

/**
 * Provides access to JAX-RPC configuration root (org.netbeans.modules.websvc.wsdl.config.api.Configuration)
 *
 * Copied/Inspired by DDProvider from web/ddapi
 *
 * @author Peter Williams
 */
public final class DDProvider {
    private static DDProvider ddProvider;
    private Map ddMap;
    private Map baseBeanMap;
    private Map errorMap;
    private FCA fileChangeListener;

    private DDProvider() {
        ddMap=new java.util.HashMap(5);
        baseBeanMap=new java.util.HashMap(5);
        errorMap=new java.util.HashMap(5);
        fileChangeListener = new FCA ();
    }

    /**
    * Accessor method for DDProvider singleton
    * @return DDProvider object
    */
    public static synchronized DDProvider getDefault() {
        if (ddProvider==null) {
            ddProvider = new DDProvider();
        }
        return ddProvider;
    }


    /**
     * Returns the root of configuration bean graph for given file object.
     * The method is useful for clients planning to read only the configuration
     * or to listen to the changes.
     * @param fo FileObject representing the ???-config.xml file
     * @return Configuration object - root of the configuration bean graph
     */
    public Configuration getDDRoot(FileObject fo) throws java.io.IOException {

        ConfigurationProxy configuration = getFromCache (fo);
        if (configuration!=null) {
            return configuration;
        }

        fo.addFileChangeListener(fileChangeListener);

        SAXParseException error = null;
        try {
            Configuration original = getOriginalFromCache (fo);
            if (original == null) {
                // preparsing
                error = parse(fo);
                original = createConfiguration(fo.getInputStream());
                baseBeanMap.put(fo, new WeakReference (original));
            } else {
                error = (SAXParseException) errorMap.get (fo);
            }
            configuration=new ConfigurationProxy(original);
            if (error!=null) {
                configuration.setStatus(Configuration.STATE_INVALID_PARSABLE);
                configuration.setError(error);
            }
        } catch (SAXException ex) {
            configuration = new ConfigurationProxy(null);
            configuration.setStatus(Configuration.STATE_INVALID_UNPARSABLE);
            if (ex instanceof SAXParseException) {
                configuration.setError((SAXParseException)ex);
            } else if ( ex.getException() instanceof SAXParseException) {
                configuration.setError((SAXParseException)ex.getException());
            }
        }
        ddMap.put(fo, new WeakReference (configuration));
        return configuration;
    }

    /**
     * Returns the root of deployment descriptor bean graph for given file object.
     * The method is useful for clients planning to modify the deployment descriptor.
     * Finally the {@link org.netbeans.modules.j2ee.dd.api.web.Configuration#write(org.openide.filesystems.FileObject)} should be used
     * for writing the changes.
     * @param fo FileObject representing the web.xml file
     * @return Configuration object - root of the deployment descriptor bean graph
     */
    public Configuration getDDRootCopy(FileObject fo) throws java.io.IOException {
        return (Configuration)getDDRoot(fo).clone();
    }

    private ConfigurationProxy getFromCache (FileObject fo) throws java.io.IOException {
        WeakReference wr = (WeakReference) ddMap.get(fo);
        if (wr == null) {
            return null;
        }
        ConfigurationProxy configuration = (ConfigurationProxy) wr.get ();
        if (configuration == null) {
            ddMap.remove (fo);
        }
        return configuration;
    }

    private Configuration getOriginalFromCache (FileObject fo) throws java.io.IOException {
        WeakReference wr = (WeakReference) baseBeanMap.get(fo);
        if (wr == null) {
            return null;
        }
        Configuration configuration = (Configuration) wr.get ();
        if (configuration == null) {
            baseBeanMap.remove (fo);
            errorMap.remove (fo);
        }
        return configuration;
    }

    /**
     * Returns the root of configuration bean graph for java.io.File object.
     *
     * @param f File representing the ???-config.xml file
     * @return Configuration object - root of the deployment descriptor bean graph
     */
    public Configuration getDDRoot(File f) throws IOException, SAXException {
        return createConfiguration(new FileInputStream(f));
    }

//    /**  Convenient method for getting the BaseBean object from CommonDDBean object.
//     * The j2eeserver module needs BaseBean to implement jsr88 API.
//     * This is a temporary workaround until the implementation of jsr88 moves into ddapi
//     * or the implementation in j2eeserver gets changed.
//     * @deprecated do not use - temporary workaround that exposes the schema2beans implementation
//     */
//    public org.netbeans.modules.schema2beans.BaseBean getBaseBean(org.netbeans.modules.websvc.wsdl.config.api.CommonDDBean bean) {
//        if (bean instanceof org.netbeans.modules.schema2beans.BaseBean) {
//            return (org.netbeans.modules.schema2beans.BaseBean)bean;
//        } else if (bean instanceof ConfigurationProxy) {
//            return (org.netbeans.modules.schema2beans.BaseBean) ((ConfigurationProxy)bean).getOriginal();
//        }
//        return null;
//    }

    private static Configuration createConfiguration(java.io.InputStream is) throws java.io.IOException, SAXException {
        try {
            return org.netbeans.modules.websvc.wsdl.config.impl.Configuration.createGraph(is);
        } catch (RuntimeException ex) {
            throw new SAXException (ex.getMessage());
        }
    }

    private static class DDResolver implements EntityResolver {
        static DDResolver resolver;
        static synchronized DDResolver getInstance() {
            if(resolver==null) {
                resolver=new DDResolver();
            }
            return resolver;
        }
        public InputSource resolveEntity (String publicId, String systemId) {
            String resource=null;
            // return a proper input source
            //
            // !PW for schema documents, EntityResolvers do not seem to ever be called.
            // so this entire object (DDResolver) may be unnecessary.
            if("http://java.sun.com/xml/ns/jax-rpc/ri/config".equals(systemId)) {
                resource="/org/netbeans/modules/websvc/wsdl/resources/jax-rpc-ri-config_1_1.xsd"; //NOI18N
            }
            if(resource==null) {
                return null;
            }
            java.net.URL url = this.getClass().getResource(resource);
            return new InputSource(url.toString());
        }
    }

    private static class ErrorHandler implements org.xml.sax.ErrorHandler {
        private int errorType=-1;
        SAXParseException error;

        public void warning(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
//            System.out.println("ERROR HANDLER (warning): " + sAXParseException.getMessage());
            if (errorType<0) {
                errorType=0;
                error=sAXParseException;
            }
            //throw sAXParseException;
        }
        public void error(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
//            System.out.println("ERROR HANDLER (error): " + sAXParseException.getMessage());
            if (errorType<1) {
                errorType=1;
                error=sAXParseException;
            }
            //throw sAXParseException;
        }
        public void fatalError(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
//            System.out.println("ERROR HANDLER (fatal): " + sAXParseException.getMessage());
            errorType=2;
            throw sAXParseException;
        }

        public int getErrorType() {
            return errorType;
        }
        public SAXParseException getError() {
            return error;
        }
    }
    
    public SAXParseException parse (FileObject fo)
            throws org.xml.sax.SAXException, java.io.IOException {
        DDProvider.ErrorHandler errorHandler = new DDProvider.ErrorHandler();
        try {
            SAXParser parser = createSAXParserFactory().newSAXParser();
            XMLReader reader = parser.getXMLReader();
            reader.setErrorHandler(errorHandler);
            reader.setEntityResolver(DDProvider.DDResolver.getInstance());

            reader.parse(new InputSource(fo.getInputStream()));
            SAXParseException error = errorHandler.getError();
            if (error!=null) {
                return error;
            }
        } catch (ParserConfigurationException ex) {
            throw new org.xml.sax.SAXException(ex.getMessage());
        } catch (SAXException ex) {
            throw ex;
        }
        return null;
    }
    
    /** Method that retrieves SAXParserFactory to get the parser prepared to validate against XML schema
     */
    private static SAXParserFactory createSAXParserFactory() throws ParserConfigurationException {
        try {
            SAXParserFactory fact = SAXParserFactory.newInstance();
            if (fact!=null) {
                try {
                    fact.getClass().getMethod("getSchema", new Class[]{}); //NOI18N
                    return fact;
                } catch (NoSuchMethodException ex) {}
            }
            return (SAXParserFactory) Class.forName("org.apache.xerces.jaxp.SAXParserFactoryImpl").newInstance(); // NOI18N
        } catch (Exception ex) {
            throw new ParserConfigurationException(ex.getMessage());
        }
    }

    private class FCA extends FileChangeAdapter {
        public void fileChanged(FileEvent evt) {
            FileObject fo=evt.getFile();
            try {
                ConfigurationProxy configuration = getFromCache (fo);
                Configuration orig = getOriginalFromCache (fo);
                if (configuration!=null) {
                    try {
                        // preparsing
                        SAXParseException error = parse(fo);
                        if (error!=null) {
//                            System.out.println("Update Error: " + fo.getNameExt() + ", " + error.getMessage());
                            configuration.setError(error);
                            configuration.setStatus(Configuration.STATE_INVALID_PARSABLE);
                        } else {
//                            System.out.println("Successful Update: " + fo.getNameExt());
                            configuration.setError(null);
                            configuration.setStatus(Configuration.STATE_VALID);
                        }
                        Configuration original = createConfiguration(fo.getInputStream());
                        baseBeanMap.put(fo, new WeakReference (original));
                        errorMap.put(fo, configuration.getError ());

                        // replacing original file in proxy Configuration
                        if (configuration.getOriginal()==null) {
//                        System.out.println("Update: replacing...");
                            configuration.setOriginal(original);
                        } else {
//                            System.out.println("Update: merging...");
                            configuration.getOriginal().merge(original,Configuration.MERGE_UPDATE);
                        }
                    } catch (SAXException ex) {
//                        System.out.println("Update Error: " + fo.getNameExt() + ", " + ex.getMessage());
                        if (ex instanceof SAXParseException) {
                            configuration.setError((SAXParseException)ex);
                        } else if ( ex.getException() instanceof SAXParseException) {
                            configuration.setError((SAXParseException)ex.getException());
                        }
                        configuration.setStatus(Configuration.STATE_INVALID_UNPARSABLE);
                        configuration.setOriginal(null);
                    }
                } else if (orig != null) {
                    try {
                        Configuration original = createConfiguration(fo.getInputStream());
                        if (original.getClass().equals (orig.getClass())) {
                            orig.merge(original,Configuration.MERGE_UPDATE);
                        } else {
                            baseBeanMap.put(fo, new WeakReference (original));
                        }
                    } catch (SAXException ex) {
                        baseBeanMap.remove(fo);
                    }
                }
            } catch (java.io.IOException ex){}
        }
    }
}
