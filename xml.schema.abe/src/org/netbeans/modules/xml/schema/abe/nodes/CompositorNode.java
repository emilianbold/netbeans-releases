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

package org.netbeans.modules.xml.schema.abe.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.Compositor.CompositorType;
import org.netbeans.modules.xml.schema.abe.CompositorPanel;
import org.netbeans.modules.xml.schema.abe.InstanceUIContext;
import org.netbeans.modules.xml.schema.abe.nodes.properties.*;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 *
 * @author Ayub Khan
 */
public class CompositorNode extends ABEAbstractNode {
    
    
    /**
     * Creates a new instance of CompositorNode
     */
    public CompositorNode(Compositor compositor, InstanceUIContext context) {
        super(compositor, context);
    }
    
    public CompositorNode(Compositor compositor) {
        super(compositor, new ABENodeChildren(compositor));
        CompositorType type = compositor.getType();
        switch (type) {
            case ALL: {
                setIconBaseWithExtension(
                        "org/netbeans/modules/xml/schema/abe/resources/all.png");
                break;
            }
            case CHOICE: {
                setIconBaseWithExtension(
                        "org/netbeans/modules/xml/schema/abe/resources/choice.png");
                break;
            }
            case SEQUENCE: {
                setIconBaseWithExtension(
                        "org/netbeans/modules/xml/schema/abe/resources/sequence.png");
                break;
            }
            default :
                assert false;
        }
    }
    
    
    public void propertyChange(PropertyChangeEvent evt) {
        if ( (evt.getSource() == getAXIComponent()) &&
                evt.getPropertyName().equals(Compositor.PROP_TYPE)) {
            Object oldValue = evt.getOldValue();
            String oldDisplayName = oldValue == null ? null : oldValue.toString();
            fireDisplayNameChange(oldDisplayName, getDisplayName());
        }
    }
    
    protected void populateProperties(Sheet sheet) {
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        if(set == null) {
            set = sheet.createPropertiesSet();
        }
        
        try {
            //compositor type property
            Node.Property useProp = new BaseABENodeProperty(
                    getAXIComponent(),
                    CompositorType.class, //as value type
                    "type", //property name
                    NbBundle.getMessage(CompositorNode.class,"PROP_CompositorNode_Name"), // display name
                    NbBundle.getMessage(CompositorNode.class,"PROP_CompositorNode_NameDesc"),	// descr
                    CompositorTypeEditor.class
                    );
            set.put(new SchemaModelFlushWrapper(getAXIComponent(),useProp, getContext()){
                public java.beans.PropertyEditor getPropertyEditor() {
                    java.beans.PropertyEditor ped = super.getPropertyEditor();
                    if(ped instanceof CompositorNode.CompositorTypeEditor){
                        ((CompositorNode.CompositorTypeEditor)ped).setCompositor((Compositor)getAXIComponent());
                    }
                    return ped;
                }
            });
            
            if(getAXIComponent().supportsCardinality()) {
                // minOccurs
                Property minOccursProp = new MinOccursProperty(
                        getAXIComponent(),
                        String.class,
                        Compositor.PROP_MINOCCURS,
                        NbBundle.getMessage(CompositorNode.class,"PROP_MinOccurs_DisplayName"), // display name
                        NbBundle.getMessage(CompositorNode.class,"PROP_MinOccurs_ShortDescription")	// descr
                        );
                set.put(new SchemaModelFlushWrapper(getAXIComponent(), minOccursProp, getContext()));
                
                // maxOccurs
                if (((Compositor)getAXIComponent()).allowsFullMultiplicity()) {
                    Property maxOccursProp = new BaseABENodeProperty(
                            getAXIComponent(),
                            String.class,
                            Compositor.PROP_MAXOCCURS,
                            NbBundle.getMessage(CompositorNode.class,"PROP_MaxOccurs_DisplayName"), // display name
                            NbBundle.getMessage(CompositorNode.class,"PROP_MaxOccurs_ShortDescription"),	// descr
                            MaxOccursEditor.class
                            );
                    set.put(new SchemaModelFlushWrapper(getAXIComponent(), maxOccursProp, getContext()));
                }
            }
        } catch (Exception ex) {
        }
        
        sheet.put(set);
    }
    
    public String getName(){
        if((Compositor) super.getAXIComponent() != null &&
                ((Compositor) super.getAXIComponent()).getType() != null)
            return ((Compositor) super.getAXIComponent()).getType().getName();
        else
            return "";
    }
    
    protected String getTypeDisplayName() {
        return NbBundle.getMessage(AttributeNode.class,"LBL_Compositor");
    }

    
    public static class CompositorTypeEditor extends PropertyEditorSupport {
        
        /**
         * Creates a new instance of CompositorTypeEditor
         */
        Compositor comp;
        public CompositorTypeEditor() {
        }
        
        public String[] getTags() {
            /*return new String[] {
                Compositor.CompositorType.SEQUENCE.getName(),
                Compositor.CompositorType.CHOICE.getName(),
                Compositor.CompositorType.ALL.getName()
            };*/
            CompositorType[] types = CompositorPanel.filterAllIfNeeded(comp);
            String ret[] = new String[types.length];
            for(int i = 0; i<types.length; i++)
                ret[i] = types[i].getName();
            return ret;
            
        }
        
        public void setAsText(String text) throws IllegalArgumentException {
            if (text.equals(Compositor.CompositorType.SEQUENCE.getName())){
                setValue(CompositorType.SEQUENCE);
            } else if (text.equals(Compositor.CompositorType.CHOICE.getName())){
                setValue(CompositorType.CHOICE);
            } else if (text.equals(Compositor.CompositorType.ALL.getName())){
                setValue(CompositorType.ALL);
            }
        }
        
        public void setCompositor(Compositor comp){
            this.comp = comp;
        }
        
        
        public Object getValue(){
            return super.getValue();
        }
        
        public void setValue(Object obj){
            super.setValue(obj);
        }
        
        public void setSource(Object obj){
            super.setSource(obj);
        }
        public String getAsText() {
            Object val = getValue();
            if (val instanceof CompositorType){
                return val.toString();
            }
            // TODO how to display invalid values?
            return NbBundle.getMessage(CompositorNode.class,"LBL_Empty");
        }
        
    }
}
