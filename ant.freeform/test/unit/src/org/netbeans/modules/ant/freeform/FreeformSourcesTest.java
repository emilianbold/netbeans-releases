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

import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.openide.filesystems.FileObject;
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
        Element data = extsrcroot.helper().getPrimaryConfigurationData(true);
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
        extsrcroot.helper().putPrimaryConfigurationData(data, true);
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
    
}
