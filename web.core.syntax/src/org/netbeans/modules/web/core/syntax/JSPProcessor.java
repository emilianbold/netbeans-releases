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
package org.netbeans.modules.web.core.syntax;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.core.syntax.spi.JspColoringData;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.Node.IncludeDirective;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.web.jsps.parserapi.Node.Visitor;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class JSPProcessor {

    protected Document doc;
    protected FileObject fobj;
    protected static final Logger logger = Logger.getLogger(JSPProcessor.class.getName());
    protected boolean processCalled = false;
    protected boolean processingSuccessful = true;

    protected String createBeanVarDeclarations(List<String> localBeans) {
        //TODO: the parser data contains no information about offsets and
        //therefore it is not possible to create proper java embeddings
        //inside bean declarations. We need a similar solution to what was
        //done for imports, see issue #161246
        StringBuilder beanDeclarationsBuff = new StringBuilder();

        PageInfo pageInfo = getPageInfo();

        if (pageInfo != null) {
            PageInfo.BeanData[] beanData = getBeanData();

            if (beanData != null) {
                for (PageInfo.BeanData bean : beanData) {
                    if (!localBeans.contains(bean.getId())) {
                        beanDeclarationsBuff.append(bean.getClassName() + " " + bean.getId() + ";\n"); //NOI18N
                    }
                }
            }

            if (pageInfo.isTagFile()) {
                for (TagAttributeInfo info : pageInfo.getTagInfo().getAttributes()) {
                    if (info.getTypeName() != null) { // will be null e.g. for fragment attrs
                        if (!localBeans.contains(info.getName())) {
                            beanDeclarationsBuff.append(info.getTypeName() + " " + info.getName() + ";\n"); //NOI18N
                        }
                    }
                }
            }
        }

        JspSyntaxSupport syntaxSupport = JspSyntaxSupport.get(doc);
        JspColoringData coloringData = JspUtils.getJSPColoringData(fobj);

        if (coloringData != null && coloringData.getPrefixMapper() != null) {
            Collection<String> prefixes = coloringData.getPrefixMapper().keySet();
            TagData fooArg = new TagData((Object[][]) null);

            for (String prefix : prefixes) {
                List<TagInfo> tags = syntaxSupport.getAllTags(prefix, false); //do not require fresh data - #146762

                for (TagInfo tag : tags) {
                    // #146754 - prevent NPE:
                    if (tag == null) {
                        continue;
                    }
                    VariableInfo vars[] = tag.getVariableInfo(fooArg);

                    if (vars != null) {
                        for (VariableInfo var : vars) {
                            // Create Variable Definitions
                            if (var.getVarName() != null && var.getClassName() != null && var.getDeclare()) {
                                String varDeclaration = var.getClassName() + " " + var.getVarName() + ";\n";
                                beanDeclarationsBuff.append(varDeclaration);
                            }
                        }
                    }
                }
            }
        }

        return beanDeclarationsBuff.toString();
    }

    protected PageInfo getPageInfo() {
        JspParserAPI.ParseResult parseResult = JspUtils.getCachedParseResult(fobj, true, false);

        if (parseResult != null) {
            return parseResult.getPageInfo();
        }

        //report error but do not break the entire CC
        logger.log(Level.INFO, null, "PageInfo obtained from JspParserAPI.ParseResult is null");

        return null;
    }

    private PageInfo.BeanData[] getBeanData() {

        PageInfo pageInfo = getPageInfo();
        //pageInfo can be null in some cases when the parser cannot parse
        //the webmodule or the page itself
        if (pageInfo != null) {
            return pageInfo.getBeans();
        }

        //TagLibParseSupport support = (dobj == null) ?
        //null : (TagLibParseSupport)dobj.getCookie(TagLibParseSupport.class);
        //return support.getTagLibEditorData().getBeanData();
        return null;
    }

    protected void assureProcessCalled() {
        if (!processCalled) {
            throw new IllegalStateException("process() method must be called first!"); //NOI18N
        }
    }

    protected void processIncludes() {
        PageInfo pageInfo = getPageInfo();

        if (pageInfo == null) {
            //if we do not get pageinfo it is unlikely we will get something reasonable from
            //jspSyntax.getParseResult()...
            return;
        }

        final Collection<String> processedFiles = new TreeSet<String>(processedIncludes());
        processedFiles.add(fobj.getPath());

        if (pageInfo.getIncludePrelude() != null) {
            for (String preludePath : (List<String>) pageInfo.getIncludePrelude()) {
                processIncludedFile(preludePath, processedFiles);
            }
        }

        Visitor visitor = new Visitor() {

            @Override
            public void visit(IncludeDirective includeDirective) throws JspException {
                String fileName = includeDirective.getAttributeValue("file");
                processIncludedFile(fileName, processedFiles);
            }
        };

        JspSyntaxSupport jspSyntax = JspSyntaxSupport.get(doc);
        try {
            JspParserAPI.ParseResult parseResult = jspSyntax.getParseResult();

            if (parseResult != null && parseResult.getNodes() != null) {
                parseResult.getNodes().visit(visitor);
            }
        } catch (JspException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void processIncludedFile(String filePath, Collection<String> processedFiles) {
        FileObject includedFile = JspUtils.getFileObject(doc, filePath);

        if (includedFile != null && includedFile.canRead() // prevent endless loop in case of a circular reference
                && !processedFiles.contains(includedFile.getPath())) {

            processedFiles.add(includedFile.getPath());

            try {
                DataObject includedFileDO = DataObject.find(includedFile);
                String mimeType = includedFile.getMIMEType();

                if ("text/x-jsp".equals(mimeType) || "text/x-tag".equals(mimeType)) { //NOI18N
                    EditorCookie editor = includedFileDO.getCookie(EditorCookie.class);

                    if (editor != null) {
                        IncludedJSPFileProcessor includedJSPFileProcessor = new IncludedJSPFileProcessor((BaseDocument) editor.openDocument(), processedFiles);
                        includedJSPFileProcessor.process();
                        processIncludedFile(includedJSPFileProcessor);
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void process() throws BadLocationException {
        processCalled = true;

        //workaround>>> issue #120195 - Deadlock in jspparser while reformatting JSP
        if (DocumentUtilities.isWriteLocked(doc)) {
            processingSuccessful = false;
            return;
        }
        //<<<workaround

        //get fileobject
        fobj = NbEditorUtilities.getFileObject(doc);
        if(fobj == null) {
            //do not handle non fileobject documents like coloring properties preview document
            processingSuccessful = false;
            return;
        }

        //do not process broken source
        JspParserAPI.ParseResult parseResult = JspUtils.getCachedParseResult(fobj, false, false);
        if (parseResult == null || !parseResult.isParsingSuccess()) {
            processingSuccessful = false;
            return;
        }

        final BadLocationException[] ble = new BadLocationException[1];
        doc.render(new Runnable() {

            public void run() {
                try {
                    renderProcess();
                } catch (BadLocationException ex) {
                    ble[0] = ex; //save
                }
            }
        });
        if (ble[0] != null) {
            throw ble[0]; //just rethrow to this level
        }
    }

    protected abstract void processIncludedFile(IncludedJSPFileProcessor includedJSPFileProcessor);
    
    protected abstract void renderProcess() throws BadLocationException;

    /**
     * Add extra imports according to information obtained from the JSP parser
     *
     * @param localImports imports already included in the Simplified Servlet
     * by the processImportDirectives method
     */
    protected String createImplicitImportStatements(List<String> localImports) {
        StringBuilder importsBuff = new StringBuilder();
        String[] imports = getImportsFromJspParser();

        if (imports == null || imports.length == 0) {
            processingSuccessful = false;
        } else {
            // TODO: better support for situation when imports is null
            // (JSP doesn't belong to a project)
            for (String pckg : imports) {
                if (!localImports.contains(pckg)) {
                    importsBuff.append("import " + pckg + ";\n"); //NOI18N
                }
            }
        }

        return importsBuff.toString();
    }

    private String[] getImportsFromJspParser() {
        PageInfo pi = getPageInfo();
        if (pi == null) {
            //we need at least some basic imports
            return new String[]{"javax.servlet.*", "javax.servlet.http.*", "javax.servlet.jsp.*"};
        }
        List<String> imports = pi.getImports();
        return imports == null ? null : imports.toArray(new String[imports.size()]);
    }

    protected abstract Collection<String> processedIncludes();
}
