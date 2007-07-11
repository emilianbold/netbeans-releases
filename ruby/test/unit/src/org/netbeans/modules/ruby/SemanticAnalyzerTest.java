package org.netbeans.modules.ruby;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.api.gsf.ColoringAttributes;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.junit.NbTestCase;

/**
 * Test the semantic analyzer / highlighter
 * 
 * @author Tor Norbye
 */
public class SemanticAnalyzerTest extends RubyTestBase {

    public SemanticAnalyzerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    private String annotate(Document doc, Map<OffsetRange, ColoringAttributes> highlights) throws Exception {
        StringBuilder sb = new StringBuilder();
        String text = doc.getText(0, doc.getLength());
        Map<Integer, OffsetRange> starts = new HashMap<Integer, OffsetRange>(100);
        Map<Integer, OffsetRange> ends = new HashMap<Integer, OffsetRange>(100);
        for (OffsetRange range : highlights.keySet()) {
            starts.put(range.getStart(), range);
            ends.put(range.getEnd(), range);
        }

        for (int i = 0; i < text.length(); i++) {
            if (starts.containsKey(i)) {
                sb.append("|>");
                OffsetRange range = starts.get(i);
                ColoringAttributes ca = highlights.get(range);
                if (ca != null) {
                    sb.append(ca.name());
                    sb.append(':');
                }
            }
            if (ends.containsKey(i)) {
                sb.append("<|");
            }
            sb.append(text.charAt(i));
        }

        return sb.toString();
    }

    private void checkSemantic(NbTestCase test, String relFilePath) throws Exception {
        File rubyFile = new File(test.getDataDir(), relFilePath);
        if (!rubyFile.exists()) {
            NbTestCase.fail("File " + rubyFile + " not found.");
        }

        SemanticAnalysis analyzer = new SemanticAnalysis();
        CompilationInfo info = getInfo(relFilePath);
        analyzer.run(info);
        Map<OffsetRange, ColoringAttributes> highlights = analyzer.getHighlights();

        String annotatedSource = annotate(info.getDocument(), highlights);

        File goldenFile = new File(test.getDataDir(), relFilePath + ".semantic");
        if (!goldenFile.exists()) {
            if (!goldenFile.createNewFile()) {
                NbTestCase.fail("Cannot create file " + goldenFile);
            }
            FileWriter fw = new FileWriter(goldenFile);
            try {
                fw.write(annotatedSource.toString());
            }
            finally{
                fw.close();
            }
            NbTestCase.fail("Created generated golden file " + goldenFile + "\nPlease re-run the test.");
        }

        String ruby = readFile(test, goldenFile);
        assertEquals(ruby, annotatedSource);
    }

    public void testAnalysis() throws Exception {
        checkSemantic(this, "testfiles/postgresql_adapter.rb");
    }

    public void testAnalysis2() throws Exception {
        checkSemantic(this, "testfiles/ape.rb");
    }

    public void testAnalysis3() throws Exception {
        checkSemantic(this, "testfiles/date.rb");
    }

    public void testAnalysis4() throws Exception {
        checkSemantic(this, "testfiles/resolv.rb");
    }

    public void testUnused() throws Exception {
        checkSemantic(this, "testfiles/unused.rb");
    }
}
