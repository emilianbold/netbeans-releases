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

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// XXX testExternalSourceRootChanges
// - should check that FOQ changes as well

/**
 * Test {@link FreeformSources}.
 * @author Jesse Glick
 */
public class FreeformSourcesTest extends TestBase {
    
    public FreeformSourcesTest(String name) {
        super(name);
    }
    
    public void testSources() throws Exception {
        Sources s = ProjectUtils.getSources(simple);
        SourceGroup[] groups = s.getSourceGroups(Sources.TYPE_GENERIC);
        assertEquals("one generic group", 1, groups.length);
        assertEquals("right root folder", simple.getProjectDirectory(), groups[0].getRootFolder());
        assertEquals("right display name", "Simple Freeform Project", groups[0].getDisplayName());
        groups = s.getSourceGroups("java");
        assertEquals("two Java groups", 2, groups.length);
        assertEquals("right root folder #1", simple.getProjectDirectory().getFileObject("src"), groups[0].getRootFolder());
        assertEquals("right display name #1", "Main Sources", groups[0].getDisplayName());
        assertEquals("right root folder #2", simple.getProjectDirectory().getFileObject("antsrc"), groups[1].getRootFolder());
        assertEquals("right display name #2", "Ant Task Sources", groups[1].getDisplayName());
    }
    
    public void testExternalSourceRoot() throws Exception {
        Sources s = ProjectUtils.getSources(extsrcroot);
        SourceGroup[] groups = s.getSourceGroups(Sources.TYPE_GENERIC);
        assertEquals("one generic group", 1, groups.length);
        assertEquals("right root folder", egdirFO.getFileObject("extsrcroot"), groups[0].getRootFolder());
        assertEquals("right display name", "Top-Level Dir", groups[0].getDisplayName());
        groups = s.getSourceGroups("java");
        assertEquals("one Java group", 1, groups.length);
        assertEquals("right root folder", egdirFO.getFileObject("extsrcroot/src"), groups[0].getRootFolder());
        assertEquals("right display name", "External Sources", groups[0].getDisplayName());
        assertEquals("correct file owner", extsrcroot, FileOwnerQuery.getOwner(egdirFO.getFileObject("extsrcroot/src/org/foo/Foo.java")));
    }
    
    public void testSourceRootChanges() throws Exception {
        Sources s = ProjectUtils.getSources(extsrcroot);
        SourceGroup[] groups = s.getSourceGroups("java");
        assertEquals("one Java group", 1, groups.length);
        assertEquals("right root folder", egdirFO.getFileObject("extsrcroot/src"), groups[0].getRootFolder());
        TestCL l = new TestCL();
        s.addChangeListener(l);
        Element data = extsrcroot.getPrimaryConfigurationData();
        Element folders = Util.findElement(data, "folders", FreeformProjectType.NS_GENERAL);
        assertNotNull("have <folders>", folders);
        List/*<Element>*/ sourceFolders = Util.findSubElements(folders);
        assertEquals("have 2 <source-folder>s", 2, sourceFolders.size());
        Element sourceFolder = (Element) sourceFolders.get(1);
        Element location = Util.findElement(sourceFolder, "location", FreeformProjectType.NS_GENERAL);
        assertNotNull("have <location>", location);
        NodeList nl = location.getChildNodes();
        assertEquals("one child (text)", 1, nl.getLength());
        location.removeChild(nl.item(0));
        location.appendChild(location.getOwnerDocument().createTextNode("../src2"));
        extsrcroot.putPrimaryConfigurationData(data);
        assertEquals("got a change in Sources", 1, l.changeCount());
        groups = s.getSourceGroups("java");
        assertEquals("one Java group", 1, groups.length);
        assertEquals("right root folder", egdirFO.getFileObject("extsrcroot/src2"), groups[0].getRootFolder());
    }
    
    public void testExternalBuildRoot() throws Exception {
        // Check that <build-folder> works.
        FileObject builtFile = egdirFO.getFileObject("extbuildroot/build/built.file");
        assertNotNull("have built.file", builtFile);
        assertEquals("owned by extbuildroot project", extbuildroot, FileOwnerQuery.getOwner(builtFile));
    }

    public void testIncludesExcludes() throws Exception {
        clearWorkDir();
        File d = getWorkDir();
        AntProjectHelper helper = FreeformProjectGenerator.createProject(d, d, "prj", null);
        Project p = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        FileUtil.createData(new File(d, "s/relevant/included/file"));
        FileUtil.createData(new File(d, "s/relevant/excluded/file"));
        FileUtil.createData(new File(d, "s/ignored/file"));
        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        Element sf = (Element) data.insertBefore(doc.createElementNS(Util.NAMESPACE, "folders"), Util.findElement(data, "view", Util.NAMESPACE)).
                appendChild(doc.createElementNS(Util.NAMESPACE, "source-folder"));
        sf.appendChild(doc.createElementNS(Util.NAMESPACE, "label")).appendChild(doc.createTextNode("Sources"));
        sf.appendChild(doc.createElementNS(Util.NAMESPACE, "type")).appendChild(doc.createTextNode("stuff"));
        sf.appendChild(doc.createElementNS(Util.NAMESPACE, "location")).appendChild(doc.createTextNode("s"));
        Util.putPrimaryConfigurationData(helper, data);
        ProjectManager.getDefault().saveProject(p);
        Sources s = ProjectUtils.getSources(p);
        SourceGroup[] gs = s.getSourceGroups("stuff");
        assertEquals(1, gs.length);
        assertEquals(FileUtil.toFileObject(new File(d, "s")), gs[0].getRootFolder());
        assertEquals("Sources", gs[0].getDisplayName());
        assertEquals("ignored{file} relevant{excluded{file} included{file}}", expand(gs[0]));
        // Now configure includes and excludes.
        EditableProperties ep = new EditableProperties();
        ep.put("includes", "relevant/");
        ep.put("excludes", "**/excluded/");
        helper.putProperties("config.properties", ep);
        data = Util.getPrimaryConfigurationData(helper);
        doc = data.getOwnerDocument();
        data.getElementsByTagName("properties").item(0).
                appendChild(doc.createElementNS(Util.NAMESPACE, "property-file")).
                appendChild(doc.createTextNode("config.properties"));
        Util.putPrimaryConfigurationData(helper, data);
        ProjectManager.getDefault().saveProject(p);
        data = Util.getPrimaryConfigurationData(helper);
        doc = data.getOwnerDocument();
        sf = (Element) data.getElementsByTagName("source-folder").item(0);
        sf.appendChild(doc.createElementNS(Util.NAMESPACE, "includes")).
                appendChild(doc.createTextNode("${includes}"));
        sf.appendChild(doc.createElementNS(Util.NAMESPACE, "excludes")).
                appendChild(doc.createTextNode("${excludes}"));
        Util.putPrimaryConfigurationData(helper, data);
        ProjectManager.getDefault().saveProject(p);
        gs = s.getSourceGroups("stuff");
        assertEquals("relevant{included{file}}", expand(gs[0]));
        // Now change them.
        TestPCL l = new TestPCL();
        gs[0].addPropertyChangeListener(l);
        ep = helper.getProperties("config.properties");
        ep.remove("includes");
        helper.putProperties("config.properties", ep);
        ProjectManager.getDefault().saveProject(p);
        assertEquals("ignored{file} relevant{included{file}}", expand(gs[0]));
        assertEquals(Collections.singleton(SourceGroup.PROP_CONTAINERSHIP), l.changed);
    }
    private static String expand(SourceGroup g) {
        return expand(g, g.getRootFolder());
    }
    private static String expand(SourceGroup g, FileObject d) {
        SortedSet<String> subs = new TreeSet<String>();
        for (FileObject kid : d.getChildren()) {
            if (!g.contains(kid)) {
                continue;
            }
            String sub = kid.getNameExt();
            if (kid.isFolder()) {
                sub += '{' + expand(g, kid) + '}';
            }
            subs.add(sub);
        }
        StringBuilder b = new StringBuilder();
        for (String sub : subs) {
            if (b.length() > 0) {
                b.append(' ');
            }
            b.append(sub);
        }
        return b.toString();
    }
    
}
