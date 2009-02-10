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

package org.netbeans.modules.web.core.syntax.formatting;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.MockServices;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.css.lexer.api.CSSTokenId;
import org.netbeans.modules.gsf.GsfIndentTaskFactory;
import org.netbeans.modules.gsf.GsfReformatTaskFactory;
import org.netbeans.modules.html.editor.NbReaderProvider;
import org.netbeans.modules.java.source.parsing.ClassParserFactory;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.modules.java.source.save.Reformatter;
import org.netbeans.modules.web.core.syntax.EmbeddingProviderImpl;
import org.netbeans.modules.web.core.syntax.JSPKit;
//import org.netbeans.modules.web.core.syntax.JavaSourceProviderImpl;
import org.netbeans.test.web.core.syntax.TestBase2;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 *
 * @author david
 */
public class JspIndenterTest extends TestBase2 {

    public JspIndenterTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        initParserJARs();
        NbReaderProvider.setupReaders();

        
        
        MockServices.setServices(TestLanguageProvider.class, MockMimeLookup.class);
        // init TestLanguageProvider
        Lookup.getDefault().lookup(TestLanguageProvider.class);

        TestLanguageProvider.register(CSSTokenId.language());
        TestLanguageProvider.register(HTMLTokenId.language());
        TestLanguageProvider.register(JspTokenId.language());
        TestLanguageProvider.register(JavaTokenId.language());

        GsfIndentTaskFactory cssReformatFactory = new GsfIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-css"), cssReformatFactory);
        GsfIndentTaskFactory htmlReformatFactory = new GsfIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory);
        Reformatter.Factory factory = new Reformatter.Factory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-java"), factory, new JavacParserFactory(), new ClassParserFactory());
        MockMimeLookup.setInstances(MimePath.parse("text/x-jsp"), new JSPKit("text/x-jsp"), 
                new EmbeddingProviderImpl.Factory(), new GsfIndentTaskFactory(), new GsfReformatTaskFactory());

        //MimeLookup.getLookup(MimePath.parse("text/x-jsp")).lookup(EditorKit.class);
    }

    @Override
    protected BaseDocument getDocument(FileObject fo, String mimeType, Language language) {
        // for some reason GsfTestBase is not using DataObjects for BaseDocument construction
        // which means that for example Java formatter which does call EditorCookie to retrieve
        // document will get difference instance of BaseDocument for indentation
        try {
             DataObject dobj = DataObject.find(fo);
             assertNotNull(dobj);

             EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
             assertNotNull(ec);

             return (BaseDocument)ec.openDocument();
        }
        catch (Exception ex){
            fail(ex.toString());
            return null;
        }
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testFormattingCase001() throws Exception {
        reformatFileContents("FormattingProject/web/case001.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase002() throws Exception {
        reformatFileContents("FormattingProject/web/case002.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase003() throws Exception {
        reformatFileContents("FormattingProject/web/case003.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingCase004() throws Exception {
        /*Project p = ProjectManager.getDefault().findProject(getTestFile("FormattingProject"));
        DataObject dobj = DataObject.find(getTestFile("FormattingProject/web/simple.jsp"));
        System.err.println("dobj="+dobj);
        EditorCookie ec = dobj.getCookie(EditorCookie.class);
        System.err.println("ec="+ec);
        StyledDocument doc = ec.openDocument();*/

        reformatFileContents("FormattingProject/web/case004.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingIssue121102() throws Exception {
        reformatFileContents("FormattingProject/web/issue121102.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingIssue129778() throws Exception {
        reformatFileContents("FormattingProject/web/issue129778.jsp",new IndentPrefs(4,4));
    }

    public void testFormattingIssue89174() throws Exception {
        reformatFileContents("FormattingProject/web/issue89174.jsp",new IndentPrefs(4,4));
    }

}