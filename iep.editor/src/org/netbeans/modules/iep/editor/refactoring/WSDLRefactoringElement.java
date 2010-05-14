package org.netbeans.modules.iep.editor.refactoring;

import org.netbeans.modules.iep.editor.PlanDataObject;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class WSDLRefactoringElement extends SimpleRefactoringElementImplementation {

    private FileObject source;
    Model model;
    Model targetModel;
    WSDLComponent comp;
    Node node;
    XMLRefactoringTransaction transaction;

    public WSDLRefactoringElement(Model model, Referenceable target, WSDLComponent comp) {
        
        this.model=model;
        this.comp=comp;
        this.targetModel = this.comp.getModel();
        try {
            if(targetModel instanceof WSDLModel) {
                ModelSource ms = targetModel.getModelSource();
                source = (FileObject) ms.getLookup().lookup(FileObject.class);
                    if(source != null) {
                        DataObject dObj = DataObject.find(source);
                        if(dObj != null && dObj instanceof PlanDataObject) {
//                            this.node = NodesFactory.getInstance().create(comp);
                            this.node = dObj.getNodeDelegate();
                        } 
                }
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    public String getDisplayText() {
        return "Rename " + node.getHtmlDisplayName();
    }
    
    public Lookup getLookup() {
        return Lookups.singleton(comp);
    }
    
    public FileObject getParentFile() {
        if (source == null) {
            assert this.targetModel != null : "Invalid object, expecting non-null model."; //NOI18N
            source = (FileObject)this.targetModel.getModelSource().getLookup().lookup(FileObject.class);
            assert source != null : "ModelSource should have FileObject in lookup"; //NOI18N
        }
        return source;
    }
    
    public PositionBounds getPosition() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public String getText() {
        return node.getName();
    }
    
    public void performChange() {
        // TODO Auto-generated method stub
        
    }
}
