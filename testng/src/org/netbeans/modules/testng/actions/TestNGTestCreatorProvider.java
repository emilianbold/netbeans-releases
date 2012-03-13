/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.testng.actions;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin;
import org.netbeans.modules.java.testrunner.CommonTestUtil;
import org.netbeans.modules.java.testrunner.GuiUtils;
import org.netbeans.modules.testng.api.TestNGSupport;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Line;

/**
 *
 * @author theofanis
 */
@TestCreatorProvider.Registration(displayName=GuiUtils.TESTNG_TEST_FRAMEWORK)
public class TestNGTestCreatorProvider extends TestCreatorProvider {

    private static final Logger LOGGER = Logger.getLogger(TestNGTestCreatorProvider.class.getName());
    
    @Override
    public boolean canHandleMultipleClasses(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        DataObject dataObj = activatedNodes[0].getLookup().lookup(DataObject.class);
        if (dataObj == null) {
            return false;
        }
        return dataObj.getPrimaryFile().isData();
    }

    @Override
    public boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
        if (dataObject != null) {
            Project p = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
            return TestNGSupport.isActionSupported(TestNGSupport.Action.CREATE_TEST, p);
        }
        return false;
    }

    @Override
    public void createTests(Context context) {//Node[] activatedNodes) {
//        final DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
        final DataObject dataObject = context.getActivatedNodes()[0].getLookup().lookup(DataObject.class);
        if (dataObject == null) {
            return;
        }

        /*
         * Store the configuration data:
         */
//        final boolean singleClass = isSingleClass();
        final boolean singleClass = context.isSingleClass();
        final Map<CommonPlugin.CreateTestParam, Object> params = CommonTestUtil.getSettingsMap(!singleClass);
        if (singleClass) {
//            params.put(CommonPlugin.CreateTestParam.CLASS_NAME, getTestClassName());
            params.put(CommonPlugin.CreateTestParam.CLASS_NAME, context.getTestClassName());
        }
//        final FileObject trgFolder = getTargetFolder();
//        String n = getTestClassName();
        final FileObject trgFolder = context.getTargetFolder();
        String n = context.getTestClassName();
        
        FileObject templateFO = FileUtil.getConfigFile("Templates/TestNG/EmptyTestNGTest.java");
        DataObject templateDO = null;
        try {
            templateDO = DataObject.find(templateFO);
        } catch (DataObjectNotFoundException ex) {
            LOGGER.log(Level.FINER, null, ex);
        }
        String pkg = n.indexOf(".") > -1
                ? n.substring(0, n.lastIndexOf("."))
                : null;
        String name = n.substring(n.lastIndexOf('.') + 1);
        FileObject targetFolder = trgFolder;
        
        if (pkg != null) {
            try {
                targetFolder = FileUtil.createFolder(targetFolder, pkg.replace('.', '/'));
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        if (templateDO != null) {
            DataObject createdFile = null;
            try {
                createdFile = templateDO.createFromTemplate(DataFolder.findFolder(targetFolder), name, Collections.singletonMap("package", pkg));
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
            FileObject newFile = createdFile.getPrimaryFile();
            TestNGSupport.findTestNGSupport(FileOwnerQuery.getOwner(newFile)).configureProject(newFile);
            final LineCookie lc = createdFile.getLookup().lookup(LineCookie.class);
            if (lc != null) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        //XXX - should find correct line # programatically
                        Line l = lc.getLineSet().getOriginal(16);
                        l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                    }
                });
            } else {
                final EditorCookie ec = createdFile.getLookup().lookup(EditorCookie.class);
                if (ec != null) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            ec.open();

                        }
                    });
                } else {
                    LOGGER.log(Level.INFO, "Didn''t get LineCookie nor EditorCookie for: {0}", createdFile.getPrimaryFile()); //NOI18N
                }
            }
        }
    }
    
}
