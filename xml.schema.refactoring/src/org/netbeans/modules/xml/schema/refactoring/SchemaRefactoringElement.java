package org.netbeans.modules.xml.schema.refactoring;

/*
 * SchemaRefactoringElement.java
 *
 * Created on December 30, 2006, 4:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModelAccess;
import org.netbeans.modules.xml.xam.ui.actions.ShowSourceAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.actions.SystemAction;
import org.w3c.dom.Element;
/**
 *
 * @author Sonali
 */
public class SchemaRefactoringElement  extends SimpleRefactoringElementImplementation {
    
    private FileObject source;
    SchemaComponent comp;
    Node node;
    XMLRefactoringTransaction transaction;
    
    /**
     * Creates a new instance of SchemaRefactoringElement
     */
    public SchemaRefactoringElement(SchemaComponent comp) {
        this.comp=comp;
        CategorizedSchemaNodeFactory nodeFactory = new CategorizedSchemaNodeFactory(comp.getModel(), Lookups.singleton(comp));
        this.node = nodeFactory.createNode(comp);
           
    }

    public Lookup getLookup() {
       return Lookups.singleton(comp);
       
    }

    public FileObject getParentFile() {
       FileObject source = (FileObject)comp.getModel().getModelSource().getLookup().lookup(FileObject.class);
       assert source != null : "ModelSource should have FileObject in lookup"; //NOI18N
       return source;
    }

       
        
    public String getText() {
        return node.getName();
    }

    public String getDisplayText() {
        return node.getHtmlDisplayName();
    }

    public void performChange() {
    }

   public PositionBounds getPosition() {
       if(comp.getModel() instanceof AbstractDocumentModel ) {
           Document doc = ((AbstractDocumentModel) comp.getModel()).getBaseDocument();
           DocumentModelAccess docAcc = ((AbstractDocumentModel)comp.getModel()).getAccess();
           Element elem = comp.getPeer();
           String txt = docAcc.getXmlFragmentInclusive(elem);
           int startPos = comp.findPosition();
           int endPos = startPos + txt.length();
           DataObject dob = null;
           try {
                FileObject source = (FileObject)comp.getModel().getModelSource().getLookup().lookup(FileObject.class);
                dob = DataObject.find(source);
            } catch (DataObjectNotFoundException ex) {
             ex.printStackTrace();
           }
           CloneableEditorSupport ces = SharedUtils.findCloneableEditorSupport(dob);
           if(ces == null)
                return null;
        
           PositionRef ref1 = ces.createPositionRef(startPos, Bias.Forward);
           PositionRef ref2 = ces.createPositionRef(endPos, Bias.Forward);
           PositionBounds bounds = new PositionBounds(ref1, ref2);
       
           return bounds;
       }else {
           return null;
       }
           
       
    }
   
   public void openInEditor(){
         //System.out.println("SchemaRefactoringElement:: openInEditor called");
         Action preferredAction = SystemAction.get(ShowSourceAction.class);
         String command = (String)preferredAction.getValue(Action.ACTION_COMMAND_KEY);
	 ActionEvent ae = new ActionEvent(node, 0, command);
	 preferredAction.actionPerformed(ae);
     
     }
       
    void addTransactionObject(XMLRefactoringTransaction transaction) {
        this.transaction = transaction;
    }
    
    protected String getNewFileContent() {
         if(comp.getModel() instanceof AbstractDocumentModel && transaction != null) {
             try {
                 
                String refactoredString = transaction.refactorForPreview(comp.getModel());
                return refactoredString;
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
               return null;
           }
         }
         return null;
    }
     
  
}
