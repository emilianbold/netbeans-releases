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

package org.netbeans.modules.project.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.actions.TestSupport.ChangeableLookup;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Lahoda
 */
public class ProjectChooserAccessoryTest extends NbTestCase {
    
    public ProjectChooserAccessoryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    /**The cycles in project dependencies should be handled gracefully:
     */
    public void testAddSubprojects() {
        ChangeableLookup l1 = new ChangeableLookup();
        ChangeableLookup l2 = new ChangeableLookup();
        Project p1 = new TestProject(l1);
        Project p2 = new TestProject(l2);
        
        Set<Project> subprojects1 = new HashSet<Project>();
        Set<Project> subprojects2 = new HashSet<Project>();
        
        subprojects1.add(p2);
        subprojects2.add(p1);
        
        l1.change(new SubprojectProviderImpl(subprojects1));
        l2.change(new SubprojectProviderImpl(subprojects2));
        
        List<Project> result = new ArrayList<Project>();
        
        ProjectChooserAccessory.addSubprojects(p1, result, new HashMap<Project,Set<? extends Project>>());
        
        assertTrue(new HashSet<Project>(Arrays.asList(p1, p2)).equals(new HashSet<Project>(result)));
    }
    
    private final class TestProject implements Project {
        
        private Lookup l;
        
        public TestProject(Lookup l) {
            this.l = l;
        }
        
        public FileObject getProjectDirectory() {
            throw new UnsupportedOperationException("Should not be called in this test.");
        }
        
        public Lookup getLookup() {
            return l;
        }
    }
    
    private static final class SubprojectProviderImpl implements SubprojectProvider {
        
        private Set<Project> subprojects;
        
        public SubprojectProviderImpl(Set<Project> subprojects) {
            this.subprojects = subprojects;
        }
        
        public Set<? extends Project> getSubprojects() {
            return subprojects;
        }

        public void addChangeListener(ChangeListener listener) {
        }

        public void removeChangeListener(ChangeListener listener) {
        }
        
    }
}
