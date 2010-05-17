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
