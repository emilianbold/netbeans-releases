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


package org.netbeans.test.uml.pref;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;


import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.testcases.UMLMultiTestCase;
import org.netbeans.test.umllib.testcases.UMLMultiTestSuite;
import org.netbeans.test.umllib.util.OptionsOperator;



/**
 *
 * @author psb
 * @spec UML/.xml
 */
public class AllItems extends UMLMultiTestSuite {
    
    private static String[] Test_Cases={"CheckUMLOptions"};
    
    private static boolean makeScreen=false;
    
    protected UMLMultiTestCase[] cases(){
        return new UMLMultiTestCase[]{new TestPreference(0,0)};
    }
    
    //some system properties
    private static String contextPropItemName="Properties";
    private static String umlPropertyWindowTitle="Project Properties";
    private static String umlSourcePackagesLabel="Source Packages";
    private static String umlSourcePackagesColumn="Folder Label";
    private static String umlSourceUsageColumn="Model?";
    private static String mainTreeTabName="Projects";
    //common test properties
    private static String prName= "UMLOptionsAll";
    private static String project = prName+"|Model";
    private static String sourceProject = "source";
    private static boolean codeSync=false;
    private static String defaultNewElementName=org.netbeans.test.uml.pref.utils.Utils.defaultNewElementName;
    private static String defaultReturnType=org.netbeans.test.uml.pref.utils.Utils.defaultReturnType;
    private static String defaultAttributeType=org.netbeans.test.uml.pref.utils.Utils.defaultAttributeType;
    private static String defaultAttributeVisibility=org.netbeans.test.uml.pref.utils.Utils.defaultAttributeVisibility;
    private static String defaultOperationVisibility=org.netbeans.test.uml.pref.utils.Utils.defaultOperationVisibility;
    private ProjectsTabOperator pto=null;
    private Node lastDiagramNode=null;
    private String lastTestCase=null;
    private JTreeOperator prTree=null;
    private static String workdir=System.getProperty("nbjunit.workdir");
    private static long elCount=0;
    //--
    private static String activityDiagramName1 = "acD1";
    private static String workPkg1 = "pkg1";
    private static String element1="Invocation";
    private static String elementName1="";
    private static ElementTypes elementType1=ElementTypes.INVOCATION;
    
     private static String[][] paths=
     {
         {"UML","Automatically Hide Modeling Window","Delete File when Deleting Artifacts","Show Aliases","Prompt to Save Project","Prompt to Save Diagram","Don't Show Filter Warning Dialog"},
         {"UML|Design Center"},
         {"UML|Design Center|Design Pattern Catalog","Overwrite Existing Participants"},
         {"UML|Diagrams","Ask Before Layout","Automatically Size Elements","Display Compartment Titles","Display Edit Control Tooltips","Display Empty Lists","Reconnect to Presentation Boundary","Resize with Show Aliases Mode","Show Stereotype Icons"},
         {"UML|Diagrams|Activity","Indicate Interruptible Edges"},
         {"UML|Diagrams|Collaboration","Delete Connector Messages","Show Message Numbers"},
         {"UML|Diagrams|Sequence","Automatically Create Classifiers","Classifier Type to Create","Delete Combined Fragment Messages","Group Operations by Classifier","New Message Action","Move Invoked Operation","Show Interaction Boundary as Created","Show Message Numbers"},
         {"UML|Display","Tagged Values"},
         {"UML|Expansion variables","Configuration Location"},
         {"UML|Find Dialog","Allow Lengthy Searches"},
         {"UML|Find Dialog|Displayed Columns","Alias","Fully Scoped Name","Icon","ID","Name","Project","Type"},
         {"UML|Information Logging","Log File Name and Location","Log Messages","Log Errors","Log Exceptions","Log Informational Messages","Log Method Entry","Log Method Exit"},
         {"UML|New Project","Create New Diagram","Default Diagram Name"},
         {"UML|New Project|Unknown Classifier","Create Classifier","Type to Create"},
         {"UML|New Project|Unknown Stereotype","Automatically Create"},
         {"UML|Notify When Namespace is Deleted","Actor Elements","Association Class Elements","Class Elements","Design Pattern Role Elements","Interface Elements","Package Elements","Use Case Elements"},
         {"UML|Presentation","Documentation Font","Grid Font","Set Global Colors and Fonts"},
         {"UML|Reverse Engineering","Show Process Output"},
         {"UML|Reverse Engineering|Operation Elements","Create New Operation","Prompt for Source Folders","Source Folders Config File"},
         {"UML|Code Engineering","Transform When Elements May Be Lost","Warn When Impacted Count Reaches"},
         {"UML|Code Engineering|Elements","Aggregation","Association","Association End","Attribute","Class","Generalization","Implementation","Interface","Multiplicity","Multiplicity Range","Navigable End","Operation","Package","Parameter","Project","Template Parameter","Enumeration","Enumeration Literal"},
         {"UML|Code Engineering|Java","Capitalize Attribute Name in Accessors","Create Accessor Methods","Create Constructor Methods","Create Finalize Methods","Display Duplicate Operation Dialog","Modify Redefined Operations","Name Navigable Ends","Prefix for Member Attributes","Prefix for Read Accessors","Prefix for Write Accessors","Remove Prefix from Accessor Names","Collection Override"},
         {"UML|UML Properties Pane","Default Filter","Display Type Fully Scoped Name","Maximum Display"}
     };
    
    /** Need to be defined because of JUnit */
    public AllItems(String name) {
        super(name);
    }
    public static NbTestSuite suite() {
        return new AllItems("CheckUMLOptions");
    }
    
    private int counter=0,counter_j=1;
    public class TestPreference extends UMLMultiTestCase {
        String path;
        
        int i=0,j=0;
        
        TestPreference(int i,int j) {
            this.i=i;this.j=j;
        }
        
        public void prepare() {
            makeScreen=true;
            OptionsOperator.invoke();
        }
        //
        public void setUp() {
            makeScreen=true;
            System.out.println("########  "+getName()+"  #######");
        }
        public UMLMultiTestCase create() {
            UMLMultiTestCase test = null;
            if(counter<paths.length) {
                test=new TestPreference(counter,counter_j);
                String name="testPref"+paths[counter][0];
                if(counter_j<paths[counter].length)name+="_"+paths[counter][counter_j];
                test.setName(name.replace('|','['));
                //
                counter_j++;
                if(counter_j>=paths[counter].length)
                {
                    counter_j=1;
                    counter++;
                }
            }
            return test;
        }
        //
        public void execute() {
            testPreference();
            makeScreen=false;
        }
        //
        
        public void tearDown() {
            if(makeScreen)org.netbeans.test.umllib.util.Utils.makeScreenShot("qa.uml.pref.AllItems$TestPreference",this.getName());
            log("tearDown: "+"qa.uml.pref.AllItems$TestPreference"+":"+this.getName());
             //popup protection
        }
        //
        public void cleanup() {
            org.netbeans.test.umllib.util.Utils.tearDown();
        }
        
        private void testPreference()
        {
            String path=paths[i][0];
            OptionsOperator op=new OptionsOperator();
            op=op.invokeAdvanced();
            //
            TreePath pth=op.treeTable().tree().findPath(path);
            assertFalse(87523,"Unuseful MDR Events logging is here",pth!=null && path.equals("UML|Information Logging|MDR Events"));
            assertFalse(84021,"Obsolete UML Properties Pane is here",pth!=null && path.equals("UML|UML Properties Pane"));
            assertTrue("Can't find "+path,pth!=null);
            //
            JPopupMenuOperator f;
            //
            if(!pth.equals(op.treeTable().tree().getSelectionPath()))
            {
                op.treeTable().tree().scrollToPath(pth);
                op.treeTable().tree().selectPath(pth);
                op.treeTable().tree().waitSelected(pth);
            }
            //
            new EventTool().waitNoEvent(1000);
            PropertySheetOperator ps=new PropertySheetOperator(op);
            //
            if(paths[i].length>1)
            {
                Property pr=null;
                try
                {
                        pr=new Property(ps,paths[i][j]);
                        assertFalse(87089,"Obsolete \"Delete File when Deleting Artifacts\" exists",pr!=null && "Delete File when Deleting Artifacts".equals(paths[i][j]));
                        assertFalse(79422,"\"Automatically Hide Modeling Window\""+" is present in UML Options","Automatically Hide Modeling Window".equals(paths[i][j]));
                        assertFalse(86055,"\"Grid Font\""+" is present in UML Options","Grid Font".equals(paths[i][j]));
                        assertFalse(86055,"\"Documentation Font\""+" is present in UML Options","Documentation Font".equals(paths[i][j]));
               }
                catch(JemmyException ex)
                {
                    if(paths[i][j].equalsIgnoreCase("Type To Create") && path.equals("UML|New Project|Unknown Classifier"))fail(78600, "Type to Create is missed from UML|New Project|Unknown classifier");
                    throw ex;
                }
                assertTrue("Bad value in "+path+"|"+paths[i][j],pr.getValue()!=null);
            }
            else
            {
                //should be no propertuies
                try
                {
                    Property pr=new Property(ps,0);
                    fail("There is some unexpected properties for "+path);
                }catch(Exception es)
                {
                    //all good
                }
            }
            //
            makeScreen=false;
        }
    }
    
    
}
