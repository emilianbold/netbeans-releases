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


package org.netbeans.modules.bpel.design.model.patterns;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.bpel.design.DiagramView;
import org.netbeans.modules.bpel.design.actions.ScrollToOperationAction;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.connections.MessageConnection;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.connections.Connection;

import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.actions.AbstractWrapWithAction;
import org.netbeans.modules.bpel.nodes.actions.AddBasicActivitiesAction;
import org.netbeans.modules.bpel.nodes.actions.AddFromPaletteAction;
import org.netbeans.modules.bpel.nodes.actions.AddPaletteActivityAction;
import org.netbeans.modules.bpel.nodes.actions.AddStructuredActivitiesAction;
import org.netbeans.modules.bpel.nodes.actions.AddWebServiceActivitiesAction;
import org.netbeans.modules.bpel.nodes.actions.BpelNodeAction;
import org.netbeans.modules.bpel.nodes.actions.GoToAction;
import org.netbeans.modules.bpel.nodes.actions.GoToDiagrammAction;
import org.netbeans.modules.bpel.nodes.actions.WrapAction;
import org.openide.actions.NewAction;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexey Yarmolenko
 */

public abstract class Pattern {
    
    
    private UniqueId omReference;
    
    private List<VisualElement> elements = new ArrayList<VisualElement>();
    private List<Connection> connections = new ArrayList<Connection>();
    
    private DiagramModel model;
    private BpelModel bpelModel;
    
    private CompositePattern parent;
    
    private VisualElement textElement;
    private FPoint origin;
    

    private FBounds bounds;
    
    
    public FPoint getOrigin(){
        return origin;
    }
    
    public DiagramView getView(){
        return model.getView().getProcessView();
    }
    
    public void setOrigin(double x, double y){
        origin = new FPoint(x, y);
    }
    
    
    public Pattern(DiagramModel model) {
        this.model = model;
    }
    
    
    public DiagramModel getModel(){
        return this.model;
    }
    
    public BpelModel getBpelModel(){
        return this.bpelModel;
    }
    
    
    public void initPattern(BpelEntity entity){
        this.omReference = entity.getUID();
        
        this.bpelModel = entity.getBpelModel();
        
        createElementsImpl();
        
        updateName();
    }
    
    
    public FBounds getBounds(){
        assert bounds != null: "Layout manager failed to set bounds for pattern " + this;
        return bounds;
    }
    
    
    public void setBounds(FBounds bounds){
        this.bounds = bounds;
    }
    
    public boolean isSelectable() {
        return true;
    }
    
    public boolean isDraggable() {
        return true;
    }
    
    
    public boolean isCollapsable() {
        return false;
    }
    
    
    public BpelEntity getOMReference() {
//        assert bpelModel.getEntity((UniqueId)this.omReference) != null :
//            "Can not resolve element for pattern: " + this;
        if ( this.omReference != null) {
            return bpelModel.getEntity((UniqueId) this.omReference);
            
        }
        
        return null;
    }
    
    
    public void setParent(CompositePattern newParent){
        
        CompositePattern oldParent = parent;
        
        if (oldParent != null){
            oldParent.removePattern(this);
        }
        
        if (newParent != null){
            newParent.appendPattern(this);
        }
        
        parent = newParent;
    }
    
    
    public CompositePattern getParent(){
        return parent;
    }
    
    
    public Collection<VisualElement> getElements(){
        return elements;
        
    }
    
    
    public void removeAllElements(){
        elements.clear();
    }
    
    
    public void appendElement(VisualElement element) {
        elements.add(element);
        element.setPattern(this);
    }
    
    
    public void removeElement(VisualElement element){
        elements.remove(element);
        element.setPattern(null);
    }
    
    
    public void addConnection(Connection connection) {
        if (!connections.contains(connection)) {
            connections.add(connection);
        }
    }
    
    
    public void clearConnections() {
        for (int i = connections.size() - 1; i >= 0; i--) {
            connections.get(i).remove();
        }
    }
    
    
    public void clearConnectionsExcept(Connection connection) {
        for (int i = connections.size() - 1; i >= 0; i--) {
            Connection c = connections.get(i);
            if (c != connection) {
                c.remove();
            }
        }
    }
    
    
    public void removeConnection(Connection connection) {
        connections.remove(connection);
    }
    
    
    public void reconnectElements() {}
    
    
    public abstract VisualElement getFirstElement();
    public abstract VisualElement getLastElement();
    
    
    public abstract FBounds layoutPattern(LayoutManager manager);
    
    
    protected abstract void createElementsImpl();
    
    
    public void createPlaceholders(Pattern draggedPattern,
            Collection<PlaceHolder> placeHolders) {}
    
    
    /**
     * Function builds the popup menu to show on diagram element.
     * Menu actions are taken from Node wrapping the underlying BpelOM element.
     * Derived classes should override this method to add extra menu items.
     * @returns menu to show.
     **/
    public JPopupMenu createPopupMenu() {
        //construct the node for current element
        Node node = getModel().getView().getNodeForPattern(this);
        
        if (node != null){
            //set menu label, used by some L&F
            JPopupMenu menu = new JPopupMenu(node.getDisplayName());
            
            Action collapseExpandAction = getModel().getView()
                    .getCollapseExpandDecorationProvider()
                    .createCollapseExpandAction(this);
            if (collapseExpandAction != null) {
                JMenuItem item = menu.add(collapseExpandAction);
                item.setIcon(null);
            }
            
            JMenuItem scrollToPartnerLink = createScrollToPartnerLinkMenuItem();
            if (scrollToPartnerLink != null) {
                menu.add(scrollToPartnerLink);
            }
            
            JMenuItem scrollToOperation = createScrollToOperationMenuItem();
            if (scrollToOperation != null) {
                menu.add(scrollToOperation);
            }
            
            //populate a list of actions
            Action actions[] = node.getActions(true);
            if (actions != null ){
                for (Action a: actions){
                    if (a instanceof BpelNodeAction) {
                        // inforce action enable status 
                        // It's required in case activated node haven't been changed but enable status changes
                        a.setEnabled(((BpelNodeAction)a).enable(new Node[]{node}));
                    }
                    
                    //null Action indicates a separator
                    if (a instanceof NewAction) {
                        // vb todo add handler for NewAction and sub actions
                        JMenu submenu = new JMenu(NbBundle.getMessage(Pattern.class, "LBL_Add")); // NOI18N
                        List<BpelNodeAction> addActions =  ((BpelNode)node).getAddActions();
                        for (BpelNodeAction elem : addActions) {
                            submenu.add(new Actions.MenuItem(elem, false));
                        }
                        menu.add(submenu);
                    }  else if (a instanceof WrapAction ) {
                        AbstractWrapWithAction[] wrapActions = ((WrapAction)a).getWrapActions(new Node[] {node});
                        // todo m
                        if (wrapActions.length == 1) {
                            menu.add(new Actions.MenuItem(a, false));
                        } else {
                            JMenu submenu = new JMenu(((WrapAction)a).getBundleName());
                            for (AbstractWrapWithAction wrapElem : wrapActions) {
                                submenu.add(new Actions.MenuItem(wrapElem, false));
                            }
                            menu.add(submenu);
                        }
                    }  else if (a instanceof GoToAction ) {
                        BpelNodeAction[] gotoActions = ((GoToAction)a).getGoToActions(new Node[] {node});
                        // todo m
                        if (gotoActions.length == 1 
                                && !(a instanceof GoToDiagrammAction)) 
                        {
                            menu.add(new Actions.MenuItem(a, false));
                        } else {
                            JMenu submenu = new JMenu(((GoToAction)a).getBundleName());
                            for (BpelNodeAction gotoElem : gotoActions) {
                                if (gotoElem instanceof GoToDiagrammAction) {
                                    continue;
                                }
                                submenu.add(new Actions.MenuItem(gotoElem, false));
                            }
                            menu.add(submenu);
                        }
                    } else if (a instanceof AddFromPaletteAction ) {
                        BpelNodeAction[] categoriesActions = ((AddFromPaletteAction)a).getCategoriesAction(new Node[] {node});
                        // todo m
                        if (categoriesActions != null && categoriesActions.length > 0) {
                            JMenu submenu = new JMenu(((AddFromPaletteAction)a).getBundleName());
                            for (BpelNodeAction paletteCategory : categoriesActions) {

                                AddPaletteActivityAction[] paletteActions = null;
                                if (paletteCategory instanceof AddBasicActivitiesAction) {
                                     paletteActions = ((AddBasicActivitiesAction)paletteCategory)
                                        .getPaletteActions(new Node[] {node});
                                } else if (paletteCategory instanceof AddStructuredActivitiesAction) {
                                     paletteActions = ((AddStructuredActivitiesAction)paletteCategory)
                                        .getPaletteActions(new Node[] {node});
                                } else if (paletteCategory instanceof AddWebServiceActivitiesAction) {
                                     paletteActions = ((AddWebServiceActivitiesAction)paletteCategory)
                                        .getPaletteActions(new Node[] {node});
                                }
                                
                                if (paletteActions != null && paletteActions.length == 1) {
                                    submenu.add(new Actions.MenuItem(paletteCategory, false));
                                } else if (paletteActions != null ) {
                                    JMenu subsubmenu = new JMenu(((BpelNodeAction)paletteCategory).getName());
                                    for (AddPaletteActivityAction paletteElem : paletteActions) {
                                        subsubmenu.add(new Actions.MenuItem(paletteElem, false));
                                    }
                                    submenu.add(subsubmenu);
                                }
                            }
                            menu.add(submenu);
                        }
                    } else if (a != null) {
                        // it's diagramm already !
                        if (!(a instanceof GoToDiagrammAction)) {
                            menu.add(new Actions.MenuItem(a, false));
                        }
                    } else {
                        menu.addSeparator();
                    }
                }
            }
            return menu;
        }
        
        
        return null;
    }
    
    private class AddAction implements Action {
        private NewAction na;
        public AddAction(NewAction na) {
            this.na = na;
        }
        
        public Object getValue(String key) {
            return na.getValue(key);
        }
        
        public void putValue(String key, Object value) {
            na.putValue(key, value);
        }
        
        public void setEnabled(boolean b) {
        }
        
        public boolean isEnabled() {
            return true;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
        
        public void actionPerformed(ActionEvent e) {
        }
    }
    
    private class AddActionSubMenuModel implements Actions.SubMenuModel {
        
        private List<BpelNodeAction> addActions;
        private BpelNode node;
        public AddActionSubMenuModel(List<BpelNodeAction> addActions, BpelNode node) {
            this.addActions = addActions;
            this.node = node;
        }
        
        public int getCount() {
            return addActions == null ? -1 : addActions.size();
        }
        
        public String getLabel(int index) {
            if (index < 0 || index > (getCount()-1)
            || addActions.get(index) == null) {
                return null;
            }
            
            return addActions.get(index).getName();
        }
        
        public HelpCtx getHelpCtx(int index) {
            if (index < 0 || index > (getCount()-1)
            || addActions.get(index) == null) {
                return null;
            }
            
            return addActions.get(index).getHelpCtx();
        }
        
        public void performActionAt(int index) {
            if (index < 0 || index > (getCount()-1)
            || addActions.get(index) == null) {
                return;
            }
            
            addActions.get(index).performAction(new Node[] {node});
        }
        
        public void addChangeListener(ChangeListener l) {
        }
        
        public void removeChangeListener(ChangeListener l) {
        }
        
    }
    
    /*
     *Checks if p is one of the parents in pattern hierarchy
     */
    public boolean isNestedIn(Pattern parent){
        Pattern p = getParent();
        while( p != null){
            if (p == parent){
                return true;
            }
            p = p.getParent();
        }
        return false;
    }
    
    public boolean isInModel(){
        Pattern root = model.getRootPattern();
        return ((this == root) || isNestedIn(root));
    }
    
    
    public String getDefaultName() {
        return ""; // NOI18N
    }
    
    
    public void registerTextElement(VisualElement newTextElement) {
        this.textElement = newTextElement;
    }
    
    
    public VisualElement getTextElement() {
        return textElement;
    }
    
    
    public boolean isTextElement(VisualElement element) {
        return (element != null) ? element == textElement : false;
    }
    
    
    public void setText(String text) {
        if (textElement == null) return;
        textElement.setText(text);
    }
    
    
    public String getText() {
        return (textElement != null) ? textElement.getText() : null;
    }
    
    
    public void updateName() {
        if (getOMReference() instanceof NamedElement) {
            String  n = ((NamedElement) getOMReference()).getName();
            setText(n);
        } else {
            setText(getDefaultName());
        }
    }
    
    
    
    public String toString(){
        return getClass().getName() + hashCode();
    }
    
    
    public NodeType getNodeType() {
        return NodeType.UNKNOWN_TYPE;
    }
    
    
    public List<Connection> getConnections() {
        return connections;
    }
    
    
    public List<MessageConnection> getMessageConnections() {
        List<MessageConnection> result = new ArrayList<MessageConnection>();
        
        for (Connection connection : getConnections()) {
            if (connection instanceof MessageConnection) {
                result.add((MessageConnection) connection);
            }
        }
        
        return result;
    }
    
    
    public Set<Pattern> getConnectedParnerLinkPatterns() {
        Set<Pattern> result = new HashSet<Pattern>();
        
        for (Connection connection : getConnections()) {
            if (!(connection instanceof MessageConnection)) continue;
            
            Pattern targetPattern = connection.getTarget().getPattern();
            Pattern sourcePattern = connection.getSource().getPattern();
            
            if ((targetPattern.getOMReference() instanceof PartnerLink)
                    && (sourcePattern == this))
            {
                result.add(targetPattern);
            } else if ((sourcePattern.getOMReference() instanceof PartnerLink)
                    && (targetPattern == this))
            {
                result.add(sourcePattern);
            }
        }
        
        return result;
    }
    
    
    public Area createSelection() {
        return createOutline();
    }
    
    
    public Area createOutline() {
        Area result = new Area();
        for (VisualElement ve : getElements()) {
            if (ve.getWidth() < 2 && ve.getHeight() < 2) continue;
            result.add(ve.getShape().createArea());
        }
        return result;
    }
    
    
    public boolean refersTo(UniqueId id) {
        return omReference.equals(id);
        
    }
    
    private JMenuItem createScrollToPartnerLinkMenuItem() {
        Set<Pattern> patterns = getConnectedParnerLinkPatterns();
        
        if (patterns.isEmpty()) return null;
        
        if (patterns.size() == 1) {
            return new JMenuItem(new ScrollToPattern(
                    patterns.iterator().next(), 
                    NbBundle.getMessage(Pattern.class, 
                            "LBL_ScrollToPartnerLink"))); // NOI18N
        } 
        
        JMenu menu = new JMenu(NbBundle.getMessage(Pattern.class, 
                "LBL_ScrollToPartnerLink")); // NOI18N
        
        for (Pattern p : patterns) {
            menu.add(new ScrollToPattern(p));
        }
        
        return menu;
    }
    
    private JMenuItem createScrollToOperationMenuItem() {
        for (Connection c : getConnections()) {
            if (c instanceof MessageConnection) {
                return new JMenuItem(
                        new ScrollToOperation((MessageConnection) c));
            }
        }
        return null;
    }
    
    static class ScrollToOperation extends AbstractAction {
        private MessageConnection messageConnection;
        
        public ScrollToOperation(MessageConnection connection) {
            super(ScrollToOperationAction.ACTION_NAME);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, 
                    KeyEvent.ALT_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
            this.messageConnection = connection;
        }
        
        public void actionPerformed(ActionEvent event) {
            messageConnection.getSource().getPattern().getModel().getView()
                    .scrollToOperation(messageConnection);
        }
    }
    
    
    static class ScrollToPattern extends AbstractAction {
        private Pattern pattern;
        
        public ScrollToPattern(Pattern pattern) {
            this(pattern, null);
        }
        
        
        public ScrollToPattern(Pattern pattern, String name) {
            if (name == null) {
                name = pattern.getText();
                if (name == null || name.trim().equals("")) { // NOI18N
                    name = NbBundle.getMessage(Pattern.class, "LBL_Unnamed"); // NOI18N
                }
            }
            
            this.pattern = pattern;
            
            putValue(NAME, name);
        }

        public void actionPerformed(ActionEvent e) {
            pattern.getView().scrollPatternToView(pattern);
            //pattern.getModel().getView().scrollPatternToView(pattern);
        }
    }
}

