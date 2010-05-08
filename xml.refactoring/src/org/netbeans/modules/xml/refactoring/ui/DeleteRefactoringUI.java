/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.ui.RefactoringCustomUI;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.netbeans.modules.xml.nbprefuse.AnalysisViewer;
import org.netbeans.modules.xml.nbprefuse.View;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.ui.GraphHelper;
import org.netbeans.modules.xml.refactoring.ui.views.WhereUsedView;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import prefuse.data.Graph;

/**
 *
 * @author Jeri Lockhart
 */
public class DeleteRefactoringUI implements org.netbeans.modules.refactoring.spi.ui.RefactoringUI, RefactoringCustomUI{
    
    private WhereUsedQuery query;
    private WhereUsedView view;
    private CustomRefactoringPanel panel;
    private AbstractRefactoring refactoring;
    private NamedReferenceable target;
    
    /** Creates a new instance of WhereUsedQueryUI */
    public DeleteRefactoringUI(NamedReferenceable target){
        this.target= target;
        refactoring = new SafeDeleteRefactoring(Lookups.singleton(target));
        XMLRefactoringTransaction transaction = new XMLRefactoringTransaction((Referenceable)target, refactoring);
        refactoring.getContext().add(transaction);
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
        if(panel == null)
            panel = new SafeDeletePanel(target);
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


    public HelpCtx getHelpCtx() {
        return new HelpCtx(DeleteRefactoringUI.class);
    }

    /**
     * Returns description of the refactoring.
     * 
     * @return Refactoring description.
     */
    public String getDescription() {
        return new MessageFormat(NbBundle.getMessage(DeleteRefactoringUI.class, "LBL_Delete")).format (
                    new Object[] {target.getName() } 
                );
        
    }

    public Problem checkParameters() {
        return refactoring.fastCheckParameters();
    }
    
    
    public View getView() {
       return view;    
    }
    
    public void setView(View view){
        this.view = WhereUsedView.class.cast(view);
    }
    
      
    
    /**
     * Returns name of the refactoring.
     * 
     * @return Refactoring name.
     */
    public String getName() {
        return new MessageFormat(NbBundle.getMessage(DeleteRefactoringUI.class, "LBL_Delete")).format (
                    new Object[] {target.getName()}
                );
    }
    
    ////////////////////////////////////////////////////////////////////////////
    /**  End Implementation of RefactoringUI
     */
    ////////////////////////////////////////////////////////////////////////////
   
   public NamedReferenceable getTarget() {
        return target;
    }
   
   
     public Component getCustomComponent(Collection<RefactoringElement> elements) {
        WhereUsedView view = new WhereUsedView(target);
        GraphHelper gh = new GraphHelper((target));
        
        
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
         return ImageUtilities.loadImageIcon(
             "org/netbeans/modules/xml/refactoring/resources/" + 
             "graphical_view_refactoring.png", false);
    }

    public String getCustomToolTip() {
         return NbBundle.getMessage(WhereUsedQueryUI.class, "LBL_ShowGraph");
    }
}
