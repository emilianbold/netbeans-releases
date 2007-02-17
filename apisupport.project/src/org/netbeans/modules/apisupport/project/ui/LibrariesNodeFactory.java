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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author mkleint
 */
public class LibrariesNodeFactory implements NodeFactory {
    
    /** Creates a new instance of LibrariesNodeFactory */
    public LibrariesNodeFactory() {
    }
    
    public NodeList createNodes(Project p) {
        NbModuleProject proj =  p.getLookup().lookup(NbModuleProject.class);
        assert proj != null;
        return new LibraryNL(proj);
    }
    
    private static class LibraryNL implements NodeList<String> {
        
        private NbModuleProject project;
        
        LibraryNL(NbModuleProject prj) {
            project = prj;
        }
    
        public List<String> keys() {
            List<String> toRet = new ArrayList<String>();
            toRet.add(LibrariesNode.LIBRARIES_NAME);
            if(resolveFileObjectFromProperty("test.unit.src.dir") != null) { //NOI18N
                toRet.add(UnitTestLibrariesNode.UNIT_TEST_LIBRARIES_NAME);
            }
            return toRet;
        }
        
        private FileObject resolveFileObjectFromProperty(String property){
            String filename = project.evaluator().getProperty(property);
            if (filename == null) {
                return null;
            }
            return project.getHelper().resolveFileObject(filename);
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }

        public Node node(String key) {
            if (key == LibrariesNode.LIBRARIES_NAME) {
                return  new LibrariesNode(project);
            } else if (key == UnitTestLibrariesNode.UNIT_TEST_LIBRARIES_NAME) {
                return new UnitTestLibrariesNode(project);
            }
            throw new AssertionError("Unknown key: " + key);
        }

        public void addNotify() {
            //TODO shall we somehow listen on project and ech for the 
            // test.unit.src.dir prop appearance/disappearance ??
        }

        public void removeNotify() {
        }
}

}
