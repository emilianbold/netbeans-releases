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
 * Created on May 24, 2006, 5:28 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AnyAttribute;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.schema.abe.palette.DnDHelper;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author girix
 */
public abstract class ContainerPanel extends AnnotatedBorderPanel implements AXIVisitor{
    private static final long serialVersionUID = 7526472295622776147L;
    private AXIComponent axiParent;
    Component parentPanel;
    private static final int INTER_PANEL_VERTICAL_SPACE = 0;
    private int panelIndendation = InstanceDesignConstants.TAG_INDENT ;
    private LinkedList<Component> childrenList = new LinkedList<Component>();
    protected Component visitorResult = null;
    boolean openByDefault = true;
    
    public ContainerPanel(InstanceUIContext context, AXIComponent axiParent,
            Component parentPanel, boolean openByDefault) {
        super(context);
        this.setAXIParent(axiParent);
        this.parentPanel = parentPanel;
        this.openByDefault = openByDefault;
        initialize();
    }
    
    protected abstract void setupAXIComponentListener();
    public abstract List<? extends AXIComponent> getAXIChildren();
    
    private void initialize(){
        setOpaque(false);
        setLayout(new BorderLayout());
        addHeaderPanel();
        initChildrenPanel();
        if(openByDefault)
            addAllChildren();
        setupAXIComponentListener();
    }
    
    
    public void addHeaderPanel(){
    }
    
    JPanel childrenPanel;
    SpringLayout childrenPanelLayout;
    Component childrenPanelLastComponent;
    public void initChildrenPanel(){
        //create a child panel that could expand and collapse
        childrenPanelLayout = new SpringLayout();
        childrenPanel = new JPanel(childrenPanelLayout);
        childrenPanel.setOpaque(false);
        
        add(childrenPanel, BorderLayout.CENTER);
        
        
        //add always a tweener panel to save drop logic in the compositorTypePanel
        TweenerPanel tweener = new TweenerPanel(SwingConstants.HORIZONTAL, context);
        //appendChild(tweener);
        childrenPanel.add(tweener);
        getChildrenList().add(tweener);
        addTweenerListener(tweener);
        
        //always compensate the expand button of the compositor panel
        childrenPanelLayout.putConstraint(SpringLayout.WEST, tweener,
                getChildrenIndent(), SpringLayout.WEST, childrenPanel);
        childrenPanelLayout.putConstraint(SpringLayout.NORTH, tweener,
                getInterComponentVerticalSpacing(), SpringLayout.NORTH, childrenPanel);
        adjustChildrenPanelSize();
        addComponentEventListener(tweener);
        childrenPanelLastComponent = tweener;
        
    }
    
    
    public void verifyChildrenWithModel(){
        List<Component> noTweenerList = new ArrayList<Component>();
        for(Component comp : getChildrenList()){
            if(comp instanceof TweenerPanel)
                continue;
            noTweenerList.add(comp);
        }
        
        if(noTweenerList.size() != getAXIChildren().size()){
            //this will solve the count problem
            removeAndAddAllChildren();
            return;
        }
        
        for(AXIComponent axiComponent : getAXIChildren()){
            //ensure that all the children are in order and
            if(isAlreadyAdded(axiComponent) == null){
                removeAndAddAllChildren();
                return;
            }
        }
    }
    
    
    public void removeAndAddAllChildren(){
        this.remove(childrenPanel);
        childrenPanel = null;
        initChildrenPanel();
        //childrenAdded = false;
        addAllChildren();
    }
    
    boolean childrenAdded = false;
    public void addAllChildren(){
        if(childrenAdded)
            return;
        childrenAdded = true;
        final List<? extends AXIComponent> children = getAXIChildren();
        if(children.size() < InstanceDesignerPanel.EXPAND_BY_DEFAULT_LIMIT){
            addAllChildren(children) ;
        }else{
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    addAllChildren(children) ;
                }
            });
        }
    }
    
    private void addAllChildren(List<? extends AXIComponent> children){
        initProgress(children);
        for(final AXIComponent axiComp: children){
            showProgress();
            visitorResult = null;
            axiComp.accept(ContainerPanel.this);
            final Component compToAdd = visitorResult;
            if(visitorResult != null){
                appendChild(compToAdd, false);
                TweenerPanel tweener = new TweenerPanel(SwingConstants.HORIZONTAL, context);
                addTweenerListener(tweener);
                appendChild(tweener, false);
            }
        }
        adjustChildrenPanelSize();
        revalidate();
        finishProgress();
    }
    
    
    ProgressHandle progressHandle = null;
    int stepCount = 0;
    private void initProgress(List<? extends AXIComponent> children) {
        final int size = children.size();
        if( size < InstanceDesignerPanel.EXPAND_BY_DEFAULT_LIMIT){
            return;
        }
        UIUtilities.showBulbMessage(NbBundle.getMessage(ContainerPanel.class,
                "MSG_RENDERING_CHILDREN")+"...", this.context);
        progressHandle= ProgressHandleFactory.createHandle(NbBundle.getMessage(
                ContainerPanel.class, "MSG_RENDERING_CHILDREN")+": ");
        progressHandle.setInitialDelay(1);
        progressHandle.start(size+10);
        UIUtilities.setBusyCursor(this.context);
        stepCount = 0;
    }
    
    private void showProgress(){
        if(progressHandle != null){
            progressHandle.progress(stepCount++);
        }
    }
    
    private void finishProgress(){
        if(progressHandle != null){
            progressHandle.finish();
            progressHandle = null;
            UIUtilities.setDefaultCursor(this.context);
            UIUtilities.hideGlassMessage();
            stepCount = 0;
        }
    }
    
    public int getChildrenIndent(){
        return getPanelIndendation()+InstanceDesignConstants.COMPOSITOR_CHILDREN_INDENT;//+ExpandCollapseButton.WIDTH;
    }
    
    public void appendChild(Component component, boolean resize){
        appendChild(component, 0, resize);
    }
    
    public void appendChild(Component component, int vPadding, boolean resize){
        childrenPanel.add(component);
        getChildrenList().add(component);
        //always compensate the expand button of the compositor panel
        childrenPanelLayout.putConstraint(SpringLayout.WEST, component,
                getChildrenIndent(), SpringLayout.WEST, childrenPanel);
        childrenPanelLayout.putConstraint(SpringLayout.NORTH, component,
                getInterComponentVerticalSpacing() + vPadding, SpringLayout.SOUTH, childrenPanelLastComponent);
        
        childrenPanelLastComponent = component;
        if(resize){
            adjustChildrenPanelSize();
            revalidate();
        }
        addComponentEventListener(component);
    }
    
    public boolean addChildAt(Component component, int index) {
        childrenPanel.add(component);
        Component cAbove = null;
        Component cBelow = null;
        try{
            cAbove = getAboveAdjacentComponent(index);
            cBelow = getBelowAdjacentComponent(index);
        }catch(Exception e){
            removeAndAddAllChildren();
            return false;
        }
        if(cAbove == null){
            //this usecase is invalid
            return false;
        }
        //standard left spring
        childrenPanelLayout.putConstraint(SpringLayout.WEST, component,
                getChildrenIndent(), SpringLayout.WEST, childrenPanel);
        
        childrenPanelLayout.putConstraint(SpringLayout.NORTH, component,
                getInterComponentVerticalSpacing(), SpringLayout.SOUTH, cAbove);
        //add a tweener panel...always
        TweenerPanel tweener = new TweenerPanel(SwingConstants.HORIZONTAL, context);
        addTweenerListener(tweener);
        childrenPanel.add(tweener);
        childrenPanelLayout.putConstraint(SpringLayout.WEST, tweener,
                getChildrenIndent(), SpringLayout.WEST, childrenPanel);
        
        childrenPanelLayout.putConstraint(SpringLayout.NORTH, tweener,
                getInterComponentVerticalSpacing(), SpringLayout.SOUTH, component);
        
        if(cBelow != null){
            //adjust the spring between tweener and below
            childrenPanelLayout.putConstraint(SpringLayout.NORTH, cBelow,
                    getInterComponentVerticalSpacing(), SpringLayout.SOUTH, tweener);
        }
        
        //int cAboveIndex = getChildrenList().indexOf(cAbove);
        if(getChildrenList().getLast() == cAbove){
            //already last element so just append
            getChildrenList().add(component);
            getChildrenList().add(tweener);
        }else{
            getChildrenList().add(getChildrenList().indexOf(cAbove) + 1, component);
            getChildrenList().add(getChildrenList().indexOf(component) + 1, tweener);
        }
        
        adjustChildrenPanelSize();
        revalidate();
        //repaint();
        addComponentEventListener(component);
        addComponentEventListener(tweener);
        return true;
    }
    
    
    public void removeComponent(Component component){
        int cIndex = getChildrenList().indexOf(component);
        //tweener panel index
        int belowTIndex = cIndex + 1;
        int aboveTIndex = cIndex - 1;
        Component belowTweener = getChildrenList().get(belowTIndex);
        Component aboveTweener = getChildrenList().get(aboveTIndex);
        Component belowComponent = null;
        if(getChildrenList().getLast() != belowTweener){
            belowComponent = getChildrenList().get(belowTIndex + 1);
        }
        
        childrenPanelLayout.removeLayoutComponent(component);
        childrenPanelLayout.removeLayoutComponent(belowTweener);
        childrenPanel.remove(component);
        childrenPanel.remove(belowTweener);
        fireComponentRemoved();
        
        if(belowComponent != null){
            childrenPanelLayout.putConstraint(SpringLayout.NORTH, belowComponent,
                    getInterComponentVerticalSpacing(), SpringLayout.SOUTH, aboveTweener);
        }
        
        getChildrenList().remove(component);
        getChildrenList().remove(belowTweener);
        
        adjustChildrenPanelSize();
        revalidate();
        repaint();
        
    }
    
    
    
    private void addComponentEventListener(Component comp){
        comp.addComponentListener(new ComponentListener() {
            public void componentHidden(ComponentEvent e) {
                adjustChildrenPanelSize();
            }
            public void componentMoved(ComponentEvent e) {
            }
            int callCount = 1;
            public void componentResized(ComponentEvent e) {
                if( (callCount <= 2) && ( (ContainerPanel.this instanceof GlobalComplextypeContainerPanel)
                ||(ContainerPanel.this instanceof GlobalElementsContainerPanel) ) ){
                    //skip this event for the first time.
                    //If not done then there is a 10 sec delay in the UI after expanding
                    //many nodes in a huge schema.
                    callCount++;
                    return;
                }
                adjustChildrenPanelSize();
            }
            public void componentShown(ComponentEvent e) {
                adjustChildrenPanelSize();
            }
        });
    }
    
    public Component getAboveAdjacentComponent(int index){
        return getChildrenList().get(index * 2);
    }
    
    public Component getBelowAdjacentComponent(int index){
        int ind = (index * 2) + 1;
        if(ind >= getChildrenList().size())
            return null;
        return getChildrenList().get(ind);
    }
    
    
    public void adjustChildrenPanelSize() {
        int width = 0;
        int height = 0;
        int indent = getChildrenIndent();
        int spacing = getInterComponentVerticalSpacing();
        for(Component child: childrenPanel.getComponents()){
            if(!child.isVisible())
                break;
            Dimension dim = child.getPreferredSize();
            int curWidth = dim.width +
                    indent;//+50;
            if(curWidth > width)
                width = curWidth;
            height += dim.height + spacing;
        }
        //add some fudge
        width += 20;
        Dimension dim = new Dimension(width, height+3);
        
        Dimension old = childrenPanel.getPreferredSize();
        boolean revalidateChild = false;
        if( (old.height != dim.height) || (old.width != dim.width) ){
            childrenPanel.setPreferredSize(dim);
            childrenPanel.setMinimumSize(dim);
            revalidateChild = true;
        }
        
        dim = _getMinimumSize();
        old = getPreferredSize();
        boolean revalidateMe = false;
        if( (old.height != dim.height) || (old.width != dim.width) ){
            setMinimumSize(dim);
            setPreferredSize(dim);
            revalidateMe = true;
        }
        if(revalidateChild)
            childrenPanel.revalidate();
        if(revalidateMe)
            revalidate();
        
    }
    
//Following set of methods needed for the tag size calculation and horizontal bar display logic
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }
    
    public Dimension _getMinimumSize() {
        int width = 0;
        int height = 0;
        for(Component child: this.getComponents()){
            if(!child.isVisible())
                break;
            Dimension dim = child.getPreferredSize();
            height += dim.getHeight();
            int thisW = dim.width ;
            width = width < thisW ? thisW : width;
        }
        return new Dimension(width, height);
    }
    
    
    
    public ABEBaseDropPanel isAlreadyAdded(AXIComponent axiComp){
        for(Component comp: getChildrenList()){
            //check if this component is already added to the list. if yes return
            if(comp instanceof ABEBaseDropPanel){
                if(((ABEBaseDropPanel)comp).getAXIComponent() == axiComp )
                    return (ABEBaseDropPanel)comp;
            }
        }
        return null;
    }
    
    public boolean tweenerDragAccept(TweenerPanel tweener, DnDHelper.PaletteItem paletteItem) {
        return true;
    }
    
    public void tweenerDrop(TweenerPanel tweener, DnDHelper.PaletteItem paletteItem) {
    }
    
    public void tweenerDragExited(TweenerPanel tweener) {
    }
    
    public void tweenerDragEntered(TweenerPanel tweener, DnDHelper.PaletteItem paletteItem) {
    }
    
    
    protected void addTweenerListener(final TweenerPanel tweener) {
        tweener.addTweenerListener(new TweenerListener(){
            public boolean dragAccept(DnDHelper.PaletteItem paletteItem) {
                return ContainerPanel.this.tweenerDragAccept(tweener, paletteItem);
            }
            
            public void drop(DnDHelper.PaletteItem paletteItem) {
                ContainerPanel.this.tweenerDrop(tweener, paletteItem);
            }
            
            public void dragExited() {
                ContainerPanel.this.tweenerDragExited(tweener);
            }
            
            public void dragEntered(DnDHelper.PaletteItem paletteItem) {
                ContainerPanel.this.tweenerDragEntered(tweener, paletteItem);
            }
        });
    }
    
    
    public int getInterComponentVerticalSpacing(){
        return INTER_PANEL_VERTICAL_SPACE;
    }
    
    
    public void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);
    }
    
    public AXIComponent getAXIParent() {
        return axiParent;
    }
    
    public void setAXIParent(AXIComponent axiParent) {
        this.axiParent = axiParent;
    }
    
    
    public int getPanelIndendation() {
        return panelIndendation;
    }
    
    public void setPanelIndendation(int panelIndendation) {
        this.panelIndendation = panelIndendation;
    }
    
    
    
    public LinkedList<Component> getChildrenList() {
        return childrenList;
    }
    
    public void setChildrenList(LinkedList<Component> childrenList) {
        this.childrenList = childrenList;
    }
    
    public void visit(AnyAttribute attribute) {
    }
    
    public void visit(Element element) {
    }
    
    public void visit(ContentModel element) {
    }
    
    public void visit(Datatype datatype) {
    }
    
    public void visit(AnyElement element) {
    }
    
    public void visit(AXIDocument root) {
    }
    
    public void visit(Attribute attribute) {
    }
    
    public void visit(Compositor compositor) {
    }
    
    public AXIComponent getAXIComponent() {
        return getAXIParent();
    }
    
    public ABEBaseDropPanel getChildUIComponentFor(AXIComponent axiComponent){
        this.addAllChildren();
        this.setVisible(true);
        for(Component comp: getChildrenList()){
            if(comp instanceof ABEBaseDropPanel){
                ABEBaseDropPanel uiComp = ((ABEBaseDropPanel)comp).getUIComponentFor(axiComponent);
                if( uiComp != null)
                    return uiComp;
            }
        }
        return null;
    }
    
    public ABEBaseDropPanel getParentContainerPanel(){
        return (ABEBaseDropPanel)this.parentPanel;
    }
}
