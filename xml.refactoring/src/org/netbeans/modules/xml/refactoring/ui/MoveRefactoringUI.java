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
package org.netbeans.modules.xml.refactoring.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringCustomUI;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.netbeans.modules.xml.nbprefuse.AnalysisViewer;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.ui.views.WhereUsedView;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import prefuse.data.Graph;
import org.openide.util.Utilities;

public class MoveRefactoringUI implements org.netbeans.modules.refactoring.spi.ui.RefactoringUI, RefactoringCustomUI{
    
    private MoveFilePanel panel;
    private MoveRefactoring refactoring;
    private FileObject targetFile;
    private String oldFileName;
    private Project srcProject;
    private Model target;
    private boolean disable;
    private String targetPkgName = "";
    
    
       
   public MoveRefactoringUI(Model target) {
       this.target = target;
       refactoring = new MoveRefactoring(Lookups.singleton(target));
       XMLRefactoringTransaction transaction = new XMLRefactoringTransaction((Referenceable)target, refactoring);
       refactoring.getContext().add(transaction);
       targetFile = target.getModelSource().getLookup().lookup(FileObject.class);
       srcProject = FileOwnerQuery.getOwner(targetFile);
       oldFileName = targetFile.getName();
       
       //save the original folder name
       try {
           URL url = targetFile.getParent().getURL();
           refactoring.getContext().add(new URL(url.toExternalForm()));
       } catch(Exception ex) {
            ex.printStackTrace();
        }
   }
       
    /**
     * Returns name of the refactoring.
     * 
     * @return Refactoring name.
     */
   
    public String getName() {
        return new MessageFormat(NbBundle.getMessage(FileRenameRefactoringUI.class, "LBL_FileMove")).format (
                    new Object[] { oldFileName }
                );
    }
     
    public String getDescription() {
        return new MessageFormat(NbBundle.getMessage(FileRenameRefactoringUI.class, "DSC_FileMove")).format (
                new Object[] {oldFileName, packageName()}
        );
    }
    
    public boolean isQuery() {
        return false;
    }
        
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            String pkgName = targetFile.getParent().getName();
            panel = new MoveFilePanel (parent, pkgName, 
                    new MessageFormat(NbBundle.getMessage(MoveFilePanel.class, "LBL_FileMove")).format(new Object[] {oldFileName}),
                    targetFile);
                
            panel.setCombosEnabled(!disable);
        }
        return panel;
    }
        
    private Problem setParameters(boolean checkOnly) {
        if (panel==null)
            return null;
        targetPkgName = panel.getPackageName ();

        URL url = URLMapper.findURL(panel.getRootFolder(), URLMapper.EXTERNAL);
        try {
            refactoring.setTarget(Lookups.singleton(new URL(url.toExternalForm() + URLEncoder.encode(panel.getPackageName().replace('.','/')))));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        if (checkOnly) {
            return refactoring.fastCheckParameters();
        } else {
            return refactoring.checkParameters();
        }
    }
    
    public Problem checkParameters() {
        return setParameters(true);
    }
    
    public Problem setParameters() {
        return setParameters(false);
    }
    
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }
    
    public boolean hasParameters() {
        return true;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(MoveRefactoringUI.class);
    }

    private String packageName () {
        return targetPkgName.trim().length() == 0 ? NbBundle.getMessage(MoveRefactoringUI.class, "LBL_DefaultPackage") : targetPkgName.trim ();
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
