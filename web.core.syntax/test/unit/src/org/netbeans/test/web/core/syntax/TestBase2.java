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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.test.web.core.syntax;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.gsf.GsfTestBase;
import org.netbeans.modules.gsf.api.Formatter;
import org.netbeans.modules.gsf.spi.DefaultLanguageConfig;
import org.netbeans.modules.web.core.syntax.JSPKit;
import org.netbeans.modules.web.core.syntax.gsf.JspFormatter;
import org.netbeans.modules.web.core.syntax.gsf.JspLanguage;
import org.netbeans.modules.web.jspparser.JspParserImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Common ancestor for all test classes.
 */
public class TestBase2 extends GsfTestBase {


    public TestBase2(String name) {
        super(name);
    }

//    private static final String PROP_MIME_TYPE = "mimeType"; //NOI18N
//
//    protected BaseDocument createDocument() {
//        NbEditorDocument doc = new NbEditorDocument(Css.CSS_MIME_TYPE);
//        doc.putProperty(PROP_MIME_TYPE, Css.CSS_MIME_TYPE);
//        doc.putProperty(Language.class, CSSTokenId.language());
//        return doc;
//    }
    
    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new JspLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return "text/x-jsp";
    }

    @Override
    public Formatter getFormatter(IndentPrefs preferences) {
        if (preferences == null) {
            preferences = new IndentPrefs(4,4);
        }

//        Preferences prefs = MimeLookup.getLookup(MimePath.get(Css.CSS_MIME_TYPE)).lookup(Preferences.class);
//        prefs.putInt(SimpleValueNames.SPACES_PER_TAB, preferences.getIndentation());
        // TODO: XXXX

        Formatter formatter = new JspFormatter();

        return formatter;
    }

    @Override
    protected BaseKit getEditorKit(String mimeType) {
        return new JSPKit(JSPKit.JSP_MIME_TYPE);
    }

    public final void initParserJARs() throws MalformedURLException {
        String path = System.getProperty("jsp.parser.jars");
        StringTokenizer st = new StringTokenizer(path, ":");
        List<URL> list = new ArrayList();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            File f = new File(token);
            if (!f.exists()) {
                fail("cannot find file "+token);
            }
            list.add(f.toURI().toURL());
        }
        JspParserImpl.setParserJARs(list.toArray(new URL[list.size()]));
    }

    public final void copyWebProjectJarsTo(File p) throws MalformedURLException, IOException {
        String path = System.getProperty("web.project.jars");
        StringTokenizer st = new StringTokenizer(path, ":");
        if (p.exists()) {
            return;
        }
        p.mkdir();
        FileObject dest = FileUtil.toFileObject(p);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            File f = new File(token);
            if (!f.exists()) {
                fail("cannot find file "+token);
            }
            FileObject fo = FileUtil.toFileObject(f);
            FileUtil.copyFile(fo, dest, fo.getName(), fo.getExt());
        }
    }

}
