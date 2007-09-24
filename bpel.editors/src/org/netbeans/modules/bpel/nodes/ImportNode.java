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
package org.netbeans.modules.bpel.nodes;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.NamespaceSpec;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author nk160297
 */
public class ImportNode extends BpelNode<Import> {
    
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
        PropertyUtils.registerProperty(this, mainPropertySet,
                DOCUMENTATION, "getDocumentation", "setDocumentation"); // NOI18N
        //
        return sheet;
    }
    
    public String getNameImpl() {
        Import imprt = getReference();
        if (imprt == null) {
            return super.getNameImpl();
        }

        String relativePath = getRelativePath(imprt);
        if (relativePath != null && relativePath.length() > 0) {
            return relativePath;
        }
        String location = imprt.getLocation();
        if (location != null && location.length() > 0) {
            return ResolverUtility.decodeLocation(location);
        }
        String namespace = imprt.getNamespace();
        if (namespace != null && namespace.length() > 0) {
            return namespace;
        }
        //TODO:change to use NbBundle, figure out good name
        return "Unqualified";
    }
        
//    protected String getImplShortDescription() {
//        Import imprt = getReference();
//        if (imprt == null) {
//            return super.getImplShortDescription();
//        }
//        
//        StringBuffer addTooltip = new StringBuffer();
//        addTooltip.append(
//                imprt.getNamespace() == null || imprt.getNamespace().equals(EMPTY_STRING)
//                    ? EMPTY_STRING
//                    : NbBundle.getMessage(
//                        BpelNode.class,
//                        "LBL_ATTRIBUTE_HTML_TEMPLATE", // NOI18N
//                        Import.NAMESPACE, 
//                        imprt.getNamespace()
//                        )
//                    );
//        
//        return NbBundle.getMessage(BpelNode.class,
//                "LBL_LONG_TOOLTIP_HTML_TEMPLATE", // NOI18N
//                getNodeType().getDisplayName(), 
//                getName(),
//                addTooltip.toString()
//                ); 
//    }
    
    public Component getCustomizer() {
        return null;
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
        //  Fix for 86843: Do not allow DnD this node.
        // Turn back
        return true;
    }

    public boolean canCut() {
        return false;
    }

    /**
    * This implementation only calls clipboardCopy supposing that
    * copy to clipboard and copy by d'n'd are similar.
    *
    * @return transferable to represent this node during a drag
    * @exception IOException when the
    *    cut cannot be performed
    */
    @Override
    public Transferable drag() throws IOException {
        return clipboardCopy();
    }
    
    /** Cut this delegated dataobject node to the clipboard.
    * unsupported for now
    * @return {@link org.openide.util.datatransfer.ExTransferable.Single} with one copy flavor
    * @throws IOException if it could not copy
    * @see org.openide.nodes.NodeTransfer
    */
    @Override
    public Transferable clipboardCut () throws IOException {
        DataObject dObj = getDataObject();
        if (dObj != null) {
            Node delegator = dObj.getNodeDelegate();
            return delegator != null ? delegator.clipboardCut() : null;
        }
        return null;
    }

    /** Copy this delegated dataobject node to the clipboard.
    *
    * @return {@link org.openide.util.datatransfer.ExTransferable.Single} with one copy flavor
    * @throws IOException if it could not copy
    * @see org.openide.nodes.NodeTransfer
    */
    @Override
    public Transferable clipboardCopy () throws IOException {
        DataObject dObj = getDataObject();
        if (dObj != null) {
            Node delegator = dObj.getNodeDelegate();
            return delegator != null ? delegator.clipboardCopy() : null;
        }
        return null;
    }
    
    private String getRelativePath(Import imprt) {
        assert imprt != null;
        FileObject ifo = ResolverUtility.getImportedFileObject(imprt);
        Project modelProject = ResolverUtility.safeGetProject(imprt.getBpelModel());
        return ResolverUtility.safeGetRelativePath(ifo, modelProject);
    }
    
    private DataObject getDataObject() {
        Import imprt = getReference();
        FileObject fo = null;
        synchronized (imprt) {
            if (imprt == null) {
                return null;
            }

            fo = ResolverUtility.getImportedFileObject(imprt);
        }
        
        // to prevent IllegalArgumentException from DataObject.find(fo)
        if (fo == null || !fo.isValid()) {
            return null;
        }
        
        try {
            DataObject dObj = DataObject.find(fo);
            return dObj;
        } catch (DataObjectNotFoundException ex) {
//            ex.printStackTrace();
            return null;
        }
    }
}
