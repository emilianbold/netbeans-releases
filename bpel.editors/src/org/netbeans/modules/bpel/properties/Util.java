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
package org.netbeans.modules.bpel.properties;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.modules.bpel.properties.Constants.StandardImportType;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import java.util.StringTokenizer;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JRootPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.soa.ui.nodes.NodesTreeParams;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.awt.MouseUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.Visualizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.netbeans.modules.soa.ui.SoaUtil;

/**
 *
 * @author Vitaly Bychkov
 * @author nk160297
 * @version 8 Febrary 2006
 */
public class Util {
    
    public static final String FORWARD_SLASH = "/"; //NOI18N
    public static final String UP_REL_FOLDER = "../"; //NOI18N
    public static final String CUR_REL_FOLDER = "./"; //NOI18N
    // too light - very difficult to see
    //    private static String GRAY_COLOR = "!controlShadow";
    
    private Util() {
    }
    
    public static FileObject getRelativeFO(FileObject startPoint
            , String relLocation) {
        if (startPoint == null || relLocation == null) {
            return null;
        }
        
        relLocation = ResolverUtility.decodeLocation(relLocation);
        
        if (!startPoint.isFolder()) {
            startPoint = startPoint.getParent();
        }
        
        if (relLocation.startsWith(UP_REL_FOLDER)) {
            int upRelLength = UP_REL_FOLDER.length();
            while (relLocation.startsWith(UP_REL_FOLDER)) {
                startPoint = startPoint.getParent();
                relLocation = relLocation.substring(upRelLength);
            }
            
        } else if (relLocation.startsWith(CUR_REL_FOLDER)) {
            relLocation = relLocation.substring(CUR_REL_FOLDER.length());
        }
        return startPoint.getFileObject(relLocation);
    }
    
    public static String getRelativePath(FileObject fromFo, FileObject toFo) {
        String relativePath = FileUtil.getRelativePath(fromFo, toFo);
        if (relativePath != null) {
            return relativePath;
        }
        
        if (!fromFo.isFolder()) {
            fromFo = fromFo.getParent();
        }
        
        StringTokenizer fromPath = new StringTokenizer(fromFo.getPath()
                , FORWARD_SLASH);
        StringTokenizer toPath = new StringTokenizer(toFo.getPath()
                , FORWARD_SLASH);
        String tmpFromFolder = null;
        String tmpToFolder = null;
        while (fromPath.hasMoreTokens()) {
            tmpFromFolder = fromPath.nextToken();
            tmpToFolder = toPath.hasMoreTokens() ? toPath.nextToken() : null;
            if (!(tmpFromFolder.equals(tmpToFolder))) {
                break;
            }
        }
        if (tmpToFolder == null) {
            return null;
        }
        
        StringBuffer fromRelativePathPart = new StringBuffer(UP_REL_FOLDER);
        while (fromPath.hasMoreTokens()) {
            fromPath.nextToken();
            fromRelativePathPart.append(UP_REL_FOLDER);
        }
        
        StringBuffer toRelativePathPart = new StringBuffer(tmpToFolder);
        while (toPath.hasMoreTokens()) {
            toRelativePathPart.append(FORWARD_SLASH).append(toPath.nextToken());
        }
        
        return fromRelativePathPart.append(toRelativePathPart).toString();
    }
    
    /**
     * Checks the characters of the given String to determine if they
     * syntactically match the production of an NCName as defined
     * by the W3C XML Namespaces recommendation
     * @param str the String to check
     * @return true if the given String follows the Syntax of an NCName
     **/
    public static boolean isNCName(String str) {
        
        if ((str == null) || (str.length() == 0)) return false;
        
        
        char[] chars = str.toCharArray();
        
        char ch = chars[0];
        
        //-- make sure String starts with a letter or '_'
        if ((!Character.isLetter(ch)) && (ch != '_'))
            return false;
        
        for (int i = 1; i < chars.length; i++) {
            if (!isNCNameChar(chars[i])) return false;
        }
        return true;
    } //-- isNCName
    
    /**
     * Checks the the given character to determine if it is
     * a valid NCNameChar as defined by the W3C XML
     * Namespaces recommendation
     * @param ch the char to check
     * @return true if the given char is an NCNameChar
     **/
    public static boolean isNCNameChar(char ch) {
        if (Character.isLetter(ch) || Character.isDigit(ch)) {
            return true;
        }
        if (isExtender(ch)) {
            return true;
        }
        switch(ch) {
        case '.':
        case '-':
        case '_':
            return true;
        default:
            return false;
        }
    } //-- isNCNameChar
    
    /**
     * Returns true if the given character is a valid XML Extender
     * character, according to the XML 1.0 specification
     * @param ch the character to check
     * @return true if the character is a valid XML Extender character
     **/
    public static boolean isExtender(char ch) {
        
        if ((ch >= 0x3031) && (ch <= 0x3035)) return true;
        if ((ch >= 0x30FC) && (ch <= 0x30FE)) return true;
        
        switch(ch) {
        case 0x00B7:
        case 0x02D0:
        case 0x02D1:
        case 0x0387:
        case 0x0640:
        case 0x0E46:
        case 0x0EC6:
        case 0x3005:
        case 0x309D:
        case 0x309E:
            return true;
        default:
            break;
        }
        return false;
    } //-- isExtender
    
    public static String getNewModelLocation(Model mainModel, FileObject newModelFo){
        Lookup lookup = mainModel.getModelSource().getLookup();
        if (lookup != null){
            FileObject mainModelFo = SoaUtil.getFileObjectByModel(mainModel);
            return getRelativePath(mainModelFo.getParent(), newModelFo);
        }
        return null;
    }
    
    public static String getNewModelNamespace(FileObject newModelFo, StandardImportType importType){
        ModelSource modelSource =
                Utilities.getModelSource(newModelFo, true);
        
        if (modelSource == null) {
            return null;
        }
        switch (importType) {
            // TODO
        case IMPORT_SCHEMA: {
            SchemaModel model = SchemaModelFactory.getDefault()
                    .getModel(modelSource);
            if (model == null) break;
            if (model.getState() != Model.State.NOT_WELL_FORMED) {
                return model.getSchema().getTargetNamespace();
            }
        }
        break;
        
        case IMPORT_WSDL: {
            WSDLModel model = WSDLModelFactory.getDefault()
                    .getModel(modelSource);
            if (model == null) break;
            if (model.getState() != Model.State.NOT_WELL_FORMED) {
                return model.getDefinitions().getTargetNamespace();
            }
        }
        break;
        }
        return null;
    }
    
    public static boolean isUniquePropertyName(WSDLModel wsdlModel, String propName) {
        assert wsdlModel != null && propName != null;
        List<CorrelationProperty> corrPropList =  wsdlModel.getDefinitions()
                .getExtensibilityElements(CorrelationProperty.class);
        if (corrPropList == null || corrPropList.size() < 1) {
            return true;
        }
        
        for (CorrelationProperty elem : corrPropList) {
            if (propName.equals(elem.getName())) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isUniquePartnerLinkTypeName(WSDLModel wsdlModel, String pltName) {
        assert wsdlModel != null && pltName != null;
        List<PartnerLinkType> pltList =  wsdlModel.getDefinitions().
                getExtensibilityElements(PartnerLinkType.class);
        if (pltList == null || pltList.size() < 1) {
            return true;
        }
        
        for (PartnerLinkType elem : pltList) {
            if (pltName.equals(elem.getName())) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isUniquePartnerLinkTypeRoleName(PartnerLinkType plt, String pltRoleName) {
        assert plt != null && pltRoleName != null;
        Role role = plt.getRole1();
        role = role == null ? plt.getRole2() : role;
        if (role != null && pltRoleName.equals(role.getName())) {
            return false;
        }
        return true;
    }
    
    public static void setQueryImpl(PropertyAlias propAlias, String queryValue) {
        assert propAlias != null;
        if (queryValue == null) {
            return;
        }
        Query propAliasQuery = propAlias.getQuery();
        if (propAliasQuery == null) {
            WSDLModel model = propAlias.getModel();
            if (model == null) {
                return;
            }
            
            Query query = (Query) model.getFactory()
                    .create(propAlias, BPELQName.QUERY.getQName());
            propAlias.addExtensibilityElement(query);
        }
        propAliasQuery = propAlias.getQuery();
        if (propAliasQuery != null) {
            //            System.out.println("propAliasQuery.setContent(queryValue) ");
            propAliasQuery.setContent(queryValue);
        }
    }
    
    public static void disableDefaultActionsInBeenTreeViews(Component comp) {
        BeanTreeView treeView = findChildComponentOfClass(BeanTreeView.class, comp);
        if (treeView != null) {
            treeView.setDefaultActionAllowed(false);
        }
        //        if (comp instanceof BeanTreeView) {
        //            ((BeanTreeView)comp).setDefaultActionAllowed(false);
        //            return;
        //        }
        //        if (comp instanceof Container) {
        //            for (Component child : ((Container)comp).getComponents()) {
        //                disableDefaultActionsInBeenTreeViews(child);
        //            }
        //        }
    }
    
    /**
     * Creates the action which will press the default button (Ok button)
     * for the dialog for which the specified component belongs to.
     */
    public static Action createPreferredAction(final Component comp) {
        Action okAction = new Action() {
            
            public void actionPerformed(ActionEvent e) {
                JRootPane rootPane = SwingUtilities.getRootPane(comp);
                if (rootPane != null) {
                    JButton defaultButton = rootPane.getDefaultButton();
                    if (defaultButton != null) {
                        if (defaultButton.isEnabled()) {
                            defaultButton.doClick(200);
                        }
                    }
                }
            }
            
            public void performAction(Node[] activatedNodes) {
            }
            
            public Object getValue(String key) {
                return null;
            }
            
            public void putValue(String key, Object value) {
                // do nothing
            }
            
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                // do nothing
            }
            
            public void addPropertyChangeListener(PropertyChangeListener listener) {
                // do nothing
            }
            
            public void setEnabled(boolean b) {
                // do nothing
            }
            
            public boolean isEnabled() {
                return true;
            }
            
        };
        //
        return okAction;
    }
    
    /**
     * Subscribes additional listener to the JTree component and processes
     * the double click action independ on the processing of the TreeView.
     */
    public static void attachDefaultDblClickAction(
            final Component comp, final Lookup.Provider lookupProvider) {
        disableDefaultActionsInBeenTreeViews(comp);
        //
        final JTree tree = findChildComponentOfClass(JTree.class, comp);
        if (tree != null) {
            tree.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int selRow = tree.getRowForLocation(e.getX(), e.getY());
                    //
                    if ((selRow != -1) && SwingUtilities.isLeftMouseButton(e) &&
                            MouseUtils.isDoubleClick(e)) {
                        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                        Node node = Visualizer.findNode(selPath.getLastPathComponent());
                        if (node == null) {
                            return;
                        }
                        //
                        boolean isTargetNodeClass = false;
                        //
                        NodesTreeParams treeParams = (NodesTreeParams)lookupProvider.
                                getLookup().lookup(NodesTreeParams.class);
                        if (treeParams != null) {
                            isTargetNodeClass =
                                    treeParams.isTargetNodeClass(node.getClass());
                        }
                        //
                        if (isTargetNodeClass) {
                            JRootPane rootPane = SwingUtilities.getRootPane(comp);
                            if (rootPane != null) {
                                JButton defaultButton = rootPane.getDefaultButton();
                                if (defaultButton != null) {
                                    if (defaultButton.isEnabled()) {
                                        defaultButton.doClick(200);
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
    }
    
    /**
     * Recursively looks for the first child of the specified component
     * with the specified class.
     */
    public static <T extends Component> T findChildComponentOfClass(
            Class<T> targetClass, Component comp) {
        if (targetClass.isInstance(comp)) {
            return (T)comp;
        }
        //
        if (comp instanceof Container) {
            for (Component child : ((Container)comp).getComponents()) {
                T result = findChildComponentOfClass(targetClass, child);
                if (result != null) {
                    return result;
                }
            }
        }
        //
        return null;
    }
    
    public static boolean isEqual(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        } else if (obj1 != null) {
            return obj1.equals(obj2);
        } else {
            return obj2.equals(obj1);
        }
    }
    
    public static String getTargetNamespace(Model model) {
        try {
            if (model instanceof SchemaModel) {
                return ((SchemaModel) model).getSchema().getTargetNamespace();
            } else if (model instanceof BpelModel) {
                return ((BpelModel) model).getProcess().getTargetNamespace();
            } else if (model instanceof WSDLModel) {
                return ((WSDLModel) model).getDefinitions().getTargetNamespace();
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }
}
