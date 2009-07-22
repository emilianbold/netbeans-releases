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
package org.netbeans.modules.maven.j2ee.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.maven.spi.actions.ActionConvertor;
import org.netbeans.modules.maven.spi.actions.ReplaceTokenProvider;
import org.netbeans.modules.web.api.webmodule.RequestParametersQuery;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class WebReplaceTokenProvider implements ReplaceTokenProvider, ActionConvertor {

    private static final String WEB_PATH = "webpagePath";//NOI18N
    public static final String ATTR_EXECUTION_URI = "execution.uri"; //NOI18N
    public static final String FILE_DD        = "web.xml";//NOI18N

    private Project project;

    public WebReplaceTokenProvider(Project prj) {
        project = prj;
    }
    /**
     * just gets the array of FOs from lookup.
     */
    protected static FileObject[] extractFileObjectsfromLookup(Lookup lookup) {
        List<FileObject> files = new ArrayList<FileObject>();
        Iterator<? extends DataObject> it = lookup.lookup(new Lookup.Template<DataObject>(DataObject.class)).allInstances().iterator();
        while (it.hasNext()) {
            DataObject d = it.next();
            FileObject f = d.getPrimaryFile();
            files.add(f);
        }
        return files.toArray(new FileObject[files.size()]);
    }

    public Map<String, String> createReplacements(String action, Lookup lookup) {
        FileObject[] fos = extractFileObjectsfromLookup(lookup);
        String relPath = null;
        SourceGroup group = null;
        FileObject fo = null;
        HashMap<String, String> replaceMap = new HashMap<String, String>();
        if (fos.length > 0 && action.endsWith(".deploy")) { //NOI18N
            fo = fos[0];
            Sources srcs = project.getLookup().lookup(Sources.class);
            //for jsps
            String requestParams = RequestParametersQuery.getFileAndParameters(fo);
            if (requestParams != null && !"/null".equals(requestParams)) { //IMHO a bug in the RPQI in WebExSupport.java
                relPath =  requestParams;
            }
            if (relPath == null) {
            //for html
                String url = FileUtil.getRelativePath(WebModule.getWebModule(fo).getDocumentBase(), fo); 
                if (url != null) {
                    url = url.replace(" ", "%20"); //NOI18N
                    relPath =  "/" + url; //NOI18N
                }
            }
            if (relPath == null) {
                //TODO we shall check the resources as well, not sure that is covered here..
                // if not, this code is a duplication of the above snippet only..
                SourceGroup[] grp = srcs.getSourceGroups("doc_root"); //NOI18N J2EE
                for (int i = 0; i < grp.length; i++) {
                    relPath = FileUtil.getRelativePath(grp[i].getRootFolder(), fo);
                    if (relPath != null) {
                        break;
                    }
                }
            }

            if (relPath == null) {
                // run servlet
                if ("text/x-java".equals(fo.getMIMEType())) { //NOI18N
                    String executionUri = (String) fo.getAttribute(ATTR_EXECUTION_URI);
                    if (executionUri != null) {
                        relPath = executionUri;
                    } else {
                        WebModule webModule = WebModule.getWebModule(fo);
                        String[] urlPatterns = getServletMappings(webModule, fo);
                        if (urlPatterns != null && urlPatterns.length > 0) {
                            ServletUriPanel uriPanel = new ServletUriPanel(urlPatterns, null, true);
                            DialogDescriptor desc = new DialogDescriptor(uriPanel,
                                    NbBundle.getMessage(WebReplaceTokenProvider.class, "TTL_setServletExecutionUri"));
                            Object res = DialogDisplayer.getDefault().notify(desc);
                            if (res.equals(NotifyDescriptor.YES_OPTION)) {
                                relPath = uriPanel.getServletUri(); //NOI18N
                                try {
                                    fo.setAttribute(ATTR_EXECUTION_URI, uriPanel.getServletUri());
                                } catch (IOException ex) {
                                }
                            }
                        }

                    }

                }

            }
            if (relPath == null) {
                relPath = "";
            }
            replaceMap.put(WEB_PATH, relPath);//NOI18N
        }
        return replaceMap;
    }

    public static String[] getServletMappings(WebModule webModule, FileObject javaClass) {
        if (webModule == null)
            return null;

//        FileObject webDir = webModule.getDocumentBase ();
//        if (webDir==null) return null;
//        FileObject fo = webDir.getFileObject("WEB-INF/web.xml"); //NOI18N

        FileObject webInf = webModule.getWebInf();
        if (webInf == null)
            return null;

        FileObject fo = webInf.getFileObject(FILE_DD);
        if (fo == null)
            return null;

        ClassPath classPath = ClassPath.getClassPath (javaClass, ClassPath.SOURCE);
        String className = classPath.getResourceName(javaClass,'.',false);
        try {
            WebApp webApp = DDProvider.getDefault().getDDRoot(fo);
            Servlet[] servlets = webApp.getServlet();
            java.util.List<String> mappingList = new java.util.ArrayList<String>();
            for (int i=0;i<servlets.length;i++) {
                if (className.equals(servlets[i].getServletClass())) {
                    String servletName=servlets[i].getServletName();
                    ServletMapping[] maps  = webApp.getServletMapping();
                    for (int j=0;j<maps.length;j++) {
                        if (maps[j].getServletName().equals(servletName)) {
                            String urlPattern = maps[j].getUrlPattern();
                            if (urlPattern!=null)
                                mappingList.add(urlPattern);
                        }
                    }
                }
            }
            String[] mappings = new String[mappingList.size()];
            mappingList.toArray(mappings);
            return mappings;
        } catch (java.io.IOException ex) {return null;}
    }

    public String convert(String action, Lookup lookup) {
        if (ActionProvider.COMMAND_RUN_SINGLE.equals(action) ||
            ActionProvider.COMMAND_DEBUG_SINGLE.equals(action)) {
            FileObject[] fos = extractFileObjectsfromLookup(lookup);
            if (fos.length > 0) {
                FileObject fo = fos[0];
                if ("text/x-java".equals(fo.getMIMEType())) { //NOI18N
                    //TODO sorty of clashes with .main (if both servlet and main are present.
                    // also prohitibs any other conversion method.
                    Sources srcs = project.getLookup().lookup(Sources.class);
                    SourceGroup[] grp = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                    for (int i = 0; i < grp.length; i++) {
                        if (!"2TestSourceRoot".equals(grp[i].getName())) { //NOI18N hack
                            String relPath = FileUtil.getRelativePath(grp[i].getRootFolder(), fo);
                            if (relPath != null) {
                                if (fo.getAttribute(ATTR_EXECUTION_URI) != null ||
                                        Boolean.TRUE.equals(fo.getAttribute("org.netbeans.modules.web.IsServletFile"))) {//NOI18N
                                    return action + ".deploy"; //NOI18N
                                }
                                if (isDDServlet(lookup, fo, relPath))  {
                                    try {
                                        fo.setAttribute("org.netbeans.modules.web.IsServletFile", Boolean.TRUE); //NOI18N
                                    } catch (IOException ex) {
                                    }
                                    return action + ".deploy"; //NOI18N
                                }
                            }
                        }
                    }
                }
                if ("text/x-jsp".equals(fo.getMIMEType())) { //NOI18N
                    return action + ".deploy"; //NOI18N
                }
                if ("text/html".equals(fo.getMIMEType())) { //NOI18N
                    return action + ".deploy"; //NOI18N
                }
            }
        }
        return null;
    }

    private boolean isDDServlet(Lookup context, FileObject javaClass, String relPath) {
        WebModule webModule = WebModule.getWebModule(javaClass);
        if (webModule == null) {
            return false;
        }

        FileObject webInfDir = webModule.getWebInf();
        if (webInfDir == null) {
            return false;
        }

        FileObject fo = webInfDir.getFileObject(FILE_DD);
        if (fo == null) {
            return false;
        }

        // #117888
        String className = relPath.replace('/', '.').replaceFirst("\\.java$", ""); // is there a better way how to do it?
        try {
            WebApp webApp = DDProvider.getDefault().getDDRoot(fo);
            Servlet servlet = (Servlet) webApp.findBeanByName("Servlet", "ServletClass", className); //NOI18N
            if (servlet != null) {
                return true;
            } else {
                return false;
            }

        } catch (IOException ex) {
            return false;
        }

    }

}
