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

package org.netbeans.modules.apisupport.project.layers;

import java.awt.EventQueue;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
//import java.util.HashSet;
//import java.util.Set;
//import org.netbeans.api.project.FileOwnerQuery;
//import org.netbeans.api.project.Project;
//import org.netbeans.api.project.ProjectUtils;
//import org.netbeans.api.project.ui.OpenProjects;
//import org.openide.DialogDisplayer;
//import org.openide.DialogDisplayer;
//import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Open the layer file(s) declaring the SFS node.
 */
public class OpenLayerFilesAction extends CookieAction {

    protected void performAction(final Node[] activatedNodes) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    FileObject f = activatedNodes[0].getCookie(DataObject.class).getPrimaryFile();
                    openLayersForFile(f);
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    private void openLayersForFile(FileObject f) throws FileStateInvalidException {
        URL[] location = (URL[]) f.getAttribute("layers"); // NOI18N
        if (location != null) {
            for (URL u : location) {
                FileObject layer = URLMapper.findFileObject(u);
                if (layer != null) {
                    try {
                        openLayerFileAndFind(DataObject.find(layer), f);
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }

    private static void openLayerFileAndFind(DataObject layerDataObject, FileObject originalF) {
        EditorCookie editorCookie = layerDataObject.getCookie(EditorCookie.class);
        if (editorCookie != null) {
            editorCookie.open();
            final LineCookie lineCookie = layerDataObject.getCookie(LineCookie.class);
            if (lineCookie != null) {
                List<FileObject> lineage = new LinkedList<FileObject>();
                FileObject parent = originalF;
                while (parent != null) {
                    if (parent.getParent() != null) {
                        lineage.add(0, parent);
                    }
                    parent = parent.getParent();
                }
                try {
                    InputSource in = new InputSource(layerDataObject.getPrimaryFile().getURL().toExternalForm());
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser parser = factory.newSAXParser();
                    final int[] line = new int[1];
                    //final int[] col = new int[1];
                    class Handler extends DefaultHandler {
                        private Locator locator;
                        private Iterator<FileObject> lineageIterator;
                        private FileObject lookingFor;
                        Handler(List<FileObject> lineage) {
                            lineageIterator = lineage.iterator();
                            lookingFor = lineageIterator.next(); 
                        }
                        @Override
                        public void setDocumentLocator(Locator l) {
                            locator = l;
                        }
                        @Override
                        public void startElement(String uri, String localname, String qname, Attributes attr) throws SAXException {
                            if (line[0] == 0) {
                                if (((lookingFor.isFolder() ? "folder".equals(qname) : "file".equals(qname)) &&
                                    lookingFor.getNameExt().equals(attr.getValue("name")))) { // NOI18N
                                    if (lineageIterator.hasNext()) {
                                        lookingFor = lineageIterator.next();
                                    } else {
                                        line[0] = locator.getLineNumber();
                                        // col[0] = locator.getColumnNumber();
                                    }
                                }
                            }
                        }
                        // XXX also look for comment giving class name in same project to open, e.g.
                        // <file name="org-netbeans-modules-apisupport-project-suite.instance">
                        //     <!--org.netbeans.modules.apisupport.project.suite.SuiteProject-->
                    }
                    parser.parse(in, new Handler(lineage));
                    if (line[0] < 1) {
                        return;
                    }
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            lineCookie.getLineSet().getCurrent(line[0] - 1).show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS /*, Math.max(0, col[0])*/);
                        }
                    });
                } catch (Exception e) {
                    return;
                }
            }
        }
    }        

    public String getName() {
         return NbBundle.getMessage(OpenLayerFilesAction.class, "LBL_open_layer_files_action");
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {DataObject.class};
    }
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }

}
