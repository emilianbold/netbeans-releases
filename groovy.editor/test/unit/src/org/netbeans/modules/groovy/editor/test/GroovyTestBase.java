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

package org.netbeans.modules.groovy.editor.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.codehaus.groovy.ant.Groovyc;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.api.Formatter;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.parser.GroovyLanguage;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.gsf.GsfTestBase;
import org.netbeans.modules.gsf.spi.DefaultLanguageConfig;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * In order to be able to run tests using java.source on Mac, you need to apply patch
 * java-tests-mac.diff in root of groovy.editor module (but DO NOT COMMIT IT!)
 * See issue 97290 for details.
 *
 * @author Martin Adamek
 */
public class GroovyTestBase extends GsfTestBase {

    private static void setLookups(Object... instances) {
        Object[] allInstances = new Object[instances.length + 1];
        ClassLoader classLoader = GroovyTestBase.class.getClassLoader();
        allInstances[0] = classLoader;
        System.arraycopy(instances, 0, allInstances, 1, instances.length);
        MockLookup.setInstances(allInstances);
    }
    
    protected FileObject testFO;

    public GroovyTestBase(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        GroovyIndex.setClusterUrl("file:/bogus"); // No translation
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        testFO = workDir.createData("Test.groovy");
        FileUtil.setMIMEType("groovy", GroovyTokenId.GROOVY_MIME_TYPE);
        setLookups(new DummyClassPathProvider());
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new GroovyLanguage();
    }
    
    @Override
    protected String getPreferredMimeType() {
        return GroovyTokenId.GROOVY_MIME_TYPE;
    }

    @Override
    public org.netbeans.modules.gsf.api.Formatter getFormatter(IndentPrefs preferences) {
        /* Findbugs-removed: 
        if (preferences == null) {
        preferences = new IndentPrefs(4,4);
        }*/

//        Preferences prefs = NbPreferences.forModule(JsFormatterTest.class);
//        prefs.put(FmtOptions.indentSize, Integer.toString(preferences.getIndentation()));
//        prefs.put(FmtOptions.continuationIndentSize, Integer.toString(preferences.getHangingIndentation()));
//        CodeStyle codeStyle = CodeStyle.getTestStyle(prefs);
        
        Formatter formatter = new Formatter();//codeStyle, 80);
        
        return formatter;
    }

    @Override
    protected FileObject getTestFileObject() {
        return testFO;
    }

    // Called via reflection from GsfUtilities and AstUtilities. This is necessary because
    // during tests, going from a FileObject to a BaseDocument only works
    // if all the correct data loaders are installed and working - and that
    // hasn't been the case; we end up with PlainDocuments instead of BaseDocuments.
    // If anyone can figure this out, please let me know and simplify the
    // test infrastructure.
    public static BaseDocument createDocument(String s) {
        BaseDocument doc = GsfTestBase.createDocument(s);
        doc.putProperty(org.netbeans.api.lexer.Language.class, GroovyTokenId.language());
        doc.putProperty("mimeType", GroovyTokenId.GROOVY_MIME_TYPE);

        return doc;
    }
    
    public static BaseDocument getDocumentFor(FileObject fo) {
        return createDocument(read(fo));
    }

    private static ClassPath createBootClassPath () throws IOException {
	String bcp = System.getProperty ("sun.boot.class.path");	//NOI18N
	assertNotNull (bcp);
	StringTokenizer tk = new StringTokenizer (bcp,File.pathSeparator);
	List<URL> roots = new ArrayList<URL>();
	while (tk.hasMoreTokens()) {
	    String token = tk.nextToken();
	    File f = new File (token);
	    URL url = f.toURI().toURL();
	    if (FileUtil.isArchiveFile(url)) {
		url = FileUtil.getArchiveRoot(url);
	    }
	    else if (!f.exists()) {
		url = new URL (url.toExternalForm()+'/');
	    }
	    roots.add (url);
	}
	return ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()]));
    }

    private ClassPath createSourcePath() throws IOException {
        File srcDir = getDataFile("/testfiles/completion");
        File srcDir2 = getDataFile("/testfiles/completion/types");
	return ClassPathSupport.createClassPath(new FileObject[] { 
            FileUtil.toFileObject(srcDir), FileUtil.toFileObject(srcDir2)
        });
    }

    private ClassPath createCompilePath() {
        URL url = Groovyc.class.getProtectionDomain().getCodeSource().getLocation();
        return ClassPathSupport.createClassPath(FileUtil.getArchiveRoot(url));
    }

    private class DummyClassPathProvider implements ClassPathProvider {

        public ClassPath findClassPath(FileObject file, String type) {
            try {
                if (type == ClassPath.SOURCE) {
                    return createSourcePath();
                } else if (type == ClassPath.BOOT) {
                    return createBootClassPath();
                } else if (type == ClassPath.COMPILE) {
                    return createCompilePath();
                }
            } catch (IOException ioe) {
                //Skip it
            }
            return ClassPathSupport.createClassPath(Collections.<PathResourceImplementation>emptyList());
        }
    }

}
