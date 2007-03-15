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
/*
 * QueryUnusedGlobalsReader.java
 *
 * Created on April 11, 2006, 4:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.refactoring.query.readers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.xml.nbprefuse.AnalysisConstants;
import org.netbeans.modules.xml.refactoring.spi.UIHelper;
import org.netbeans.modules.xml.refactoring.ui.CancelSignal;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.visitor.Preview;
import org.netbeans.modules.xml.schema.refactoring.SchemaUIHelper;
import org.netbeans.modules.xml.schema.refactoring.ui.QueryUtilities;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jeri Lockhart
 */
public class QueryUnusedGlobalsReader {
    
    private enum FindUsagesResult {USAGES_FOUND,
    NO_USAGES_FOUND,
    CANCEL_REQUESTED};
    
    private static final int GCT = 2;
    private static final int GST = 5;
    private static final int GRP = 4;
    private static final int AT =  0;
    private static final int ATG = 1;
    private static final int ELE = 3;
    
    
    
    /**
     * Return the root node of a tree containing any unused global components
     *  by component type category
     *
     */
    public Node findUnusedGlobals(CancelSignal cancelSignal,
            SchemaModel model,
            Boolean excludeGEs) {
        if (cancelSignal == null || model == null || excludeGEs == null){
            return null;
        }
        
        AbstractNode root = new AbstractNode(new Children.Array());
        root.setDisplayName(NbBundle.getMessage(QueryUnusedGlobalsReader.class,
                "LBL_Unused_Global_Components"));
        root.setIconBaseWithExtension("org/netbeans/modules/xml/schema/refactoring/resources/unused-query.PNG");//NOI18N
        Schema schema = model.getSchema();
                
        Node[] catChildren = new Node[excludeGEs.booleanValue()?5:6];
        
        List<SchemaComponent> theGlobals = new ArrayList<SchemaComponent>();
        
        Collection<GlobalComplexType> cts = schema.getComplexTypes();
        Node ctCat = QueryUtilities.createCategoryNode(AnalysisConstants.GlobalTypes.COMPLEX_TYPE);
        catChildren[GCT] = ctCat;
        theGlobals.addAll(cts);
        Collection<GlobalElement> elems = null;
	int groupExcludedOffset = 1;
        if (!excludeGEs){
	    groupExcludedOffset--;
            elems = schema.getElements();
            Node eCat = QueryUtilities.createCategoryNode(AnalysisConstants.GlobalTypes.ELEMENT);
            catChildren[ELE] = eCat;
            theGlobals.addAll(elems);
        }
        
        Collection<GlobalSimpleType> sts = schema.getSimpleTypes();
        Node stCat = QueryUtilities.createCategoryNode(AnalysisConstants.GlobalTypes.SIMPLE_TYPE);
        catChildren[GST-groupExcludedOffset] = stCat;
        theGlobals.addAll(sts);
        
        Collection<GlobalGroup> ggs = schema.getGroups();
        Node gCat = QueryUtilities.createCategoryNode(AnalysisConstants.GlobalTypes.GROUP);
        catChildren[GRP-groupExcludedOffset] = gCat;
        theGlobals.addAll(ggs);
        
        Collection<GlobalAttribute> ats = schema.getAttributes();
        Node atCat = QueryUtilities.createCategoryNode(AnalysisConstants.GlobalTypes.ATTRIBUTE);
        catChildren[AT] = atCat;
        theGlobals.addAll(ats);
        
        Collection<GlobalAttributeGroup> ags = schema.getAttributeGroups();
        Node agCat = QueryUtilities.createCategoryNode(AnalysisConstants.GlobalTypes.ATTRIBUTE_GROUP);
        catChildren[ATG] = agCat;
        theGlobals.addAll(ags);
        
        
        List<FileObject> allFiles = new ArrayList<FileObject>();
        FileObject qFile = (FileObject) model.getModelSource().getLookup().lookup(FileObject.class);
//        System.out.println("QueryUnusedGlobalsReader file added: " + qFile.getNameExt());
        allFiles.add(qFile);
        
        //  then its siblings in the same folder, and the schemas in subfolders
        FileObject queryFolder = qFile.getParent();
        getFiles(allFiles, queryFolder, null, qFile);
        
        
        List<SourceGroup> srcGrps =
                QueryUtilities.getProjectSourceGroups(
                model,JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        //  lastly, the other files in the project
        for(SourceGroup srcGrp : srcGrps) {
            FileObject f = srcGrp.getRootFolder();
            if (f != queryFolder){
                getFiles(allFiles, f, queryFolder, qFile);
            }
        }
        findUnused(cancelSignal, theGlobals, allFiles);
        if (cancelSignal.isCancelRequested()){
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                    QueryUnusedGlobalsReader.class, "LBL_Query_Cancelled"));
            return null;
        }
        for (SchemaComponent sc:theGlobals){
            createNode(sc, catChildren, groupExcludedOffset);
        }
        
        
        root.getChildren().add(catChildren);
        StatusDisplayer.getDefault().setStatusText((theGlobals.size()==1?
            NbBundle.getMessage(QueryUnusedGlobalsReader.class,
                "LBL_Found_1_Unused_Component"):
            MessageFormat.format(
                NbBundle.getMessage(QueryUnusedGlobalsReader.class,
                "LBL_Found_Unused_Global_Components"),
                new Object[]{Integer.valueOf(theGlobals.size())})));
        
//        System.out.println("Global components count: " + theGlobals.size() );
//        System.out.println("Files searched count: " + allFiles.size() );
//        
//        System.out.println("Time to find unused components: " +
//                ((System.currentTimeMillis()-start)/1000f) + " seconds");//NOI18N
        
        return root;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    //  private methods
    ///////////////////////////////////////////////////////////////////////////
    
    private void findUnused(CancelSignal cancelSignal, List<SchemaComponent> theGlobals, List<FileObject> theFiles){
        Iterator<FileObject> filesIt = theFiles.iterator();
        while(filesIt.hasNext()){
            FileObject f = filesIt.next();
//            System.out.println("Scanning "+ f.getNameExt());
            ModelSource modelSource =
                    org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(
                    f,
                    true); // readOnly
            
            SchemaModel currModel = SchemaModelFactory.getDefault().getModel(modelSource);
            if (currModel.getState() == Model.State.NOT_WELL_FORMED) {
                continue;
            }
            assert currModel != null:"Cannot get SchemaModel for " + f.getNameExt();
            Iterator<SchemaComponent> scIt = theGlobals.iterator();
//            System.out.println("Globals remaining " + theGlobals.size());
            while(scIt.hasNext()){
                if (cancelSignal.isCancelRequested()){
                    return;
                }
                SchemaComponent sc = scIt.next();
                Referenceable ref = Referenceable.class.cast(sc);
//                             CR check if currModel is valid
                Preview preview =
                        QueryUtilities.getUsagesPreview(
                        currModel.getSchema(),
                        NamedReferenceable.class.cast(ref));

                assert preview != null:
                    "QueryUtilities.getUsagesPreview() returned null preview";//NOI18N
                Map<SchemaComponent, List<SchemaComponent>> um =
                        preview.getUsages();
                assert um != null:
                    "QueryUtilities.getUsagesPreview() returned preview with null map"; //NOI18N

                if (um.size() > 0){
//                    System.out.println("Removed " + Named.class.cast(sc).getName());
                    scIt.remove();
                }
//                else {
//                    System.out.println("Kept " + Named.class.cast(sc).getName());
//                }
            }
        }
    }
    
    
    /**
     * Recursive methods to get all subfolders and files
     *
     */
    private void getFiles(List<FileObject> allFiles,
            FileObject fobj,
            FileObject queryFolder,
            FileObject queryFile){
        
        if (fobj != null && fobj.isFolder() && fobj != queryFolder){
            FileObject[] children = fobj.getChildren();
            for (FileObject f:children){
                if (f.isData() &&
                        f.getExt().equals(AnalysisConstants.SCHEMA_FILE_EXTENSION) &&
                        f != queryFile) { // it's a schema dataobject
//                    System.out.println("QueryUnusedGlobalsReader file added: " + f.getNameExt());
                    allFiles.add(f);
                } else {    // it's a folder
                    getFiles(allFiles, f, queryFolder,queryFile);
                }
            }
        }
        
    }
    
    
    /**
     *  Creates an AbstractNode for the schema component
     *   and adds it to the children of the categoryNode
     *   If the categoryNode is null, creates it and adds it to
     *   the children of the root node
     *
     */
    private void createNode(SchemaComponent sc, Node[] categories, int groupExcludedOffset){
        if (!(sc instanceof Referenceable) || categories == null){
            return;
        }
        int index = 0;
        if (sc instanceof GlobalComplexType){
            index = GCT;
        } else if (sc instanceof GlobalSimpleType){
            index = GST - groupExcludedOffset;
        } else if (sc instanceof GlobalGroup){
            index = GRP - groupExcludedOffset;
        } else if (sc instanceof GlobalAttribute){
            index = AT;
        } else if (sc instanceof GlobalAttributeGroup){
            index = ATG;
        } else if (sc instanceof GlobalElement){
            index = ELE;
        }
        
        System.out.println("QueryUnusedGlobalsReader:: create node called");
        SchemaUIHelper uiHelper = new SchemaUIHelper();
        Node displayNode = uiHelper.getDisplayNode(sc);
  //      Node displayNode = RefactoringManager.getInstance().
        //        getTargetComponentUIHelper((Referenceable)sc).getDisplayNode(sc);
        displayNode = new FilterNode(displayNode) {
            public String getHtmlDisplayName() {
                return null;
            }
        };
        categories[index].getChildren().add(new Node[]{displayNode});
    }
}
