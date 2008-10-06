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

package org.netbeans.modules.uml.project.ui.customizer;
import org.netbeans.modules.uml.project.ProjectUtil;
import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IImportEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.profiles.IProfile;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementChangeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mike Frisino
 */
public class UMLImportsUiSupport
{
    
    //TODO - fix the table values so that they are user friendly instead of
    // current object.toString
    
    private UMLImportsSupport importSupport;
    private ImportListener impListener;
    public static final int COL_UML_PROJECT_NAME = 0;
    public static final int COL_UML_PROJECT_FOLDER = 1;
    
    private UMLProject mProject = null;
    private ArrayList < ImportElementListener > mImportListeners = new ArrayList <ImportElementListener> ();
    private HashMap importedElements = null;
    
    public UMLImportsUiSupport(UMLProject project)
    {
        mProject = project;
        
////        RequestProcessor.getDefault().post(new Runnable()
////        {
////            public void run()
//            {
//                System.err.println("UMLImportsUiSupport:Loading");
//                ImportListener impListener = new ImportListener();
//                IElementChangeEventDispatcher disp = null;
//                IElementLifeTimeEventDispatcher lifeTimeDisp = null;
//                EventDispatchRetriever ret = EventDispatchRetriever.instance();
//                disp = (IElementChangeEventDispatcher)ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
//                lifeTimeDisp = (IElementLifeTimeEventDispatcher)ret.getDispatcher(EventDispatchNameKeeper.lifeTime());
//                disp.registerForImportEventsSink(impListener);
//                lifeTimeDisp.registerForLifeTimeEvents(impListener);
//            }
////        }); 
////        DispatchHelper helper = new DispatchHelper();
////        helper.registerForImportEventsSink(new ImportListener());
    }
            
    public void initializeProject()
    {
//        System.err.println("UMLImportsUiSupport:Loading");
        impListener = new ImportListener();
        IElementChangeEventDispatcher disp = null;
        IElementLifeTimeEventDispatcher lifeTimeDisp = null;
        EventDispatchRetriever ret = EventDispatchRetriever.instance();
        disp = (IElementChangeEventDispatcher)ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
        lifeTimeDisp = (IElementLifeTimeEventDispatcher)ret.getDispatcher(EventDispatchNameKeeper.lifeTime());
        disp.registerForImportEventsSink(impListener);
        lifeTimeDisp.registerForLifeTimeEvents(impListener);
    }

    public void unInitializeProject()
    {
        IElementChangeEventDispatcher disp = null;
        IElementLifeTimeEventDispatcher lifeTimeDisp = null;
        EventDispatchRetriever ret = EventDispatchRetriever.instance();
        disp = (IElementChangeEventDispatcher)ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
        lifeTimeDisp = (IElementLifeTimeEventDispatcher)ret.getDispatcher(EventDispatchNameKeeper.lifeTime());
        disp.revokeImportEventsSink(impListener);
        lifeTimeDisp.revokeLifeTimeSink(impListener);
    }
    
    public void addImportElementListener(ImportElementListener listener)
    {
        mImportListeners.add(listener);
    }
    
    public void removeImportElementListener(ImportElementListener listener)
    {
        mImportListeners.remove(listener);
    }
    
    public void fireElementImported(IProject project, 
                                    IElement element,
                                    IElementImport importElement)
    {
        for(ImportElementListener listener : mImportListeners)
        {
            listener.elementImported(project, element, importElement);
        }
    }
    
    public void firePackageImported(IProject project, 
                                    IElement element,
                                    IPackageImport importElement)
    {
        for(ImportElementListener listener : mImportListeners)
        {
            listener.packageImported(project, element, importElement);
        }
    }
    
    public void fireElementDeleted(IProject project, IElement element)
    {
        for(ImportElementListener listener : mImportListeners)
        {
            listener.elementDeleted(project, element);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Methods for working with list models ------------------------------------
    
    public static DefaultTableModel createTableModel( Iterator it )
    {
        
        DefaultTableModel model = new UMLImportsModel(new Object[0][0]);
        
        
        while( it.hasNext() )
        {
            Object curItem = it.next();
            
            Object[] data = new Object[2];
            data[COL_UML_PROJECT_NAME] = curItem;
            if(curItem instanceof UMLImportsSupport.Item)
            {
                UMLImportsSupport.Item item = (UMLImportsSupport.Item)curItem;                
                data[COL_UML_PROJECT_FOLDER] = item.getDirectoryLocation();
            }
            else
            {
                data[COL_UML_PROJECT_FOLDER] = "";
            }
            
            model.addRow(data);
        }
        
        return model;
    }
    
    
    public static class UMLImportsModel extends DefaultTableModel
    {
        
        
        static String colProjLabel = (NbBundle.getMessage(UMLImportsUiSupport.class,
                "LBL_ImportedProjectsColProject")); //NOI18N
        static String colProjFolder = (NbBundle.getMessage(UMLImportsUiSupport.class,
                "LBL_ImportedProjectsColFolder")); //NOI18N
        
        public UMLImportsModel(Object[][] data)
        {
            super(data, new Object[]{colProjLabel,colProjFolder});
        }
        
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }
        
        public Class getColumnClass(int columnIndex)
        {
            switch (columnIndex)
            {
                case COL_UML_PROJECT_NAME:
                    return Object.class;
                case COL_UML_PROJECT_FOLDER:
                    return String.class;
                default:
                    return super.getColumnClass(columnIndex);
            }
        }
    }
    
    
    
    public static Iterator getIterator( DefaultTableModel model )
    {
        // XXX Better performing impl. would be nice
        return getList( model ).iterator();
    }
    
    public static List getList( DefaultTableModel model )
    {
        
        List list = new ArrayList();
        for(int i=0; i< model.getRowCount(); i++)
        {
            list.add(model.getValueAt(i,COL_UML_PROJECT_NAME));
        }
        return list;
    }
    
    
    
    public static int[] addArtifacts( DefaultTableModel tableModel, AntArtifactChooser.ArtifactItem artifactItems[] )
    {
        
        // int lastIndex = indices == null || indices.length == 0 ? -1 : indices[indices.length - 1];
        int[] indexes = new int[artifactItems.length];
        for( int i = 0; i < artifactItems.length; i++ )
        {
            
            //   int current = lastIndex + 1 + i;
            UMLImportsSupport.Item item = UMLImportsSupport.Item.create(
                    artifactItems[i].getArtifact(), artifactItems[i].getArtifactURI(), null ) ;
            boolean alreadyListed = false;
            for(int j=0; j<tableModel.getRowCount();j ++)
            {
                
                if (tableModel.getValueAt(j, COL_UML_PROJECT_NAME) == item )
                {
                    alreadyListed = true;
                    break;
                }
            }
            if(! alreadyListed)
                tableModel.addRow(new Object[] {item, "TODO"} );
                
        }
        return indexes;
    }
    
    protected boolean inSameProject(IElement element)
    {
        boolean retVal = false;
        
        if((element != null) && (mProject != null))
        {
            UMLProjectHelper helper = (UMLProjectHelper)mProject.getLookup().lookup(UMLProjectHelper.class);
            if(helper != null)
            {
                IProject project = helper.getProject();
                retVal = element.inSameProject(project);
            }
        }
        
        return retVal;
    }
    
    protected void modelElementImported(IPackage importingPackage, 
                                        IElement element,
                                        IElementImport importedElement)
    {
//        if((element != null) && (inSameProject(importingPackage) == true))
//        {
            IProject p = ProjectUtil.getOwningProjectOfImportedElement(element);
            Project foundProject = ProjectUtil.findElementOwner(p);
            Project referencingProject = ProjectUtil.findReferencingProject(importedElement);
            
            // add reference to the project only if it differes from the owner
            if((foundProject instanceof UMLProject) && 
               (referencingProject  instanceof UMLProject) && 
               (foundProject != referencingProject))
            {
                UMLProject umlProject = (UMLProject)foundProject;   
                UMLProject umlRefProject = (UMLProject)referencingProject;
                fireElementImported(umlProject.getLookup().lookup(UMLProjectHelper.class).getProject(), element, importedElement);

                // Check if a reference to the refrence project already
                // exist.  If a reference already exist do not create a new
                // reference.
                if(doesReferenceExist(umlRefProject, umlProject) == false)
                {   
                    boolean foundOne = doesReferenceExist(umlProject, umlRefProject);    
                    if(foundOne == false)
                    {                    
                        AntArtifact[] artifacts = AntArtifactQuery.findArtifactsByType( umlProject, UMLProject.ARTIFACT_TYPE_UML_PROJ );
                        AntArtifactChooser.ArtifactItem[] items = new AntArtifactChooser.ArtifactItem[artifacts.length];
                        for( int i = 0; i < artifacts.length; i++ )
                        {
                            URI uris[] = artifacts[i].getArtifactLocations();
                            for( int y = 0; y < uris.length; y++ )
                            {
                                items[i] = new AntArtifactChooser.ArtifactItem(artifacts[i], uris[y]);
                            }
                        }

                        UMLProjectProperties properties = umlRefProject.getUMLProjectProperties();
                        UMLImportsUiSupport.addArtifacts(properties.umlProjectImportsModel, items);
                        properties.save();
                    }
                }
            }
//        }
    }
    
    protected void modelPackageImported(IPackage importingPackage, 
                                        IElement element,
                                        IPackageImport importedElement)
    {
        if((element != null) && (inSameProject(importingPackage) == true))
        {
            Project foundProject = ProjectUtil.findElementOwner(element);
            Project referencingProject = ProjectUtil.findReferencingProject(element);
            
            // add reference to the project only if it differes from the owner
            if((foundProject instanceof UMLProject) && 
               (referencingProject  instanceof UMLProject) && 
               (foundProject != referencingProject))
            {
                UMLProject umlProject = (UMLProject)foundProject;   
                UMLProject umlRefProject = (UMLProject)referencingProject;
                firePackageImported(umlProject.getLookup().lookup(UMLProjectHelper.class).getProject(), element, importedElement);

                // Check if a reference to the refrence project already
                // exist.  If a reference already exist do not create a new
                // reference.
                if(doesReferenceExist(umlRefProject, umlProject) == false)
                {   
                    boolean foundOne = doesReferenceExist(umlProject, umlRefProject);    
                    if(foundOne == false)
                    {                    
                        AntArtifact[] artifacts = AntArtifactQuery.findArtifactsByType( umlProject, UMLProject.ARTIFACT_TYPE_UML_PROJ );
                        AntArtifactChooser.ArtifactItem[] items = new AntArtifactChooser.ArtifactItem[artifacts.length];
                        for( int i = 0; i < artifacts.length; i++ )
                        {
                            URI uris[] = artifacts[i].getArtifactLocations();
                            for( int y = 0; y < uris.length; y++ )
                            {
                                items[i] = new AntArtifactChooser.ArtifactItem(artifacts[i], uris[y]);
                            }
                        }

                        UMLProjectProperties properties = umlRefProject.getUMLProjectProperties();
                        UMLImportsUiSupport.addArtifacts(properties.umlProjectImportsModel, items);
                        properties.save();
                    }
                }
            }
        }
    }
    
    /**
     * Check if the project that will be referenced already has a dependency on 
     * the project that will be referencing the referenced project.
     */
    protected boolean doesReferenceExist(UMLProject referencedProject,
                                         UMLProject refereningProject)
    {
        boolean retVal = false;
        
        UMLProjectProperties properties = referencedProject.getUMLProjectProperties();
        
        // First check if the project as already been added to the table.        
        DefaultTableModel tableModel = properties.umlProjectImportsModel;
        for(int i = 0; i < tableModel.getRowCount(); i++)
        {
            UMLImportsSupport.Item curItem = (UMLImportsSupport.Item)tableModel.getValueAt(i, COL_UML_PROJECT_NAME);
            AntArtifact artifact = curItem.getArtifact();
            if(artifact != null)
            {
                Project curProject = artifact.getProject();
                if(curProject.getProjectDirectory().equals(refereningProject.getProjectDirectory()))
                {
                    retVal = true;
                    break;
                }
            }
        }
        
        return retVal;
    }
    
    protected void modelElementDeleted(IElement element) {
        if (element == null)
            return;
        
//        Project foundProject = ProjectUtil.findElementOwner(element);

//        if (foundProject instanceof UMLProject) {
//            UMLProject umlProject = (UMLProject)foundProject;   
            fireElementDeleted(element.getProject(), element);
//        }
    }
    
    protected synchronized Map getImportedElements() {
        if (importedElements == null) {
            importedElements = new HashMap();
            UMLProjectHelper helper = (UMLProjectHelper)mProject.getLookup().lookup(UMLProjectHelper.class);
            IProject project = helper.getProject();
            ETList < INamespace > packages = project.getImportedPackages();
            if(packages != null) {
                for(INamespace ns : packages) {
                    importedElements.put(new ElementWrapper(ns), ns);
                }
            }

            ETList < IElement > elements = project.getImportedElements();
            if(elements != null) {
                for(IElement elem : elements) {
                    importedElements.put(new ElementWrapper(elem), elem);
                }
            }
        }
        return importedElements;
    }
    
    public class ImportListener implements IImportEventsSink, IElementLifeTimeEventsSink
    {
        // IImportEventsSink interface implementation ...........................
        
        public void onElementImported(IElementImport elImport, IResultCell cell)
        {
            if (inSameProject(elImport) == true)
            {
                modelElementImported(elImport.getImportingPackage(),
                        elImport.getImportedElement(), elImport);
            }
        }

        public void onPackageImported(IPackageImport packImport, IResultCell cell)
        {
            if(!(packImport instanceof IProfile))
            {
                modelPackageImported(packImport.getImportingPackage(),
                                     packImport.getImportedPackage(),
                                     packImport);
            }
        }

        public void onPreElementImport(IPackage importingPackage, 
                                       IElement element, 
                                       INamespace owner,
                                       IResultCell cell)
        {        
            if (importingPackage == null || owner == null)
                return;
            Project referencedProject = ProjectUtil.findNetBeansProjectForModel(owner.getProject());
            Project referencingProject = ProjectUtil.findNetBeansProjectForModel(importingPackage.getProject());
            
            if(referencedProject.equals(referencingProject) == false)
            {
                if(mProject.equals(referencingProject) == true)
                {
                    if ( ProjectUtils.hasSubprojectCycles( mProject, referencedProject ) )
                    {
                        DialogDisplayer.getDefault().notify( new NotifyDescriptor.Message(
                                NbBundle.getMessage( AntArtifactChooser.class, "MSG_AACH_Cycles" ),
                                NotifyDescriptor.INFORMATION_MESSAGE ) );
                        cell.setContinue(false);
                    }
                }
            }
            
        }

        public void onPrePackageImport(IPackage importingPackage, 
                                       IPackage importedPackage,
                                       INamespace owner,
                                       IResultCell cell)
        {
            if (owner == null)
                return;
            Project referencedProject = ProjectUtil.findNetBeansProjectForModel(owner.getProject());
            Project referencingProject = ProjectUtil.findNetBeansProjectForModel(importingPackage.getProject());
            
            if(referencedProject.equals(referencingProject) == false)
            {
                if(mProject.equals(referencingProject) == true)
                {
                    if ( ProjectUtils.hasSubprojectCycles( mProject, referencedProject ) )
                    {
                        DialogDisplayer.getDefault().notify( new NotifyDescriptor.Message(
                                NbBundle.getMessage( AntArtifactChooser.class, "MSG_AACH_Cycles" ),
                                NotifyDescriptor.INFORMATION_MESSAGE ) );
                        cell.setContinue(false);
                    }
                }
            }
        }
        
        // IElementLifeTimeEventsSink interface implementation ..................

        public void onElementDeleted(IVersionableElement element, IResultCell cell) {
            if (element instanceof IElement)
                modelElementDeleted((IElement)element);
        }
        
        public void onElementDuplicated(IVersionableElement element, IResultCell cell) {
        }
        
        public void onElementPreCreate(String ElementType, IResultCell cell) {
        }
        
        public void onElementPreDuplicated(IVersionableElement element, IResultCell cell) {
        }
        
        public void onElementCreated(IVersionableElement element, IResultCell cell) {
        }
        
        public void onElementPreDelete(IVersionableElement element, IResultCell cell) {
        }
        
    }
    
    // ..........................................................................
    private static class ElementWrapper {
        
        IElement element;
        
        ElementWrapper(IElement element) {
            this.element = element;
        }
        
        public boolean equals(Object obj) {
            if (obj instanceof ElementWrapper) {
                return element.isSame(((ElementWrapper)obj).element);
            }
            return false;
        }
        
        public int hashCode() {
            return element.getXMIID().hashCode();
        }
        
    }
    
}
