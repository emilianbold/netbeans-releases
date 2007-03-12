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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

/**
 * Creates Ant manual in JavaHelp format.
 * @author Jesse Glick
 * @see <a href="http://svn.apache.org/repos/asf/ant/sandbox/antlibs/manual4eclipse/src/main/org/apache/ant/manual4eclipse/Manual4EclipseTask.java">Eclipse version</a>
 */
public class MakeManual extends Task {

    private File dir;
    public void setDir(File d) {
        dir = d;
    }

    private File hs;
    public void setHS(File f) {
        hs = f;
    }

    public void execute() throws BuildException {
        if (dir == null || hs == null) {
            throw new BuildException();
        }
        File antdocs = new File(dir, "ant-docs");
        if (!antdocs.isDirectory()) {
            throw new BuildException();
        }
        try {
            String[] stylesheets = {"antmanual.css", "style.css"};
            for (int i = 0; i < stylesheets.length; i++) {
                OutputStream os = new FileOutputStream(new File(antdocs, "stylesheets/" + stylesheets[i]));
                try {
                    PrintStream ps = new PrintStream(os);
                    ps.println("/* This document was modified on " + new Date() + " by ant.netbeans.org to meet accessibility guidelines. */");
                    ps.println("@import \"../../../../../../../netbeans/modules/usersguide/ide.css\";");
                    ps.flush();
                } finally {
                    os.close();
                }
            }
            Properties taskdefs = new Properties();
            taskdefs.load(Project.class.getResourceAsStream("/org/apache/tools/ant/taskdefs/defaults.properties"));
            Properties typedefs = new Properties();
            typedefs.load(Project.class.getResourceAsStream("/org/apache/tools/ant/types/defaults.properties"));
            TocItem top = tockify(antdocs, "toc.html", null, null, new HashSet/*<String>*/(), taskdefs.keySet(), typedefs.keySet());
            writeMapAndToc(top);
        } catch (Exception x) {
            throw new BuildException(x);
        }
        // XXX delete old TOC.toc and Map.jhm when done
    }

    private static int depth = 0;
    private TocItem tockify(File antdocs, String path, String anchor, String linktext, Set/*<String>*/ files, Set/*<String>*/ tasknames, Set/*<String>*/ typenames) throws Exception {
        String file = anchor != null ? path + "#" + anchor : path;
        if (!files.add(file)) {
            return null;
        }
        File f = new File(antdocs, path.replace('/', File.separatorChar));
        if (!f.isFile()) {
            return null;
        }
        String id;
        String token = anchor != null ? anchor : path.replaceFirst("\\.html$", "").replaceFirst("^.+/", "");
        String linktextlower = linktext != null ? linktext.toLowerCase(Locale.US) : "";
        if (tasknames.contains(token)) {
            id = "org.apache.tools.ant.module.tasks." + token;
        } else if (tasknames.contains(linktextlower)) {
            id = "org.apache.tools.ant.module.tasks." + linktextlower;
        } else if (typenames.contains(token)) {
            id = "org.apache.tools.ant.module.types." + token;
        } else if (typenames.contains(linktextlower)) {
            id = "org.apache.tools.ant.module.types." + linktextlower;
        } else {
            id = "org.apache.tools.ant.module.ant-docs." + path.replaceFirst("\\.html$", "").replace('/', '.');
            if (anchor != null) {
                id += "." + anchor;
            }
        }
        String title = linktext != null ? linktext : "Ant 1.7.0 Manual";
        String log = "";
        for (int i = 0; i < depth; i++) {
            log += "\t";
        }
        log(log + path + " as " + id + ": \"" + title + "\"");
        StringBuffer contents = new StringBuffer();
        Reader r = new FileReader(f);
        try {
            int c;
            while ((c = r.read()) != -1) {
                contents.append((char) c);
            }
        } finally {
            r.close();
        }
        /*
        boolean irrelevantForNb =
                path.matches("(ide|running|proxy|cover)\\.html|Integration/\\.*") |
                (path.equals("install.html") && !"librarydependencies".equals(anchor));
        if (irrelevantForNb) {
            title = "(" + title + ")";
        }
         */
        TocItem ti = new TocItem(id, file, title);
        if (path.matches("(toc|.+list|tutorials)\\.html")) {
            depth++;
            Matcher m = Pattern.compile("<a href=\"([^\"#]+)(#([^\"]+))?\".*?>(.+?)</a>", Pattern.CASE_INSENSITIVE).matcher(contents);
            while (m.find()) {
                String relurl = m.group(1);
                String newanchor = m.group(3);
                String newpath = URI.create(path).resolve(relurl).toString();
                if (path.equals("coretasklist.html") && newpath.equals("optionaltasklist.html")) {
                    // Wait until the next link to it.
                    continue;
                }
                if (path.equals("developlist.html") && newpath.startsWith("tutorial-")) {
                    // Ditto.
                    continue;
                }
                String newlinktext = m.group(4).replaceAll("<[iI]>(.+?)</[iI]>", "($1)").replace("&amp;", "&");
                TocItem subitem = tockify(antdocs, newpath, newanchor, newlinktext, files, tasknames, typenames);
                if (subitem != null) {
                    ti.subitems.add(subitem);
                }
            }
            depth--;
        }
        return ti;
    }

    private void writeMapAndToc(TocItem top) throws Exception {
        Document mapXml = createDocument("map", "-//Sun Microsystems Inc.//DTD JavaHelp Map Version 2.0//EN", "http://java.sun.com/products/javahelp/map_2_0.dtd");
        mapXml.getDocumentElement().setAttribute("version", "2.0");
        Document tocXml = createDocument("toc", "-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 2.0//EN", "http://java.sun.com/products/javahelp/toc_2_0.dtd");
        tocXml.getDocumentElement().setAttribute("version", "2.0");
        insertTocItem(top, mapXml, tocXml.getDocumentElement(), new HashMap());
        write(new File(dir, "map.xml"), mapXml);
        write(new File(dir, "toc.xml"), tocXml);
    }
    private static Element insertTocItem(TocItem ti, Document mapXml, Element toc, Map mapEntries) {
        Element el = toc.getOwnerDocument().createElement("tocitem");
        if (ti.id != null) {
            if (mapEntries.containsKey(ti.id) && !mapEntries.get(ti.id).equals(ti.file)) {
                throw new IllegalArgumentException("Dupes: " + ti.id + " " + ti.file + " " + mapEntries.get(ti.id));
            }
            mapEntries.put(ti.id, ti.file);
            el.setAttribute("target", ti.id);
            Element map = mapXml.createElement("mapID");
            map.setAttribute("target", ti.id);
            map.setAttribute("url", "ant-docs/" + ti.file);
            mapXml.getDocumentElement().appendChild(map);
        }
        toc.appendChild(el);
        el.setAttribute("text", ti.text);
        Iterator it = ti.subitems.iterator();
        while (it.hasNext()) {
            insertTocItem((TocItem) it.next(), mapXml, el, mapEntries);
        }
        return el;
    }
    private static class TocItem {
        final String id;
        final String file;
        final String text;
        final List subitems = new ArrayList();
        public TocItem(String id, String file, String text) {
            assert id != null ^ file == null;
            assert text != null;
            this.id = id;
            this.file = file;
            this.text = text;
        }
    }

    // The below loosely copied from org.openide.xml.XMLUtil.

    private static final String IDENTITY_XSLT_WITH_INDENT =
            "<xsl:stylesheet version='1.0' " +
            "xmlns:xsl='http://www.w3.org/1999/XSL/Transform' " +
            "xmlns:xalan='http://xml.apache.org/xslt' " +
            "exclude-result-prefixes='xalan'>" +
            "<xsl:output method='xml' indent='yes' xalan:indent-amount='4'/>" +
            "<xsl:template match='@*|node()'>" +
            "<xsl:copy>" +
            "<xsl:apply-templates select='@*|node()'/>" +
            "</xsl:copy>" +
            "</xsl:template>" +
            "</xsl:stylesheet>";
    private static void write(File xml, Document doc) throws Exception {
        OutputStream out = new FileOutputStream(xml);
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer(
                    new StreamSource(new StringReader(IDENTITY_XSLT_WITH_INDENT)));
            DocumentType dt = doc.getDoctype();
            if (dt != null) {
                String pub = dt.getPublicId();
                if (pub != null) {
                    t.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, pub);
                }
                t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dt.getSystemId());
            }
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            Source source = new DOMSource(doc);
            Result result = new StreamResult(out);
            t.transform(source, result);
        } finally {
            out.close();
        }
    }

    private static Document createDocument(String root, String doctypePublicID, String doctypeSystemID) throws Exception {
        DOMImplementation impl = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
        DocumentType dtd = null;
        if (doctypeSystemID != null) {
            dtd = impl.createDocumentType(root, doctypePublicID, doctypeSystemID);
        }
        return impl.createDocument(null, root, dtd);
    }

}
