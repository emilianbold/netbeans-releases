/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.nodes;

import java.awt.Component;
import org.netbeans.modules.bpel.design.nodes.DiagramExtInfo;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.NamespaceSpec;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.bpel.properties.editors.ImportMainPanel;
import org.netbeans.modules.bpel.properties.editors.controls.CustomNodeEditor.EditingMode;
import org.netbeans.modules.bpel.properties.editors.controls.SimpleCustomEditor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author nk160297
 */
public class ImportNode extends DiagramBpelNode<Import, DiagramExtInfo> {
    
    public ImportNode(Import reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public ImportNode(Import reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.IMPORT;
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        DiagramExtInfo diagramReference = getDiagramReference();
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                Import.IMPORT_TYPE, IMPORT_TYPE,
                "getImportType", "setImportType", null); // NOI18N
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                NamespaceSpec.NAMESPACE, IMPORT_NAMESPACE,
                "getNamespace", "setNamespace", "removeNamespace"); // NOI18N
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                Import.LOCATION, IMPORT_LOCATION,
                "getLocation", "setLocation", "removeLocation"); // NOI18N
        //
        return sheet;
    }
    
    public String getNameImpl() {
        Import imprt = getReference();
        if (imprt == null) {
            return super.getNameImpl();
        }
        String imprtLocation = imprt.getLocation();
        
        imprtLocation = ResolverUtility.decodeLocation(imprtLocation);
        
        FileObject fo = ResolverUtility.getImportedFile(imprtLocation, getLookup());
        if (fo != null && fo.isValid()) {
            String relativePath =
                    ResolverUtility.calculateRelativePathName(fo, getLookup());
            if (relativePath != null && relativePath.length() != 0) {
                return relativePath;
            }
        }
        //
        return imprtLocation == null ? "[" + Constants.MISSING + "] " : imprtLocation; // NOI18N
    }
    
//    protected String getImplShortDescription() {
//        Import imprt = getReference();
//        String tooltipString = "";
//        if (imprt != null) {
//            tooltipString = imprt.getNamespace();
//        }
//        return super.getImplShortDescription() + " " +
//                NbBundle.getMessage(ImportNode.class,
//                "LBL_IMPORT_NODE_TOOLTIP", // NOI18N
//                tooltipString,
//                ""
//                );
//    }
    
    public Component getCustomizer() {
        SimpleCustomEditor customEditor = new SimpleCustomEditor<Import>(
                this, ImportMainPanel.class,
                EditingMode.CREATE_NEW_INSTANCE);
        return customEditor;
    }
    
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.GO_TO_SOURCE,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }
    
    public boolean canCopy() {
        //Fix for 86843: Do not allow DnD this node.
        return false;
    }
}
