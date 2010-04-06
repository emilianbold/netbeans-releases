/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.xml.xam.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.ui.cookies.GetComponentCookie;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 * Common utilities for XAM user interface module.
 *
 * @author Nam Nguyen
 * @author Nathan Fiedler
 */
public class XAMUtils {

    /**
     * Retrieve the XAM component for the given node.
     *
     * @param  node  node from which to acquire component.
     * @return  the model component, or null if none.
     */
    public static Component getComponent(Node node) {
        GetComponentCookie cake = (GetComponentCookie) node.getCookie(
                GetComponentCookie.class);
        Component component = null;
        try {
            if (cake != null) {
                component = cake.getComponent();
            }
        } catch (IllegalStateException ise) {
            // Happens if the component is no longer in the model.
            // Ignore this here since the caller will deal with it.
        }
        if (component == null) {
            component = (Component) node.getLookup().lookup(Component.class);
        }
        return component;
    }

    /**
     * Retrieve the cookie for showing the component in the editor.
     *
     * @param  comp   component to be shown.
     * @param  view   the desired view in which to show the component.
     * @return  the cookie to view the component.
     */
    public static ViewComponentCookie getViewCookie(Component comp,
            ViewComponentCookie.View view) {
        if (comp == null) {
            return null;
        }
        try {
            Model model = comp.getModel();
            if (model != null) {
                FileObject fobj = (FileObject) model.getModelSource().
                        getLookup().lookup(FileObject.class);
                if (fobj != null) {
                    DataObject dobj = DataObject.find(fobj);
                    if (dobj != null) {
                        ViewComponentCookie cake = (ViewComponentCookie) dobj.
                                getCookie(ViewComponentCookie.class);
                        if (cake != null && cake.canView(view, comp)) {
                            return cake;
                        }
                    }
                }
            }
        } catch (DataObjectNotFoundException donfe) {
            // fall through to return null
        }
        return null;
    }

    /**
     * Determine if the given model is writable, which requires that the
     * source file also be writable.
     *
     * @param  model  the model to be tested.
     * @return  true if model source is editable and the source file is writable.
     */
    public static boolean isWritable(Model model) {
        if (model != null) {
            ModelSource ms = model.getModelSource();
            if (ms.isEditable()) {
                FileObject fo = (FileObject) ms.getLookup().lookup(FileObject.class);
                if (fo != null) {
                    return fo.canWrite();
                }
            }
        }
        return false;
    }

    /**
     * A wrapper listener. It guarantees that the event processing
     * will be executed in the Event Dispatch thread.
     *
     * WARNING! Hold the instance somewhere if you are going to wrap it
     * with the WeakListener. Otherwise it will be garbage collected.
     */
    public static class AwtPropertyChangeListener implements PropertyChangeListener {

        private PropertyChangeListener mListener;

        public AwtPropertyChangeListener(PropertyChangeListener listener) {
            mListener = listener;
        }

        public void propertyChange(final PropertyChangeEvent evt) {
            if (SwingUtilities.isEventDispatchThread()) {
                mListener.propertyChange(evt);
            }
            else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        mListener.propertyChange(evt);
                    }
                });
            }
        }

    }

    /**
     * A wrapper listener. It guarantees that the event processing
     * will be executed in the Event Dispatch thread.
     * 
     * WARNING! Hold the instance somewhere if you are going to wrap it
     * with the WeakListener. Otherwise it will be garbage collected.
     */
    public static class AwtComponentListener implements ComponentListener {

        private ComponentListener mListener;

        public AwtComponentListener(ComponentListener listener) {
            mListener = listener;
        }

        public void valueChanged(final ComponentEvent evt) {
            if (SwingUtilities.isEventDispatchThread()) {
                mListener.valueChanged(evt);
            }
            else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        mListener.valueChanged(evt);
                    }
                });
            }
        }

        public void childrenAdded(final ComponentEvent evt) {
            if (SwingUtilities.isEventDispatchThread()) {
                mListener.childrenAdded(evt);
            }
            else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        mListener.childrenAdded(evt);
                    }
                });
            }
        }

        public void childrenDeleted(final ComponentEvent evt) {
            if (SwingUtilities.isEventDispatchThread()) {
                mListener.childrenAdded(evt);
            }
            else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        mListener.childrenAdded(evt);
                    }
                });
            }
        }

    }

    public static String getDisplayName(SchemaComponent type) {
        if ( !(type instanceof GlobalSimpleType)) {
            return getTypeName(type);
        }
        GlobalSimpleType simpleType = (GlobalSimpleType) type;
        NamedComponentReference<GlobalSimpleType> ref = simpleType.createReferenceTo(simpleType, GlobalSimpleType.class);

        return ref.getRefString();
    }

    public static Component getBasedSimpleType(Component component) {
        if ( !(component instanceof SchemaComponent)) {
            return component;
        }
        SchemaComponent type = getBuiltInSimpleType((SchemaComponent) component);

        if (type == null) {
            return component;
        }
        return type;
    }

    public static String getNamespace(SchemaComponent component) {
        if (component == null || component.getModel() == null) {
            return null;
        }
        return component.getModel().getEffectiveNamespace(component);
    }

    public static String getTypeName(Component component) {
        if (component == null) {
            return "n/a"; // NOI18N
        }
        if (component instanceof Named) {
            return ((Named) component).getName();
        }
        return component.toString();
    }

    public static Attribute attributeName() {
        return new GenericExtensibilityElement.StringAttribute(SCHEMA_COMPONENT_ATTRIBUTE_NAME);
    }
    
    public static Attribute attributeType() {
        return new GenericExtensibilityElement.StringAttribute(SCHEMA_COMPONENT_ATTRIBUTE_TYPE);
    }
    
    public static GlobalSimpleType getBuiltInSimpleType(SchemaComponent schemaComponent) {
//System.out.println();
//System.out.println();
//System.out.println("--------------------");
//System.out.println("BUILT IN SIMPLE TYPE: " + getTypeName(schemaComponent));
        if (schemaComponent == null) {
            return null;
        }
        GlobalSimpleType globalSimpleType = findBuiltInType(schemaComponent);
        if (globalSimpleType != null) {
//System.out.println("  !!!!! SIMPLE !!!!!!!!");
            return globalSimpleType;
        }
        String baseTypeName = schemaComponent.getAnyAttribute(new QName(
            SCHEMA_COMPONENT_ATTRIBUTE_BASE));
        globalSimpleType = null;

//System.out.println("baseTypeName: " + baseTypeName);
        // # 130281
        Collection<GlobalSimpleType> schemaSimpleTypes = null;
        SchemaModel sModel = schemaComponent.getModel();
        if (sModel != null) {
            Schema schema = sModel.getSchema();
            if (schema != null) {
                schemaSimpleTypes = schema.getSimpleTypes();
            }
        }
        //
        List<GlobalSimpleType> simpleTypes = new LinkedList<GlobalSimpleType>();
        simpleTypes.addAll(schemaSimpleTypes);
        simpleTypes.addAll(BUILT_IN_SIMPLE_TYPES);

//System.out.println("Simple Types:");
//System.out.println("" + simpleTypes);
//System.out.println();

        if (baseTypeName != null) {
            baseTypeName = ignoreNamespace(baseTypeName);
            globalSimpleType = findGlobalSimpleType(baseTypeName, simpleTypes);

            if (globalSimpleType != null) {
                // # 130281
                return getBuiltInSimpleType(globalSimpleType);
            }
        }
//System.out.println("getSchemaComponentTypeName: " + getSchemaComponentTypeName(schemaComponent));
        // # 130281
        globalSimpleType = findGlobalSimpleType(getSchemaComponentTypeName(schemaComponent), simpleTypes);
//System.out.println("globalSimpleType: " + globalSimpleType);

        if (globalSimpleType != null) {
            for (SchemaComponent childComponent : globalSimpleType.getChildren()) {
                globalSimpleType = getBuiltInSimpleType(childComponent);
               
                if (globalSimpleType != null) {
                    return globalSimpleType;
                }
            }
            return null;
        }
//System.out.println();
        // # 130281
        for (SchemaComponent child : schemaComponent.getChildren()) {
//System.out.println("  child: " + child.getClass().getName());
          if (child instanceof SimpleContent) {
//System.out.println("        getLocalDefinition: " + ((SimpleContent) child).getLocalDefinition());
              globalSimpleType = getBuiltInSimpleType(((SimpleContent) child).getLocalDefinition());

              if (globalSimpleType != null) {
                  return globalSimpleType;
              }
          }
        }
        return null;
    }
    
    public static GlobalSimpleType findBuiltInType(SchemaComponent schemaComponent) {
        return findGlobalSimpleType(getSchemaComponentTypeName(schemaComponent), BUILT_IN_SIMPLE_TYPES);
    }
    
    public static boolean isBuiltInSimpleType(SchemaComponent schemaComponent) {
        /*
        if ( !(schemaComponent instanceof GlobalSimpleType)) {
            return false;
        }
        return (findGlobalSimpleType(getSchemaComponentTypeName(schemaComponent), 
                BUILT_IN_SIMPLE_TYPES) != null);
        */
        return findBuiltInType(schemaComponent) != null;
    }

    public static GlobalSimpleType findGlobalSimpleType(String typeName, Collection<GlobalSimpleType> globalSimpleTypes) {
        if (typeName != null && globalSimpleTypes != null) {
            for (GlobalSimpleType globalSimpleType : globalSimpleTypes) {
                if (ignoreNamespace(globalSimpleType.toString()).equals(ignoreNamespace(typeName))) {
                    return globalSimpleType;
                }
            }
        }
        return null;
    }

    private static String getSchemaComponentTypeName(SchemaComponent schemaComponent) {
        String typeName = null;

        if ((schemaComponent instanceof SimpleType) || (schemaComponent instanceof ComplexType)) {
            typeName = schemaComponent.getAttribute(attributeName());
        }
        else {
            NamedComponentReference<? extends GlobalType> typeRef = getSchemaComponentTypeRef(schemaComponent);
        
            if (typeRef != null && typeRef.get() != null) {
                typeName = typeRef.get().getName();
            }
            else {
                typeName = ((SchemaComponent) schemaComponent).getAttribute(attributeType());
            }
        }
        return typeName;
    }
    
    private static NamedComponentReference<? extends GlobalType> getSchemaComponentTypeRef(SchemaComponent schemaComponent) {
        NamedComponentReference<? extends GlobalType> typeRef = null;
        try {
            typeRef = ((TypeContainer) schemaComponent).getType();
        } 
        catch (Exception e) {
        }
        return typeRef;
    }
    
    public static String ignoreNamespace(String dataWithNamespace) {
        if (dataWithNamespace == null) {
          return null;
        }
        int index = dataWithNamespace.indexOf(":");

        if ((index > -1) && (index < dataWithNamespace.length() - 1)) {
            return dataWithNamespace.substring(index + 1);
        }
        return dataWithNamespace;
    }

    public static boolean equal(Object o1, Object o2) {
        if (o1 == o2) return true;
        return (o1 == null || o2 == null) ? false : o1.equals(o2);
    }

    private static final String SCHEMA_COMPONENT_ATTRIBUTE_BASE = "base"; // NOI18N
    private static final String SCHEMA_COMPONENT_ATTRIBUTE_NAME = "name"; // NOI18N
    private static final String SCHEMA_COMPONENT_ATTRIBUTE_TYPE = "type"; // NOI18N
    public static Collection<GlobalSimpleType> BUILT_IN_SIMPLE_TYPES = SchemaModelFactory.getDefault().getPrimitiveTypesModel().getSchema().getSimpleTypes();
}
