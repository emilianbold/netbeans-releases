/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.project.TestUtil;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class FileCommandActionTest extends NbTestCase {
    
    public FileCommandActionTest(String name) {
        super( name );
    }

    private FileObject p1;
    private FileObject p2;
    private FileObject f1_1; 
    private FileObject f1_2; 
    private FileObject f2_1;
    private FileObject f2_2;
    private DataObject d1_1; 
    private DataObject d1_2;
    private DataObject d2_1;
    private DataObject d2_2;
    private TestSupport.TestProject project1;
    private TestSupport.TestProject project2;

    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setLookup(new Object[] {
            TestSupport.testProjectFactory(),
        });
        clearWorkDir();
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
    
        
        p1 = TestSupport.createTestProject( workDir, "project1" );
        f1_1 = p1.createData("f1_1.java");
        f1_2 = p1.createData("f1_2.krava");
        d1_1 = DataObject.find(f1_1);
        d1_2 = DataObject.find(f1_2);
               
        project1 = (TestSupport.TestProject)ProjectManager.getDefault().findProject( p1 );
        project1.setLookup( Lookups.fixed( new Object[] { new TestActionProvider() } ) );  
        
        p2 = TestSupport.createTestProject( workDir, "project2" );
        f2_1 = p2.createData("f2_1.java");
        f2_2 = p2.createData("f2_2.krava");
        d2_1 = DataObject.find(f2_1);
        d2_2 = DataObject.find(f2_2);
               
        project2 = (TestSupport.TestProject)ProjectManager.getDefault().findProject( p2 );
        
    }
    
    protected void tearDown() throws Exception {
        clearWorkDir();
        super.tearDown();
    }
    
    public void testCommandEnablement() {
        TestSupport.ChangeableLookup lookup = new TestSupport.ChangeableLookup( new Object[]{} );
        FileCommandAction action = new FileCommandAction( "COMMAND", "TestFileCommandAction", (Icon)null, lookup );
        
        assertFalse( "Action should NOT be enabled", action.isEnabled() );        
        
        lookup.change( new Object[] { d1_1 } );
        assertTrue( "Action should be enabled", action.isEnabled() );        
        
        lookup.change( new Object[] { d1_1, d1_2 } );       
        assertFalse( "Action should NOT be enabled", action.isEnabled() );        
        
        lookup.change( new Object[] { d1_1, d2_1 } );       
        assertFalse( "Action should NOT be enabled", action.isEnabled() );        
        
    }
        
    private static class TestActionProvider implements ActionProvider {
        
        public String COMMAND = "COMMAND";
        
        private String[] ACTIONS = new String[] { COMMAND };
        
        private List invocations = new ArrayList();
        
        public String[] getSupportedActions() {
            return ACTIONS;
        }
                
        public void invokeAction( String command, Lookup context ) throws IllegalArgumentException {
            
            if ( COMMAND.equals( command ) ) {
                invocations.add( command );
            }            
            else {
                throw new IllegalArgumentException();
            }
            
        }

        public boolean isActionEnabled( String command, Lookup context) throws IllegalArgumentException {
            
            if ( COMMAND.equals( command ) ) {
                Collection dobjs = context.lookup( new Lookup.Template( DataObject.class ) ).allInstances();
                for ( Iterator it = dobjs.iterator(); it.hasNext();  ) {
                    DataObject dobj = (DataObject)it.next();
                    if ( !dobj.getPrimaryFile().getNameExt().endsWith( ".java" ) ) {
                        return false;
                    }                    
                }
                return true;
            }            
            else {
                throw new IllegalArgumentException();
            }
            
        }

        
    }
    
    private static class TestActionPerformer implements ProjectActionPerformer {
        
        private int enableCount;
        private Project project;
        private boolean on;
        
        public boolean enable( Project project ) {
            enableCount ++;
            this.project = project;
            return on;
        }

        public void perform( Project project ) {
            this.project = project;
        }
        
        public void on( boolean on ) {
            this.on = on;
        }
        
        public void clear() {
            this.project = null;
            enableCount = 0;
        }
        
        
    }
    
}
