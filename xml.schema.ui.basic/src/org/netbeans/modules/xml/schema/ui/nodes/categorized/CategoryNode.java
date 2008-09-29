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

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.ui.nodes.DefaultExpandedCookie;
import org.netbeans.modules.xml.schema.ui.nodes.ReadOnlyCookie;
import org.netbeans.modules.xml.schema.ui.nodes.RefreshableChildren;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.ui.ComponentPasteType;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.netbeans.modules.xml.xam.ui.cookies.CountChildrenCookie;
import org.netbeans.modules.xml.xam.ui.highlight.Highlight;
import org.netbeans.modules.xml.xam.ui.highlight.Highlighted;
import org.netbeans.modules.xml.xam.ui.highlight.HighlightManager;
import org.openide.actions.NewAction;
import org.openide.actions.PasteAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
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
 */
public class CategoryNode extends AbstractNode
        implements Node.Cookie, ComponentListener, Highlighted,
        CountChildrenCookie {

	/**
	 *
	 *
	 */
	public CategoryNode(SchemaUIContext context, 
		SchemaComponentReference<? extends SchemaComponent> parentReference,
		Class<? extends SchemaComponent> childType, 
		RefreshableChildren children)
	{
		this(context,parentReference,childType,children,new InstanceContent());
	}


	/**
	 * Constructor HACK to allow creation of our own lookup
	 *
	 */
	private CategoryNode(SchemaUIContext context, 
		SchemaComponentReference<? extends SchemaComponent> parentReference,
		Class<? extends SchemaComponent> childType, 
		Children children,
		InstanceContent contents)
	{
		super(children, createLookup(context, contents));
		this.context=context;
		this.reference=parentReference;
		this.childType=childType;
		this.lookupContents=contents;

		// Add various objects to the lookup
                try {
                    // Include the data object in order for the Navigator to
                    // show the structure of the current document.
                    FileObject fobj = (FileObject) reference.get().getModel().
                            getModelSource().getLookup().lookup(FileObject.class);
                    if (fobj != null) {
                        contents.add(DataObject.find(fobj));
                    }
                } catch (DataObjectNotFoundException donfe) {
                }
		contents.add(this);
		contents.add(context);

		// Expand all nodes except the "All" node by default
		contents.add(new DefaultExpandedCookie(true));
                // For the 'All Components' node, allow reordering of the nodes.
                if (childType.equals(SchemaComponent.class) &&
                        children instanceof Index) {
                    contents.add(children);
                }

		// Get the name based on the child types this node will show
		String name=childType.getName();
		// Find a friendly name
		try
		{
			name = NbBundle.getMessage(CategoryNode.class,
				"LBL_CategoryNode_" + name);
		}
		catch (MissingResourceException e)
		{
			assert false: e;
		}
		
		setName(name);
		setDisplayName(name);

		// TODO: Need to allow the children object to do the work here
                SchemaModel model = parentReference.get().getModel();
                weakComponentListener = (ComponentListener) WeakListeners.create(
                        ComponentListener.class, this, model);
                model.addComponentListener(weakComponentListener);

                referenceSet = new HashSet<Component>();
                highlights = new LinkedList<Highlight>();
                HighlightManager hm = HighlightManager.getDefault();
                // Must check for the existence of the highlight manager
                // since the component selection panel does not highlight.
                SchemaComponent comp = (SchemaComponent) reference.get();
                List<? extends SchemaComponent> subcomps = comp.getChildren(childType);
                Iterator<? extends SchemaComponent> iter = subcomps.iterator();
                while (iter.hasNext()) {
                    referenceSet.add(iter.next());
                }
                hm.addHighlighted(this);
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
                Lookups.exclude(context.getLookup(), new Class[] {
                    Node.class,
                    DataObject.class,
                }),
                new AbstractLookup(contents),
            });
        }

        @Override
        public boolean equals(Object o) {
            // Without this, the tree view collapses when nodes are changed.
            if (o instanceof CategoryNode) {
                CategoryNode cn = (CategoryNode) o;
                String name = getName();
                String oname = cn.getName();
                if (name != null && oname != null) {
                    SchemaComponentReference scr = cn.getReference();
                    return name.equals(oname) && scr.equals(reference);
                }
            }
            return false;
        }

	/**
	 *
	 *
	 */
	public int hashCode() {
		// Without this, the tree view collapses when nodes are changed.
		return getName().hashCode();
	}

        public int getChildCount() {
            return getReference().get().getChildren(getChildType()).size();
        }

	/**
	 *
	 *
	 */
	public SchemaUIContext getContext()
	{
		return context;
	}


	/**
	 *
	 *
	 */
	public SchemaComponentReference<? extends SchemaComponent> getReference()
	{
		return reference;
	}


	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getChildType()
	{
		return childType;
	}


	/**
	 * Returns the contents of the lookup.  All cookies and other objects that
	 * should be findable via the lookup should be added to this.
	 *
	 */
	protected InstanceContent getLookupContents()
	{
		return lookupContents;
	}


	/**
	 *
	 *
	 */
	public boolean isDefaultExpanded()
	{
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
	public void setDefaultExpanded(boolean value)
	{
		DefaultExpandedCookie cookie=(DefaultExpandedCookie)
			getCookie(DefaultExpandedCookie.class);
		if (cookie!=null)
			cookie.setDefaultExpanded(value);
	}




	////////////////////////////////////////////////////////////////////////////
	// Node methods
	////////////////////////////////////////////////////////////////////////////

	/**
	 *
	 *
	 */
	@Override
	public HelpCtx getHelpCtx()
	{
		return new HelpCtx(getClass());
	}


	/**
	 *
	 *
	 */
	@Override
	public boolean canCut()
	{
		return false;
	}


	/**
	 *
	 *
	 */
	@Override
	public boolean canCopy()
	{
		return false;
	}

        @SuppressWarnings("unchecked")
        protected void createPasteTypes(Transferable transferable, List list) {
            if (isValid() && isEditable()) {
                PasteType type = ComponentPasteType.getPasteType(
                        reference.get(), transferable, childType);
                if (type != null) {
                    list.add(type);
                }
            }
        }

        @Override
        public PasteType getDropType(Transferable transferable, int action, int index) {
            if (isValid() && isEditable()) {
                PasteType type = ComponentPasteType.getDropType(
                        reference.get(), transferable, childType, action, index);
                if (type != null) {
                    return type;
                }
            }
            return null;
        }

	/**
	 *
	 *
	 */
	@Override
	public boolean canDestroy()
	{
		return false;
	}


	/**
	 *
	 *
	 */
	@Override
	public boolean canRename()
	{
		return false;
	}


	/**
	 *
	 *
	 */
	@Override
        public Action[] getActions(boolean b)
        {
            ReadOnlyCookie roc = (ReadOnlyCookie) getContext().getLookup().lookup(
                    ReadOnlyCookie.class);
            if (roc != null && roc.isReadOnly()) {
                return ACTIONS_READONLY;
            } else {
                return ACTIONS;
            }
        }


	/**
	 *
	 *
	 */
	public NewType[] getNewTypes()
	{
		SchemaModel model = getReference().get().getModel();
		if(model!=null && isEditable())
		{
			return new AdvancedNewTypesFactory().getNewTypes(getReference(), 
					getChildType());
		}
		return new NewType[0];
	}




	////////////////////////////////////////////////////////////////////////////
	// Listener methods
	////////////////////////////////////////////////////////////////////////////

        public boolean isValid() {
            return getReference().get() != null && getReference().get().getModel() != null;
        }
        
    protected boolean isEditable() {
        SchemaModel model = getReference().get().getModel();
        return model != null && model == getContext().getModel() && 
				XAMUtils.isWritable(model);
    }
    
	/**
	 *
	 *
	 */
	public void childrenAdded(ComponentEvent evt) {
             if (! isValid()) return;
		if (evt.getSource() == getReference().get()) {
			((RefreshableChildren) getChildren()).refreshChildren();
		}
	}


	/**
	 *
	 *
	 */
	public void childrenDeleted(ComponentEvent evt) {
             if (! isValid()) return;
		if (evt.getSource() == getReference().get()) {
			((RefreshableChildren) getChildren()).refreshChildren();
		}
	}


	/**
	 *
	 *
	 */
	public void valueChanged(ComponentEvent evt) {
             if (! isValid()) return;
		// Do nothing
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
                String code = null;
                Highlight hl = highlights.get(count - 1);
                String type = hl.getType();
                if (type.equals(Highlight.SEARCH_RESULT) ||
                        type.equals(Highlight.SEARCH_RESULT_PARENT)) {
                    // Always use the parent color for search results, as
                    // a category cannot possibly be a search result.
                    code = "ffc73c";
                }
                else if (type.equals(Highlight.FIND_USAGES_RESULT) ||
                        type.equals(Highlight.FIND_USAGES_RESULT_PARENT)) {
                    // Always use the parent color for search results, as
                    // a category cannot possibly be a search result.
                    // color = chartreuse
                    code = "c7ff3c";
                }
                
                name = "<strong><font color=\"#" + code + "\">" + name +
                    "</font></strong>";
            }
            return name;
        }

	private Node getFolderNode() {
	    FileObject fo =
		Repository.getDefault().getDefaultFileSystem().getRoot();
	    Node n = null;
	    try {
		DataObject dobj = DataObject.find(fo);
		n = dobj.getNodeDelegate();
	    } catch (DataObjectNotFoundException ex) {
		// cannot get the node for this, this shouldn't happen
		// so just ignore
	    }
	    
	    return n;
	}
	
	@Override
	public Image getIcon(int type) {
	    Node n = getFolderNode();
	    Image i = super.getIcon(type);
	    if (n != null) {
		i = n.getIcon(type);
	    }
	    return badgeImage(i);
	}

	@Override
	public Image getOpenedIcon(int type) {
	    Node n = getFolderNode();
	    Image i = super.getOpenedIcon(type);
	    if (n != null) {
		i = n.getOpenedIcon(type);
	    }
	    return badgeImage(i);
	}
	
	private Image badgeImage(Image main) {
	    Image rv = main;
	    if (badge != null) {
		Image badgeImage = ImageUtilities.loadImage(badge);
		rv = ImageUtilities.mergeImages(main, badgeImage, 8, 8);
	    }
	    return rv;
	}
	
	public void setBadge(String badge) {
	    this.badge = badge;
	}
	
        public String getHtmlDisplayName() {
            String name = getDisplayName();
            // Need to escape any HTML meta-characters in the name.
            name = name.replace("<", "&lt;").replace(">", "&gt;");
            return applyHighlights(name);
        }

	////////////////////////////////////////////////////////////////////////////
	// Class members
	////////////////////////////////////////////////////////////////////////////

	private static final SystemAction[] ACTIONS=
		new SystemAction[]
		{
                        SystemAction.get(PasteAction.class),
                        null,
			SystemAction.get(NewAction.class),
		};

        private static final SystemAction[] ACTIONS_READONLY =
                new SystemAction[] {
        };

	////////////////////////////////////////////////////////////////////////////
	// Instance variables
	////////////////////////////////////////////////////////////////////////////

	private SchemaUIContext context;
	private SchemaComponentReference<? extends SchemaComponent> reference;
        private Set<Component> referenceSet;
        /** Ordered list of highlights applied to this node. */
        private List<Highlight> highlights;
	private Class<? extends SchemaComponent> childType;
	private InstanceContent lookupContents;
        private ComponentListener weakComponentListener;
	private String badge;
	
}
