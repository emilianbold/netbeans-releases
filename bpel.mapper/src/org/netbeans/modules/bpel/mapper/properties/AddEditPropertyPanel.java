package org.netbeans.modules.bpel.mapper.properties;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.xml.catalog.XmlGlobalCatalog;
import org.netbeans.modules.bpel.core.helper.api.BusinessProcessHelper;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.soa.ui.schema.SchemaTreeInfoProvider;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModelFactory;
import org.netbeans.modules.xml.xpath.ext.schema.FindAllChildrenSchemaVisitor;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;

/**
 *
 * @author  anjeleevich
 */
public class AddEditPropertyPanel extends javax.swing.JPanel {

    private BpelModel bpelModel;
    private Message message;
    
    private DialogDescriptor dialogDescriptor = null;
    private Lookup lookup;
    private FindAllChildrenSchemaVisitor schemaSearcher =
            new FindAllChildrenSchemaVisitor(true, true, true);
    
    private NewNMPropertyNode newNMPropertyNode;
    private TypeChooserTreeModel typeChooserTreeModel;
    
    private JScrollPane typeChooserScrollPane;
    private JTree typeChooserTree;
    
    private Map<String, String> prefixesMap;
    private Map<String, String> createPrefixesMap 
            = new HashMap<String, String>();
    
    private List<SchemaComponent> schemaComponentsToImport 
            = new ArrayList<SchemaComponent>();

    /** Creates new form AddEditPropertyPanel */
    public AddEditPropertyPanel(BpelModel bpelModel, Message message, 
            Lookup lookup, Map<String, String> prefixesMap) {
        initComponents();

        this.prefixesMap = prefixesMap;
        
        this.bpelModel = bpelModel;
        this.lookup = lookup;
        this.message = message;

        typeChooserTreeModel = new TypeChooserTreeModel(message);

        typeChooserTree = new JTree(typeChooserTreeModel);
        typeChooserTree.setCellRenderer(new TypeChooserCellRenderer());
        typeChooserTree.setRootVisible(false);
        typeChooserTree.setShowsRootHandles(true);
        typeChooserScrollPane = new JScrollPane(typeChooserTree);

        propertyTypeChooser.add(typeChooserScrollPane, BorderLayout.CENTER);

        newNMPropertyCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateNewNMPropertyNode();
            }
        });
        
        propertyMessageTextField.setText(message.getName());
        
        propertyNameTextField.getDocument().addDocumentListener(
                new DocumentListener() 
        {
            public void insertUpdate(DocumentEvent e) {
                updateValide();
            }

            public void removeUpdate(DocumentEvent e) {
                updateValide();
            }

            public void changedUpdate(DocumentEvent e) {
                updateValide();
            }
        });

        newNMPropertyTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateNewNMPropertyNode();
            }

            public void removeUpdate(DocumentEvent e) {
                updateNewNMPropertyNode();
            }

            public void changedUpdate(DocumentEvent e) {
                updateNewNMPropertyNode();
            }
        });
        
        typeChooserTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                TreePath treePath = typeChooserTree.getSelectionPath();
                if (treePath != null) {
                    TypeChooserTreeNode node = (TypeChooserTreeNode) treePath
                            .getLastPathComponent();
                    SchemaComponent typeOrElement = node.getTypeOrElement();
                    String typeOrElementStringValue = "";
                    if (typeOrElement != null) {
                        String name = null;
                        
                        if (typeOrElement instanceof GlobalType) {
                            name = ((GlobalType) typeOrElement).getName();
                        }

                        if (typeOrElement instanceof GlobalElement) {
                            name = ((GlobalElement) typeOrElement).getName();
                        }
                        
                        String ns = null;
                        SchemaModel model = typeOrElement.getModel();
                        if (model != null) {
                            Schema schema = model.getSchema();
                            if (schema != null) {
                                ns = schema.getTargetNamespace();
                            }
                        }
                        
                        if (name != null && ns != null) {
                            name = name.trim();
                            ns = ns.trim();
                            
                            if (name.length() > 0 && ns.length() > 0) {
                                typeOrElementStringValue = "{" + ns 
                                        + "}" + name; // NOI18N
                            }
                        }
                    }
                    
                    propertyTypeTextField.setText(typeOrElementStringValue);
                }
                updateQuery();
                updateValide();
            }
        });
        
        syncCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateQuery();
            }
        });
    }
    
    private void clearCreatePrefixes() {
        createPrefixesMap.clear();
        schemaComponentsToImport.clear();
    }
    
    private String getPrefix(String uri) {
        if (uri == null) {
            return null;
        }
        
        uri = uri.trim();
        if (uri.length() == 0) {
            return null;
        }
                
        if (prefixesMap != null) {
            for (Map.Entry<String, String> entry : prefixesMap.entrySet()) {
                if (uri.equals(entry.getValue())) {
                    return entry.getKey();
                }
            }
        }
        
        for (Map.Entry<String, String> entry : createPrefixesMap.entrySet()) {
            if (uri.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        
        for (int i = 0;;i++) {
            String prefix = "ns" + i; // NOI18N
            if (!createPrefixesMap.containsKey(prefix)) {
                createPrefixesMap.put(prefix, uri);
                return prefix;
            }
        }
    }
    
    private String addPrefix(String name, SchemaComponent component) {
        String uri = component.getModel().getEffectiveNamespace(component);
        String prefix = getPrefix(uri);
        
        if (prefix != null && prefix.length() > 0) {
            return prefix + ":" + name;
        }
        
        return name;
    }
    
    public String getPropertyName() {
        String name = propertyNameTextField.getText().trim();
        return (name.length() > 0) ? name : null;
    }
    
    public GlobalType getPropertyType() {
        TreePath treePath = typeChooserTree.getSelectionPath();
        if (treePath == null) {
            return null;
        }
        
        TypeChooserTreeNode node = (TypeChooserTreeNode) treePath
                .getLastPathComponent();
        SchemaComponent component = node.getTypeOrElement();
        return (component instanceof GlobalType) 
                ? (GlobalType) component
                : null;
    }
    
    public GlobalElement getPropertyElemenet() {
        TreePath treePath = typeChooserTree.getSelectionPath();
        if (treePath == null) {
            return null;
        }
        
        TypeChooserTreeNode node = (TypeChooserTreeNode) treePath
                .getLastPathComponent();
        SchemaComponent component = node.getTypeOrElement();
        return (component instanceof GlobalElement) 
                ? (GlobalElement) component
                : null;
    }
    
    public boolean isAssociatePropertyWithMessage() {
        return associatePropertyWithMessage.isSelected();
    }
    
    public String getQuery() {
        String query = queryTextArea.getText().trim();
        return (query.length() == 0) ? null : query;
    }
    
    public Map<String, String> getCreatedPrefixes() {
        return new HashMap<String, String>(createPrefixesMap);
    }
    
    public List<SchemaComponent> getSchemaComponentsToImport() {
        List<SchemaComponent> result = new ArrayList<SchemaComponent>();
        result.addAll(schemaComponentsToImport);
        
        SchemaComponent type = getPropertyType();
        if (type != null) {
            result.add(type);
        } 
        
        SchemaComponent element = getPropertyElemenet();
        if (element != null) {
            result.add(element);
        }
        
        return result;
    }
    
    public String getNMProperty() {
        TreePath treePath = typeChooserTree.getSelectionPath();
        if (treePath == null) {
            return null;
        }
        
        Object[] path = treePath.getPath();
        
        for (int i = 0; i < path.length; i++) {
            TypeChooserTreeNode node = (TypeChooserTreeNode) path[i];
            if (node instanceof NMPropertyNode) {
                String nmProperty = ((NMPropertyNode) node).getNMProperty();
                if (nmProperty != null && nmProperty.length() > 0) {
                    return nmProperty;
                }
                return null;
            }
            
            if (node instanceof NewNMPropertyNode) {
                if (!newNMPropertyCheckbox.isSelected()) {
                    return null;
                }
                String nmProperty = ((NewNMPropertyNode) node).getUserObject();
                if (nmProperty != null && nmProperty.length() > 0) {
                    return nmProperty;
                }
                return null;
            }
        }
        
        return null;
    }
    
    private void updateValide() {
        if (dialogDescriptor != null) {
            boolean valid = (getPropertyName() != null)
                    && ((getPropertyElemenet() != null) 
                            || (getPropertyType() != null))
                    && (getNMProperty() != null);
            dialogDescriptor.setValid(valid);
        }
    }
    
    private void updateQuery() {
        if (!syncCheckBox.isSelected()) {
            return;
        }
        
        clearCreatePrefixes();
        
        StringBuilder builder = new StringBuilder();
        
        TreePath treePath = typeChooserTree.getSelectionPath();
        if (treePath != null) {
            TypeChooserTreeNode node = (TypeChooserTreeNode) treePath
                    .getLastPathComponent();
            SchemaComponent typeOrElement = node.getTypeOrElement();
            
            if (typeOrElement != null) {
                Object[] path = treePath.getPath();
                
                boolean firstQueryStep = true;
                
                NewNMPropertyNode newNMPNode = null;
                
                for (int i = 0; i < path.length; i++) {
                    TypeChooserTreeNode step = (TypeChooserTreeNode) path[i];
                    
                    if (step instanceof NewNMPropertyNode) {
                        newNMPNode = (NewNMPropertyNode) step;
                    }

                    if (step instanceof SchemaComponentNode 
                            && newNMPNode != null) 
                    {
                        newNMPNode = null;
                        continue;
                    }
                    
                    if (step instanceof SchemaComponentNode 
                            && !(step instanceof NMPropertyNode)) 
                    {
                        SchemaComponent component = (SchemaComponent) step
                                .getUserObject();
                        if (component instanceof AnyElement) {
                            continue;
                        }
                        
                        String name = null;
                        
                        if (component instanceof ElementReference) {
                            NamedComponentReference<GlobalElement> ref 
                                    = ((ElementReference) component).getRef();
                            GlobalElement element = (ref == null)
                                    ? null
                                    : ref.get();

                            if (element != null) {
                                schemaComponentsToImport.add(element);
                            
                                name = element.getName();
                                name = addPrefix(name, element);
                                if (name != null) {
                                    if (!firstQueryStep) {
                                        name = "/" + name;
                                    } else {
                                        firstQueryStep = false;
                                    }
                                }
                            }
                        } else if (component instanceof Element 
                                && component instanceof Named)
                        {
                            name = ((Named) component).getName();
                            if (name != null) {
                                schemaComponentsToImport.add(component);
                                
                                name = addPrefix(name, component);
                                if (!firstQueryStep) {
                                    name = "/" + name;
                                } else {
                                    firstQueryStep = false;
                                }
                            }
                        } else if (component instanceof Attribute
                                && component instanceof Named) 
                        {
                            name = ((Named) component).getName();
                            if (name != null) {
                                schemaComponentsToImport.add(component);
                                
                                name = addPrefix(name, component);
                                name = "@" + name;
                            }
                        }
                        
                        if (name != null) {
                            builder.append(name);
                        }
                    }
                }
            }
        }
        
        queryTextArea.setText(builder.toString());
    }

    public void setDialogDescriptor(DialogDescriptor dialogDescriptor) {
        this.dialogDescriptor = dialogDescriptor;
        updateValide();
    }

    private String getNewNMPropertyName() {
        return (!newNMPropertyCheckbox.isSelected()) ? "" // NOI18N
                : newNMPropertyTextField.getText().trim();
    }
    
    private NewNMPropertyNode getNewNMPropertyNode() {
        if (newNMPropertyNode == null) {
            newNMPropertyNode = new NewNMPropertyNode(getNewNMPropertyName());
        }
        return newNMPropertyNode;
    }
    
    private void updateNewNMPropertyNode() {
        String newNMPropertyName = getNewNMPropertyName();
        NewNMPropertyNode _newNMPropertyNode = getNewNMPropertyNode();
        RootNode rootNode = (RootNode) typeChooserTreeModel.getRoot();
        if (newNMPropertyName.length() == 0) {
            if (_newNMPropertyNode.getParent() != null) {
                int index = rootNode.getIndex(_newNMPropertyNode);
                rootNode.remove(index);
                typeChooserTreeModel.nodesWereRemoved(rootNode, new int[]{index},
                        new Object[]{_newNMPropertyNode});
                newNMPropertyNode.setUserObject(""); // NOI18N

            }
        } else {
            _newNMPropertyNode.setUserObject(newNMPropertyName);
            if (_newNMPropertyNode.getParent() != null) {
                typeChooserTreeModel.nodeChanged(_newNMPropertyNode);
            } else {
                int index = rootNode.getChildCount();
                rootNode.add(_newNMPropertyNode);
                typeChooserTreeModel.nodesWereInserted(rootNode, 
                        new int[]{index});
            }
        }
    }
    
    private class TypeChooserCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, 
                boolean sel, boolean expanded, boolean leaf, 
                int row, boolean hasFocus) 
        {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, 
                    leaf, row, hasFocus);
            
            TypeChooserTreeNode node = (TypeChooserTreeNode) value;
            Icon customIcon = node.getIcon();
            
            if (customIcon != null) {
                setIcon(customIcon);
            }
            
            if (node.getTypeOrElement() != null) {
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setFont(getFont().deriveFont(Font.PLAIN));
            }
            
            return this;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        propertyNameLabel = new javax.swing.JLabel();
        propertyNameTextField = new javax.swing.JTextField();
        propertyTypeLabel = new javax.swing.JLabel();
        propertyTypeTextField = new javax.swing.JTextField();
        mapPropertyToPanel = new javax.swing.JPanel();
        propertyTypeChooser = new javax.swing.JPanel();
        newNMPropertyCheckbox = new javax.swing.JCheckBox();
        newNMPropertyTextField = new javax.swing.JTextField();
        queryLabel = new javax.swing.JLabel();
        queryScrollPane = new javax.swing.JScrollPane();
        queryTextArea = new javax.swing.JTextArea();
        syncCheckBox = new javax.swing.JCheckBox();
        validationError = new javax.swing.JTextArea();
        associatePropertyWithMessage = new javax.swing.JCheckBox();
        propertyMessageTextField = new javax.swing.JTextField();

        propertyNameLabel.setLabelFor(propertyNameTextField);
        propertyNameLabel.setText(org.openide.util.NbBundle.getMessage(AddEditPropertyPanel.class, "AddEditPropertyPanel.propertyNameLabel.text")); // NOI18N

        propertyNameTextField.setText(org.openide.util.NbBundle.getMessage(AddEditPropertyPanel.class, "AddEditPropertyPanel.propertyNameTextField.text")); // NOI18N
        propertyNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                propertyNameTextFieldActionPerformed(evt);
            }
        });

        propertyTypeLabel.setLabelFor(propertyTypeTextField);
        propertyTypeLabel.setText(org.openide.util.NbBundle.getMessage(AddEditPropertyPanel.class, "AddEditPropertyPanel.propertyTypeLabel.text")); // NOI18N

        propertyTypeTextField.setEditable(false);
        propertyTypeTextField.setText(org.openide.util.NbBundle.getMessage(AddEditPropertyPanel.class, "AddEditPropertyPanel.propertyTypeTextField.text")); // NOI18N

        mapPropertyToPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(AddEditPropertyPanel.class, "AddEditPropertyPanel.mapPropertyToPanel.border.title"))); // NOI18N

        propertyTypeChooser.setLayout(new java.awt.BorderLayout());

        newNMPropertyCheckbox.setText(org.openide.util.NbBundle.getMessage(AddEditPropertyPanel.class, "AddEditPropertyPanel.newNMPropertyCheckbox.text")); // NOI18N

        newNMPropertyTextField.setText(org.openide.util.NbBundle.getMessage(AddEditPropertyPanel.class, "AddEditPropertyPanel.newNMPropertyTextField.text")); // NOI18N

        queryLabel.setText(org.openide.util.NbBundle.getMessage(AddEditPropertyPanel.class, "AddEditPropertyPanel.queryLabel.text")); // NOI18N

        queryTextArea.setColumns(10);
        queryTextArea.setRows(3);
        queryScrollPane.setViewportView(queryTextArea);

        syncCheckBox.setSelected(true);
        syncCheckBox.setText(org.openide.util.NbBundle.getMessage(AddEditPropertyPanel.class, "AddEditPropertyPanel.syncCheckBox.text")); // NOI18N
        syncCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout mapPropertyToPanelLayout = new org.jdesktop.layout.GroupLayout(mapPropertyToPanel);
        mapPropertyToPanel.setLayout(mapPropertyToPanelLayout);
        mapPropertyToPanelLayout.setHorizontalGroup(
            mapPropertyToPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mapPropertyToPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mapPropertyToPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(queryScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                    .add(propertyTypeChooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                    .add(mapPropertyToPanelLayout.createSequentialGroup()
                        .add(newNMPropertyCheckbox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(newNMPropertyTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))
                    .add(mapPropertyToPanelLayout.createSequentialGroup()
                        .add(queryLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 266, Short.MAX_VALUE)
                        .add(syncCheckBox)))
                .addContainerGap())
        );
        mapPropertyToPanelLayout.setVerticalGroup(
            mapPropertyToPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, mapPropertyToPanelLayout.createSequentialGroup()
                .add(propertyTypeChooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(mapPropertyToPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(newNMPropertyCheckbox)
                    .add(newNMPropertyTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mapPropertyToPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(queryLabel)
                    .add(syncCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(queryScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        validationError.setColumns(20);
        validationError.setRows(2);
        validationError.setOpaque(false);

        associatePropertyWithMessage.setSelected(true);
        associatePropertyWithMessage.setText(org.openide.util.NbBundle.getMessage(AddEditPropertyPanel.class, "AddEditPropertyPanel.associatePropertyWithMessage.text")); // NOI18N
        associatePropertyWithMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                associatePropertyWithMessageActionPerformed(evt);
            }
        });

        propertyMessageTextField.setEditable(false);
        propertyMessageTextField.setText(org.openide.util.NbBundle.getMessage(AddEditPropertyPanel.class, "AddEditPropertyPanel.propertyMessageTextField.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, mapPropertyToPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(associatePropertyWithMessage)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(propertyMessageTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(propertyNameLabel)
                            .add(propertyTypeLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(propertyTypeTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                            .add(propertyNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, validationError, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(propertyNameLabel)
                    .add(propertyNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(propertyTypeLabel)
                    .add(propertyTypeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(associatePropertyWithMessage)
                    .add(propertyMessageTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mapPropertyToPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(validationError, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 36, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void syncCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncCheckBoxActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_syncCheckBoxActionPerformed

private void propertyNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_propertyNameTextFieldActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_propertyNameTextFieldActionPerformed

private void associatePropertyWithMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_associatePropertyWithMessageActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_associatePropertyWithMessageActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox associatePropertyWithMessage;
    private javax.swing.JPanel mapPropertyToPanel;
    private javax.swing.JCheckBox newNMPropertyCheckbox;
    private javax.swing.JTextField newNMPropertyTextField;
    private javax.swing.JTextField propertyMessageTextField;
    private javax.swing.JLabel propertyNameLabel;
    private javax.swing.JTextField propertyNameTextField;
    private javax.swing.JPanel propertyTypeChooser;
    private javax.swing.JLabel propertyTypeLabel;
    private javax.swing.JTextField propertyTypeTextField;
    private javax.swing.JLabel queryLabel;
    private javax.swing.JScrollPane queryScrollPane;
    private javax.swing.JTextArea queryTextArea;
    private javax.swing.JCheckBox syncCheckBox;
    private javax.swing.JTextArea validationError;
    // End of variables declaration//GEN-END:variables

    private class TypeChooserTreeModel extends DefaultTreeModel {
        public TypeChooserTreeModel(Message message) {
            super(new RootNode(message));
        }
    }

    private abstract class TypeChooserTreeNode extends DefaultMutableTreeNode {
        private boolean childrenLoaded = false;
        
        protected CachedValue<String> displayName;
        protected CachedValue<Icon> icon;
        protected CachedValue<SchemaComponent> typeOrElement;
        
        TypeChooserTreeNode(Object userObject) {
            super(userObject);
        }
        
        protected abstract void loadChildren();
        protected abstract String loadDisplayName();
        protected abstract Icon loadIcon();
        
        protected SchemaComponent loadTypeOrElement() {
            return null;
        }

        @Override
        public TypeChooserTreeNode getParent() {
            return (TypeChooserTreeNode) super.getParent();
        }
        
        public final Icon getIcon() {
            if (icon == null) {
                icon = new CachedValue<Icon>(loadIcon());
            }
            return icon.getCachedValue();
        }
        
        public String getDisplayName() {
            if (displayName == null) {
                displayName = new CachedValue<String>(loadDisplayName());
            }
            return displayName.getCachedValue();
        }
        
        public final SchemaComponent getTypeOrElement() {
            if (typeOrElement == null) {
                typeOrElement = new CachedValue<SchemaComponent>(
                        loadTypeOrElement());
            }
            return typeOrElement.getCachedValue();
        }
        
        @Override
        public int getChildCount() {
            if (!childrenLoaded) {
                childrenLoaded = true;
                loadChildren();
            }
            
            return super.getChildCount();
        }
        
        @Override
        public String toString() {
            String _displayName = getDisplayName();
            return (_displayName != null) ? _displayName : super.toString();
        }
    }
    
    private static class CachedValue<T> {
        private T cachedValue;
        
        public CachedValue(T cachedValue) {
            this.cachedValue = cachedValue;
        }
        
        public T getCachedValue() {
            return cachedValue;
        }
    }
    
    
    private class SchemaNode extends TypeChooserTreeNode {
        public SchemaNode(SchemaModel schemaModel) {
            super(schemaModel);
        }

        @Override
        public SchemaModel getUserObject() {
            return (SchemaModel) super.getUserObject();
        }

        @Override
        protected void loadChildren() {
            SchemaComponent schemaComponent = null;
            
            TypeChooserTreeNode _parent = getParent();
            if (_parent instanceof BpelGlobalCatalogNode) {
                TypeChooserTreeNode parentParent = _parent.getParent();
                if (parentParent instanceof SchemaComponentNode) {
                    schemaComponent = ((SchemaComponentNode) parentParent)
                            .getUserObject();
                }
            } else if (_parent instanceof SchemaComponentNode) {
                schemaComponent = ((SchemaComponentNode) _parent)
                        .getUserObject();
            }
            
            SchemaModel schemaModel = getUserObject();
            if (schemaComponent instanceof AnyElement) {
                Schema schema = schemaModel.getSchema();
                if (schema != null) {
                    Collection<GlobalElement> elements = schema.getElements();
                    if (elements != null) {
                        for (GlobalElement element : elements) {
                            add(new SchemaComponentNode(element));
                        }
                    }
                }
            } else {
                add(new SchemaGlobalComplexTypesNode(schemaModel));
                add(new SchemaGlobalSimpleTypesNode(schemaModel));
                add(new SchemaGlobalElementsNode(schemaModel));
            }
        }

        @Override
        protected Icon loadIcon() {
            return null;
        }

        @Override
        protected String loadDisplayName() {
            FileObject fo = getUserObject().getModelSource().getLookup()
                    .lookup(FileObject.class);
            if (fo != null && fo.isValid()) {
                return fo.getName() + "." + fo.getExt();
            } else {
                Schema schema = getUserObject().getSchema();
                return (schema == null) ? null : schema.getTargetNamespace();
            }
        }
        
        @Override
        public boolean isLeaf() {
            return Boolean.FALSE;
        }
    }
    
    private class SchemaGlobalComplexTypesNode extends TypeChooserTreeNode {
        public SchemaGlobalComplexTypesNode(SchemaModel schemaModel) {
            super(schemaModel);
        }
        
        @Override
        public SchemaModel getUserObject() {
            return (SchemaModel) super.getUserObject();
        }

        @Override
        protected void loadChildren() {
            Schema schema = getUserObject().getSchema();
            if (schema != null) {
                Collection<GlobalComplexType> globalTypes = schema
                        .getComplexTypes();
                if (globalTypes != null) {
                    for (GlobalComplexType complexType : globalTypes) {
                        add(new SchemaComponentNode(complexType));
                    }
                }
            }
        }

        @Override
        protected String loadDisplayName() {
            return "Global Complex Types";
        }

        @Override
        protected Icon loadIcon() {
            return null;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }
    }
    
    private class SchemaGlobalSimpleTypesNode extends TypeChooserTreeNode {
        public SchemaGlobalSimpleTypesNode(SchemaModel schemaModel) {
            super(schemaModel);
        }
        
        @Override
        public SchemaModel getUserObject() {
            return (SchemaModel) super.getUserObject();
        }

        @Override
        protected void loadChildren() {
            Schema schema = getUserObject().getSchema();
            if (schema != null) {
                Collection<GlobalSimpleType> simpleTypes 
                        = schema.getSimpleTypes();
                if (simpleTypes != null) {
                    for (GlobalSimpleType simpleType : simpleTypes) {
                        add(new SchemaComponentNode(simpleType));
                    }
                }
            }
        }

        @Override
        protected String loadDisplayName() {
            return "Global Simple Types";
        }

        @Override
        protected Icon loadIcon() {
            return null;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }
    }
    
    private class CastAnyElementNode extends SchemaGlobalElementsNode {
        public CastAnyElementNode(SchemaModel schemaModel) {
            super(schemaModel);
        }
        
        @Override
        protected String loadDisplayName() {
            FileObject fo = getUserObject().getModelSource().getLookup()
                    .lookup(FileObject.class);
            if (fo != null && fo.isValid()) {
                return fo.getName() + "." + fo.getExt();
            } else {
                Schema schema = getUserObject().getSchema();
                return (schema == null) ? null : schema.getTargetNamespace();
            }
        }
    }

    private class SchemaGlobalElementsNode extends TypeChooserTreeNode {
        public SchemaGlobalElementsNode(SchemaModel schemaModel) {
            super(schemaModel);
        }
        
        @Override
        public SchemaModel getUserObject() {
            return (SchemaModel) super.getUserObject();
        }

        @Override
        protected void loadChildren() {
            Schema schema = getUserObject().getSchema();
            if (schema != null) {
                Collection<GlobalElement> globalElements 
                        = schema.getElements();
                if (globalElements != null) {
                    for (GlobalElement element : globalElements) {
                        add(new SchemaComponentNode(element));
                    }
                }
            }
        }

        @Override
        protected String loadDisplayName() {
            return "Global Elements";
        }

        @Override
        protected Icon loadIcon() {
            return null;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }
    }
    
    private class SchemaComponentNode extends TypeChooserTreeNode {
        public SchemaComponentNode(SchemaComponent schemaComponent) {
            super(schemaComponent);
        }
        
        @Override
        public SchemaComponent getUserObject() {
            return (SchemaComponent) super.getUserObject();
        }

        @Override
        protected void loadChildren() {
            if (getUserObject() instanceof AnyElement) {
                loadTypeChooserChildren(this);
            } else {
                schemaSearcher.lookForSubcomponents(getUserObject());
                List<SchemaComponent> schemaChildren = schemaSearcher.getFound();
                if (schemaChildren != null) {
                    for (SchemaComponent child : schemaChildren) {
                        add(new SchemaComponentNode(child));
                    }
                }
            }
        }

        @Override
        protected String loadDisplayName() {
            return SchemaTreeInfoProvider.getInstance()
                    .getDisplayByDataObj(getUserObject());
        }
        
        @Override
        protected Icon loadIcon() {
            return SchemaTreeInfoProvider.getInstance()
                    .getIconByDataObj(getUserObject());
        }

        @Override
        protected SchemaComponent loadTypeOrElement() {
            SchemaComponent schemaComponent = getUserObject();
            if (schemaComponent instanceof Attribute) {
                return SchemaTreeInfoProvider.getGlobalType(schemaComponent);
            }
            
            if (schemaComponent instanceof GlobalElement) {
                TypeChooserTreeNode _parent = getParent();
                
                boolean anyElementChild = false;
                boolean newNMPropertyChild = false;
                
                while (_parent != null) {
                    if (_parent instanceof NewNMPropertyNode) {
                        newNMPropertyChild = true;
                    }
                    if (_parent.getUserObject() instanceof AnyElement) {
                        anyElementChild = true;
                    }
                    _parent = _parent.getParent();
                }
                
                if (newNMPropertyChild && !anyElementChild) {
                    return schemaComponent;
                }
            }

            if (schemaComponent instanceof ElementReference) {
                NamedComponentReference<GlobalElement> elementRef 
                        = ((ElementReference) schemaComponent).getRef();
                if (elementRef != null) {
                    return elementRef.get();
                }
            }
            
            if (schemaComponent instanceof Element) {
                GlobalType type = SchemaTreeInfoProvider
                        .getGlobalType(schemaComponent);
                if (type != null) {
                    return type;
                }
            }
            
            if (schemaComponent instanceof GlobalType) {
                TypeChooserTreeNode _parent = getParent();
                while (_parent != null) {
                    if (_parent instanceof NewNMPropertyNode) {
                        return schemaComponent;
                    }
                    _parent = _parent.getParent();
                }
            }
            
            return null;
        }
        
        @Override
        public boolean isLeaf() {
            SchemaComponent c = getUserObject();

            if (c instanceof GlobalType) {
                if ("anyType".equals(((GlobalType) c).getName())) {
                    return Boolean.FALSE;
                }
            }
            
            if (c instanceof Attribute) {
                return Boolean.TRUE;
            }
            
            if (c instanceof AnyAttribute) {
                return Boolean.TRUE;
            }
            
            if (c instanceof AnyElement) {
                return Boolean.FALSE;
            }
            
            if (c instanceof SimpleType) {
                return Boolean.TRUE;
            }
            
//            if (c instanceof Element) {
//            }
            
            return Boolean.FALSE;
        }
    }
    
    private class NMPropertiesFolderNode extends TypeChooserTreeNode 
            implements PropertiesConstants 
    {
        public NMPropertiesFolderNode(FileObject fileObject) {
            super(fileObject);
        }
        
        @Override
        public FileObject getUserObject() {
            return (FileObject) super.getUserObject();
        }

        @Override
        protected void loadChildren() {
            FileObject[] _children = getUserObject().getChildren();
            if (_children != null) {
                for (FileObject child : _children) {
                    addFileObjectNode(this, child);
                }
            }
        }

        @Override
        protected String loadDisplayName() {
            Object displayNameAttr = getUserObject()
                    .getAttribute(DISPLAY_NAME_ATTR);
            
            if (displayNameAttr instanceof String) {
                return (String) displayNameAttr;
            }
            
            return getUserObject().getName();
        }

        @Override
        protected Icon loadIcon() {
            Object urlAttr = getUserObject().getAttribute(ICON_ATTR);
            if (urlAttr instanceof URL) {
                return new ImageIcon((URL) urlAttr);
            }
            return null;
        }
    }
    
    private class NMPropertyNode extends SchemaComponentNode implements 
            PropertiesConstants
    {
        private FileObject fileObject;
        
        public NMPropertyNode(FileObject fileObject, 
                SchemaComponent schemaComponent) 
        {
            super(schemaComponent);
            this.fileObject = fileObject;
        }

        public FileObject getFileObject() {
            return fileObject;
        }

        @Override
        protected void loadChildren() {
            SchemaComponent schemaComponent = getUserObject();
            
            if (schemaComponent instanceof GlobalType) {
                if ("anyType".equals(((GlobalType) schemaComponent).getName())) { // NOI18N
                    loadTypeChooserChildren(this);
                    return;
                }
            }
            
            super.loadChildren();
        }
        
        @Override
        protected String loadDisplayName() {
            Object displayNameAttr = getFileObject()
                    .getAttribute(DISPLAY_NAME_ATTR);
            
            if (displayNameAttr instanceof String) {
                String displayNameValue = ((String) displayNameAttr).trim();
                if (displayNameValue.length() > 0) {
                    return displayNameValue;
                }
            }
            
            Object nmProperty = getFileObject()
                    .getAttribute(NM_PROPERTY_ATTR);
            if (nmProperty instanceof String) {
                return ((String) nmProperty).trim();
            }
            
            return super.loadDisplayName();
        }

        @Override
        protected Icon loadIcon() {
            Object iconURLAttr = getFileObject().getAttribute(ICON_ATTR);
            if (iconURLAttr instanceof URL) {
                return new ImageIcon((URL) iconURLAttr);
            }
            return PropertiesConstants.FO_PROPERTY_ICON;
        }

        @Override
        protected SchemaComponent loadTypeOrElement() {
            return getUserObject();
        }
        
        public String getNMProperty() {
            Object nmPropertyAttr = getFileObject()
                    .getAttribute(NM_PROPERTY_ATTR);
            if (nmPropertyAttr instanceof String) {
                return ((String) nmPropertyAttr).trim();
            }
            return null;
        }
    }
    
    private class NewNMPropertyNode extends TypeChooserTreeNode {
        public NewNMPropertyNode() {
            super(null);
        }
        
        public NewNMPropertyNode(String userObject) {
            super(userObject);
        }
        
        @Override
        public String getUserObject() {
            return (String) super.getUserObject();
        }

        @Override
        protected void loadChildren() {
            add(new BpelGlobalCatalogNode());
            add(new BuildInTypesNode());
            
            BusinessProcessHelper helper = lookup.lookup(BusinessProcessHelper.class);
            if (helper != null) {
                Collection<FileObject> fileObjectCollection = helper.getSchemaFilesInProject();
                if (fileObjectCollection != null) {
                    for (FileObject fileObject : fileObjectCollection) {
                        ModelSource modelSource = Utilities
                                .getModelSource(fileObject, true);
                        if (modelSource != null) {
                            SchemaModel schemaModel = SchemaModelFactory
                                    .getDefault().getModel(modelSource);
                            if (schemaModel != null) {
                                 add(new SchemaNode(schemaModel));
                            }
                        }
                    }
                }
            }
        }

        @Override
        protected String loadDisplayName() { return null; }

        @Override
        public String getDisplayName() {
            return getUserObject();
        }
        
        @Override
        protected Icon loadIcon() {
            return PropertiesConstants.NM_PROPERTY_ICON;
        }
    }

    private class RootNode extends TypeChooserTreeNode {
        public RootNode(Message message) {
            super(message);
        }

        @Override
        protected void loadChildren() {
            FileSystem fileSystem = Repository.getDefault()
                    .getDefaultFileSystem();
            FileObject nmPropertiesFileObject = fileSystem.findResource(
                    PropertiesNode.ROOT_FOLDER);

            if (nmPropertiesFileObject != null) {
                FileObject[] fileObjectChildren = nmPropertiesFileObject.getChildren();
                if (fileObjectChildren != null) {
                    for (FileObject fileObject : fileObjectChildren) {
                        addFileObjectNode(this, fileObject);
                    }
                }
            }

            NewNMPropertyNode newNMPropertyNode = getNewNMPropertyNode();
            if (newNMPropertyNode.getUserObject() != null
                    && newNMPropertyNode.getUserObject().length() > 0) 
            {
                add(newNMPropertyNode);
            }
        }

        @Override
        protected String loadDisplayName() { return null; }

        @Override
        public String getDisplayName() {
            return "Root"; // NOI18N
        }
        
        @Override
        protected Icon loadIcon() {
            return null;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }
    }
    
    
    private class BuildInTypesNode extends SchemaNode {
        public BuildInTypesNode() {
            super(SchemaModelFactory.getDefault().getPrimitiveTypesModel());
        }

        @Override
        protected void loadChildren() {
            Schema schema = getUserObject().getSchema();
            if (schema == null) {
                return;
            }
            
            Collection<GlobalSimpleType> simpleTypes = schema.getSimpleTypes();
            if (simpleTypes != null) {
                for (GlobalSimpleType simpleType : simpleTypes) {
                    add(new SchemaComponentNode(simpleType));
                }
            }
        }
        
        @Override
        protected String loadDisplayName() {
            return "Build In Types";
        }
    }
    
    private class BpelGlobalCatalogNode extends TypeChooserTreeNode {
        public BpelGlobalCatalogNode() {
            super("BPEL Global Catalog");
        }

        @Override
        protected void loadChildren() {
            Iterator<String> iterator = XmlGlobalCatalog.getBpelGlobalCatalog().getPublicIDs();

            if (iterator != null) {
                while (iterator.hasNext()) {
                    String pubId = iterator.next();
                    if (pubId == null) continue;

                    String sysID = XmlGlobalCatalog.getBpelGlobalCatalog().getSystemID(pubId);
                    if (sysID == null) continue;
                    
                    ModelSource modelSource = null;
                    try {
                        modelSource = CatalogModelFactory.getDefault().getCatalogModel(
                            bpelModel.getModelSource()).getModelSource(new URI(sysID));
                    } catch (URISyntaxException ex) {/*ignore exceptoion*/
                    } catch (CatalogModelException ex) {/*ignore exceptoion*/}
                    
                    if (modelSource != null 
                            && pubId.startsWith(XmlGlobalCatalog.SCHEMA)) 
                    {
                        SchemaModel schemaModel = SchemaModelFactory.getDefault().
                                getModel(modelSource);
                        if (schemaModel != null) {
                            add(new SchemaNode(schemaModel));
                        }
                    }
                }
            }
        }

        @Override
        protected String loadDisplayName() {
            return "BPEL Global Catalog";
        }

        @Override
        protected Icon loadIcon() {
            return null;
        }
    }
    
    private void loadTypeChooserChildren(TypeChooserTreeNode node) {
        node.add(new BpelGlobalCatalogNode());
        
        boolean anyElement = (node.getUserObject() instanceof AnyElement);
        boolean anyType = ((node.getUserObject() instanceof GlobalType)
                && "anyType".equals(((GlobalType) node.getUserObject()).getName())); // NOI18N
        
        if (!(anyElement)) {
            node.add(new BuildInTypesNode());
        }

        BusinessProcessHelper helper = lookup.lookup(BusinessProcessHelper.class);
        
        if (helper != null) {
            Collection<FileObject> fileObjectCollection = helper
                    .getSchemaFilesInProject();
            if (fileObjectCollection != null) {
                for (FileObject fileObject : fileObjectCollection) {
                    ModelSource modelSource = Utilities
                            .getModelSource(fileObject, true);
                    if (modelSource != null) {
                        SchemaModel schemaModel = SchemaModelFactory
                                .getDefault().getModel(modelSource);
                        if (schemaModel != null) {
                             node.add(new SchemaNode(schemaModel));
                        }
                    }
                }
            }
        }
    }
    
    private void addFileObjectNode(TypeChooserTreeNode parent, FileObject fileObject) {
        if (fileObject.isFolder()) {
            parent.add(new NMPropertiesFolderNode(fileObject));
        }
        
        Object nmPropertyAttr = fileObject.getAttribute(PropertiesConstants
                .NM_PROPERTY_ATTR);
        
        if (!(nmPropertyAttr instanceof String)) {
            return;
        }
        
        String nmProperty = ((String) nmPropertyAttr).trim();
        
        if (nmProperty.length() == 0) {
            return;
        } 
        
        Object elementAttr = fileObject.getAttribute("element"); // NOI18N
        Object typeAttr = fileObject.getAttribute("type"); // NOI18N
        
        String elementAttrValue = (elementAttr instanceof String) 
                ? ((String) elementAttr).trim()
                : ""; // NOI18N
        
        String typeAttrValue = (typeAttr instanceof String) 
                ? ((String) typeAttr).trim()
                : ""; // NOI18N
                
        SchemaComponent schemaComponent = null;
        
        if (elementAttrValue.length() > 0) {
            int i1 = elementAttrValue.indexOf("{"); // NOI18N
            int i2 = elementAttrValue.indexOf("}"); // NOI18N
            
            if (i1 == 0 && i2 > 1 && i2 + 1 < elementAttrValue.length()) {
                String ns = elementAttrValue.substring(1, i2);
                String name = elementAttrValue.substring(i2 + 1);
                
                FileSystem fileSystem = Repository.getDefault().getDefaultFileSystem();
                FileObject bpelXmlCatalog = fileSystem.findResource(
                        "bpel-xml-catalog"); // NOI18N
                
                SchemaModel schemaModel = null;

                if (bpelXmlCatalog != null) {
                    FileObject[] fileObjectChildren = bpelXmlCatalog
                            .getChildren();
                    if (fileObjectChildren != null) {
                        for (FileObject fo : fileObjectChildren) {
                            Object schemaIdAttr = fo.getAttribute("schema-id"); // NOI18N
                            if (schemaIdAttr instanceof String 
                                    && ns.equals(schemaIdAttr)) 
                            {
                                ModelSource modelSource = Utilities
                                        .getModelSource(fo, true);
                                if (modelSource != null) {
                                    schemaModel = SchemaModelFactory
                                            .getDefault()
                                            .getModel(modelSource);
                                    break;
                                }
                            }
                        }
                    }
                }

                if (schemaModel != null) {
                    schemaComponent = schemaModel
                            .findByNameAndType(name, GlobalElement.class);
                }
            } 
        } else if (typeAttrValue.length() > 0) {
            int i1 = typeAttrValue.indexOf("{");
            int i2 = typeAttrValue.indexOf("}");
            if (i1 == 0 && i2 > 1 && i2 + 1 < typeAttrValue.length()) {
                String ns = typeAttrValue.substring(1, i2);
                String name = typeAttrValue.substring(i2 + 1);

                FileSystem fileSystem = Repository.getDefault().getDefaultFileSystem();
                FileObject bpelXmlCatalog = fileSystem.findResource(
                        "bpel-xml-catalog");

                SchemaModel schemaModel = null;

                if (bpelXmlCatalog != null) {
                    FileObject[] fileObjectChildren = bpelXmlCatalog
                            .getChildren();
                    if (fileObjectChildren != null) {
                        for (FileObject fo : fileObjectChildren) {
                            Object schemaIdAttr = fo.getAttribute("schema-id");
                            if (schemaIdAttr instanceof String 
                                    && ns.equals(schemaIdAttr)) 
                            {
                                ModelSource modelSource = Utilities
                                        .getModelSource(fo, true);
                                if (modelSource != null) {
                                    schemaModel = SchemaModelFactory
                                            .getDefault()
                                            .getModel(modelSource);
                                    break;
                                }
                            }
                        }
                    }
                }

                if (schemaModel == null) {
                    SchemaModel primitiveTypesModel = SchemaModelFactory
                            .getDefault().getPrimitiveTypesModel();
                    if (ns.equals(primitiveTypesModel.getSchema()
                            .getTargetNamespace())) 
                    {
                        schemaModel = primitiveTypesModel;
                    }
                }

                if (schemaModel != null) {
                    schemaComponent = schemaModel.findByNameAndType(name, 
                            GlobalType.class);
                }
            }
        }

        if (schemaComponent != null) {
            parent.add(new NMPropertyNode(fileObject, schemaComponent));
        } 
    }
}
