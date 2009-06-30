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


/*
 * JavaProject.java
 *
 * Created on January 25, 2006, 1:38 PM
 *
 */

package org.netbeans.test.umllib.project;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.JavaProjectRootNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.test.umllib.util.LabelsAndTitles;
import org.netbeans.test.umllib.util.Utils;

/**
 *
 * @author Alexandr Scherbatiy
 */

public class JavaProject extends Project {
    
    /** Creates a new instance of JavaProject */
    private static long TIME_WAIT = 1000;
    
    
    public static final String  BUILD_FAILED = "BUILD FAILED";
    public static final String  BUILD_SUCCESSFUL = "BUILD SUCCESSFUL";
    
    
    
    String mainClass;
    JavaProjectRootNode rootNode;
    
    
    JavaClassLoader classLoader;
    
    /**
     *
     * @param name
     * @param type
     */
    public JavaProject(String name) {
	this(name, ProjectType.JAVA_APPLICATION);
    }
    
    /**
     *
     * @param name
     * @param type
     */
    public JavaProject(String name, ProjectType type) {
	this(name, type, Utils.WORK_DIR);
    }
    
    
    /**
     *
     * @param name
     * @param type
     * @param location
     */
    public JavaProject(String name, ProjectType type, String location) {
	this(name, type, location, null);
    }
    
    /**
     *
     * @param name
     * @param type
     * @param location
     * @param mainClass
     */
    public JavaProject(String name, ProjectType type, String location, String mainClass) {
	super(name, type, location);
	this.mainClass = mainClass;
	rootNode = new JavaProjectRootNode( ProjectsTabOperator.invoke().tree(), name);
    }
    
    
    
    
    /**
     *
     * @return
     */
    public String  getMainClass(){
	return mainClass;
    }
    
    /**
     *
     * @return
     */
    public ProjectRootNode getProjectNode(){
	return rootNode;
    }
    
    
    /**
     *
     * @param name
     * @param type
     * @return
     */
    public static JavaProject createProject(String name, ProjectType type) {
	return createProject(name, type, Utils.WORK_DIR);
    }
    
    /**
     *
     * @param name
     * @param type
     * @param location
     * @return
     */
    public static JavaProject createProject(String name, ProjectType type, String location) {
	return createProject(name, type, location, true);
    }
    
    
    public static JavaProject createProject(String name, ProjectType type, boolean setAsMain, boolean createMainClass) {
	return createProject(name, type, Utils.WORK_DIR, setAsMain, createMainClass, null);
    }
    
    /**
     *
     * @param name
     * @param type
     * @param location
     * @param setAsMain
     * @return
     */
    public static JavaProject createProject(String name, ProjectType type, String location, boolean setAsMain) {
	return createProject(name, type, location, setAsMain, true, null);
    }
    
    /**
     *
     * @param name
     * @param type
     * @param location
     * @param setAsMain
     * @param createMainClass
     * @return
     */
    
    
    public static JavaProject createProject(String name, ProjectType type, String location, boolean setAsMain, boolean createMainClass) {
	return createProject(name, type, location, setAsMain, createMainClass, null);
    }
    
    /**
     *
     * @param name
     * @param type
     * @param location
     * @param setAsMain
     * @param createMainClass
     * @param mainClass
     * @return
     */
    public static JavaProject createProject(String name, ProjectType type, String location, boolean setAsMain, boolean createMainClass, String mainClass) {
	
	location = (location == null) ?  Utils.WORK_DIR : location;
	
	
	NewProjectWizardOperator newProject = NewProjectWizardOperator.invoke();
	//try{ Thread.sleep(TIME_WAIT); } catch(Exception e){}
	
	newProject.selectCategory(LabelsAndTitles.PROJECT_CATEGORY_GENERAL);
	newProject.selectProject(type.toString());
	
	newProject.next();
	
	//try{ Thread.sleep(TIME_WAIT); } catch(Exception e){}
	
	//newProject.setName(name);
	
	new JTextFieldOperator(newProject, 0).setText(name);
	
	//try{ Thread.sleep(TIME_WAIT); } catch(Exception e){}
	
	JTextFieldOperator projectLocation = new JTextFieldOperator(newProject, 1);
	
	projectLocation.setText(location);
	
	
	new JCheckBoxOperator(newProject,0).setSelected(setAsMain);
	new JCheckBoxOperator(newProject,1).setSelected(createMainClass);
	
	JTextFieldOperator mainClassTextField = new JTextFieldOperator(newProject, 3);
	
	if(mainClass != null){
	    mainClassTextField.setText(mainClass);
	}else{
	    mainClass = mainClassTextField.getText();
	}
	try{ Thread.sleep(TIME_WAIT); } catch(Exception e){}
	
	//this.location = location;
	//this.mainClass = mainClass;
	
	
	new JButtonOperator(newProject, "Finish").push();
	try{ Thread.sleep(TIME_WAIT); } catch(Exception e){}
	
	Utils.waitScanningClassPath();
	
	return new JavaProject(name, type, location, mainClass);
	
    }
    
    public UMLProject reverseEngineer(String umlProjectName){

        rootNode.performPopupActionNoBlock("Reverse Engineer...");
        JDialogOperator dialog = new JDialogOperator("Reverse Engineer");
        
        new JTextFieldOperator(dialog,2).setText(umlProjectName); 
        new JButtonOperator(dialog, "OK").pushNoBlock();
       
        return new  UMLProject(umlProjectName, ProjectType.UML_JAVA_REVERSE_ENGINEERING);

	//return UMLProject.createProject(umlProjectName, ProjectType.UML_JAVA_REVERSE_ENGINEERING, this);
	
    }
    
    
    public Node getSrcNode(){
        return new Node(getProjectNode(),"Source Packages");
    }
    
    public Class getJavaClass(String fullName){
	
	if( classLoader == null ){
	    classLoader = new JavaClassLoader(getLocation() + "/" + getName() + "/build/classes");
	}
	
	
	try {
	    return classLoader.loadClass(fullName);
	} catch (ClassNotFoundException ex) {
	    ex.printStackTrace();
	}
	
	return null;
	
    }
    
    
    
    
    
    
    public void build(){
	rootNode.buildProject();
    }
    
    public void close(){
        rootNode.performPopupActionNoBlock("Close");
    }
    
    public Output getOutput(){
	return new Output();
    }
    
    public class Output{
	
	OutputTabOperator output = new OutputTabOperator(getName() + " (jar) ");
	
	public boolean isCompiled(){
	    return getText().contains(BUILD_SUCCESSFUL);
	}
	
	public String getText(){
	    return output.getText();
	}
	
	public OutputTabOperator getOutputOperator(){
	    return output;
	}
	
    }
        
 
    
}
