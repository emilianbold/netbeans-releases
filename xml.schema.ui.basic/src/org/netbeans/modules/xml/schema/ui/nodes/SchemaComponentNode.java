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

package org.netbeans.modules.xml.schema.ui.nodes;

import java.awt.Dialog;
import java.awt.datatransfer.Transferable;
import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;

//import org.netbeans.modules.xml.refactoring.actions.RefactorAction;
import org.netbeans.modules.xml.refactoring.ui.ReferenceableProvider;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.ui.basic.DesignGotoType;
import org.netbeans.modules.xml.schema.ui.basic.SchemaGotoType;
import org.netbeans.modules.xml.schema.ui.basic.SchemaSettings;
import org.netbeans.modules.xml.schema.ui.basic.UIUtilities;
import org.netbeans.modules.xml.xam.dom.Utils;
import org.netbeans.modules.xml.xam.ui.ComponentPasteType;
import org.netbeans.modules.xml.xam.ui.actions.GotoType;
import org.netbeans.modules.xml.xam.ui.actions.SourceGotoType;
import org.netbeans.modules.xml.xam.ui.actions.SuperGotoType;
import org.netbeans.modules.xml.xam.ui.cookies.GetComponentCookie;
import org.netbeans.modules.xml.xam.ui.cookies.GetSuperCookie;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.schema.ui.basic.ShowSchemaAction;
import org.netbeans.modules.xml.xam.ui.cookies.GotoCookie;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.netbeans.modules.xml.xam.ui.customizer.CustomizerProvider;
import org.netbeans.modules.xml.xam.ui.highlight.Highlight;
import org.netbeans.modules.xml.xam.ui.highlight.HighlightManager;
import org.netbeans.modules.xml.xam.ui.highlight.Highlighted;
import org.netbeans.modules.xml.schema.ui.basic.editors.StringEditor;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BaseSchemaProperty;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.netbeans.modules.xml.xam.ui.actions.GoToAction;
import org.netbeans.modules.xml.xam.ui.cookies.CountChildrenCookie;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.NewAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ReorderAction;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 * @author  Nathan Fiedler
 */
public abstract class SchemaComponentNode<T extends SchemaComponent>
        extends AbstractNode
        implements Node.Cookie, ComponentListener, PropertyChangeListener,
        Highlighted, ReferenceableProvider, CountChildrenCookie,
        GetComponentCookie, GetSuperCookie, GotoCookie {

    /**
     *
     *
     */
    public SchemaComponentNode(SchemaUIContext context,
            SchemaComponentReference<T> reference, Children children) {
        this(context,reference,children,new InstanceContent());
    }
    
    
    /**
     * Constructor HACK to allow creating of our own lookup
     *
     */
    private SchemaComponentNode(SchemaUIContext context,
            SchemaComponentReference<T> reference, Children children,
            InstanceContent contents) {
        super(children, createLookup(context, contents));
        
        this.context=context;
        this.reference=reference;
        this.lookupContents=contents;
        
        // Add various objects to the lookup.
        contents.add(this);
        // Include the data object in order for the Navigator to
        // show the structure of the current document.
        DataObject dobj = getDataObject();
        if (dobj != null) {
            contents.add(dobj);
        }
        contents.add(context);
        contents.add(reference);
        contents.add(new DefaultExpandedCookie(false));
//        // add customizer provider if provided
//        CustomizerProvider provider = getCustomizerProvider();
//        if (provider != null) {
//            contents.add(provider);
//        }
        
        // reorder must be enabled only if its editable node
        if (children instanceof Index && isEditable() &&
                // dont show for schema bug 80138
                !(reference.get() instanceof Schema)) {
            contents.add(children);
        }
        T comp = reference.get();
        contents.add(comp);
        
        // Listen to changes in the model using a WeakListener. I hold onto
        // the WeakListener instance so I can explicitly remove it in the
        // destroy method.
        SchemaModel model = reference.get().getModel();
        if (model != null) {
            weakModelListener=
                    WeakListeners.propertyChange(this,model);
            model.addPropertyChangeListener(weakModelListener);
            weakComponentListener = (ComponentListener) WeakListeners.create(
                    ComponentListener.class, this, model);
            model.addComponentListener(weakComponentListener);
        }
        // Determine default names for the node
        if (comp instanceof Named) {
            // Just set the name, and let the method call below handle
            // the display name
            _setName(((Named) comp).getName());
        } else {
            _setName(comp.getPeer().getLocalName());
        }
        // Need a model for the following to work properly.
        if (model != null) {
            // Let the node try to update its display name
            updateDisplayName();
            // Let the node try to update its short desc
            updateShortDescription();
        }

        setIconBaseWithExtension(
                "org/netbeans/modules/xml/schema/ui/nodes/resources/"+
                "generic.png");

        referenceSet = Collections.singleton(
                (Component) ((SchemaComponentReference) reference).get());
        highlights = new LinkedList<Highlight>();
        HighlightManager.getDefault().addHighlighted(this);
    }
    
    /**
     * Create a lookup for this node, based on the given contents.
     *
     * @param  context   from which a Lookup is retrieved.
     * @param  contents  the basis of our new lookup.
     */
    private static Lookup createLookup(SchemaUIContext context,
            InstanceContent contents) {
        // We want our lookup to be based on the lookup from the context,
        // which provides a few necessary objects, such as a SaveCookie.
        // However, we do not want the Nodes or DataObjects, since we
        // provide our own.
        return new ProxyLookup(new Lookup[] {
            // Keep our lookup contents first, so that whatever we add to
            // the lookup is at the top of the lookup, such as this node,
            // which provides certain cookies, rather than that of the
            // currently selected node.
            new AbstractLookup(contents),
            Lookups.exclude(context.getLookup(), new Class[] {
                Node.class,
                DataObject.class,
            }),
        });
    }
    
    /**
     * Attempt to retrieve the DataObject associated with the model that
     * contains the component this node represents.
     *
     * @return  schema data object, if available, or null if not.
     */
    private DataObject getDataObject() {
        try {
            // Include the data object in order for the Navigator to
            // show the structure of the current document.
            SchemaModel model = reference.get().getModel();
            if (model != null) {
                FileObject fobj = (FileObject) model.getModelSource().
                        getLookup().lookup(FileObject.class);
                if (fobj != null) {
                    return DataObject.find(fobj);
                }
            }
        } catch (DataObjectNotFoundException donfe) {
            // fall through to return null
        }
        return null;
    }
    
    /**
     * This api returns the customizer provider.
     * Subclasses must override this api to return appropriate
     * customizer provider
     */
    protected CustomizerProvider getCustomizerProvider() {
        return null;
    }
    
    /**
     * Overriden to provide custom customizer.
     * Gets the customizer component from customizer provider
     * and displays it as modal dialog.
     * Subclasses who provide customizer should override
     * hasCustomizer and return true.
     */
    @Override
            public java.awt.Component getCustomizer() {
        if(!hasCustomizer()|| !isEditable()) return null;
        // get it from soft ref
        if(custRef==null || custRef.get()==null) {
            CustomizerProvider cp = getCustomizerProvider();
            if (cp==null) return null;
            Customizer cust = cp.getCustomizer();
            if (cust==null || cust.getComponent()==null) return null;
            custRef = new SoftReference<Customizer>(cust);
        } else {
            // might not be in sync so sync
            custRef.get().reset();
        }
        Customizer customizer = custRef.get();
        DialogDescriptor descriptor = UIUtilities.
                getCustomizerDialog(customizer,getTypeDisplayName(),isEditable());
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
        return dlg;
    }
    
    /**
     *
     *
     */
    public boolean equals(Object o) {
        // Without this, the tree view collapses when nodes are changed.
        if (o instanceof SchemaComponentNode) {
            SchemaComponentNode scn = (SchemaComponentNode) o;
            SchemaComponentReference scr = scn.getReference();
            return scr.equals(reference);
        }
        return false;
    }
    
    
    /**
     *
     *
     */
    public int hashCode() {
        // Without this, the tree view collapses when nodes are changed.
        return reference.hashCode();
    }
    
    
    /**
     *
     *
     */
    public SchemaUIContext getContext() {
        return context;
    }
    
    
    /**
     *
     *
     */
    public SchemaComponentReference<T> getReference() {
        return reference;
    }
    
    
    /**
     * Returns the contents of the lookup.  All cookies and other objects that
     * should be findable via the lookup should be added to this.
     *
     */
    protected InstanceContent getLookupContents() {
        return lookupContents;
    }
    
    /**
     * Determines if this node represents a component that is contained
     * in a valid (non-null) model.
     *
     * @return  true if model is valid, false otherwise.
     */
    protected boolean isValid() {
        return getReference().get().getModel() != null;
    }
    
    /**
     * Determines if this node represents a component that is contained
     * is editable
     *
     * @return  true if component is editable, false otherwise.
     */
    protected boolean isEditable() {
        SchemaModel model = getReference().get().getModel();
        return model != null && model == getContext().getModel() && 
				XAMUtils.isWritable(model);
    }
    
    /**
     * Used by subclasses to update the display name as needed.  The default
     * implementation updates the display name for named schema components.
     * Note, this method may be called from the constructor, so be sure to
     * avoid using member variables!
     *
     */
    protected void updateDisplayName() {
        if (!isValid()) {
            // If there is no model, exceptions will occur.
            return;
        }
        T component = getReference().get();
        if (component instanceof Named) {
            String name=((Named)component).getName();
            // Automatically keep the name in sync for named schema components.
			_setName(name);
			if(name==null||name.equals("")) name = component.getPeer().getLocalName();
            setDisplayName(name);
        }
    }
    
    
    /**
     * updates the short descrption associated with node.
     * checks for if there is any annotation with documentation element.
     */
    private void updateShortDescription() {
        if (!isValid()) {
            // If there is no model, exceptions will occur.
            return;
        }
        T component = getReference().get();
        Documentation d = null;
        Annotation a = null;
        String language = SchemaSettings.getDefault().getLanguage();
        if (component instanceof Documentation) {
            d = (Documentation) component;
        } else if (component instanceof Annotation) {
            a = (Annotation) component;
        } else {
            a = component.getAnnotation();
        }
        if (a != null && !a.getDocumentationElements().isEmpty()) {
            if(language==null) {
                d = a.getDocumentationElements().iterator().next();
            } else {
                for (Documentation doc:a.getDocumentationElements()) {
                    if(language.equals(doc.getLanguage())) {
                        d = doc;
                        break;
                    }
                }
                if(d==null) {
                    d = a.getDocumentationElements().iterator().next();
                }
            }
        }
        if (d != null) {
            setShortDescription(d.getContentFragment());
        } else {
            setShortDescription(null);
        }
    }
    
    
    /**
     *
     *
     */
    public abstract String getTypeDisplayName();
    
    
    /**
     *
     *
     */
    protected String getHtmlTypeDisplayName() {
        return "<font color='#aaaaaa'>("+getTypeDisplayName()+")</font>";
    }
    
    
    /**
     *
     *
     */
    public boolean isDefaultExpanded() {
        DefaultExpandedCookie cookie=(DefaultExpandedCookie)
        getCookie(DefaultExpandedCookie.class);
        if (cookie!=null)
            return cookie.isDefaultExpanded();
        else
            return false;
    }
    
    
    /**
     *
     *
     */
    public void setDefaultExpanded(boolean value) {
        DefaultExpandedCookie cookie=(DefaultExpandedCookie)
        getCookie(DefaultExpandedCookie.class);
        if (cookie!=null)
            cookie.setDefaultExpanded(value);
    }
    
    
    /**
     * Finds the super definition of the schema component.
     * Returns null as default implementation
     * Subclasses which fave global type or reference definitions,
     * must override to return the global reference.
     */
    protected ReferenceableSchemaComponent getSuperDefinition() {
        return null;
    }

    public int getChildCount() {
        return getReference().get().getChildren().size();
    }

    public Component getComponent() {
        return getReference().get();
    }

    public Class<? extends Component> getComponentType() {
        return getReference().get().getComponentType();
    }

	// implementation of get super cookie
	public SchemaComponent getSuper()
	{
		return getSuperDefinition();
	}
  
    
    ////////////////////////////////////////////////////////////////////////////
    // Node methods
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     *
     *
     */
    @Override
            public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }
    
    
    /**
     *
     *
     */
    @Override
            public boolean canCut() {
        return isEditable();
    }
    
    
    /**
     *
     *
     */
    @Override
            public boolean canCopy() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void createPasteTypes(Transferable transferable, List list) {
        if (isValid() && isEditable()) {
            PasteType type = ComponentPasteType.getPasteType(
                    reference.get(), transferable, null);
            if (type != null) {
                list.add(type);
            }
        }
    }

    @Override
    public PasteType getDropType(Transferable transferable, int action, int index) {
        if (isValid() && isEditable()) {
            PasteType type = ComponentPasteType.getDropType(
                    reference.get(), transferable, null, action, index);
            if (type != null) {
                return type;
            }
        }
        return null;
    }

    @Override
            public boolean canDestroy() {
        SchemaComponent component = getReference().get();
        if (component instanceof Schema || !isEditable()) {
            return false;
        }
        return true;
    }
    
    
    /**
     *
     *
     */
    @Override
            public boolean canRename() {
        return supportsRename();
    }
    
    
    /**
     * Indicates if the component is nameable.
     *
     * @return  true if nameable, false otherwise.
     */
    private boolean isNameable() {
        // Need to check the component type instead of the component, to
        // avoid allowing rename of an element reference, in which the
        // implementation extends Nameable.
        return Nameable.class.isAssignableFrom(
                getReference().get().getComponentType());
    }
    
    
    /**
     * Indicates if the component can be renamed.
     *
     * @return  true if nameable, false otherwise.
     */
    private boolean supportsRename() {
        // check if its nameable and editable
        return isNameable() && isEditable();
    }
    
    
    /**
     * Set the name property directly without adjusting the associated model
     *
     */
    private void _setName(String value) {
		// prevent NPE from explorermanager
		if(value==null) value="";
        super.setName(value);
    }
    
    
    /**
     *
     *
     */
    @Override
            public void setName(String value) {
		NamedReferenceable ref = getReferenceable();
		if(ref==null)
		{
			_setName(value);
			if (supportsRename())
			{
				try
				{
					getReference().get().getModel().startTransaction();
					Nameable n = (Nameable)getReference().get();
					n.setName(value);
				}
				finally
				{
    				getReference().get().getModel().endTransaction();
				}
			}
		}
		else
		{
            SharedUtils.locallyRenameRefactor((Nameable)ref, value);
		}
    }
    
    /**
     * Checks for references to this component, and if none are found,
     * remove it from the model.
     */
    public void destroy() throws IOException {
        SchemaModel model = getReference().get().getModel();
        if(model == null) {
            // fix bug 6421899
            // this node might have been deleted from model as a result of
            // deletion of its parent. get model from context and remove
            // listeners. no need to remove it again from model.
            model = getContext().getModel();
            model.removeComponentListener(weakComponentListener);
            model.removePropertyChangeListener(weakModelListener);
        } else {
            model.removeComponentListener(weakComponentListener);
            model.removePropertyChangeListener(weakModelListener);
            
            // Remove the component from the model.
            SchemaComponent component = getReference().get();
            try {
                model.startTransaction();
                model.removeChildComponent(component);
                //need to provide a hook
                cleanup();
            } finally {
                model.endTransaction();
            }
        }
        super.destroy();
    }

    /**
     * This is a hook for the subclasses if they want to do something special in the same transaction.
     * For example, when an import gets deleted, we should also remove the namespace declaration.
     */
    protected void cleanup() {
        //default implementation needs to be empty
        //subclasses should override, if they want do extra stuff inside the same transaction.
        //See AdvancedImportNode for details.
    }
    
    
    /**
     *
     *
     */
    @Override
            protected Sheet createSheet() {
        super.createSheet();
        Sheet sheet=Sheet.createDefault();
        Sheet.Set set=sheet.get(Sheet.PROPERTIES);
        set.put(
                new PropertySupport("kind",String.class,
                NbBundle.getMessage(SchemaComponentNode.class,
                "PROP_SchemaComponentNode_Kind"),
                "",true,false) {
            public Object getValue() {
                return getTypeDisplayName();
            }
            
            public void setValue(Object value) {
                // Not modifiable
            }
        });
        
        try {
            // id property
            Property idProperty = new BaseSchemaProperty((SchemaComponent)getReference().get(),
                    String.class,
                    SchemaComponent.ID_PROPERTY,
                    NbBundle.getMessage(SchemaComponentNode.class,
                    "PROP_SchemaComponentNode_ID"),
                    NbBundle.getMessage(SchemaComponentNode.class,
                    "PROP_SchemaComponentNode_IDDesc"),
                    StringEditor.class
                    ){
                public void setValue(Object o) throws
                        IllegalAccessException, InvocationTargetException {
                    if (o instanceof String) {
                        if("".equals(o)) {
                            super.setValue(null);
                        } else if (Utils.isValidNCName(o.toString())){
                            super.setValue(o);                        
                        } else {
                            String msg = NbBundle.getMessage(BaseSchemaProperty.class, 
                                    "MSG_Neg_Int_Value", o); //NOI18N
                            IllegalArgumentException iae = new IllegalArgumentException(msg);
                            ErrorManager.getDefault().annotate(iae, ErrorManager.USER,
                                    msg, msg, null, new java.util.Date());
                            throw iae;
                        }
                    } else {
                        super.setValue(o);
                    }
                }
            };
            set.put(new SchemaModelFlushWrapper(getReference().get(), idProperty));
        } catch (NoSuchMethodException nsme) {
            assert false: "properties must be defined";
        }
        
        // If we are a named node, display that in the property sheet
        if (isNameable())
            set.put(new PropertySupport.Name(this));
        
        if(hasCustomizer()&&isEditable()) {
            Property structureProp = new PropertySupport.ReadWrite("structure", //NOI18N
                    String.class,
                    NbBundle.getMessage(SchemaComponentNode.class,
                    "PROP_SchemaComponentNode_Customize"),
                    NbBundle.getMessage(SchemaComponentNode.class,
                    "PROP_SchemaComponentNode_Customize_ShortDesc")) {
                public Object getValue() throws IllegalAccessException,InvocationTargetException {
                    return NbBundle.getMessage(SchemaComponentNode.class,
                            "PROP_SchemaComponentNode_Customize_Label");
                }
                public void setValue(Object val) throws IllegalAccessException,IllegalArgumentException,InvocationTargetException {
                }
                public PropertyEditor getPropertyEditor() {
                    return new StructurePropertyEditor();
                }
            };
            set.put(structureProp);
        }

        return sheet;
    }

    /**
     * Indicates if this node should allow reordering of its children.
     * The default implementation allows reordering only if there is
     * more than one child component.
     *
     * @return  true if reordering of this node's children is permitted.
     */
    protected boolean allowReordering() {
        // Check if we have more than one physical child in the model.
        // Using the node children count results in too many index out
        // of bounds exceptions.
        return getReference().get().getChildren().size() > 1;
    }

    @Override
    public Action[] getActions(boolean context) {
        ReadOnlyCookie roc = (ReadOnlyCookie) getContext().getLookup().lookup(
                ReadOnlyCookie.class);
        List<Action> actions = new ArrayList<Action>();
        if (roc != null && roc.isReadOnly()) {
            // Set of actions for read-only components.
            actions.add(SystemAction.get(GoToAction.class));
        } else {
            // Set of actions for modifiable components.
            actions.add(SystemAction.get(CutAction.class));
            actions.add(SystemAction.get(CopyAction.class));
            actions.add(SystemAction.get(PasteAction.class));
            actions.add(null);
            actions.add(SystemAction.get(NewAction.class));
            actions.add(SystemAction.get(DeleteAction.class));
            if (allowReordering()) {
                actions.add(SystemAction.get(ReorderAction.class));
            }
            actions.add(null);
            actions.add(SystemAction.get(GoToAction.class));
            //actions.add(SystemAction.get(FindUsagesAction.class));
            //new action based on new refactoring API
            actions.add(RefactoringActionsFactory.whereUsedAction());
            actions.add(null);
            //actions.add(SystemAction.get(RefactorAction.class));
            //new action based on new refactoring API
            actions.add(RefactoringActionsFactory.editorSubmenuAction());
            actions.add(null);
            actions.add(SystemAction.get(PropertiesAction.class));
        }
        return actions.toArray(new Action[actions.size()]);
    }
    
    @Override
    public Action getPreferredAction() {
        // This exists for use in the Navigator.
        ReadOnlyCookie roc = (ReadOnlyCookie) getCookie(ReadOnlyCookie.class);
        if (roc != null && roc.isReadOnly()) {
            return SystemAction.get(ShowSchemaAction.class);
        }
        return super.getPreferredAction();
    }
    
    
   /**
     * This api returns the factory which gives back new types for this node.
     * Default FactoryImpl provides addition of annotation.
     * Subclasses can override this api to allow addition of their allowed
     * child types.
     */
    protected NewTypesFactory getNewTypesFactory() {
        return new NewTypesFactory();
    }
    
    public final NewType[] getNewTypes() {
        if(isEditable()) {
            return getNewTypesFactory().getNewTypes(getReference(), null);
        }
        return new NewType[] {};
    }

    public GotoType[] getGotoTypes() {
        return GOTO_TYPES;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Listener methods
    ////////////////////////////////////////////////////////////////////////////
    
    public void childrenAdded(ComponentEvent evt) {
        if (isValid()) {
			if(evt.getSource() == getReference().get())
			{
				((RefreshableChildren) getChildren()).refreshChildren();
			}
			if(evt.getSource() == getReference().get() ||
					evt.getSource() == getReference().get().getAnnotation())
			{
				updateShortDescription();
			}
        }
    }
    
    public void childrenDeleted(ComponentEvent evt) {
        if (isValid()) {
			if(evt.getSource() == getReference().get())
			{
				((RefreshableChildren) getChildren()).refreshChildren();
			}
			if(evt.getSource() == getReference().get() ||
					evt.getSource() == getReference().get().getAnnotation())
			{
				updateShortDescription();
			}
        }
    }
    
    public void valueChanged(ComponentEvent evt) {
		if (isValid())
		{
			T component = getReference().get();
			if(evt.getSource() == component)
			{
				updateDisplayName();
			}
			Documentation d = null;
			if(component instanceof Documentation)
				d = (Documentation)component;
			else if(component instanceof Annotation)
			{
				Annotation a = (Annotation)component;
				if(!a.getDocumentationElements().isEmpty())
					d = a.getDocumentationElements().iterator().next();
			} 
			else
			{
				Annotation a = component.getAnnotation();
				if(a!=null && !a.getDocumentationElements().isEmpty())
					d = a.getDocumentationElements().iterator().next();
			}
			if(evt.getSource()==d)
			{
				updateShortDescription();
			}
		}
    }
    
    /**
     * Reacts to granular property change events from model.
     * Updates displayname if needed.
     * Fires properties changed events if needed.
     * Subclasses override if needed.
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (isValid() && event.getSource() == getReference().get()) {
            try {
                updateDisplayName();
                String propName = event.getPropertyName();
                Sheet.Set propertySet = getSheet().get(Sheet.PROPERTIES);
                if(propertySet!=null){
                    if (propertySet.get(propName)!=null) {
                        firePropertyChange(propName,event.getOldValue(),
                                event.getNewValue());
                    }
                    else {
                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
                                propName + " property is not defined in " +
                                getTypeDisplayName());
                    }
                }
            } catch (IllegalStateException ise) {
                // Component is not in the model.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ise);
            } catch (NullPointerException npe) {
                // Does not reproduce reliably, but catch and log regardless.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, npe);
            }
        }
    }

    public Set<Component> getComponents() {
        return referenceSet;
    }
    
    public void highlightAdded(Highlight hl) {
        highlights.add(hl);
        fireDisplayNameChange("TempName", getDisplayName());
    }
    
    public void highlightRemoved(Highlight hl) {
        highlights.remove(hl);
        fireDisplayNameChange("TempName", getDisplayName());
    }
    
    /**
     * Given a display name, add the appropriate HTML tags to highlight
     * the display name as dictated by any Highlights associated with
     * this node.
     *
     * @param  name  current display name.
     * @return  marked up display name.
     */
    protected String applyHighlights(String name) {
        int count = highlights.size();
        if (count > 0) {
            // Apply the last highlight that was added to our list.
            Highlight hl = highlights.get(count - 1);
            String type = hl.getType();
            String code = null;


            if (type.equals(Highlight.SEARCH_RESULT)) {
                code = "e68b2c";
            } else if (type.equals(Highlight.SEARCH_RESULT_PARENT)) {
                code = "ffc73c";
            }  else if (type.equals(Highlight.FIND_USAGES_RESULT_PARENT)) {
                code = "B5E682";    // was c7ff3c chartreuse
            }else if (type.equals(Highlight.FIND_USAGES_RESULT)) {
                code = "8be62c";    // darker green
            }
            name = "<strong><font color=\"#" + code + "\">" + name +
                    "</font></strong>";
        }
        return name;
    }
    
    public String getDisplayName() {
        String instanceName = getDefaultDisplayName();
        return instanceName.length()==0 ? instanceName : 
	       instanceName + " " + "[" + getTypeDisplayName() + "]"; // NOI18N
    }
    
    public String getDefaultDisplayName() {
	String instanceName = super.getDisplayName();
        return instanceName == null || instanceName.length() == 0
	    ? "" : instanceName; 
    }
    
    public String getHtmlDisplayName() {
        String name = getDefaultDisplayName();
        // Need to escape any HTML meta-characters in the name.
        if(name!=null)
            name = name.replace("<", "&lt;").replace(">", "&gt;");
        return applyHighlights(name);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////
    
    private class StructurePropertyEditor extends PropertyEditorSupport
            implements ExPropertyEditor {
        public boolean supportsCustomEditor() {
            return true;
        }
        
        public java.awt.Component getCustomEditor() {
            return getCustomizer();
        }
        
        public void attachEnv(PropertyEnv env ) {
            FeatureDescriptor desc = env.getFeatureDescriptor();
            desc.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        }
    }

    private SchemaUIContext context;
    private SchemaComponentReference<T> reference;
    private Set<Component> referenceSet;
    /** Ordered list of highlights applied to this node. */
    private List<Highlight> highlights;
    private InstanceContent lookupContents;
    private PropertyChangeListener modelListener;
    private PropertyChangeListener weakModelListener;
    private ComponentListener weakComponentListener;
    private SoftReference<Customizer> custRef;
    private static final GotoType[] GOTO_TYPES = new GotoType[] {
        new SourceGotoType(),
        new SchemaGotoType(),
        new DesignGotoType(),
        new SuperGotoType(),
    };

    /**
     * Implement ReferenceableProvider
     * 
     * 
     * @returns NamedReferenceable used by Refactoring Find Usage, Safe Delete, 
     *          and Rename
     */
    public NamedReferenceable getReferenceable() {
        SchemaComponent comp = reference.get();
        if (comp instanceof NamedReferenceable && isValid() && comp.getModel().
                getModelSource().getLookup().lookup(FileObject.class) != null){
            return NamedReferenceable.class.cast(comp);
        }
        return null;
    }
    
    /**
     * This api is used to set the back pointer to the ReadOnlySchemaComponentNode,
     * which represents this node on UI in case of refrenced components.
     */
    public void setReferencingNode(final Node referencingNode) {
        getLookupContents().add(
                new ReferencingNodeProvider() {
            public Node getNode() {
                return referencingNode;
            }
        });
    }
}
