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
package org.netbeans.modules.xml.schema.abe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.schema.abe.nodes.ABEAbstractNode;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.windows.TopComponent;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class InstanceDesignerPanel extends ABEBaseDropPanel {
    public static final int EXPAND_BY_DEFAULT_LIMIT  = 50;
    private static final long serialVersionUID = 7526472295622776147L;
    /**
     *
     *
     */
    public InstanceDesignerPanel(AXIModel axiModel, DataObject schemaDataObject, TopComponent tc) {
        this(axiModel, schemaDataObject, tc, new InstanceUIContext());
    }
    
    private InstanceDesignerPanel(AXIModel axiModel, DataObject schemaDataObject, TopComponent tc, InstanceUIContext context) {
        super(context);
        this.axiModel = axiModel;
        context.initialize(tc, schemaDataObject, this, getPaletteController());
        initComponents();
        initialize();
        initMouseListener();
    }
    
    /**
     *
     *
     */
    private void initialize() {
        initComponents();                
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(true);
        wrapperPanel.setBackground(new Color(252, 250, 245));
//        wrapperPanel.putClientProperty("print.printable", Boolean.TRUE); // NOI18N
//        wrapperPanel.putClientProperty("print.size", new java.awt.Dimension(width, height)); // NOI18N
        /*wrapperPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                weakThis.get().dispatchEvent(e);
            }
            public void mousePressed(MouseEvent e) {
                weakThis.get().dispatchEvent(e);
            }
            public void mouseReleased(MouseEvent e) {
                weakThis.get().dispatchEvent(e);
            }
        });*/
        
        namespacePanel = new NamespacePanel(context);
        add(getNamespacePanel(), BorderLayout.NORTH);
        //wrapperPanel.add(namespacePanel, BorderLayout.NORTH);
        
        boolean expand = getAXIModel().getRoot().getElements().size() > EXPAND_BY_DEFAULT_LIMIT ? false : true;
        globalElementsChildrenPanel = new GlobalElementsContainerPanel(
                getUIContext(), getAXIModel().getRoot(), expand);
        TitleWrapperPanel geWrapper = new TitleWrapperPanel(globalElementsChildrenPanel,
                globalElementsStr, getAXIModel().getRoot(), expand, context){
            private static final long serialVersionUID = 7526472295622776147L;
            public int getChildrenItemsCount() {
                return getAXIModel().getRoot().getElements().size();
            }
        };
        wrapperPanel.add(geWrapper, BorderLayout.NORTH);        
        
        expand = getAXIModel().getRoot().getContentModels().size() > 0 ? false : true;        
        globalComplextypeChildrenPanel = new GlobalComplextypeContainerPanel(
                getUIContext(), getAXIModel().getRoot(), expand);
        TitleWrapperPanel gcWrapper = new TitleWrapperPanel(globalComplextypeChildrenPanel,
                globalComplexTypesStr, getAXIModel().getRoot(), expand, context){
            private static final long serialVersionUID = 7526472295622776147L;
            public int getChildrenItemsCount() {
                int count = 0;
                for(ContentModel cm: getAXIModel().getRoot().getContentModels()){
                    if(cm.getType() == cm.getType().COMPLEX_TYPE)
                        count++;
                }
                return count;
            }
        };
        wrapperPanel.add(gcWrapper, BorderLayout.CENTER);
        
        JScrollPane scrollPane=new JScrollPane(wrapperPanel);
        context.setInstanceDesignerScrollPane(scrollPane);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.setBorder(null);
        scrollPane.setViewportBorder(null);
        scrollPane.setAutoscrolls(true);
        add(scrollPane,BorderLayout.CENTER);
        
        context.getComponentSelectionManager().setSelectedComponent(namespacePanel);
        context.setFocusTraversalManager(new FocusTraversalManager(context));

        // vlv: print
        wrapperPanel.putClientProperty("print.printable", Boolean.TRUE); // NOI18N
    }
    
    private static String globalElementsStr = NbBundle.getMessage(InstanceDesignerPanel.class,"" +
            "LBL_GLOBAL_ELEMENTS");
    private static String globalComplexTypesStr = NbBundle.getMessage(InstanceDesignerPanel.class,"" +
            "LBL_GLOBAL_COMPLEXTYPES");
    
    
    
    /**
     *
     *
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());

        setBackground(java.awt.Color.white);
    }// </editor-fold>//GEN-END:initComponents
    
    
    /**
     *
     *
     */
    public SchemaModel getSchemaModel() {
        return schemaModel;
    }
    
    
    /**
     *
     *
     */
    public InstanceUIContext getUIContext() {
        return context;
    }
    
    
    /**
     *
     *
     */
    public static synchronized PaletteController getPaletteController() {
        if (paletteController!=null)
            return paletteController;
        
        PaletteActions actions=
                new PaletteActionsImpl();
        
        DragAndDropHandler dndHandler = 
                new DragAndDropHandlerImpl();
        
        try {
            paletteController=
                    PaletteFactory.createPalette("xmlschema-abe-palette",actions, null, dndHandler);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        return paletteController;
    }
    
    public AXIModel getAXIModel() {
        return axiModel;
    }
    
    public NamespacePanel getNamespacePanel() {
        return namespacePanel;
    }
    
    
    
    public void selectUIComponent(AXIComponent axiComponent){
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try{
            ABEBaseDropPanel uiComp = null;
            if(axiComponent == null){
                uiComp = namespacePanel;
            }else{
                //get the path from the root to the child.
                LinkedList<AXIComponent> path = new LinkedList<AXIComponent>();
                AXIComponent current = axiComponent;
                path.addFirst(current);
                while( (current != null)  && !(current.getParent() instanceof AXIDocument) ){
                    current = current.getParent();
                    path.addFirst(current);
                }
                //start opening up the UI components and reach the leaf
                current = path.getFirst();
                
                if(current instanceof Element){
                    //then the root is global element
                    uiComp = globalElementsChildrenPanel.getChildUIComponentFor(current);
                }else if(current instanceof ContentModel){
                    //need to try with global complex type
                    uiComp = globalComplextypeChildrenPanel.getChildUIComponentFor(current);
                }
                if(uiComp == null)
                    return;
                path.removeFirst();
                for(AXIComponent comp: path){
                    ABEBaseDropPanel cur = uiComp.getChildUIComponentFor(comp);
                    if(cur == null)
                        return;
                    uiComp = cur;
                }
            }
            context.getComponentSelectionManager().setSelectedComponent(uiComp);
            final ABEBaseDropPanel ftemp = uiComp;
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    UIUtilities.scrollViewTo(ftemp, context);
                }
            });
        }finally{
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    
    public void selectUIComponent(org.netbeans.modules.xml.xam.Component component) {
        if(component == null){
            selectUIComponent(null);
            return;
        }
        if(component instanceof AXIComponent)
            selectUIComponent((AXIComponent)component);
        else {
            AXIComponent axiComp = null;
            try{
                axiComp = UIUtilities.findMatchingAXIComponent((SchemaComponent) component);
            }catch (Exception e){}
            selectUIComponent(axiComp);
        }
    }
    
    public GlobalElementsContainerPanel getGlobalElementsPanel() {
        return globalElementsChildrenPanel;
    }
    
    public GlobalComplextypeContainerPanel getGlobalComplextypePanel() {
        return globalComplextypeChildrenPanel;
    }
    
    /*void setUserInducedEventMode(boolean mode) {
        if(this.userInducedEventMode == mode)
            return;
        Boolean oldValue = Boolean.valueOf(userInducedEventMode);
        this.userInducedEventMode = mode;
        firePropertyChange(PROP_USER_INDUCED_EVENT_MODE, oldValue,
                Boolean.valueOf(mode));
    }*/
    
    public void accept(UIVisitor visitor) {
        visitor.visit(context.getNamespacePanel());
    }
    
    public ABEAbstractNode getNBNode() {
        //just return the namespace panel node
        return context.getNamespacePanel().getNBNode();
    }
    
    
    
    protected void initMouseListener(){
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                mouseClickedActionHandler(e);
            }
            public void mouseClicked(MouseEvent e){
                mouseClickedActionHandler(e);
            }
            
            public void mousePressed(MouseEvent e) {
                mouseClickedActionHandler(e);
            }
            
        });
    }
    
    
    protected void mouseClickedActionHandler(MouseEvent e){
        if(e.getClickCount() == 1){
            if(e.isPopupTrigger()){
                context.getMultiComponentActionManager().showPopupMenu(e, this);
                return;
            }
        }
        //the tag is selected
        context.getComponentSelectionManager().setSelectedComponent(this);
    }
    
    public void drop(DropTargetDropEvent event) {
        getNamespacePanel().drop(event);
    }
    
    public void dragExit(DropTargetEvent event) {
        getNamespacePanel().dragExit(event);
    }
    
    public void dragEnter(DropTargetDragEvent event) {
        getNamespacePanel().dragEnter(event);
    }
    
    public void dragOver(DropTargetDragEvent event) {
        getNamespacePanel().dragOver(event);
    }
    
    public void shutdown() {
        context.shutdown();
        context = null;
    }
    
    
////////////////////////////////////////////////////////////////////////////
// Instance variables
////////////////////////////////////////////////////////////////////////////
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    private SchemaModel schemaModel;
    private AXIModel axiModel;
    private static PaletteController paletteController;
    GlobalElementsContainerPanel globalElementsChildrenPanel;
    List<Component> childrenList = new ArrayList<Component>();
    NamespacePanel namespacePanel;
    GlobalComplextypeContainerPanel globalComplextypeChildrenPanel;
//private boolean userInducedEventMode;
//public static final String PROP_USER_INDUCED_EVENT_MODE = "user_induced_event_mode";
}

class PaletteActionsImpl extends PaletteActions {
    public Action[] getImportActions() {
        return new Action[0];
    }
    
    public Action[] getCustomPaletteActions() {
        return new Action[0];
    }
    
    public Action[] getCustomCategoryActions(Lookup lookup) {
        return new Action[0];
    }
    
    public Action[] getCustomItemActions(Lookup lookup) {
        return new Action[0];
    }
    
    public Action getPreferredAction(Lookup lookup) {
        return null;
    }
    
}

class DragAndDropHandlerImpl extends DragAndDropHandler {

    public DragAndDropHandlerImpl() {
        super( true );
    }
    
    @Override
    public void customize(ExTransferable t, Lookup item) {
    }

}
