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

package org.netbeans.modules.xml.schema.ui.nodes.schema;

import java.beans.PropertyEditorSupport;
import java.util.MissingResourceException;
import org.netbeans.modules.xml.schema.model.Whitespace;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BaseSchemaProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BooleanProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class WhitespaceNode extends SchemaComponentNode<Whitespace>
{
    private static final String NAME = "whitespace";

    /**
     *
     *
     */
    public WhitespaceNode(SchemaUIContext context, 
		SchemaComponentReference<Whitespace> reference,
		Children children)
    {
        super(context,reference,children);
    }


	/**
	 *
	 *
	 */
	@Override
	public String getTypeDisplayName()
	{
		return NbBundle.getMessage(WhitespaceNode.class,
			"LBL_WhitespaceNode_TypeDisplayName"); // NOI18N
	}
	
	
        @Override
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            Sheet.Set set = sheet.get(Sheet.PROPERTIES);
            try {
                //fixed property
                Property fixedProp = new BooleanProperty(
                        getReference().get(), // schema component
                        Whitespace.FIXED_PROPERTY, // property name
                        NbBundle.getMessage(WhitespaceNode.class,"PROP_Facet_Fixed_DisplayName"), // display name
                        NbBundle.getMessage(WhitespaceNode.class,"PROP_Facet_Fixed_ShortDescription"),	// descr
                        true // default value is false
                        );
                set.put(new SchemaModelFlushWrapper(getReference().get(), fixedProp));

                //value property
                Property valueProp = new BaseSchemaProperty(
                        getReference().get(), // schema component
                        Whitespace.Treatment.class, // value type
                        Whitespace.VALUE_PROPERTY, // property name
                        NbBundle.getMessage(WhitespaceNode.class,"PROP_Whitespace_Value_DisplayName"), // display name
                        NbBundle.getMessage(WhitespaceNode.class,"PROP_Whitespace_Value_ShortDescription"),	// descr
                        WhitespaceValueEditor.class // editor class
                        ){
                            public boolean supportsDefaultValue() { // does not support default
                                return false;
                            }
                };
                set.put(new SchemaModelFlushWrapper(getReference().get(), valueProp));
            } catch (NoSuchMethodException nsme) {
                assert false : "properties should be defined";
            }
            return sheet;
        }
        
        public static class WhitespaceValueEditor extends PropertyEditorSupport {
            
            /**
             * Creates a new instance of ProcessContentsEditor
             */
            public WhitespaceValueEditor() {
            }
            
            public String[] getTags() {
                return new String[] {NbBundle.getMessage(WhitespaceNode.class,"LBL_WhitespacePreserve"),
                NbBundle.getMessage(WhitespaceNode.class,"LBL_WhitespaceReplace"),
                NbBundle.getMessage(WhitespaceNode.class,"LBL_WhitespaceCollapse")};
            }
            
            public void setAsText(String text) throws IllegalArgumentException {
                if (text.equals(NbBundle.getMessage(WhitespaceNode.class,"LBL_WhitespacePreserve"))){
                    setValue(Whitespace.Treatment.PRESERVE);
                } else if (text.equals(NbBundle.getMessage(WhitespaceNode.class,"LBL_WhitespaceReplace"))){
                    setValue(Whitespace.Treatment.REPLACE);
                } else if (text.equals(NbBundle.getMessage(WhitespaceNode.class,"LBL_WhitespaceCollapse"))){
                    setValue(Whitespace.Treatment.COLLAPSE);
                }
            }
            
            public String getAsText() {
                Object val = getValue();
                if (val instanceof Whitespace.Treatment){
                    if (Whitespace.Treatment.PRESERVE.equals(val)) {
                        return NbBundle.getMessage(WhitespaceNode.class,"LBL_WhitespacePreserve");
                    } else if (Whitespace.Treatment.REPLACE.equals(val)) {
                        return NbBundle.getMessage(WhitespaceNode.class,"LBL_WhitespaceReplace");
                    } else if (Whitespace.Treatment.COLLAPSE.equals(val)) {
                        return NbBundle.getMessage(WhitespaceNode.class,"LBL_WhitespaceCollapse");
                    }
                }
                // This should never happen
                return NbBundle.getMessage(WhitespaceNode.class,"LBL_Empty");
            }
        }
}
