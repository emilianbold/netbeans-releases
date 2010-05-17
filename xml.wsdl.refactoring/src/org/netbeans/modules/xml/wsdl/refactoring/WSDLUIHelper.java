/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.xml.wsdl.refactoring;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLDataObject;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.NodesFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.ui.actions.GoToAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Jeri Lockhart
 */
public class WSDLUIHelper {
    
    private static final SystemAction[] ACTIONS =  
        new SystemAction[] {
            SystemAction.get(GoToAction.class)
        };
    
    private WSDLHelperNode displayNode;
    
    /**
     * Creates a new instance of WSDLUIHelper
     */
    public WSDLUIHelper() {
        super();
    }

  /**
     * Return UI relevant path from root.  Specific implementation should
     * override.
     */
   
    public List<Component> getRelevantPathFromRoot(Component component) {
        ArrayList<Component> pathFromRoot = new ArrayList<Component>();
        Component dc = component;
        pathFromRoot.add(dc);
        while (dc.getParent() != null) {
            dc = dc.getParent();
            if(!(dc instanceof Definitions)) {
                pathFromRoot.add(0, dc);
            }
            
        }
        return pathFromRoot;
    }
    /**
     * Returns specific node for displaying the component in a preview window.
     * 
     * The Node should return the following information that will be used
     * in the refactoring UI: 
     * 
     * getActions(boolean) - 
     * other Actions for the Component, preferably navigational actions
     * Minimally, getActions() should return a Go To Source Action, which
     * will open the source (text) view with the cursor at the Component line 
     * The Actions should also implement org.openide.util.actions.Presenter.
     * When the action is invoked from a prefuse graph node, 
     * actionPerformed(ActionEvent) is called with the Component as the source
     * in the ActionEvent.
     * 
     * getDisplayName() - 
     * a String that will be used as the label on the Component's explorer and 
     * graph nodes.
     *  
     * getHtmlDisplayName() -
     * For the usage component, a one line code snippet with the name
     * of the query component bolded.  The Html display name is used in the
     * Find Usages explorer and the refactoring preview explorer on the
     * usage node.
     * The string should be formatted to use &lt and &gt for
     * the XML tags, and < and > for the HTML tags.  In the following example,
     * Find Usages was run on a schema global type named "POSLogCurrencyCode".
     * The Node represents a schema local element that uses POSLogCurrencyCode.
     * getHtmlDisplayName() returns the first line of the local element.  
     * The text "POSLogCurrencyCode" in the snippet will be bolded because it is 
     * the name of the query Component.
     * 
     * &lt;xs:element name="CurrencyCode" type="<b>POSLogCurrencyCode</b>" minOccurs="0"/&gt;
     * 
     * getIcon() -      
     * an Image for the icon on the Components explorer and graph nodes.
     * 
     * getPreferredAction() - 
     * the Action which navigates to the primary view of the Component
     */
   
    public Node getDisplayNode(Component component) {
        if (! (component instanceof WSDLComponent) && 
            ! (component instanceof SchemaComponent)) 
        {
            return null;
        }
        
        if (component instanceof SchemaComponent) {
            SchemaComponent sc = (SchemaComponent) component;
            SchemaModel sm = sc.getModel();
            assert sm != null : "Given a dead component";
           // return null;
           // UIHelper delegate = RefactoringManager.getInstance().getTargetComponentUIHelper(sm);
         //   if (delegate != null) {
         //       return delegate.getDisplayNode(component);
         //   } 
        }
        
        Model model = component.getModel();
        try {
        if(model instanceof WSDLModel) {
            ModelSource ms = model.getModelSource();
            FileObject fo = (FileObject) ms.getLookup().lookup(FileObject.class);
            if(fo != null) {
                DataObject dObj = DataObject.find(fo);
                if(dObj != null && dObj instanceof WSDLDataObject) {
                    Node node = NodesFactory.getInstance().create(component);
                    if(node != null) {
                        Node filterNode = new FilterNode(node) {
                             @Override
                            public SystemAction[] getActions(boolean val) {
                                return ACTIONS;
                             }
                             
                             @Override
                            public Action getPreferredAction() {
                                  return ACTIONS[0];
                             }
                        };
                        
                        return filterNode;
                    }
                }
            }
        }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        
         if (displayNode == null){
            displayNode = new WSDLHelperNode(component);
        } 
        
        return displayNode;
    }
    
    
    public Node getDisplayNode(Model model) {
        if (model instanceof DocumentModel) {
            return getDisplayNode(((DocumentModel)model).getRootComponent());
        } 
        return null;
    }
    
   public class WSDLHelperNode extends AbstractNode {
       private Component comp;
              
       public WSDLHelperNode(Component component){
           super(Children.LEAF);
           this.comp = component;
           initialize();
       }
       
       public void initialize() {   
            this.setName("Temporary Name");                  // Name
       }
       
        @Override
        public Image getIcon(int i) {
            return super.getIcon(i);
        }

        /**
         * XML code snippet
         *
         */
        @Override
        public String getHtmlDisplayName() {
            return "Temporary <b>Name</b>";
        }

        @Override
        public Action[] getActions(boolean b) {
           return ACTIONS;
        }

        @Override
        public Action getPreferredAction() {
            return ACTIONS[0];
        }
        
       
   } 
  
   
   
    
}
