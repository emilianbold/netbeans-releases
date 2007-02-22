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

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import java.lang.ref.SoftReference;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.xam.ui.column.Column;
import org.netbeans.modules.xml.xam.ui.column.ColumnProvider;
import org.netbeans.modules.xml.xam.ui.customizer.CustomizerProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * @author  Ajit Bhate
 */
public class DetailsNode extends AbstractNode
{
	
	private CustomizerProvider provider;
	/**
	 * If readlony
	 */
	private boolean readonly;
	
	
	public DetailsNode(SchemaUIContext context, CustomizerProvider provider)
	{
		this(context,provider,new InstanceContent());
	}
	
	private DetailsNode(SchemaUIContext context,
			CustomizerProvider provider,
			InstanceContent contents)
	{
		
		super(Children.LEAF, createLookup(context, contents));
		setDisplayName(getTypeDisplayName());
		setIconBaseWithExtension(
                    "org/netbeans/modules/xml/schema/ui/nodes/resources/"+
                    "XML-Schema-element-details.png");
		
		this.provider = provider;
		
		contents.add(this);
		contents.add(context);
		contents.add(provider);
		contents.add(
		new ColumnProvider()
		{
			public Column getColumn()
			{
				DetailsColumn column=columnRef!=null ?
					columnRef.get() : null;
				if (column==null)
				{
					column=createColumn();
					columnRef=new SoftReference<DetailsColumn>(column);
				}
				column.setReadOnly(isReadOnly());
				return column;
			}
			
			private SoftReference<DetailsColumn> columnRef;
		});
                try {
                    // Include the data object in order for the Navigator to
                    // show the structure of the current document.
                    FileObject fobj = (FileObject) context.getModel().
                            getModelSource().getLookup().lookup(FileObject.class);
                    if (fobj != null) {
                        contents.add(DataObject.find(fobj));
                    }
                } catch (DataObjectNotFoundException donfe) {
                }
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

	public boolean isReadOnly()
	{
		return readonly;
	}

	public void setReadOnly(boolean readonly)
	{
		this.readonly = readonly;
	}
	
	/**
	 * create column
	 */
	protected DetailsColumn createColumn()
	{
		return new DetailsColumn(provider.getCustomizer());
	}
	
	
	
	/**
	 * return display name
	 */
	public String getTypeDisplayName()
	{
		return NbBundle.getMessage(DetailsNode.class, "LBL_DetailsNode"); // NOI18N
	}

	public int hashCode()
	{
		return provider.hashCode();
	}

}
