/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.beans.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.TopManager;
import org.openide.loaders.XMLDataObject;
import org.openide.util.io.NbObjectInputStream;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.layoutsupport.delegates.*;
import org.netbeans.modules.form.codestructure.*;

/**
 * XML persistence manager - responsible for saving/loading forms to/from XML.
 * The class contains lots of complicated code with many hacks ensuring full
 * compatibility of the format despite that many original classes don't exist
 * yet (e.g. FormInfo and DesignLayout and subclasses).
 *
 * @author Ian Formanek, Tomas Pavek
 */

public class GandalfPersistenceManager extends PersistenceManager {
    static final String NB32_VERSION = "1.0"; // NOI18N
    static final String CURRENT_VERSION = "1.1"; // NOI18N

    // XML elements names
    static final String XML_FORM = "Form"; // NOI18N
    static final String XML_NON_VISUAL_COMPONENTS = "NonVisualComponents"; // NOI18N
    static final String XML_CONTAINER = "Container"; // NOI18N
    static final String XML_COMPONENT = "Component"; // NOI18N
    static final String XML_COMPONENT_REF = "ComponentRef"; // NOI18N
    static final String XML_MENU_COMPONENT = "MenuItem"; // NOI18N
    static final String XML_MENU_CONTAINER = "Menu"; // NOI18N
    static final String XML_LAYOUT = "Layout"; // NOI18N
    static final String XML_LAYOUT_CODE = "LayoutCode"; // NOI18N
    static final String XML_CONSTRAINTS = "Constraints"; // NOI18N
    static final String XML_CONSTRAINT = "Constraint"; // NOI18N
    static final String XML_SUB_COMPONENTS = "SubComponents"; // NOI18N
    static final String XML_EVENTS = "Events"; // NOI18N
    static final String XML_EVENT = "EventHandler"; // NOI18N
    static final String XML_PROPERTIES = "Properties"; // NOI18N
    static final String XML_PROPERTY = "Property"; // NOI18N
    static final String XML_VALUE = "Value"; // NOI18N
    static final String XML_SYNTHETIC_PROPERTY = "SyntheticProperty"; // NOI18N
    static final String XML_SYNTHETIC_PROPERTIES = "SyntheticProperties"; // NOI18N
    static final String XML_AUX_VALUES = "AuxValues"; // NOI18N
    static final String XML_AUX_VALUE = "AuxValue"; // NOI18N
    static final String XML_SERIALIZED_PROPERTY_VALUE = "SerializedValue"; // NOI18N
    static final String XML_CODE_EXPRESSION = "CodeExpression"; // NOI18N
    static final String XML_CODE_VARIABLE = "CodeVariable"; // NOI18N
    static final String XML_CODE_ORIGIN = "ExpressionOrigin"; // NOI18N
    static final String XML_CODE_STATEMENT = "CodeStatement"; // NOI18N
    static final String XML_CODE_PARAMETERS = "Parameters"; // NOI18N
    static final String XML_CODE_STATEMENTS = "Statements"; // NOI18N
    static final String XML_ORIGIN_META_OBJECT = "ExpressionProvider"; // NOI18N
    static final String XML_STATEMENT_META_OBJECT = "StatementProvider"; // NOI18N
    static final String XML_CODE_CONSTRUCTOR = "CodeConstructor"; // NOI18N
    static final String XML_CODE_METHOD = "CodeMethod";
    static final String XML_CODE_FIELD = "CodeField";

    // XML attributes names
    static final String ATTR_FORM_VERSION = "version"; // NOI18N
    static final String ATTR_FORM_TYPE = "type"; // NOI18N
    static final String ATTR_COMPONENT_NAME = "name"; // NOI18N
    static final String ATTR_COMPONENT_CLASS = "class"; // NOI18N
    static final String ATTR_PROPERTY_NAME = "name"; // NOI18N
    static final String ATTR_PROPERTY_TYPE = "type"; // NOI18N
    static final String ATTR_PROPERTY_EDITOR = "editor"; // NOI18N
    static final String ATTR_PROPERTY_VALUE = "value"; // NOI18N
    static final String ATTR_PROPERTY_PRE_CODE = "preCode"; // NOI18N
    static final String ATTR_PROPERTY_POST_CODE = "postCode"; // NOI18N
    static final String ATTR_EVENT_NAME = "event"; // NOI18N
    static final String ATTR_EVENT_LISTENER = "listener"; // NOI18N
    static final String ATTR_EVENT_PARAMS = "parameters"; // NOI18N
    static final String ATTR_EVENT_HANDLER = "handler"; // NOI18N
    static final String ATTR_AUX_NAME = "name"; // NOI18N
    static final String ATTR_AUX_VALUE = "value"; // NOI18N
    static final String ATTR_AUX_VALUE_TYPE = "type"; // NOI18N
    static final String ATTR_LAYOUT_CLASS = "class"; // NOI18N
    static final String ATTR_CONSTRAINT_LAYOUT = "layoutClass"; // NOI18N
    static final String ATTR_CONSTRAINT_VALUE = "value"; // NOI18N
    static final String ATTR_EXPRESSION_ID = "id"; // NOI18N
    static final String ATTR_VAR_NAME = "name"; // NOI18N
    static final String ATTR_VAR_TYPE = "type"; // NOI18N
    static final String ATTR_VAR_DECLARED_TYPE = "declaredType"; // NOI18N
    static final String ATTR_META_OBJECT_TYPE = "type"; // NOI18N
    static final String ATTR_MEMBER_CLASS = "class"; // NOI18N
    static final String ATTR_MEMBER_PARAMS = "parameterTypes"; // NOI18N
    static final String ATTR_MEMBER_NAME = "name"; // NOI18N

    private static final String ONE_INDENT =  "  "; // NOI18N
    private static final Object NO_VALUE = new Object();

    private org.w3c.dom.Document topDocument =
        org.openide.xml.XMLUtil.createDocument("topDocument",null,null,null); // NOI18N

    private FileObject formFile;

    private FormModel formModel;

    // map of properties that cannot be loaded before a container is filled
    private Map containerDependentProperties;

    // map of loaded components (not necessarily added to FormModel yet)
    private Map loadedComponents;

    // XML persistence of code structure
    private Map expressions; // map of expressions/IDs already saved/loaded
    private int lastExpId; // CodeExpression ID counter (for saving)
    private Map savedVariables; // set of code variables already saved
    private boolean codeFlow = true; // we can save/load either code flow
                                     // or static code structure

    private String formInfoName; // name of FormInfo class loaded from the form file
    private String formatVersion; // format version for saving the form file


    /** A method which allows the persistence manager to provide infotrmation
     * on whether is is capable to store info about advanced features provided
     * from Developer 3.0
     * - all persistence managers except the one providing backward compatibility with 
     * Developer 2.X should return true from this method.
     * @return true if this PersistenceManager is capable to store advanced
     * form features, false otherwise
     */
    public boolean supportsAdvancedFeatures() {
        return true;
    }

    /** A method which allows the persistence manager to check whether it can
     * read given form format.
     * @return true if this PersistenceManager can load form stored in the
     * specified form, false otherwise
     * @exception IOException if any problem occured when accessing the form
     */
    public boolean canLoadForm(FormDataObject formObject) throws IOException {
        FileObject formFile = formObject.getFormEntry().getFile();
        try {
            org.w3c.dom.Document doc = org.openide.loaders.XMLDataObject.parse(formFile.getURL());
        } catch (IOException e) {
            if (System.getProperty("netbeans.debug.exceptions") != null) // NOI18N
                e.printStackTrace();
            return false;
        } catch (org.xml.sax.SAXException e) {
            if (System.getProperty("netbeans.debug.exceptions") != null) // NOI18N
                e.printStackTrace();
            return false;
        }
        return true;
    }

    private String readEncoding(InputStream is) {
        // If all else fails, assume XML without a declaration, and
        // using UTF-8 encoding.
        String useEncoding = "UTF-8"; // NOI18N
        byte buf [];
        int	len;
        buf = new byte [4];
        // See if we can figure out the character encoding used
        // in this file by peeking at the first few bytes.
        try {
            len = is.read(buf);
            if (len == 4) switch (buf [0] & 0x0ff) {
                case 0:
                    // 00 3c 00 3f == illegal UTF-16 big-endian
                    if (buf [1] == 0x3c && buf [2] == 0x00 && buf [3] == 0x3f) {
                        useEncoding = "UnicodeBig"; // NOI18N
                    }
                    // else it's probably UCS-4
                    break;

                case '<':      // 0x3c: the most common cases!
                    switch (buf [1] & 0x0ff) {
                        // First character is '<'; could be XML without
                        // an XML directive such as "<hello>", "<!-- ...", // NOI18N
                        // and so on.
                        default:
                            break;
                            // 3c 00 3f 00 == illegal UTF-16 little endian
                        case 0x00:
                            if (buf [2] == 0x3f && buf [3] == 0x00) {
                                useEncoding = "UnicodeLittle"; // NOI18N
                            }
                            // else probably UCS-4
                            break;

                            // 3c 3f 78 6d == ASCII and supersets '<?xm'
                        case '?':
                            if (buf [2] != 'x' || buf [3] != 'm')
                                break;
                            //
                            // One of several encodings could be used:
                            // Shift-JIS, ASCII, UTF-8, ISO-8859-*, etc
                            //
                            useEncoding = "UTF8"; // NOI18N
                    }
                    break;

                    // 4c 6f a7 94 ... some EBCDIC code page
                case 0x4c:
                    if (buf [1] == 0x6f
                        &&(0x0ff & buf [2]) == 0x0a7
                        &&(0x0ff & buf [3]) == 0x094) {
                        useEncoding = "CP037"; // NOI18N
                    }
                    // whoops, treat as UTF-8
                    break;

                    // UTF-16 big-endian
                case 0xfe:
                    if ((buf [1] & 0x0ff) != 0xff) break;
                    useEncoding = "UTF-16"; // NOI18N

                    // UTF-16 little-endian
                case 0xff:
                    if ((buf [1] & 0x0ff) != 0xfe) break;
                    useEncoding = "UTF-16"; // NOI18N

                    // default ... no XML declaration
                default:
                    break;
            }

            byte buffer[] = new byte [1024];
            is.read(buffer);
            String s = new String(buffer, useEncoding);
            int pos = s.indexOf("encoding"); // NOI18N
            String result=null;
            int startPos, endPos;
            if ((pos > 0) &&(pos < s.indexOf(">"))) { // NOI18N
                if ((startPos = s.indexOf('"', pos)) > 0 &&
                    (endPos = s.indexOf('"', startPos+1)) > startPos) {
                    result = s.substring(startPos+1, endPos);
                }
            }
            if (result == null) {
                // encoding not specified in xml
                //result = System.getProperty("file.encoding");
                result = null;
            }
            return result;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Called to actually load the form stored in specified formObject.
     * @param formObject the FormDataObject which represents the form files
     * @return the FormModel representing the loaded form or null if some problem occured
     * @exception IOException if any problem occured when loading the form
     */
    public void loadForm(FormDataObject formObject, FormModel formModel)
        throws IOException
    {
        formFile = formObject.getFormEntry().getFile();
        org.w3c.dom.Document doc;
        org.w3c.dom.Element mainElement;
        String encoding;
        try {
            encoding = readEncoding(formFile.getURL().openStream());
            doc = org.openide.loaders.XMLDataObject.parse(formFile.getURL());
            mainElement = doc.getDocumentElement();
        }
        catch (org.xml.sax.SAXException e) {
            throw new IOException(e.getMessage());
        }

        // 1. check the top-level element name
        if (!XML_FORM.equals(mainElement.getTagName()))
            throw new IOException(FormEditor.getFormBundle().getString("ERR_BadXMLFormat"));

        // 2. check the form version
        String version = mainElement.getAttribute(ATTR_FORM_VERSION);
        if (!NB32_VERSION.equals(version) && !CURRENT_VERSION.equals(version))
            throw new IOException(FormEditor.getFormBundle().getString("ERR_BadXMLVersion"));

        // [what is this check good for ??]
        org.w3c.dom.NodeList childNodes = mainElement.getChildNodes();
        if (childNodes == null)
            throw new IOException(FormEditor.getFormBundle().getString("ERR_BadXMLFormat"));

        formInfoName = mainElement.getAttribute(ATTR_FORM_TYPE);

        this.formModel = formModel;
        formModel.setName(formObject.getName());

        try {
            formModel.initialize(formObject.getSource());
        }
        catch (Throwable ex) {
            if (ex instanceof ThreadDeath)
                throw (ThreadDeath) ex;
            ex.printStackTrace();
        }

        if (formModel.getFormBaseClass() == null) {
            // derive the form type from the FormInfo type saved in form file
            // [user should be warned that the form type is not taken from java]
            Class formClass = getCompatibleFormClass(formInfoName);
            if (formClass != null) {
                try {
                    System.err.println("[WARNING] Form type detection falls back to FormInfo type."); // NOI18N
                    formModel.setFormBaseClass(formClass);
                }
                catch (Throwable ex) {
                    if (ex instanceof ThreadDeath)
                        throw (ThreadDeath) ex;
                    ex.printStackTrace();
                }
            }

            if (formModel.getFormBaseClass() == null) {
                // cannot determine form base class
                throw new IOException(FormEditor.getFormBundle()
                                      .getString("MSG_ERR_FormType")); // NOI18N
            }
        }

        if (loadedComponents != null)
            loadedComponents.clear();
        if (expressions != null)
            expressions.clear();
        containerDependentProperties = null;

        loadNonVisuals(mainElement); //, formModel

        RADComponent topComp = formModel.getTopRADComponent();
        if (topComp != null) {
            try {
                loadComponent(mainElement, topComp, null);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        containerDependentProperties = null;
        if (expressions != null)
            expressions.clear();
        if (loadedComponents != null)
            loadedComponents.clear();
    }

    private void loadNonVisuals(org.w3c.dom.Node node/*, FormModel formModel*/) {
        org.w3c.dom.Node nonVisualsNode =
                                findSubNode(node, XML_NON_VISUAL_COMPONENTS);
        org.w3c.dom.NodeList childNodes = nonVisualsNode == null ? null :
                                          nonVisualsNode.getChildNodes();
        ArrayList list = new ArrayList();

        if (childNodes != null) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                org.w3c.dom.Node subnode = childNodes.item(i);
                if (subnode.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
                    continue; // ignore text nodes

                RADComponent comp = restoreComponent(subnode, null);
                if (comp != null)
                    list.add(comp);
            }
        }

        RADComponent[] nonVisualComps = new RADComponent[list.size()];
        list.toArray(nonVisualComps);
        formModel.getModelContainer().initSubComponents(nonVisualComps);
    }

    // recognizes, creates, initializes and loads a meta component
    private RADComponent restoreComponent(org.w3c.dom.Node node,
                                          RADComponent parentComponent)
    {
        String className = findAttribute(node, ATTR_COMPONENT_CLASS);
        String compName = findAttribute(node, ATTR_COMPONENT_NAME);

        try {
            Class compClass = PersistenceObjectRegistry.loadClass(className);

            RADComponent newComponent;

            if (XML_COMPONENT.equals(node.getNodeName())) {
                if (java.awt.Component.class.isAssignableFrom(compClass))
                    newComponent = new RADVisualComponent();
                else newComponent = new RADComponent();
            }
            else if (XML_MENU_COMPONENT.equals(node.getNodeName())) {
                newComponent = new RADMenuItemComponent();
            }
            else if (XML_MENU_CONTAINER.equals(node.getNodeName())) {
                newComponent = new RADMenuComponent();
            }
            else if (XML_CONTAINER.equals(node.getNodeName())) {
                if (java.awt.Container.class.isAssignableFrom(compClass))
                    newComponent = new RADVisualContainer();
                else newComponent = new RADContainer();
            }
            else return null;

            newComponent.initialize(formModel);
            newComponent.initInstance(compClass);
            newComponent.setName(compName);

            getComponentsMap().put(compName, newComponent);

            loadComponent(node, newComponent, parentComponent);

            return newComponent;
        }
        catch (Exception ex) { // or Throwable?
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();

            FormEditor.fileError(
                java.text.MessageFormat.format(
                    FormEditor.getFormBundle().getString("FMT_ERR_ComponentLoading"), // NOI18N
                    new Object [] { compName,
                                    ex.getMessage(),
                                    ex.getClass().getName() }),
                ex);

            return null;
        }
    }

    private void loadComponent(org.w3c.dom.Node node,
                               RADComponent component,
                               RADComponent parentComponent)
        throws Exception
    {
        org.w3c.dom.NodeList childNodes = node.getChildNodes();
        if (childNodes == null)
            return;

        org.w3c.dom.Node layoutNode = null;
        org.w3c.dom.Node layoutCodeNode = null;
        org.w3c.dom.Node subCompsNode = null;
        org.w3c.dom.Node constraintsNode = null;

        for (int i = 0; i < childNodes.getLength(); i++) {
            org.w3c.dom.Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
                continue; // ignore text nodes

            String nodeName = childNode.getNodeName();

            if (XML_PROPERTIES.equals(nodeName)) {
                loadProperties(childNode, component);
            }
            else if (XML_EVENTS.equals(nodeName)) {
                Collection events = loadEvents(childNode);
                if (events != null) {
                    component.getEventHandlers().initEvents(events);
                }
            }
            else if (XML_CONSTRAINTS.equals(nodeName)) {
                constraintsNode = childNode;
            }
            else if (XML_LAYOUT.equals(nodeName)) {
                if (layoutCodeNode == null)
                    layoutNode = childNode;
            }
            else if (XML_LAYOUT_CODE.equals(nodeName)) {
                layoutCodeNode = childNode;
                layoutNode = null;
            }
            else if (XML_SUB_COMPONENTS.equals(nodeName)) {
                subCompsNode = childNode;
            }
            else if (XML_AUX_VALUES.equals(nodeName)) {
                HashMap auxValues = loadAuxValues(childNode);
                if (auxValues != null) {
                    for (Iterator it = auxValues.keySet().iterator(); it.hasNext();) {
                        String auxName =(String)it.next();
                        component.setAuxValue(auxName, auxValues.get(auxName));
                    }

                    // if the component is serialized, deserialize it
                    if (JavaCodeGenerator.VALUE_SERIALIZE.equals(
                            auxValues.get(JavaCodeGenerator.AUX_CODE_GENERATION)))
                    {
                        try {
                            String serFile = (String) auxValues.get(
                                             JavaCodeGenerator.AUX_SERIALIZE_TO);
                            if (serFile == null)
                                serFile = formFile.getName() + "_" + component.getName(); // NOI18N

                            // !! [this won't work when filesystem root != classpath root]
                            String serName = formFile.getParent().getPackageName('.');
                            if (!"".equals(serName)) // NOI18N
                                serName += "."; // NOI18N
                            serName += serFile;

                            Object instance = Beans.instantiate(
                                TopManager.getDefault().currentClassLoader(),
                                serName);

                            component.setInstance(instance);
                        }
                        catch (Exception ex) { // ignore
                            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                                ex.printStackTrace();
                        }
                    }
                }
            }
            else if (XML_SYNTHETIC_PROPERTIES.equals(nodeName)) {
                loadSyntheticProperties(childNode, component);
            }
        }

        if (component instanceof RADVisualComponent
            && parentComponent instanceof RADVisualContainer
            && layoutConvIndex != LAYOUT_FROM_CODE)
        {   // this is a visual component in a visual contianer,
            // load NB 3.1 layout constraints for it
            CodeExpression compExp = component.getCodeExpression();
            LayoutSupportManager layoutSupport =
                ((RADVisualContainer)parentComponent).getLayoutSupport();

            org.w3c.dom.Node[] constrNodes = constraintsNode != null ?
                findSubNodes(constraintsNode, XML_CONSTRAINT) : null;

            if (constrNodes == null || constrNodes.length == 0)
                loadConstraints(null, compExp, layoutSupport);
            else {
                // NB 3.1 used to save all constraints ever set. We must
                // go through all of them, but only those of current layout
                // will be loaded.
                for (int i=0; i < constrNodes.length; i++)
                    loadConstraints(constrNodes[i], compExp, layoutSupport);
            }
        }

        ComponentContainer container =
                component instanceof ComponentContainer ?
                       (ComponentContainer) component : null;
        if (container == null)
            return; // this component is not a container

        // we continue in container loading

        RADVisualContainer visualContainer =
                component instanceof RADVisualContainer ?
                        (RADVisualContainer) component : null;

        int convIndex = LAYOUT_FROM_CODE;
        if (visualContainer != null && layoutNode != null) {
            // this visual container has NB 3.1 layout properties saved
            convIndex = loadLayout(layoutNode,
                                   visualContainer.getLayoutSupport());
        }

        // load subcomponents
        RADComponent[] childComponents;
        childNodes = subCompsNode != null ?
                     subCompsNode.getChildNodes() : null;
        if (childNodes != null) {
            ArrayList list = new ArrayList();
            for (int i=0, n=childNodes.getLength(); i < n; i++) {
                org.w3c.dom.Node componentNode = childNodes.item(i);
                if (componentNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
                    continue; // ignore text nodes

                // hack for dealing with multiple constraints saved by NB 3.1
                layoutConvIndex = convIndex;

                RADComponent newComp = restoreComponent(componentNode, component);

                if (newComp != null)
                    list.add(newComp);
            }

            childComponents = new RADComponent[list.size()];
            list.toArray(childComponents);
        }
        else childComponents = new RADComponent[0];

        if (visualContainer != null && layoutCodeNode != null) {
            // this visual container has complete layout code saved (doesn't
            // use NB 3.1 format for saving layout properties and constraints)
            loadLayoutCode(layoutCodeNode);
        }

        // initialize layout support from restored code
        if (visualContainer != null) {
            if (visualContainer.getLayoutSupport().initializeFromCode()) {
                visualContainer.initSubComponents(childComponents);
                visualContainer.getLayoutSupport().setupPrimaryContainer();
            }
            else {
                System.err.println("[WARNING] Cannot initialize layout support class for container: " // NOI18N
                                   + visualContainer.getName() + " [" // NOI18N
                                   + visualContainer.getBeanClass().getName() + "]"); // NOI18N
                visualContainer.initSubComponents(childComponents);
                // [this won't work !!]
            }
        }
        else // non-visual container
            container.initSubComponents(childComponents);

        // hack for properties that can't be set until all subcomponents
        // are added to the container
        List postProps;
        if (containerDependentProperties != null
            && (postProps = (List) containerDependentProperties
                                       .get(component)) != null)
        {
            for (Iterator it = postProps.iterator(); it.hasNext(); ) {
                RADProperty prop = (RADProperty) it.next();
                Object propValue = it.next();
                try {
                    prop.setValue(propValue);
                }
                catch (Exception e) { // ignore
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                        e.printStackTrace();
                }
            }
        }
    }

    private void loadConstraints(org.w3c.dom.Node node,
                                 CodeExpression compExp,
                                 LayoutSupportManager layoutSupport)
    {
        int convIndex = -1;
        String layout31ConstraintName = node != null ?
                   findAttribute(node, ATTR_CONSTRAINT_VALUE) : null;
        if (layout31ConstraintName != null)
            for (int i=0; i < layout31ConstraintsNames.length; i++)
                if (layout31ConstraintName.equals(layout31ConstraintsNames[i])) {
                    convIndex = i;
                    break;
                }

        // skip constraints saved by NB 3.1 which are not for the current layout
        if (convIndex >= 0 && layoutConvIndex >= 0
                && convIndex != layoutConvIndex)
            return;

        org.w3c.dom.Node constrNode = null;
        org.w3c.dom.NamedNodeMap constrAttr = null;

        if (convIndex >= 0 && reasonable31Constraints[convIndex]) {
            org.w3c.dom.NodeList children = node.getChildNodes();
            if (children != null)
                for (int i=0, n=children.getLength(); i < n; i++) {
                    org.w3c.dom.Node cNode = children.item(i);
                    if (cNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        constrNode = cNode;
                        constrAttr = cNode.getAttributes();
                        break;
                    }
                }
        }

        try { // obligatory try/catch block for finding methods and constructors

        if (constrNode == null) { // no constraints found
            if (convIndex < 0 && layoutConvIndex == LAYOUT_JSCROLL) {
                // JScrollPane requires special add code although there are
                // no constraints ...
                if (setViewportViewMethod == null)
                    setViewportViewMethod =
                            javax.swing.JScrollPane.class.getMethod(
                                    "setViewportView", // NOI18N
                                    new Class[] { java.awt.Component.class });

                CodeStructure.createStatement(
                                  layoutSupport.getContainerCodeExpression(),
                                  setViewportViewMethod,
                                  new CodeExpression[] { compExp });
            }
            else { // create simple add method statement with no constraints
                CodeStructure.createStatement(
                        layoutSupport.getContainerDelegateCodeExpression(),
                        getSimpleAddMethod(),
                        new CodeExpression[] { compExp });
            }
            return;
        }

        CodeStructure codeStructure = layoutSupport.getCodeStructure();
        CodeExpression contCodeExp = layoutSupport.getContainerCodeExpression();
        CodeExpression contDelCodeExp =
            layoutSupport.getContainerDelegateCodeExpression();

        if (convIndex == LAYOUT_BORDER) {
            if (!"BorderConstraints".equals(constrNode.getNodeName())) // NOI18N
                return; // should not happen

            node = constrAttr.getNamedItem("direction"); // NOI18N
            if (node != null) {
                String strValue = node.getNodeValue();
                // create add method statement
                CodeStructure.createStatement(
                    contDelCodeExp,
                    getAddWithConstrMethod(),
                    new CodeExpression[] { compExp,
                                           codeStructure.createExpression(
                                                           String.class,
                                                           strValue,
                                                           strValue) });
            }
        }

        else if (convIndex == LAYOUT_GRIDBAG) {
            if (!"GridBagConstraints".equals(constrNode.getNodeName())) // NOI18N
                return; // should not happen

            // create GridBagConstraints constructor expression
            if (gridBagConstrConstructor == null)
                gridBagConstrConstructor =
                    java.awt.GridBagConstraints.class.getConstructor(
                                                          new Class[0]);

            CodeExpression constrExp = codeStructure.createExpression(
                    gridBagConstrConstructor, CodeStructure.EMPTY_PARAMS);

            // create statements for GridBagConstraints fields
            String[] gbcAttrs = new String[] {
                "gridX", "gridY", "gridWidth", "gridHeight", // NOI18N
                "fill", "ipadX", "ipadY", // NOI18N
                "anchor", "weightX", "weightY" }; // NOI18N
            String[] gbcFields = new String[] {
                "gridx", "gridy", "gridwidth", "gridheight", // NOI18N
                "fill", "ipadx", "ipady", // NOI18N
                "anchor", "weightx", "weighty" }; // NOI18N

            for (int i=0; i < gbcAttrs.length; i++) {
                node = constrAttr.getNamedItem(gbcAttrs[i]);
                if (node != null) {
                    Class valueType;
                    Object value;
                    String strValue = node.getNodeValue();
                    if (i < 8) { // treat as int
                        valueType = Integer.TYPE;
                        value = Integer.valueOf(strValue);
                    }
                    else { // treat as double
                        valueType = Double.TYPE;
                        value = Double.valueOf(strValue);
                    }

                    CodeStructure.createStatement(
                        constrExp,
                        java.awt.GridBagConstraints.class.getField(gbcFields[i]),
                        codeStructure.createExpression(valueType, value, strValue));
                }
            }

            // Insets
            CodeExpression[] insetsParams = new CodeExpression[4];
            String[] insetsAttrs = new String[] {
                "insetsTop", "insetsLeft", "insetsBottom", "insetsRight" }; // NOI18N

            for (int i=0; i < insetsAttrs.length; i++) {
                node = constrAttr.getNamedItem(insetsAttrs[i]);
                String strValue = node != null ? node.getNodeValue() : "0"; // NOI18N
                insetsParams[i] = codeStructure.createExpression(
                                                    Integer.TYPE,
                                                    Integer.valueOf(strValue),
                                                    strValue);
            }

            if (insetsConstructor == null)
                insetsConstructor = java.awt.Insets.class.getConstructor(
                    new Class[] { Integer.TYPE, Integer.TYPE,
                                  Integer.TYPE, Integer.TYPE });

            CodeStructure.createStatement(
                          constrExp,
                          java.awt.GridBagConstraints.class.getField("insets"), // NOI18N
                          codeStructure.createExpression(insetsConstructor,
                                                         insetsParams));

            // create add method statement
            CodeStructure.createStatement(
                contDelCodeExp,
                getAddWithConstrMethod(),
                new CodeExpression[] { compExp, constrExp });
        }

        else if (convIndex == LAYOUT_JTAB) {
            if (!"JTabbedPaneConstraints".equals(constrNode.getNodeName())) // NOI18N
                return; // should not happen

            Object tabName = null;
            Object toolTip = null;
            Object icon = null;

            org.w3c.dom.Node[] propNodes = findSubNodes(constrNode, XML_PROPERTY);
            if (propNodes != null)
                for (int i=0; i < propNodes.length; i++) {
                    node = propNodes[i];
                    Object value;
                    try {
                        value = getEncodedPropertyValue(node);
                    }
                    catch (Exception ex) {
                        if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                            ex.printStackTrace();
                        continue;
                    }

                    String name = findAttribute(node, ATTR_PROPERTY_NAME);
                    if ("tabTitle".equals(name)) // NOI18N
                        tabName = value;
                    else if ("tabToolTip".equals(name)) // NOI18N
                        toolTip = value;
                    else if ("icon".equals(name)) // NOI18N
                        icon = value;
                }

            if (tabName == null
                    && (node = constrAttr.getNamedItem("tabName")) != null) // NOI18N
                tabName = node.getNodeValue();
            if (toolTip == null
                    && (node = constrAttr.getNamedItem("toolTip")) != null) // NOI18N
                toolTip = node.getNodeValue();

            if (tabName != null && icon != null && toolTip != null) {
                if (addTabMethod1 == null)
                    addTabMethod1 = javax.swing.JTabbedPane.class.getMethod(
                                    "addTab", // NOI18N
                                    new Class[] { String.class,
                                                  javax.swing.Icon.class,
                                                  java.awt.Component.class,
                                                  String.class });
                CodeStructure.createStatement(
                    contCodeExp,
                    addTabMethod1,
                    new CodeExpression[] { codeStructure.createExpression(
                                                      tabName.getClass(),
                                                      tabName,
                                                      tabName.toString()),
                                        codeStructure.createExpression(
                                                      icon.getClass(),
                                                      icon,
                                                      icon.toString()), // [??]
                                        compExp,
                                        codeStructure.createExpression(
                                                      toolTip.getClass(),
                                                      toolTip,
                                                      toolTip.toString()) });
            }
            else if (tabName != null && icon != null) {
                if (addTabMethod2 == null)
                    addTabMethod2 = javax.swing.JTabbedPane.class.getMethod(
                                    "addTab", // NOI18N
                                    new Class[] { String.class,
                                                  javax.swing.Icon.class,
                                                  java.awt.Component.class });
                CodeStructure.createStatement(
                    contCodeExp,
                    addTabMethod2,
                    new CodeExpression[] { codeStructure.createExpression(
                                                      tabName.getClass(),
                                                      tabName,
                                                      tabName.toString()),
                                        codeStructure.createExpression(
                                                      icon.getClass(),
                                                      icon,
                                                      icon.toString()), // [??]
                                        compExp });
            }
            else if (tabName != null) {
                if (addTabMethod3 == null)
                    addTabMethod3 = javax.swing.JTabbedPane.class.getMethod(
                                    "addTab", // NOI18N
                                    new Class[] { String.class,
                                                  java.awt.Component.class });
                CodeStructure.createStatement(
                    contCodeExp,
                    addTabMethod3,
                        new CodeExpression[] { codeStructure.createExpression(
                                                      tabName.getClass(),
                                                      tabName,
                                                      tabName.toString()),
                                        compExp });
            }
        }

        else if (convIndex == LAYOUT_JSPLIT) {
            if (!"JSplitPaneConstraints".equals(constrNode.getNodeName())) // NOI18N
                return;

            node = constrAttr.getNamedItem("position");
            if (node != null) {
                String position = node.getNodeValue();
                Method addMethod;

                if ("top".equals(position)) { // NOI18N
                    if (setTopComponentMethod == null)
                        setTopComponentMethod =
                            javax.swing.JSplitPane.class.getMethod(
                                    "setTopComponent", // NOI18N
                                    new Class[] { java.awt.Component.class });
                    addMethod = setTopComponentMethod;
                }
                else if ("bottom".equals(position)) { // NOI18N
                    if (setBottomComponentMethod == null)
                        setBottomComponentMethod =
                            javax.swing.JSplitPane.class.getMethod(
                                    "setBottomComponent", // NOI18N
                                    new Class[] { java.awt.Component.class });
                    addMethod = setBottomComponentMethod;
                }
                else if ("left".equals(position)) { // NOI18N
                    if (setLeftComponentMethod == null)
                        setLeftComponentMethod =
                            javax.swing.JSplitPane.class.getMethod(
                                    "setLeftComponent", // NOI18N
                                    new Class[] { java.awt.Component.class });
                    addMethod = setLeftComponentMethod;
                }
                else if ("right".equals(position)) { // NOI18N
                    if (setRightComponentMethod == null)
                        setRightComponentMethod =
                            javax.swing.JSplitPane.class.getMethod(
                                    "setRightComponent", // NOI18N
                                    new Class[] { java.awt.Component.class });
                    addMethod = setRightComponentMethod;
                }
                else return;

                CodeStructure.createStatement(contCodeExp,
                                              addMethod,
                                              new CodeExpression[] { compExp });
            }
        }

        else if (convIndex == LAYOUT_CARD) {
            if (!"CardConstraints".equals(constrNode.getNodeName())) // NOI18N
                return;

            node = constrAttr.getNamedItem("cardName"); // NOI18N
            if (node != null) {
                String strValue = node.getNodeValue();
                // create add method statement
                CodeStructure.createStatement(
                    contDelCodeExp,
                    getAddWithConstrMethod(),
                    new CodeExpression[] { compExp,
                                           codeStructure.createExpression(
                                                           String.class,
                                                           strValue,
                                                           strValue) });
            }
        }

        else if (convIndex == LAYOUT_JLAYER) {
            if (!"JLayeredPaneConstraints".equals(constrNode.getNodeName())) // NOI18N
                return;

            CodeExpression[] boundsParams = new CodeExpression[4];
            String[] boundsAttrs = new String[] { "x", "y", "width", "height" }; // NOI18N

            for (int i=0; i < boundsAttrs.length; i++) {
                node = constrAttr.getNamedItem(boundsAttrs[i]);
                String strValue = node != null ?
                                      node.getNodeValue() :
                                      (i < 2 ? "0" : "-1"); // NOI18N
                boundsParams[i] = codeStructure.createExpression(
                                                    Integer.TYPE,
                                                    Integer.valueOf(strValue),
                                                    strValue);
            }

            if (setBoundsMethod == null)
                setBoundsMethod = java.awt.Component.class.getMethod(
                                    "setBounds", // NOI18N
                                    new Class[] { Integer.TYPE, Integer.TYPE,
                                                  Integer.TYPE, Integer.TYPE });
            CodeStructure.createStatement(
                            compExp, setBoundsMethod, boundsParams);

            node = constrAttr.getNamedItem("layer"); // NOI18N
            if (node != null) {
                String strValue = node.getNodeValue();
                // create add method statement
                CodeStructure.createStatement(
                    contDelCodeExp,
                    getAddWithConstrMethod(),
                    new CodeExpression[] { compExp,
                                           codeStructure.createExpression(
                                                           String.class,
                                                           strValue,
                                                           strValue) });
            }
        }

        else if (convIndex == LAYOUT_ABSOLUTE) {
            if (!"AbsoluteConstraints".equals(constrNode.getNodeName())) // NOI18N
                return;

            CodeExpression[] boundsParams = new CodeExpression[4];
            String[] boundsAttrs = new String[] { "x", "y", "width", "height" }; // NOI18N

            for (int i=0; i < boundsAttrs.length; i++) {
                node = constrAttr.getNamedItem(boundsAttrs[i]);
                String strValue = node != null ?
                                      node.getNodeValue() :
                                      (i < 2 ? "0" : "-1"); // NOI18N
                boundsParams[i] = codeStructure.createExpression(
                                                    Integer.TYPE,
                                                    Integer.valueOf(strValue),
                                                    strValue);
            }

            Iterator it = CodeStructure.getDefinedStatementsIterator(contDelCodeExp);
            CodeStatement[] statements = CodeStructure.filterStatements(
                                                it, getSetLayoutMethod());
            boolean nullLayout;
            if (statements.length > 0) {
                CodeExpression layoutExp =
                    statements[0].getStatementParameters()[0];
                nullLayout = layoutExp.getOrigin().getType()
                             != org.netbeans.lib.awtextra.AbsoluteLayout.class;
            }
            else nullLayout = true;

            if (nullLayout) {
                if (setBoundsMethod == null)
                    setBoundsMethod = java.awt.Component.class.getMethod(
                                      "setBounds", // NOI18N
                                      new Class[] { Integer.TYPE, Integer.TYPE,
                                                    Integer.TYPE, Integer.TYPE });
                CodeStructure.createStatement(
                    compExp, setBoundsMethod, boundsParams);

                // create add method statement
                CodeStructure.createStatement(contDelCodeExp,
                                              getSimpleAddMethod(),
                                              new CodeExpression[] { compExp });
            }
            else {
                if (absoluteConstraintsConstructor == null)
                    absoluteConstraintsConstructor =
                        org.netbeans.lib.awtextra.AbsoluteConstraints.class
                                                      .getConstructor(
                            new Class[] { Integer.TYPE, Integer.TYPE,
                                          Integer.TYPE, Integer.TYPE });

                // create add method statement
                CodeStructure.createStatement(
                    contDelCodeExp,
                    getAddWithConstrMethod(),
                    new CodeExpression[] { compExp,
                                        codeStructure.createExpression(
                                            absoluteConstraintsConstructor,
                                            boundsParams) });
            }
        }

        }
        catch (NoSuchMethodException ex) { // should not happen
            ex.printStackTrace();
        }
        catch (NoSuchFieldException ex) { // should not happen
            ex.printStackTrace();
        }
    }

    private static Method getSimpleAddMethod() {
        if (simpleAddMethod == null) {
            try {
                simpleAddMethod = java.awt.Container.class.getMethod(
                                      "add", // NOI18N
                                      new Class[] { java.awt.Component.class });
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
            }
        }
        return simpleAddMethod;
    }

    private static Method getAddWithConstrMethod() {
        if (addWithConstrMethod == null) {
            try {
                addWithConstrMethod = java.awt.Container.class.getMethod(
                                      "add", // NOI18N
                                      new Class[] { java.awt.Component.class,
                                                    Object.class });
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
            }
        }
        return addWithConstrMethod;
    }

    private static Method getSetLayoutMethod() {
        if (setLayoutMethod == null) {
            try {
                setLayoutMethod = java.awt.Container.class.getMethod(
                            "setLayout", // NOI18N
                            new Class[] { java.awt.LayoutManager.class });
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
            }
        }
        return setLayoutMethod;
    }

    private int loadLayout(org.w3c.dom.Node layoutNode,
                           LayoutSupportManager layoutSupport)
    {
        org.w3c.dom.NamedNodeMap layoutAttr = layoutNode.getAttributes();
        org.w3c.dom.Node node = layoutAttr.getNamedItem(ATTR_LAYOUT_CLASS);
        if (node == null)
            return -1;

        String layout31Name = PersistenceObjectRegistry.getClassName(
                                                            node.getNodeValue());
        int convIndex = -1;
        for (int i=0; i < layout31Names.length; i++)
            if (layout31Name.equals(layout31Names[i])) {
                convIndex = i;
                break;
            }

        if (convIndex < 0)
            return -1; // unknown layout

        org.w3c.dom.Node[] propNodes = findSubNodes(layoutNode, XML_PROPERTY);
        String[] propertyNames;
        Object[] propertyValues;

        if (propNodes != null && propNodes.length > 0) {
            propertyNames = new String[propNodes.length];
            propertyValues = new Object[propNodes.length];
            for (int i=0; i < propNodes.length; i++) {
                node = propNodes[i];
                propertyNames[i] = findAttribute(node, ATTR_PROPERTY_NAME);
                try {
                    propertyValues[i] = getEncodedPropertyValue(node);
                }
                catch (Exception ex) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                        ex.printStackTrace();
                }
            }
        }
        else {
            propertyNames = null;
            propertyValues = null;
        }

        CodeStructure codeStructure = layoutSupport.getCodeStructure();
        CodeExpression[] layoutParams = null;
        Class[] paramTypes = null;
        Class layoutClass = null;

        String[] layoutPropNames = layout31PropertyNames[convIndex];
        if (convIndex == LAYOUT_BORDER) {
            int hgap = findName(layoutPropNames[0], propertyNames);
            int vgap = findName(layoutPropNames[1], propertyNames);
            if (hgap >= 0 || vgap >= 0) {
                layoutParams = new CodeExpression[2];
                Object value;

                value = hgap >= 0 ? propertyValues[hgap] : new Integer(0);
                layoutParams[0] = codeStructure.createExpression(
                                      Integer.TYPE, value, value.toString());

                value = vgap >= 0 ? propertyValues[vgap] : new Integer(0);
                layoutParams[1] = codeStructure.createExpression(
                                      Integer.TYPE, value, value.toString());

                paramTypes = new Class[] { Integer.TYPE, Integer.TYPE };
            }
            else {
                layoutParams = CodeStructure.EMPTY_PARAMS;
                paramTypes = new Class[0];
            }
            layoutClass = java.awt.BorderLayout.class;
        }

        else if (convIndex == LAYOUT_FLOW) {
            int alignment = findName(layoutPropNames[0], propertyNames);
            int hgap = findName(layoutPropNames[1], propertyNames);
            int vgap = findName(layoutPropNames[2], propertyNames);
            if (hgap >= 0 || vgap >= 0) {
                layoutParams = new CodeExpression[3];
                Object value;

                value = alignment >= 0 ? propertyValues[alignment] : new Integer(1);
                layoutParams[0] = codeStructure.createExpression(
                                      Integer.TYPE, value, value.toString());

                value = hgap >= 0 ? propertyValues[hgap] : new Integer(5);
                layoutParams[1] = codeStructure.createExpression(
                                      Integer.TYPE, value, value.toString());

                value = vgap >= 0 ? propertyValues[vgap] : new Integer(5);
                layoutParams[2] = codeStructure.createExpression(
                                      Integer.TYPE, value, value.toString());

                paramTypes = new Class[] { Integer.TYPE, Integer.TYPE, Integer.TYPE };
            }
            else if (alignment >= 0) {
                layoutParams = new CodeExpression[1];
                Object value;

                value = alignment >= 0 ? propertyValues[alignment] : new Integer(1);
                layoutParams[0] = codeStructure.createExpression(
                                      Integer.TYPE, value, value.toString());

                paramTypes = new Class[] { Integer.TYPE };
            }
            else {
                layoutParams = CodeStructure.EMPTY_PARAMS;
                paramTypes = new Class[0];
            }
            layoutClass = java.awt.FlowLayout.class;
        }

        else if (convIndex == LAYOUT_GRIDBAG) {
            layoutParams = CodeStructure.EMPTY_PARAMS;
            paramTypes = new Class[0];
            layoutClass = java.awt.GridBagLayout.class;
        }

        else if (convIndex == LAYOUT_BOX) {
            int axis = findName(layoutPropNames[0],
                                propertyNames);
            Object axisObj = axis >= 0 ?
                                 propertyValues[axis] :
                                 new Integer(javax.swing.BoxLayout.X_AXIS);

            layoutParams = new CodeExpression[2];
            layoutParams[0] = layoutSupport.getContainerCodeExpression();
            layoutParams[1] = codeStructure.createExpression(Integer.TYPE,
                                                             axisObj,
                                                             axisObj.toString());
            paramTypes = new Class[] { java.awt.Container.class, Integer.TYPE };
            layoutClass = javax.swing.BoxLayout.class;
        }

        else if (convIndex == LAYOUT_GRID) {
            int rows = findName(layoutPropNames[0], propertyNames);
            int columns = findName(layoutPropNames[1], propertyNames);
            int hgap = findName(layoutPropNames[2], propertyNames);
            int vgap = findName(layoutPropNames[3], propertyNames);
            if (hgap >= 0 || vgap >= 0) {
                layoutParams = new CodeExpression[4];
                Object value;

                value = rows >= 0 ? propertyValues[rows] : new Integer(1);
                layoutParams[0] = codeStructure.createExpression(
                                      Integer.TYPE, value, value.toString());

                value = columns >= 0 ? propertyValues[columns] : new Integer(0);
                layoutParams[1] = codeStructure.createExpression(
                                      Integer.TYPE, value, value.toString());

                value = hgap >= 0 ? propertyValues[hgap] : new Integer(0);
                layoutParams[2] = codeStructure.createExpression(
                                      Integer.TYPE, value, value.toString());

                value = vgap >= 0 ? propertyValues[vgap] : new Integer(0);
                layoutParams[3] = codeStructure.createExpression(
                                      Integer.TYPE, value, value.toString());

                paramTypes = new Class[] { Integer.TYPE, Integer.TYPE,
                                           Integer.TYPE, Integer.TYPE };
            }
            else if (rows >= 0 || columns >= 0) {
                layoutParams = new CodeExpression[2];
                Object value;

                value = rows >= 0 ? propertyValues[rows] : new Integer(1);
                layoutParams[0] = codeStructure.createExpression(
                                      Integer.TYPE, value, value.toString());

                value = columns >= 0 ? propertyValues[columns] : new Integer(0);
                layoutParams[1] = codeStructure.createExpression(
                                      Integer.TYPE, value, value.toString());

                paramTypes = new Class[] { Integer.TYPE, Integer.TYPE };
            }
            else {
                layoutParams = CodeStructure.EMPTY_PARAMS;
                paramTypes = new Class[0];
            }
            layoutClass = java.awt.GridLayout.class;
        }

        else if (convIndex == LAYOUT_CARD) {
            int hgap = findName(layoutPropNames[0], propertyNames);
            int vgap = findName(layoutPropNames[1], propertyNames);
            if (hgap >= 0 && vgap >= 0) {
                layoutParams = new CodeExpression[2];
                Object value;

                value = hgap >= 0 ? propertyValues[hgap] : new Integer(0);
                layoutParams[0] = codeStructure.createExpression(
                                      Integer.TYPE, value, value.toString());

                value = vgap >= 0 ? propertyValues[vgap] : new Integer(0);
                layoutParams[1] = codeStructure.createExpression(
                                      Integer.TYPE, value, value.toString());

                paramTypes = new Class[] { Integer.TYPE, Integer.TYPE };
            }
            else {
                layoutParams = CodeStructure.EMPTY_PARAMS;
                paramTypes = new Class[0];
            }
            layoutClass = java.awt.CardLayout.class;
        }

        else if (convIndex == LAYOUT_ABSOLUTE) {
            boolean nullLayout = false;
            int i = findName("useNullLayout", propertyNames); // NOI18N
            if (i >= 0)
                nullLayout = Boolean.TRUE.equals(propertyValues[i]); 

            layoutParams = CodeStructure.EMPTY_PARAMS;
            paramTypes = new Class[0];
            layoutClass = nullLayout ? null :
                          org.netbeans.lib.awtextra.AbsoluteLayout.class;
        }

        else return convIndex; // no layout manager

        CodeExpression layoutExp;
        if (layoutClass != null) {
            Constructor layoutConstructor;
            try {
                layoutConstructor = layoutClass.getConstructor(paramTypes);
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
                return -1;
            }
            layoutExp = layoutSupport.getCodeStructure().createExpression(
                                layoutConstructor, layoutParams);
        }
        else {
            layoutExp = layoutSupport.getCodeStructure().createNullExpression(
                                                 java.awt.LayoutManager.class);
        }

        CodeStructure.createStatement(
            layoutSupport.getContainerDelegateCodeExpression(),
            getSetLayoutMethod(),
            new CodeExpression[] { layoutExp });

        return convIndex;
    }

    private static int findName(String name, String[] names) {
        if (names != null)
            for (int i=0; i < names.length; i++)
                if (name.equals(names[i]))
                    return i;
        return -1;
    }

    private void loadLayoutCode(org.w3c.dom.Node node) {
        org.w3c.dom.NodeList childNodes = node.getChildNodes();
        if (childNodes != null) {
//            codeFlow = true;
            for (int i=0, n=childNodes.getLength(); i < n; i++) {
                org.w3c.dom.Node childNode = childNodes.item(i);

                if (XML_CODE_STATEMENT.equals(childNode.getNodeName()))
                    loadCodeStatement(childNode, null);
            }
        }
    }

    private void loadProperties(org.w3c.dom.Node node, RADComponent comp) {
        org.w3c.dom.Node[] propNodes = findSubNodes(node, XML_PROPERTY);
        if (propNodes.length > 0) {
            for (int i = 0; i < propNodes.length; i++) {
                Object propValue;
                try {
                    propValue = getEncodedPropertyValue(propNodes[i], comp);
                    if (propValue == NO_VALUE) {
                        // the value was not saved, just the pre/post code, which was already set inside the getEncodedPropertyValue method
                        continue;
                    }
                } catch (Exception e) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) e.printStackTrace(); // NOI18N
                    // [PENDING - notify error]
                    continue; // ignore this property
                }

                String propName = findAttribute(propNodes[i], ATTR_PROPERTY_NAME);
                String propType = findAttribute(propNodes[i], ATTR_PROPERTY_TYPE);

                RADProperty prop = comp.getPropertyByName(propName);
                if (prop == null)
                    continue; // property doesn't exist

                String propertyEditor = findAttribute(propNodes[i], ATTR_PROPERTY_EDITOR);
                if (propertyEditor != null) {
                    try {
                        Class editorClass = PersistenceObjectRegistry.loadClass(propertyEditor);
                        Class propertyClass = getClassFromString(propType);
                        PropertyEditor ed = createPropertyEditor(editorClass, propertyClass, prop);
                        ((RADProperty)prop).setCurrentEditor(ed);
                    } catch (Exception e) {
                        if (Boolean.getBoolean("netbeans.debug.exceptions")) e.printStackTrace(); // NOI18N
                        // ignore
                    }
                }

                // hack for properties that can't be set until all children 
                // are added to the container
                if (FormUtils.isContainerContentDependentProperty(
                                    comp.getBeanClass(), prop.getName())) {
                    List propList;
                    if (containerDependentProperties != null) {
                        propList = (List) containerDependentProperties.get(comp);
                    }
                    else {
                        containerDependentProperties = new HashMap();
                        propList = null;
                    }
                    if (propList == null) {
                        propList = new LinkedList();
                        containerDependentProperties.put(comp, propList);
                    }

                    propList.add(prop);
                    propList.add(propValue);
                    continue;
                }

                try {
                    prop.setValue(propValue);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                        e.printStackTrace();
                    // ignore this property // [PENDING]
                } catch (IllegalAccessException e) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                        e.printStackTrace();
                    // ignore this property // [PENDING]
                } catch (Exception e) {
                    // unexpected exception - always printed
                    e.printStackTrace();
                    // ignore this property
                }
            }
        }
    }

    private void loadSyntheticProperties(org.w3c.dom.Node node, RADComponent comp) {
        org.w3c.dom.Node[] propNodes = findSubNodes(node, XML_SYNTHETIC_PROPERTY);
        if (propNodes.length > 0) {
            for (int i = 0; i < propNodes.length; i++) {
                String propName = findAttribute(propNodes[i], ATTR_PROPERTY_NAME);
                String encodedValue = findAttribute(propNodes[i], ATTR_PROPERTY_VALUE);
                String propType = findAttribute(propNodes[i], ATTR_PROPERTY_TYPE);

                Class propClass = null;
                try {
                    if (propType != null)
                        propClass = getClassFromString(propType);
                } catch (Exception e2) {
                    // OK, try to use decodeValue in this case
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) e2.printStackTrace(); // NOI18N
                }
                Object propValue=null;
                //System.out.println("loading name="+propName+", encodedValue="+encodedValue); // NOI18N
                try {
                    if (propClass != null) {
                        try {
                            propValue = decodePrimitiveValue(encodedValue, propClass);
                        } catch (IllegalArgumentException e) {
                            // not a primitive type
                            propValue = decodeValue(encodedValue);
                        }
                    } else { // info about the property type was not saved
                        propValue = decodeValue(encodedValue);
                    }
                } catch (IOException e) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) e.printStackTrace(); // NOI18N
                    // [PENDING - handle error]
                }

                // compatibility hack for loading form's menu bar
                if ("menuBar".equals(propName) && propValue instanceof String
                    && comp instanceof RADVisualFormContainer)
                {
                    RADComponent[] nvComps = comp.getFormModel().getNonVisualComponents();
                    for (int j=0; j < nvComps.length; j++)
                        if (nvComps[j] instanceof RADMenuComponent
                            && propValue.equals(nvComps[j].getName()))
                        {
                            RADMenuComponent menuComp = (RADMenuComponent) nvComps[j];
                            RADVisualFormContainer formCont = (RADVisualFormContainer) comp;
                            menuComp.getFormModel().removeComponentFromContainer(menuComp);
                            formCont.add(menuComp);
                            menuComp.setParentComponent(formCont);
                            break;
                        }
                    continue;
                }

                //System.out.println("......encoded to:"+propValue); // NOI18N

                Node.Property [] props = comp.getSyntheticProperties();
                Node.Property prop=null;
                for (int j=0, n=props.length; j<n; j++) {
                    if (props[j].getName().equals(propName)) {
                        prop = props [j];
                        break;
                    }
                }

                if (prop == null)       // unknown property, ignore
                    continue;

                try {
                    prop.setValue(propValue);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) e.printStackTrace(); // NOI18N
                    // ignore this property // [PENDING]
                } catch (IllegalAccessException e) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) e.printStackTrace(); // NOI18N
                    // ignore this property // [PENDING]
                } catch (Exception e) {
                    // unexpected exception - always printed
                    e.printStackTrace();
                    // ignore this property
                }
            }
        }
    }

    private Collection loadEvents(org.w3c.dom.Node node) {
        org.w3c.dom.NodeList children = node.getChildNodes();
        if (children != null) {
            ArrayList events = new ArrayList();

            for (int i=0, n=children.getLength(); i < n; i++) {
                if (children.item(i).getNodeType() == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes

                if (XML_EVENT.equals(children.item(i).getNodeName())) {
                    String eventName = findAttribute(children.item(i), ATTR_EVENT_NAME);
                    String eventListener = findAttribute(children.item(i), ATTR_EVENT_LISTENER);
                    String paramTypes = findAttribute(children.item(i), ATTR_EVENT_PARAMS);
                    String eventHandlers = findAttribute(children.item(i), ATTR_EVENT_HANDLER);
                    if (eventName != null && eventHandlers != null) { // [PENDING - error check]
                        events.add(new Event.EventInfo(eventName,eventListener,paramTypes,eventHandlers));
                    }
                }
            }
            return events;
        }
        return null;
    }

    private HashMap loadAuxValues(org.w3c.dom.Node node) {
        HashMap auxTable = new HashMap(20);

        org.w3c.dom.NodeList children = node.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes

                if (XML_AUX_VALUE.equals(children.item(i).getNodeName())) {

                    String auxName = findAttribute(children.item(i), ATTR_AUX_NAME);
                    String auxValue = findAttribute(children.item(i), ATTR_AUX_VALUE);
                    String auxValueClass = findAttribute(children.item(i), ATTR_AUX_VALUE_TYPE);
                    if ((auxName != null) &&(auxValue != null)) { // [PENDING - error check]
                        try {
                            Object auxValueDecoded = null;
                            Class auxValueType = null;
                            if (auxValueClass != null) {
                                try {
                                    auxValueType = getClassFromString(auxValueClass);
                                } catch (Exception e2) {
                                    // OK, try to use decodeValue in this case
                                    if (Boolean.getBoolean("netbeans.debug.exceptions")) e2.printStackTrace(); // NOI18N
                                }
                            }
                            if (auxValueType != null) {
                                try {
                                    auxValueDecoded = decodePrimitiveValue(auxValue, auxValueType);
                                } catch (IllegalArgumentException e3) {
                                    // not decoded as primitive value
                                    auxValueDecoded = decodeValue(auxValue);
                                }
                            } else {
                                // info about property class not stored
                                auxValueDecoded = decodeValue(auxValue);
                            }
                            auxTable.put(auxName, auxValueDecoded);
                        } catch (IOException e) {
                            if (Boolean.getBoolean("netbeans.debug.exceptions")) e.printStackTrace(); // NOI18N
                            // [PENDING - handle error]
                        }
                    }
                }
            }
        }
        return auxTable;
    }

    /** Called to actually save the form represented by specified FormModel into specified formObject.
     * @param formObject the FormDataObject which represents the form files
     * @param manager the FormModel representing the form to be saved
     * @exception IOException if any problem occured when saving the form
     */
    public void saveForm(FormDataObject formObject, FormModel formModel)
        throws IOException
    {
        formFile = formObject.getFormEntry().getFile();
        if (formFile.isReadOnly()) // should not happen
            throw new IllegalStateException("Tried to save read-only form: "+formFile.getName()); // NOI18N

        if (formModel != this.formModel) {
            this.formModel = formModel;
            formInfoName = null;
        }

        FileLock lock = null;
        java.io.OutputStream os = null;
        String encoding = "UTF-8"; // NOI18N

        try {
            lock = formFile.lock();
            StringBuffer buf1 = new StringBuffer();
            StringBuffer buf2 = new StringBuffer();

            lastExpId = 0; // CodeExpression ID counter
            if (expressions != null)
                expressions.clear();
            if (savedVariables != null)
                savedVariables.clear();

            // start with the lowest version; if there is nothing in the
            // form that requires higher format version, then the form file
            // will be compatible with NB 3.2
            formatVersion = NB32_VERSION;

            RADComponent topComp = formModel.getTopRADComponent();
            RADVisualFormContainer formCont =
                topComp instanceof RADVisualFormContainer ?
                    (RADVisualFormContainer) topComp : null;

            // store XML file header
            buf1.append("<?xml version=\"1.0\" encoding=\"");
            buf1.append(encoding);
            buf1.append("\" ?>\n\n"); // NOI18N

            // store "Other Components"
            RADComponent[] nonVisuals = formModel.getNonVisualComponents();

            // compatibility hack for saving form's menu bar (part I)
            if (formCont != null && formCont.getContainerMenu() != null) {
                RADComponent[] comps = new RADComponent[nonVisuals.length + 1];
                System.arraycopy(nonVisuals, 0, comps, 0, nonVisuals.length);
                comps[nonVisuals.length] = formCont.getContainerMenu();
                nonVisuals = comps;
            }

            if (nonVisuals.length > 0) {
                buf2.append(ONE_INDENT);
                addElementOpen(buf2, XML_NON_VISUAL_COMPONENTS);

                for (int i = 0; i < nonVisuals.length; i++)
                    saveAnyComponent(nonVisuals[i],
                                     buf2, ONE_INDENT + ONE_INDENT,
                                     true);

                buf2.append(ONE_INDENT);
                addElementClose(buf2, XML_NON_VISUAL_COMPONENTS);
            }

            // store form main hierarchy
            if (topComp != null) {
                saveAnyComponent(topComp, buf2, ONE_INDENT, false);

                if (!(topComp instanceof RADVisualContainer))
                    raiseFormatVersion(CURRENT_VERSION);
            }
            addElementClose(buf2, XML_FORM);

            // determine FormInfo type (for backward compatibility)
            String compatFormInfo = getCompatibleFormInfoName(
                                                   formModel.getFormBaseClass(),
                                                   formInfoName);

            // add form specification element at the beginning of the form file
            // (this is done in the end because the required form version is
            // not determined until all data is saved)
            if (compatFormInfo == null) {
                raiseFormatVersion(CURRENT_VERSION);

                addElementOpenAttr(buf1, XML_FORM,
                    new String[] { ATTR_FORM_VERSION },
                    new String[] { formatVersion });
            }
            else {
                addElementOpenAttr(buf1, XML_FORM,
                    new String[] { ATTR_FORM_VERSION, ATTR_FORM_TYPE },
                    new String[] { formatVersion, compatFormInfo });
            }

            os = formFile.getOutputStream(lock); // [PENDING - first save to ByteArray for safety]
            os.write(buf1.toString().getBytes(encoding));
            os.write(buf2.toString().getBytes(encoding));
        }
        finally {
            if (expressions != null)
                expressions.clear();
            if (savedVariables != null)
                savedVariables.clear();

            if (os != null)
                os.close();
            if (lock != null)
                lock.releaseLock();
        }
    }

    private void raiseFormatVersion(String ver) {
        if (NB32_VERSION.equals(formatVersion) && CURRENT_VERSION.equals(ver))
            formatVersion = CURRENT_VERSION;
    }

    private void saveAnyComponent(RADComponent comp,
                                  StringBuffer buf,
                                  String indent,
                                  boolean createElement) {
        String elementType = null;
        String elementIndent = indent;
        if (createElement) {
            if (comp instanceof RADMenuComponent)
                elementType = XML_MENU_CONTAINER;
            else if (comp instanceof RADMenuItemComponent)
                elementType = XML_MENU_COMPONENT;
            else if (comp instanceof ComponentContainer)
                elementType = XML_CONTAINER;
            else elementType = XML_COMPONENT;

            buf.append(elementIndent);
            addElementOpenAttr(buf, elementType,
                new String[] { ATTR_COMPONENT_CLASS,
                               ATTR_COMPONENT_NAME },
                new String[] { comp.getBeanClass().getName(),
                               comp.getName() });

            indent += ONE_INDENT;
        }

        if (comp instanceof RADMenuItemComponent) {
            saveMenuComponent((RADMenuItemComponent) comp, buf, indent);
        }
        else if (comp instanceof ComponentContainer) {
            saveContainer((ComponentContainer) comp, buf, indent);
        }
        else if (comp instanceof RADVisualComponent) {
            saveVisualComponent((RADVisualComponent) comp, buf, indent);
        }
        else {
            saveComponent(comp, buf, indent);
        }

        if (createElement) {
            buf.append(elementIndent);
            addElementClose(buf, elementType);
        }
    }

    private void saveContainer(ComponentContainer container,
                               StringBuffer buf, String indent)
    {
        RADVisualContainer visualContainer =
            container instanceof RADVisualContainer ?
                (RADVisualContainer) container : null;

        RADComponent[] children = null;

        if (visualContainer != null) {
            saveVisualComponent(visualContainer, buf, indent);
            layoutConvIndex = saveLayout(visualContainer, buf, indent);

            // compatibility hack for saving form's menu bar (part II)
            if (container instanceof RADVisualFormContainer)
                children = visualContainer.getSubComponents();
        } 
        else saveComponent((RADComponent)container, buf, indent);

        if (children == null)
            children = container.getSubBeans();

        if (children.length > 0) {
            buf.append(indent);
            addElementOpen(buf, XML_SUB_COMPONENTS);
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof RADMenuItemComponent)
                    raiseFormatVersion(CURRENT_VERSION);

                saveAnyComponent(children[i], buf, indent+ONE_INDENT, true);
            }
            buf.append(indent);
            addElementClose(buf, XML_SUB_COMPONENTS);
        }

        if (visualContainer != null && layoutConvIndex < 0)
            saveLayoutCode(visualContainer.getLayoutSupport(), buf, indent);
    }

    private int saveLayout(RADVisualContainer container,
                           StringBuffer buf, String indent)
    {
        LayoutSupportManager layoutSupport = container.getLayoutSupport();
        Class layoutClass = layoutSupport.getLayoutDelegate().getSupportedClass();

        int convIndex = -1; // index in conversion table

        if (layoutClass == null)
            convIndex = LAYOUT_NULL;
        else {
            String className = layoutClass.getName();
            for (int i=0; i < supportedClassNames.length; i++)
                if (className.equals(supportedClassNames[i])) {
                    convIndex = i;
                    break;
                }

            if (convIndex < 0) // not a standard layout
                return convIndex;
        }

        StringBuffer buf2 = new StringBuffer();

        if (convIndex != LAYOUT_ABSOLUTE && convIndex != LAYOUT_NULL) {
            Node.Property[] properties = layoutSupport.getAllProperties();
            for (int i=0; i < properties.length; i++) {
                FormProperty property = (FormProperty) properties[i];
                if (property.isChanged()
                    // NB 3.1 considered special values as default for
                    // GridLayout, so we must always save rows and columns
                    || (convIndex == LAYOUT_GRID
                        && ("rows".equals(property.getName())
                            || "columns".equals(property.getName()))))
                {
                    String delegatePropName = property.getName();
                    String layout31PropName = null;
                    String[] delPropNames = layoutDelegatePropertyNames[convIndex];
                    for (int j=0; j < delPropNames.length; j++)
                        if (delegatePropName.equals(delPropNames[j])) {
                            layout31PropName = layout31PropertyNames[convIndex][j];
                            break;
                        }

                    if (layout31PropName != null) {
                        saveProperty(property, layout31PropName,
                                     buf2, indent + ONE_INDENT);
                    }
                }
            }
        }
        else { // AbsoluteLayout and null layout are special...
            String nullLayout = convIndex == LAYOUT_NULL ? "true" : "false"; // NOI18N
            buf2.append(indent);
            buf2.append(ONE_INDENT);
            addLeafElementOpenAttr(
                buf2,
                XML_PROPERTY,
                new String[] { ATTR_PROPERTY_NAME,
                               ATTR_PROPERTY_TYPE,
                               ATTR_PROPERTY_VALUE },
                new String[] { "useNullLayout", "boolean", nullLayout } // NOI18N
            );
        }

        buf.append("\n"); // NOI18N
        buf.append(indent);
        if (buf2.length() > 0) {
            addElementOpenAttr(
                buf,
                XML_LAYOUT,
                new String[] { ATTR_LAYOUT_CLASS },
                new String[] { PersistenceObjectRegistry.getPrimaryName(
                                   layout31Names[convIndex]) }
            );
            buf.append(buf2);
            buf.append(indent);
            addElementClose(buf, XML_LAYOUT);
        }
        else {
            addLeafElementOpenAttr(
                buf,
                XML_LAYOUT,
                new String[] { ATTR_LAYOUT_CLASS },
                new String[] { PersistenceObjectRegistry.getPrimaryName(
                                   layout31Names[convIndex]) }
            );
        }

        return convIndex;
    }

    private void saveLayoutCode(LayoutSupportManager layoutSupport,
                                StringBuffer buf, String indent)
    {
        raiseFormatVersion(CURRENT_VERSION);

        StringBuffer buf2 = new StringBuffer();
        String subIndent = indent + ONE_INDENT;
//        codeFlow = true;

        // layout manager code
        CodeGroup code = layoutSupport.getLayoutCode();
        if (code != null) {
            Iterator it = code.getStatementsIterator();
            while (it.hasNext()) {
                saveCodeStatement((CodeStatement) it.next(), buf2, subIndent);
            }
        }

        // components code
        for (int i=0, n=layoutSupport.getComponentCount(); i < n; i++) {
            code = layoutSupport.getComponentCode(i);
            if (code != null) {
                Iterator it = code.getStatementsIterator();
                while (it.hasNext()) {
                    saveCodeStatement((CodeStatement) it.next(), buf2, subIndent);
                }
            }
        }

        if (buf2.length() > 0) {
            buf.append(indent);
            addElementOpen(buf, XML_LAYOUT_CODE);

            buf.append(buf2.toString());

            buf.append(indent);
            addElementClose(buf, XML_LAYOUT_CODE);
        }
    }

    private void saveVisualComponent(RADVisualComponent component,
                                     StringBuffer buf, String indent)
    {
        saveComponent(component, buf, indent);

        RADVisualContainer container = component.getParentContainer();
        if (container == null)
            return;

        int componentIndex = container.getIndexOf(component);
        LayoutConstraints constr =
            container.getLayoutSupport().getConstraints(componentIndex);
        if (constr == null)
            return; // no constraints

        StringBuffer buf2 = new StringBuffer(); // [might be not used at all]
        int convIndex = saveConstraints(constr, buf2,
                                        indent + ONE_INDENT + ONE_INDENT);
        if (convIndex >= 0) { // standard constraints (saved in buf2)
            buf.append(indent);
            addElementOpen(buf, XML_CONSTRAINTS);
            buf.append(indent + ONE_INDENT);
            addElementOpenAttr(
                buf,
                XML_CONSTRAINT,
                new String[] { ATTR_CONSTRAINT_LAYOUT, ATTR_CONSTRAINT_VALUE },
                new String[] { PersistenceObjectRegistry.getPrimaryName(
                                   layout31Names[convIndex]),
                               PersistenceObjectRegistry.getPrimaryName(
                                   layout31ConstraintsNames[convIndex]) }
            );
            buf.append(buf2);
            buf.append(indent + ONE_INDENT);
            addElementClose(buf, XML_CONSTRAINT);
            buf.append(indent);
            addElementClose(buf, XML_CONSTRAINTS);
        }
    }

    private int saveConstraints(LayoutConstraints constr,
                                StringBuffer buf,
                                String indent)
    {
        // constraints of BorderLayout
        if (constr instanceof BorderLayoutSupport.BorderConstraints) {
            String position = (String) constr.getConstraintsObject();
            buf.append(indent);
            addLeafElementOpenAttr(
                buf,
                "BorderConstraints", // NOI18N
                new String[] { "direction" }, // NOI18N
                new String[] { position });

            return LAYOUT_BORDER;
        }

        // constraints of GridBagLayout
        if (constr instanceof GridBagLayoutSupport.GridBagLayoutConstraints) {
            java.awt.GridBagConstraints gbConstr =
                (java.awt.GridBagConstraints) constr.getConstraintsObject();

            buf.append(indent);
            addLeafElementOpenAttr(
                buf,
                "GridBagConstraints", // NOI18N
                new String[] { "gridX", "gridY", "gridWidth", "gridHeight", // NOI18N
                               "fill", "ipadX", "ipadY", // NOI18N
                               "insetsTop", "insetsLeft", // NOI18N
                               "insetsBottom", "insetsRight", // NOI18N
                               "anchor", "weightX", "weightY" }, // NOI18N
                new String[] { Integer.toString(gbConstr.gridx),
                               Integer.toString(gbConstr.gridy),
                               Integer.toString(gbConstr.gridwidth),
                               Integer.toString(gbConstr.gridheight),
                               Integer.toString(gbConstr.fill),
                               Integer.toString(gbConstr.ipadx),
                               Integer.toString(gbConstr.ipady),
                               Integer.toString(gbConstr.insets.top),
                               Integer.toString(gbConstr.insets.left),
                               Integer.toString(gbConstr.insets.bottom),
                               Integer.toString(gbConstr.insets.right),
                               Integer.toString(gbConstr.anchor),
                               Double.toString(gbConstr.weightx),
                               Double.toString(gbConstr.weighty) });

            return LAYOUT_GRIDBAG;
        }

        // constraints of JTabbedPane
        if (constr instanceof JTabbedPaneSupport.TabConstraints) {
            JTabbedPaneSupport.TabConstraints tabConstr =
                (JTabbedPaneSupport.TabConstraints) constr;

            StringBuffer buf2 = new StringBuffer();
            Node.Property[] tabProperties = constr.getProperties();

            for (int i=0; i < tabProperties.length; i++) {
                FormProperty prop = (FormProperty) tabProperties[i];
                if (prop.isChanged())
                    saveProperty(prop, prop.getName(),
                                 buf2, indent + ONE_INDENT);
            }

            buf.append(indent);
            if (buf2.length() > 0) {
                addElementOpenAttr(
                    buf,
                    "JTabbedPaneConstraints", // NOI18N
                    new String[] { "tabName", "toolTip" }, // NOI18N
                    new String[] { tabConstr.getTitle(),
                                   tabConstr.getToolTip() });
                buf.append(buf2);
                buf.append(indent);
                addElementClose(buf, "JTabbedPaneConstraints"); // NOI18N
            }
            else {
                addLeafElementOpenAttr(
                    buf,
                    "JTabbedPaneConstraints", // NOI18N
                    new String[] { "tabName", "toolTip" }, // NOI18N
                    new String[] { tabConstr.getTitle(),
                                   tabConstr.getToolTip() });
            }

            return LAYOUT_JTAB;
        }

        // constraints of JSplitPane
        if (constr instanceof JSplitPaneSupport.SplitConstraints) {
            Object constrObject = constr.getConstraintsObject();
            String position;

            if (javax.swing.JSplitPane.TOP.equals(constrObject))
                position = "top"; // NOI18N
            else if (javax.swing.JSplitPane.BOTTOM.equals(constrObject))
                position = "bottom"; // NOI18N
            else if (javax.swing.JSplitPane.LEFT.equals(constrObject))
                position = "left"; // NOI18N
            else
                position = "right"; // NOI18N

            buf.append(indent);
            addLeafElementOpenAttr(
                buf,
                "JSplitPaneConstraints", // NOI18N
                new String[] { "position" }, // NOI18N
                new String[] { position });

            return LAYOUT_JSPLIT;
        }

        // constraints of CardLayout
        if (constr instanceof CardLayoutSupport.CardConstraints) {
            String card = (String) constr.getConstraintsObject();
            buf.append(indent);
            addLeafElementOpenAttr(
                buf,
                "CardConstraints", // NOI18N
                new String[] { "cardName" }, // NOI18N
                new String[] { card });

            return LAYOUT_CARD;
        }

        // constraints of JLayeredPane (must be tested before AbsoluteLayout)
        if (constr instanceof JLayeredPaneSupport.LayeredConstraints) {
            int layer =
                ((JLayeredPaneSupport.LayeredConstraints)constr).getLayer();
            java.awt.Rectangle r =
                ((JLayeredPaneSupport.LayeredConstraints)constr).getBounds();

            buf.append(indent);
            addLeafElementOpenAttr(
                buf,
                "JLayeredPaneConstraints", // NOI18N
                new String[] { "x", "y", "width", "height",
                               "layer", "position" }, // NOI18N
                new String[] { Integer.toString(r.x),
                               Integer.toString(r.y),
                               Integer.toString(r.width),
                               Integer.toString(r.height),
                               Integer.toString(layer),
                               "-1" }); // NOI18N

            return LAYOUT_JLAYER;
        }

        // constraints of AbsoluteLayout
        if (constr instanceof AbsoluteLayoutSupport.AbsoluteLayoutConstraints) {
            java.awt.Rectangle r =
                ((AbsoluteLayoutSupport.AbsoluteLayoutConstraints)constr)
                    .getBounds();

            buf.append(indent);
            addLeafElementOpenAttr(
                buf,
                "AbsoluteConstraints", // NOI18N
                new String[] { "x", "y", "width", "height" }, // NOI18N
                new String[] { Integer.toString(r.x),
                               Integer.toString(r.y),
                               Integer.toString(r.width),
                               Integer.toString(r.height) });

            return LAYOUT_ABSOLUTE;
        }

        return -1;
    }

    private void saveMenuComponent(RADMenuItemComponent component, StringBuffer buf, String indent) {
        saveComponent(component, buf, indent);

        if (component instanceof RADMenuComponent) {
            RADComponent[] children =((RADMenuComponent)component).getSubBeans();
            if (children.length > 0) {
                buf.append(indent); addElementOpen(buf, XML_SUB_COMPONENTS);
                for (int i = 0; i < children.length; i++) {
                    String elementType;
                    if (children[i] instanceof RADMenuComponent) elementType = XML_MENU_CONTAINER;
                    else if (children[i] instanceof RADMenuItemComponent) elementType = XML_MENU_COMPONENT;
                    else elementType = XML_COMPONENT;

                    buf.append(indent + ONE_INDENT);
                    addElementOpenAttr(
                        buf,
                        elementType,
                        new String[] { ATTR_COMPONENT_CLASS, ATTR_COMPONENT_NAME },
                        new String[] { children[i].getBeanClass().getName(), children[i].getName() }
                        );
                    // [PENDING - RADComponents which are not menu???]
                    saveMenuComponent((RADMenuItemComponent)children[i], buf, indent + ONE_INDENT + ONE_INDENT);
                    buf.append(indent + ONE_INDENT); addElementClose(buf, elementType);
                }
                buf.append(indent); addElementClose(buf, XML_SUB_COMPONENTS);
            }
        }
    }

    private void saveComponent(RADComponent component, StringBuffer buf, String indent) {
        // 1. Properties
        if (!JavaCodeGenerator.VALUE_SERIALIZE.equals(
                component.getAuxValue(JavaCodeGenerator.AUX_CODE_GENERATION))) {
            // save properties only if the component is not to be serialized
            boolean doSaveProps = false;
            RADProperty[] props = component.getAllBeanProperties();
            for (int i = 0; i < props.length; i++) {
                if (props[i].isChanged()
                        || props[i].getPreCode() != null
                        || props[i].getPostCode() != null) {
                    doSaveProps = true;
                    break;
                }
            }

            if (doSaveProps) {
                buf.append(indent); addElementOpen(buf, XML_PROPERTIES);
                saveProperties(component, buf, indent + ONE_INDENT);
                buf.append(indent); addElementClose(buf, XML_PROPERTIES);
            }
        }

        // 1.a synthetic properties - only for RADVisualFormContainer
        if (component instanceof RADVisualFormContainer) {
            buf.append(indent); addElementOpen(buf, XML_SYNTHETIC_PROPERTIES);
            saveSyntheticProperties(component, buf, indent + ONE_INDENT);
            buf.append(indent); addElementClose(buf, XML_SYNTHETIC_PROPERTIES);
        }

        // 2. Events
        Collection events = component.getEventHandlers().getEventsInfo();
        if (events.size() > 0) {
            buf.append("\n"); // NOI18N
            buf.append(indent); addElementOpen(buf, XML_EVENTS);
            saveEvents(events, buf, indent + ONE_INDENT);
            buf.append(indent); addElementClose(buf, XML_EVENTS);
        }

        // 3. Aux Values
        Map auxValues = component.getAuxValues();
        if (auxValues != null && auxValues.size() > 0) {
            buf.append("\n"); // NOI18N
            buf.append(indent); addElementOpen(buf, XML_AUX_VALUES);
            saveAuxValues(auxValues, buf, indent + ONE_INDENT);
            buf.append(indent); addElementClose(buf, XML_AUX_VALUES);
        }
    }

    private void saveProperties(RADComponent component, StringBuffer buf, String indent) {
        RADProperty[] props = component.getAllBeanProperties();
        for (int i = 0; i < props.length; i++) {
            RADProperty prop = (RADProperty) props[i];
            if (!prop.isChanged()) {
                if (prop.getPreCode() != null || prop.getPostCode() != null) {
                    buf.append(indent);
                    // in this case save only the pre/post code
                    addLeafElementOpenAttr(
                        buf,
                        XML_PROPERTY,
                        new String[] {
                            ATTR_PROPERTY_NAME,
                            ATTR_PROPERTY_PRE_CODE,
                            ATTR_PROPERTY_POST_CODE,
                        },
                        new String[] {
                            prop.getName(),
                            prop.getPreCode(),
                            prop.getPostCode(),
                        }
                        );
                }
                continue; // not changed, so do not save value
            }

            saveProperty(prop, prop.getName(), buf, indent);
        }
    }

    private boolean saveProperty(FormProperty property,
                                 String propertyName,
                                 StringBuffer buf,
                                 String indent)
    {
        Object value;
        try {
            value = property.getValue();
        }
        catch (Exception ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace(); // problem getting value => ignore
            return false;
        }

        String encodedValue = null;
        String encodedSerializeValue = null;
        org.w3c.dom.Node valueNode = null;

        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd instanceof XMLPropertyEditor) {
            prEd.setValue(value);
            valueNode = ((XMLPropertyEditor)prEd).storeToXML(topDocument);
            if (valueNode == null)
                return false; // property editor refused to save the value
        }
        else {
            encodedValue = encodePrimitiveValue(value);
            if (encodedValue == null) {
                encodedSerializeValue = encodeValue(value);
                if (encodedSerializeValue == null) {
                    System.err.println("[WARNING] Cannot save property to XML: " // NOI18N
                                       + propertyName + ", " // NOI18N
                                       + property.getValueType().getName());
                    return false;
                }
            }
        }

        buf.append(indent);

        if (encodedValue != null) {
            addLeafElementOpenAttr(
                buf,
                XML_PROPERTY,
                new String[] {
                    ATTR_PROPERTY_NAME,
                    ATTR_PROPERTY_TYPE,
                    ATTR_PROPERTY_VALUE,
                    ATTR_PROPERTY_PRE_CODE,
                    ATTR_PROPERTY_POST_CODE },
                new String[] {
                    propertyName,
                    property.getValueType().getName(),
                    encodedValue,
                    property.getPreCode(),
                    property.getPostCode() });
        }
        else {
            addElementOpenAttr(
                buf,
                XML_PROPERTY,
                new String[] {
                    ATTR_PROPERTY_NAME,
                    ATTR_PROPERTY_TYPE,
                    ATTR_PROPERTY_EDITOR,
                    ATTR_PROPERTY_PRE_CODE,
                    ATTR_PROPERTY_POST_CODE },
                new String[] {
                    propertyName,
                    property.getValueType().getName(),
                    prEd.getClass().getName(),
                    property.getPreCode(),
                    property.getPostCode() });

            if (valueNode != null) {
                saveNodeIntoText(buf, valueNode, indent + ONE_INDENT);
            }
            else {
                buf.append(indent + ONE_INDENT);
                addLeafElementOpenAttr(
                    buf,
                    XML_SERIALIZED_PROPERTY_VALUE,
                    new String[] { ATTR_PROPERTY_VALUE },
                    new String[] { encodedSerializeValue });
            }
            buf.append(indent);
            addElementClose(buf, XML_PROPERTY);
        }
        return true;
    }

    private boolean saveValue(Object value,
                              Class valueType,
                              PropertyEditor prEd,
                              StringBuffer buf,
                              String indent)
    {
        String encodedValue = null;
        String encodedSerializeValue = null;
        org.w3c.dom.Node valueNode = null;

        if (prEd instanceof XMLPropertyEditor) {
            prEd.setValue(value);
            valueNode = ((XMLPropertyEditor)prEd).storeToXML(topDocument);
            if (valueNode == null)
                return false; // property editor refused to save the value
        }
        else {
            encodedValue = encodePrimitiveValue(value);
            if (encodedValue == null) {
                encodedSerializeValue = encodeValue(value);
                if (encodedSerializeValue == null) {
                    System.err.println("[WARNING] Cannot save value to XML: " // NOI18N
                                       + value);
                    return false;
                }
            }
        }

        buf.append(indent);

        if (encodedValue != null) {
            addLeafElementOpenAttr(
                buf,
                XML_VALUE,
                new String[] { ATTR_PROPERTY_TYPE, ATTR_PROPERTY_VALUE },
                new String[] { valueType.getName(), encodedValue });
        }
        else {
            addElementOpenAttr(
                buf,
                XML_VALUE,
                new String[] { ATTR_PROPERTY_TYPE, ATTR_PROPERTY_EDITOR },
                new String[] { valueType.getName(), prEd.getClass().getName() });

            if (valueNode != null) {
                saveNodeIntoText(buf, valueNode, indent + ONE_INDENT);
            }
            else {
                buf.append(indent + ONE_INDENT);
                addLeafElementOpenAttr(
                    buf,
                    XML_SERIALIZED_PROPERTY_VALUE,
                    new String[] { ATTR_PROPERTY_VALUE },
                    new String[] { encodedSerializeValue });
            }
            buf.append(indent);
            addElementClose(buf, XML_VALUE);
        }
        return true;
    }

    private void saveSyntheticProperties(RADComponent component, StringBuffer buf, String indent) {
        // compatibility hack for saving form's menu bar (part III)
        if (component instanceof RADVisualFormContainer) {
            RADMenuComponent menuComp =
                ((RADVisualFormContainer)component).getContainerMenu();
            if (menuComp != null) {
                buf.append(indent);
                addLeafElementOpenAttr(buf,
                    XML_SYNTHETIC_PROPERTY,
                    new String[] { ATTR_PROPERTY_NAME,
                                   ATTR_PROPERTY_TYPE,
                                   ATTR_PROPERTY_VALUE },
                    new String[] { "menuBar",
                                   "java.lang.String",
                                   menuComp.getName() });
            }
        }

        Node.Property[] props = component.getSyntheticProperties();
        for (int i = 0; i < props.length; i++) {
            Node.Property prop = props[i];

            Object value = null;
            try {
                value = prop.getValue();
            } catch (Exception e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) e.printStackTrace(); // NOI18N
                // problem getting value => ignore this property
                continue;
            }
            String valueType = prop.getValueType().getName();
            String encodedValue = encodePrimitiveValue(value);
            if (encodedValue == null) {
                encodedValue = encodeValue(value);
            }
            if (encodedValue == null) {
                // [PENDING - notify problem?]
                continue;
            }
            //System.out.println("saving name="+prop.getName()+", value="+value); // NOI18N
            buf.append(indent);

            addLeafElementOpenAttr(
                buf,
                XML_SYNTHETIC_PROPERTY,
                new String[] {
                    ATTR_PROPERTY_NAME,
                    ATTR_PROPERTY_TYPE,
                    ATTR_PROPERTY_VALUE,
                },
                new String[] {
                    prop.getName(),
                    valueType,
                    encodedValue,
                }
                );
        }
    }

    private void saveEvents(Collection events, StringBuffer buf, String indent) {
        Iterator it=events.iterator();
        while (it.hasNext()) {
            Event.EventInfo eventInfo = (Event.EventInfo)it.next();

            buf.append(indent);
            addLeafElementOpenAttr(
                buf,
                XML_EVENT,
                new String[] {
                    ATTR_EVENT_NAME,
                    ATTR_EVENT_LISTENER,
                    ATTR_EVENT_PARAMS,
                    ATTR_EVENT_HANDLER
                },
                new String[] {
                    eventInfo.eventName,
                    eventInfo.eventListener,
                    eventInfo.paramTypes,
                    eventInfo.eventHandlers
                }
            );
        }
    }

    private void saveAuxValues(Map auxValues, StringBuffer buf, String indent) {
        for (Iterator it = auxValues.keySet().iterator(); it.hasNext();) {
            String valueName =(String) it.next();
            Object value = auxValues.get(valueName);
            if (value == null) continue; // such values are not saved
            String valueType = value.getClass().getName();
            String encodedValue = encodePrimitiveValue(value);
            if (encodedValue == null) {
                encodedValue = encodeValue(value);
            }
            if (encodedValue == null) {
                // [PENDING - solve problem?]
                continue;
            }
            buf.append(indent);
            addLeafElementOpenAttr(
                buf,
                XML_AUX_VALUE,
                new String[] {
                    ATTR_AUX_NAME,
                    ATTR_AUX_VALUE_TYPE,
                    ATTR_AUX_VALUE },
                new String[] {
                    valueName,
                    valueType,
                    encodedValue
                }
                );
        }
    }

    private static PropertyEditor createPropertyEditor(Class editorClass,
                                                       Class propertyType,
                                                       FormProperty property)
        throws InstantiationException,
               IllegalAccessException
    {
        PropertyEditor ed =
            editorClass.equals(RADConnectionPropertyEditor.class) ?
                new RADConnectionPropertyEditor(propertyType) :
                (PropertyEditor) editorClass.newInstance();

        if (property != null)
            property.getPropertyContext().initPropertyEditor(ed);

        return ed;
    }

    // -------
    // The following code ensures persistence of code structure in XML. The
    // code is quite general except special hacks for meta components which
    // must be handled specially (as references) - as we don't save full code
    // yet but only its parts; components are saved separately. [This feature
    // is used only for saving/loading code of non-standard layout supports.]
    //
    // There are two possible ways how to save the code structure - to save
    // the code flow or the static structure.
    //
    // In the first case (code flow), a sequence of code statements is saved
    // (together with epxressions used by the statements). In the second case
    // (static structure), root code expressions are saved as trees including
    // all used expressions and all defined statements. Which style is used
    // is controlled by the codeFlow variable. [We use only code flow now.]

    // XML persistence of code structure - saving

    private void saveCodeExpression(CodeExpression exp,
                                    StringBuffer buf, String indent)
    {
        buf.append(indent);

        Object value = getExpressionsMap().get(exp);
        if (value != null) { // save expression reference only
            addLeafElementOpenAttr(buf,
                                   XML_CODE_EXPRESSION,
                                   new String[] { ATTR_EXPRESSION_ID },
                                   new String[] { value.toString() });
        }
        else { // save complete expression
            // create expression ID
            lastExpId++;
            String expId = Integer.toString(lastExpId);
            CodeVariable var = exp.getVariable();
            if (var != null)
                expId += "_" + var.getName(); // NOI18N
            getExpressionsMap().put(exp, expId);

            addElementOpenAttr(buf,
                               XML_CODE_EXPRESSION,
                               new String[] { ATTR_EXPRESSION_ID },
                               new String[] { expId.toString() });

            String subIndent = indent + ONE_INDENT;

            if (var != null)
                saveCodeVariable(var, buf, subIndent);

            saveExpressionOrigin(exp.getOrigin(), buf, subIndent);

            if (!codeFlow) {
                // if static code structure is being saved, statements are
                // saved inside their parent expressions
                Iterator it = CodeStructure.getDefinedStatementsIterator(exp);
                if (it.hasNext()) {
                    buf.append(subIndent);
                    addElementOpen(buf, XML_CODE_STATEMENTS);

                    String subSubIndent = subIndent + ONE_INDENT;
                    do {
                        saveCodeStatement((CodeStatement) it.next(),
                                          buf, subSubIndent);
                    }
                    while (it.hasNext());

                    buf.append(subIndent);
                    addElementClose(buf, XML_CODE_STATEMENTS);
                }
            }

            buf.append(indent);
            addElementClose(buf, XML_CODE_EXPRESSION);
        }
    }

    private void saveCodeVariable(CodeVariable var,
                                  StringBuffer buf, String indent)
    {
        buf.append(indent);
        if (getVariablesMap().get(var) != null) {
            addLeafElementOpenAttr(buf,
                                   XML_CODE_VARIABLE,
                                   new String[] { ATTR_VAR_NAME },
                                   new String[] { var.getName() });
        }
        else {
            addLeafElementOpenAttr(
                buf,
                XML_CODE_VARIABLE,
                new String[] { ATTR_VAR_NAME,
                               ATTR_VAR_TYPE,
                               ATTR_VAR_DECLARED_TYPE },
                new String[] { var.getName(),
                               Integer.toString(var.getType()),
                               var.getDeclaredType().getName() });

            getVariablesMap().put(var, var);
        }
    }

    private void saveExpressionOrigin(CodeExpressionOrigin origin,
                                      StringBuffer buf, String indent)
    {
        buf.append(indent);
        addElementOpen(buf, XML_CODE_ORIGIN);

        String subIndent = indent + ONE_INDENT;

        CodeExpression parentExp = origin.getParentExpression();
        if (parentExp != null)
            saveCodeExpression(parentExp, buf, subIndent);

        Object metaObject = origin.getMetaObject();
        if (metaObject != null)
            saveOriginMetaObject(metaObject, buf, subIndent);
        else
            saveValue(origin.getValue(), origin.getType(), null,
                      buf, subIndent);

        saveParameters(origin.getCreationParameters(), buf, subIndent);

        buf.append(indent);
        addElementClose(buf, XML_CODE_ORIGIN);
    }

    private void saveCodeStatement(CodeStatement statement,
                                   StringBuffer buf, String indent)
    {
        buf.append(indent);
        addElementOpen(buf, XML_CODE_STATEMENT);

        String subIndent = indent + ONE_INDENT;

        if (codeFlow) {
            // if code flow is being saved, also the parent expression of
            // the statement must be saved for it
            CodeExpression parentExp = statement.getParentExpression();
            if (parentExp != null)
                saveCodeExpression(parentExp, buf, subIndent);
        }

        Object metaObject = statement.getMetaObject();
        if (metaObject != null)
            saveStatementMetaObject(metaObject, buf, subIndent);

        saveParameters(statement.getStatementParameters(), buf, subIndent);

        buf.append(indent);
        addElementClose(buf, XML_CODE_STATEMENT);
    }

    private void saveOriginMetaObject(Object metaObject,
                                      StringBuffer buf, String indent)
    {
        if (metaObject instanceof Node.Property) {
            Node.Property property = (Node.Property) metaObject;
            Object value;
            try {
                value = property.getValue();
            }
            catch (Exception ex) { // should not happen
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    ex.printStackTrace();
                return;
            }

            PropertyEditor prEd = property instanceof FormProperty ?
                                  ((FormProperty)property).getCurrentEditor() :
                                  property.getPropertyEditor();
            saveValue(value, property.getValueType(), prEd, buf,indent);
            return;
        }

        StringBuffer buf2 = new StringBuffer();
        String subIndent = indent + ONE_INDENT;
        String originType = null;

        if (metaObject instanceof Constructor) {
            Constructor ctor = (Constructor) metaObject;
            StringBuffer buf3 = new StringBuffer();
            Class[] paramTypes = ctor.getParameterTypes();

            for (int i=0; i < paramTypes.length; i++) {
                buf3.append(paramTypes[i].getName());
                if (i+1 < paramTypes.length)
                    buf3.append(", "); // NOI18N
            }

            buf2.append(subIndent);
            addLeafElementOpenAttr(
                buf2,
                XML_CODE_CONSTRUCTOR,
                new String[] { ATTR_MEMBER_CLASS,
                               ATTR_MEMBER_PARAMS },
                new String[] { ctor.getDeclaringClass().getName(),
                               buf3.toString() });

            originType = XML_CODE_CONSTRUCTOR;
        }

        // special code for handling meta component references
        else if (metaObject instanceof RADComponent) {
            RADComponent metacomp = (RADComponent) metaObject;

            buf2.append(subIndent);
            addLeafElementOpenAttr(
                buf2,
                XML_COMPONENT_REF,
                new String[] { ATTR_COMPONENT_NAME },
                new String[] { metacomp != formModel.getTopRADComponent() ?
                               metacomp.getName() : "." }); // NOI18N

            originType = XML_COMPONENT_REF;
        }

        else if (metaObject instanceof Method) {
            saveMethod((Method) metaObject, buf2, subIndent);
            originType = XML_CODE_METHOD;
        }

        else if (metaObject instanceof Field) {
            saveField((Field) metaObject, buf2, subIndent);
            originType = XML_CODE_FIELD;
        }

        if (originType == null)
            return; // unknown origin

        buf.append(indent);
        addElementOpenAttr(buf,
                           XML_ORIGIN_META_OBJECT,
                           new String[] { ATTR_META_OBJECT_TYPE },
                           new String[] { originType } );
        buf.append(buf2);
        buf.append(indent);
        addElementClose(buf, XML_ORIGIN_META_OBJECT);
    }

    private void saveStatementMetaObject(Object metaObject,
                                         StringBuffer buf, String indent)
    {
        StringBuffer buf2 = new StringBuffer();
        String subIndent = indent + ONE_INDENT;
        String statementType = null;

        if (metaObject instanceof Method) {
            saveMethod((Method) metaObject, buf2, subIndent);
            statementType = XML_CODE_METHOD;
        }
        else if (metaObject instanceof Field) {
            saveField((Field) metaObject, buf2, subIndent);
            statementType = XML_CODE_FIELD;
        }
        else if (metaObject instanceof CodeExpression) { // variable assignment
            CodeExpression exp = (CodeExpression) metaObject;
            if (exp.getVariable() != null) {
                saveCodeExpression(exp, buf2, subIndent);
                statementType = XML_CODE_EXPRESSION;
            }
        }
        // [... variable declaration statement]

        if (statementType == null)
            return; // unknown statement

        buf.append(indent);
        addElementOpenAttr(buf,
                           XML_STATEMENT_META_OBJECT,
                           new String[] { ATTR_META_OBJECT_TYPE },
                           new String[] { statementType } );
        buf.append(buf2);
        buf.append(indent);
        addElementClose(buf, XML_STATEMENT_META_OBJECT);
    }

    private void saveParameters(CodeExpression[] parameters,
                                StringBuffer buf, String indent)
    {
        if (parameters.length > 0) {
            buf.append(indent);
            addElementOpen(buf, XML_CODE_PARAMETERS);

            String subIndent = indent + ONE_INDENT;
            for (int i=0; i < parameters.length; i++)
                saveCodeExpression(parameters[i], buf, subIndent);

            buf.append(indent);
            addElementClose(buf, XML_CODE_PARAMETERS);
        }
    }

    private static void saveMethod(Method method,
                                   StringBuffer buf, String indent)
    {
        StringBuffer buf2 = new StringBuffer();
        Class[] paramTypes = method.getParameterTypes();

        for (int i=0; i < paramTypes.length; i++) {
            buf2.append(paramTypes[i].getName());
            if (i+1 < paramTypes.length)
                buf2.append(", "); // NOI18N
        }

        buf.append(indent);
        addLeafElementOpenAttr(
            buf,
            XML_CODE_METHOD,
            new String[] { ATTR_MEMBER_NAME,
                           ATTR_MEMBER_CLASS,
                           ATTR_MEMBER_PARAMS },
            new String[] { method.getName(),
                           method.getDeclaringClass().getName(),
                           buf2.toString() });
    }

    private static void saveField(Field field, StringBuffer buf, String indent)
    {
        buf.append(indent);
        addLeafElementOpenAttr(
            buf,
            XML_CODE_FIELD,
            new String[] { ATTR_MEMBER_NAME,
                           ATTR_MEMBER_CLASS },
            new String[] { field.getName(),
                           field.getDeclaringClass().getName() });
    }

    // -------
    // XML persistence of code structure - loading

    private CodeExpression loadCodeExpression(org.w3c.dom.Node node) {
        String expId = findAttribute(node, ATTR_EXPRESSION_ID);
        if (expId == null)
            return null; // missing ID error

        CodeExpression exp = (CodeExpression) getExpressionsMap().get(expId);
        if (exp != null)
            return exp;

        org.w3c.dom.NodeList childNodes = node.getChildNodes();
        if (childNodes == null)
            return null; // missing subnodes (expression content) error

        org.w3c.dom.Node variableNode = null;
        org.w3c.dom.Node originNode = null;
        org.w3c.dom.Node statementsNode = null;

        for (int i=0, n=childNodes.getLength(); i < n; i++) {
            org.w3c.dom.Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
                continue; // ignore text nodes

            String nodeName = childNode.getNodeName();

            if (XML_CODE_VARIABLE.equals(nodeName))
                variableNode = childNode;
            else if (XML_CODE_ORIGIN.equals(nodeName))
                originNode = childNode;
            else if (!codeFlow && XML_CODE_STATEMENTS.equals(nodeName))
                statementsNode = childNode;
        }

        if (originNode == null)
            return null; // missing origin error

        CodeExpressionOrigin origin = loadExpressionOrigin(originNode);
        if (origin == null)
            return null; // origin loading error

        // special code for handling meta component references
        Object originMetaObject = origin.getMetaObject();
        if (originMetaObject instanceof RADComponent) {
            // use the expression from meta component
            exp = ((RADComponent)originMetaObject).getCodeExpression();
        }
        else { // create a new expression normally
            exp = getCodeStructure().createExpression(origin);

            CodeVariable var = variableNode != null ?
                               loadCodeVariable(variableNode) : null;
            if (var != null)
                getCodeStructure().attachExpressionToVariable(exp, var);
        }

        getExpressionsMap().put(expId, exp);

        if (statementsNode != null) {
            childNodes = statementsNode.getChildNodes();
            if (childNodes != null) {
                for (int i=0, n=childNodes.getLength(); i < n; i++) {
                    org.w3c.dom.Node childNode = childNodes.item(i);

                    if (XML_CODE_STATEMENT.equals(childNode.getNodeName()))
                        loadCodeStatement(childNode, exp);
                }
            }
        }

        return exp;
    }

    private CodeVariable loadCodeVariable(org.w3c.dom.Node node) {
        org.w3c.dom.NamedNodeMap attr = node.getAttributes();
        if (attr == null)
            return null; // no attributes error

        node = attr.getNamedItem(ATTR_VAR_NAME);
        if (node == null)
            return null; // missing variable name error
        String name = node.getNodeValue();

        CodeVariable var = getCodeStructure().getVariable(name);
        if (var != null)
            return var;

        node = attr.getNamedItem(ATTR_VAR_TYPE);
        if (node == null)
            return null; // missing variable type error
        int type = Integer.parseInt(node.getNodeValue());

        node = attr.getNamedItem(ATTR_VAR_DECLARED_TYPE);
        if (node == null)
            return null; // missing variable declared type error
        Class declaredType = null;

        try {
            declaredType = getClassFromString(node.getNodeValue());
        }
        catch (ClassNotFoundException ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
        }
        if (declaredType == null)
            return null; // variable declared type loading error

        return getCodeStructure().createVariable(type, declaredType, name);
    }

    private CodeExpressionOrigin loadExpressionOrigin(org.w3c.dom.Node node) {
        org.w3c.dom.NodeList childNodes = node.getChildNodes();
        if (childNodes == null)
            return null; // missing subnodes (origin content) error

        org.w3c.dom.Node parentExpNode = null;
        org.w3c.dom.Node metaObjectNode = null;
        org.w3c.dom.Node valueNode = null;
        org.w3c.dom.Node parametersNode = null;

        for (int i=0, n=childNodes.getLength(); i < n; i++) {
            org.w3c.dom.Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
                continue; // ignore text nodes

            String nodeName = childNode.getNodeName();

            if (XML_CODE_EXPRESSION.equals(nodeName))
                parentExpNode = childNode;
            else if (XML_ORIGIN_META_OBJECT.equals(nodeName))
                metaObjectNode = childNode;
            else if (XML_VALUE.equals(nodeName))
                valueNode = childNode;
            else if (XML_CODE_PARAMETERS.equals(nodeName))
                parametersNode = childNode;
        }

        if (metaObjectNode == null && valueNode == null)
            return null; // missing origin metaobject or value error

        CodeExpression parentExp;
        if (parentExpNode != null) {
            parentExp = loadCodeExpression(parentExpNode);
            if (parentExp == null)
                return null; // parent expression loading error
        }
        else parentExp = null; // origin without parent expression

        CodeExpression[] parameters = parametersNode != null ?
                                        loadParameters(parametersNode) :
                                        CodeStructure.EMPTY_PARAMS;
        if (parameters == null)
            return null; // error loading parameters

        CodeExpressionOrigin origin = null;

        if (metaObjectNode != null) {
            String metaObjectType = findAttribute(metaObjectNode,
                                                  ATTR_META_OBJECT_TYPE);
            childNodes = metaObjectNode.getChildNodes();
            if (metaObjectType != null && childNodes != null) {
                for (int i=0, n=childNodes.getLength(); i < n; i++) {
                    org.w3c.dom.Node childNode = childNodes.item(i);

                    String nodeName = childNode.getNodeName();
                    if (!metaObjectType.equals(nodeName))
                        continue;

                    if (XML_VALUE.equals(nodeName)) {
                        valueNode = childNode;
                        break;
                    }

                    if (XML_CODE_CONSTRUCTOR.equals(nodeName)) {
                        org.w3c.dom.NamedNodeMap attr = childNode.getAttributes();
                        if (attr == null)
                            return null; // no attributes error

                        node = attr.getNamedItem(ATTR_MEMBER_CLASS);
                        if (node == null)
                            return null; // missing constructor class error

                        Class ctorClass;
                        try {
                            ctorClass = getClassFromString(node.getNodeValue());
                        }
                        catch (ClassNotFoundException ex) {
                            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                                ex.printStackTrace();
                            return null; // constructor class loading error
                        }

                        node = attr.getNamedItem(ATTR_MEMBER_PARAMS);
                        if (node == null)
                            return null; // missing constructor parameter types error

                        Class[] paramTypes;
                        StringTokenizer paramTokens =
                            new StringTokenizer(node.getNodeValue(), ", "); // NOI18N
                        List typeList = new ArrayList();
                        try {
                            while (paramTokens.hasMoreTokens()) {
                                typeList.add(getClassFromString(
                                                 paramTokens.nextToken()));
                            }
                            paramTypes = new Class[typeList.size()];
                            typeList.toArray(paramTypes);
                        }
                        catch (ClassNotFoundException ex) {
                            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                                ex.printStackTrace();
                            return null; // parameters classes loading error
                        }

                        Constructor ctor;
                        try {
                            ctor = ctorClass.getConstructor(paramTypes);
                        }
                        catch (NoSuchMethodException ex) {
                            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                                ex.printStackTrace();
                            return null; // constructor not found error
                        }

                        origin = CodeStructure.createOrigin(ctor, parameters);
                        break;
                    }

                    // special code for handling meta component references
                    if (XML_COMPONENT_REF.equals(nodeName)) {
                        String name = findAttribute(childNode,
                                                    ATTR_COMPONENT_NAME);
                        if (name == null)
                            return null; // missing component name error

                        RADComponent comp = name.equals(".") ?
                                formModel.getTopRADComponent() :
                                (RADComponent) getComponentsMap().get(name);
                        if (comp == null)
                            return null; // no such component error

                        origin = comp.getCodeExpression().getOrigin();
                        break;
                    }

                    if (XML_CODE_METHOD.equals(nodeName)) {
                        Method m = loadMethod(childNode);
                        if (m == null)
                            return null; // method loading error

                        origin = CodeStructure.createOrigin(
                                                   parentExp, m, parameters);
                        break;
                    }

                    if (XML_CODE_FIELD.equals(nodeName)) {
                        Field f = loadField(childNode);
                        if (f == null)
                            return null; // field loading error

                        origin = CodeStructure.createOrigin(parentExp, f);
                        break;
                    }
                }
            }
        }

        if (origin == null) {
            if (valueNode == null)
                return null; // origin metaobject loading error

            String typeStr = findAttribute(valueNode, ATTR_PROPERTY_TYPE);
            if (typeStr == null)
                return null; // missing value type error

            try {
                Class valueType = getClassFromString(typeStr);
                Object value = getEncodedPropertyValue(valueNode);

                origin = CodeStructure.createOrigin(
                             valueType,
                             value,
                             value != null ? value.toString() : "null"); // NOI18N
            }
            catch (Exception ex) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    ex.printStackTrace();
                return null; // value loading error
            }
        }

        return origin;
    }

    private CodeStatement loadCodeStatement(org.w3c.dom.Node node,
                                            CodeExpression parentExp)
    {
        org.w3c.dom.NodeList childNodes = node.getChildNodes();
        if (childNodes == null)
            return null; // missing subnodes (statement content) error

        org.w3c.dom.Node parentExpNode = null;
        org.w3c.dom.Node metaObjectNode = null;
        org.w3c.dom.Node parametersNode = null;

        for (int i=0, n=childNodes.getLength(); i < n; i++) {
            org.w3c.dom.Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
                continue; // ignore text nodes

            String nodeName = childNode.getNodeName();

            if (XML_CODE_EXPRESSION.equals(nodeName)) {
                if (parentExp == null)
                    parentExpNode = childNode;
            }
            else if (XML_STATEMENT_META_OBJECT.equals(nodeName))
                metaObjectNode = childNode;
            else if (XML_CODE_PARAMETERS.equals(nodeName))
                parametersNode = childNode;
        }

        if (metaObjectNode == null)
            return null; // missing statement metaobject error

        if (parentExpNode != null) {
            parentExp = loadCodeExpression(parentExpNode);
            if (parentExp == null)
                return null; // parent expression loading error
        }

        CodeExpression[] parameters = parametersNode != null ?
                                        loadParameters(parametersNode) :
                                        CodeStructure.EMPTY_PARAMS;
        if (parameters == null)
            return null; // error loading parameters

        CodeStatement statement = null;

        String metaObjectType = findAttribute(metaObjectNode,
                                              ATTR_META_OBJECT_TYPE);
        childNodes = metaObjectNode.getChildNodes();
        if (metaObjectType != null && childNodes != null) {
            for (int i=0, n=childNodes.getLength(); i < n; i++) {
                org.w3c.dom.Node childNode = childNodes.item(i);

                String nodeName = childNode.getNodeName();
                if (!metaObjectType.equals(nodeName))
                    continue;

                if (XML_CODE_METHOD.equals(nodeName)) {
                    Method m = loadMethod(childNode);
                    if (m == null)
                        return null; // method loading error

                    statement = CodeStructure.createStatement(
                                                parentExp, m, parameters);
                    break;
                }

                if (XML_CODE_FIELD.equals(nodeName)) {
                    Field f = loadField(childNode);
                    if (f == null)
                        return null; // field loading error

                    if (parameters.length != 1)
                        return null; // inconsistent data error

                    statement = CodeStructure.createStatement(
                                                  parentExp, f, parameters[0]);
                    break;
                }

                if (XML_CODE_EXPRESSION.equals(nodeName)) {
                    // variable assignment
                    CodeExpression exp = loadCodeExpression(childNode);
                    if (exp != parentExp)
                        return null; // inconsistent data error

                    CodeVariable var = exp.getVariable();
                    if (var == null)
                        return null; // non-existing variable error

                    statement = var.getAssignment(exp);
                    break;
                }
            }
        }

        return statement;
    }

    private CodeExpression[] loadParameters(org.w3c.dom.Node node) {
        List paramList = new ArrayList();
        org.w3c.dom.NodeList childNodes = node.getChildNodes();
        if (childNodes != null) {
            for (int i=0, n=childNodes.getLength(); i < n; i++) {
                org.w3c.dom.Node childNode = childNodes.item(i);

                if (XML_CODE_EXPRESSION.equals(childNode.getNodeName())) {
                    CodeExpression exp = loadCodeExpression(childNode);
                    if (exp == null)
                        return null; // parameter loading error

                    paramList.add(exp);
                }
            }

            CodeExpression[] params = new CodeExpression[paramList.size()];
            paramList.toArray(params);
            return params;
        }
        else return CodeStructure.EMPTY_PARAMS;
    }

    private static Method loadMethod(org.w3c.dom.Node node) {
        org.w3c.dom.NamedNodeMap attr = node.getAttributes();
        if (attr == null)
            return null; // no attributes error

        node = attr.getNamedItem(ATTR_MEMBER_NAME);
        if (node == null)
            return null; // missing method name error
        String name = node.getNodeValue();

        node = attr.getNamedItem(ATTR_MEMBER_CLASS);
        if (node == null)
            return null; // missing method class error

        Class methodClass;
        try {
            methodClass = getClassFromString(node.getNodeValue());
        }
        catch (ClassNotFoundException ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
            return null; // method class loading error
        }

        node = attr.getNamedItem(ATTR_MEMBER_PARAMS);
        if (node == null)
            return null; // missing method parameter types error

        Class[] paramTypes;
        StringTokenizer paramTokens =
            new StringTokenizer(node.getNodeValue(), ", "); // NOI18N
        List typeList = new ArrayList();
        try {
            while (paramTokens.hasMoreTokens()) {
                typeList.add(getClassFromString(
                                 paramTokens.nextToken()));
            }
            paramTypes = new Class[typeList.size()];
            typeList.toArray(paramTypes);
        }
        catch (ClassNotFoundException ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
            return null; // parameters classes loading error
        }

        try {
            return methodClass.getMethod(name, paramTypes);
        }
        catch (NoSuchMethodException ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
            return null; // method not found error
        }
    }

    private static Field loadField(org.w3c.dom.Node node) {
        org.w3c.dom.NamedNodeMap attr = node.getAttributes();
        if (attr == null)
            return null; // no attributes error

        node = attr.getNamedItem(ATTR_MEMBER_NAME);
        if (node == null)
            return null; // missing field name error
        String name = node.getNodeValue();

        node = attr.getNamedItem(ATTR_MEMBER_CLASS);
        if (node == null)
            return null; // missing field class error

        Class fieldClass;
        try {
            fieldClass = getClassFromString(node.getNodeValue());
        }
        catch (ClassNotFoundException ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
            return null; // field class loading error
        }

        try {
            return fieldClass.getField(name);
        }
        catch (NoSuchFieldException ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
            return null; // field not found error
        }
    }

    // -------

    private CodeStructure getCodeStructure() {
        return formModel.getCodeStructure();
    }

    // -------

    private Map getExpressionsMap() {
        if (expressions == null)
            expressions = new HashMap(100);
        return expressions;
    }

    private Map getVariablesMap() {
        if (savedVariables == null)
            savedVariables = new HashMap(50);
        return savedVariables;
    }

    private Map getComponentsMap() {
        if (loadedComponents == null)
            loadedComponents = new HashMap(50);
        return loadedComponents;
    }

    // --------------------------------------------------------------------------------------
    // Value encoding methods

    /** Obtains value from given propertyNode for specified RADComponent.
     * @param propertyNode XML node where the property is stored
     * @param radComponent the RADComponent of which the property is to be loaded
     * @return the property value decoded from the node
     */
    private Object getEncodedPropertyValue(org.w3c.dom.Node propertyNode,
                                           RADComponent radComponent)
        throws IOException,
               ClassNotFoundException,
               IllegalAccessException,
               InstantiationException
    {
        org.w3c.dom.NamedNodeMap attrs = propertyNode.getAttributes();
        if (attrs == null)
            throw new IOException(); // [PENDING - explanation of problem]

        org.w3c.dom.Node nameNode = attrs.getNamedItem(ATTR_PROPERTY_NAME);
        org.w3c.dom.Node typeNode = attrs.getNamedItem(ATTR_PROPERTY_TYPE);
        org.w3c.dom.Node editorNode = attrs.getNamedItem(ATTR_PROPERTY_EDITOR);
        org.w3c.dom.Node valueNode = attrs.getNamedItem(ATTR_PROPERTY_VALUE);
        org.w3c.dom.Node preCodeNode = attrs.getNamedItem(ATTR_PROPERTY_PRE_CODE);
        org.w3c.dom.Node postCodeNode = attrs.getNamedItem(ATTR_PROPERTY_POST_CODE);

        if (nameNode == null) {
            throw new IOException(); // [PENDING - explanation of problem]
        }

        RADProperty prop = radComponent != null ?
               radComponent.getPropertyByName(nameNode.getNodeValue()) : null;

        if (typeNode == null) {
            if (prop != null) {
                if (preCodeNode != null)
                    prop.setPreCode(preCodeNode.getNodeValue());
                if (postCodeNode != null)
                    prop.setPostCode(postCodeNode.getNodeValue());
            }
            return NO_VALUE; // value is not stored for this property
        }

        Class propertyType = getClassFromString(typeNode.getNodeValue());
        PropertyEditor ed = null;
        if (editorNode != null) {
            Class editorClass =
                PersistenceObjectRegistry.loadClass(editorNode.getNodeValue());
            if (prop != null) {
                ed = createPropertyEditor(editorClass, propertyType, prop);
            } else {
//                if (Boolean.getBoolean("netbeans.debug.form")) { // NOI18N
                    System.err.println("[WARNING] Property: "+nameNode.getNodeValue()+", of component: "+radComponent.getName()+"["+radComponent.getBeanClass().getName()+"] not found."); // NOI18N
//                } // [PENDING better notification, localize]
            }
        }
        Object value = null;

        if (prop != null) {
            if (preCodeNode != null) {
                prop.setPreCode(preCodeNode.getNodeValue());
            }
            if (postCodeNode != null) {
                prop.setPostCode(postCodeNode.getNodeValue());
            }
        }

        if (valueNode != null) {
            try {
                value = decodePrimitiveValue(valueNode.getNodeValue(), propertyType);
                if (ed != null) {
                    ed.setValue(value);
                    value = ed.getValue();
                }
            } catch (IllegalArgumentException e) {
                value = null; // should not happen
            }
        } else {
            if ((ed != null) &&(ed instanceof XMLPropertyEditor)) {
                org.w3c.dom.NodeList propChildren = propertyNode.getChildNodes();
                if ((propChildren != null) &&(propChildren.getLength() > 0)) {
                    // for forward compatibility - to be able to read props that support XML now
                    // but were saved in past when class did not support XML
                    boolean isXMLSerialized = false;
                    for (int i = 0; i < propChildren.getLength(); i++) {
                        if (XML_SERIALIZED_PROPERTY_VALUE.equals(propChildren.item(i).getNodeName())) {
                            isXMLSerialized = true;
                            String serValue = findAttribute(propChildren.item(i), ATTR_PROPERTY_VALUE);
                            if (serValue != null) {
                                value = decodeValue(serValue);
                            }
                            break;
                        }
                    }
                    if (!isXMLSerialized) {
                        for (int i = 0; i < propChildren.getLength(); i++) {
                            if (propChildren.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                                ((XMLPropertyEditor)ed).readFromXML(propChildren.item(i));
                                value = ed.getValue();
                                break;
                            }
                        }
                    }
                }
            } else {
                org.w3c.dom.NodeList propChildren = propertyNode.getChildNodes();
                if ((propChildren != null) &&(propChildren.getLength() > 0)) {
                    for (int i = 0; i < propChildren.getLength(); i++) {
                        if (XML_SERIALIZED_PROPERTY_VALUE.equals(propChildren.item(i).getNodeName())) {
                            String serValue = findAttribute(propChildren.item(i), ATTR_PROPERTY_VALUE);
                            if (serValue != null) {
                                value = decodeValue(serValue);
                            }
                            break;
                        }
                    }
                }
            }
        }

        return value;
    }

    private Object getEncodedPropertyValue(org.w3c.dom.Node propertyNode)
        throws IOException,
               ClassNotFoundException,
               IllegalAccessException,
               InstantiationException
    {
        org.w3c.dom.NamedNodeMap attrs = propertyNode.getAttributes();
        if (attrs == null)
            throw new IOException(); // [PENDING - explanation of problem]

        org.w3c.dom.Node typeNode = attrs.getNamedItem(ATTR_PROPERTY_TYPE);
        org.w3c.dom.Node editorNode = attrs.getNamedItem(ATTR_PROPERTY_EDITOR);
        org.w3c.dom.Node valueNode = attrs.getNamedItem(ATTR_PROPERTY_VALUE);

        if (typeNode == null)
            return null; // value is not stored for this property

        Class valueType = getClassFromString(typeNode.getNodeValue());
        Object value = null;

        PropertyEditor prEd = null;
        if (editorNode != null) {
            Class editorClass = PersistenceObjectRegistry
                                    .loadClass(editorNode.getNodeValue());
            prEd = createPropertyEditor(editorClass, valueType, null);
        }

        if (valueNode != null) {
            try {
                value = decodePrimitiveValue(valueNode.getNodeValue(),
                                             valueType);
                if (prEd != null) {
                    prEd.setValue(value);
                    value = prEd.getValue();
                }
            }
            catch (IllegalArgumentException e) { // should not happen
                value = null;
            }
        }
        else {
            org.w3c.dom.NodeList propChildren = propertyNode.getChildNodes();
            if (propChildren != null && propChildren.getLength() > 0) {
                boolean isXMLSerialized = false;
                for (int i=0, n=propChildren.getLength(); i < n; i++) {
                    org.w3c.dom.Node node = propChildren.item(i);
                    if (XML_SERIALIZED_PROPERTY_VALUE.equals(
                                                        node.getNodeName()))
                    {
                        isXMLSerialized = true;
                        String serValue = findAttribute(node,
                                                        ATTR_PROPERTY_VALUE);
                        if (serValue != null)
                            value = decodeValue(serValue);
                        break;
                    }
                }

                if (!isXMLSerialized && prEd instanceof XMLPropertyEditor) {
                    for (int i=0, n=propChildren.getLength(); i < n; i++) {
                        org.w3c.dom.Node node = propChildren.item(i);
                        if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                            ((XMLPropertyEditor)prEd).readFromXML(node);
                            value = prEd.getValue();
                            break;
                        }
                    }
                }
            }
        }

        return value;
    }

    private static Class getClassFromString(String type)
        throws ClassNotFoundException
    {
        if ("int".equals(type)) // NOI18N
            return Integer.TYPE;
        else if ("short".equals(type)) // NOI18N
            return Short.TYPE;
        else if ("byte".equals(type)) // NOI18N
            return Byte.TYPE;
        else if ("long".equals(type)) // NOI18N
            return Long.TYPE;
        else if ("float".equals(type)) // NOI18N
            return Float.TYPE;
        else if ("double".equals(type)) // NOI18N
            return Double.TYPE;
        else if ("boolean".equals(type)) // NOI18N
            return Boolean.TYPE;
        else if ("char".equals(type)) // NOI18N
            return Character.TYPE;
        else
            return PersistenceObjectRegistry.loadClass(type);
    }

    /** Decodes a value of given type from the specified String. Supported types are: <UL>
     * <LI> RADConnectionPropertyEditor.RADConnectionDesignValue
     * <LI> Class
     * <LI> String
     * <LI> Integer, Short, Byte, Long, Float, Double, Boolean, Character </UL>
     * @return decoded value
     * @exception IllegalArgumentException thrown if specified object is not of supported type
     */
    private Object decodePrimitiveValue(String encoded, Class type) throws IllegalArgumentException{
        if ("null".equals(encoded)) return null; // NOI18N

        if (Integer.class.isAssignableFrom(type) || Integer.TYPE.equals(type)) {
            return Integer.valueOf(encoded);
        } else if (Short.class.isAssignableFrom(type) || Short.TYPE.equals(type)) {
            return Short.valueOf(encoded);
        } else if (Byte.class.isAssignableFrom(type) || Byte.TYPE.equals(type)) {
            return Byte.valueOf(encoded);
        } else if (Long.class.isAssignableFrom(type) || Long.TYPE.equals(type)) {
            return Long.valueOf(encoded);
        } else if (Float.class.isAssignableFrom(type) || Float.TYPE.equals(type)) {
            return Float.valueOf(encoded);
        } else if (Double.class.isAssignableFrom(type) || Double.TYPE.equals(type)) {
            return Double.valueOf(encoded);
        } else if (Boolean.class.isAssignableFrom(type) || Boolean.TYPE.equals(type)) {
            return Boolean.valueOf(encoded);
        } else if (Character.class.isAssignableFrom(type) || Character.TYPE.equals(type)) {
            return new Character(encoded.charAt(0));
        } else if (String.class.isAssignableFrom(type)) {
            return encoded;
        } else if (Class.class.isAssignableFrom(type)) {
            try {
                return PersistenceObjectRegistry.loadClass(encoded);
            } catch (ClassNotFoundException e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    e.printStackTrace();
                // will return null as the notification of failure
            }
        }
        throw new IllegalArgumentException();
    }

    /** Encodes specified value into a String. Supported types are: <UL>
     * <LI> Class
     * <LI> String
     * <LI> Integer, Short, Byte, Long, Float, Double, Boolean, Character </UL>
     * @return String containing encoded value or null if specified object is not of supported type
     */
    private String encodePrimitiveValue(Object value) {
        if ((value instanceof Integer) ||
            (value instanceof Short) ||
            (value instanceof Byte) ||
            (value instanceof Long) ||
            (value instanceof Float) ||
            (value instanceof Double) ||
            (value instanceof Boolean) ||
            (value instanceof Character)) {
            return value.toString();
        }

        if (value instanceof String) {
            return(String)value;
        }

        if (value instanceof Class) {
            return((Class)value).getName();
        }

        if (value == null) {
            return "null"; // NOI18N
        }

        return null; // is not a primitive type
    }

    /** Decodes a value of from the specified String containing textual representation of serialized stream.
     * @return decoded object
     * @exception IOException thrown if an error occures during deserializing the object
     */
    public static Object decodeValue(String value) throws IOException {
        if ((value == null) ||(value.length() == 0)) return null;

        char[] bisChars = value.toCharArray();
        byte[] bytes = new byte[bisChars.length];
        String singleNum = ""; // NOI18N
        int count = 0;
        for (int i = 0; i < bisChars.length; i++) {
            if (',' == bisChars[i]) {
                try {
                    bytes[count++] = Byte.parseByte(singleNum);
                } catch (NumberFormatException e) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) e.printStackTrace(); // NOI18N
                    throw new IOException();
                }
                singleNum = ""; // NOI18N
            } else {
                singleNum += bisChars[i];
            }
        }

        // add the last byte
        bytes[count++] = Byte.parseByte(singleNum);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes, 0, count);
        try {
            class PORObjectInputStream extends ObjectInputStream {
                public PORObjectInputStream(InputStream is) throws IOException {
                    super(is);
                }
                protected Class resolveClass(ObjectStreamClass v)
                throws IOException, ClassNotFoundException {
                    return PersistenceObjectRegistry.loadClass(v.getName());
                }
            }
            ObjectInputStream ois = new PORObjectInputStream(bis);
            return ois.readObject();
        }
        catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                e.printStackTrace();
            throw new IOException();
        }
    }

    /** Encodes specified value to a String containing textual representation of serialized stream.
     * @return String containing textual representation of the serialized object
     */
    public static String encodeValue(Object value) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            oos.close();
        } catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) e.printStackTrace(); // NOI18N
            return null; // problem during serialization
        }
        byte[] bosBytes = bos.toByteArray();
        StringBuffer sb = new StringBuffer(bosBytes.length);
        for (int i = 0; i < bosBytes.length; i++) {
            if (i != bosBytes.length - 1) {
                sb.append(bosBytes[i]+","); // NOI18N
            } else {
                sb.append(""+bosBytes[i]); // NOI18N
            }
        }
        return sb.toString();
    }

    // --------------------------------------------------------------------------------------
    // Utility formatting methods

    private static void addElementOpen(StringBuffer buf, String elementName) {
        buf.append("<"); // NOI18N
        buf.append(elementName);
        buf.append(">\n"); // NOI18N
    }

    private static void addElementOpenAttr(StringBuffer buf,
                                           String elementName,
                                           String[] attrNames,
                                           String[] attrValues)
    {
        buf.append("<"); // NOI18N
        buf.append(elementName);
        for (int i = 0; i < attrNames.length; i++) {
            if (attrValues[i] == null) continue;
            buf.append(" "); // NOI18N
            buf.append(attrNames[i]);
            buf.append("=\""); // NOI18N
            buf.append(encodeToProperXML(attrValues[i]));
            buf.append("\""); // NOI18N
        }
        buf.append(">\n"); // NOI18N
    }

    private static void addLeafElementOpenAttr(StringBuffer buf,
                                               String elementName,
                                               String[] attrNames,
                                               String[] attrValues)
    {
        buf.append("<"); // NOI18N
        buf.append(elementName);
        for (int i = 0; i < attrNames.length; i++) {
            if (attrValues[i] == null) continue;
            buf.append(" "); // NOI18N
            buf.append(attrNames[i]);
            buf.append("=\""); // NOI18N
            buf.append(encodeToProperXML(attrValues[i]));
            buf.append("\""); // NOI18N
        }
        buf.append("/>\n"); // NOI18N
    }

    private static void addElementClose(StringBuffer buf, String elementName) {
        buf.append("</"); // NOI18N
        buf.append(elementName);
        buf.append(">\n"); // NOI18N
    }

    private void saveNodeIntoText(StringBuffer buf, org.w3c.dom.Node valueNode, String indent) {
        buf.append(indent);
        buf.append("<"); // NOI18N
        buf.append(valueNode.getNodeName());

        org.w3c.dom.NamedNodeMap attributes = valueNode.getAttributes();

        if (attributes != null) {
            ArrayList attribList = new ArrayList(attributes.getLength());
            for (int i = 0; i < attributes.getLength(); i++) {
                attribList.add(attributes.item(i));
            }

            // sort the attributes by attribute name
            // probably not necessary, but there is no guarantee that
            // the order of attributes will remain the same in DOM
            Collections.sort(attribList, new Comparator() {
                public int compare(Object o1, Object o2) {
                    org.w3c.dom.Node n1 =(org.w3c.dom.Node)o1;
                    org.w3c.dom.Node n2 =(org.w3c.dom.Node)o2;
                    return n1.getNodeName().compareTo(n2.getNodeName());
                }
            }
                             );

            for (Iterator it = attribList.iterator(); it.hasNext();) {
                org.w3c.dom.Node attrNode =(org.w3c.dom.Node)it.next();
                String attrName = attrNode.getNodeName();
                String attrValue = attrNode.getNodeValue();

                buf.append(" "); // NOI18N
                buf.append(encodeToProperXML(attrName));
                buf.append("=\""); // NOI18N
                buf.append(encodeToProperXML(attrValue));
                buf.append("\""); // NOI18N
            }
        }
        // [PENDING - CNODES, TEXT NODES, ...]

        org.w3c.dom.NodeList children = valueNode.getChildNodes();
        if ((children == null) ||(children.getLength() == 0)) {
            buf.append("/>\n"); // NOI18N
        } else {
            buf.append(">\n"); // NOI18N
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes
                saveNodeIntoText(buf, children.item(i), indent + ONE_INDENT);
            }
            buf.append(indent);
            buf.append("</"); // NOI18N
            buf.append(encodeToProperXML(valueNode.getNodeName()));
            buf.append(">\n"); // NOI18N
        }
    }

    // --------------------------------------------------------------------------------------
    // Utility DOM access methods

    private static String encodeToProperXML(String text) {
        if (text.indexOf('&') != -1)
            text = Utilities.replaceString(text, "&", "&amp;"); // must be the first to prevent changes in the &XX; codes // NOI18N

        if (text.indexOf('<') != -1)
            text = Utilities.replaceString(text, "<", "&lt;"); // NOI18N
        if (text.indexOf('>') != -1)
            text = Utilities.replaceString(text, ">", "&gt;"); // NOI18N
        if (text.indexOf('\'') != -1)
            text = Utilities.replaceString(text, "\'", "&apos;"); // NOI18N
        if (text.indexOf('\"') != -1)
            text = Utilities.replaceString(text, "\"", "&quot;"); // NOI18N
        if (text.indexOf('\n') != -1)
            text = Utilities.replaceString(text, "\n", "&#xa;"); // NOI18N
        if (text.indexOf('\t') != -1)
            text = Utilities.replaceString(text, "\t", "&#x9;"); // NOI18N

        return text;
    }

    /** Finds first subnode of given node with specified name.
     * @param node the node whose subnode we are looking for
     * @param name requested name of the subnode
     * @return the found subnode or null if no such subnode exists
     */
    private org.w3c.dom.Node findSubNode(org.w3c.dom.Node node, String name) {
        org.w3c.dom.NodeList children = node.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes
                if (name.equals(children.item(i).getNodeName())) {
                    return children.item(i);
                }
            }
        }
        return null;
    }

    /** Finds all subnodes of given node with specified name.
     * @param node the node whose subnode we are looking for
     * @param name requested name of the subnode
     * @return array of the found subnodes
     */
    private org.w3c.dom.Node[] findSubNodes(org.w3c.dom.Node node, String name) {
        ArrayList list = new ArrayList();
        org.w3c.dom.NodeList children = node.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes
                if (name.equals(children.item(i).getNodeName())) {
                    list.add(children.item(i));
                }
            }
        }
        return(org.w3c.dom.Node[]) list.toArray(new org.w3c.dom.Node[list.size()]);
    }

    /** Utility method to obtain given attribute value from specified Node.
     * @return attribute name or null if the attribute is not present
     */
    private String findAttribute(org.w3c.dom.Node node, String attributeName) {
        org.w3c.dom.NamedNodeMap attributes = node.getAttributes();
        org.w3c.dom.Node valueNode = attributes.getNamedItem(attributeName);
        if (valueNode == null) return null;
        else return valueNode.getNodeValue();
    }

    // --------------
    // NB 3.2 compatibility - FormInfo conversions

    /**
     * @return class corresponding to given FormInfo class name
     */
    private static Class getCompatibleFormClass(String formInfoName) {
        if (formInfoName == null)
            return null; // no FormInfo name found in form file

        Class formClass = getClassForKnownFormInfo(formInfoName);
        if (formClass != null)
            return formClass; // a well-known FormInfo

        try { // try to instantiate the unknown FormInfo
            org.netbeans.modules.form.forminfo.FormInfo formInfo =
                (org.netbeans.modules.form.forminfo.FormInfo)
                    PersistenceObjectRegistry.createInstance(formInfoName);
            return formInfo.getFormInstance().getClass();
        }
        catch (Throwable ex) { // ignore
            if (ex instanceof ThreadDeath)
                throw (ThreadDeath) ex;
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
        }

        return null;
    }

    /**
     * @return compatible FormInfo class name for given form base class
     */
    private static String getCompatibleFormInfoName(Class formClass,
                                                    String loadedFormInfo) {
        if (loadedFormInfo != null) {
            Class loadedFormClass = getClassForKnownFormInfo(loadedFormInfo);
            if (loadedFormClass == null || loadedFormClass == formClass)
                return loadedFormInfo; // don't change unknown or convenient FormInfo
        }

        return getFormInfoForKnownClass(formClass);
    }


    // FormInfo names used in NB 3.2
    private static final String[] defaultFormInfoNames = {
        "JFrameFormInfo", // NOI18N
        "JPanelFormInfo", // NOI18N
        "JDialogFormInfo", // NOI18N
        "JInternalFrameFormInfo", // NOI18N
        "JAppletFormInfo", // NOI18N
        "FrameFormInfo", // NOI18N
        "PanelFormInfo", // NOI18N
        "DialogFormInfo", // NOI18N
        "AppletFormInfo" }; // NOI18N

    private static Class getClassForKnownFormInfo(String infoName) {
        if (infoName == null)
            return null;
        int i = infoName.lastIndexOf('.');
        String shortName = infoName.substring(i+1);

        if (defaultFormInfoNames[0].equals(shortName))
            return javax.swing.JFrame.class;
        else if (defaultFormInfoNames[1].equals(shortName))
            return javax.swing.JPanel.class;
        else if (defaultFormInfoNames[2].equals(shortName))
            return javax.swing.JDialog.class;
        else if (defaultFormInfoNames[3].equals(shortName))
            return javax.swing.JInternalFrame.class;
        else if (defaultFormInfoNames[4].equals(shortName))
            return javax.swing.JApplet.class;
        else if (defaultFormInfoNames[5].equals(shortName))
            return java.awt.Frame.class;
        else if (defaultFormInfoNames[6].equals(shortName))
            return java.awt.Panel.class;
        else if (defaultFormInfoNames[7].equals(shortName))
            return java.awt.Dialog.class;
        else if (defaultFormInfoNames[8].equals(shortName))
            return java.applet.Applet.class;

        return null;
    }

    private static String getFormInfoForKnownClass(Class formType) {
        String shortName;

        if (javax.swing.JFrame.class.isAssignableFrom(formType))
            shortName = defaultFormInfoNames[0];
        else if (javax.swing.JPanel.class.isAssignableFrom(formType))
            shortName = defaultFormInfoNames[1];
        else if (javax.swing.JDialog.class.isAssignableFrom(formType))
            shortName = defaultFormInfoNames[2];
        else if (javax.swing.JInternalFrame.class.isAssignableFrom(formType))
            shortName = defaultFormInfoNames[3];
        else if (javax.swing.JApplet.class.isAssignableFrom(formType))
            shortName = defaultFormInfoNames[4];
        else if (java.awt.Frame.class.isAssignableFrom(formType))
            shortName = defaultFormInfoNames[5];
        else if (java.awt.Panel.class.isAssignableFrom(formType))
            shortName = defaultFormInfoNames[6];
        else if (java.awt.Dialog.class.isAssignableFrom(formType))
            shortName = defaultFormInfoNames[7];
        else if (java.applet.Applet.class.isAssignableFrom(formType))
            shortName = defaultFormInfoNames[7];
        else return null;

        return "org.netbeans.modules.form.forminfo." + shortName; // NOI18N
    }

    // --------
    // NB 3.1 compatibility - layout persistence conversion tables

    private static final int LAYOUT_BORDER = 0;
    private static final int LAYOUT_FLOW = 1;
    private static final int LAYOUT_BOX = 2;
    private static final int LAYOUT_GRIDBAG = 3;
    private static final int LAYOUT_GRID = 4;
    private static final int LAYOUT_CARD = 5;
    private static final int LAYOUT_ABSOLUTE = 6;
    private static final int LAYOUT_NULL = 7;
    private static final int LAYOUT_JSCROLL = 8;
    private static final int LAYOUT_SCROLL = 9;
    private static final int LAYOUT_JSPLIT = 10;
    private static final int LAYOUT_JTAB = 11;
    private static final int LAYOUT_JLAYER = 12;
    private static final int LAYOUT_TOOLBAR = 13;

    private static final int LAYOUT_UNKNOWN = -1;
    private static final int LAYOUT_FROM_CODE = -2;

    private static final String[] layout31Names = {
        "org.netbeans.modules.form.compat2.layouts.DesignBorderLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignFlowLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignBoxLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignGridLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignCardLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignAbsoluteLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignAbsoluteLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.JScrollPaneSupportLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.ScrollPaneSupportLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.JSplitPaneSupportLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.JTabbedPaneSupportLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.JLayeredPaneSupportLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignBoxLayout" // NOI18N
    }; // fixed table, do not change!

    private static final String[] layout31ConstraintsNames = {
        "org.netbeans.modules.form.compat2.layouts.DesignBorderLayout$BorderConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignFlowLayout$FlowConstraintsDescription", // NOI18N,
        "org.netbeans.modules.form.compat2.layouts.DesignBoxLayout$BoxConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout$GridBagConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignGridLayout$GridConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignCardLayout$CardConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignAbsoluteLayout$AbsoluteConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignAbsoluteLayout$AbsoluteConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.JScrollPaneSupportLayout$JScrollPaneConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.ScrollPaneSupportLayout$ScrollPaneConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.JSplitPaneSupportLayout$JSplitPaneConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.JTabbedPaneSupportLayout$JTabbedPaneConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.JLayeredPaneSupportLayout$JLayeredPaneConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignBoxLayout$BoxConstraintsDescription" // NOI18N
    }; // fixed table, do not change!

    private static final boolean[] reasonable31Constraints = {
        true, false, false, true, false, true, true,
        true, false, false, true, true, true, false
    }; // fixed table, do not change!

    private static final String[] supportedClassNames = {
        "java.awt.BorderLayout", // NOI18N
        "java.awt.FlowLayout", // NOI18N
        "javax.swing.BoxLayout", // NOI18N
        "java.awt.GridBagLayout", // NOI18N
        "java.awt.GridLayout", // NOI18N
        "java.awt.CardLayout", // NOI18N
        "org.netbeans.lib.awtextra.AbsoluteLayout", // NOI18N
        null,
        "javax.swing.JScrollPane", // NOI18N
        "java.awt.ScrollPane", // NOI18N
        "javax.swing.JSplitPane", // NOI18N
        "javax.swing.JTabbedPane", // NOI18N
        "javax.swing.JLayeredPane", // NOI18N
        "javax.swing.JToolBar" // NOI18N
    }; // fixed table, do not change!

    private static final String[][] layout31PropertyNames = {
        { "horizontalGap", "verticalGap" }, // BorderLayout // NOI18N
        { "alignment", "horizontalGap", "verticalGap" }, // FlowLayout // NOI18N
        { "axis" }, // BoxLayout // NOI18N
        { }, // GridBagLayout
        { "rows", "columns", "horizontalGap", "verticalGap" }, // GridLayout // NOI18N
        { "horizontalGap", "verticalGap" }, // CardLayout (ignoring "currentCard") // NOI18N
        { "useNullLayout" }, // AbsoluteLayout // NOI18N
        { "useNullLayout" }, // AbsoluteLayout // NOI18N
        { }, // JScrollPane
        { }, // ScrollPane
        { }, // JSplitPane
        { }, // JTabbedPane
        { }, // JLayeredPane
        { "axis" } // BoxLayout // NOI18N
    }; // fixed table, do not change!

    private static final String[][] layoutDelegatePropertyNames = {
        { "hgap", "vgap" }, // BorderLayout // NOI18N
        { "alignment", "hgap", "vgap" }, // FlowLayout // NOI18N
        { "axis" }, // BoxLayout // NOI18N
        { }, // GridBagLayout
        { "rows", "columns", "hgap", "vgap" }, // GridLayout // NOI18N
        { "hgap", "vgap" }, // CardLayout (ignoring "currentCard") // NOI18N
        { null }, // AbsoluteLayout
        { null }, // null layout
        { }, // JScrollPane
        { }, // ScrollPane
        { }, // JSplitPane
        { }, // JTabbedPane
        { }, // JLayeredPane
        { null } // JToolBar
    }; // fixed table, do not change!

    // methods and constructors for creating code structure
    private static Method setLayoutMethod;
    private static Method simpleAddMethod;
    private static Method addWithConstrMethod;
    private static Method addTabMethod1;
    private static Method addTabMethod2;
    private static Method addTabMethod3;
    private static Method setLeftComponentMethod;
    private static Method setRightComponentMethod;
    private static Method setTopComponentMethod;
    private static Method setBottomComponentMethod;
    private static Method setBoundsMethod;
    private static Method setViewportViewMethod;
    private static Constructor gridBagConstrConstructor;
    private static Constructor insetsConstructor;
    private static Constructor absoluteConstraintsConstructor;

    // Special static field holding last loaded layout index. This is hack for
    // dealing with multiple constraints types saved by NB 3.1 - we load only
    // constraints matching with the current layout. At time of loading
    // constraints, the layout is already loaded but the layout support is not
    // established yet, so the loadConstraints method cannot find out what the
    // current layout of container is.
    private static int layoutConvIndex = LAYOUT_UNKNOWN;
}
