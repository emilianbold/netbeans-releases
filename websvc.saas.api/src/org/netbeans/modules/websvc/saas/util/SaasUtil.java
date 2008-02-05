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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.saas.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.jaxb.Group;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices;
import org.netbeans.modules.websvc.saas.model.wadl.Application;
import org.netbeans.modules.websvc.saas.spi.SaasNodeActionsProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author nam
 */
public class SaasUtil {

    public static <T> T loadJaxbObject(FileObject input, Class<T> type, boolean includeAware) throws IOException {
        if (input == null) {
            return null;
        }
        InputStream in = input.getInputStream();
        try {
            JAXBException jbex = null;
            try {
                T t = loadJaxbObject(in, type, includeAware);
                if (t != null) {
                    return t;
                }
            } catch (JAXBException ex) {
                jbex = ex;
            }
            String msg = NbBundle.getMessage(SaasUtil.class, "MSG_ErrorLoadingJaxb", type.getName(), input.getPath());
            IOException ioe = new IOException(msg);
            if (jbex != null) {
                ioe.initCause(jbex);
            }
            throw ioe;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public static <T> T loadJaxbObject(InputStream in, Class<T> type) throws JAXBException {
        return loadJaxbObject(in, type, false);
    }
    
    public static <T> T loadJaxbObject(InputStream in, Class<T> type, boolean includeAware) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(type.getPackage().getName());
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        Object o;
        if (includeAware) {
            SAXSource ss = getSAXSourceWithXIncludeEnabled(in);
            o = unmarshaller.unmarshal(ss);
        } else {
            o = unmarshaller.unmarshal(in);
        }
        
        if (type.equals(o.getClass())) {
            return type.cast(o);
        } else if (o instanceof JAXBElement) {
            JAXBElement e = (JAXBElement) o;
            return type.cast(e.getValue());
        }

        throw new IllegalArgumentException("Expect: " + type.getName() + " get: " + o.getClass().getName());
    }

    public static SAXSource getSAXSourceWithXIncludeEnabled(InputStream in) {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setXIncludeAware(true);
            SAXParser saxParser = spf.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            return new SAXSource(xmlReader, new InputSource(in));
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static SaasGroup loadSaasGroup(FileObject input) throws IOException {
        if (input == null) {
            return null;
        }
        return loadJaxbObject(input, SaasGroup.class, false);
    }

    public static SaasGroup loadSaasGroup(InputStream input) throws JAXBException {
        Group g = loadJaxbObject(input, Group.class);
        if (g != null) {
            return new SaasGroup(null, g);
        }
        return null;
    }

    public static void saveSaasGroup(SaasGroup saasGroup, File outFile) throws IOException, JAXBException {
        FileOutputStream out = new FileOutputStream(outFile);
        try {
            saveSaasGroup(saasGroup, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    public static final QName QNAME_GROUP = new QName(Saas.NS_SAAS, "group");

    public static void saveSaasGroup(SaasGroup saasGroup, OutputStream output) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(Group.class.getPackage().getName());
        Marshaller marshaller = jc.createMarshaller();
        JAXBElement<Group> jbe = new JAXBElement<Group>(QNAME_GROUP, Group.class, saasGroup.getDelegate());
        marshaller.marshal(jbe, output);
    }

    public static Application loadWadl(FileObject wadlFile) throws IOException {
        return loadJaxbObject(wadlFile, Application.class, true);
    }

    public static Application loadWadl(InputStream in) throws JAXBException {
        return loadJaxbObject(in, Application.class, true);
    }

    public static SaasServices loadSaasServices(FileObject wadlFile) throws IOException {
        return loadJaxbObject(wadlFile, SaasServices.class, true);
    }

    public static SaasServices loadSaasServices(InputStream in) throws JAXBException {
        return loadJaxbObject(in, SaasServices.class, true);
    }

    private static Lookup.Result<SaasNodeActionsProvider> extensionsResult = null;
    public static Collection<? extends SaasNodeActionsProvider> getSaasNodeActionsProviders() {
        if (extensionsResult == null) {
            extensionsResult = Lookup.getDefault().lookupResult(SaasNodeActionsProvider.class);
        }
        return extensionsResult.allInstances();
    }
    
}
