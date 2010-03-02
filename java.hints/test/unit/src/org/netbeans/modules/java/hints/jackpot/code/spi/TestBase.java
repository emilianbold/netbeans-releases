/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.code.spi;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.jackpot.code.CodeHintProviderImpl;
import org.netbeans.modules.java.hints.jackpot.code.FSWrapper;
import org.netbeans.modules.java.hints.jackpot.code.FSWrapper.ClassWrapper;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription;
import org.netbeans.modules.java.hints.jackpot.spi.HintMetadata;
import org.netbeans.modules.java.hints.jackpot.spi.HintsRunner;
import org.netbeans.modules.java.source.TreeLoader;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public abstract class TestBase extends NbTestCase {
    private final Class<?> hintClass;
    protected final Logger LOG;

    public TestBase(String name, Class<?> hintClass) {
        super(name);
        this.hintClass = hintClass;
        LOG = Logger.getLogger("test." + name);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml", "META-INF/generated-layer.xml"}, new Object[0]);
        TreeLoader.DISABLE_CONFINEMENT_TEST = true;
    }

    private String sourceLevel = "1.5";

    protected void setSourceLevel(String sourceLevel) {
        this.sourceLevel = sourceLevel;
    }
    
    protected void prepareTest(String fileName, String code) throws Exception {
        clearWorkDir();
        File wdFile = getWorkDir();
        FileUtil.refreshFor(wdFile);

        FileObject wd = FileUtil.toFileObject(wdFile);
        assertNotNull(wd);
        sourceRoot = FileUtil.createFolder(wd, "src");
        FileObject buildRoot = FileUtil.createFolder(wd, "build");
        FileObject cache = FileUtil.createFolder(wd, "cache");

        FileObject data = FileUtil.createData(sourceRoot, fileName);
        File dataFile = FileUtil.toFile(data);

        assertNotNull(dataFile);

        TestUtilities.copyStringToFile(dataFile, code);

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache, extraClassPath());

        SourceUtilsTestUtil.setSourceLevel(data, sourceLevel);

        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);

        assertNotNull(ec);

        doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");

        JavaSource js = JavaSource.forFileObject(data);

        assertNotNull(js);

        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);

        assertNotNull(info);
    }

    private FileObject sourceRoot;
    private CompilationInfo info;
    private Document doc;

    protected final void setTestFileCaretLocation(int pos) throws Exception {
        Method m = CaretAwareJavaSourceTaskFactory.class.getDeclaredMethod("setLastPosition", FileObject.class, int.class);

        m.setAccessible(true);
        m.invoke(null, info.getFileObject(), pos);
    }

    private List<ErrorDescription> computeErrors(CompilationInfo info) {
        Map<HintMetadata, Collection<HintDescription>> hints = new HashMap<HintMetadata, Collection<HintDescription>>();

        ClassWrapper found = null;

        for (ClassWrapper w : FSWrapper.listClasses()) {
            if (w.getName().equals(hintClass.getName())) {
                found = w;
                break;
            }
        }

        assertNotNull(found);
        
        CodeHintProviderImpl.processClass(found, hints);

        List<HintDescription> total = new LinkedList<HintDescription>();

        for (Collection<? extends HintDescription> l : hints.values()) {
            total.addAll(l);
        }

        return HintsRunner.computeErrors(info, total, new AtomicBoolean());
    }

    protected String toDebugString(CompilationInfo info, Fix f) {
        return "FixImpl";
    }

    protected void performAnalysisTest(String fileName, String code, String... golden) throws Exception {
        prepareTest(fileName, code);

        List<ErrorDescription> errors = computeErrors(info);
        Collections.sort (errors, ERRORS_COMPARATOR);
        List<String> errorsNames = new LinkedList<String>();

        errors = errors != null ? errors : Collections.<ErrorDescription>emptyList();

        for (ErrorDescription e : errors) {
            errorsNames.add(e.toString());
        }

        assertTrue("The warnings provided by the hint do not match expected warnings. Provided warnings: " + errorsNames.toString(), Arrays.equals(golden, errorsNames.toArray(new String[0])));
    }

    protected void performAnalysisContainsTest(String fileName, String code, String... golden) throws Exception {
        prepareTest(fileName, code);

        Set<String> goldenSet = new HashSet<String>();
        for (String s : golden) {
            goldenSet.add(s);
        }

        List<ErrorDescription> errors = computeErrors(info);
        Collections.sort (errors, ERRORS_COMPARATOR);
        List<String> errorsNames = new LinkedList<String>();

        errors = errors != null ? errors : Collections.<ErrorDescription>emptyList();

        for (ErrorDescription d : errors) {
            goldenSet.remove(d.toString());
            errorsNames.add(d.toString());
        }
        assertTrue("The warnings provided by the hint do not contain expected warnings. Provided warnings: " + errorsNames.toString(), goldenSet.isEmpty());
    }

    protected void performAnalysisExcludesTest(String fileName, String code, String... golden) throws Exception {
        prepareTest(fileName, code);

        Set<String> goldenSet = new HashSet<String>();
        for (String s : golden) {
            goldenSet.add(s);
        }

        List<ErrorDescription> errors = computeErrors(info);
        Collections.sort (errors, ERRORS_COMPARATOR);
        List<String> errorsNames = new LinkedList<String>();

        errors = errors != null ? errors : Collections.<ErrorDescription>emptyList();

        boolean fail = false;
        for (ErrorDescription d : errors) {
            if (goldenSet.remove(d.getDescription()))
                fail = true;
            errorsNames.add(d.toString());
        }
        assertFalse("The warnings provided by the hint do not exclude expected warnings. Provided warnings: " + errorsNames.toString(), fail);
    }

    protected String performFixTest(String fileName, String code, String errorDescriptionToString, String fixDebugString, String golden) throws Exception {
        return performFixTest(fileName, code, errorDescriptionToString, fixDebugString, fileName, golden);
    }

    protected String performFixTest(String fileName, String code, String errorDescriptionToString, String fixDebugString, String goldenFileName, String golden) throws Exception {
        prepareTest(fileName, code);

        List<ErrorDescription> errors = computeErrors(info);
        Collections.sort (errors, ERRORS_COMPARATOR);

        ErrorDescription toFix = null;

        for (ErrorDescription d : errors) {
            if (errorDescriptionToString.equals(d.toString())) {
                toFix = d;
                break;
            }
        }

        assertNotNull("Error: \"" + errorDescriptionToString + "\" not found. All ErrorDescriptions: " + errors.toString(), toFix);

        assertTrue("Must be computed", toFix.getFixes().isComputed());

        List<Fix> fixes = toFix.getFixes().getFixes();
        List<String> fixNames = new LinkedList<String>();
        Fix toApply = null;

        for (Fix f : fixes) {
            if (fixDebugString.equals(toDebugString(info, f))) {
                toApply = f;
            }

            fixNames.add(toDebugString(info, f));
        }

        assertNotNull("Cannot find fix to invoke: " + fixNames.toString(), toApply);

        toApply.implement();

        FileObject toCheck = sourceRoot.getFileObject(goldenFileName);

        assertNotNull(toCheck);

        DataObject toCheckDO = DataObject.find(toCheck);
        EditorCookie ec = toCheckDO.getLookup().lookup(EditorCookie.class);
        Document toCheckDocument = ec.openDocument();

        String realCode = toCheckDocument.getText(0, toCheckDocument.getLength());

        //ignore whitespaces:
        realCode = realCode.replaceAll("[ \t\n]+", " ");

        if (golden != null) {
            assertEquals("The output code does not match the expected code.", golden, realCode);
        }

        LifecycleManager.getDefault().saveAll();

        return realCode;
    }

    protected FileObject[] extraClassPath() {
        return new FileObject[0];
    }

    static {
        NbBundle.setBranding("test");
    }

    private static final Comparator<ErrorDescription> ERRORS_COMPARATOR = new Comparator<ErrorDescription> () {

        public int compare (ErrorDescription e1, ErrorDescription e2) {
            return e1.getRange ().getBegin ().getOffset () - e2.getRange ().getBegin ().getOffset ();
        }
    };
}
