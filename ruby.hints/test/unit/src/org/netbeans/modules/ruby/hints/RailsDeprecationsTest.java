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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.modules.ruby.RubyLanguage;
import org.netbeans.modules.ruby.hints.infrastructure.RubyAstRule;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Test the rails deprecations test hint
 *
 * @author Tor Norbye
 */
public class RailsDeprecationsTest extends HintTestBase {

    public RailsDeprecationsTest(String testName) {
        super(testName);
    }

    private RubyAstRule createRule() {
        return new RailsDeprecations();
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        Map<String, ClassPath> result = new HashMap<String, ClassPath>();
        // include gem urls in boot classpath so that rails gems get indexed.
        Collection<URL> gemUrls = RubyPlatformManager.getDefaultPlatform().getGemManager().getGemUrls().values();
        result.put(RubyLanguage.BOOT,
                ClassPathSupport.createProxyClassPath(
                ClassPathSupport.createClassPath(RubyPlatform.getRubyStubs()),
                ClassPathSupport.createClassPath(gemUrls.toArray(new URL[gemUrls.size()]))));
        // golden files
        FileObject testFileFO = FileUtil.toFileObject(getDataFile("/testfiles"));
        FileObject controllers = FileUtil.toFileObject(getDataFile("/testfiles/projects/railsproj/app/controllers"));
        result.put(RubyLanguage.SOURCE, ClassPathSupport.createClassPath(testFileFO, controllers));

        return result;
    }

    public void testRegistered() throws Exception {
        ensureRegistered(createRule());
    }

    public void testInstanceField() throws Exception {
        // Refers to @request
        checkHints(this, createRule(), "testfiles/projects/railsproj/app/controllers/foo_controller.rb", null);
    }

    public void testInstanceFieldNonController() throws Exception {
        // there should be no hints for this file as it doesn't represent a controller
        checkHints(this, createRule(), "testfiles/projects/railsproj/app/controllers/not_a_controller.rb", null);
    }

    public void testSkipNonRails() throws Exception {
        // Shouldn't find deprecations in files that aren't in Rails projects
        checkHints(this, createRule(), "testfiles/notrails.rb", null);
    }

    public void testFinders() throws Exception {
        // Shouldn't mistake Enumerations find_all methods for ActiveRecord ones
        checkHints(this, createRule(), "testfiles/projects/railsproj/app/controllers/findall.rb", null);
    }

    public void testRenderTemplate() throws Exception {
        // Should identify render_template in non spec files
        checkHints(this, createRule(), "testfiles/projects/railsproj/app/controllers/rendertemplate.rb", null);
    }
    public void testRSpec() throws Exception {
        // Shouldn't identify render_template in specs
        checkHints(this, createRule(), "testfiles/projects/railsproj/app/controllers/rendertemplate_spec.rb", null);
    }
}
