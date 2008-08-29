/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.maven;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import hidden.org.codehaus.plexus.util.StringOutputStream;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * implementation of AuxiliaryConfiguration that relies on FileObject's attributes
 * for the non shared elements and on ${basedir}/nb-configuration file for share ones.
 * @author mkleint
 */
public class M2AuxilaryConfigImpl implements AuxiliaryConfiguration {

    private static final String AUX_CONFIG = "AuxilaryConfiguration"; //NOI18N
    private static final String CONFIG_FILE_NAME = "nb-configuration.xml"; //NOI18N
    private NbMavenProjectImpl project;

    /** Creates a new instance of M2AuxilaryConfigImpl */
    public M2AuxilaryConfigImpl(NbMavenProjectImpl proj) {
        this.project = proj;
    }

    public synchronized Element getConfigurationFragment(final String elementName, final String namespace, boolean shared) {
        if (shared) {
            final FileObject config = project.getProjectDirectory().getFileObject(CONFIG_FILE_NAME);
            if (config != null) {
                Document doc;
                InputStream in = null;
                try {
                    in = config.getInputStream();
                    //TODO shall be have some kind of caching here to prevent frequent IO?
                    doc = XMLUtil.parse(new InputSource(in), false, true, null, null);
                    return findElement(doc.getDocumentElement(), elementName, namespace);
                } catch (SAXException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
                return null;
            }
            return null;
        } else {
            String str = (String) project.getProjectDirectory().getAttribute(AUX_CONFIG);
            if (str != null) {
                Document doc;
                try {
                    doc = XMLUtil.parse(new InputSource(new StringReader(str)), false, true, null, null);
                    return findElement(doc.getDocumentElement(), elementName, namespace);
                } catch (SAXException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        }
    }

    public synchronized void putConfigurationFragment(final Element fragment, final boolean shared) throws IllegalArgumentException {
        Document doc = null;
        FileObject config = project.getProjectDirectory().getFileObject(CONFIG_FILE_NAME);
        if (shared) {
            if (config != null) {
                try {
                    doc = XMLUtil.parse(new InputSource(config.getInputStream()), false, true, null, null);
                } catch (SAXException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                String element = "project-shared-configuration"; // NOI18N
                doc = XMLUtil.createDocument(element, null, null, null);
                doc.getDocumentElement().appendChild(doc.createComment(
                        "\nThis file contains additional configuration written by modules in the NetBeans IDE.\n" + //NOI18N will be part of a file on disk, don't translate
                        "The configuration is intended to be shared among all the users of project and\n" +//NOI18N
                        "therefore it is assumed to be part of version control checkout.\n" +//NOI18N
                        "Without this configuration present, some functionality in the IDE may be limited or fail altogether.\n"));//NOI18N
            }
        } else {
            String str = (String) project.getProjectDirectory().getAttribute(AUX_CONFIG);
            if (str != null) {
                try {
                    doc = XMLUtil.parse(new InputSource(new StringReader(str)), false, true, null, null);
                } catch (SAXException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (doc == null) {
                String element = "project-private"; // NOI18N
                doc = XMLUtil.createDocument(element, null, null, null);
            }
        }
        if (doc != null) {
            Element el = findElement(doc.getDocumentElement(), fragment.getNodeName(), fragment.getNamespaceURI());
            if (el != null) {
                doc.getDocumentElement().removeChild(el);
            }
            doc.getDocumentElement().appendChild(doc.importNode(fragment, true));
        }
        if (shared) {
            FileLock lck = null;
            OutputStream out = null;
            try {
                if (config == null) {
                    config = project.getProjectDirectory().createData(CONFIG_FILE_NAME);
                }
                lck = config.lock();
                out = config.getOutputStream(lck);
                XMLUtil.write(doc, out, "UTF-8"); //NOI18N
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                if (lck != null) {
                    lck.releaseLock();
                }
            }
        } else {
            try {
                StringOutputStream wr = new StringOutputStream();
                XMLUtil.write(doc, wr, "UTF-8"); //NOI18N
                project.getProjectDirectory().setAttribute(AUX_CONFIG, wr.toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    public synchronized boolean removeConfigurationFragment(final String elementName, final String namespace, final boolean shared) throws IllegalArgumentException {
        Document doc = null;
        FileObject config = project.getProjectDirectory().getFileObject(CONFIG_FILE_NAME);
        if (shared) {
            if (config != null) {
                try {
                    doc = XMLUtil.parse(new InputSource(config.getInputStream()), false, true, null, null);
                } catch (SAXException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                return false;
            }

        } else {
            String str = (String) project.getProjectDirectory().getAttribute(AUX_CONFIG);
            if (str != null) {
                try {
                    doc = XMLUtil.parse(new InputSource(new StringReader(str)), false, true, null, null);
                } catch (SAXException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                return false;
            }
        }
        if (doc != null) {
            Element el = findElement(doc.getDocumentElement(), elementName, namespace);
            if (el != null) {
                doc.getDocumentElement().removeChild(el);
            }
        }
        if (shared) {
            FileLock lck = null;
            OutputStream out = null;
            try {
                lck = config.lock();
                out = config.getOutputStream(lck);
                XMLUtil.write(doc, out, "UTF-8"); //NOI18N
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                if (lck != null) {
                    lck.releaseLock();
                }
            }
        } else {
            try {
                StringOutputStream wr = new StringOutputStream();
                XMLUtil.write(doc, wr, "UTF-8"); //NOI18N
                project.getProjectDirectory().setAttribute(AUX_CONFIG, wr.toString());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return true;
    }

    private static Element findElement(Element parent, String name, String namespace) {
        Element result = null;
        NodeList l = parent.getChildNodes();
        int len = l.getLength();
        for (int i = 0; i < len; i++) {
            if (l.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) l.item(i);
                if (name.equals(el.getLocalName()) && ((namespace == el.getNamespaceURI()) /*check both namespaces are null*/ || (namespace != null && namespace.equals(el.getNamespaceURI())))) {
                    if (result == null) {
                        result = el;
                    } else {
                        return null;
                    }
                }
            }
        }
        return result;
    }
}
