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
