package org.netbeans.modules.gsf.codecoverage;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.GsfTestBase;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProvider;
import org.netbeans.modules.gsf.codecoverage.api.CoverageType;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails;
import org.openide.filesystems.FileObject;
import org.openide.xml.XMLUtil;

public class CodeCoverageTestHelper {
    public static String annotateCoverage(CoverageProvider provider, FileObject fo, FileCoverageDetails details, BaseDocument doc) throws Exception {
        StringBuilder sb = new StringBuilder();

        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("<style>\n");
        sb.append("div { white-space: pre; font-family: monospace; margin: 0px; padding: 0px }\n");
        sb.append(".covered { background-color: CCFFCC }\n");
        sb.append(".not_covered { background-color: FFCCCC }\n");
        sb.append(".inferred { background-color: E0FFE0 }\n");
        sb.append(".unknown { background-color: EEEEEE }\n");
        sb.append("</style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n<h1>File Coverage for ");
        sb.append(fo.getNameExt());
        sb.append("</h1>\n");
        int offset = 0;
        int lineno = 0;
        int maxLines = details.getLineCount();
        while (offset < doc.getLength() && lineno < maxLines) {
            CoverageType type = details.getType(lineno);
            String line = doc.getText(offset, Utilities.getRowEnd(doc, offset) - offset);
            sb.append("<div class=\"");
            switch (type) {
                case COVERED:
                    sb.append("covered");
                    break;
                case UNKNOWN:
                    sb.append("unknown");
                    break;
                case INFERRED:
                    sb.append("inferred");
                    break;
                case NOT_COVERED:
                    sb.append("not_covered");
                    break;
            }
            sb.append("\">");
            sb.append(XMLUtil.toElementContent(line));
            sb.append("</div>\n");

            offset = Utilities.getRowEnd(doc, offset) + 1;
            lineno++;
        }

        sb.append("\n</body></html>\n");

        return sb.toString();
    }

    public static void checkCoverage(GsfTestBase test, String projectPath, String exeFile) throws Exception {
        FileObject projectDir = test.getTestFile(projectPath);
        GsfTestBase.assertNotNull(projectDir);
        String relFilePath = projectPath + "/" + exeFile; // NOI18N
        FileObject fo = test.getTestFile(relFilePath);
        GsfTestBase.assertNotNull(fo);
        BaseDocument doc = test.getDocument(fo);
        GsfTestBase.assertNotNull(doc);

        Project project = ProjectManager.getDefault().findProject(projectDir);
        GsfTestBase.assertNotNull(project);
        CoverageProvider provider = project.getLookup().lookup(CoverageProvider.class);
        GsfTestBase.assertNotNull(provider);
        GsfTestBase.assertTrue(provider.isEnabled());
        FileCoverageDetails details = provider.getDetails(fo, doc);

        String annotatedSource = annotateCoverage(provider, fo, details, doc);
        test.assertDescriptionMatches(relFilePath, annotatedSource, false, ".coverage.html");
    }
}

