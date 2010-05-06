/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.smarty;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.modules.php.smarty.ui.options.SmartyOptions;
import org.netbeans.modules.php.spi.commands.FrameworkCommandSupport;
import org.netbeans.modules.php.spi.editor.EditorExtender;
import org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleCustomizerExtender;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleIgnoredFilesExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author Martin Fousek
 */
public final class SmartyPhpFrameworkProvider extends PhpFrameworkProvider {

    private static final SmartyPhpFrameworkProvider INSTANCE = new SmartyPhpFrameworkProvider();

    public static SmartyPhpFrameworkProvider getInstance() {
        return INSTANCE;
    }

    private SmartyPhpFrameworkProvider() {
        super(NbBundle.getMessage(SmartyPhpFrameworkProvider.class, "LBL_FrameworkName"), NbBundle.getMessage(SmartyPhpFrameworkProvider.class, "LBL_FrameworkDescription"));
    }

    public static boolean isSmartyTemplateExtension(String ext) {
        for (String mimeExt : FileUtil.getMIMETypeExtensions("text/x-tpl")) {
            if (ext.equals(mimeExt)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Try to locate (find) a TPL files in source directory.
     * Currently, it searches source dir and its subdirs.
     * @return {@code false} if not found
     */
    public static boolean locatedTplFiles(FileObject fo, int maxDepth, int actualDepth) {
        while (actualDepth <= maxDepth) {
            for (FileObject child : fo.getChildren()) {
                if (!child.isFolder()) {
                    if (isSmartyTemplateExtension(child.getExt())) {
                        return true;
                    }
                } else if (child.isFolder() && actualDepth < maxDepth) {
                    if (locatedTplFiles(child, maxDepth, actualDepth + 1)) {
                        return true;
                    }
                }
            }
            actualDepth++;
        }
        return false;
    }

    public static FileObject locate(PhpModule phpModule, String relativePath, boolean subdirs) {
        FileObject sourceDirectory = phpModule.getSourceDirectory();

        FileObject fileObject = sourceDirectory.getFileObject(relativePath);
        if (fileObject != null || !subdirs) {
            return fileObject;
        }
        for (FileObject child : sourceDirectory.getChildren()) {
            fileObject = child.getFileObject(relativePath);
            if (fileObject != null) {
                return fileObject;
            }
        }
        return null;
    }

    @Override
    public boolean isInPhpModule(final PhpModule phpModule) {
        // get php files within the module
        final FoundSmarty fs = new FoundSmarty();
        Index index = ElementQueryFactory.getIndexQuery(QuerySupportFactory.get(phpModule.getSourceDirectory()));
        final Set<FileObject> filesWithUsedSmarty = index.getLocationsForIdentifiers(SmartyFramework.BASE_CLASS_NAME);
        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                for (FileObject fileObject : filesWithUsedSmarty) {
                    try {
                        ParserManager.parse(Collections.singleton(Source.create(fileObject)), new UserTask() {

                            @Override
                            public void run(ResultIterator resultIterator) throws Exception {
                                PHPParseResult result = (PHPParseResult) resultIterator.getParserResult();
                                if (result.getProgram() != null) {
                                    SmartyVerificationVisitor smartyVerificationVisitor = new SmartyVerificationVisitor();
                                    result.getProgram().accept(smartyVerificationVisitor);
                                    if (smartyVerificationVisitor.isFoundSmarty()) {
                                        fs.setFound(true);
                                    }
                                }

                            }
                        });
                    } catch (ParseException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            }
        }, 0);

        if (fs.isFound()) {
            return true;
        } else {
            FileObject sourceDirectory = phpModule.getSourceDirectory();
            return locatedTplFiles(sourceDirectory, SmartyOptions.getInstance().getScanningDepth(), 0);
        }
    }

    @Override
    public File[] getConfigurationFiles(PhpModule phpModule) {
        return null;
    }

    @Override
    public PhpModuleExtender createPhpModuleExtender(PhpModule phpModule) {
        return null;
    }

    @Override
    public PhpModuleProperties getPhpModuleProperties(PhpModule phpModule) {
        PhpModuleProperties properties = new PhpModuleProperties();
        FileObject web = locate(phpModule, "web", true); // NOI18N
        if (web != null) {
            properties = properties.setWebRoot(web);
        }
        FileObject testUnit = locate(phpModule, "test/unit", true); // NOI18N
        if (testUnit != null) {
            properties = properties.setTests(testUnit);
        }
        return properties;
    }

    @Override
    public PhpModuleActionsExtender getActionsExtender(PhpModule phpModule) {
        return null;
    }

    @Override
    public PhpModuleIgnoredFilesExtender getIgnoredFilesExtender(PhpModule phpModule) {
        return null;
    }

    @Override
    public FrameworkCommandSupport getFrameworkCommandSupport(PhpModule phpModule) {
        return null;
    }

    @Override
    public EditorExtender getEditorExtender(PhpModule phpModule) {
        return null;
    }

    @Override
    public PhpModuleCustomizerExtender createPhpModuleCustomizerExtender(PhpModule phpModule) {
        return new SmartyPhpModuleCustomizerExtender(phpModule);
    }

    private static final class SmartyVerificationVisitor extends DefaultVisitor {

        private boolean foundSmarty;

        public SmartyVerificationVisitor() {
            foundSmarty = false;
        }

        @Override
        public void visit(ClassInstanceCreation node) {
            super.visit(node);
            if (node.getClassName().getName() instanceof NamespaceName) {
                NamespaceName name = ((NamespaceName) node.getClassName().getName());
                if (!name.getSegments().isEmpty()) {
                    if (name.getSegments().iterator().next().getName().equals(SmartyFramework.BASE_CLASS_NAME)) {
                        foundSmarty = true;
                    }
                }
            }
        }

        public boolean isFoundSmarty() {
            return foundSmarty;
        }
    }

    private class FoundSmarty {
        private boolean isFound;

        public FoundSmarty() {
            setFound(false);
        }

        public synchronized void setFound(boolean isFound) {
            this.isFound = isFound;
        }

        public synchronized boolean isFound() {
            return this.isFound;
        }
    }
}
