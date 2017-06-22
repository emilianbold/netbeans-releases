/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.diagnostics.clank.ui.codesnippet;

//import com.sun.tools.analytics.utils.CndFileUtilBridge;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class CodeSnippet {

    private FileObject fo;
    private volatile AnnotatedCode code;
    private final String fileURI;
    private final int line;
//    private final int column;
    private final String path;
    private final int[][] startLineColumns;
    private final int[][] endLineColumns;

    public CodeSnippet(FileObject fo, String path, int[][] startLineColumns, int[][] endLineColumns) {
        this(fo, null, path, startLineColumns, endLineColumns);//, descr);
    }

    public CodeSnippet(String fileURI, String path,  int[][] startLineColumns, int[][] endLineColumns) {
        this(null, fileURI, path, startLineColumns, endLineColumns);//, descr);
    }

    private CodeSnippet(FileObject fo, String fileURI, String path, int[][] startLineColumns, int[][] endLineColumns) {
        this.fo = fo;
        this.line = startLineColumns[0][0];
        this.path = path;
        this.fileURI = fileURI;
        this.startLineColumns = startLineColumns;
        this.endLineColumns = endLineColumns;     
        assert fileURI != null || fo != null : "fo or fileURI should be not null: fo = " + fo + "\n uri = " + fileURI; //NOI18N
    }

//    public Description getDescription() {
//        return descr;
//    }


    public String getFilePath() {
        return path;
    }

    public synchronized FileObject getFileObject() {
        if (fo == null && fileURI != null) {
            String uri = this.fileURI;
            //   fo = URLMapper.findFileObject(new URL(uri));
            fo = CndFileUtils.urlToFileObject(uri);
        }
        return fo;
    }

    public int getLine() {
        return line;
    }

//    public int getColumn() {
//        return column;
//    }

    public synchronized AnnotatedCode getCode() throws IOException {
        if (code == null) {
            code = AnnotatedCode.create(this);
        }
        return code;
    }

    @Override
    public String toString() {
        return "CodeSnippet{" + "path=" + path + ", fo=" + fo + '}';//NOI18N
    }

    public boolean isAnnotatedCodeReady() {
        return code != null;
    }

    public static final class AnnotatedCode {

        private final String mimeType;
        private final List<LineInfo> lines;        

        private AnnotatedCode(List<LineInfo> lines, String mimeType) {
            this.mimeType = mimeType;
            this.lines = lines;
        }

        public String getText() {
            StringBuilder out = new StringBuilder();
            for (LineInfo line : lines) {
                out.append(line.lineNumberPrefix).append(line.lineText).append('\n');
            }
            return out.toString();
        }

        public String getMimeType() {
            return mimeType;
        }

        public Collection<LineInfo> getAnnotations() {
            Collection<LineInfo> out = new ArrayList<LineInfo>(1);
            for (LineInfo line : lines) {
                if (line.type == LineType.ANNOTATION || line.type == LineType.ERROR) {
                    out.add(line);
                }
            }
            return out;
        }

        public List<LineInfo> getLines() {
            return Collections.unmodifiableList(lines);
        }

        private static final int CONTEXT_LINE_NUM = Integer.getInteger("diagnostic.code.snippet.lines", 1);//NOI18N

        private static AnnotatedCode create(CodeSnippet codeSnippet) throws IOException {
            FileObject fo = codeSnippet.getFileObject();
            CsmFile csmFile = CsmUtilities.getCsmFile(fo, true, false);
            // line is 1-based number
            int line = codeSnippet.getLine();
            int currentLineNumber = 0;
            if (fo != null) {
                List<String> asLines = fo.asLines();
                if (asLines.size() >= line) {
                    List<LineInfo> lines = new ArrayList<LineInfo>(4);
                    String lineNumber = toLineNumber(line);
                    String space = String.format("%" + lineNumber.length() + "s", " ");//NOI18N
                    // add prev line if any
                    int prevLineIndex = line - 1;
                    int addedBeforeIssueLines = 0;
                    while (prevLineIndex > 0) {
                        String lineText = asLines.get(prevLineIndex - 1);
                        if (!lineText.isEmpty()) {
                            lines.add(0, new LineInfo(LineType.SOURCE, lineText, toLineNumber(prevLineIndex), prevLineIndex));
                            if (addedBeforeIssueLines++ > CONTEXT_LINE_NUM) {
                                break;
                            }
                        }
                        currentLineNumber++;
                        prevLineIndex--;
                    }
                    String lineWithIssueText = asLines.get(line - 1);
                    final LineInfo lineInfo = new LineInfo(LineType.ANNOTATION, lineWithIssueText, toLineNumber(line), line);
                    //start column in this line
                    lines.add(lineInfo);
                    int size = codeSnippet.startLineColumns.length;
                    lineInfo.startColumns = new int[size];
                    lineInfo.endColumns = new int[size];
                    for (int i = 0; i < size; i++) {
                        lineInfo.startColumns[i] = codeSnippet.startLineColumns[i][1];
                        lineInfo.endColumns[i] = codeSnippet.endLineColumns[i][1];
                    }

                    
                    int nextLineIndex = line + 1;
                    int addedAfterIssueLines = 0;
                    currentLineNumber++;
                    while (asLines.size() >= nextLineIndex) {
                        String lineText = asLines.get(nextLineIndex - 1);
                        if (!lineText.isEmpty()) {
                            lines.add( new LineInfo(LineType.SOURCE, lineText, toLineNumber(nextLineIndex), nextLineIndex));
                            if (++addedAfterIssueLines >= CONTEXT_LINE_NUM) {
                                break;
                            }
                        }
                        nextLineIndex++;
                        currentLineNumber++;
                    }
                    final AnnotatedCode annotatedCode = new AnnotatedCode(lines, fo.getMIMEType());
                    return annotatedCode;
                }
            }
            String msg = NbBundle.getMessage(CodeSnippet.class, "LBL_NoCodeSnippet", codeSnippet.getFilePath(), line);//NOI18N
            LineInfo lineInfo = new LineInfo(LineType.ERROR, msg, "", -1);//NOI18N
            return new AnnotatedCode(Collections.singletonList(lineInfo), "text/unknown");//NOI18N
        }


        public enum LineType {
            SOURCE,
            ANNOTATION,
            ERROR,
        }

        public static final class LineInfo {

            private final String lineText;
            private final String lineNumberPrefix;
            private final int line;
            private final LineType type;
            private int[] startColumns;
            private int[] endColumns;

            public LineInfo(LineType type, String lineText, String lineNumberPrefix, int line) {
                this.lineText = lineText;
                this.lineNumberPrefix = lineNumberPrefix;
                this.line = line;
                this.type = type;
            }

            @Override
            public String toString() {
                return "LineInfo{" + "lineText=" + lineText + ", lineNumberPrefix=" + lineNumberPrefix + ", line=" + line + '}';//NOI18N
            }

            public LineType getType() {
                return type;
            }

            public int getLine() {
                return line;
            }
            
            int[] getStartColumns() {
                return startColumns;
            }
            
            int[] getEndColumns() {
                return endColumns;
            }            

            public String getPrefix() {
                return lineNumberPrefix;
            }

            public String getText() {
                return lineText;
            }
        }
    }

    private static String toLineNumber(int line) {
        int digits = (int) Math.ceil(Math.log10(line + 1.0));
        String fmt = "%" + digits + "d: ";//NOI18N
        return String.format(fmt, line);
    }
    
   

}
