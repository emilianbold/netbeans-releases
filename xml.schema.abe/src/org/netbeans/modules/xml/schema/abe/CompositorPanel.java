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
 * CompositorPanel.java
 *
 * Created on June 6, 2006, 4:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.Compositor.CompositorType;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.schema.abe.nodes.ABEAbstractNode;
import org.netbeans.modules.xml.schema.abe.nodes.CompositorNode;
import org.netbeans.modules.xml.schema.abe.palette.DnDHelper;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author girix
 */
public class CompositorPanel extends ElementsContainerPanel{
    private static final long serialVersionUID = 7526472295622776147L;
    
    CompositorType compositorType;
    ExpandCollapseButton expandButton;
    
    InplaceEditableLabel compositorTypeLabel;
    JPanel compositorTypePanel;
    JLabel contentModelInfoLabel;
    private ABEAbstractNode compositorNode;
    
    private JLabel itemCountLabel;
    private static final int BORDER_THICKNESS = 2;
    private static final EmptyBorder emptyBorder = new EmptyBorder(BORDER_THICKNESS, BORDER_THICKNESS,
            BORDER_THICKNESS, BORDER_THICKNESS);
    
    public CompositorPanel(InstanceUIContext context, Compositor compositor,
            Component parentPanel) {
        super(context, compositor, parentPanel, true);
        setBorder(emptyBorder);
        //draw annotation
        setDrawAnnotation(false);
        initMouseListener();
        initKeyListener();
        makeNBNode();
        addSelectionListener();
        
        compositor.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        verifyChildrenWithModel();
                    }
                });
            }
        });
    }
    
    
    protected void initMouseListener(){
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                mouseClickedActionHandler(e, true);
            }
            public void mouseClicked(MouseEvent e){
                mouseClickedActionHandler(e, false);
            }
            
            public void mousePressed(MouseEvent e) {
                mouseClickedActionHandler(e, true);
            }
        });
    }
    
    
    protected void mouseClickedActionHandler(MouseEvent e, boolean handelPopupOnly){
        if(e.getClickCount() == 1){
            if(e.isPopupTrigger()){
                context.getMultiComponentActionManager().showPopupMenu(e, this);
                return;
            }
            if(handelPopupOnly)
                return;
            //the tag is selected
            if(e.isControlDown())
                context.getComponentSelectionManager().addToSelectedComponents(this);
            else
                context.getComponentSelectionManager().setSelectedComponent(this);
        }
    }
    
    protected void initKeyListener(){
        addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if( e.getKeyCode() == e.VK_F2 ){
                    compositorTypeLabel.showEditor();
                }
                if(context.getFocusTraversalManager().isFocusChangeEvent(e))
                    context.getFocusTraversalManager().handleEvent(e, CompositorPanel.this);
            }
            public void keyReleased(KeyEvent e) {
            }
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == e.VK_SPACE){
                    compositorTypeLabel.showEditor();
                    
                }
            }
        });
    }
    
    
    SpringLayout compositorTypePanelLayout;
    public void addHeaderPanel(){
        AutoSizingPanel rbp = new AutoSizingPanel(context);
        rbp.setHorizontalScaling(true);
        rbp.setOpaque(false);
        compositorTypePanel = rbp;
        //compositorTypePanel.setOpaque(true);
        compositorTypePanelLayout = new SpringLayout();
        compositorTypePanel.setLayout(compositorTypePanelLayout);
        compositorTypeLabel = new InplaceEditableLabel(getCompositorTypeString());
        compositorTypeLabel.setForeground(InstanceDesignConstants.COMPOSITOR_TYPE_LABEL_COLOR);
        
        if(getAXIParent().isReadOnly()){
            //compositorTypeLabel.setIcon(UIUtilities.getImageIcon("import-include-redefine.png"));
            compositorTypeLabel.setToolTipText(NbBundle.getMessage(CompositorPanel.class,
                    "TTP_COMPOSITOR_READONLY", getCompositorType().getName()));
        }
        
        //add indentation
        if( !(parentPanel instanceof ElementPanel)) {
            //if the parent panel is a compositor then dont indent
            setPanelIndendation(0);
        }
        
        Component hgap = Box.createHorizontalStrut(getPanelIndendation());
        compositorTypePanel.add(hgap);
        compositorTypePanelLayout.putConstraint(SpringLayout.WEST, hgap, 0,
                SpringLayout.WEST, compositorTypePanel);
        compositorTypePanelLayout.putConstraint(SpringLayout.NORTH, hgap, 0,
                SpringLayout.NORTH, compositorTypePanel);
        
        compositorTypePanel.add(compositorTypeLabel);
        compositorTypePanelLayout.putConstraint(SpringLayout.WEST, compositorTypeLabel, 0,
                SpringLayout.EAST, hgap);
        compositorTypePanelLayout.putConstraint(SpringLayout.NORTH, compositorTypeLabel, 0,
                SpringLayout.NORTH, compositorTypePanel);
        
        
        
        hgap = Box.createHorizontalStrut(10);
        compositorTypePanel.add(hgap);
        compositorTypePanelLayout.putConstraint(SpringLayout.WEST, hgap, 0,
                SpringLayout.EAST, compositorTypeLabel);
        compositorTypePanelLayout.putConstraint(SpringLayout.NORTH, hgap, 0,
                SpringLayout.NORTH, compositorTypePanel);
        
        CompositorPropertiesPanel cpp = new CompositorPropertiesPanel(getCompositor(), context);
        compositorTypePanel.add(cpp);
        compositorTypePanelLayout.putConstraint(SpringLayout.WEST, cpp, 0,
                SpringLayout.EAST, hgap);
        compositorTypePanelLayout.putConstraint(SpringLayout.NORTH, cpp, 0,
                SpringLayout.NORTH, compositorTypePanel);
        
        //add an artificial mouse listener for transmiting the mouse events to the parent
        cpp.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e) {
                CompositorPanel.this.dispatchEvent(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                CompositorPanel.this.dispatchEvent(e);
            }
            
            public void mousePressed(MouseEvent e) {
                CompositorPanel.this.dispatchEvent(e);
            }
            
        });
        
        compositorTypePanel.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e) {
                CompositorPanel.this.dispatchEvent(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                CompositorPanel.this.dispatchEvent(e);
            }
            
            public void mousePressed(MouseEvent e) {
                CompositorPanel.this.dispatchEvent(e);
            }
            
        });
        
        
        hgap = Box.createHorizontalStrut(10);
        compositorTypePanel.add(hgap);
        compositorTypePanelLayout.putConstraint(SpringLayout.WEST, hgap, 0,
                SpringLayout.EAST, cpp);
        compositorTypePanelLayout.putConstraint(SpringLayout.NORTH, hgap, 0,
                SpringLayout.NORTH, compositorTypePanel);
        
        //init item count label
        itemCountLabel = new JLabel();
        itemCountLabel.setForeground(Color.GRAY.brighter());
        refreshItemCount();
        compositorTypePanel.add(itemCountLabel);
        compositorTypePanelLayout.putConstraint(SpringLayout.WEST, itemCountLabel, 0,
                SpringLayout.EAST, hgap);
        compositorTypePanelLayout.putConstraint(SpringLayout.NORTH, itemCountLabel, 0,
                SpringLayout.NORTH, compositorTypePanel);
        
        
        
        add(compositorTypePanel, BorderLayout.NORTH);
        
        //add an artificial mouse listener for transmiting the mouse events to the parent
        compositorTypeLabel.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e) {
                CompositorPanel.this.dispatchEvent(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                CompositorPanel.this.dispatchEvent(e);
            }
            
            public void mousePressed(MouseEvent e) {
                CompositorPanel.this.dispatchEvent(e);
            }
            
        });
        
        initCompositorTypeEditListener();
    }
    
    
    protected void initCompositorTypeEditListener(){
        compositorTypeLabel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(InplaceEditableLabel.PROPERTY_MODE_CHANGE)){
                    if(evt.getNewValue() == InplaceEditableLabel.Mode.EDIT){
                        //user selected edit give the editor JComponent
                        //show a combo box field
                        CompositorType options[] = filterAllIfNeeded(getCompositor());
                        final JComboBox field = new JComboBox(options);
                        field.setSelectedItem(getCompositorType());
                        field.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e) {
                                CompositorType newType = (CompositorType) field.getSelectedItem();
                                //do validation
                                compositorTypeLabel.hideEditor();
                                setCompositorTypeInModel(newType);
                                
                                if(getParentContainerPanel() instanceof ElementPanel){
                                    //since model removes and adds all the element and compositors, open the element by default
                                    ((ElementPanel)getParentContainerPanel()).expandChild();
                                }
                            }
                        });
                        if(getAXIParent().isReadOnly()){
                            String str = NbBundle.getMessage(CompositorPanel.class,
                                    "MSG_READONLY_COMPOSITORTYPE_EDIT", getCompositorType().getName());
                            compositorTypeLabel.setEditInfoText(str, context);
                        }else{
                            compositorTypeLabel.setInlineEditorComponent(field);
                        }
                    }
                }
            }
        });
    }
    
    
    void initButton(){
        expandButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleExpandOrCollapse();
            }
        });
    }
    
    void handleExpandOrCollapse(){
        if(!expandButton.isExpanded()){
            //expand
            expandChild();
        }else{
            //collapse
            collapseChild();
        }
    }
    
    void expandChild(){
        if(childrenPanel != null){
            //children already added just show
            childrenPanel.setVisible(true);
            revalidate();
            repaint();
        }else{
            //children, if present, are expanded and added by default.
        }
    }
    
    void collapseChild(){
        if(childrenPanel != null){
            //children already added just hide
            childrenPanel.setVisible(false);
            revalidate();
            repaint();
        }
    }
    
    void refreshItemCount(){
        //add some space in the end to keep the label away from the border paint area.
        int size = getAXIChildren().size();
        String item = NbBundle.getMessage(ContainerPanel.class, "LBL_ITEM_STRING");
        String items = NbBundle.getMessage(ContainerPanel.class, "LBL_ITEMS_STRING");
        String itemStr = size == 1 ? item : items;
        itemCountLabel.setText("["+size+ " "+itemStr+"]     ");
    }
    
    public void _paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        super.setAnnotationString(getCompositorType().toString());
        Rectangle rect = this.getBounds();
        int x = rect.x + this.getWidth() + 2;
        int y = rect.y - 10;
        Point pt = new Point(x, y);
        super.setStartDrawPoint(pt);
        super.paintComponent(g2d);
    }
    
    
    public CompositorType getCompositorType(){
        AXIComponent parent = getAXIParent();
        if( parent instanceof Compositor)
            return ((Compositor)parent).getType();
        
        return null;
    }
    
    public String getCompositorTypeString(){
        CompositorType compType= getCompositorType();
        String comp = null;
        switch(compType){
            case SEQUENCE:
                comp = NbBundle.getMessage(CompositorPanel.class,"LBL_Sequence");
                break;
            case CHOICE:
                comp = NbBundle.getMessage(CompositorPanel.class,"LBL_Choice");
                break;
            case ALL:
                comp = NbBundle.getMessage(CompositorPanel.class,"LBL_All");
                break;
        }
        if(comp == null)
            comp = getCompositorType().getName();
        StringBuffer str = new StringBuffer(comp);
        while(str.length() < 8)
            str.append(" ");
        return str.toString();
    }
    
    public void setCompositorTypeInModel(CompositorType ctype){
        ((Compositor) getAXIParent()).setType(ctype);
    }
    
    private Compositor getCompositor() {
        return (Compositor) getAXIParent();
    }
    
    public List<? extends AXIComponent> getAXIChildren() {
        return getAXIParent().getChildren();
    }
    
    
    public void removeElement(Element element) {
        super.removeElement(element);
        refreshItemCount();
    }
    
    public void addElement(Element element) {
        super.addElement(element);
        refreshItemCount();
    }
    
    
    public void addCompositor(Compositor newCompositor){
        //look in to the children list find out where the child was added
        //create a new CompositorPanel and add @ that index. Adjust the layout accordingly
        int index = getAXIChildren().indexOf(newCompositor);
        
        if(isAlreadyAdded(newCompositor) != null)
            return;
        
        CompositorPanel cp = new CompositorPanel(context, newCompositor, this);
        addChildAt(cp, index);
        refreshItemCount();
    }
    
    public void removeChildCompositor(Compositor compositor){
        //look in to the children list find out where the child was present
        //remove the CompositorPanel @ that index. Adjust the layout accordingly
        Component rmComp = null;
        for(Component component: getChildrenList()){
            if(component instanceof CompositorPanel){
                if( ((CompositorPanel)component).getCompositor() == compositor){
                    rmComp = component;
                    break;
                }
                
            }
        }
        if(rmComp != null){
            removeComponent(rmComp);
        }
        refreshItemCount();
    }
    
    
    private void addNewCompositorAt(TweenerPanel tweener, DnDHelper.PaletteItem compType){
        int index = getChildrenList().indexOf(tweener);
        if(index == -1){
            //must not happen
            return;
        }
        index = index/2;
        AXIModel model = getAXIParent().getModel();
        
        Compositor comp = null;
        switch(compType){
            case SEQUENCE:
                comp = model.getComponentFactory().createSequence();
                break;
            case CHOICE:
                comp = model.getComponentFactory().createChoice();
                break;
            case ALL:
                comp = model.getComponentFactory().createAll();
                break;
        }
        
        if(comp == null)
            return;
        
        model.startTransaction();
        try{
            getAXIParent().addChildAtIndex(comp, index);
        }finally{
            model.endTransaction();
        }
    }
    
    protected void setupAXIComponentListener(){
        super.setupAXIComponentListener();
        getAXIParent().addPropertyChangeListener(new ModelEventMediator(this, getAXIParent()) {
            public void _propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(Compositor.PROP_COMPOSITOR)){
                    //a compositor event
                    if((evt.getOldValue() == null) && (evt.getNewValue() != null)){
                        //new element added
                        addCompositor((Compositor) evt.getNewValue());
                    }else if((evt.getNewValue() == null) && (evt.getOldValue() != null)){
                        //old element removed
                        removeChildCompositor((Compositor) evt.getOldValue());
                    }
                }else if(evt.getPropertyName().equals(Compositor.PROP_TYPE)){
                    //handle compositor change event
                    compositorTypeLabel.setText(getCompositorTypeString());
                }
                refreshItemCount();
            }
        });
        
    }
    
    
    public void visit(Compositor compositor) {
        super.visit(compositor);
        visitorResult = null;
        CompositorPanel compPanel = new CompositorPanel(context,
                compositor, CompositorPanel.this);
        visitorResult = compPanel;
    }
    
    
    public void tweenerDrop(TweenerPanel tweener, DnDHelper.PaletteItem paletteItem) {
        super.tweenerDrop(tweener, paletteItem);
        if(DnDHelper.isCompositor(paletteItem) && (paletteItem != paletteItem.ALL) &&
                ((Compositor)getAXIComponent()).getType() != CompositorType.ALL ){
            //add only seq/choice
            addNewCompositorAt(tweener, paletteItem);
        }
    }
    
    public void tweenerDragEntered(TweenerPanel tweener, DnDHelper.PaletteItem paletteItem) {
        super.tweenerDragEntered(tweener, paletteItem);
        if(!tweenerDragAccept(tweener, paletteItem))
            return;
        //this.setDrawBorder(true);
        LineBorder lineBorder = new LineBorder(InstanceDesignConstants.DARK_BLUE, BORDER_THICKNESS, true);
        this.setBorder(lineBorder);
        this.repaint();
        
        //set the info message for tweener panel to display
        
        ContentModel cm = getCompositor().getContentModel();
        if(cm != null){
            String type = UIUtilities.getContentModelTypeString(cm.getType());
            String locDrpStrType = NbBundle.getMessage(UIUtilities.class, "MSG_DROP_INFO_TYPE");
            tweener.setDropInfoText(locDrpStrType+" "+type+" "+cm.getName());
        }else{
            String locDrpStrLocal = NbBundle.getMessage(UIUtilities.class, "MSG_DROP_INFO_LOCAL");
            tweener.setDropInfoText(locDrpStrLocal+getCompositor().getType().getName());
        }
    }
    
    public boolean tweenerDragAccept(TweenerPanel tweener, DnDHelper.PaletteItem paletteItem) {
        if(getAXIParent().isReadOnly()){
            String str = NbBundle.getMessage(CompositorPanel.class,
                    "MSG_READONLY_COMPOSITOR_DROP", getAXIParent().getContentModel().getName());
            UIUtilities.showErrorMessageFor(str, context, tweener);
            return false;
        }
        
        if( (getCompositorType() == CompositorType.ALL) &&
                (paletteItem != DnDHelper.PaletteItem.ELEMENT)){
            String str = NbBundle.getMessage(CompositorPanel.class,
                    "MSG_ALL_COMPOSITOR_DROP_REJECT");
            UIUtilities.showErrorMessageFor(str, context, tweener);
            return false;
        }
        //accept only element and compositor
        if( (paletteItem == DnDHelper.PaletteItem.ELEMENT) ||
                ( (DnDHelper.isCompositor(paletteItem)) && (paletteItem != paletteItem.ALL))){
            return true;
        }
        String str = NbBundle.getMessage(CompositorPanel.class,
                "MSG_COMPOSITOR_DROP_REJECT");
        UIUtilities.showErrorMessageFor(str, context, tweener);
        return false;
    }
    
    public void tweenerDragExited(TweenerPanel tweener) {
        super.tweenerDragExited(tweener);
        UIUtilities.hideGlassMessage();
        this.setBorder(emptyBorder);
        this.repaint();
    }
    
    protected void makeNBNode() {
        compositorNode = new CompositorNode((Compositor) getAXIParent(), context);
        if(getAXIParent().isReadOnly())
            ((ABEAbstractNode)compositorNode).setReadOnly(true);
    }
    
    public ABEAbstractNode getNBNode() {
        return compositorNode;
    }
    
    public AXIComponent getAXIComponent() {
        return getAXIParent();
    }
    
    public void removeCompositor() {
        if(getAXIComponent() instanceof Compositor){
            AXIComponent axiComponent = getAXIComponent();
            if(axiComponent.getModel() != null){
                context.getAXIModel().startTransaction();
                try{
                    axiComponent.getParent().removeChild(getAXIComponent());
                }finally{
                    context.getAXIModel().endTransaction();
                }
            }
        }
    }
    
    private void addSelectionListener() {
        addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(PROP_SELECTED)){
                    if(((Boolean)evt.getNewValue()).booleanValue()){
                        //set the tag name color to orange
                        compositorTypeLabel.setForeground(
                                InstanceDesignConstants.COMPOSITOR_TYPE_LABEL_SELECTED_COLOR);
                        Font font = compositorTypeLabel.getFont();
                        font = new Font(font.getName(), Font.BOLD, font.getSize());
                        compositorTypeLabel.setFont(font);
                        drawBoldString(true);
                        setBorder(new LineBorder(InstanceDesignConstants.XP_ORANGE, BORDER_THICKNESS, true));
                    }else{
                        //set the tage name color to normal color
                        compositorTypeLabel.setForeground(
                                InstanceDesignConstants.COMPOSITOR_TYPE_LABEL_COLOR);
                        Font font = compositorTypeLabel.getFont();
                        font = new Font(font.getName(), Font.PLAIN, font.getSize());
                        compositorTypeLabel.setFont(font);
                        drawBoldString(false);
                        setBorder(emptyBorder);
                    }
                    
                }
            }
        });
    }
    
    public void showExpandButton(boolean show){
        expandButton.setVisible(show);
    }
    
    public void accept(UIVisitor visitor) {
        visitor.visit(this);
    }
    
    
    public static CompositorType[] filterAllIfNeeded(Compositor compositor){
        if(compositor.getParent() instanceof Compositor){
            //parent is also a compositor. This cant be all
            return new CompositorType[] {CompositorType.SEQUENCE, CompositorType.CHOICE};
        }
        for(AXIComponent child: compositor.getChildren()){
            if(child instanceof Compositor){
                //children contains a compositor so this cant be all
                return new CompositorType[] {CompositorType.SEQUENCE, CompositorType.CHOICE};
            }
        }
        return CompositorType.values();
        
    }
}
