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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.customizer;

import com.sun.javacard.filemodels.WebXmlModel;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.modules.javacard.project.JCProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.javacard.JCUtil;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.spi.Card;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.modules.javacard.spi.capabilities.UrlCapability;

public class WebProjectProperties extends JCProjectProperties {

    // Run customizer
    Document SPECIFIC_URL;
    Document SERVLET_URL = new PlainDocument();
    Document COMPLETE_URL = new PlainDocument();
    ComboBoxModel PAGES;
    ButtonModel LAUNCH_BROWSER;
    ButtonModel SELECT_SERVLET;
    ButtonModel SELECT_PAGE;
    ButtonModel SELECT_URL;

    private StoreGroup projectGroup;
    private PropertyEvaluator eval;
    private AntProjectHelper antHelper;

    public WebProjectProperties(JCProject project,
            PropertyEvaluator eval, AntProjectHelper antHelper) {
        super(project);
        this.eval = eval;
        this.antHelper = antHelper;
        projectGroup = new StoreGroup();
        init();
    }
    private WebXmlModel fromFileModel;
    private WebXmlModel fromUiModel;
    private String webContextPath;
    private String servletMapping;

    void setWebContextPathAndMainUrl(String defaultServlet, String defaultMapping) {
        this.webContextPath = defaultServlet;
        this.servletMapping = defaultMapping;
        try {
            SERVLET_URL.remove(0, SERVLET_URL.getLength());
            String servletUrl = assembleUrl(baseUrl(), webContextPath, servletMapping);
            SERVLET_URL.insertString(0, servletUrl, null); //NOI18N
            updateFullUrl();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    void setWebXmlFileModel(WebXmlModel mdl) {
        assert fromFileModel == null || fromFileModel != fromUiModel;
        this.fromFileModel = mdl;
    }

    void setWebXmlUiModel(WebXmlModel mdl) {
        assert fromFileModel != fromUiModel;
        this.fromUiModel = mdl;
    }

    private void init() {
        LAUNCH_BROWSER = projectGroup.createToggleButtonModel(
                eval, ProjectPropertyNames.PROJECT_PROP_LAUNCH_EXTERNAL_BROSER);
        SELECT_PAGE = new ToggleButtonModel();
        SELECT_SERVLET = new ToggleButtonModel();
        SELECT_URL = new ToggleButtonModel();

        ProjectManager.mutex().readAccess(new Runnable() {

            @Override
            public void run() {
                platformName = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
                activeDevice = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE);
                FileObject staticPagesDir = antHelper.getProjectDirectory().getFileObject(JCConstants.HTML_FILE_PATH);
                Vector<String> pages = new Vector<String>();
                if (staticPagesDir != null) {
                    Enumeration<? extends FileObject> allFiles =
                            staticPagesDir.getChildren(true /* recursive */);
                    while (allFiles.hasMoreElements()) {
                        FileObject file = allFiles.nextElement();
                        if (file.isData() && "html".equals(file.getExt()) || "htm".equals(file.getExt())) { //NOI18N
                            pages.add("/" + FileUtil.getRelativePath(
                                    staticPagesDir, file));
                        }
                    }
                }
                PAGES = new DefaultComboBoxModel(pages);

                webContextPath = eval.getProperty ( //"webcontextpath"
                        ProjectPropertyNames.PROJECT_PROP_WEB_CONTEXT_PATH);
                servletMapping = eval.getProperty ( //"mainurl"
                        ProjectPropertyNames.PROJECT_PROP_MAIN_URL);

                SELECT_PAGE.setSelected(Boolean.valueOf(eval.getProperty(ProjectPropertyNames.PROJECT_PROP_USE_PAGE)));
                SELECT_URL.setSelected(Boolean.valueOf(eval.getProperty(ProjectPropertyNames.PROJECT_PROP_USE_URL)));
                SELECT_SERVLET.setSelected(Boolean.valueOf(eval.getProperty(ProjectPropertyNames.PROJECT_PROP_USE_SERVLET)));

                SPECIFIC_URL = projectGroup.createStringDocument(eval, ProjectPropertyNames.PROJECT_PROP_EXPLICIT_URL);

                class L implements ActionListener, DocumentListener {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        updateFullUrl();
                    }

                    private void change() {
                        updateFullUrl();
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        change();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        change();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        change();
                    }
                }
                L cl = new L();
                SELECT_PAGE.addActionListener(cl);
                SELECT_URL.addActionListener(cl);
                SELECT_SERVLET.addActionListener(cl);
                SPECIFIC_URL.addDocumentListener(cl);
                try {
                    String url = assembleUrl(baseUrl(), webContextPath, servletMapping);
//                    SERVLET_URL.insertString(0, webContextPath + '/' + servletMapping, null);
                    SERVLET_URL.insertString(0, url, null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                updateFullUrl();
            }
        });
    }

    private String baseUrl() {
        JavacardPlatform platform = JCUtil.findPlatformNamed(platformName);
        Card card = platform.getCards().find(activeDevice, true);
        UrlCapability urls = card.getCapability(UrlCapability.class);
        String result = null;
        if (urls != null) {
            result = urls.getURL();
        }
        if (result == null) {
            result = "http://???/"; //NOI18N
        }
        return result;
    }
    
    private String trimSlashes (String s) {
        if (s == null) {
            return ""; //NOI18N
        }
        if (s.startsWith("/") && s.length() > 1) { //NOI18N
            s = s.substring(1);
        }
        if (s.endsWith("/") && s.length() > 1) { //NOI18N
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private String assembleUrl (String base, String webContextPath, String servletMapping) {
        return trimSlashes(base) + '/' + trimSlashes (webContextPath) + //NOI18N
                '/' + trimSlashes(servletMapping); //NOI18N
    }

    private void updateFullUrl() {
        try {
            COMPLETE_URL.remove(0, COMPLETE_URL.getLength());
            String fullUrl = null;
            if (SELECT_URL.isSelected()) {
                fullUrl = SPECIFIC_URL.getText(0, SPECIFIC_URL.getLength());
            } else {
                String baseUrl = baseUrl();
                if (SELECT_SERVLET.isSelected()) {
                    fullUrl = assembleUrl (baseUrl, webContextPath, servletMapping);
                } else {
                    String page = PAGES.getSelectedItem() != null ? (String) PAGES.getSelectedItem().toString() : "";
                    if (page.startsWith("/")) { //NOI18N
                        page = page.substring(1);
                    }
                    fullUrl = assembleUrl(baseUrl, webContextPath, page);
                }
            }
            COMPLETE_URL.insertString(0, fullUrl, null);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    public static boolean isPropertyFromRuntimeDescriptor(String property) {
        return ProjectPropertyNames.PROJECT_PROP_WEB_CONTEXT_PATH.equals(property);
    }

    public String getWebContextPath() {
        return webContextPath;
    }

    public String getServletMapping() {
        return servletMapping;
    }

    @Override
    public Boolean onStoreProperties(EditableProperties projectProps) throws IOException {
        projectGroup.store(projectProps);
        if (fromUiModel != null && fromFileModel != null && !fromFileModel.equals(fromUiModel)) {
            try {
                rewriteWebXml(fromUiModel);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        if (webContextPath != null) {
            projectProps.setProperty (ProjectPropertyNames.PROJECT_PROP_WEB_CONTEXT_PATH, webContextPath);
        }
        if (servletMapping != null) {
            projectProps.setProperty (ProjectPropertyNames.PROJECT_PROP_MAIN_URL, servletMapping);
        }
        if (LAUNCH_BROWSER.isSelected()) {
            projectProps.setProperty(ProjectPropertyNames.PROJECT_PROP_LAUNCH_EXTERNAL_BROSER, "true");
        } else {
            projectProps.remove(ProjectPropertyNames.PROJECT_PROP_LAUNCH_EXTERNAL_BROSER);
        }
        if (SELECT_SERVLET.isSelected()) {
            projectProps.setProperty (ProjectPropertyNames.PROJECT_PROP_USE_SERVLET, Boolean.TRUE.toString());
        } else {
            projectProps.remove (ProjectPropertyNames.PROJECT_PROP_USE_SERVLET);
        }
        if (SELECT_PAGE.isSelected()) {
            projectProps.setProperty (ProjectPropertyNames.PROJECT_PROP_USE_PAGE, Boolean.TRUE.toString());
        } else {
            projectProps.remove (ProjectPropertyNames.PROJECT_PROP_USE_PAGE);
        }
        if (SELECT_URL.isSelected()) {
            projectProps.setProperty (ProjectPropertyNames.PROJECT_PROP_USE_URL, Boolean.TRUE.toString());
        } else {
            projectProps.remove (ProjectPropertyNames.PROJECT_PROP_USE_URL);
        }
        Object o = PAGES.getSelectedItem();
        if (o != null && o.toString().length() > 0) {
            projectProps.setProperty(ProjectPropertyNames.PROJECT_PROP_PAGE_URL,
                    o.toString());
        } else {
            projectProps.remove(ProjectPropertyNames.PROJECT_PROP_PAGE_URL);
        }
        antHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,
                projectProps);
        return true;
    }

    private void rewriteWebXml(WebXmlModel fromUiModel) throws IOException {
        FileObject file = project.getProjectDirectory().getFileObject(JCConstants.WEB_DESCRIPTOR_PATH); //NOI18N
        if (file == null) {
            file = FileUtil.createData(project.getProjectDirectory(), JCConstants.WEB_DESCRIPTOR_PATH); //NOI18N
        }
        FileLock lock = file.lock();
        OutputStream out = file.getOutputStream(lock);
        PrintWriter writer = new PrintWriter(out);
        try {
            writer.print(fromUiModel.toXml());
            writer.flush();
        } finally {
            writer.close();
            out.close();
            lock.releaseLock();
        }
    }
}
