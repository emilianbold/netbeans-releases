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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.railsprojects;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ruby.RubyTestBase;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup.Result;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;
import org.xml.sax.SAXException;

public class RailsProjectTestBase extends RubyTestBase {

    public RailsProjectTestBase(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    protected RailsProject createTestProject() throws Exception {
        return createTestProject("RubyProject_" + getName());
    }
    
    protected RailsProject createTestProject(String projectName, String... paths) throws Exception {
        File prjDirF = new File(getWorkDir(), projectName);
        RailsProjectGenerator.createProject(prjDirF, projectName, false, null, false, false);
        RubyTestBase.createFiles(prjDirF, paths);
        RailsProject project = (RailsProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(prjDirF));
        assertNotNull(project);
        return project;
    }

    protected RailsProject createTestProjectFromDataFile(final String dataFile) throws Exception {
        RailsProject project = createTestProject("RubyProject");
        createFilesFromDesc(project.getProjectDirectory(), dataFile);
        return project;
    }

    protected void registerLayer() throws Exception {
        MockLookup.setInstances(new Repo(this));
        Result<NodeFactory> result = Lookups.forPath("Projects/org-netbeans-modules-ruby-railsprojects/Nodes").lookupResult(NodeFactory.class);
        assertTrue("layer registered", result.allInstances().size() > 0);
    }

    private static final class Repo extends Repository {

        public Repo(NbTestCase t) throws Exception {
            super(mksystem(t));
        }

        private static FileSystem mksystem(NbTestCase t) throws Exception {
            List<FileSystem> layers = new ArrayList<FileSystem>();
            addLayer(layers, "org/netbeans/modules/ruby/railsprojects/ui/resources/layer.xml");
            return new MultiFileSystem(layers.toArray(new FileSystem[layers.size()]));
        }

        private static void addLayer(List<FileSystem> layers, String layerRes) throws SAXException {
            URL layerFile = Repo.class.getClassLoader().getResource(layerRes);
            assert layerFile != null : layerRes + " found";
            layers.add(new XMLFileSystem(layerFile));
        }
    }
}