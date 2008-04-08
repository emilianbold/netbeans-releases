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

/*
 * NameSpacePanel.java
 *
 * Created on June 28, 2006, 4:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.schema.abe.nodes.ABEAbstractNode;
import org.netbeans.modules.xml.schema.abe.nodes.NamespaceNode;
import org.netbeans.modules.xml.schema.abe.palette.DnDHelper;
import org.openide.util.NbBundle;

/**
 *
 * @author girix
 */
public class NamespacePanel extends GradientShadePanel{
    protected static final long serialVersionUID = 7526472295622776147L;
    private InplaceEditableLabel namespaceLabel;
    /** Creates a new instance of NameSpacePanel */
    public NamespacePanel(InstanceUIContext context) {
        super(context);
        super.selectedTopGradientColor = Color.WHITE;
        super.selectedBottomGradientColor = Color.WHITE;
        initialize();
        initKeyListener();
        this.setFocusable(true);
        this.requestFocusInWindow();
        
    }
    
    
    private void initKeyListener(){
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
               if(context.getFocusTraversalManager().isFocusChangeEvent(e))
                    context.getFocusTraversalManager().handleEvent(e, NamespacePanel.this);
            }
            
        });
    }
    
    private void initialize(){
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(1,1,1,1));
        //setBorder(new javax.swing.border.LineBorder(java.awt.Color.lightGray, 1, true));
        
        String namespace = getNamespace();
        namespaceLabel = new InplaceEditableLabel(namespace);
        //Font font = namespaceLabel.getFont();
        //namespaceLabel.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
        namespaceLabel.setForeground(InstanceDesignConstants.NAMESPACE_COLOR);
        namespaceLabel.setWidthMagnificationFactor(1);
        initNamespaceEditListener();
        initNamespaceChangeListener();
        add(Box.createHorizontalStrut(TitleWrapperPanel.TITLE_BEGIN_FUDGE), BorderLayout.WEST);
        add(Box.createVerticalStrut(5), BorderLayout.NORTH);
        add(namespaceLabel, BorderLayout.CENTER);
        JPanel botP = new JPanel(new BorderLayout());
        botP.setOpaque(false);
        botP.add(Box.createVerticalStrut(5), BorderLayout.NORTH);
        JSeparator hline = new JSeparator();
        hline.setForeground(new Color(250, 232, 213));
        botP.add(hline, BorderLayout.CENTER);
        add(botP, BorderLayout.SOUTH);
        
        /*JLabel msgLabel = new JLabel("      "+
                NbBundle.getMessage(InstanceDesignerPanel.class, "MSG_DROP_HERE_FOR_ELEMENT"));
        msgLabel.setForeground(Color.gray);
        namespacePanel.add(msgLabel, BorderLayout.CENTER);*/
        //namespacePanel.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        
        addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(ABEBaseDropPanel.PROP_SELECTED)){
                    Font font = namespaceLabel.getFont();
                    if(((Boolean)evt.getNewValue()).booleanValue()){
                        setBorder(new LineBorder(InstanceDesignConstants.XP_ORANGE, 2));
                        namespaceLabel.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
                    }else{
                        setBorder(new EmptyBorder(1,1,1,1));
                        namespaceLabel.setFont(new Font(font.getName(), Font.PLAIN, font.getSize()));
                    }
                }
            }
        });
        
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                mouseClickedActionHandler(e, false);
            }
            public void mouseEntered(MouseEvent e) {
                mouseClickedActionHandler(e, true);
            }
            public void mouseReleased(MouseEvent e) {
                mouseClickedActionHandler(e, true);
            }
        });
        
        this.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NamespacePanel.class,"LBL_NAMESPACE_PANEL"));
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NamespacePanel.class,"HINT_NAMESPACE_PANEL"));
    }
    
    public void mouseClickedActionHandler(MouseEvent e, boolean handelPopupOnly){
        if(e.getClickCount() == 1){
            if(e.isPopupTrigger()){
                context.getMultiComponentActionManager().showPopupMenu(e, NamespacePanel.this);
                return;
            }
            if(handelPopupOnly)
                return;
            //the attr is selected
            if(e.isControlDown())
                context.getComponentSelectionManager().addToSelectedComponents(NamespacePanel.this);
            else
                context.getComponentSelectionManager().setSelectedComponent(NamespacePanel.this);
        }
    }
    
    public String getNamespace(){
        String namespace = context.getAXIModel().getRoot().getTargetNamespace();
        if(namespace == null)
            return NbBundle.getMessage(
                    NamespacePanel.class, "LBL_NO_NAMESPACE");
        else
            return namespace;
    }
    
    public void setNamespaceInModel(String nsStr){
        AXIModel model = context.getAXIModel();
        model.startTransaction();
        try{
            model.getRoot().setTargetNamespace(nsStr);
        }finally{
            model.endTransaction();
        }
    }
    
    protected void initNamespaceEditListener(){
        namespaceLabel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(namespaceLabel.PROPERTY_MODE_CHANGE)){
                    if(evt.getNewValue() == InplaceEditableLabel.Mode.EDIT){
                        //user selected edit give the editor JComponent
                        //show a text field
                        final JTextField field = new JTextField(getNamespace());
                        field.select(0, field.getText().length());
                        field.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e) {
                                String newName = field.getText();
                                try {
                                    new URI(newName);
                                } catch (URISyntaxException ex) {
                                    org.openide.ErrorManager.getDefault().notify(ex);
                                    return;
                                }
                                setNamespaceInModel(newName);
                                namespaceLabel.setText(getNamespace());
                            }
                        });
                        namespaceLabel.setInlineEditorComponent(field);
                    }
                }
            }
        });
    }
    
    
    
    
    public void addElement(){
        AXIDocument adoc = context.getAXIModel().getRoot();
        AXIModel model = context.getAXIModel();
        model.startTransaction();
        try{
            Element nelm = model.getComponentFactory().createElement();
            String str = UIUtilities.getUniqueName(
                    InstanceDesignConstants.NEW_ELEMENT_NAME, adoc);
            nelm.setName(str);
            adoc.addElement(nelm);
        }finally{
            context.setUserInducedEventMode(true, this);
            model.endTransaction();
        }
    }
    
    public void addComplexType(){
        AXIDocument adoc = context.getAXIModel().getRoot();
        AXIModel model = context.getAXIModel();
        model.startTransaction();
        try{
            ContentModel nelm = model.getComponentFactory().createComplexType();
            String str = UIUtilities.getUniqueName(
                    InstanceDesignConstants.NEW_COMPLEXTYPE_NAME, adoc);
            nelm.setName(str);
            adoc.addContentModel(nelm);
        }finally{
            context.setUserInducedEventMode(true, this);
            model.endTransaction();
        }
    }
    
    public void drop(DropTargetDropEvent event) {
        super.drop(event);
        DnDHelper.PaletteItem pi = DnDHelper.getDraggedPaletteItem(event);
        if( (pi != pi.ELEMENT) && (pi != pi.COMPLEXTYPE) )
            event.rejectDrop();
        else{
            context.setUserInducedEventMode(true, this);
            try{
                if(pi == pi.ELEMENT){
                    addElement();
                } else if(pi == pi.COMPLEXTYPE){
                    addComplexType();
                }
            }finally{
                context.setUserInducedEventMode(false);
            }
        }
    }
    
    public void dragExit(DropTargetEvent event) {
        super.dragExit(event);
    }
    
    public void dragOver(DropTargetDragEvent event) {
        super.dragOver(event);
        DnDHelper.PaletteItem pi = DnDHelper.getDraggedPaletteItem(event);
        if( (pi != pi.ELEMENT) && (pi != pi.COMPLEXTYPE) )
            event.rejectDrag();
    }
    
    public void dragEnter(DropTargetDragEvent event) {
        super.dragEnter(event);
        DnDHelper.PaletteItem pi = DnDHelper.getDraggedPaletteItem(event);
        if( (pi != pi.ELEMENT) && (pi != pi.COMPLEXTYPE) )
            event.rejectDrag();
    }
    
    NamespaceNode nsNode;
    public ABEAbstractNode getNBNode() {
        if(nsNode == null){
            nsNode = new NamespaceNode(context.getAXIModel().getRoot(), context);
        }
        return nsNode;
    }
    
    public AXIComponent getAXIComponent(){
        return context.getAXIModel().getRoot();
    }
    
    private void initNamespaceChangeListener() {
        context.getAXIModel().getRoot().addPropertyChangeListener(new ModelEventMediator(this, context.getAXIModel().getRoot()){
            public void _propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(AXIDocument.PROP_TARGET_NAMESPACE)){
                    namespaceLabel.setText(getNamespace());
                }
            }
        });
    }
    
    public void accept(UIVisitor visitor) {
        visitor.visit(this);
    }
    
    
}
