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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import org.apache.commons.jxpath.JXPathContext;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.jaxb.Group;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices;
import org.netbeans.modules.websvc.saas.model.wadl.Application;
import org.netbeans.modules.websvc.saas.model.wadl.Method;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;
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
        InputStream in = null;
        try {
            Exception jbex = null;
            try {
                in = input.getInputStream();
                T t = loadJaxbObject(in, type, includeAware);
                if (t != null) {
                    return t;
                }
            } catch (JAXBException ex) {
                jbex = ex;
            } catch (IOException ioe) {
                jbex = ioe;
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
        //TODO fix claspath: http://www.jroller.com/navanee/entry/unsupportedoperationexception_this_parser_does_not
        includeAware = false;
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
//TODO: fix classpath http://www.jroller.com/navanee/entry/unsupportedoperationexception_this_parser_does_not            
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
    
    public static <T> T fromXPath(Object root, String xpath, Class<T> type) {
        JXPathContext context = JXPathContext.newContext(root);
        context.registerNamespace("", Saas.NS_WADL);
        return type.cast(context.getValue(xpath));
    }

    public static Method wadlMethodFromIdRef(Application app, String methodIdRef) {
        String methodId = methodIdRef;
        if (methodId.charAt(0) == '#') {
            methodId = methodId.substring(1);
        }
        Method result = null;
        for (Object o : app.getResourceTypeOrMethodOrRepresentation()) {
            if (o instanceof Method) {
                Method m = (Method) o;
                if (methodId.equals(m.getId())) {
                    return m;
                }
            }
        }
        for (Resource base : app.getResources().getResource()) {
            result = findMethodById(base, methodId);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
    
    static Method findMethodById(Resource base, String methodId) {
        for (Object o : base.getMethodOrResource()) {
            if (o instanceof Method) {
                Method m = (Method)o;
                if (methodId.equals(m.getId())) {
                    return m;
                }
                continue;
            } else {
                Method m = findMethodById((Resource)o, methodId);
                if (m != null) {
                    return m;
                }
            }
        }
        return null;
    }
    
    public static Method wadlMethodFromXPath(Application app, String xpath) {
        String paths[] = xpath.split("/");
        Resource current = null;
        for (String path : paths) {
            if ("application".equals(path) || path.length() == 0 || "resources".equals(path)) {
                continue;
            } else if (path.startsWith("resource[")) {
                int i = getIndex(path);
                if (i > -1) {
                    List<Resource> resources = getCurrentResources(app, current);
                    if (i < resources.size()) {
                        current = resources.get(i);
                        continue;
                    }    
                }
                return null;
            } else if (path.startsWith("method[")) {
                int iTarget = getIndex(path);
                if (iTarget > -1) {
                    int i = 0;
                    for (Object o : current.getMethodOrResource()) {
                        if (o instanceof Method) {
                            if (i == iTarget) {
                                return (Method) o;
                            }
                            if (i < iTarget) {
                                i++;
                            } else {
                                break;
                            }
                        }
                    }
                }
                return null;
            }
        }
        return null;
    }
    
    static List<Resource> getCurrentResources(Application app, Resource current) {
        if (current == null) {
            return app.getResources().getResource();
        }
        List<Resource> result = new ArrayList<Resource>();
        for (Object o : current.getMethodOrResource()) {
            if (o instanceof Resource) {
                result.add((Resource)o);
            }
        }
        return result;
    }
    
    static int getIndex(String path) {
        int iOpen = path.indexOf('[');
        int iClose = path.indexOf(']');
        if (iOpen < 0 || iClose < 0 || iClose <= iOpen) {
            return -1;
        }
        try {
            return Integer.valueOf(path.substring(iOpen+1, iClose)) - 1; //xpath index is 1-based
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
