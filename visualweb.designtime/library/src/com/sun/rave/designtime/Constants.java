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

package com.sun.rave.designtime;

/**
 * <p>These are the common constants used in several places within the Creator Design-Time API.
 * These constants are separated into different sub-interfaces to better associate them with where
 * they are used in the API.  See the separate sub-interfaces for detailed documentation.</p>
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public interface Constants {

    /**
     * These constants are used as attribute keys for the BeanDescriptor.  Use the 'setValue' method
     * on the BeanDescriptor to set these values, and the 'getValue' method to retrieve them.
     *
     * @see java.beans.BeanDescriptor#setValue(String, Object)
     * @see java.beans.BeanDescriptor#getValue(String)
     */
    public interface BeanDescriptor {

        /**
         * This String attribute defines which JSP tag represents this component in the markup. For
         * example HtmlCommandButton defines "command_button" in it's tagName attribute. If no
         * tagName is defined, the component is assumed to be a non-visual, and will not be diplayed
         * on the visual design surface.
         */
        public static final String TAG_NAME = "tagName"; // NOI18N

        /**
         * This String attribute defines which JSP taglib contains this component.
         */
        public static final String TAGLIB_URI = "taglibUri"; // NOI18N

        /**
         * This String attribute defines the suggested JSP taglib prefix (if not already added to
         * the JSP file)
         */
        public static final String TAGLIB_PREFIX = "taglibPrefix"; // NOI18N
        
        /** 
         * This String[] attribute specifies an array of properties that are "inline editable"
         * as text. The format of each String is as follows:
         * <ul>
         * <li> An initial "*" which indicates that this is the default property and should
         *    be used to inline edit this property when the component is first dropped.
         * <li> The name of the property
         * <li> Optionally, a colon followed by an XPath expression pointing to the node
         *   containing the text in the rendered markup. Currently, only a very limited
         *   subset of XPath is supported; this will be improved over time.
         *   This xpath expression can in turn be followed by another comma, and another xpath
         *   expression, repeated as many times as necessary. These supply additional
         *   xpath possibilities for the component renderer. The designer will try each of
         *   these expressions in order until it finds a match in the rendered output from
         *   a component. This is useful for example when a component may render different
         *   output based on its properties, and you want to be able to support inline editing
         *   in all these scenarios.
         * </ul>
         * As an example, you might have a String like this:
         * <pre>
         *   "*value://span[@class='FooBar']"
         * </pre>
         * This tells the designer to initiate inline editing whe &lt;span&gt; found in
         * the markup that has a class attribute set to FooBar.
         */
        public static final String INLINE_EDITABLE_PROPERTIES = "inlineEditable";

        /**
         * This String attribute defines the base instance name to use for new components of this
         * type. This base instance name will be auto-numbered for subsequent instances of the
         * component on the form. For example, HtmlCommandButton defines "button" in it's
         * instanceName attribute, so the first HtmlCommandButton dropped on a form is called
         * "button1", and the second "button2", etc. If no instanceName is defined, the class name
         * will be used (with the initial letter lower-cased), like "htmlCommandButton1".
         */
        public static final String INSTANCE_NAME = "instanceName"; // NOI18N

        /**
         * This Boolean attribute specifies if this component should have its instance name
         * prepended with the instance name of its parent when it is created.  This is used for
         * related containership situations, like a table component and its columns.  Setting this
         * attribute to Boolean.TRUE allows a table called "itemTable" to have columns automatically
         * named "itemTableColumn1", "itemTableColumn2", etc.  If this attribute is not set, it is
         * treated as Boolean.FALSE, and the parent instance name will not be prepended.
         */
        public static final String PREPEND_PARENT_INSTANCE_NAME = "prependParentInstanceName"; // NOI18N

        /**
         * This Boolean attribute defines whether this component should be treated as a container in
         * the designer or not. If not defined, all JSF components are treated as containers,
         * because the UIComponent base-class has parent/child symantics. The HtmlCommandButton, for
         * example, defines this attribute to Boolean.FALSE so that the designer does not show it as
         * a component that can have children.
         */
        public static final String IS_CONTAINER = "isContainer"; // NOI18N

        /**
         * This String[] attribute defines the set of preferred childrens' class names.  This
         * tells Creator which children types to place in a right-click "Add >" context item for
         * this bean. If this is not set, no "Add >" context item will be present.
         */
        public static final String PREFERRED_CHILD_TYPES = "preferredChildTypes"; //NOI18N

        /**
         * This Boolean attribute (Boolean.TRUE or Boolean.FALSE) defines wether this component
         * should be shown in the tray. The default setting is Boolean.FALSE for BeanDescriptors
         * that do not include this attribute. Typically, this is only used for database-related
         * components, and the occasional non-painting visual component like HtmlInputHidden.
         * @deprecated
         */
        public static final String TRAY_COMPONENT = "trayComponent"; // NOI18N

        /**
         * This Boolean attribute specifies that the MarkupDesignInfo class for this markup bean
         * will do the rendering at design-time via the customizeRender(...) method.  If this
         * attribute is set to Boolean.TRUE, the markup component will not be invoked to render
         * itself at design-time, and the associated MarkupDesignInfo class will be required to
         * provide the rendered content.  If this is attribute is set to Boolean.FALSE, or not set,
         * or there is no associated MarkupDesignInfo for this markup bean, the component rendering
         * will be invoked.
         */
        public static final String SKIP_COMPONENT_RENDER = "skipComponentRender"; // NOI18N

        /**
         * This String attribute defines a method on the component that should be called when the
         * host is cleaning up to allow this bean to cleanup resources, etc. This method should be
         * called at both design-time and runtime, and must have zero arguments.
         */
        public static final String CLEANUP_METHOD = "cleanupMethod"; // NOI18N

        /**
         * This attribute (of type FacetDescriptor[]) defines the facets that this JSF component
         * surfaces.
         *
         * @see com.sun.rave.designtime.faces.FacetDescriptor
         */
        public static final String FACET_DESCRIPTORS = "facetDescriptors"; // NOI18N

        /**
         * This attribute (of type PropertyCategory[]) defines the set of property categories that
         * this component's property set should be organized in.  This is used to control the
         * display order of the categories, as individual PropertyDescriptor classes will define
         * which category a property belongs in.
         *
         * @see PropertyCategory
         */
        public static final String PROPERTY_CATEGORIES = "propertyCategories"; // NOI18N

        /**
         * This String attribute defines the section of markup that a particular component has an
         * 'affinity' for - meaning what part of the document does it need to be parented by. For
         * example, the Stylesheet component defines it's markupSection as "head", to get rendered
         * into the head section of the document. "body" and/or "form" is assumed if nothing is set.
         */
        public static final String MARKUP_SECTION = "markupSection"; // NOI18N

        /**
         * This String attribute defines the help key that resolves to the appropriate help contents
         * for this component. This will be shown when the user presses F1 when a component is
         * selected on the palette - or if a dynamic help window is showing.
         */
        public static final String HELP_KEY = "helpKey"; // NOI18N

        /**
         * This String attribute defines the help key that resolves to the appropriate help contents
         * for this component. This will be shown when the user presses F1 when a component is
         * selected on the palette - or if a dynamic help window is showing.
         */
        public static final String PROPERTIES_HELP_KEY = "propertiesHelpKey"; // NOI18N

        /**
         * This attribute (of type Integer) defines the bitmask of constraints (from
         * ResizeConstraints interface) that dictates how a user can resize this component
         *
         * @see Constants.ResizeConstraints
         */
        public static final String RESIZE_CONSTRAINTS = "resizeConstraints"; // NOI18N
    }

    /**
     * The component may or may not be resizable on the design surface. This bitmask (stored in the
     * Constants.BeanDescriptor.RESIZE_CONSTRAINTS attribute on the BeanDescriptor) defines how the
     * selection 'nibs' will appear in a visual designer.
     *
     * @see Constants.BeanDescriptor.RESIZE_CONSTRAINTS
     */
    public interface ResizeConstraints {
        /** The top edge of the component can be manipulated to resize */
        public static final int TOP = 0x1;
        /** The left edge of the component can be manipulated to resize */
        public static final int LEFT = 0x2;
        /** The bottom edge of the component can be manipulated to resize */
        public static final int BOTTOM = 0x4;
        /** The right edge of the component can be manipulated to resize */
        public static final int RIGHT = 0x8;
        /** If resizable, the component should maintain its aspect ratio while it is resized */
        public static final int MAINTAIN_ASPECT_RATIO = 0x10;
        /** The component can be resized vertically - the top and bottom edges can be manipulated to resize */
        public static final int VERTICAL = TOP | BOTTOM;
        /** The component can be resized horizontally - the left and right edges can be manipulated to resize */
        public static final int HORIZONTAL = LEFT | RIGHT;
        /** The component can be resized in any way - the top, bottom, left, and right edges can be manipulated to resize */
        public static final int ANY = VERTICAL | HORIZONTAL;
        /** The component can not be resized */
        public static final int NONE = 0;
    }

    /**
     * These constants are used as attribute keys for the PropertyDescriptor.  Use the 'setValue'
     * method on the PropertyDescriptor to set these values, and the 'getValue' method to retrieve
     * them.
     *
     * @see java.beans.PropertyDescriptor#setValue(String, Object)
     * @see java.beans.PropertyDescriptor#getValue(String)
     */
    public interface PropertyDescriptor {

        /**
         * This attribute (of type PropertyCategory) defines the category for a particular
         * PropertyDescriptor.
         *
         * @see java.beans.PropertyDescriptor
         */
        public static final String CATEGORY = "category"; // NOI18N

        /**
         * This String attribute defines the help key that resolves to the appropriate help contents
         * for this specific property. This will be shown when the user presses F1 when a property
         * is selected in the property inspector - or if a dynamic help window is showing when a
         * property is selected.
         */
        public static final String HELP_KEY = "helpKey"; // NOI18N

        /**
         * This attribute (of type AttributeDescriptor) defines the markup attribute that this
         * property corresponds to. Any property settings in the designer that have a corresponding
         * AttributeDescriptor here will generate the property setting code in the JSP file as a
         * markup attribute.
         *
         * @see com.sun.rave.designtime.markup.AttributeDescriptor
         */
        public static final String ATTRIBUTE_DESCRIPTOR = "attributeDescriptor"; // NOI18N
    }

    /**
     * These constants are used as attribute keys for the EventSetDescriptor.  Use the 'setValue'
     * method on the EventSetDescriptor to set these values, and the 'getValue' method to retrieve
     * them.
     *
     * @see java.beans.EventSetDescriptor#setValue(String, Object)
     * @see java.beans.EventSetDescriptor#getValue(String)
     */
    public interface EventSetDescriptor {

        /** 
         * This attribute (of type EventDescriptor[]) defines the set of events
         * included in this EventSet.  EventDescriptor objects are used to define
         * additional metadata for particular events.
         */
        public static final String EVENT_DESCRIPTORS = "eventDescriptors";

        /**
         * This attribute (of type PropertyDescriptor) defines the property that is used to bind
         * this event set.  This is used when a JSF component surfaces a property of type
         * MethodBinding that is used to define an event.
         *
         * @todo Remove me! Use EventDescriptor.BINDING_PROPERTY instead! Left here
         *   to avoid having to update references for now.
         * @see java.beans.PropertyDescriptor
         */
        public static final String BINDING_PROPERTY = "bindingProperty"; // NOI18N
    }
    
    /**
     * These constants are used as attribute keys for the EventDescriptor.  Use the 'setValue'
     * method on the EventDescriptor to set these values, and the 'getValue' method to retrieve
     * them.
     *
     * @see EventDescriptor#setValue(String, Object)
     * @see EventDescriptor#getValue(String)
     */
    public interface EventDescriptor {
        /**
         * This attribute (of type PropertyDescriptor) defines the property that is used to bind
         * this event set.  This is used when a JSF component surfaces a property of type
         * MethodBinding that is used to define an event.
         *
         * @see java.beans.PropertyDescriptor
         */
        public static final String BINDING_PROPERTY = "bindingProperty"; // NOI18N

        /**
         * This attribute (of type String) defines a default method body (expressed
         * as formatted Java Source) for the event handler when it is first created.
         * The body starts immediately after the opening brace, so normally it would
         * need to start with a newline, then 8 spaces for indentation to cover the
         * four spaces to the method inset and another 4 for the code itself
         */
        public static final String DEFAULT_EVENT_BODY = "defaultEventBody"; // NOI18N

        /**
         * This attribute (of type String[]) defines a list of classes that must
         * be imported when an event handler is created for this event. The sample
         * code (see DEFAULT_EVENT_BODY) may require it, or common usage in the code
         * may require it (for example, a validate event may typically want a
         * ValidatorException to be pre imported.
         */
        public static final String REQUIRED_IMPORTS = "requiredImports"; // NOI18N
        
        /**
         * This attribute (of type String[]) defines an array of parameter names to
         * be used when constructing an event handler in the Java source code for
         * this event. The array should have at least as many items as there are
         * parameters.
         */
        public static final String PARAMETER_NAMES = "parameterNames"; // NOI18N
    }

    /**
     * These constants are used as pre-defined keys for the DesignContext.getContextData(String)
     * method.
     *
     * @see DesignContext#getContextData(String)
     */
    public interface ContextData {

        /**
         * Returns an array of StyleClassDescriptor objects (StyleClassDescriptor[]) representing
         * the CSS style classes currently in scope for this context.
         *
         * @see com.sun.rave.designtime.markup.StyleClassDescriptor
         */
        public static final String CSS_STYLE_CLASS_DESCRIPTORS = "css-style-class-descriptors"; // NOI18N

        /**
         * Returns a comma-separated list of datasource names as a String
         */
        public static final String DATASOURCE_NAMES = "datasource-names"; // NOI18N

        /**
         * Returns a String representing the scope of the context, like "request", "session", or
         * "application"
         */
        public static final String SCOPE = "scope"; // NOI18N

        /**
         * Returns the fully qualified class name (String) of the context
         */
        public static final String CLASS_NAME = "className"; // NOI18N

        /**
         * Returns a Class object representing the base class of the context
         */
        public static final String BASE_CLASS = "baseClass"; // NOI18N

        /**
         * Returns a URI object representing the project resource for the JSP page (if any)
         * represented by this context.  If there is no page associated with this context, this
         * constant returns <code>null</code>.
         */
        public static final String PAGE_RESOURCE_URI = "pageResourceUri"; // NOI18N

        /**
         * Returns a URI object representing the project resource for the Java source file
         * represented by this context.
         */
        public static final String JAVA_RESOURCE_URI = "javaResourceUri"; // NOI18N

        /**
         * Returns a {@link ContextMethod} object representing the <code>init()</code> method from
         * the Java source file represented by this context, or <code>null</null> if there is no
         * <code>init</code> method defined by this context.
         */
        public static final String INIT_METHOD = "initMethod"; // NOI18N

        /**
         * Returns a {@link ContextMethod} object representing the <code>preprocess()</code> method
         * from the Java source file represented by this context, or <code>null</null> if there is
         * no <code>preprocess()</code> method defined by this context.
         */
        public static final String PREPROCESS_METHOD = "preprocessMethod"; // NOI18N

        /**
         * Returns a {@link ContextMethod} object representing the <code>prerender()</code> method
         * from the Java source file represented by this context, or <code>null</null> if there is
         * no <code>prerender()</code> method defined by this context.
         */
        public static final String PRERENDER_METHOD = "prerenderMethod"; // NOI18N

        /**
         * Returns a {@link ContextMethod} object representing the <code>destroy()</code> method
         * from the Java source file represented by this context, or <code>null</null> if there is
         * no <code>destroy()</code> method defined by this context.
         */
        public static final String DESTROY_METHOD = "destroyMethod"; // NOI18N
    }
}
