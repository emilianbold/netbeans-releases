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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIContainer;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.schema.abe.action.AttributeOnElementNewType;
import org.netbeans.modules.xml.schema.abe.action.ElementOnElementNewType;
import org.netbeans.modules.xml.schema.abe.palette.DnDHelper;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

/**
 *
 *
 */
public class StartTagPanel extends TagPanel {
    protected static final long serialVersionUID = 7526472295622776147L;    
    private ExpandCollapseButton expandButton;
    private Font tagNameFont;
    
    public StartTagPanel(final ElementPanel elementPanel, InstanceUIContext context) {
        super(elementPanel, context);
        //add listener for name changes
        elementPanel.getAXIContainer().addPropertyChangeListener(new ModelEventMediator(this, elementPanel.getAXIContainer()) {
            public void _propertyChange(PropertyChangeEvent evt) {
                String property = evt.getPropertyName();
                if(property.equals(Element.PROP_NAME)){
                    setTagName(getElementPanel().getAXIContainer().getName());
                    forceSizeRecalculate();
                    revalidate();
                    repaint();
                } else if(property.equals(Attribute.PROP_ATTRIBUTE) ||
                          property.equals(Attribute.PROP_ATTRIBUTE_REF)){
                    updateAttributes();
                    if(evt.getNewValue() != null){
                        //its a new attribute add event
                        if((StartTagPanel.super.context.getUserActedComponent() == StartTagPanel.this)){
                            showAttributes();
                            showAttributeEditFor((Attribute) evt.getNewValue());
                            StartTagPanel.super.context.resetUserActedComponent();
                        }
                    }
                }else if(evt.getPropertyName().equals(Element.PROP_TYPE)){
                    //indirectly update the element properties panel
                    updateAttributes();
                }
            }
        });
        
        initialize();
        initKeyListener();
    }
    
    
    protected void initKeyListener(){
        addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if( e.getKeyCode() == e.VK_F2 ){
                    tagNameLabel.showEditor();
                }
                if(context.getFocusTraversalManager().isFocusChangeEvent(e))
                    context.getFocusTraversalManager().handleEvent(e, StartTagPanel.this);
            }
            public void keyReleased(KeyEvent e) {
            }
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == e.VK_SPACE){
                    tagNameLabel.showEditor();
                }else if(e.getKeyChar() == e.VK_E){
                    //create a new child element
                    for(NewType nt : getNBNode().getNewTypes()){
                        if(nt instanceof ElementOnElementNewType){
                            try {  nt.create();  } catch (IOException ex) {}
                            return;
                        }
                    }
                }else if(e.getKeyChar() == e.VK_A){
                    for(NewType nt : getNBNode().getNewTypes()){
                        //create a new attribute
                        if(nt instanceof AttributeOnElementNewType){
                            try {  nt.create();  } catch (IOException ex) {}
                            return;
                        }
                    }
                }
            }
        });
    }
    
    
    boolean initialized = false;
    SpringLayout startTagPanelLayout;
    Color tagNameLabelColor = InstanceDesignConstants.TAG_NAME_COLOR;
    private void initialize() {
        initialized = true;
        setOpaque(false);
        startTagPanelLayout = new SpringLayout();
        setLayout(startTagPanelLayout);
        
        attributeCollapseButton.setToolTipText(NbBundle.getMessage(StartTagPanel.class, "TTP_ATTR_EXPAND_COLLAPSE_BUTTON"));
        attributeCountLabel.setForeground(InstanceDesignConstants.ITEM_COUNT_COLOR);
        
        
        expandButton = getElementPanel().getExpandButton();
        add(expandButton);
        startTagPanelLayout.putConstraint(SpringLayout.WEST, expandButton,
                getA(), SpringLayout.WEST, this);
        startTagPanelLayout.putConstraint(SpringLayout.NORTH, expandButton,
                LABEL_HEAD_ROOM_SPACE+5, SpringLayout.NORTH, this);
        //excludePaintComponentList.add(expandButton);
        
        
        tagNameLabel = new InplaceEditableLabel();
        tagNameLabel.setToolTipText(NbBundle.getMessage(StartTagPanel.class, "TTP_ELEMENT_NAME_LABEL"));
        
        tagNameLabelColor = InstanceDesignConstants.TAG_NAME_COLOR;
        
        if(getElementPanel().getAXIContainer().isReadOnly() ||
                (getElementPanel().getAXIContainer() instanceof AnyElement)){
            tagNameLabelColor = InstanceDesignConstants.TAG_NAME_READONLY_COLOR;
            tagNameLabel.setToolTipText(NbBundle.getMessage(StartTagPanel.class, "TTP_ELEMENT_NAME_LABEL_READONLY"));
        }else{
            if(getElementPanel().getAXIContainer().isShared()){
                tagNameLabelColor = InstanceDesignConstants.TAG_NAME_SHARED_COLOR;
                tagNameLabel.setToolTipText(NbBundle.getMessage(StartTagPanel.class, "TTP_ELEMENT_NAME_LABEL_SHARED"));
            }
        }
        
        /*if(getElementPanel().getAXIContainer().isReadOnly()){
            tagNameLabel.setIcon(UIUtilities.getImageIcon("import-include-redefine.png"));
        }*/
        tagNameLabel.setForeground(tagNameLabelColor);
        
        getEndSlash().setForeground(tagNameLabel.getForeground());
        initTagEditListener();
        
        
        
        Component hgap = Box.createHorizontalStrut(5);
        add(hgap);
        startTagPanelLayout.putConstraint(SpringLayout.WEST, hgap,
                getA()+INTER_PANEL_SPACE+8, SpringLayout.WEST, this);
        startTagPanelLayout.putConstraint(SpringLayout.NORTH, hgap,
                LABEL_HEAD_ROOM_SPACE, SpringLayout.NORTH, this);
        
        //setup the tagLabel
        add(tagNameLabel);
        startTagPanelLayout.putConstraint(SpringLayout.WEST, tagNameLabel,
                0, SpringLayout.EAST, hgap);
        startTagPanelLayout.putConstraint(SpringLayout.NORTH, tagNameLabel,
                LABEL_HEAD_ROOM_SPACE, SpringLayout.NORTH, this);
        
        hgap = Box.createHorizontalStrut(5);
        add(hgap);
        startTagPanelLayout.putConstraint(SpringLayout.WEST, hgap,
                0, SpringLayout.EAST, tagNameLabel);
        startTagPanelLayout.putConstraint(SpringLayout.NORTH, hgap,
                LABEL_HEAD_ROOM_SPACE, SpringLayout.NORTH, this);
        
        firstRowLastComp = hgap;
        //addElementPropertiesPanel();
        
        updateTagName();
        addNonAttributeComponents();
        updateAttributes();
        addSelectionListener();
    }
    
    Component lastNonAtribComponent;
    Component lastAtribComponent;
    
    
    
    protected void initTagEditListener(){
        
        tagNameLabel.addCtrlClickHandler(new InplaceEditableLabel.CtrlClickHandler(){
            public void handleCtrlClick() {
                getNBNode().showSuperDefinition();
            }
        });
        
        tagNameLabel.setInputValidator(new InputValidator(){
            public boolean isStringValid(String input) {
                return org.netbeans.modules.xml.xam.dom.Utils.isValidNCName(input);
            }
        }, NbBundle.getMessage(StartTagPanel.class, "MSG_NOT_A_NCNAME"));
        tagNameLabel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(tagNameLabel.PROPERTY_MODE_CHANGE)){
                    if(evt.getNewValue() == InplaceEditableLabel.Mode.EDIT){
                        //user selected edit give the editor JComponent
                        //show a text field
                        final JTextField field = new JTextField(getElementPanel().getAXIContainer().getName());
                        field.select(0, field.getText().length());
                        field.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e) {
                                String newName = field.getText();
                                if(getElementPanel().getAXIContainer().getName().equals(newName))
                                    return;
                                //do validation
                                if(org.netbeans.modules.xml.xam.dom.Utils.isValidNCName(newName)){
                                    field.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                                    try{
                                        if(firstTimeRename)
                                            getNBNode().setNameInModel(newName);
                                        else
                                            setTagNameInModel(newName);
                                        firstTimeRename = false;
                                    }finally{
                                        field.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                    }
                                }
                            }
                        });
                        if(getElementPanel().getAXIContainer().isShared()){
                            String str = NbBundle.getMessage(StartTagPanel.class, "MSG_SHARED_ELEMENT_EDIT");
                            tagNameLabel.setEditInfoText(str, context);
                        }
                        if(!getElementPanel().getAXIContainer().isReadOnly()){
                            if(getElementPanel().getAXIContainer() instanceof AnyElement){
                                String str = NbBundle.getMessage(StartTagPanel.class, "MSG_ANY_ELEMENT_EDIT");
                                tagNameLabel.setEditInfoText(str, context);
                            }else{
                                tagNameLabel.setInlineEditorComponent(field);
                            }
                        }else{
                            String str = NbBundle.getMessage(StartTagPanel.class, "MSG_READONLY_ELEMENT_EDIT");
                            tagNameLabel.setEditInfoText(str, context);
                        }
                    }
                }
            }
        });
    }
    
    
    private void setTagNameInModel(String name){
        getNBNode().setName(name);
    }
    
    
    /**
     *
     *
     */
    public void setTagName(String value) {
        String tagName=value;
        Font font = tagNameLabel.getFont();
        //font = font.deriveFont((font.getStyle() | java.awt.Font.BOLD), font.getSize());
        font = font.deriveFont((font.getStyle() | java.awt.Font.PLAIN), font.getSize());
        tagNameLabel.setFont(font);
        tagNameLabel.setText(tagName);
    }
    
    
    /**
     *
     *
     */
    public void updateTagName() {
        setTagName(getElementPanel().getAXIContainer().getName());
    }
    
    
    
    private void showAttributeEditFor(Attribute attr){
        if(componentList != null){
            for(Component child: componentList){
                if(child instanceof AttributePanel){
                    final AttributePanel attrP = (AttributePanel) child;
                    if(attrP.getAttribute() == attr){
                        context.getComponentSelectionManager().setSelectedComponent(attrP);
                        SwingUtilities.invokeLater(new Runnable(){
                            public void run() {
                                UIUtilities.scrollViewTo(attrP, context);
                                attrP.showEditorForName(true);
                            }
                        });
                        return;
                    }
                }
            }
        }
    }
    
    
    /**
     *
     *
     */
    Component firstRowLastComp;
    public void updateAttributes() {
        updateAttributeCountLabel();
        //Remove the existing panels
        if(componentList != null){
            for (Component panel: componentList){
                //except firstTweener remove everything
                if(panel != firstTweener){
                    startTagPanelLayout.removeLayoutComponent(panel);
                    remove(panel);
                }
            }
        }
        
        componentList = new ArrayList<Component>();
        //firstTweener has to be added for the drop logic to work
        componentList.add(firstTweener);
        
        int attrCount = getElementPanel().getAXIContainer().getAttributes().size();
        if(attrCount > 0){
            if(!attributeCollapseButton.isVisible()){
                attributeCollapseButton.setVisible(true);
                attributeCountLabel.setVisible(true);
                //then may be user dropped an attr. So Default expand.
                if(context.isUserInducedEventMode())
                    attributeCollapseButton.setText("-");
            }
        }else{
            if(attributeCollapseButton.isVisible()){
                attributeCollapseButton.setVisible(false);
                attributeCountLabel.setVisible(false);
            }
        }
        
        lastAtribComponent = firstTweener;
        firstRowLastComp = null;
        if(attributesAreShown){
            addAttributes();
            if(firstRowLastComp == null){
                //there may be no attrs
                firstRowLastComp = getEndSlash();
            }
        }else{
            //makesure that the collapse button moves closer to tagNameLabel
            hgap.setPreferredSize(new Dimension(0,0));
            firstRowLastComp = getEndSlash();
        }
        
        
        addEndSlashLabel();
        addElementPropertiesPanel();
        revalidate();
        //repaint();
        getElementPanel().repaint();
    }
    
    static final String locHidden = NbBundle.getMessage(StartTagPanel.class, "LBL_HIDDEN");
    static final String locAttr = NbBundle.getMessage(StartTagPanel.class, "LBL_ATTRIBUTE");
    static final String locAttrs = NbBundle.getMessage(StartTagPanel.class, "LBL_ATTRIBUTES");
    
    protected void addAttributeCountLabel(){
        if(attributesAreShown)
            //dont show anything if the attributes are seen
            return;
        int count = getElementPanel().getAXIContainer().getAttributes().size();
        if(count > 0){
            String hidden = (attributesAreShown) ? "" : " "+locHidden;
            String attributeStr = "";
            if(!attributesAreShown)
                attributeStr = (count == 1) ? " "+locAttr : " "+locAttrs;
            
            String str = "["+count+attributeStr+hidden+"]";
            String countStr = "["+count+"]";
            attributeCountLabel.setText(countStr);
            attributeCountLabel.setToolTipText(str);
            add(attributeCountLabel);
            startTagPanelLayout.putConstraint(SpringLayout.WEST, attributeCountLabel,
                    0/*INTER_PANEL_SPACE*/, SpringLayout.EAST, lastAtribComponent);
            startTagPanelLayout.putConstraint(SpringLayout.NORTH, attributeCountLabel,
                    (getRowCount() - 1) * getTagHeight() + LABEL_HEAD_ROOM_SPACE, SpringLayout.NORTH, this);
            //componentList.add(attributeCountLabel);
            lastAtribComponent = attributeCountLabel;
        }
    }
    
    private void updateAttributeCountLabel(){
        int count = getElementPanel().getAXIContainer().getAttributes().size();
        if(count > 0){
            String hidden = (attributesAreShown) ? "" : " "+locHidden;
            String attributeStr = "";
            if(!attributesAreShown)
                attributeStr = (count == 1) ? " "+locAttr : " "+locAttrs;
            
            String str = "["+count+attributeStr+hidden+"]";
            String countStr = "["+count+"]";
            attributeCountLabel.setText(countStr);
            attributeCountLabel.setToolTipText(str);
        }else{
            attributeCountLabel.setText("");
            attributeCountLabel.setToolTipText("");
        }
    }
    
    protected void addEndSlashLabel() {
        add(getEndSlash());
        startTagPanelLayout.putConstraint(SpringLayout.WEST, getEndSlash(),
                INTER_PANEL_SPACE, SpringLayout.EAST, lastAtribComponent);
        startTagPanelLayout.putConstraint(SpringLayout.NORTH, getEndSlash(),
                getLastRowComponentsHeadRoom()+5 , SpringLayout.NORTH, this);
        componentList.add(getEndSlash());
        lastAtribComponent = getEndSlash();
    }
    
    private int getLastRowComponentsHeadRoom(){
        return (getRowCount() - 1) * (getTagHeight() - TAG_HEIGHT_ADJUSTMENT);
    }
    private void addElementPropertiesPanel() {
        if(elementPropertiesPanel != null)
            excludePaintComponentList.remove(elementPropertiesPanel);
        elementPropertiesPanel = getNewAXIContainerPropertiesPanel();
        add(elementPropertiesPanel);
        startTagPanelLayout.putConstraint(SpringLayout.WEST, elementPropertiesPanel,
                getA()+INTER_PANEL_SPACE+6, SpringLayout.EAST, getEndSlash());
        startTagPanelLayout.putConstraint(SpringLayout.NORTH, elementPropertiesPanel,
                getLastRowComponentsHeadRoom(), SpringLayout.NORTH, this);
        elementPropertiesPanel.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e) {
                StartTagPanel.this.dispatchEvent(e);
            }
            public void mouseReleased(MouseEvent e) {
                StartTagPanel.this.dispatchEvent(e);
            }
            public void mousePressed(MouseEvent e) {
                StartTagPanel.this.dispatchEvent(e);
            }
        });
        componentList.add(elementPropertiesPanel);
        excludePaintComponentList.add(elementPropertiesPanel);
    }
    
    
    protected void addNonAttributeComponents() {
        add(attributeCollapseButton);
        attributeCollapseButton.setVisible(false);
        startTagPanelLayout.putConstraint(SpringLayout.WEST, attributeCollapseButton,
                INTER_PANEL_SPACE, SpringLayout.EAST, firstRowLastComp);
        startTagPanelLayout.putConstraint(SpringLayout.NORTH, attributeCollapseButton,
                ATTR_HEAD_ROOM_SPACE+2, SpringLayout.NORTH, this);
        attributeCollapseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showOrHideAttrs();
            }
        });
        
        lastAtribComponent = attributeCollapseButton;
        addAttributeCountLabel();
        
        
        /*//add a hgap between attr collapse button and first tweener.
        add(hgap);
        startTagPanelLayout.putConstraint(SpringLayout.WEST, hgap,
                0, SpringLayout.EAST, lastAtribComponent);
        startTagPanelLayout.putConstraint(SpringLayout.NORTH, hgap,
                ATTR_HEAD_ROOM_SPACE, SpringLayout.NORTH, this);*/
        
        
        // Add tweener before first attribute..Always
        firstTweener = Box.createHorizontalStrut(0);//new TweenerPanel(SwingConstants.VERTICAL, context);
        //addTweenerListener(firstTweener);
        
        add(firstTweener);
        startTagPanelLayout.putConstraint(SpringLayout.WEST, firstTweener,
                INTER_PANEL_SPACE, SpringLayout.EAST, lastAtribComponent);
        startTagPanelLayout.putConstraint(SpringLayout.NORTH, firstTweener,
                ATTR_HEAD_ROOM_SPACE, SpringLayout.NORTH, this);
    }
    
    private void showOrHideAttrs(){
        if(attributesAreShown){
            hideAttributes();
        }else{
            showAttributes();
        }
        
    }
    
    public void showAttributes(){
        if(attributesAreShown)
            return;
        attributesAreShown = true;
        attributeCollapseButton.setText("-");
        attributeCountLabel.setVisible(false);
        updateAttributes();
        
    }
    
    public void hideAttributes(){
        if(!attributesAreShown)
            return;
        attributesAreShown = false;
        attributeCollapseButton.setText("+");
        attributeCountLabel.setVisible(true);
        updateAttributes();
    }
    int TAG_HEIGHT_ADJUSTMENT = 5;
    protected void addAttributes() {
        int rowCount = 0;
        int attrPosition = 0;
        Component tweener;
        int tagHeight = getTagHeight() - TAG_HEIGHT_ADJUSTMENT;
        List<AbstractAttribute> attributeList = getElementPanel().getAXIContainer().getAttributes();
        for (int i = 0; i< attributeList.size(); i++) {
            //calculate the position of this attr
            if(i < NO_OF_FIRST_ROW_ATTRS){
                //attr must be in the 1st row
                rowCount = 0;
                attrPosition = i % NO_OF_FIRST_ROW_ATTRS;
            } else{
                //these belong to other rows
                rowCount = (int) Math.ceil((double)(i+1 - NO_OF_FIRST_ROW_ATTRS) / (double)NO_OF_ATTRS_PER_ROW);
                attrPosition = (i+1 - NO_OF_FIRST_ROW_ATTRS) % (NO_OF_ATTRS_PER_ROW);
                attrPosition--;
                if(attrPosition == -1)
                    attrPosition = NO_OF_ATTRS_PER_ROW - 1;
            }
            
            AttributePanel panel=new AttributePanel(this,attributeList.get(i), context);
            add(panel);
            componentList.add(panel);
            if((attrPosition != 0) || (rowCount == 0)){
                startTagPanelLayout.putConstraint(SpringLayout.WEST, panel,
                        0/*INTER_PANEL_SPACE*/, SpringLayout.EAST, lastAtribComponent);
            }else{
                //this is first attr
                startTagPanelLayout.putConstraint(SpringLayout.WEST, panel,
                        firstAttrPos, SpringLayout.WEST, this);
            }
            startTagPanelLayout.putConstraint(SpringLayout.NORTH, panel,
                    rowCount * tagHeight+ATTR_HEAD_ROOM_SPACE + 1, SpringLayout.NORTH, this);
            
            
            tweener = Box.createHorizontalStrut(10);//new TweenerPanel(SwingConstants.VERTICAL, context);
            //addTweenerListener(tweener);
            
            add(tweener);
            componentList.add(tweener);
            startTagPanelLayout.putConstraint(SpringLayout.WEST, tweener,
                    0/*INTER_PANEL_SPACE*/, SpringLayout.EAST, panel);
            startTagPanelLayout.putConstraint(SpringLayout.NORTH, tweener,
                    rowCount * tagHeight+ATTR_HEAD_ROOM_SPACE, SpringLayout.NORTH, this);
            
            lastAtribComponent = tweener;
            if((i + 1) == NO_OF_FIRST_ROW_ATTRS && ((i+1) != attributeList.size())){
                //means first row last element and not last element
                firstRowLastComp = lastAtribComponent;
            }
        }
    }
    
    public int getRowCount(){
        int tmprowCount = 0;
        if(!attributesAreShown){
            return 1;
        }
        int atrCount = getElementPanel().getAXIContainer().getAttributes().size();
        if(atrCount < NO_OF_FIRST_ROW_ATTRS){
            return 1;
        }else{
            atrCount -= NO_OF_FIRST_ROW_ATTRS;
            return (int) Math.ceil((double) atrCount / (double) NO_OF_ATTRS_PER_ROW) + 1;
        }
    }
    
    private void addNewAttributeAt(TweenerPanel tweener){
        int index = componentList.indexOf(tweener);
        if(index == -1){
            //must not happen
            return;
        }
        index = index/2;
        AXIContainer element = getElementPanel().getAXIContainer();
        AXIModel model = element.getModel();
        model.startTransaction();
        try{
            Attribute attr = model.getComponentFactory().createAttribute();
            String str = UIUtilities.getUniqueName(
                    InstanceDesignConstants.NEW_ATTRIBUTE_NAME, element);
            attr.setName(str);
            element.addChildAtIndex(attr, index);
        }finally{
            model.endTransaction();
        }
    }
    
    private void addTweenerListener(final TweenerPanel tweener){
        tweener.addTweenerListener(new TweenerListener(){
            public boolean dragAccept(DnDHelper.PaletteItem paletteItem) {
                //accept only Attributes
                if(paletteItem != DnDHelper.PaletteItem.ATTRIBUTE){
                    String str = NbBundle.getMessage(StartTagPanel.class,
                            "MSG_ATTRIBUTE_TWEENER_DROP_REJECT");
                    UIUtilities.showErrorMessageFor(str, context, StartTagPanel.this);
                    return false;
                }
                String str = NbBundle.getMessage(StartTagPanel.class,
                        "MSG_ATTRIBUTE_TWEENER_DROP_ACCEPT",
                        getElementPanel().getAXIContainer().getName());
                UIUtilities.showBulbMessageFor(str, context, StartTagPanel.this);
                return true;
            }
            
            public void drop(DnDHelper.PaletteItem paletteItem) {
                UIUtilities.hideGlassMessage();
                addNewAttributeAt(tweener);
            }
            
            public void dragEntered(DnDHelper.PaletteItem paletteItem) {
            }
            
            public void dragExited() {
                UIUtilities.hideGlassMessage();
            }
        });
    }
    
    
    public int getInterComponentSpacing() {
        return INTER_PANEL_SPACE;
    }
    
    public void addElement(){
        if(context.isUserInducedEventMode()){
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    getElementPanel().setExpanded(true);
                }
            });
        }
        AXIContainer elm = getElementPanel().getAXIContainer();
        AXIModel model = elm.getModel();
        Compositor comp = elm.getCompositor();
        boolean addCompositorFirst =false;
        //add a compositor first
        //I explicitly start and end transaction to prevent 2 ElementPanels addition in the UI
        if(comp == null){
            comp = model.getComponentFactory().createSequence();
            addCompositorFirst = true;
        }
        Element nelm = model.getComponentFactory().createElement();
        String str = UIUtilities.getUniqueName(InstanceDesignConstants.NEW_ELEMENT_NAME, comp);
        nelm.setName(str);
        try{
            model.startTransaction();
            if(addCompositorFirst)
                elm.addCompositor(comp);
            if(comp.getParent() != null && comp.getModel() != null)
                comp.addElement(nelm);
        }finally{
            model.endTransaction();
        }
    }
    
    
    public void addCompositor(Compositor.CompositorType compType){
        AXIContainer elm = getElementPanel().getAXIContainer();
        AXIModel model = elm.getModel();
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
        addCompositor(comp);
    }
    
    
    public void addCompositor(DnDHelper.PaletteItem compType){
        AXIContainer elm = getElementPanel().getAXIContainer();
        AXIModel model = elm.getModel();
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
        addCompositor(comp);
    }
    
    public void addCompositor(Compositor comp){
        if(comp == null)
            return;
        AXIContainer elm = getElementPanel().getAXIContainer();
        AXIModel model = elm.getModel();
        
        model.startTransaction();
        try{
            elm.addCompositor(comp);
        }finally{
            model.endTransaction();
        }
    }
    
    public void addAttribute(){
        AXIContainer elm = getElementPanel().getAXIContainer();
        AXIModel model = elm.getModel();
        model.startTransaction();
        try{
            Attribute attr = model.getComponentFactory().createAttribute();
            String str = UIUtilities.getUniqueName(
                    InstanceDesignConstants.NEW_ATTRIBUTE_NAME, elm);
            attr.setName(str);
            elm.addAttribute(attr);
        }finally{
            model.endTransaction();
        }
    }
    
    
    public void drop(DropTargetDropEvent event) {
        //hide the bulb message before doing anything
        UIUtilities.hideGlassMessage();
        if(getElementPanel().getAXIContainer().isReadOnly()){
            event.rejectDrop();
            return;
        }
        setDragMode(false);
        context.setUserInducedEventMode(true, this);
        try{
            super.drop(event);
            if(DnDHelper.getDraggedPaletteItem(event) == DnDHelper.PaletteItem.ELEMENT){
                //append the new element
                addElement();
            }
            if(DnDHelper.isCompositor(DnDHelper.getDraggedPaletteItem(event))){
                if(getElementPanel().getAXIContainer().getCompositor() != null){
                    //compositor already added so reject this drop
                    event.rejectDrop();
                    return;
                }else{
                    //add a compositor;
                    addCompositor(DnDHelper.getDraggedPaletteItem(event));
                }
            }
            if(DnDHelper.getDraggedPaletteItem(event) == DnDHelper.PaletteItem.ATTRIBUTE){
                addAttribute();
            }
        }finally{
            context.setUserInducedEventMode(false);
        }
    }
    
    public void dragOver(DropTargetDragEvent event) {
        super.dragOver(event);
        if(getElementPanel().getAXIContainer().isReadOnly()){
            String str = NbBundle.getMessage(StartTagPanel.class,
                    "MSG_READONLY_COMPOSITOR_DROP", getElementPanel().getAXIContainer().getName());
            UIUtilities.showErrorMessageFor(str, context, this);
            event.rejectDrag();
            return;
        }
        DnDHelper.PaletteItem item = DnDHelper.getDraggedPaletteItem(event);
        String messageKey = "MSG_ELEMENT_DROP_ACCEPT";
        switch(item){
            case ELEMENT:
                if(getElementPanel().getAXIContainer() instanceof Element){
                    AXIType dt = ((Element)getElementPanel().getAXIContainer()).getType();
                    if(dt != null){
                        messageKey = (dt instanceof Datatype) ? "MSG_SIMPLE2COMPLEX_WARNING" :
                            messageKey;
                    }
                }
            case ATTRIBUTE:
                String[] options = new String[]{
                    item.toString().toLowerCase()
                    , getElementPanel().getAXIContainer().getName()
                };
                String str = NbBundle.getMessage(StartTagPanel.class,
                        messageKey, options );
                UIUtilities.showBulbMessageFor(str, context, this);
                return;
            case CHOICE:
            case SEQUENCE:
            case ALL:
                if(getElementPanel().getAXIContainer().getCompositor() != null){
                    options = new String[]{
                        getElementPanel().getAXIContainer().getCompositor().getType().getName()
                        , getElementPanel().getAXIContainer().getName()
                    };
                    //compositor already added so reject this drag
                    str = NbBundle.getMessage(StartTagPanel.class,
                            "MSG_ELEMENT_COMPOSITOR_DROP_REJECT", options );
                    UIUtilities.showErrorMessageFor(str, context, this);
                    event.rejectDrag();
                    return;
                }else{
                    options = new String[]{
                        item.toString().toLowerCase()
                        , getElementPanel().getAXIContainer().getName()
                    };
                    str = NbBundle.getMessage(StartTagPanel.class,
                            "MSG_ELEMENT_DROP_ACCEPT", options );
                    UIUtilities.showBulbMessageFor(str, context, this);
                }
                return;
            default:
                options = new String[]{
                    getElementPanel().getAXIContainer().getName()
                };
                str = NbBundle.getMessage(StartTagPanel.class,
                        "MSG_ELEMENT_DROP_REJECT", options );
                UIUtilities.showErrorMessageFor(str, context, this);
                event.rejectDrag();
                return;
        }
    }
    
    public void dragEnter(DropTargetDragEvent event) {
        super.dragEnter(event);
        setDragMode(true);
        dragOver(event);
    }
    
    public void dragExit(DropTargetEvent event) {
        super.dragExit(event);
        setDragMode(false);
        UIUtilities.hideGlassMessage();
    }
    
    private void setDragMode(boolean dragMode){
        if(dragMode){
            tagNameLabel.setForeground(Color.WHITE);
            expandButton.setDragMode(true);
            attributeCollapseButton.setDragMode(true);
        }else{
            tagNameLabel.setForeground(tagNameLabelColor);
            expandButton.setDragMode(false);
            attributeCollapseButton.setDragMode(false);
        }
        
    }
    
    public JLabel getEndSlash() {
        endSlash.setVisible(false);
        return endSlash;
    }
    
    
    public void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);
        
        chainAttributeGroups(g2d);
    }
    
    public JPanel getNewAXIContainerPropertiesPanel(){
        return new ElementPropertiesPanel((AbstractElement) getElementPanel().getAXIContainer(), context);
    }
    
    void showTagNameEditor(boolean firstTime) {
        this.firstTimeRename = firstTime;
        if(tagNameLabel != null)
            tagNameLabel.showEditor();
    }
    
    private void addSelectionListener() {
        addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(PROP_SELECTED)){
                    if(((Boolean)evt.getNewValue()).booleanValue()){
                        //set the tage name color to orange
                        //tagNameLabel.setForeground(InstanceDesignConstants.TAG_NAME_SELECTED_COLOR);
                        if(tagNameFont == null) {
                            Font font = tagNameLabel.getFont();
                            //font = new Font(font.getName(), Font.BOLD, font.getSize());
                            tagNameFont = font.deriveFont((font.getStyle() | java.awt.Font.PLAIN), font.getSize());
                            tagNameLabel.setFont(tagNameFont);
                        }
                        getElementPanel().repaint();
                    } else {
                        //set the tage name color to normal color
                        /*tagNameLabel.setForeground(InstanceDesignConstants.TAG_NAME_COLOR);
                        if(getElementPanel().getAXIContainer().isReadOnly(context.getAXIModel())){
                            tagNameLabel.setForeground(InstanceDesignConstants.TAG_NAME_READONLY_COLOR);
                        }else{
                            if(getElementPanel().getAXIContainer().getContentModel() != null){
                                //tagNameLabel.setForeground(InstanceDesignConstants.TAG_NAME_SELECTED_COLOR);
                            }
                        }*/
                        if(tagNameFont == null) {
                            Font font = tagNameLabel.getFont();
                            font = new Font(font.getName(), Font.PLAIN, font.getSize());
                            tagNameLabel.setFont(font);
                        }
                        getElementPanel().repaint();
                    }
                    getEndSlash().setForeground(tagNameLabel.getForeground());
                    getEndSlash().setFont(tagNameLabel.getFont());
                }
            }
        });
    }
    
    protected void chainAttributeGroups(Graphics2D g2d) {
        List<AttributePanel> attrPanels = new ArrayList<AttributePanel>();
        for(Component comp: componentList){
            if(comp instanceof AttributePanel)
                attrPanels.add((AttributePanel) comp);
        }
        int i = 0;
        while(i < attrPanels.size()){
            if(i+1 < attrPanels.size()){
                chainIfNeeded(attrPanels.get(i), attrPanels.get(i+1), g2d);
            }
            i++;
        }
    }
    
    public ABEBaseDropPanel getChildUIComponentFor(AXIComponent axiComponent) {
        if(!(axiComponent instanceof AbstractAttribute)){
            //i deal only with attributes. Rest all element panel should care
            return getElementPanel().getChildUIComponentFor(axiComponent);
        }
        if(componentList == null)
            return null;
        showAttributes();
        for(Component comp: componentList){
            if(comp instanceof AttributePanel){
                if( ((AttributePanel)comp).getUIComponentFor(axiComponent) != null){
                    return (AttributePanel) comp;
                }
            }
        }
        return null;
    }
    
    
    public List<AttributePanel> getAttributePanels(){
        if(componentList == null)
            return Collections.EMPTY_LIST;
        showAttributes();
        List<AttributePanel>  result = new ArrayList<AttributePanel>();
        for(Component comp: componentList){
            if(comp instanceof AttributePanel){
                result.add((AttributePanel) comp);
            }
        }
        return result;
    }
    
    private void chainIfNeeded(AttributePanel attr1, AttributePanel attr2, Graphics2D g2d) {
        final int L_H_BOX_W = 5;
        final int R_H_BOX_W = 10;
        final int chainWidth = AttributePanel.getAttributePanelHeight();
        
        AbstractAttribute at1 = (AbstractAttribute) attr1.getAXIComponent();
        AbstractAttribute at2 = (AbstractAttribute) attr2.getAXIComponent();
        
        if(at1.getContentModel() != at2.getContentModel())
            return;
        
        
        
        if(at1.getContentModel() == null){
            //both are local attr
            return;
        }
        
        if(!at1.isShared()){
            //not an attr grp.
            return;
        }
        
        Color fillColor = InstanceDesignConstants.ATTR_BG_SHARED_COLOR;
        
        if(at1.isReadOnly())
            fillColor = InstanceDesignConstants.ATTR_BG_READONLY_COLOR;
        
        Rectangle r1 = attr1.getBounds();
        Rectangle r2 = attr2.getBounds();
        
        Color oldColor = g2d.getColor();
        g2d.setColor(fillColor);
        
        if(r1.y == r2.y){
            //attrs are next to each other
            //draw a horizontal chain
            int x, y, width, height;
            x = r1.x + r1.width;
            y   = r1.y + (r1.height - chainWidth)/2;
            width = r2.x - x;
            height = chainWidth;
            //g2d.drawRect( x, y, width, height);
            g2d.fillRect( x, y, width, height);
        }else{
            //attrs are not in the same line
            
            //draw right broken attr tape
            int xfudge = 8;
            
            Point pt = new Point(r1.x+r1.width+xfudge, r1.y);
            List<Point> plist = UIUtilities.getBrokenTapePoints(pt, r1.y+r1.height-1,
                    5, 4, true);
            Polygon rightTape = new Polygon();
            for(Point tpt: plist){
                rightTape.addPoint(tpt.x, tpt.y);
            }
            rightTape.addPoint(r1.x+r1.width, r1.y+r1.height-1);
            rightTape.addPoint(r1.x+r1.width, r1.y);
            g2d.draw(rightTape);
            g2d.fill(rightTape);
            
            //draw left broken attr tape
            xfudge -= 3;
            pt = new Point(r2.x - xfudge, r2.y);
            plist = UIUtilities.getBrokenTapePoints(pt, r2.y+r2.height-1,
                    5, 4, false);
            Polygon leftTape = new Polygon();
            for(Point tpt: plist){
                leftTape.addPoint(tpt.x, tpt.y);
            }
            leftTape.addPoint(r2.x, r2.y+r2.height-1);
            leftTape.addPoint(r2.x, r2.y);
            g2d.draw(leftTape);
            g2d.fill(leftTape);
        }
        g2d.setColor(oldColor);
    }
    
    public void accept(UIVisitor visitor) {
        visitor.visit(this);
    }
    
    
////////////////////////////////////////////////////////////////////////////
// Instance members
////////////////////////////////////////////////////////////////////////////
    
    private InplaceEditableLabel tagNameLabel;
    
    List<Component> componentList;
    private Component firstTweener;
    
    private static final int ATTR_HEAD_ROOM_SPACE = 3;
    private static final int LABEL_HEAD_ROOM_SPACE = (TagPanel.getTagHeight() / 2) -9;
    private static final int INTER_PANEL_SPACE = 2;
    
    private final JLabel endSlash = new JLabel("");
    private JPanel elementPropertiesPanel;
    RoundExpandCollapseButton attributeCollapseButton = new RoundExpandCollapseButton("+", false);
    boolean attributesAreShown = false;
    JLabel attributeCountLabel = new JLabel();
    public static final int NO_OF_FIRST_ROW_ATTRS = 5;
    public static final int NO_OF_ATTRS_PER_ROW = 6;
    int firstAttrPos = getA() + INTER_PANEL_SPACE + 12;
    JLabel hgap = new JLabel();
}
