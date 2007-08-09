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

package org.netbeans.modules.xml.schema.abe.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.AXIContainer;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.refactoring.CannotRefactorException;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.refactoring.ui.ReferenceableProvider;
import org.netbeans.modules.xml.schema.abe.InstanceDesignConstants;
import org.netbeans.modules.xml.schema.abe.InstanceUIContext;
import org.netbeans.modules.xml.schema.abe.StartTagPanel;
import org.netbeans.modules.xml.schema.abe.UIUtilities;
import org.netbeans.modules.xml.schema.abe.action.ShowDesignAction;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.ui.basic.SchemaGotoType;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.netbeans.modules.xml.xam.ui.actions.GoToAction;
import org.netbeans.modules.xml.xam.ui.actions.GotoType;
import org.netbeans.modules.xml.xam.ui.actions.SourceGotoType;
import org.netbeans.modules.xml.xam.ui.cookies.GetSuperCookie;
import org.netbeans.modules.xml.xam.ui.cookies.GotoCookie;
import org.openide.ErrorManager;
import org.openide.actions.DeleteAction;
import org.openide.actions.NewAction;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Base class of all ABE nodes. Each node is associated with some
 * AXI component. However, there are two ways to obtain the axi component
 * from these nodes.
 *
 * 1. Call getAXIComponent()
 * 2. Find it from the node's lookup()
 *
 * getAXIComponent() will return the component this node is associated with.
 * In contrast, the component that you get from lookup is always the original.
 * IN AXIOM there can be proxy components that act on behalf of an original or
 * shared component. For example, lets say PO.xsd declares an element 'shipTo' and
 * the type of this eleemnt is from a differnet source file Address.xsd.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class ABEAbstractNode extends AbstractNode
        implements GotoCookie, PropertyChangeListener, ReferenceableProvider {
    
    private InstanceUIContext context;
    private AXIComponent axiComponent;
    private Datatype datatype;
    boolean uiNode = false;
    private InstanceContent icont = new InstanceContent();
    private boolean readOnly = false;
    
    /**
     * Creates a new instance of ABEAbstractNode
     */
    public ABEAbstractNode(AXIComponent axiComponent, InstanceUIContext instanceUIContext) {
        this(axiComponent, Children.LEAF, instanceUIContext, new InstanceContent());
    }
    
    public ABEAbstractNode(AXIComponent axiComponent, Children children) {
        this(axiComponent, children, null, new InstanceContent());
    }
    
    private ABEAbstractNode(AXIComponent axiComponent, Children children,
            final InstanceUIContext instanceUIContext, InstanceContent icont){
        super(children, createLookup(axiComponent, icont, instanceUIContext));
        this.icont = icont;
        this.setAXIComponent(axiComponent);
        this.setContext(instanceUIContext);
        if(instanceUIContext != null){
            uiNode = true;
            this.icont.add(instanceUIContext);
            instanceUIContext.addPropertyChangeListener(new PropertyChangeListener(){
                public void propertyChange(PropertyChangeEvent evt) {
                    if(evt.getPropertyName().equals(InstanceDesignConstants.PROP_SHUTDOWN)){
                        ABEAbstractNode.this.icont.remove(instanceUIContext);
                        ABEAbstractNode.this.axiComponent = null;
                    }
                }
            });
        }
        this.icont.add(this);
    }
    
    /**
     * This was added for go to source.
     * Keep the axiComponent in the node's lookup. If the component
     * is a proxy, keep the original or shared component.
     */
    private static Lookup createLookup(final AXIComponent component, InstanceContent icont, InstanceUIContext context) {
        AXIComponent lookupComponent = component;
        if(component.getComponentType() == ComponentType.PROXY)
            lookupComponent = component.getOriginal();
        final AXIComponent tmpLookupComponent = component;
        Lookup doLookup = null;
        if(context != null){
            doLookup = context.getSchemaDataObject().getNodeDelegate().getLookup();
            doLookup = Lookups.exclude(doLookup, new Class[]{Node.class});
        }
        
        return new ProxyLookup(new Lookup[]{
            // schemamodel lookup
            // exclude the DataObject here because the DataObject for the
            // model this node is displayed in will be coming from the
            // NodeDelegate. If this is not done there end up being two
            // DataObjects in the lookup and this may cause a problem with
            // save cookies, etc.
            Lookups.exclude(
                    component.getModel().getSchemaModel().getModelSource().getLookup(),
                    new Class[] {DataObject.class}
            ),
            // axi component
            Lookups.singleton(lookupComponent),
            Lookups.singleton(new GetSuperCookie(){
                // this is for go to super definition.
                public Component getSuper() {
                    return UIUtilities.getSuperDefn(tmpLookupComponent);
                }
            }),
            //Schema DO's lookup
            (doLookup == null ? Lookup.EMPTY : doLookup),
            //and misc
            new AbstractLookup(icont)
        });
    }
    
    
    public void showSuperDefinition(){
        InstanceUIContext context = (InstanceUIContext) getLookup().lookup(InstanceUIContext.class);
        UIUtilities.showDefinition(context, getAXIComponent(), true);
    }
    
    
    /**
     * Overwrites AbstractNode's createSheet to allow the creation of sheet.
     */
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        populateProperties(sheet);
        return sheet;
    }
    
    protected abstract void populateProperties(Sheet sheet);
    
    protected abstract String getTypeDisplayName();
    
    public String getDisplayName() {
        String instanceName = super.getDisplayName();
        return ((instanceName == null || instanceName.length() == 0)
        ? "" : instanceName + " ") +
                "[" + getTypeDisplayName() + "]"; // NOI18N
    }
    
    public final String getHtmlDisplayName() {
        return super.getDisplayName();
    }
    
    public Action[] getActions(boolean b) {
        if(uiNode){
            if(getAXIComponent().isReadOnly()){
                //filter out refactor action if this is a readonly file
                SystemAction[] ret = new SystemAction[ALL_ACTIONS.length];
                for(int i = 0; i < ALL_ACTIONS.length; i++){
                    String name = null;
                    if(ALL_ACTIONS[i] != null)
                        name = (String)ALL_ACTIONS[i].getValue(Action.NAME);
                                
                    if(name != null && name.equals("Refactor") ){
                        ret[i] = null;
                    }else{
                        ret[i] = ALL_ACTIONS[i];
                    }
                }
                return ret;
            }
            return ALL_ACTIONS;
        } else
            return SUB_ACTIONS;
    }
    
    private static final GotoType[] GOTO_TYPES = new GotoType[] {
        new SourceGotoType(),
        new SchemaGotoType(),
    };
    
    
    private static final SystemAction[] ALL_ACTIONS=
            new SystemAction[]
    {
        /*SystemAction.get(CutAction.class),
        SystemAction.get(CopyAction.class),
        SystemAction.get(PasteAction.class),
        null,*/
        SystemAction.get(NewAction.class),
        SystemAction.get(DeleteAction.class),
        null,
        SystemAction.get(GoToAction.class),
        null,
        (SystemAction)RefactoringActionsFactory.whereUsedAction(),
        (SystemAction)RefactoringActionsFactory.editorSubmenuAction(),
        null,
        SystemAction.get(PropertiesAction.class)
    };
    
    
    private static final SystemAction[] SUB_ACTIONS=
            new SystemAction[]
    {
        SystemAction.get(GoToAction.class),
    };
    
    
    public InstanceUIContext getContext() {
        return context;
    }
    
    public void setContext(InstanceUIContext context) {
        this.context = context;
    }
    
    public AXIComponent getAXIComponent() {
        return axiComponent;
    }
    
    private void setAXIComponent(AXIComponent axiComponent) {
        this.axiComponent = axiComponent;
        axiComponent.getModel().addPropertyChangeListener(
                WeakListeners.propertyChange(this, axiComponent.getModel())
                );
    }
    
    public Datatype getDatatype() {
        return datatype;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == axiComponent &&
                evt.getPropertyName().equals(AXIContainer.PROP_NAME)) {
            Object oldValue = evt.getOldValue();
            String oldDisplayName = oldValue == null ? null : oldValue.toString();
            fireDisplayNameChange(oldDisplayName, getDisplayName());
        }
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(ShowDesignAction.class);
    }
    
    public NewType[] getNewTypes() {
        return new NewType[0];
    }    

    public GotoType[] getGotoTypes() {
        return GOTO_TYPES;
    }
    
    public void remove() {
        if(!canWrite())
            return;
        UIUtilities.setBusyCursor(context);
        try {
            if(getReferenceable() != null) {
                safeDelete();
                return;
            }
            //use normal delete
            doDelete();
        } finally {
            UIUtilities.setDefaultCursor(context);
        }
    }
    
    private void doDelete() {
        if(getAXIComponent() == null ||
                getAXIComponent().getModel() == null)
            return;
        AXIModel model = getAXIComponent().getModel();
        model.startTransaction();
        try{
            getAXIComponent().getParent().removeChild(getAXIComponent());
        }finally{
            model.endTransaction();
        }
    }
    
    public boolean isReadOnly() {
        return readOnly;
    }
    
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    public boolean canDestroy() {
        return canWrite();
    }
    
    public void destroy() throws IOException {
        super.destroy();
        remove();
    }
    
    
    
    public boolean canWrite() {
        // Check for null model since component may have been removed.
        AXIComponent c = getAXIComponent();
        if(c == null || (c.getModel() == null))
            return false;
        
        AXIComponent o = c.getOriginal();
        if(c != o && c.isReadOnly()) {
            return false;
        }
        
        SchemaModel model = c.getModel().getSchemaModel();
        return XAMUtils.isWritable(model);
    }
    
    
    public void setName(String name) {
        if(name == null || getName().equals(name))
            return;
        InstanceUIContext context = (InstanceUIContext) this.
                getLookup().lookup(InstanceUIContext.class);
        AXIComponent axiComponent = getAXIComponent();
        if(canWrite()){
            if(org.netbeans.modules.xml.xam.dom.Utils.isValidNCName(name)){
                //call refactoring.
                UIUtilities.setBusyCursor(context);
                try{
                    setNameByRefactoring(name);
                }finally{
                    //reset the wait cursor
                    UIUtilities.setDefaultCursor(context);
                }
            }else{
                if(context != null){
                    String errorMessage = NbBundle.getMessage(StartTagPanel.class,
                            "MSG_NOT_A_NCNAME");
                    UIUtilities.showErrorMessage(errorMessage, context);
                }
            }
        }else{
            if(context != null){
                String errorMessage = NbBundle.getMessage(ABEAbstractNode.class,
                        "MSG_IS_READONLY");
                UIUtilities.showErrorMessage(errorMessage, context);
            }
        }
    }
    
    
    public void setNameByRefactoring(String value) {
        NamedReferenceable ref = getReferenceable();
        if(ref == null){
            //since this is a ref, it can not be renamed here.
            //this often happens for references
            /*setNameInModel(value);
            return;*/
            AXIComponent axiComponent = getAXIComponent();
            if( (axiComponent instanceof Element) || (axiComponent instanceof Attribute) ){
                if(axiComponent instanceof Element){
                    axiComponent = ((Element) axiComponent).getReferent();
                } else{
                    axiComponent = ((Attribute) axiComponent).getReferent();
                }
                if(axiComponent == null){
                    setNameInModel(value);
                    return;
                }
                ref = getReferenceable(axiComponent);
            }else{
                setNameInModel(value);
                return;
            }
        }
        AXIModel model = getAXIComponent().getModel();
        // try rename silently
        
        try {
            
            context.setUserInducedEventMode(true);
            SchemaModel sm = model.getSchemaModel();
          //  RefactoringManager.getInstance().execute(request, false);
            SharedUtils.silentRename((Nameable)ref,value, false);
            model.sync();
        } catch(CannotRefactorException ex) {
            SharedUtils.showRenameRefactoringUI((Nameable)ref);
            // call rename refactoring UI
           /*
            WhereUsedView wuv = new WhereUsedView(ref);
            RenameRefactoringUI ui = new RenameRefactoringUI(wuv, request);
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            if (activetc instanceof CloneableEditorSupport.Pane) {
                new RefactoringPanel(ui, activetc);
            } else {
                new RefactoringPanel(ui);
            }*/
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    public void setNameInModel(String name){
        //local component so just rename
        try {
            axiComponent.getModel().startTransaction();
            if(axiComponent instanceof Element)
                ((Element)axiComponent).setName(name);
            else if(axiComponent instanceof ContentModel)
                ((ContentModel)axiComponent).setName(name);
            else if(axiComponent instanceof Attribute)
                ((Attribute)axiComponent).setName(name);
        } finally{
            axiComponent.getModel().endTransaction();
        }
    }
    
    public NamedReferenceable getReferenceable() {
        // if(!getAXIComponent().getOriginal().isGlobal())
        //    return null;
        
        SchemaComponent comp = getAXIComponent().getOriginal().getPeer();
        if (comp instanceof NamedReferenceable && isValid() && comp.getModel().
                getModelSource().getLookup().lookup(FileObject.class) != null){
            return NamedReferenceable.class.cast(comp);
        }
        return null;
    }
    
   private NamedReferenceable getReferenceable(AXIComponent axiComponent) {
       // if(!axiComponent.getOriginal().isGlobal())
       //     return null;
        SchemaComponent comp = axiComponent.getOriginal().getPeer();
        if (comp instanceof NamedReferenceable && isValid() && comp.getModel().
                getModelSource().getLookup().lookup(FileObject.class) != null){
            return NamedReferenceable.class.cast(comp);
        }
        return null;
    }
    
    protected boolean isValid() {
        return getAXIComponent().getPeer().getModel() != null;
    }
    
    private void safeDelete() {
        final NamedReferenceable ref = getReferenceable();
        // try delete silently
        AXIModel model = getAXIComponent().getModel();
        try {
            context.setUserInducedEventMode(true);
            SchemaModel sm = model.getSchemaModel();
            SharedUtils.silentDeleteRefactor(ref, true);
            model.sync();
        } catch(CannotRefactorException ex) {
            SharedUtils.showDeleteRefactoringUI(ref);
            // call delete refactoring UI
                 
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    
    public Sheet.Set getSharedSet(Sheet sheet){
        Sheet.Set sharedSet = null;
        sharedSet = sheet.get("shared");
        if(sharedSet != null)
            return sharedSet;
        sharedSet = sheet.createPropertiesSet();
        String shared = NbBundle.getMessage(ElementNode.class, "LBL_SHARED");
        String sharedMessage = NbBundle.getMessage(ElementNode.class, "LBL_SHARED_MESSAGE");
        sharedSet.setName("shared");//NOI18N
        sharedSet.setDisplayName(shared);
        sharedSet.setShortDescription(sharedMessage);
        return sharedSet;
    }
    
}
