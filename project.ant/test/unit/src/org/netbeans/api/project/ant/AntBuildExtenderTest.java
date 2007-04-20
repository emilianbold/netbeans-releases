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

package org.netbeans.api.project.ant;

import org.netbeans.spi.project.support.ant.*;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ant.AntBuildExtenderAccessor;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Test functionality of AntBuildExtender.
 * @author mkleint
 */
public class AntBuildExtenderTest extends NbTestCase {
    
    public AntBuildExtenderTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private FileObject extension1;
    private ProjectManager pm;
    private Project p;
    private AntProjectHelper h;
    private GeneratedFilesHelper gfh;
    private ExtImpl extenderImpl;
    
    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        TestUtil.createFileFromContent(GeneratedFilesHelperTest.class.getResource("data/project.xml"), projdir, "nbproject/project.xml");
        extension1 = TestUtil.createFileFromContent(GeneratedFilesHelperTest.class.getResource("data/extension1.xml"), projdir, "nbproject/extension1.xml");
        extenderImpl = new ExtImpl();
        TestUtil.setLookup(new Object[] {
            AntBasedTestUtil.testAntBasedProjectType(extenderImpl),
        });
        pm = ProjectManager.getDefault();
        p = pm.findProject(projdir);
        extenderImpl.project = p;
        h = p.getLookup().lookup(AntProjectHelper.class);
        gfh = p.getLookup().lookup(GeneratedFilesHelper.class);
        assertNotNull(gfh);
    }
    
    public void testGetExtendableTargets() {
        AntBuildExtender instance = p.getLookup().lookup(AntBuildExtender.class);

        List<String> result = instance.getExtensibleTargets();

        assertEquals(1, result.size());
        assertEquals("all", result.get(0));
    }

    public void testAddExtension() {
        AntBuildExtender instance = p.getLookup().lookup(AntBuildExtender.class);
        instance.addExtension("milos", extension1);
        Element el = p.getLookup().lookup(AuxiliaryConfiguration.class).getConfigurationFragment(
                AntBuildExtenderAccessor.ELEMENT_ROOT, AntBuildExtenderAccessor.AUX_NAMESPACE, true);
        assertNotNull(el);
        NodeList nl = el.getElementsByTagName(AntBuildExtenderAccessor.ELEMENT_EXTENSION);
        assertEquals(1, nl.getLength());
        Element extens = (Element) nl.item(0);
        assertEquals("milos", extens.getAttribute(AntBuildExtenderAccessor.ATTR_ID));
        assertEquals("extension1.xml", extens.getAttribute(AntBuildExtenderAccessor.ATTR_FILE));
    }

    public void testRemoveExtension() {
        AntBuildExtender instance = p.getLookup().lookup(AntBuildExtender.class);
        testAddExtension();
        extenderImpl.oldElement = p.getLookup().lookup(AuxiliaryConfiguration.class).getConfigurationFragment(
                AntBuildExtenderAccessor.ELEMENT_ROOT, AntBuildExtenderAccessor.AUX_NAMESPACE, true);
        instance.removeExtension("milos");
        Element el = p.getLookup().lookup(AuxiliaryConfiguration.class).getConfigurationFragment(
                AntBuildExtenderAccessor.ELEMENT_ROOT, AntBuildExtenderAccessor.AUX_NAMESPACE, true);

        assertNotNull(el);
        NodeList nl = el.getElementsByTagName(AntBuildExtenderAccessor.ELEMENT_EXTENSION);
        assertEquals(0, nl.getLength());
    }

    public void testGetExtension() {
        AntBuildExtender instance = p.getLookup().lookup(AntBuildExtender.class);
        testAddExtension();
        AntBuildExtender.Extension ext = instance.getExtension("milos");
        assertNotNull(ext);
    }
    
    private class ExtImpl implements AntBuildExtenderImplementation {
        Project project;
        Element newElement;
        Element oldElement;
        List<String> targets = Collections.singletonList("all");

        public List<String> getExtensibleTargets() {
            return targets;
        }

        public void updateBuildExtensionMetadata(Element element) {
            newElement = element;
        }

        public Element getBuildExtensionMetadata() {
            return oldElement;
        }

        public Project getOwningProject() {
            return project;
        }

    }
    
}
