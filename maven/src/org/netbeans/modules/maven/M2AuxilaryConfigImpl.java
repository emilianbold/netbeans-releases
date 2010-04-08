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
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.maven.api.problem.ProblemReport;
import org.netbeans.modules.maven.problems.ProblemReporterImpl;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * implementation of AuxiliaryConfiguration that relies on FileObject's attributes
 * for the non shared elements and on ${basedir}/nb-configuration file for share ones.
 * @author mkleint
 */
public class M2AuxilaryConfigImpl implements AuxiliaryConfiguration {
    public static final String BROKEN_NBCONFIG = "BROKENNBCONFIG"; //NOI18N

    private static final String AUX_CONFIG = "AuxilaryConfiguration"; //NOI18N
    private static final String CONFIG_FILE_NAME = "nb-configuration.xml"; //NOI18N
    private static final int SAVING_DELAY = 100;
    private final NbMavenProjectImpl project;
    private RequestProcessor.Task savingTask;
    private Document scheduledDocument;
    private Date timeStamp = new Date(0);
    private Document cachedDoc;

    /** Creates a new instance of M2AuxilaryConfigImpl */
    public M2AuxilaryConfigImpl(NbMavenProjectImpl proj) {
        this.project = proj;
        savingTask = RequestProcessor.getDefault().create(new Runnable() {

            public void run() {
                try {
                    project.getProjectDirectory().getFileSystem().runAtomicAction(new AtomicAction() {
                        public void run() throws IOException {
                            FileLock lck = null;
                            OutputStream out = null;
                            try {
                                FileObject config = project.getProjectDirectory().getFileObject(CONFIG_FILE_NAME);
                                if (config == null) {
                                    config = project.getProjectDirectory().createData(CONFIG_FILE_NAME);
                                }
                                Document doc;
                                synchronized (M2AuxilaryConfigImpl.this) {
                                    //do saving here..
                                    doc = scheduledDocument;
                                    scheduledDocument = null;

                                    lck = config.lock();
                                }
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
                        }
                    });
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }


            }
        });
    }

    public synchronized Element getConfigurationFragment(final String elementName, final String namespace, boolean shared) {
        if (shared) {
            //first check the document schedule for persistence
            if (scheduledDocument != null) {
                Element el = XMLUtil.findElement(scheduledDocument.getDocumentElement(), elementName, namespace);
                if (el != null) {
                    el = (Element) el.cloneNode(true);
                }
                return el;
            }
            final FileObject config = project.getProjectDirectory().getFileObject(CONFIG_FILE_NAME);
            if (config != null) {
                if (config.lastModified().after(timeStamp)) {
                    // we need to re-read the config file..
                    Document doc;
                    InputStream in = null;
                    try {
                        in = config.getInputStream();
                        //TODO shall be have some kind of caching here to prevent frequent IO?
                        doc = XMLUtil.parse(new InputSource(in), false, true, null, null);
                        cachedDoc = doc;
                        return XMLUtil.findElement(doc.getDocumentElement(), elementName, namespace);
                    } catch (SAXException ex) {
                        ProblemReporterImpl impl = project.getProblemReporter();
                        if (!impl.hasReportWithId(BROKEN_NBCONFIG)) {
                            ProblemReport rep = new ProblemReport(ProblemReport.SEVERITY_MEDIUM,
                                    NbBundle.getMessage(M2AuxilaryConfigImpl.class, "TXT_Problem_Broken_Config"),
                                    NbBundle.getMessage(M2AuxilaryConfigImpl.class, "DESC_Problem_Broken_Config", ex.getMessage()),
                                    new OpenConfigAction(config));
                            rep.setId(BROKEN_NBCONFIG);
                            impl.addReport(rep);
                        }
                        Logger.getLogger(M2AuxilaryConfigImpl.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                        cachedDoc = null;
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                        cachedDoc = null;
                    } finally {
                        timeStamp = config.lastModified();
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                    return null;
                } else {
                    //reuse cached value if available;
                    if (cachedDoc != null) {
                        return XMLUtil.findElement(cachedDoc.getDocumentElement(), elementName, namespace);
                    }
                }
            } else {
                // no file.. remove possible cache
                cachedDoc = null;
            }
            return null;
        } else {
            String str = (String) project.getProjectDirectory().getAttribute(AUX_CONFIG);
            if (str != null) {
                Document doc;
                try {
                    doc = XMLUtil.parse(new InputSource(new StringReader(str)), false, true, null, null);
                    return XMLUtil.findElement(doc.getDocumentElement(), elementName, namespace);
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
            if (scheduledDocument != null) {
                doc = scheduledDocument;
            } else {
                if (config != null) {
                    try {
                        doc = XMLUtil.parse(new InputSource(config.getInputStream()), false, true, null, null);
                    } catch (SAXException ex) {
                        Logger.getLogger(M2AuxilaryConfigImpl.class.getName()).log(Level.INFO, "Cannot parse file " + config.getPath(), ex);
                        if (config.getSize() == 0) {
                            //something got wrong in the past..
                            doc = createNewSharedDocument();
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(M2AuxilaryConfigImpl.class.getName()).log(Level.INFO, "IO Error with " + config.getPath(), ex);
                    }
                } else {
                    doc = createNewSharedDocument();
                }
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
            Element el = XMLUtil.findElement(doc.getDocumentElement(), fragment.getNodeName(), fragment.getNamespaceURI());
            if (el != null) {
                doc.getDocumentElement().removeChild(el);
            }
            doc.getDocumentElement().appendChild(doc.importNode(fragment, true));

            if (shared) {
                if (scheduledDocument == null) {
                    scheduledDocument = doc;
                }
                savingTask.schedule(SAVING_DELAY);
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

    }

    public synchronized boolean removeConfigurationFragment(final String elementName, final String namespace, final boolean shared) throws IllegalArgumentException {
        Document doc = null;
        FileObject config = project.getProjectDirectory().getFileObject(CONFIG_FILE_NAME);
        if (shared) {
            if (scheduledDocument != null) {
                doc = scheduledDocument;
            } else {
                if (config != null) {
                    try {
                        try {
                            doc = XMLUtil.parse(new InputSource(config.getInputStream()), false, true, null, null);
                        } catch (SAXException ex) {
                            Logger.getLogger(M2AuxilaryConfigImpl.class.getName()).log(Level.INFO, "Cannot parse file " + config.getPath(), ex);
                            if (config.getSize() == 0) {
                                //just delete the empty file, something got wrong a while back..
                                config.delete();
                            }
                            return true;
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(M2AuxilaryConfigImpl.class.getName()).log(Level.INFO, "IO Error with " + config.getPath(), ex);
                    }
                } else {
                    return false;
                }
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
            Element el = XMLUtil.findElement(doc.getDocumentElement(), elementName, namespace);
            if (el != null) {
                doc.getDocumentElement().removeChild(el);
            }
            if (shared) {
                if (scheduledDocument == null) {
                    scheduledDocument = doc;
                }
                savingTask.schedule(SAVING_DELAY);
            } else {
                try {
                    StringOutputStream wr = new StringOutputStream();
                    XMLUtil.write(doc, wr, "UTF-8"); //NOI18N
                    project.getProjectDirectory().setAttribute(AUX_CONFIG, wr.toString());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return true;
    }

    static class OpenConfigAction extends AbstractAction {

        private FileObject fo;

        OpenConfigAction(FileObject file) {
            putValue(Action.NAME, NbBundle.getMessage(M2AuxilaryConfigImpl.class, "TXT_OPEN_FILE"));
            fo = file;
        }


        public void actionPerformed(ActionEvent e) {
            if (fo != null) {
                try {
                    DataObject dobj = DataObject.find(fo);
                    EditCookie edit = dobj.getCookie(EditCookie.class);
                    edit.edit();
                } catch (DataObjectNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private Document createNewSharedDocument() throws DOMException {
        String element = "project-shared-configuration";
        Document doc = XMLUtil.createDocument(element, null, null, null);
        doc.getDocumentElement().appendChild(doc.createComment(
                "\nThis file contains additional configuration written by modules in the NetBeans IDE.\n" +
                "The configuration is intended to be shared among all the users of project and\n" +
                "therefore it is assumed to be part of version control checkout.\n" +
                "Without this configuration present, some functionality in the IDE may be limited or fail altogether.\n"));
        return doc;
    }
}
