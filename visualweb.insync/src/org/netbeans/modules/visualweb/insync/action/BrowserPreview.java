/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.visualweb.insync.action;

import org.netbeans.modules.visualweb.api.designerapi.DesignerServiceHack;
import org.netbeans.modules.visualweb.insync.InSyncServiceProvider;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.openide.ErrorManager;
import org.openide.LifecycleManager;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.insync.Util;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.netbeans.modules.visualweb.insync.markup.JspxSerializer;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;


// XXX Moved from designer.
/**
 * Preview a webform in the browser
 * @todo Rewrite to align more with box tree. I'm already depending on
 *   this for the jsp-include stuff.
 * @todo "Escape" characters into entities! The DOM parser may take
 *   e.g. &nbsp and convert it into char 160; I need to undo that
 *   transformation when going back into html!
 * @todo Handle attributes correctly; might possible share some code
 *   with the PrettyJspWriter in jsfsupport which has similar needs.
 * @author Tor Norbye
 */
// TODO Make package private when removed old outline impl.
public class BrowserPreview {
    private static Map jarCache;
//    private WebForm webform;
    private FacesModel facesModel;

    public BrowserPreview(/*WebForm webform*/FacesModel facesModel) {
//        this.webform = webform;
        this.facesModel = facesModel;
    }

    /** Preview the current document in the browser */
    public void preview() {
        // We should first force a save all, like execute project does,
        // to ensure that for example referenced stylesheets that have been
        // edited will be reflected correctly
        LifecycleManager.getDefault().saveAll();

        // Dump out html content in a temporary file
        File file = null;

        try {
            // On the Mac at least, .xml will open some .xml editor,
            // .xhtml opens IE instead of Safari... so for now stay with
            // the .html extension even if that doesn't force the right
            // xml processing mode on say Mozilla
            //file = File.createTempFile("browserpreview", ".xml"); // NOI18N
            file = File.createTempFile("browserpreview", ".html"); // NOI18N
            file.deleteOnExit();

            Writer writer;

            try {
//                String encoding = null;
//                if (webform.getMarkup() != null) {
//                    encoding = webform.getMarkup().getEncoding();
//                }
                MarkupUnit markupUnit = facesModel.getMarkupUnit();
                String encoding = markupUnit == null ? null : markupUnit.getEncoding();

                if (encoding != null) {
                    writer = new OutputStreamWriter(new FileOutputStream(file), encoding);
                } else {
                    writer = new OutputStreamWriter(new FileOutputStream(file));
                }
            } catch (UnsupportedEncodingException ue) {
                writer = new FileWriter(file);
            }

//            if (DesignerUtils.isBraveheartPage(webform.getDom())) {
//            if (DesignerServiceHack.getDefault().isBraveheartPage(facesModel.getJspDom())) {
            if (InSyncServiceProvider.get().isBraveheartPage(facesModel.getJspDom())) {
                // Braveheart pages already write out DOCTYPEs from the page component, but I've
                // suppressed that in my own internal DOM. Emit it here.
                // Regular (Reef) pages already have <jsp:text> nodes that should take
                // care of this (but verify that)
                writer.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" + // NOI18N
                    "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"); // NOI18N
            }

            boolean fragment = false;

            try {
                // TODO - handle page previewing here
//                Element bodyElement = webform.getBody();
                Element bodyElement = facesModel.getHtmlBody();
                String bodyTag = null;

                if (bodyElement != null) {
                    bodyTag = bodyElement.getTagName();
                }

                if ((bodyTag != null) && !bodyTag.equals(HtmlTag.BODY.name) &&
                        !bodyTag.equals(HtmlTag.FRAMESET.name)) {
                    fragment = true;

                    // what about \r\n's etc?
                    writer.write("<html>\n"); // NOI18N
                    injectHeadStart(writer);

                    // Write in theme links: fetch them from the context page
//                    WebForm page = webform.getContextPage();
                    FileObject contextFile = DesignerServiceHack.getDefault().getContextFileForFragmentFile(facesModel.getMarkupFile());
                    FacesModel page = contextFile == null ? null : FacesModel.getInstance(contextFile);

                    if (page != null) {
                        DocumentFragment df = page.getHtmlDomFragment();

                        if (df != null) {
                            Node head = Util.findDescendant(HtmlTag.HEAD.name, df);

                            if (head != null) {
                                NodeList nl = head.getChildNodes();

                                for (int i = 0, n = nl.getLength(); i < n; i++) {
                                    Node node = nl.item(i);

                                    if (node.getNodeName().equals(HtmlTag.SCRIPT.name) ||
                                            node.getNodeName().equals(HtmlTag.LINK.name)) {
                                        dump(writer, node, 0);
                                    }
                                }
                            }
                        }
                    }

//                    writer.write("<title>" + webform.getMarkup().getFileObject().getNameExt() +
//                        "</title></head>\n<body>\n"); // NOI18N
                    writer.write("<title>" + facesModel.getMarkupUnit().getFileObject().getNameExt() +
                        "</title></head>\n<body>\n"); // NOI18N
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Skip head etc.
// <removing set/getRoot from RaveDocument>
//            dump(writer, webform.getDom().getRoot(), 0);
// ====
            DocumentFragment html = facesModel.getHtmlDomFragment();
            if (html == null) {
                // XXX #6469774 NPE.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new NullPointerException("Null Html Dom Fragment from FacesModel, it is probably invalid, facesModel=" + facesModel)); // NOI18N
                return;
            }
            
            Node effectiveRoot = null;
            NodeList nl = html.getChildNodes();
            for (int i = 0, n = nl.getLength(); i < n; i++) {
                Node node = nl.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    effectiveRoot = node;
                    break;
                }
            }
            dump(writer, effectiveRoot, 0);
// <removing set/getRoot from RaveDocument>
            
            if (fragment) {
                writer.write("</body>\n</html>\n"); // NOI18N
            }

            writer.close();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);

            return;
        }

        // Show in Browser
        URL url = null;

        try {
            // TODO what about inserting a / infront of the C:\ in paths
            // etc?? See MarkupUnit.getBase() - why does this work?
            // and what about calling MarkupUnit.toURL here to escape
            // \'s etc.?
            url = new URL("file:" + file.getPath()); // NOI18N
        } catch (MalformedURLException e) {
            // Can't show URL
            ErrorManager.getDefault().notify(e);

            return;
        }

        URLDisplayer.getDefault().showURL(url);

        // TODO -- delete OTHER existing/old browser preview files in the
        // same directory?  Or just record the new file name and stash
        // it in a to-be-deleted list somewhere?
    }

    /** Dump out the DOM to the given writer, skipping whitespace
     * and comments if it feels like it.
     * @todo Unhackify.
     */
    private void dump(Writer writer, Node n, int depth)
        throws IOException {
        String close = null;

        if (n.getNodeType() != Node.ATTRIBUTE_NODE) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)n;

                String tagName = element.getTagName();

                FileObject markupFile = facesModel.getMarkupFile();
                DataObject dob;
                try {
                    dob = DataObject.find(markupFile);
                } catch (DataObjectNotFoundException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    dob = null;
                }
//                if (HtmlTag.HEAD.name.equals(tagName) && (webform.getDataObject() != null)) {
                if (HtmlTag.HEAD.name.equals(tagName) && (dob != null)) {
                
                    injectHeadStart(writer);
                    close = HtmlTag.HEAD.name;
                } else if (HtmlTag.BASE.name.equals(tagName)) {
                    // We've put a <base> tag into the head already
                    return;
                } else if (HtmlTag.JSPINCLUDE.name.equals(tagName) || HtmlTag.JSPINCLUDEX.name.equals(tagName)) {
//                    CssBox includeBox = CssBox.getBox(element);
//
//                    if ((includeBox != null) && includeBox instanceof JspIncludeBox) {
//                        WebForm frameForm = ((JspIncludeBox)includeBox).getExternalForm();
//
//                        if ((frameForm != null) && (frameForm != WebForm.EXTERNAL)) {
//                            // Recurse
//                            Element body = frameForm.getBody();
//                            dump(writer, body, depth + 1);
//                        }
//                    }
                    FileObject externalFormFile = DesignerServiceHack.getDefault().getExternalFormFileForElement(element);
                    FacesModel frameForm = externalFormFile == null ? null : FacesModel.getInstance(externalFormFile);
                    if (frameForm != null) {
                        Element body = frameForm.getHtmlBody();
                        dump(writer, body, depth + 1);
                    }
                } else if (tagName.equals(HtmlTag.FSUBVIEW)) { // NOI18N

                    // Skip
                } else if (tagName.equals("f:view")) { // NOI18N

                    // Skip
                } else if (!tagName.startsWith("jsp:")) { // NOI18N // Skip meta stuff
                    writer.write('<');
                    writer.write(tagName);

                    // Later, if we use class="jsferror" instead of inlining styles
                    // in the mock container, we need to insert the styles here
                    //boolean fixStyle = false;
                    //boolean processed = false;
                    //if (element.getAttribute("class").equals("jsferror")) {
                    //    fixStyle = true;
                    //}
                    int num = element.getAttributes().getLength();

                    for (int i = 0; i < num; i++) {
                        Node a = element.getAttributes().item(i); // XXX move element.getAttributes out of loop
                        writer.write(' ');

                        String name = a.getNodeName();
                        writer.write(name);
                        writer.write('=');
                        writer.write('"');

                        //if (fixStyle && a.getNodeName().equals("style")) {
                        //    writer.write(a.getNodeValue()); // XXX TODO: escape "'s, &'s, etc.
                        //    writer.write(';');
                        //    writer.write(getErrorCss());
                        //    processed = true;
                        //} else {
                        //writer.write(a.getNodeValue()); // XXX TODO: escape "'s, &'s, etc.
                        String val = a.getNodeValue();

                        if (name.equals(HtmlAttribute.SRC) || name.equals(HtmlAttribute.HREF)) {
                            if (val.startsWith("jar:file:")) { // NOI18N

                                // We have to translate jar urls because
                                // some browsers like IE and Safari don't
                                // understand them
                                val = translateJarUrl(val);
                            } else if (val.startsWith("/")) {
                                // Context relative path - just strip it and rely
                                // on base tag
                                val = val.substring(1);
                            }
                        }

                        for (int j = 0, m = val.length(); j < m; j++) {
                            char c = val.charAt(j);

                            switch (c) {
                            case '"':
                                writer.write("&quot;");

                                break; // NOI18N

                            /* Don't escape '.  It doesn't seem to be
                               necessary - the XML parser will not be confused
                               by an apostrophy in the middle of a quote;
                               and more importantly translating these to
                               &apos; will break javascript attributes
                               (like onmouseout) on Explorer. Mozilla handles
                               these correctly.
                            case '\'': writer.write("&apos;"); break; // NOI18N
                            */
                            case '<':
                                writer.write("&lt;");

                                break; // NOI18N

                            case '>':
                                writer.write("&gt;");

                                break; // NOI18N

                            case '&':
                                writer.write("&amp;");

                                break; // NOI18N

                            default:
                                writer.write(c);

                                break;
                            }
                        }

                        //}
                        writer.write('"');
                    }

                    //if (fixStyle && !processed) { // Still haven't inserted the jsferror stuff
                    //    writer.write(" style=\"");
                    //    writer.write(getErrorCss()); // look up CSS string for class=jsferror
                    //    writer.write('"');
                    //}
                    boolean closeImmediately = JspxSerializer.canMinimizeTag(tagName);

                    if (closeImmediately) {
                        writer.write(' ');
                        writer.write('/');
                    }

                    writer.write('>');
                    close = tagName;

                    if (closeImmediately) {
                        close = null;
                    }
                }
            } else if (n.getNodeType() == Node.TEXT_NODE) {
                String str = n.getNodeValue();

                // We don't want to strip spaces
                //if (!Utilities.onlyWhitespace(str)) {
                //
                boolean windows = org.openide.util.Utilities.isWindows();

                for (int i = 0, max = str.length(); i < max; i++) {
                    char c = str.charAt(i);

                    if (windows && (c == '\n')) {
                        // On Windows? - fix newline issue so View
                        // Source looks okay Change \n's into \r\n -
                        // unless we already have \r's in there.
                        writer.write('\n');
                        writer.write('\r');
                    } else if (c == '<') {
                        writer.write("&lt;"); // NOI18N
                    } else if (c == '>') {
                        writer.write("&gt;"); // NOI18N

                        //  XXX should we also change & -> &amp;, " -> &quot; and ' -> &apos; ??
                    } else {
                        writer.write(c);
                    }
                }

                //}
            } else if (n.getNodeType() == Node.CDATA_SECTION_NODE) {
                writer.write(n.getNodeValue()); // TODO Windows \r\n handling??
            } else if (n.getNodeType() == Node.COMMENT_NODE) {
                // Needed at browser preview for components which emit
                // script tags for example with comments embedded for
                // browser compatibility
                writer.write("<!--"); // NOI18N
                writer.write(n.getNodeValue()); // TODO Windows \r\n handling??
                writer.write("-->"); // NOI18N
            }

            //            else {
            //                //System.out.println("Skipping node " + n);
            //            }
        }

        if (n.hasChildNodes()) {
            NodeList list = n.getChildNodes();
            int len = list.getLength();

            for (int i = 0; i < len; i++) {
                dump(writer, list.item(i), depth + 1);
            }
        }

        if (close != null) {
            writer.write('<');
            writer.write('/');
            writer.write(close);
            writer.write('>');
        }
    }

    /** Given a JAR URL (jar:file:/foo/bar/baz.jar!/foo/bar/baz.css)
     * translate it to a file in the userdir's cache directory, and
     * return a URL pointing to this new file. This enables browsers that
     * don't support the jar: url (which means pretty much everybody except
     * Firefox/Mozilla) to view the preview contents.
     *
     * The jars are cached such that they only are extracted when a cached copy
     * is not found.
     *
     * @todo Figure out if we need to refresh this copy occasionally.
     *
     * @param A jar-syntax url
     * @return A file url, or if parsing the jar url fails (or extracting the file
     *   fails) the original url.
     */
    private String translateJarUrl(String jarUrl) {
        URL source = null;

        try {
            source = new URL(jarUrl);
        } catch (MalformedURLException mfue) {
            ErrorManager.getDefault().notify(mfue);

            return jarUrl;
        }

        if (jarCache == null) {
            jarCache = new HashMap();
        }

        String s = source.getFile();

        // Decode %20's etc.
        // <markup_separation>
//        s = MarkupUnit.fromURL(s);
        // ====
        s = InSyncServiceProvider.get().fromURL(s);
        // </markup_separation>

        if (s.startsWith("file:")) {
            s = s.substring(5);
        }

        int bang = s.indexOf('!');

        if (bang == -1) {
            return jarUrl;
        }

        String jar = s.substring(0, bang);
        String file = s.substring(bang + 2); // skip !, and skip first /

        String dirName = jar.substring(jar.lastIndexOf('/'));
        int n = dirName.length();
        StringBuffer sb = new StringBuffer(n);

        for (int i = 0; i < n; i++) {
            char c = dirName.charAt(i);

            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            }
        }

        if (sb.length() == 0) {
            return jarUrl;
        }

        // TODO Should I extract the version number too and put that on the dir?
        dirName = sb.toString();

        String cacheDir = (String)jarCache.get(dirName);

        if (cacheDir == null) {
            cacheDir = extract(jar, dirName);

            if (cacheDir == null) { // IO error extracting jar

                return jarUrl;
            }

            jarCache.put(dirName, cacheDir);
        }

        try {
            return new File(cacheDir, file).toURI().toURL().toExternalForm();
        } catch (MalformedURLException mue) {
            return jarUrl;
        }
    }

    /**
     * Given a jar file and a dir name extract the jar file into the dirname in
     * the cache directory
     */
    private String extract(String jar, String dirname) {
        String cacheDir = System.getProperty("netbeans.user") + "/var/cache/" + dirname;

        try {
            File f = new File(cacheDir);

            if (f.exists()) {
                // Cache already exists. Assume current. Perhaps we should look
                // at the time and occasionally regenerate - or is there a way to
                // look at the checksum of the jar perhaps?
                return cacheDir;
            }

            String message = NbBundle.getMessage(BrowserPreview.class, "ExtractingJars");
            StatusDisplayer.getDefault().setStatusText(message);

            f.mkdirs();

            FileObject fo = FileUtil.toFileObject(f);
            InputStream is = new BufferedInputStream(new FileInputStream(new File(jar)));
            FileUtil.extractJar(fo, is);

            return cacheDir;
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);

            return null;
        } finally {
            StatusDisplayer.getDefault().setStatusText("");
        }
    }

    private void injectHeadStart(Writer writer) throws IOException {
        // Inject a base to make urls work okay
        writer.write('<');
        writer.write(HtmlTag.HEAD.name);
        writer.write('>');

        //URI uri = webform.getMarkup().getBaseURI();
        // The components seem to render project-relative paths (/resources/foo.gif becomes resources/foo.gif)
        // so I should use the project as the base!
//        FileObject webroot = JsfProjectUtils.getDocumentRoot(webform.getProject());
        FileObject webroot = JsfProjectUtils.getDocumentRoot(facesModel.getProject());
        
        URI uri = FileUtil.toFile(webroot).toURI();

        if (uri != null) {
            writer.write("<base href=\""); // NOI18N
            writer.write(uri.toASCIIString());
            writer.write("\" />\n"); // NOI18N

            // Set up encoding
            String content = getContentType();
            writer.write("<meta http-equiv=\"Content-Type\" content=\""); // NOI18N
            writer.write(content);
            writer.write("\"/>\n"); // NOI18N
        }
    }

    private String getContentType() {
        String content = "text/html;charset=UTF-8"; // NOI18N

        // Look for the jsp directive
//        Element root = webform.getDom().getDocumentElement();
        Element root = facesModel.getJspDom().getDocumentElement();
                
        Element e = MarkupUnit.getFirstDescendantElement(root, "jsp:directive.page"); // NOI18N

        if ((e != null) && e.hasAttribute("contentType")) { // NOI18N
            content = e.getAttribute("contentType"); // NOI18N
        }

        return content;
    }
}
