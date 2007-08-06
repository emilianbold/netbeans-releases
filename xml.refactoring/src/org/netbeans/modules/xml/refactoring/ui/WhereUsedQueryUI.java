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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.BeanInfo;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.RefactoringCustomUI;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.xml.nbprefuse.AnalysisViewer;
import org.netbeans.modules.xml.nbprefuse.View;
import org.netbeans.modules.xml.nbprefuse.util.GraphUtilities;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.refactoring.ui.GraphHelper;
import org.netbeans.modules.xml.refactoring.spi.AnalysisUtilities;
import org.netbeans.modules.xml.refactoring.ui.views.WhereUsedView;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import prefuse.data.Graph;

/**
 *
 * @author Jeri Lockhart
 */
public class WhereUsedQueryUI implements org.netbeans.modules.refactoring.spi.ui.RefactoringUI, RefactoringCustomUI {
    
    private String name = ""; //NOI18N
    private WhereUsedQuery query;
    private Referenceable ref;
    private WhereUsedView view;
    
    /** Creates a new instance of WhereUsedQueryUI */
    public WhereUsedQueryUI(Referenceable ref) {
        this.view = view;
        this.ref = ref;
        name = SharedUtils.getName(ref);
        query = new WhereUsedQuery(Lookups.singleton(ref));
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
        return new WhereUsedPanel(ref, parent);
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
        return null;
    }

    /**
     * Indicates whether this class represents a real refactoring that changes
     * code or whether it is just a query (e.g. all usages for a class).
     * 
     * @return <code>true</code> if the class represents only a query,
     * <code>false</code> if the class represents a real refactoring.
     */
    public boolean isQuery() {
        return true;
    }

    public boolean hasParameters() {
        return false;
    }

    /**
     * Returns underlying refactoring object.
     * 
     * @return Underlying refactoring object.
     */
    public AbstractRefactoring getRefactoring() {
        return query;
    }

    /**
     * Returns name of the refactoring.
     * 
     * @return Refactoring name.
     */
   
    public String getName() {
        return new MessageFormat(NbBundle.getMessage(WhereUsedQueryUI.class, "LBL_WhereUsed")).format (
                    new Object[] {name}
                );
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(WhereUsedQueryUI.class);
    }

    /**
     * Returns description of the refactoring.
     * 
     * @return Refactoring description.
     */
    public String getDescription() {
        return new MessageFormat(NbBundle.getMessage(WhereUsedQueryUI.class, "DSC_WhereUsed")).format (
                    new Object[] {name} 
                );
        
    }

    public Problem checkParameters() {
        return null;
    }
    
    public View getView() {
       return view;    
    }

    public Referenceable getTarget() {
        return ref;
    }
    
    /**
     * View graph View of Usages
     */
    public void setView(View view) {
        this.view = WhereUsedView.class.cast(view);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    /**  End Implementation of RefactoringUI
     */
    ////////////////////////////////////////////////////////////////////////////

    public Component getCustomComponent(Collection<RefactoringElement> elements) {
        GraphHelper gh = new GraphHelper(ref);
        WhereUsedView view = new WhereUsedView(ref);
       // Map<RefactoringElement, TreeElement> nodes = new HashMap<RefactoringElement,TreeElement>();
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
