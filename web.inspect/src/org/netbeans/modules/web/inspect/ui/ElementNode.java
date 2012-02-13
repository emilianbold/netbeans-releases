/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.swing.Action;
import org.netbeans.modules.web.inspect.ElementHandle;
import org.netbeans.modules.web.inspect.PageModel;
import org.netbeans.modules.web.inspect.actions.GoToElementSourceAction;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A node representing a document element.
 *
 * @author Jan Stola
 */
public class ElementNode extends AbstractNode {
    /** Icon base of the node. */
    static final String ICON_BASE = "org/netbeans/modules/web/inspect/resources/domElement.png"; // NOI18N
    /** Name of the property set of attributes. */
    private static final String PROP_SET_ATTRIBUTES = "attributes"; // NOI18N
    /** Name of the property set of computed style information. */
    private static final String PROP_SET_STYLE = "style"; // NOI18N
    /**
     * If some attribute starts with this prefix then it shouldn't
     * be shown to the user because it is not included in the original
     * document. It has been set by the plugin (for internal purposes).
     */
    private static final String ATTR_HIDDEN_PREFIX = ":netbeans"; // NOI18N
    /** Index of the property set of attributes. */
    private static final int SET_ATTRIBUTES_INDEX = 0;
    /** Index of the property set of computed style information. */
    private static final int SET_STYLE_INDEX = 1;
    /** Request processor that is used to obtain properties. */
    private static RequestProcessor RP = new RequestProcessor(ElementNode.class);
    /** Element that this node represents. */
    private Element element;
    /** Property sets of the node. */
    private PropertySet[] propertySets;
    /** Actions of the node. */
    private Action[] actions;

    /**
     * Creates new {@code ElementNode}.
     */
    public ElementNode() {
        super(new ElementChildren(), new ElementLookup());
        setIconBaseWithExtension(ICON_BASE);
    }

    /**
     * Sets the element this node should represent.
     * 
     * @param element element this node should represent.
     */
    void setElement(Element element) {
        this.element = element;
        setDisplayName(element.getTagName().toLowerCase());
        ((ElementLookup)getLookup()).setElement(element);
        ((ElementChildren)getChildren()).setElement(element);
        updateProperties();
    }

    /**
     * Updates properties of the node; (re-)fetches them from the page.
     */
    private void updateProperties() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                PageModel pageModel = PageModel.getDefault();
                ElementHandle handle = ElementHandle.forElement(element);
                updateProperties(SET_ATTRIBUTES_INDEX, pageModel.getAtrributes(handle));
                updateProperties(SET_STYLE_INDEX, pageModel.getComputedStyle(handle));
            } 
        });
    }

    /**
     * Updates the specified property set of the node.
     * 
     * @param propertySetIndex index of the property set to update.
     * @param newValuesMap map with the new names and values of the properties.
     */
    private void updateProperties(int propertySetIndex, Map<String,String> newValuesMap) {
        SortedPropertySet set = (SortedPropertySet)getPropertySets()[propertySetIndex];

        for (final Map.Entry<String,String> entry : newValuesMap.entrySet()) {
            String attrName = entry.getKey();
            String attrValue = entry.getValue();
            if (!skipAttribute(attrName, attrValue)) {
                ReadOnlyProperty prop = (ReadOnlyProperty)set.getProperty(attrName);
                if (prop == null) {
                    // New key => new property
                    prop = new ReadOnlyProperty(attrName, attrName);
                    set.addProperty(prop);
                }
                prop.setValue0(attrValue);
            }
        }
        
        // Remove properties corresponding to keys that no longer exist
        for (Node.Property prop : set.getProperties()) {
            String propName = prop.getName();
            if (!newValuesMap.containsKey(propName)) {
                set.removeProperty(prop);
            }
        }
    }

    /**
     * Determines whether the given attribute should be shown
     * to the user or not.
     * 
     * @param name name of the attribute.
     * @param value value of the attribute.
     * @return {@code true} if the attribute should be skipped (i.e., not shown
     * to the user), returns {@code false} otherwise.
     */
    private static boolean skipAttribute(String name, String value) {
        // Empty style attributes are result of our highlighting logic.
        return ("style".equals(name) && value.isEmpty()) // NOI18N
                || name.startsWith(ATTR_HIDDEN_PREFIX);
    }

    @Override
    public synchronized PropertySet[] getPropertySets() {
        if (propertySets == null) {
            propertySets = createPropertySets();
        }
        return propertySets;
    }

    /**
     * Creates property sets of the node.
     * 
     * @return property sets of the node.
     */
    private PropertySet[] createPropertySets() {
        ResourceBundle bundle = NbBundle.getBundle(ElementNode.class);
        PropertySet attributes = new SortedPropertySet(
                PROP_SET_ATTRIBUTES,
                bundle.getString("DomNode.propertySet.attributes"), // NOI18N
                null);
        attributes.setValue(
                "tabName", // NOI18N
                bundle.getString("DomNode.propertyTab.attributes")); // NOI18N
        PropertySet style = new SortedPropertySet(
                PROP_SET_STYLE,
                bundle.getString("DomNode.propertySet.computedStyle"), // NOI18N
                null);
        style.setValue(
                "tabName", // NOI18N
                bundle.getString("DomNode.propertyTab.computedStyle")); // NOI18N
        return new PropertySet[] {attributes, style};
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(GoToElementSourceAction.class);
    }

    @Override
    public Action[] getActions(boolean context) {
        if (actions == null) {
            actions = new Action[] {
                SystemAction.get(GoToElementSourceAction.class),
            };
        }
        return actions;
    }

    /**
     * Children of {@code ElementNode}.
     */
    static class ElementChildren extends Children.Keys<ElementKey> {
        /** Element represented by the enclosing node. */
        private Element element;
        /** Map from keys to childrens. */
        private java.util.Map<ElementKey,ElementNode> childrenMap = new HashMap<ElementKey,ElementNode>();

        /**
         * Sets the element represented by the enclosing node.
         * 
         * @param element element represented by the enclosing node.
         */
        void setElement(Element element) {
            if (this.element == element) {
                return;
            }
            this.element = element;
            updateKeys();
        }

        /**
         * Updates keys by (re-)fetching sub-element information from
         * the current element.
         */
        private void updateKeys() {
            NodeList list = element.getChildNodes();
            List<ElementKey> keys = new ArrayList<ElementKey>(list.getLength());
            for (int i=0; i<list.getLength(); i++) {
                Element subElement = (Element)list.item(i);
                keys.add(new ElementKey(subElement));
            }
            // Clear map from children that are no longer valid
            childrenMap.keySet().retainAll(keys);
            // Update elements in valid children
            for (ElementKey key : keys) {
                ElementNode node = childrenMap.get(key);
                if (node != null) {
                    node.setElement(key.getElement());
                }
            }
            setKeys(keys);
        }

        @Override
        protected Node[] createNodes(ElementKey key) {
            ElementNode node = new ElementNode();
            node.setElement(key.getElement());
            childrenMap.put(key, node);
            return new Node[] {node};
        }
    }

    /**
     * Children key of a sub-element.
     */
    static class ElementKey {
        /** Name of the attribute where we store the ID of the element. */
        private static final String NB_ID_ATTR = ":netbeans_id"; // NOI18N
        /** ID of the element. */
        private String id;
        /** Element corresponding to this element key. */
        private Element element;

        /**
         * Creates new {@code ElementKey} for the specified element.
         * 
         * @param element element for the new {@code ElementKey}.
         */
        ElementKey(Element element) {
            this.element = element;
            id = element.getAttribute(NB_ID_ATTR);
        }

        /**
         * Returns element that corresponds to this element key.
         * 
         * @return element that corresponds to this element key.
         */
        Element getElement() {
            return element;
        }

        @Override
        public boolean equals(Object obj) {
            boolean equal = false;
            if (obj instanceof ElementKey) {
                equal = id.equals(((ElementKey)obj).id);
            }
            return equal;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
        
    }

    /**
     * Lookup that contains just the specified element.
     */
    static class ElementLookup extends ProxyLookup {
        
        /**
         * Set element that this lookup should contain.
         * 
         * @param element 
         */
        void setElement(Element element) {
            ElementHandle handle = ElementHandle.forElement(element);
            setLookups(Lookups.fixed(element, handle));
        }
    }

    /**
     * Property that is read-only from the users point of view,
     * but whose value can be modified programmatically.
     */
    static class ReadOnlyProperty extends PropertySupport.ReadOnly<String> {
        /** Value of the property. */
        private String value;

        /**
         * Creates a new {@code ReadOnlyProperty}.
         * 
         * @param name name of the property.
         * @param displayName display name of the property.
         */
        ReadOnlyProperty(String name, String displayName) {
            super(name, String.class, displayName, null);
        }

        /**
         * Sets the value of the property.
         * 
         * @param value new value of the property.
         */
        void setValue0(String value) {
            this.value = value;
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return value;
        }
        
    }

    /**
     * Property set sorted according to property names.
     */
    class SortedPropertySet extends PropertySet {
        /** Property name to property map sorted according to property names. */
        private SortedMap<String,Property<?>> properties;

        /**
         * Creates a new {@code SortedPropertySet}.
         * 
         * @param name name of the property set.
         * @param displayName display name of the property set.
         * @param shortDescription short description of the property set.
         */
        SortedPropertySet(String name, String displayName, String shortDescription) {
            super(name, displayName, shortDescription);
            properties = new TreeMap<String,Property<?>>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
        }

        /**
         * Adds a property into this property set.
         * 
         * @param property property to add into this set.
         */
        void addProperty(Property<?> property) {
            properties.put(property.getName(), property);
            firePropertySetsChange(null, null);
        }

        /**
         * Removes a property from this property set.
         * 
         * @param property property to remove from this set.
         */
        void removeProperty(Property<?> property) {
            properties.remove(property.getName());
            firePropertySetsChange(null, null);
        }

        /**
         * Returns the property of the specified name.
         * 
         * @param name name of the requested property.
         * @return property of the specified name.
         */
        Property<?> getProperty(String name) {
            return properties.get(name);
        }

        @Override
        public Property<?>[] getProperties() {
            return properties.values().toArray(new Property<?>[properties.size()]);
        }
        
    }
    
}
