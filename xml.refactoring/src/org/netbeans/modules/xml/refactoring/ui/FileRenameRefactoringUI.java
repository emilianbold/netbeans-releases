/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.xml.refactoring.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.ui.RefactoringCustomUI;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.netbeans.modules.xml.nbprefuse.AnalysisViewer;
import org.netbeans.modules.xml.nbprefuse.View;
import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.spi.RefactoringUtil;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.xml.refactoring.ui.GraphHelper;
import org.netbeans.modules.xml.refactoring.ui.views.WhereUsedView;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import prefuse.data.Graph;

/**
 *
 * @author Nam Nguyen
 */
public class FileRenameRefactoringUI implements org.netbeans.modules.refactoring.spi.ui.RefactoringUI, RefactoringCustomUI {
    
    private WhereUsedQuery query;
    private WhereUsedView view;
    private RenamePanel panel;
    String newName;
    private Model target;
    String oldFileName, displayName;

    private RenameRefactoring refactoring;
    
      
    public FileRenameRefactoringUI(Model target, String newName){
        this.target = target;
        refactoring = new RenameRefactoring(Lookups.singleton(target));
        oldFileName =( target.getModelSource().getLookup().lookup(FileObject.class)).getName();
        if(newName != null)
            displayName = newName;
        else 
            displayName = oldFileName;
        XMLRefactoringTransaction transaction = new XMLRefactoringTransaction((Referenceable)target, refactoring);
        refactoring.getContext().add(transaction);
        //TEMP solution :: ask jbecika if renameRefactoring can have a getOldName()
        //have filed issue#98842 on Refactoring API..till then use context obj
        refactoring.getContext().add(oldFileName);
        
    }

     public FileRenameRefactoringUI(Model target){
         this.target = target;
         refactoring = new RenameRefactoring(Lookups.singleton(target));
         oldFileName =( target.getModelSource().getLookup().lookup(FileObject.class)).getName();
         displayName = oldFileName;
         XMLRefactoringTransaction transaction = new XMLRefactoringTransaction((Referenceable)target, refactoring);
         refactoring.getContext().add(transaction);
         //TEMP solution :: ask jbecika if renameRefactoring can have a getOldName()
         //have filed issue#98842 on Refactoring API..till then use context obj
         refactoring.getContext().add(oldFileName); 
        
    }
    
    ////////////////////////////////////////////////////////////////////////////
    /**  Start Implementation of RefactoringUI
     */
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Returns refactoring-specific panel containing input fields for 
     * refactoring parameters. This method is called by ParametersPanel
     * which is responsible for displaying refactoring parameters dialog.
     * Name of the panel returned from this method will be used as the dialog
     * name. This panel can use setPreviewEnabled method of the passed
     * ParametersPanel to enable/disable Preview button of the refactoring
     * parameters dialog.
     * 
     * @param parent ParametersPanel that the returned panel will be displayed in.
     * @return Refactoring-specific parameters panel.
     */
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
         if (panel == null) {
            panel = new RenamePanel(displayName, 
                    parent, NbBundle.getMessage(RenamePanel.class, "LBL_FileRename"), 
                    true, 
                    false);
        }
        return panel;
    }

    /**
     * Implementation of this method should set the refactoring parameters entered
     * by user into the refactoring-specific parameters panel (returned from getPanel
     * method) into the underlying refactoring object.
     * 
     * @return Chain of problems returned from the underlying refactoring object
     * when trying to set its parameters.
     */
    public Problem setParameters() {
        newName = panel.getNameValue();
        if (refactoring instanceof RenameRefactoring) {
            ((RenameRefactoring) refactoring).setNewName(newName);
        }
        return refactoring.checkParameters();
       
    }

    /**
     * Indicates whether this class represents a real refactoring that changes
     * code or whether it is just a query (e.g. all usages for a class).
     * 
     * @return <code>true</code> if the class represents only a query,
     * <code>false</code> if the class represents a real refactoring.
     */
    public boolean isQuery() {
        return false;
    }

    public boolean hasParameters() {
        return true;
    }

    /**
     * Returns underlying refactoring object.
     * 
     * @return Underlying refactoring object.
     */
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    /**
     * Returns name of the refactoring.
     * 
     * @return Refactoring name.
     */
   
    public String getName() {
        return new MessageFormat(NbBundle.getMessage(FileRenameRefactoringUI.class, "LBL_FileRename")).format (
                    new Object[] { oldFileName }
                );
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(FileRenameRefactoringUI.class);
    }

    /**
     * Returns description of the refactoring.
     * 
     * @return Refactoring description.
     */
    public String getDescription() {
        return new MessageFormat(NbBundle.getMessage(FileRenameRefactoringUI.class, "DSC_Rename")).format (
                    new Object[] { oldFileName, refactoring.getNewName()} 
                );
        
    }

    public Problem checkParameters() {
        newName = panel.getNameValue();
        if (refactoring instanceof RenameRefactoring) {
            ((RenameRefactoring) refactoring).setNewName(newName);
        }
        return refactoring.fastCheckParameters();
    }
    
    public View getView() {
       return view;    
    }
    
    public void setView(View view){
        
        this.view = WhereUsedView.class.cast(view);
    }
    
    
       
    ////////////////////////////////////////////////////////////////////////////
    /**  End Implementation of RefactoringUI
     */
    ////////////////////////////////////////////////////////////////////////////

  
    public Referenceable getTarget() {
        return target;
    }

    public Component getCustomComponent(Collection<RefactoringElement> elements) {
        WhereUsedView view = new WhereUsedView(target);
        GraphHelper gh = new GraphHelper(target);
        
        ArrayList<TreeElement> nodes = new ArrayList<TreeElement>();
        for (RefactoringElement element: elements) {
                TreeElement previewNode = TreeElementFactory.getTreeElement(element);
            if(previewNode != null)
                nodes.add(previewNode);
        }
        
        Graph graph = gh.loadGraph(nodes);
        view.setGraph(graph);
        AnalysisViewer analysisViewer = new AnalysisViewer();
        analysisViewer.setCurrentView(view);
        analysisViewer.getPanel().setMinimumSize(new Dimension(10,10));
        analysisViewer.getPanel().setPreferredSize(new Dimension(10,10));
        view.showView(analysisViewer);
       
       return analysisViewer.getPanel();
    }

    public Icon getCustomIcon() {
         return new ImageIcon(
            Utilities.loadImage(
            "org/netbeans/modules/xml/refactoring/resources/"+
            "graphical_view_refactoring.png"));
    }

    public String getCustomToolTip() {
         return NbBundle.getMessage(WhereUsedQueryUI.class, "LBL_ShowGraph");
    }
    
}
