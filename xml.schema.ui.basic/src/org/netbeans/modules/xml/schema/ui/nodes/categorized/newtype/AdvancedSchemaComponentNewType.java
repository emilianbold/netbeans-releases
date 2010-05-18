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

/*
 * AdvancedSchemaComponentNewType.java
 *
 * Created on January 19, 2006, 1:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized.newtype;

// java imports
import java.awt.Dialog;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.Schema;

//netbeans imports
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

//local imports
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.basic.UIUtilities;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.ImportCreator;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.IncludeCreator;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.RedefineCreator;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Ajit Bhate (ajit.bhate@Sun.Com)
 */
public class AdvancedSchemaComponentNewType extends NewType {
    
    private SchemaComponentReference<? extends SchemaComponent> reference;
    private Class<? extends SchemaComponent> childType;
    private AdvancedSchemaComponentCreator creator;
    private SchemaComponent component;
    private SchemaComponent container;
    
    /**
     * Creates a new instance of AdvancedSchemaComponentNewType
     */
    public AdvancedSchemaComponentNewType(SchemaComponentReference<? extends SchemaComponent>
            reference, Class<? extends SchemaComponent> childType) {
        super();
        this.reference=reference;
        this.childType=childType;
        this.creator = new AdvancedSchemaComponentCreator();
    }
    
    public String getName() {
        return NbBundle.getMessage(AdvancedSchemaComponentNewType.class,
                "LBL_NewType_".concat(getChildType().getSimpleName()));
    }
    
    public void create() {
        if (!canCreate()) {
            showIncompleteDefinitionMessage();
            return;
        }
        SchemaModel model = getSchemaComponent().getModel();
        assert model != null;
        boolean showComponent = false;
        try {
            if(customize()) {
                model.startTransaction();
                addComponent(container);
                showComponent = true;
            }
        } finally {
            if (model.isIntransaction()) {
                model.endTransaction();
            }
        }
        
        // Select in view, only if component was successfully created.
        if (showComponent) {
            try {
                FileObject fobj = (FileObject) model.getModelSource().
                        getLookup().lookup(FileObject.class);
                if (fobj != null) {
                    DataObject dobj = DataObject.find(fobj);
                    if (dobj != null) {
                        ViewComponentCookie svc = (ViewComponentCookie) dobj.getCookie(
                                ViewComponentCookie.class);
                        if (svc != null) {
                            svc.view(ViewComponentCookie.View.STRUCTURE,
                                    getComponent());
                        }
                    }
                }
            } catch (DataObjectNotFoundException donfe) {
            }
        }
    }
    
    public boolean canCreate() {
        if(getComponent()==null) {
            setComponent(createComponent());
            setContainer(findContainer());
        }
        return getContainer()!=null;
    }
    
    /**
     * The container of the new type.
     * In most cases it will be getSchemaComponent(), but need to ensure correct type.
     * It uses AdvancedSchemaComponentCreator to find appropriate container.
     */
    protected SchemaComponent findContainer() {
        return getCreator().findContainer(getSchemaComponent(), getComponent());
    }
    
    /**
     * This api adds required new type to the container.
     * This is called from create.
     * The create method ensures a transaction and does error reporting.
     */
    protected void addComponent(SchemaComponent container) {
        getCreator().add(container, getComponent());
    }
    
    /**
     * This api creates required new type.
     * uses SchemaComponentCreator to add
     */
    protected SchemaComponent createComponent() {
        return SchemaComponentCreator.createComponent(getSchemaComponent().
                getModel().getFactory(), getChildType());
    }
    
    protected SchemaComponent getSchemaComponent() {
        return getReference().get();
    }
    
    protected SchemaComponentReference<? extends SchemaComponent> getReference() {
        return reference;
    }
    
    protected Class<? extends SchemaComponent> getChildType() {
        return childType;
    }
    
    
    protected AdvancedSchemaComponentCreator getCreator() {
        return creator;
    }
    
    /**
     * getter for newly created component
     */
    protected SchemaComponent getComponent() {
        return component;
    }
    
    /**
     * setter for newly created component
     */
    protected void setComponent(SchemaComponent component) {
        this.component = component;
    }
    
    /**
     * getter for container
     */
    protected SchemaComponent getContainer() {
        return container;
    }
    
    /**
     * setter for container
     */
    protected void setContainer(SchemaComponent container) {
        this.container = container;
    }
    
    /**
     * This apis check if newtype needs a customizer and returns true,
     * if customizer is not needed or if user OKs customization, false otherwise.
     */
    protected boolean customize() {
        // XXX: This bit is an ugly hack, need a better way to create a
        //      different customizer depending on whether the component
        //      is new versus existing.
        SchemaComponent comp = getComponent();
        Customizer customizer;
        boolean created = true;
        if (comp instanceof Import) {
            SchemaModel model = getSchemaComponent().getModel();
            Schema schema = model.getSchema();
            customizer = new ImportCreator(schema);
        } else if (comp instanceof Include) {
            SchemaModel model = getSchemaComponent().getModel();
            Schema schema = model.getSchema();
            customizer = new IncludeCreator(schema);
        } else if (comp instanceof Redefine) {
            SchemaModel model = getSchemaComponent().getModel();
            Schema schema = model.getSchema();
            customizer = new RedefineCreator(schema);
        } else {
            customizer = getCreator().createCustomizer(comp, getContainer());
            created = false;
        }
        if(customizer==null || customizer.getComponent()==null) return true;
        DialogDescriptor descriptor = UIUtilities.
                getCustomizerDialog(customizer,getName(),true);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setTitle(NbBundle.getMessage(AdvancedSchemaComponentNewType.class,
                "LBL_Customizer_".concat(getChildType().getSimpleName())));
        dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
        dlg.setVisible(true);
        // For the created case, return false so that the component will not
        // be created again, and then not have any customization performed
        // on it; the creators have already created the component(s).
        return !created && descriptor.getValue() == DialogDescriptor.OK_OPTION;
    }
    /**
     * This will show a message to user if this newtype can't be created
     *
     */
    private void showIncompleteDefinitionMessage() {
        String message = NbBundle.getMessage(AdvancedSchemaComponentNewType.class,
                "MSG_NewType_IncompleteDefinition",	getName().toLowerCase());
        NotifyDescriptor.Message descriptor =
                new NotifyDescriptor.Message(message);
        DialogDisplayer.getDefault().notify(descriptor);
    }
}
