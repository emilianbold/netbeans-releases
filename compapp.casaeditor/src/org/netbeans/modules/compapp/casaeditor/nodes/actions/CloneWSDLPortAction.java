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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import org.openide.util.actions.NodeAction;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.netbeans.modules.compapp.casaeditor.nodes.WSDLEndpointNode;
import org.netbeans.modules.compapp.casaeditor.model.casa.*;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.retriever.Retriever;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.FileOwnerQuery;

import java.io.*;
import java.nio.channels.FileChannel;
import java.net.URI;

import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;

/**
 * Extract WSDL files from a SU.jar and copy them in the source directory
 * of the compapp project to allow editting.
 *
 * @author  tli
 */
public class CloneWSDLPortAction extends NodeAction {

    private static final String JBI_SOURCE_DIR = "jbiasa";      // NOI18N
    private static final String JBI_SU_JAR_DIR = "jbiServiceUnits";      // NOI18N

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return NbBundle.getMessage(CloneWSDLPortAction.class, "LBL_CloneWSDLPortAction_Name"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected void performAction(Node[] activatedNodes) {
        if ((activatedNodes.length > 0) &&
            (activatedNodes[0] instanceof WSDLEndpointNode)) {

            NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(CloneWSDLPortAction.class, "MSG_CloneWSDLPort"), // NOI18N
                    NbBundle.getMessage(CloneWSDLPortAction.class, "TTL_CloneWSDLPort"), // NOI18N
                    NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
                return;
            }

            final WSDLEndpointNode node = ((WSDLEndpointNode) activatedNodes[0]);
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                   doClone(node);
                }
            });
        }
    }

    private void doClone(WSDLEndpointNode node) {
        try {
            CasaWrapperModel model = node.getModel();
            ModelSource ms = model.getModelSource();
            Lookup lookup = ms.getLookup();
            FileObject casaFO = lookup.lookup(FileObject.class);
            Project proj = FileOwnerQuery.getOwner(casaFO);
            FileObject projFO = proj.getProjectDirectory();
            String projPath = FileUtil.toFile(projFO).getAbsolutePath();
            String srcPath = projPath + "/src";
            // String srcPath = FileUtil.toFile(casaFO).getParentFile().getParentFile().getAbsolutePath();

            CasaPort cp = (CasaPort) node.getData();
            Port port = model.getLinkedWSDLPort(cp);
            Lookup wlookup = port.getModel().getModelSource().getLookup();
            FileObject wsdlFO = wlookup.lookup(FileObject.class);
            File wFile = FileUtil.toFile(wsdlFO).getAbsoluteFile();
            if (wFile.exists()) {
                /*
                  wPath  E:\a2k7\nb61\t0225\CompositeApp6\src\jbiServiceUnits\
                  wProj  CL_MutualCerts_BP\
                  wRoot  localhost_9080\secure_echo_mutualcerts.wsdl
                 */
                String wPath = wFile.getCanonicalPath();
                String wRoot = wPath.substring(wPath.indexOf(JBI_SU_JAR_DIR) + JBI_SU_JAR_DIR.length() + 1).replace('\\', '/');
                String wProj = wRoot.substring(0, wRoot.indexOf('/'));
                wRoot = wRoot.substring(wRoot.indexOf('/') + 1);  // NOI18N
                // System.out.println("wProj: "+wProj+"\nwPath: "+wPath+"\nwRoot: "+wRoot);

                // todo: need to use the target cataglog when copying..
                // IZ#128869, xml retriever can not utitlize the remote catalog...
                // FileObject destFO = FileUtil.toFileObject(new File(rPath));
                //Retriever ret = Retriever.getDefault();
                //FileObject fo = ret.retrieveResourceClosureIntoSingleDirectory(destFO, wFile.toURI());
                // String oPath = fo.getPath();

                // Copy remote resouces to local
                File dstDir = new File(srcPath + "/"+JBI_SOURCE_DIR+"/"+wProj);  // NOI18N
                File srcDir = new File(srcPath + "/"+JBI_SU_JAR_DIR+"/"+wProj);  // NOI18N
                copyDirectory(srcDir, dstDir);

                // Merge remote catalog with the local one
                File dstCat = new File(projPath + "/catalog.xml");  // NOI18N
                File srcCat = new File(srcPath + "/"+JBI_SU_JAR_DIR+"/META-INF/"+wProj + "/catalog.xml");  // NOI18N
                mergeCatalog(srcCat, dstCat, wProj);

                // Update casa port wsdl link
                String oHref = cp.getLink().getHref();
                String href = oHref.replaceFirst(JBI_SU_JAR_DIR, JBI_SOURCE_DIR);
                model.setEndpointLink(cp, href);
                // todo: search and replace all WSDL ports.. ???
                //System.out.println("Link: "+ cp.getLink().getHref());
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }

    }

    /**
     * Copying all wsdl and xsd files for now. Will try to filter base on project catalog
     *
     * @param srcDir  source directory
     * @param dstDir  target directory
     * @throws IOException i/o exception
     */
    private void copyDirectory(File srcDir, File dstDir) throws IOException {
        // todo: calculate closure from starting wsdl (using project catalog...
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }

            String[] children = srcDir.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(srcDir, children[i]),
                              new File(dstDir, children[i]));
            }
        } else { // copy xsd and wsdl files...
            String fname = srcDir.getName();
            String fext = fname.substring(fname.lastIndexOf('.'));
            if ((fext != null) && (fext.equalsIgnoreCase(".xsd") || fext.equalsIgnoreCase(".wsdl"))) {  // NOI18N
                FileChannel srcChannel = new FileInputStream(srcDir).getChannel();
                FileChannel dstChannel = new FileOutputStream(dstDir).getChannel();
                dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
                srcChannel.close();
                dstChannel.close();
            }
        }
    }

    /**
     * Merge the remote catalog content with the project catalog
     *
     * @param srcCat  remote catalog
     * @param dstCat  project catalog
     */
    private void mergeCatalog(File srcCat, File dstCat, String wsdlProj) {
        if (!srcCat.exists()) {
            return;
        }
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = fact.newDocumentBuilder();
            Document srcDoc = builder.parse(srcCat);

            // filter and convert URI...
            String prefix = "..";  // NOI18N
            String newfix = "src/jbiasa/"+wsdlProj; // NOI18N
            NodeList systemNodes = srcDoc.getElementsByTagName("system"); // NOI18N
            for (int i = 0; i < systemNodes.getLength(); i++) {
                Element systemNode = (Element) systemNodes.item(i);
                String uri = systemNode.getAttribute("uri"); // NOI18N
                if ((uri != null) && (uri.startsWith(prefix))) {
                    URI realUri = new URI(uri);
                    if (realUri.getScheme() == null) {
                        uri = newfix + uri.substring(2);
                        systemNode.setAttribute("uri", uri); // NOI18N
                    }
                }
            }

            DOMSource src = null;
            if (dstCat.exists()) {
                Document dstDoc = builder.parse(dstCat);
                // merge catalogs...
                Element dstRoot = dstDoc.getDocumentElement();
                Element srcRoot = srcDoc.getDocumentElement();
                NodeList childNodes = srcRoot.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    org.w3c.dom.Node childNode = childNodes.item(i);

                    // todo: Need to check for duplicated entries...
                    childNode = dstDoc.importNode(childNode, true);
                    dstRoot.appendChild(childNode);
                }

                // write catalog
                dstCat.createNewFile();
                src = new DOMSource(dstDoc);
            } else {
                src = new DOMSource(srcDoc);                
            }

            FileOutputStream fos = new FileOutputStream(dstCat);
            StreamResult rest = new StreamResult(fos);
            TransformerFactory transFact = TransformerFactory.newInstance();
            Transformer transformer = transFact.newTransformer();
            transformer.transform(src, rest);
            fos.flush();
            fos.close();
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

}