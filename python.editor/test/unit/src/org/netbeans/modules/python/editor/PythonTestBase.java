/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.editor;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.python.editor.hints.PythonAstRule;
import org.netbeans.modules.python.editor.hints.PythonSelectionRule;
import org.netbeans.modules.python.editor.lexer.PythonTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.GsfTestBase;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.netbeans.modules.gsf.spi.DefaultLanguageConfig;
import org.netbeans.modules.gsfret.hints.infrastructure.GsfHintsManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;

/**
 * @author Tor Norbye
 */
public abstract class PythonTestBase extends GsfTestBase {
    static {
        PythonIndex.setClusterUrl(getClusterUrl().toExternalForm());
    }

    public PythonTestBase(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new PythonLanguage();
    }
    
    @Override
    protected String getPreferredMimeType() {
        return PythonTokenId.PYTHON_MIME_TYPE;
    }

    @Override
    protected void initializeClassPaths() {
        System.setProperty("netbeans.user", getWorkDirPath());
        org.netbeans.modules.gsfret.source.usages.Index.addPreindexRoot(getClusterHome());
        org.netbeans.modules.gsfret.source.usages.Index.addPreindexRoot(getXTestPythonHomeFo().getFileObject("Lib"));
        initializeRegistry();
        super.initializeClassPaths();
    }

    @Override
    protected List<URL> getExtraCpUrls() {
        try {
            return Collections.singletonList(getXTestPythonHomeFo().getFileObject("Lib").getURL());
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

   @SuppressWarnings("unchecked")
    public void ensureRegistered(PythonAstRule hint) throws Exception {
        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(getPreferredMimeType());
        assertNotNull(language.getHintsProvider());
        GsfHintsManager hintsManager = language.getHintsManager();
        Map<Integer, List<PythonAstRule>> hints = (Map)hintsManager.getHints();
        Set<Class> kinds = hint.getKinds();
        for (Class nodeType : kinds) {
            List<PythonAstRule> rules = hints.get(nodeType);
            assertNotNull(rules);
            boolean found = false;
            for (PythonAstRule rule : rules) {
                if (rule.getClass() == hint.getClass()) {
                    found  = true;
                    break;
                }
            }
            
            assertTrue(found);
        }
    }

   @SuppressWarnings("unchecked")
    public void ensureRegistered(PythonSelectionRule hint) throws Exception {
        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(getPreferredMimeType());
        assertNotNull(language.getHintsProvider());
        GsfHintsManager hintsManager = language.getHintsManager();
        List<PythonSelectionRule> hints = (List<PythonSelectionRule>) hintsManager.getSelectionHints();
        boolean found = false;
        for (PythonSelectionRule rule : hints) {
            if (rule.getClass() == hint.getClass()) {
                found  = true;
                break;
            }
        }

        assertTrue(found);
    }

    // Called via reflection from GsfUtilities. This is necessary because
    // during tests, going from a FileObject to a BaseDocument only works
    // if all the correct data loaders are installed and working - and that
    // hasn't been the case; we end up with PlainDocuments instead of BaseDocuments.
    // If anyone can figure this out, please let me know and simplify the
    // test infrastructure.
    public static BaseDocument getDocumentFor(FileObject fo) {
        BaseDocument doc = GsfTestBase.createDocument(read(fo));
        doc.putProperty(org.netbeans.api.lexer.Language.class, PythonTokenId.language());
        doc.putProperty("mimeType", PythonTokenId.PYTHON_MIME_TYPE);

        return doc;
    }

    // Locate as many Python files from the JPython distribution as possible: libs, gems, etc.
    protected List<FileObject> findJythonFiles() {
        List<FileObject> l = new ArrayList<FileObject>();
        addPythonFiles(l, getXTestPythonHomeFo());

        return l;
    }

    private void addPythonFiles(List<FileObject> list, FileObject parent) {
        for (FileObject child : parent.getChildren()) {
            if (child.isFolder()) {
                if (child.getName().equals("test")) {
                    // Skip test stuff
                    continue;
                }
                addPythonFiles(list, child);
            } else if (child.getMIMEType().equals(PythonTokenId.PYTHON_MIME_TYPE)) {
                list.add(child);
            }
        }
    }

    public static File getXTestPythonHome() {
        String destDir = System.getProperty("xtest.python.home");
        if (destDir == null) {
            throw new RuntimeException("xtest.Python.home property has to be set when running within binary distribution");
        }
        return new File(destDir);
    }

    public static String getXTestPythonHomePath() {
        return getXTestPythonHome().getAbsolutePath();
    }

    public static FileObject getXTestPythonHomeFo() {
        return FileUtil.toFileObject(getXTestPythonHome());
    }

    public static FileObject getClusterHome() {
        return FileUtil.toFileObject(getXTestPythonHome().getParentFile());
    }

    public static URL getClusterUrl() {
        try {
            return getXTestPythonHome().getParentFile().toURI().toURL();
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.toString());
            return null;
        }
    }

    protected List<PythonTree> getAllNodes(PythonTree root) throws Exception {
        final List<PythonTree> nodes = new ArrayList<PythonTree>();

        Visitor visitor = new Visitor() {

            @Override
            public void traverse(PythonTree node) throws Exception {
                nodes.add(node);
                super.traverse(node);
            }

        };
        visitor.visit(root);
        return nodes;
    }
}
