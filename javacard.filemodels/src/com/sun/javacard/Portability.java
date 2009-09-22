/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.javacard;

import org.w3c.dom.Document;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility methods to allow the classes in this package to use NetBeans
 * classes if available, but fall back to some reasonable default when
 * invoked from an Ant task.
 *
 * @author Tim Boudreau
 */
public final class Portability {
    private Portability(){}

    private static Reference<ResourceBundle> bundleRef;

    public static String getString(String key, Object substitutions) {
        if (inNetBeans()) {
            return getViaNbBundle(key, substitutions);
        } else {
            return getViaResourceBundle(key, substitutions);
        }
    }

    public static String getString (String key) {
        return getString (key, null);
    }

    private static String getViaNbBundle(String key, Object substitutions) {
        try {
            Class<?> clazz = Class.forName("org.openide.util.NbBundle"); //NOI18N
            Method m;
            if (substitutions == null) {
                m = clazz.getMethod ("getMessage", Class.class, String.class );
                return (String) m.invoke (null, Portability.class, key);
            } else {
                m = clazz.getMethod ("getMessage", Class.class, String.class, Object.class );
                return (String) m.invoke(null, Portability.class, key, substitutions);
            }
        } catch (Exception ex) {
            Logger.getLogger(Portability.class.getName()).log(Level.SEVERE,
                    null, ex);
            return getViaResourceBundle(key, substitutions);
        }
    }

    private static String getViaResourceBundle(String key, Object substitutions) {
        ResourceBundle bundle = bundleRef == null ? null : bundleRef.get();
        if (bundle == null) {
            try {
                bundle = ResourceBundle.getBundle(
                        "com.sun.javacard.Bundle", Locale.getDefault()); //NOI18N
            } catch (MissingResourceException e) {
                try {
                    bundle = ResourceBundle.getBundle(
                            "com.sun.javacard.Bundle", Locale.ENGLISH); //NOI18N
                } catch (MissingResourceException e1) {
                    InputStream in = Portability.class.getResourceAsStream(
                            "Bundle.properties"); //NOI18N
                    try {
                        bundle = new PropertyResourceBundle(in);
                    } catch (IOException ioe) {
                        Logger.getLogger(Portability.class.getName()).log(Level.SEVERE,
                                null, ioe);
                        return key;
                    } finally {
                        try {
                            in.close();
                        } catch (IOException ex) {
                            Logger.getLogger(Portability.class.getName()).log(Level.SEVERE,
                                    null, ex);
                        }
                    }
                }
            }
            if (bundle != null) {
                bundleRef = new SoftReference<ResourceBundle>(bundle);
            }
        }
        if (bundle != null) {
            String result = bundle.getString(key);
            if (substitutions != null) {
                MessageFormat fmt = new MessageFormat(result);
                return fmt.format(new Object[] { substitutions } );
            } else {
                return result;
            }
        }
        return key;
    }

    public static XMLReader createXMLReader() {
        if (inNetBeans()) {
            return getXmlReaderViaXmlUtil();
        } else {
            try {
                return XMLReaderFactory.createXMLReader();
            } catch (SAXException ex) {
                throw new Error ("Could not create XML reader", ex);
            }
        }
    }

    private static XMLReader getXmlReaderViaXmlUtil() {
        try {
            Class<?> clazz = Class.forName("org.openide.xml.XMLUtil"); //NOI18N
            Method m = clazz.getMethod ("createXMLReader", new Class[] {
                Boolean.TYPE, Boolean.TYPE });
            return (XMLReader) m.invoke(null, false, false);
        } catch (Exception ex) {
            Portability.logException(ex);
            try {
                return XMLReaderFactory.createXMLReader();
            } catch (SAXException ex1) {
                Portability.logException(ex);
                throw new Error ("Cannot create an XML reader", ex);
            }
        }

    }

    public static Document parse(InputStream in) throws IOException {
        if (inNetBeans()) {
            return parseViaXmlUtil(in);
        } else {
            return parseDefault(in);
        }
    }
    private static Boolean inNetBeans;

    private static synchronized boolean inNetBeans() {
        if (inNetBeans == null) {
            try {
                Class.forName("org.openide.util.NbBundle"); //NOI18N
                inNetBeans = Boolean.TRUE;
            } catch (ClassNotFoundException e) {
                inNetBeans = Boolean.FALSE;
            }
        }
        return inNetBeans.booleanValue();
    }

    private static Document parseDefault(InputStream in) throws IOException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(in);
            return doc;
        } catch (SAXException ex) {
            if (System.getProperty("netbeans.home") == null) {
                ex.printStackTrace();
            }
            IOException ioe = new IOException();
            ioe.initCause (ex);
            throw ioe;
        } catch (ParserConfigurationException ex) {
            IOException ioe = new IOException();
            ioe.initCause (ex);
            throw ioe;
        } finally {
            in.close();
        }
    }

    private static Document parseViaXmlUtil(InputStream in) throws IOException {
        try {
            Class<?>[] types = new Class[]{InputSource.class, Boolean.TYPE, Boolean.TYPE, ErrorHandler.class, EntityResolver.class};
            Class<?> clazz = Class.forName("org.openide.xml.XMLUtil"); //NOI18N
            Method m = clazz.getMethod("parse", types);
            InputSource src = new InputSource(in);
            Object[] args = new Object[]{src, Boolean.FALSE, Boolean.FALSE, null, null};
            return (Document) m.invoke(null, args);
        } catch (Exception ex) {
            IOException ioe = new IOException();
            ioe.initCause(ex);
            throw ioe;
        } finally {
            in.close();
        }
    }

    public static void logException (Exception e) {
        if (inNetBeans()) {
            logExceptionViaExceptions(e);
        } else {
            logViaLogger(e);
        }
    }

    private static void logViaLogger (Exception e) {
        Logger.getLogger(Portability.class.getName()).log (Level.SEVERE, null, e);
    }

    private static void logExceptionViaExceptions(Exception e) {
        try {
            Class<?> clazz = Class.forName("org.openide.util.Exceptions");
            Method m = clazz.getDeclaredMethod("printStackTrace", Throwable.class);
            m.invoke(null, e);
        } catch (Exception ex) {
            logViaLogger(e);
            logViaLogger(ex);
        }
    }

}
