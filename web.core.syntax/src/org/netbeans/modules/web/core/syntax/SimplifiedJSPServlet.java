/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.syntax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import static org.netbeans.api.jsp.lexer.JspTokenId.JavaCodeType;

/**
 * Utility class for generating a simplified <em>JSP servlet</em> class from a JSP file.
 * Using a full featured JSP parser would be too resource demanding,
 * we need a lightweight solution to be used with code completion.
 *
 * Inputs: original JSP document, caret offset within the original document
 * Outputs: a body of a simplified JSP servlet class, offset of the corresponding
 *          position in the servlet class
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class SimplifiedJSPServlet {

    private static final String CLASS_HEADER = "\nclass SimplifiedJSPServlet extends HttpServlet {\n"; //NOI18N
    private static final String METHOD_HEADER = "\n\tvoid mergedScriptlets(\n"
            + "\t\tHttpServletRequest request,\n" 
            + "\t\tHttpServletResponse response,\n" 
            + "\t\tHttpSession session,\n" 
            + "\t\tServletContext application,\n" 
            + "\t\tJspWriter out,\n" 
            + "\t\tServletConfig config,\n"
            + "\t\tJspContext jspContext,\n" 
            + "\t\tObject page,\n" 
            + "\t\tPageContext pageContext\n"
            + ") throws javax.servlet.ServletException, java.io.IOException {\n\n"; //NOI18N
    private static final String CLASS_FOOTER = "\n\t}\n}"; //NOI18N
    @Deprecated
    private final JspSyntaxSupport sup;

    private final Document doc;
    private final ArrayList<CodeBlockData> codeBlocks = new ArrayList<CodeBlockData>();

    private String mergedScriptlets = null;
    private String mergedDeclarations = null;
    private boolean processCalled = false;
    private String importStatements = null;
    private int expressionIndex = 1;
    private static final Logger logger = Logger.getLogger(SimplifiedJSPServlet.class.getName());

    /** Creates a new instance of ScripletsBodyExtractor */
    public SimplifiedJSPServlet(Document doc) {
        this.doc = doc;
        sup = (JspSyntaxSupport) new JSPKit().createSyntaxSupport((BaseDocument) doc);
    }

    public void process() throws BadLocationException {
        processCalled = true;
        final StringBuilder buffScriplets = new StringBuilder();
        final StringBuilder buffDeclarations = new StringBuilder();
        final BadLocationException[] ex = new BadLocationException[1];

        doc.render(new Runnable() {

            public void run() {
                TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
                TokenSequence tokenSequence = tokenHierarchy.tokenSequence(); //get top level token sequence
                if (!tokenSequence.moveNext()) {
                    return; //no tokens in token sequence
                }

                /**
                 * process java code blocks one by one
                 * note: We count on the fact the scripting language in JSP is Java
                 */
                do {
                    Token token = tokenSequence.token();

                    if (token.id() == JspTokenId.SCRIPTLET) {
                        int blockStart = token.offset(tokenHierarchy);
                        int blockEnd = blockStart + token.length();

                        JavaCodeType blockType = (JavaCodeType) token.getProperty(JspTokenId.SCRIPTLET_TOKEN_TYPE_PROPERTY);

                        try {
                            String blockBody = doc.getText(blockStart, blockEnd - blockStart);
                            StringBuilder buff = blockType == JavaCodeType.DECLARATION ? buffDeclarations : buffScriplets;
                            int newBlockStart = buff.length();

                            if (blockType == JavaCodeType.EXPRESSION) {
                                String exprPrefix = String.format("\t\tObject expr%1$d = ", expressionIndex++); //NOI18N
                                newBlockStart += exprPrefix.length();
                                buff.append(exprPrefix + blockBody + ";\n");
                            } else {
                                buff.append(blockBody + "\n");
                            }

                            CodeBlockData blockData = new CodeBlockData(blockStart, newBlockStart, blockEnd, blockType);
                            codeBlocks.add(blockData);
                        } catch (BadLocationException e) {
                            ex[0] = e;
                            return;
                        }
                    }
                } while (tokenSequence.moveNext());
            }
        });

        if (ex[0] != null) {
            throw ex[0];
        }

        importStatements = createImportStatements();
        mergedDeclarations = buffDeclarations + "\n" + createBeanVarDeclarations();
        mergedScriptlets = buffScriplets.toString();
    }

    private String createBeanVarDeclarations() {
        StringBuilder beanDeclarationsBuff = new StringBuilder();

        PageInfo.BeanData[] beanData = sup.getBeanData();

        if (beanData != null) {
            for (PageInfo.BeanData bean : beanData) {
                beanDeclarationsBuff.append(bean.getClassName() + " " + bean.getId() + ";\n"); //NOI18N
            }
        }

        return beanDeclarationsBuff.toString();
    }

    private String createImportStatements() {
        StringBuilder importsBuff = new StringBuilder();
        String[] imports = sup.getImports();

        if (imports != null) {
            // TODO: better support for situation when imports is null
            // (JSP doesn't belong to a project)
            for (String pckg : imports) {
                importsBuff.append("import " + pckg + ";\n"); //NOI18N
            }
        }

        return importsBuff.toString();
    }

    private void assureProcessCalled() {
        if (!processCalled) {
            throw new IllegalStateException("process() method must be called first!"); //NOI18N
        }
    }

    public int getShiftedOffset(int originalOffset) {
        assureProcessCalled();

        CodeBlockData codeBlock = getCodeBlockAtOffset(originalOffset);

        if (codeBlock == null) {
            return -1; // no embedded java code at the offset
        }

        int offsetWithinBlock = originalOffset - codeBlock.getStartOffset();
        int shiftedOffset = codeBlock.getNewBlockStart() + offsetWithinBlock;

        return shiftedOffset;
    }

    public int getRealOffset(int offset) {
        assureProcessCalled();

        for (CodeBlockData codeBlock : codeBlocks) {
            int len = codeBlock.getEndOffset() - codeBlock.getStartOffset();

            if (codeBlock.getNewBlockStart() <= offset && codeBlock.getNewBlockStart() + len >= offset) {
                return codeBlock.getStartOffset() + offset - codeBlock.getNewBlockStart();
            }
        }

        return -1;
    }

    public String getVirtualClassBody() {
        assureProcessCalled();
        return importStatements + CLASS_HEADER + mergedDeclarations + METHOD_HEADER + mergedScriptlets + CLASS_FOOTER;
    }

    private CodeBlockData getCodeBlockAtOffset(int offset) {

        for (CodeBlockData codeBlock : codeBlocks) {
            if (codeBlock.getStartOffset() <= offset && codeBlock.getEndOffset() >= offset) {
                return codeBlock;
            }
        }

        return null;
    }

    public static abstract class VirtualJavaClass {

        public final void create(Document doc, String virtualClassBody) {
            FileObject fileDummyJava = null;
            List<? extends CompletionItem> javaCompletionItems = null;

            try {
                FileSystem memFS = FileUtil.createMemoryFileSystem();
                fileDummyJava = memFS.getRoot().createData("SimplifiedJSPServlet", "java"); //NOI18N
                PrintWriter writer = new PrintWriter(fileDummyJava.getOutputStream());
                writer.print(virtualClassBody);
                writer.close();

                FileObject jspFile = NbEditorUtilities.getFileObject(doc);
                ClasspathInfo cpInfo = ClasspathInfo.create(jspFile);
                JavaSource source = JavaSource.create(cpInfo, fileDummyJava);
                process(fileDummyJava, source);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            } 
        }

        protected abstract void process(FileObject fileObject, JavaSource javaSource);
    }

    private class CodeBlockData {

        private int startOffset;
        private int endOffset;
        private int newRelativeBlockStart; // offset in created java class
        private JavaCodeType type;

        public CodeBlockData(int startOffset, int newRelativeBlockStart, int endOffset, JavaCodeType type) {
            this.startOffset = startOffset;
            this.newRelativeBlockStart = newRelativeBlockStart;
            this.endOffset = endOffset;
            this.type = type;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        public JavaCodeType getType() {
            return type;
        }

        public int getNewBlockStart() {
            int newBlockStart = newRelativeBlockStart + CLASS_HEADER.length() + importStatements.length();

            if (getType() != JavaCodeType.DECLARATION) {
                newBlockStart += mergedDeclarations.length() + METHOD_HEADER.length();
            }

            return newBlockStart;
        }
    }
}
