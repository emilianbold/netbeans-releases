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

package org.netbeans.modules.uml.project;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.project.ui.common.JavaSourceRootsUI;
import org.netbeans.modules.uml.project.ui.customizer.UMLProjectProperties;
import org.netbeans.modules.uml.project.ui.wizards.NewUMLProjectWizardIterator;
import org.openide.util.NbBundle;


/**
 * Create a fresh J2SEProject from scratch.
 * Currently does not permit much to be specified - feel free to 
 * add more parameters as needed.
 * @author Mike Frisino
 */
public class UMLProjectGenerator
{
	
	private UMLProjectGenerator()
	{}
	
	/**
	 * Create a new UML project then performs reverse engineering to populatet
	 * the model.
	 */
	public static AntProjectHelper createRoseImprtProject(File dir,
		String displayName,
		File roseFile,
        String modelingMode)
		throws IOException
	{
            AntProjectHelper helper = null; //JM: 03/31 this is just a temp fix to get the build going.
            
//		String mode = "PSK_IMPLEMENTATION"; // NOI18N
//		String strLanaguage = "java"; // NOI18N
//		
//		AntProjectHelper helper =
//			createEmptyProject(
//				dir, displayName,
//				modelingMode, 
//				null, new String[0], 
//				NewUMLProjectWizardIterator.TYPE_ROSE_IMPORT);
//		
//		if (roseFile != null)
//		{
//			
//			RoseImport roseImport =  new RoseImport();
//			roseImport.init(roseFile.getPath());
//			
//			INewDialogProjectDetails cpDetails = new NewDialogProjectDetails();
//			
//			cpDetails.setName(displayName);
//			cpDetails.setLocation(dir.getPath());
//			cpDetails.setAddToSourceControl(false);
//			cpDetails.setProjectKind(NewProjectKind.NPK_PROJECT);
//			cpDetails.setMode(mode);
//			cpDetails.setLanguage(strLanaguage);
//			
//			FileObject dirFO = FileUtil.toFileObject(dir);
//			Project project = ProjectManager.getDefault().findProject(dirFO);
//			
//			if (project != null)
//			{
//				UMLProjectHelper prjHelper = (UMLProjectHelper)project
//					.getLookup().lookup(UMLProjectHelper.class);
//				
//				if (prjHelper != null)
//				{
//					IProject umlProject = prjHelper.getProject();
//					
//					if (umlProject != null)
//					{
//						// TODO: Actually perform Rose Import Here.
//						cpDetails.setCreatedProject(umlProject);
//						roseImport.parseIntoProject(cpDetails);
//						ADProduct product=(ADProduct)prjHelper.getProduct();
//						product.initializeAddIns();
//						
//					}
//				}
//				
//				prjHelper.saveProject();
//			}
//		}
//		
//		return helper;
                return helper;
	}
	
    /**
     * Create a new UML project then performs reverse engineering to populate
     * the model
     */
    public static AntProjectHelper createRevEngProject(File dir,
        String displayName,
        Project javaSrcProject,
        JavaSourceRootsUI.JavaSourceRootsModel rootsModel,
        ArrayList<String> sourcesToRE,
        int umlProjectType)
        throws IOException
    {
        boolean partialRE = false;
        
        if (javaSrcProject == null) // || rootsModel == null)
        {
            return createEmptyProject(dir, displayName,
                UMLProject.PROJECT_MODE_IMPL_STR,
                javaSrcProject, new String[0],
                umlProjectType);
        }

        else if (sourcesToRE != null && rootsModel == null)
        {
            rootsModel = JavaSourceRootsUI.createModel(javaSrcProject);
            partialRE = true;
        }
        
        ArrayList<File> reSrcsList = new ArrayList<File>();
        ArrayList<String> reSrcsIdList = new ArrayList<String>();

        for (int i=0; i < rootsModel.getRowCount(); i++)
        {
            boolean includeSrcGrp = 
                partialRE || 
                ((Boolean)rootsModel.getValueAt(i, 
                    JavaSourceRootsUI.COL_INCLUDE_FLAG)).booleanValue();

            // if the source group was manually selected or we have a partial
            // RE situation, we add the source group; with partial RE, we want
            // all of the source groups added 
            // I know this seems logically backwards, but it is done for speed
            // and simplicity with the partial RE scenario
            if (includeSrcGrp)
            {
                SourceGroup grp = (SourceGroup)rootsModel.getSourceGroup(i);

                if (!partialRE)
                    reSrcsList.add(FileUtil.toFile(grp.getRootFolder()));
                
                reSrcsIdList.add(grp.getName());
            }
        }

        if (partialRE)
        {
            for (String source: sourcesToRE)
                reSrcsList.add(new File(source));
        }
        
        File[] reSrcs = (File[]) reSrcsList.toArray(new File[reSrcsList.size()]);
        String[] javaSrcRootIds = (String[])reSrcsIdList.toArray(
            new String[reSrcsIdList.size()]);

        AntProjectHelper retVal = createEmptyProject(dir, displayName,
            UMLProject.PROJECT_MODE_IMPL_STR, javaSrcProject, javaSrcRootIds,
            umlProjectType);

        FileObject dirFO = FileUtil.toFileObject(dir);
        Project p = ProjectManager.getDefault().findProject(dirFO);

        if (p instanceof UMLProject)
        {
            UMLProject umlProject = (UMLProject)p;
            umlProject.reverseEngineer(reSrcs);
            // MCF - i think we need to save reverse engineering
            // baseline model. Not sure of best place to invoke or what to
            // invoke. Subject to change.
            // We should move this somewhere higher up food chain because
            // right now i am calling in both this an the createEmptyUMLProject
            // below. So save will be called TWICE in rev eng case.
            // TODO move higher up chain after integrating new wiz iterator
            umlProject.saveProject();
        }

        return retVal;
    }
	
	
	
    /**
     * Create a new empty UML project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param codename the code name for the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createEmptyProject(
            File dir,
            String displayName,
            String modelingMode,
            Project javaSrcProj,
            String[] javaSrcRootIds,
            int umlProjectType)
            throws IOException
    {
//        dir.mkdirs();
//        
//        // XXX clumsy way to refresh, but otherwise it doesn't work for new folders
//        File rootF = dir;
//        while (rootF.getParentFile() != null)
//        {
//            rootF = rootF.getParentFile();
//        }
//        
//        FileObject fo = FileUtil.toFileObject(rootF);
//        assert fo != null : "At least disk roots must be mounted! " + rootF; // NOI18N
//        
//        fo.getFileSystem().refresh(false);
//        FileObject dirFO = FileUtil.toFileObject(dir);
        FileObject dirFO = FileUtil.createFolder(dir);
        
        assert dirFO != null: "No such dir on disk: " + dir; // NOI18N
        
        assert dirFO.isFolder() : "Not really a dir: " + dir; // NOI18N
        //assert dirFO.getChildren().length == 1 : "Dir must have been empty: " + dir;
        
        AntProjectHelper h = createProject(
                dirFO, displayName, modelingMode, javaSrcProj, javaSrcRootIds);
        
        // This is a HACK.  Basically I can not do this when the constructor
        // of the project because the project does not know it's name until the
        // above code.  So, I have to wait until the project knows its name
        // before I can initialize the project.
        Project p = ProjectManager.getDefault().findProject(dirFO);
        if (p instanceof UMLProject)
        {
            final UMLProjectHelper helper = (UMLProjectHelper)p.getLookup()
            .lookup(UMLProjectHelper.class);
            
            helper.initializeProject();
            ((UMLProject)p).saveProject();
            
            // bring up new diagram dialog when creating empty project
            //if "new project -> create new diagram" preference is set to yes
            if (umlProjectType==NewUMLProjectWizardIterator.TYPE_UML ||
                    umlProjectType==NewUMLProjectWizardIterator.TYPE_UML_JAVA)
            {
                //Kris Richards - options no longer available. Default to yes, so removed
                // condition also.
//                IPreferenceManager2 prefMgr = ProductHelper.getPreferenceManager();
//                String pref = prefMgr.getPreferenceValue(
//                        "Default|NewProject|QueryForNewDiagram"); // NOI18N

                    IDiagram newDiagram =
                            ProductHelper.getProductDiagramManager()
                            .newDiagramDialog(
                            (INamespace)helper.getProject(),
                            IDiagramKind.DK_UNKNOWN,
                            IDiagramKind.DK_ALL,
                            null);
                    
                    // Fixed issue 95782. When a diagram is 1st created, its dirty state is false.
                    // Set the dirty state to true to have the diagram autosaved.
                    if (newDiagram != null )
                    {
                          newDiagram.setIsDirty(true);
                          newDiagram.save();
                    }

            }
        }
        
        return h;
    }
	
	
    // This method creates the project artifacts , project.xml, 
    // project.proprties, private.properties etc.
    private static AntProjectHelper createProject(
        FileObject dirFO, 
        String name,
        String modelingMode, 
        Project javaSrcProj, 
        String[] javaSrcRootIds )
        // String srcRoot, String testRoot, String mainClass,
        // String manifestFile, boolean isLibrary)
        throws IOException

    {
        AntProjectHelper h = ProjectGenerator.createProject(
                dirFO, UMLProjectType.TYPE);

        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();

        Element nameEl = doc.createElementNS(
            UMLProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N

        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);

        Element minant = doc.createElementNS(
            UMLProjectType.PROJECT_CONFIGURATION_NAMESPACE,
            "minimum-ant-version"); // NOI18N

        minant.appendChild(doc.createTextNode("1.6")); // NOI18N
        data.appendChild(minant);

        // Manage the source roots stuff here

        // ep - the project.properties
        EditableProperties ep = 
            h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        h.putPrimaryConfigurationData(data, true);

        // Initialize and store the project props
        ep.setProperty(UMLProjectProperties.MODELING_MODE, modelingMode);
        ep.setProperty(UMLProjectProperties.UML_PROJECT_ANT_ARTIFACT, name);

// for now, just always default to Java code generation templates for
// types of UML projects
//        if (modelingMode.equals(NbBundle.getMessage(
//            org.netbeans.modules.uml.project.ui.common.CommonUiSupport.class,
//            "LBL_ProjectMode_Implementation"))) // NOI18N
//        {
            ep.setProperty(UMLProjectProperties.CODE_GEN_TEMPLATES, 
                UMLProjectProperties.DEFAULT_JAVA_TEMPLATES);
//        }

        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);

        // Initialize the references to Java project
        // It looks like we need to do the auxiliary config stuff
        // after we have putProperties, otherwise the putProperties call
        // overwrites everything.
        setJavaProjectReferences(h, javaSrcProj, javaSrcRootIds);

        return h;
    }
	
	
	// This method is refactored as a independent method so that it 
	//  can be reused from code generation user interface.
	public static void setJavaProjectReferences(
		AntProjectHelper h, Project javaSrcProj, String[] javaSrcRootIds)
	{
		
		String javaSrcProjRefVal = ""; // NOI18N
		EditableProperties ep = h.getProperties(
			AntProjectHelper.PROJECT_PROPERTIES_PATH);
		// epPriv - the private.properties
		EditableProperties epPriv = h.getProperties(
			AntProjectHelper.PRIVATE_PROPERTIES_PATH);
		
		if (javaSrcProj != null )
		{
			javaSrcProjRefVal = getJavaSrcProjRefVal(h, javaSrcProj);
		}
		
		ep.setProperty(
			UMLProjectProperties.REFERENCED_JAVA_PROJECT, javaSrcProjRefVal);
        
		ep.setProperty(
			UMLProjectProperties.REFERENCED_JAVA_PROJECT_SRC, javaSrcRootIds);
        
		h.putProperties(
			AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
		
		// Initialize and store the private props.
		if (javaSrcProjRefVal.length()>0)
		{
			epPriv.setProperty(javaSrcProjRefVal.substring(
					2, javaSrcProjRefVal.length()-1), 
				getJavaSrcProjectDirectory(javaSrcProj));
			// it's not a nice solution,
			// free form project uses this property to define source group folder name, it
			// it appears in uml project.properties "uml.javaproject.src=${project.dir}/src/java" 
			// which causes broken reference to project "dir" when loading uml project
			// add this to uml project private properties file to avoid broken reference
			epPriv.setProperty("project.dir", getJavaSrcProjectDirectory(javaSrcProj));
		}
		h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, epPriv);
		
		// It looks like we need to do the auxiliary config stuff
		// after we have putProperties, otherwise the putProperties call
		// overwrites everything. Or else, we have to just getProperties again
		
		// Should we set the uml java src property even in cases where
		// there is no initial project?
		// Note this is relying on assumption that the reference is set
		// by the ReferenceHelper
		ReferenceHelper referenceHelper = new ReferenceHelper(h,
			h.createAuxiliaryConfiguration(), h.getStandardPropertyEvaluator());
		
		if (javaSrcProj != null )
		{
			String[] refStrs = 
				addJavaSrcProjRef(h, referenceHelper, javaSrcProj);
            
			ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            
			ep.setProperty(UMLProjectProperties
                .REFERENCED_JAVA_PROJECT_ARTIFACTS, refStrs);
            
			h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
		}
	}
	
	public static String[] addJavaSrcProjRef(AntProjectHelper h,
		ReferenceHelper referenceHelper,
		Project javaSrcProj)
	{
		
		
		// If I use the ReferenceHelper, this should indirectly modify the ep and
		// epPriv. Is there any order faults that I can incurr depending on how
		// I structure this code? The ReferenceHelper is useful and it brings
		// some good benefits for free re: managing some of the broken refs
		// opening dependent projects etc.
		// But it does depend on AntArtifact as the key linkage.
		// This should be ok, and may help if we actually want to create
		// build scripts in uml which call java build scripts.
		// So i think it can't hurt and may help.
		
		// TODO - what is best practice to getting a handle to a ReferenceHelper
		// there is one already in the UMLProject but for some reason most
		// code I've seen avoids dereferencing the Project itself. So I am not
		// sure if I should be just creating a new Ref helper like i am below or
		// trying to get access to the one already in the UMLProject object
		
		
		AntArtifactProvider javaSrcArtiProv = (AntArtifactProvider)
		javaSrcProj.getLookup().lookup(AntArtifactProvider.class);
		ArrayList refStrs = new ArrayList();
		
		if (javaSrcArtiProv != null)
		{
			// there may be n uri's per ant artifact
			AntArtifact[] javaSrcAntArtis = javaSrcArtiProv.getBuildArtifacts();
            
			for (int i=0; i<javaSrcAntArtis.length; i++)
			{
				URI[] artiURIs =  javaSrcAntArtis[i].getArtifactLocations();
				
				for (int j=0; j<artiURIs.length; j++)
				{
					// AntArtifact artifact, URI location
					refStrs.add(
						referenceHelper.addReference( 
							javaSrcAntArtis[i], artiURIs[j]));
				}
				
			}
			
		}
		return (String[]) refStrs.toArray(new String[refStrs.size()]);
	}
	
	/**
	 * Project reference ID cannot contain dot character.
	 * File reference can.
	 */
	public static String getUsableReferenceID(String ID)
	{
		return PropertyUtils.getUsablePropertyName(ID).replace('.', '_');
	}
	
	/**
	 * Find reference ID (e.g. something you can then pass to RawReference
	 * as foreignProjectName) for the given property base name, prefix and path.
	 * @param property project name or jar filename
	 * @param prefix prefix used for reference, i.e. "project." for project
	 *    reference or "file.reference." for file reference
	 * @param path absolute filename the reference points to
	 * @return found reference ID or null
	 */
	public static String findReferenceID( AntProjectHelper h,
		String property, String prefix, String path)
	{
		Map m = h.getStandardPropertyEvaluator().getProperties();
		Iterator it = m.keySet().iterator();
        
		while (it.hasNext())
		{
			String key = (String)it.next();
            
			if (key.startsWith(prefix+property))
			{
				String v = h.resolvePath((String)m.get(key));
                
				if (path.equals(v))
				{
					return key.substring(prefix.length());
				}
			}
		}
        
		return null;
	}
	
	
	/**
	 * Generate unique reference ID for the given property base name, prefix
	 * and path. See also {@link #findReferenceID(String, String, String)}.
	 * @param property project name or jar filename
	 * @param prefix prefix used for reference, i.e. "project." for project
	 *    reference or "file.reference." for file reference
	 * @param path absolute filename the reference points to
	 * @return generated unique reference ID
	 */
	public static String generateUniqueID(AntProjectHelper h,
		String property, String prefix, String value)
	{
		PropertyEvaluator pev = h.getStandardPropertyEvaluator();
        
		if (pev.getProperty(prefix+property) == null)
		{
			return property;
		}
		
		int i = 1;
		while (pev.getProperty(prefix+property+"-"+i) != null) // NOI18N
		{
			i++;
		}
		
		return property+"-"+i; // NOI18N
	}
	
	public static String getJavaSrcProjRefVal(
        AntProjectHelper h, Project forProj)
	{
		
		// We want to generate something that looks like this
		// uml.javaproject=${project.A3}
		//  ${project.foreignProjectName}
		//ep.setProperty("dist.jar", "${dist.dir}/" +  // NOI18N
		// PropertyUtils.getUsablePropertyName(name) + ".jar"); // NOI18N
		
		File forProjDir = FileUtil.toFile(forProj.getProjectDirectory());
		assert forProjDir != null : forProj.getProjectDirectory();
		
		String projName = getUsableReferenceID(
			ProjectUtils.getInformation(forProj).getName());
		
		String forProjName = findReferenceID(h, projName, 
			"project.", forProjDir.getAbsolutePath()); // NOI18N
		
		if (forProjName == null)
		{
			forProjName = generateUniqueID(h, projName, 
				"project.", forProjDir.getAbsolutePath()); // NOI18N
		}
		
		return "${project."+forProjName+"}"; // NOI18N
	}
	
	private static String getJavaSrcProjectDirectory(Project forProj)
	{
		File forProjDir = FileUtil.toFile(forProj.getProjectDirectory());
		assert forProjDir != null : forProj.getProjectDirectory();
		
		return forProjDir.getAbsolutePath();
	}
	
	
	/* added to fix free-form project reference #6317890
	 * for j2se project, the reference is automatically resolved, the source
	 * project directory path is saved uml project private properties
	 */
	
	public static void fixJavaProjectReferences(
		AntProjectHelper h, Project javaSrcProj)
	{
		
		String javaSrcProjRefVal = ""; // NOI18N
		
		// epPriv - the private.properties
		EditableProperties epPriv = h.getProperties(
			AntProjectHelper.PRIVATE_PROPERTIES_PATH);
		
		if (javaSrcProj != null )
		{
			javaSrcProjRefVal = getJavaSrcProjRefVal(h, javaSrcProj);
		}
       
		// Initialize and store the private props.
		if (javaSrcProjRefVal.length()>0)
		{
			epPriv.setProperty(javaSrcProjRefVal.substring(
					2, javaSrcProjRefVal.length()-1), 
				getJavaSrcProjectDirectory(javaSrcProj));
		}
		h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, epPriv);
	}
        
        public static void createNewDiagram(INamespace namespace, int diagramKind, String diagramName)
              throws IOException
        {
           IDiagram newDiagram = ProductHelper.getProductDiagramManager().
                 createDiagram(diagramKind, namespace, diagramName, null);
           if (newDiagram != null)
           {
              newDiagram.setIsDirty(true);
              newDiagram.save();
           }
           
        }
}
