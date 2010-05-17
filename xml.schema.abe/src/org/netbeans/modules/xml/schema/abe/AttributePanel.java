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

package org.netbeans.modules.xml.schema.abe;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIContainer;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AnyAttribute;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.schema.abe.nodes.ABEAbstractNode;
import org.netbeans.modules.xml.schema.abe.nodes.AttributeNode;
import org.netbeans.modules.xml.schema.abe.palette.DnDHelper;
import org.openide.util.NbBundle;

/*import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.AdvancedLocalAttributeCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;*/

/**
 *
 *
 * @author Todd Fast, todd.fast@sun.com
 */
public class AttributePanel extends ABEBaseDropPanel {
    private static final long serialVersionUID = 7526472295622776147L;
    private static final Border normalBorder = new
            EmptyBorder(2,2,2,2);//LineBorder(Color.WHITE/*InstanceDesignConstants.TAG_OUTLINE_COLOR*/, 1);
    private static final Border selectedBorder =
            new LineBorder(InstanceDesignConstants.XP_ORANGE, 2);
    /**
     * Default constructor
     *
     */
    public AttributePanel(StartTagPanel elementPanel, AbstractAttribute attribute
            , final InstanceUIContext context) {
        super(context);
        this.startTagPanel=elementPanel;
        this.attribute=attribute;
        initialize();
        attribute.addPropertyChangeListener(new ModelEventMediator(this, attribute) {
            public void _propertyChange(PropertyChangeEvent evt) {
                String str = evt.getPropertyName();
                if(str.equals(Attribute.PROP_NAME) || str.equals(Attribute.PROP_TYPE))
                    attributePropertyChangeAction(evt);
            }
        });
        initKeyListener();
        addSelectionListener();
        makeNBNode();
        //installFocusManager();
    }
    
    protected void initMouseListener(){
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                mouseClickedActionHandler(e, true);
            }
            
            public void mousePressed(MouseEvent e) {
                mouseClickedActionHandler(e, true);
            }
            
            public void mouseClicked(MouseEvent e) {
                mouseClickedActionHandler(e, false);
            }
            
        });
    }
    
    /**
     *
     *
     */
    Color attrColor = InstanceDesignConstants.ATTRIBUTE_COLOR;
    Color attrBGColor = InstanceDesignConstants.NO_BACKGROUND_COLOR;
    private void initialize() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 1, HEAD_ROOM_SPACE));
        AbstractAttribute attr = getAttribute();
        boolean shared = attr.isShared();/*attr.getContentModel() == null ? false :
            ((attr.getContentModel().getType() == ContentModel.ContentModelType.ATTRIBUTE_GROUP) ?
                true : false);*/
        boolean readOnly = attr.isReadOnly();
        String valueTTP = NbBundle.getMessage(AttributePanel.class, "TTP_ATTR_LABEL_VALUE");
        String nameTTP = NbBundle.getMessage(AttributePanel.class, "TTP_ATTR_LABEL_NAME");
        String attrTTP = NbBundle.getMessage(AttributePanel.class, "TTP_ATTR");
        setOpaque(false);
        setBorder(normalBorder);
        if(shared || attr.isGlobal()){
            attrBGColor = InstanceDesignConstants.ATTR_BG_SHARED_COLOR;
            setBackground(attrBGColor);
            setOpaque(true);
            attrColor = InstanceDesignConstants.ATTRIBUTE_COLOR;
            if(attr.isGlobal()){
                valueTTP = NbBundle.getMessage(AttributePanel.class, "TTP_ATTR_LABEL_VALUE_GLOBAL");
                nameTTP = NbBundle.getMessage(AttributePanel.class, "TTP_ATTR_LABEL_NAME_GLOBAL");
                attrTTP = NbBundle.getMessage(AttributePanel.class, "TTP_ATTR_GLOBAL");
            }else{
                if(attr.getContentModel() != null){
                    String name = attr.getContentModel().getName();
                    String typeStr = attr.getContentModel().getType().equals(ContentModel.ContentModelType.ATTRIBUTE_GROUP) ?
                        "LBL_GLOBAL_ATTRIBUTE_GROUP" : "LBL_GLOBAL_COMPLEX_TYPE";
                    
                    typeStr = NbBundle.getMessage(AttributePanel.class, typeStr);
                    
                    valueTTP = NbBundle.getMessage(AttributePanel.class,
                            "TTP_ATTR_LABEL_VALUE_SHARED", typeStr, name);
                    nameTTP = NbBundle.getMessage(AttributePanel.class,
                            "TTP_ATTR_LABEL_NAME_SHARED", typeStr, name);
                    attrTTP = NbBundle.getMessage(AttributePanel.class,
                            "TTP_ATTR_SHARED", typeStr, name);
                }else{
                    String elmName = attr.getParentElement().getName();
                    valueTTP = NbBundle.getMessage(AttributePanel.class,
                            "TTP_ATTR_LABEL_VALUE_SHARED_NO_CM", elmName);
                    nameTTP = NbBundle.getMessage(AttributePanel.class,
                            "TTP_ATTR_LABEL_NAME_SHARED_NO_CM", elmName);
                    attrTTP = NbBundle.getMessage(AttributePanel.class,
                            "TTP_ATTR_SHARED_NO_CM", elmName);
                }
            }
        }
        if(readOnly){
            attrBGColor = InstanceDesignConstants.ATTR_BG_READONLY_COLOR;
            setBackground(attrBGColor);
            setOpaque(true);
            attrColor = InstanceDesignConstants.TAG_NAME_READONLY_COLOR;
            valueTTP = NbBundle.getMessage(AttributePanel.class, "TTP_ATTR_LABEL_VALUE_READONLY");
            nameTTP = NbBundle.getMessage(AttributePanel.class, "TTP_ATTR_LABEL_NAME_READONLY");
            attrTTP = NbBundle.getMessage(AttributePanel.class, "TTP_ATTR_READONLY");
        }
        
        setToolTipText(attrTTP);
        
        attributeNameLabel=new InplaceEditableLabel(attr.getName());
        attributeNameLabel.setForeground(attrColor);
        attributeNameLabel.setToolTipText(nameTTP);
        add(attributeNameLabel);
        
        
        
        equals = new JLabel(" = ");
        equals.setForeground(attrColor);
        equals.setToolTipText(attrTTP);
        add(equals);
        //transmit mouse click events on eq sign to the attr panel
        equals.addMouseListener(new MouseAdapter(){
            public void mouseReleased(MouseEvent e) {
                AttributePanel.this.dispatchEvent(e);
            }
            
            public void mousePressed(MouseEvent e) {
                AttributePanel.this.dispatchEvent(e);
            }
            
            public void mouseClicked(MouseEvent e) {
                AttributePanel.this.dispatchEvent(e);
            }
            
        });
        
        
        
        String value="?";
        if(attr instanceof Attribute) {
            Attribute a = (Attribute)attr;
            if ( a.getType()!=null)
                value=a.getType().getName();
        }
        
        attributeValueLabel=new InplaceEditableLabel(value);
        attributeValueLabel.setForeground(attrColor);
        attributeValueLabel.setToolTipText(valueTTP);
        add(attributeValueLabel);
        
        
        
        refreshAttributeParameters();
        initAttributeEditListeners();
        initMouseListener();
    }
    
    protected void initKeyListener(){
        addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                //name edit
                if( e.getKeyCode() == e.VK_F2 ){
                    attributeNameLabel.showEditor();
                }
                if(context.getFocusTraversalManager().isFocusChangeEvent(e))
                    context.getFocusTraversalManager().handleEvent(e, AttributePanel.this);
            }
            public void keyReleased(KeyEvent e) {
            }
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == e.VK_SPACE){
                    attributeNameLabel.showEditor();
                }
            }
        });
    }
    
    protected void mouseClickedActionHandler(MouseEvent e, boolean handelPopupOnly) {
        if(e.getClickCount() == 1){
            if(e.isPopupTrigger()){
                context.getMultiComponentActionManager().showPopupMenu(e, this);
                return;
            }
            if(handelPopupOnly)
                return;
            //the attr is selected
            if(e.isControlDown())
                context.getComponentSelectionManager().addToSelectedComponents(this);
            else
                context.getComponentSelectionManager().setSelectedComponent(this);
        }
    }
    
    
    /**
     *
     *
     */
    public AbstractAttribute getAttribute() {
        return attribute;
    }
    
    public void refreshAttributeParameters(){
        attributeNameLabel.setText(getAttribute().toString());
        String value="?";
        AbstractAttribute attr = getAttribute();
        if(attr instanceof Attribute) {
            Attribute a = (Attribute)attr;
            if ( a.getType()!=null)
                value=a.getType().getName();
        }
        
        attributeValueLabel.setText(value);
    }
    
    
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, getAttributePanelHeight());
    }
    
    private void initAttributeEditListeners(){
        initAttributeNameEditListener();
        initAttributeValueEditListener();
    }
    
    
    protected void initAttributeNameEditListener(){
        
        attributeNameLabel.addCtrlClickHandler(new InplaceEditableLabel.CtrlClickHandler(){
            public void handleCtrlClick() {
                getNBNode().showSuperDefinition();
            }
        });
        
        attributeNameLabel.setInputValidator(new InputValidator(){
            public boolean isStringValid(String input) {
                return org.netbeans.modules.xml.xam.dom.Utils.isValidNCName(input);
            }
        }, NbBundle.getMessage(AttributePanel.class, "MSG_NOT_A_NCNAME"));
        attributeNameLabel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(InplaceEditableLabel.PROPERTY_MODE_CHANGE)){
                    if(evt.getNewValue() == InplaceEditableLabel.Mode.EDIT){
                        //user selected edit give the editor JComponent
                        //show a text field
                        final JTextField field = new JTextField(getAttribute().toString());
                        field.select(0, field.getText().length());
                        field.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e) {
                                String newName = field.getText();
                                //do validation
                                if(org.netbeans.modules.xml.xam.dom.Utils.isValidNCName(newName)){
                                    field.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                                    try{
                                        if(firstTimeRename)
                                            getNBNode().setNameInModel(newName);
                                        else
                                            setAttrNameInModel(newName);
                                        firstTimeRename = false;
                                    }finally{
                                        field.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                    }
                                }
                            }
                        });
                        
                        if(attribute.isShared() || attribute.isGlobal()){
                            String str = NbBundle.getMessage(StartTagPanel.class, "MSG_SHARED_ATTRIBUTE_EDIT");
                            attributeNameLabel.setEditInfoText(str, context);
                        }
                        if(!attribute.isReadOnly()){
                            if(attribute instanceof AnyAttribute){
                                String str = NbBundle.getMessage(StartTagPanel.class, "MSG_ANY_ATTRIBUTE_EDIT");
                                attributeNameLabel.setEditInfoText(str, context);
                            }else{
                                //attributeNameLabel.setWidthMagnificationFactor(1);
                                attributeNameLabel.setInlineEditorComponent(field);
                            }
                        }else{
                            String str = NbBundle.getMessage(StartTagPanel.class, "MSG_READONLY_ATTRIBUTE_EDIT");
                            attributeNameLabel.setEditInfoText(str, context);
                        }
                    }
                }
            }
        });
    }
    
    protected void initAttributeValueEditListener(){
        attributeValueLabel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(InplaceEditableLabel.PROPERTY_MODE_CHANGE)){
                    if(evt.getNewValue() == InplaceEditableLabel.Mode.EDIT){
                        attributeValueLabel.hideEditor();
                    }
                }
            }
        });
    }
    
    private void setAttrNameInModel(String name){
        getNBNode().setName(name);
    }
    
    private void setAttrValueInModel(String name){
        //TODO
    }
    
    public void removeAttribute(){
        AXIContainer elm = startTagPanel.getElementPanel().getAXIContainer();
        AXIModel model = elm.getModel();
        if(model != null){
            model.startTransaction();
            try{
                elm.removeAttribute(attribute);
            }finally{
                model.endTransaction();
            }
        }
    }
    
    private void attributePropertyChangeAction(PropertyChangeEvent evt){
        refreshAttributeParameters();
    }
    
    
    private void addAttributeToAttrGrp() {
        /*if(getAttribute().getContentModel().getType() ==
                ContentModel.ContentModelType.ATTRIBUTE_GROUP){*/
        ContentModel cm = getAttribute().getContentModel();
        AXIModel model = context.getAXIModel();
        model.startTransaction();
        try{
            Attribute attr = model.getComponentFactory().createAttribute();
            String str = UIUtilities.getUniqueName(
                    InstanceDesignConstants.NEW_ATTRIBUTE_NAME, cm);
            attr.setName(str);
            cm.addAttribute(attr);
        }finally{
            model.endTransaction();
        }
        //}
    }
    
    public void drop(DropTargetDropEvent event) {
        removeDragFeedback();
        ContentModel cm = getAttribute().getContentModel();
        if((cm != null) && (cm.getType() ==
                ContentModel.ContentModelType.ATTRIBUTE_GROUP)){
            addAttributeToAttrGrp();
        }else{
            event.rejectDrop();
        }
    }
    
    public void dragOver(DropTargetDragEvent event) {
        DnDHelper.PaletteItem item = DnDHelper.getDraggedPaletteItem(event);
        if(item != DnDHelper.PaletteItem.ATTRIBUTE){
            event.rejectDrag();
            return;
        }
        ContentModel cm = getAttribute().getContentModel();
        if((cm != null) && (cm.getType() ==
                ContentModel.ContentModelType.ATTRIBUTE_GROUP)){
            return;
        }
        event.rejectDrag();
    }
    
    public void dragEnter(DropTargetDragEvent event) {
        DnDHelper.PaletteItem item = DnDHelper.getDraggedPaletteItem(event);
        if(item != DnDHelper.PaletteItem.ATTRIBUTE){
            String str = NbBundle.getMessage(AttributePanel.class,
                    "MSG_ATTRIBUTE_PANEL_DROP_REJECT_WRONG_COMPONENT", this.attribute.getName());
            UIUtilities.showErrorMessageFor(str, context, this);
            event.rejectDrag();
        }
        
        setDragFeedback();
        ContentModel cm = getAttribute().getContentModel();
        if((cm != null) && (cm.getType() ==
                ContentModel.ContentModelType.ATTRIBUTE_GROUP)){
            String str = NbBundle.getMessage(AttributePanel.class,
                    "MSG_ATTRIBUTE_PANEL_DROP_ACCEPT", getAttribute().getContentModel().getName());
            UIUtilities.showBulbMessageFor(str, context, this);
            return;
        }
        String str = NbBundle.getMessage(AttributePanel.class,
                "MSG_ATTRIBUTE_PANEL_DROP_REJECT", this.attribute.getName());
        UIUtilities.showErrorMessageFor(str, context, this);
        event.rejectDrag();
    }
    
    public void dragExit(DropTargetEvent event) {
        removeDragFeedback();
    }
    
    
    public void setDragFeedback(){
        setBackground(InstanceDesignConstants.DARK_BLUE);
        setOpaque(true);
        attributeNameLabel.setForeground(Color.WHITE);
        attributeValueLabel.setForeground(Color.WHITE);
        equals.setForeground(Color.WHITE);
        setBorder(new LineBorder(InstanceDesignConstants.DARK_BLUE, 2));
        revalidate();
    }
    
    public void removeDragFeedback(){
        UIUtilities.hideGlassMessage();
        if(attrBGColor == InstanceDesignConstants.NO_BACKGROUND_COLOR)
            setOpaque(false);
        else
            setBackground(attrBGColor);
        attributeNameLabel.setForeground(attrColor);
        attributeValueLabel.setForeground(attrColor);
        equals.setForeground(attrColor);
        setBorder(normalBorder);
        revalidate();
    }
    
    void showEditorForName(boolean firstTimeRename) {
        this.firstTimeRename = firstTimeRename;
        attributeNameLabel.showEditor();
    }
    
    public static int getAttributePanelHeight() {
        return ATTR_HEIGHT;
    }
    
    protected void makeNBNode() {
        attributeNode = new AttributeNode(attribute, context);
        if(attribute.isReadOnly())
            attributeNode.setReadOnly(true);
    }
    
    public ABEAbstractNode getNBNode() {
        return attributeNode;
    }
    
    public AXIComponent getAXIComponent(){
        return attribute;
    }
    
    private void addSelectionListener() {
        addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(PROP_SELECTED)){
                    if(((Boolean)evt.getNewValue()).booleanValue()){
                        /*Font font = attributeNameLabel.getFont();
                        font = new Font(font.getName(), Font.BOLD, font.getSize());
                        attributeNameLabel.setFont(font);
                        attributeValueLabel.setFont(font);
                        equals.setFont(font);*/
                        
                        attributeNameLabel.setForeground(Color.BLACK);
                        attributeValueLabel.setForeground(Color.BLACK);
                        equals.setForeground(Color.BLACK);
                        setBorder(selectedBorder);
                        revalidate();
                    }else{
                        attributeNameLabel.setForeground(attrColor);
                        attributeValueLabel.setForeground(attrColor);
                        equals.setForeground(attrColor);
                        setBorder(normalBorder);
                        revalidate();
                    }
                    
                }
            }
        });
    }
    
    public void accept(UIVisitor visitor) {
        visitor.visit(this);
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Class members
    ////////////////////////////////////////////////////////////////////////////
    
    private static final int ATTR_HEIGHT = TagPanel.getTagHeight() - 10;
    private static final int INTER_LABEL_SPACE = 5;
    public static final int HEAD_ROOM_SPACE = (getAttributePanelHeight()/2) - 11;
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance members
    ////////////////////////////////////////////////////////////////////////////
    
    private JLabel equals;
    private StartTagPanel startTagPanel;
    private AbstractAttribute attribute;
    private InplaceEditableLabel attributeNameLabel;
    private InplaceEditableLabel attributeValueLabel;
    private AttributeNode attributeNode;
    
}
