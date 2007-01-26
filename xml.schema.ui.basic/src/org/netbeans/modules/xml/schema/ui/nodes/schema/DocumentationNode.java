/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.schema.ui.nodes.schema;

import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;
import javax.swing.Action;
import org.netbeans.modules.xml.schema.ui.basic.SchemaSettings;
import org.openide.actions.CustomizeAction;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.netbeans.modules.xml.xam.ui.customizer.CustomizerProvider;
import org.netbeans.modules.xml.schema.ui.basic.editors.StringEditor;
import org.netbeans.modules.xml.schema.ui.nodes.ReadOnlyCookie;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaModelFlushWrapper;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.DocumentationCustomizer;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BaseSchemaProperty;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.NamespaceProperty;
import org.openide.nodes.PropertySupport;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 * @author  Jeri Lockhart
 * @author  Ajit Bhate
 */
public class DocumentationNode extends SchemaComponentNode<Documentation> {
    /**
     *
     *
     */
    public DocumentationNode(SchemaUIContext context,
            SchemaComponentReference<Documentation> reference,
            Children children) {
        super(context,reference,children);
        
        setIconBaseWithExtension(
                "org/netbeans/modules/xml/schema/ui/nodes/resources/documentation.png");
    }
    
    
    public String getHtmlDisplayName() {
        String language = getReference().get().getLanguage();
        String retValue = super.getDefaultDisplayName();
        if(language!=null) {
            retValue = retValue +" <font color='#999999'>"+"(" + language + ")"+"</font>";
        }
        return retValue;
    }
    
    /**
     *
     *
     */
    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(DocumentationNode.class,
                "LBL_DocumentationNode_TypeDisplayName"); // NOI18N
    }
    
    
    public void propertyChange(PropertyChangeEvent event) {
        if(!isValid()) return;
        super.propertyChange(event);
        String property = event.getPropertyName();
        if(Documentation.LANGUAGE_PROPERTY.equals(event.getPropertyName()) &&
                event.getSource() == getReference().get() ) {
            fireDisplayNameChange(null,getDisplayName());
        }
    }
    
    @Override
    protected Sheet createSheet() {
        Sheet sheet = null;
        //sheet = super.createSheet();
        sheet = Sheet.createDefault();
        Sheet.Set props = sheet.get(Sheet.PROPERTIES);
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
        if (props == null) {
            props = Sheet.createPropertiesSet();
            sheet.put(props);
        }
        
        try {
            // Source (URI)
            Node.Property uriProp = new NamespaceProperty(
                    getReference().get(),
                    Documentation.SOURCE_PROPERTY,			// name
                    NbBundle.getMessage(DocumentationNode.class,"PROP_URI_DisplayName"), // display name
                    NbBundle.getMessage(DocumentationNode.class,"HINT_URI")	// descr
                    ,null) {
                @Override
                public java.beans.PropertyEditor getPropertyEditor() {
                    return new StringEditor();
                }
            };
            props.put(new SchemaModelFlushWrapper(getReference().get(), uriProp));
            
            // Language
            Node.Property langProp = new BaseSchemaProperty(
                    getReference().get(),
                    String.class,		// type
                    Documentation.LANGUAGE_PROPERTY,			// name
                    NbBundle.getMessage(DocumentationNode.class,"PROP_Language_DisplayName"), // display name
                    NbBundle.getMessage(DocumentationNode.class,"HINT_Language")	// descr
                    , LanguageEditor.class) {
                public void setValue(Object o) throws
                        IllegalAccessException, InvocationTargetException {
                    if(o==null) {
                        super.setValue(null);
                        return;
                    }
                    if(o instanceof String) {
                        String lang = (String)o;
                        if(Pattern.matches(
                                "([a-zA-Z]{2}|[iI]-[a-zA-Z]+|[xX]-[a-zA-Z]{1,8})(-[a-zA-Z]{1,8})*"
                                ,lang)) {
                            super.setValue(o);
                            SchemaSettings.getDefault().setLanguage(lang);
                            return;
                        }
                    }
                    String msg = NbBundle.getMessage(DocumentationNode.class,
                            "MSG_Invalid_Language",o); //NOI18N
                    IllegalArgumentException iae = new IllegalArgumentException(msg);
                    ErrorManager.getDefault().annotate(iae, ErrorManager.USER,
                            msg, msg, null, new java.util.Date());
                    throw iae;
                }
            };
            
            props.put(new SchemaModelFlushWrapper(getReference().get(),langProp));
            
            // Text
            Node.Property textProp = new BaseSchemaProperty(
                    getReference().get(),
                    String.class,		// type
                    Documentation.CONTENT_PROPERTY,				// name
                    NbBundle.getMessage(DocumentationNode.class,"PROP_Text_DisplayName"), // display name
                    NbBundle.getMessage(DocumentationNode.class,"HINT_Text")	// descr
                    , null) {
                public Object getValue() {
                    return getReference().get().getContentFragment();
                }
                public PropertyEditor getPropertyEditor() {
                    if (hasCustomizer()) {
                        return new PropertyEditorSupport() {
                            public boolean supportsCustomEditor() {
                                return true;
                            }
                            public java.awt.Component getCustomEditor() {
                                return getCustomizer();
                            }
                            
                        };
                    }
                    return super.getPropertyEditor();
                }
            };
            
            props.put(new SchemaModelFlushWrapper(getReference().get(),textProp));
            
            props.remove("structure");
            
        } catch (NoSuchMethodException nsme) {
            assert false:"properties must be defined";
        }
        return sheet;
    }
    
    @Override
    public Action getPreferredAction() {
        ReadOnlyCookie roc = (ReadOnlyCookie) getContext().getLookup().lookup(
                ReadOnlyCookie.class);
        if (roc == null || !roc.isReadOnly()) {
            return SystemAction.get(CustomizeAction.class);
        }
        return super.getPreferredAction();
    }
    
    
    protected CustomizerProvider getCustomizerProvider() {
        return new CustomizerProvider() {
            public Customizer getCustomizer() {
                return new DocumentationCustomizer(getReference());
            }
        };
    }
    
    public boolean hasCustomizer() {
        return isEditable();
    }
    
    public static class LanguageEditor  extends StringEditor
            implements ExPropertyEditor {
        /**
         * Creates a new instance of LanguageEditor
         */
        public LanguageEditor() {
        }
        
        private static String[] languages = {"en","en-US","en-GB"};
        public String[] getTags() {
            return languages;
        }
        
        public boolean isPaintable() {
            return false;
        }
        /**
         *
         *  implement ExPropertyEditor
         *
         */
        public void attachEnv(PropertyEnv env ) {
            FeatureDescriptor desc = env.getFeatureDescriptor();
            // make this an editable combo tagged editor
            desc.setValue("canEditAsText", Boolean.TRUE); // NOI18N
        }
    }
}
