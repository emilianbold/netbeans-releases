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

import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.TopManager;
import org.openide.loaders.XMLDataObject;
import org.openide.util.io.NbObjectInputStream;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.compat2.layouts.*;

/**
 *
 * @author Ian Formanek
 */
public class GandalfPersistenceManager extends PersistenceManager {
    public static final String NB32_VERSION = "1.0"; // NOI18N
    public static final String CURRENT_VERSION = "1.1"; // NOI18N

    public static final String XML_FORM = "Form"; // NOI18N
    public static final String XML_NON_VISUAL_COMPONENTS = "NonVisualComponents"; // NOI18N
    public static final String XML_CONTAINER = "Container"; // NOI18N
    public static final String XML_COMPONENT = "Component"; // NOI18N
    public static final String XML_MENU_COMPONENT = "MenuItem"; // NOI18N
    public static final String XML_MENU_CONTAINER = "Menu"; // NOI18N
    public static final String XML_LAYOUT = "Layout"; // NOI18N
    public static final String XML_CONSTRAINTS = "Constraints"; // NOI18N
    public static final String XML_CONSTRAINT = "Constraint"; // NOI18N
    public static final String XML_SUB_COMPONENTS = "SubComponents"; // NOI18N
    public static final String XML_EVENTS = "Events"; // NOI18N
    public static final String XML_EVENT = "EventHandler"; // NOI18N
    public static final String XML_PROPERTIES = "Properties"; // NOI18N
    public static final String XML_PROPERTY = "Property"; // NOI18N
    public static final String XML_SYNTHETIC_PROPERTY = "SyntheticProperty"; // NOI18N
    public static final String XML_SYNTHETIC_PROPERTIES = "SyntheticProperties"; // NOI18N
    public static final String XML_AUX_VALUES = "AuxValues"; // NOI18N
    public static final String XML_AUX_VALUE = "AuxValue"; // NOI18N
    public static final String XML_SERIALIZED_PROPERTY_VALUE = "SerializedValue"; // NOI18N

    public static final String ATTR_FORM_VERSION = "version"; // NOI18N
    public static final String ATTR_FORM_TYPE = "type"; // NOI18N
    public static final String ATTR_COMPONENT_NAME = "name"; // NOI18N
    public static final String ATTR_COMPONENT_CLASS = "class"; // NOI18N
    public static final String ATTR_PROPERTY_NAME = "name"; // NOI18N
    public static final String ATTR_PROPERTY_TYPE = "type"; // NOI18N
    public static final String ATTR_PROPERTY_EDITOR = "editor"; // NOI18N
    public static final String ATTR_PROPERTY_VALUE = "value"; // NOI18N
    public static final String ATTR_PROPERTY_PRE_CODE = "preCode"; // NOI18N
    public static final String ATTR_PROPERTY_POST_CODE = "postCode"; // NOI18N
    public static final String ATTR_EVENT_NAME = "event"; // NOI18N
    public static final String ATTR_EVENT_LISTENER = "listener"; // NOI18N
    public static final String ATTR_EVENT_PARAMS = "parameters"; // NOI18N
    public static final String ATTR_EVENT_HANDLER = "handler"; // NOI18N
    public static final String ATTR_AUX_NAME = "name"; // NOI18N
    public static final String ATTR_AUX_VALUE = "value"; // NOI18N
    public static final String ATTR_AUX_VALUE_TYPE = "type"; // NOI18N
    public static final String ATTR_LAYOUT_CLASS = "class"; // NOI18N
    public static final String ATTR_CONSTRAINT_LAYOUT = "layoutClass"; // NOI18N
    public static final String ATTR_CONSTRAINT_VALUE = "value"; // NOI18N

    private static final String ONE_INDENT =  "  "; // NOI18N
    private static final Object NO_VALUE = new Object();

    private org.w3c.dom.Document topDocument =
        org.openide.xml.XMLUtil.createDocument("topDocument",null,null,null); // NOI18N

    private Map containerDependentProperties;

    private FileObject formFile;

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
    public FormModel loadForm(FormDataObject formObject) throws IOException {
        formFile = formObject.getFormEntry().getFile();
        org.w3c.dom.Document doc;
        org.w3c.dom.Element mainElement;
        String encoding;
        try {
            encoding = readEncoding(formFile.getURL().openStream());
            doc = org.openide.loaders.XMLDataObject.parse(formFile.getURL());
            mainElement = doc.getDocumentElement();
        } catch (org.xml.sax.SAXException e) {
            throw new IOException(e.getMessage());
        }

        // 1. check the top-level element name
        if (!XML_FORM.equals(mainElement.getTagName()))
            throw new IOException(FormEditor.getFormBundle().getString("ERR_BadXMLFormat"));

        // 2. check the form version
        String version = mainElement.getAttribute(ATTR_FORM_VERSION);
        if (!NB32_VERSION.equals(version) && !CURRENT_VERSION.equals(version))
            throw new IOException(FormEditor.getFormBundle().getString("ERR_BadXMLVersion"));

        formInfoName = mainElement.getAttribute(ATTR_FORM_TYPE);

        FormModel formModel = new FormModel();
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
                    System.out.println("[WARNING] Form type detection falls back to FormInfo type."); // NOI18N
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
        org.w3c.dom.NodeList childNodes = mainElement.getChildNodes();
        if (childNodes == null) {
            throw new IOException(FormEditor.getFormBundle().getString("ERR_BadXMLFormat"));
        }

        containerDependentProperties = null;

        loadNonVisuals(mainElement, formModel);

        RADComponent topComp = formModel.getTopRADComponent();
        if (topComp != null) {
            try {
                loadComponent(mainElement, topComp);
                if (topComp instanceof ComponentContainer)
                    loadContainer(mainElement, topComp);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        containerDependentProperties = null;

        return formModel;
    }

    private void loadNonVisuals(org.w3c.dom.Node node, FormModel formModel) {
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

                RADComponent comp = restoreComponent(subnode, formModel);
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
                                          FormModel formModel) {
        String className = findAttribute(node, ATTR_COMPONENT_CLASS);
        String compName = findAttribute(node, ATTR_COMPONENT_NAME);

        try {
            Class compClass = PersistenceObjectRegistry.loadClass(className);

            RADComponent newComp;

            if (XML_COMPONENT.equals(node.getNodeName())) {
                if (java.awt.Component.class.isAssignableFrom(compClass))
                    newComp = new RADVisualComponent();
                else newComp = new RADComponent();
            }
            else if (XML_MENU_COMPONENT.equals(node.getNodeName())) {
                newComp = new RADMenuItemComponent();
            }
            else if (XML_MENU_CONTAINER.equals(node.getNodeName())) {
                newComp = new RADMenuComponent();
            }
            else if (XML_CONTAINER.equals(node.getNodeName())) {
                if (java.awt.Container.class.isAssignableFrom(compClass))
                    newComp = new RADVisualContainer();
                else newComp = new RADContainer();
            }
            else return null;

            newComp.initialize(formModel);
            newComp.initInstance(compClass);
            newComp.setName(compName);

            loadComponent(node, newComp);
            if (newComp instanceof ComponentContainer)
                loadContainer(node, newComp);

            return newComp;
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
                               RADComponent comp)
    throws Exception {
        org.w3c.dom.NodeList childNodes = node.getChildNodes();
        if (childNodes == null)
            return;

        for (int i = 0; i < childNodes.getLength(); i++) {
            org.w3c.dom.Node componentNode = childNodes.item(i);
            if (componentNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
                continue; // ignore text nodes

            if (XML_PROPERTIES.equals(componentNode.getNodeName())) {
                loadProperties(componentNode, comp);
            }
            else if (XML_EVENTS.equals(componentNode.getNodeName())) {
                Collection events = loadEvents(componentNode);
                if (events != null) {
                    comp.getEventHandlers().initEvents(events);
                }
            }
            else if (XML_AUX_VALUES.equals(componentNode.getNodeName())) {
                HashMap auxValues = loadAuxValues(componentNode);
                if (auxValues != null) {
                    for (Iterator it = auxValues.keySet().iterator(); it.hasNext();) {
                        String auxName =(String)it.next();
                        comp.setAuxValue(auxName, auxValues.get(auxName));
                    }

                    // if the component is serialized, deserialize it
                    if (JavaCodeGenerator.VALUE_SERIALIZE.equals(
                            auxValues.get(JavaCodeGenerator.AUX_CODE_GENERATION)))
                    {
                        try {
                            String serFile = (String) auxValues.get(
                                             JavaCodeGenerator.AUX_SERIALIZE_TO);
                            if (serFile == null)
                                serFile = formFile.getName() + "_" + comp.getName(); // NOI18N

                            String serName = formFile.getParent().getPackageName('.');
                            if (!"".equals(serName)) // NOI18N
                                serName += "."; // NOI18N
                            serName += serFile;

                            Object instance = Beans.instantiate(
                                TopManager.getDefault().currentClassLoader(),
                                serName);

                            comp.setInstance(instance);
                        }
                        catch (Exception ex) { // ignore
                            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                                ex.printStackTrace();
                        }
                    }
                }
            }
            else if (XML_SYNTHETIC_PROPERTIES.equals(componentNode.getNodeName())) {
                loadSyntheticProperties(componentNode, comp);
            }
        }

        if (!(comp instanceof RADVisualComponent))
            return; // not a visual component

        org.w3c.dom.Node constraintsNode = findSubNode(node, XML_CONSTRAINTS);
        if (constraintsNode == null)
            return; // no constraints

        // load layout constraints of the visual component
        RADVisualComponent vcomp = (RADVisualComponent) comp;

        org.w3c.dom.Node[] constrNodes = findSubNodes(constraintsNode, XML_CONSTRAINT);
        for (int i = 0; i < constrNodes.length; i++) {
            org.w3c.dom.Node constrNode = constrNodes[i];
            String designLayoutName = findAttribute(constrNode,
                                                    ATTR_CONSTRAINT_LAYOUT);
            String cdName = findAttribute(constrNode,
                                          ATTR_CONSTRAINT_VALUE);
            if (designLayoutName == null || cdName == null)
                continue;

            DesignLayout.ConstraintsDescription cd =
                (DesignLayout.ConstraintsDescription)
                    PersistenceObjectRegistry.createInstance(cdName);

            org.w3c.dom.NodeList children = constrNode.getChildNodes();
            if (children != null) {
                for (int j = 0; j < children.getLength(); j++) {
                    if (children.item(j).getNodeType()
                            == org.w3c.dom.Node.ELEMENT_NODE) {
                        cd.readFromXML(children.item(j));
                        break;
                    }
                }
            }

            String layoutSupportName = Compat31LayoutFactory
                     .getCompatibleLayoutSupportName(designLayoutName);

            if (layoutSupportName != null) { // convert constraints
                LayoutSupport.ConstraintsDesc lsDesc =
                    Compat31LayoutFactory.createCompatibleConstraints(cd);
                if (lsDesc != null) { // use converted constraints for LayoutSupport
                    ((RADVisualComponent)comp).setConstraintsDesc(
                        PersistenceObjectRegistry.loadClass(layoutSupportName),
                        lsDesc);
                }
            }
            else { // use the original constraints (of DesignLayout)
                ((RADVisualComponent)comp).setConstraints(
                    PersistenceObjectRegistry.loadClass(designLayoutName), cd);
            }
        }
    }

    // expects that the component has been already loaded by loadComponent(...)
    private void loadContainer(org.w3c.dom.Node node,
                               RADComponent comp)
    throws Exception {
        if (!(comp instanceof ComponentContainer))
            return;

        org.w3c.dom.Node subCompsNode = findSubNode(node, XML_SUB_COMPONENTS);
        org.w3c.dom.NodeList children = null;
        if (subCompsNode != null)
            children = subCompsNode.getChildNodes();
        if (children != null) {
            ArrayList list = new ArrayList();
            for (int i = 0; i < children.getLength(); i++) {
                org.w3c.dom.Node componentNode = children.item(i);
                if (componentNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
                    continue; // ignore text nodes

                RADComponent newComp = restoreComponent(componentNode,
                                                        comp.getFormModel());
                if (newComp != null)
                    list.add(newComp);
            }

            RADComponent[] childComps = new RADComponent[list.size()];
            list.toArray(childComps);
            ((ComponentContainer)comp).initSubComponents(childComps);
        }
        else {
            ((ComponentContainer)comp).initSubComponents(new RADComponent[0]);
        }

        if (comp instanceof RADVisualContainer) {
            org.w3c.dom.Node layoutNode = findSubNode(node, XML_LAYOUT);
            if (layoutNode != null
                    && LayoutSupportRegistry.getLayoutSupportForContainer(
                                                comp.getBeanClass()) == null) {
                String dlClassName = findAttribute(layoutNode, ATTR_LAYOUT_CLASS);
//                try {
                DesignLayout dl = (DesignLayout)
                    PersistenceObjectRegistry.createInstance(dlClassName);

                org.w3c.dom.Node[] propNodes = findSubNodes(layoutNode, XML_PROPERTY);
                if (propNodes.length > 0) {
                    HashMap propsMap = new HashMap(propNodes.length * 2);
                    for (int i = 0; i < propNodes.length; i++) {
                        try {
                            Object propValue = getEncodedPropertyValue(propNodes[i], null);
                            String propName = findAttribute(propNodes[i], ATTR_PROPERTY_NAME);
                            if ((propName != null) &&(propValue != null) &&(propValue != NO_VALUE)) {
                                propsMap.put(propName, propValue);
                            }
                        } catch (Exception e) {
                            if (Boolean.getBoolean("netbeans.debug.exceptions"))
                                e.printStackTrace(); // NOI18N
                            // ignore property with problem
                            // [PENDING - notify problem]
                        }
                    }
                    dl.initChangedProperties(propsMap);
                    dl.setRADContainer((RADVisualContainer)comp);
                }
                LayoutSupport layoutSupp =
                    Compat31LayoutFactory.createCompatibleLayoutSupport(dl);
                ((RADVisualContainer)comp).setLayoutSupport(layoutSupp);
//                } catch (Exception e) {
//                    // if (System.getProperty("netbeans.debug.exceptions") != null) // [PENDING]
//                    if (Boolean.getBoolean("netbeans.debug.exceptions")) e.printStackTrace(); // NOI18N
//                    return false; // [PENDING - notify]
//                }
            }
            else { // no layout saved, or it is a dedicated layout support
                ((RADVisualContainer)comp).initLayoutSupport();
            }
        }

        // hack for properties that can't be set until all children 
        // are added to the container
        List postProps;
        if (containerDependentProperties != null
            && (postProps = (List) containerDependentProperties
                                       .get(comp)) != null)
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
                        Class propertyClass = findPropertyType(propType);
                        PropertyEditor ed = FormEditor.createPropertyEditor(editorClass, propertyClass, prop);
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
                    if (propType != null) propClass = findPropertyType(propType);
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
                            menuComp.getFormModel().removeComponent(menuComp);
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
                                    auxValueType = findPropertyType(auxValueClass);
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
    public void saveForm(FormDataObject formObject, FormModel manager) throws IOException {
        FileObject formFile = formObject.getFormEntry().getFile();
        if (formFile.isReadOnly()) // should not happen
            throw new IllegalStateException("Tried to save read-only form: "+formFile.getName()); // NOI18N

        FileLock lock = null;
        java.io.OutputStream os = null;
        String encoding = "UTF-8"; // NOI18N

        try {
            lock = formFile.lock();
            StringBuffer buf1 = new StringBuffer();
            StringBuffer buf2 = new StringBuffer();

            // start with the lowest version; if there is nothing in the
            // form that requires higher format version, then the form file
            // will be compatible with NB 3.2
            formatVersion = NB32_VERSION;

            RADComponent topComp = manager.getTopRADComponent();
            RADVisualFormContainer formCont =
                topComp instanceof RADVisualFormContainer ?
                    (RADVisualFormContainer) topComp : null;

            // store XML file header
            buf1.append("<?xml version=\"1.0\" encoding=\"");
            buf1.append(encoding);
            buf1.append("\" ?>\n\n"); // NOI18N

            // store "Other Components"
            RADComponent[] nonVisuals = manager.getNonVisualComponents();

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
                                                   manager.getFormBaseClass(),
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
            if (os != null) os.close();
            if (lock != null) lock.releaseLock();
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

    private void saveContainer(ComponentContainer container, StringBuffer buf, String indent) {
        RADComponent[] children = null;

        if (container instanceof RADVisualContainer) {
            saveVisualComponent((RADVisualComponent)container, buf, indent);
            saveLayout(((RADVisualContainer)container).getLayoutSupport(), buf, indent);

            // compatibility hack for saving form's menu bar (part II)
            if (container instanceof RADVisualFormContainer)
                children = ((RADVisualContainer)container).getSubComponents();
        } 
        else saveComponent((RADComponent)container, buf, indent);

        if (children == null)
            children = container.getSubBeans();

        if (children.length > 0) {
            buf.append(indent); addElementOpen(buf, XML_SUB_COMPONENTS);
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof RADMenuItemComponent)
                    raiseFormatVersion(CURRENT_VERSION);

                saveAnyComponent(children[i], buf, indent+ONE_INDENT, true);
            }
            buf.append(indent);
            addElementClose(buf, XML_SUB_COMPONENTS);
        }
    }

    private void saveLayout(LayoutSupport layout, StringBuffer buf, String indent) {
        DesignLayout dl = Compat31LayoutFactory.createCompatibleDesignLayout(layout);
        if (dl != null)
            saveLayout(dl, buf, indent);
    }
    
    private void saveLayout(DesignLayout layout, StringBuffer buf, String indent) {
        buf.append("\n"); // NOI18N
        buf.append(indent);
        List changedProperties = layout.getChangedProperties();
        if (changedProperties.size() == 0) {
            addLeafElementOpenAttr(
                buf,
                XML_LAYOUT,
                new String[] { ATTR_LAYOUT_CLASS },
                new String[] { PersistenceObjectRegistry.getPrimaryName(layout) }
                );
        } else {
            addElementOpenAttr(
                buf,
                XML_LAYOUT,
                new String[] { ATTR_LAYOUT_CLASS },
                new String[] { PersistenceObjectRegistry.getPrimaryName(layout) }
                );
            for (Iterator it = changedProperties.iterator(); it.hasNext();) {
                Node.Property prop =(Node.Property)it.next();
                String propertyName = prop.getName();
                Object value = null;
                try {
                    value = prop.getValue();
                } catch (java.lang.reflect.InvocationTargetException e) {
                    continue; // ignore this property
                } catch (IllegalAccessException e) {
                    continue; // ignore this property
                }
                PropertyEditor ed = prop.getPropertyEditor();

                String encodedValue = null;
                String encodedSerializeValue = null;
                org.w3c.dom.Node valueNode = null;
                if (ed instanceof XMLPropertyEditor) {
                    ed.setValue(value);
                    valueNode =((XMLPropertyEditor)ed).storeToXML(topDocument);
                    if (valueNode == null) continue; // editor refused to save the value
                } else {
                    encodedValue = encodePrimitiveValue(value);
                    if (encodedValue == null) encodedSerializeValue = encodeValue(value);
                    if ((encodedValue == null) &&(encodedSerializeValue == null)) {
                        // [PENDING - notify problem?]
                        continue;
                    }
                }

                buf.append(indent + ONE_INDENT);

                if (encodedValue != null) {
                    addLeafElementOpenAttr(
                        buf,
                        XML_PROPERTY,
                        new String[] { ATTR_PROPERTY_NAME, ATTR_PROPERTY_TYPE, ATTR_PROPERTY_VALUE },
                        new String[] { propertyName, prop.getValueType().getName(), encodedValue }
                        );
                } else {
                    addElementOpenAttr(
                        buf,
                        XML_PROPERTY,
                        new String[] {
                            ATTR_PROPERTY_NAME,
                            ATTR_PROPERTY_TYPE,
                            ATTR_PROPERTY_EDITOR,
                        },
                        new String[] {
                            prop.getName(),
                            prop.getValueType().getName(),
                            ed.getClass().getName(), // XXX ed == null?
                        }
                        );
                    if (valueNode != null) {
                        saveNodeIntoText(buf, valueNode, indent + ONE_INDENT);
                    } else {
                        addLeafElementOpenAttr(
                            buf,
                            XML_SERIALIZED_PROPERTY_VALUE,
                            new String[] {
                                ATTR_PROPERTY_VALUE,
                            },
                            new String[] {
                                encodedSerializeValue,
                            }
                            );
                    }
                    buf.append(indent);
                    addElementClose(buf, XML_PROPERTY);
                }
            }
            buf.append(indent); addElementClose(buf, XML_LAYOUT);
        }
    }

    private void saveVisualComponent(RADVisualComponent component, StringBuffer buf, String indent) {
        saveComponent(component, buf, indent);
        if (!(component instanceof FormContainer)) {
//            buf.append(indent); addElementOpen(buf, XML_CONSTRAINTS);
            saveConstraints(component, buf, indent); // + ONE_INDENT);
//            buf.append(indent); addElementClose(buf, XML_CONSTRAINTS);
        }
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
        if (component.getAuxValues().size() > 0) {
            buf.append("\n"); // NOI18N
            buf.append(indent); addElementOpen(buf, XML_AUX_VALUES);
            saveAuxValues(component.getAuxValues(), buf, indent + ONE_INDENT);
            buf.append(indent); addElementClose(buf, XML_AUX_VALUES);
        }
    }

    private void saveProperties(RADComponent component, StringBuffer buf, String indent) {
        RADProperty[] props = component.getAllBeanProperties();
        for (int i = 0; i < props.length; i++) {
            RADProperty prop =(RADProperty) props[i];

            if (!props[i].isChanged()) {
                if (props[i].getPreCode() != null || props[i].getPostCode() != null) {
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

            Object value = null;
            try {
                value = prop.getValue();
            } catch (Exception e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) e.printStackTrace(); // NOI18N
                // problem getting value => ignore this property
                continue;
            }
            String encodedValue = null;
            String encodedSerializeValue = null;
            org.w3c.dom.Node valueNode = null;
            if (prop.getCurrentEditor() instanceof XMLPropertyEditor) {
                prop.getCurrentEditor().setValue(value);
                valueNode =((XMLPropertyEditor)prop.getCurrentEditor()).storeToXML(topDocument);
                if (valueNode == null) continue; // property editor refused to save the value
            } else {
                encodedValue = encodePrimitiveValue(value);
                if (encodedValue == null) encodedSerializeValue = encodeValue(value);
                if ((encodedValue == null) &&(encodedSerializeValue == null)) {
                    // [PENDING - notify problem?]
                    continue;
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
                        ATTR_PROPERTY_POST_CODE,
                    },
                    new String[] {
                        prop.getName(),
                        prop.getValueType().getName(),
                        encodedValue,
                        prop.getPreCode(),
                        prop.getPostCode(),
                    }
                    );
            } else {
                addElementOpenAttr(
                    buf,
                    XML_PROPERTY,
                    new String[] {
                        ATTR_PROPERTY_NAME,
                        ATTR_PROPERTY_TYPE,
                        ATTR_PROPERTY_EDITOR,
                        ATTR_PROPERTY_PRE_CODE,
                        ATTR_PROPERTY_POST_CODE,
                    },
                    new String[] {
                        prop.getName(),
                        prop.getValueType().getName(),
                        prop.getCurrentEditor().getClass().getName(),
                        prop.getPreCode(),
                        prop.getPostCode(),
                    }
                    );
                if (valueNode != null) {
                    saveNodeIntoText(buf, valueNode, indent + ONE_INDENT);
                } else {
                    buf.append(indent + ONE_INDENT);
                    addLeafElementOpenAttr(
                        buf,
                        XML_SERIALIZED_PROPERTY_VALUE,
                        new String[] {
                            ATTR_PROPERTY_VALUE,
                        },
                        new String[] {
                            encodedSerializeValue,
                        }
                        );
                }
                buf.append(indent);
                addElementClose(buf, XML_PROPERTY);
            }
        }
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

    private void saveConstraints(RADVisualComponent component,
                                 StringBuffer buf,
                                 String indent) {
        RADVisualContainer parentCont = component.getParentContainer();
        if (parentCont == null)
            return;

        LayoutSupport laySup = parentCont.getLayoutSupport();
        if (laySup == null)
            return;

        LayoutSupport.ConstraintsDesc lsConstr = laySup.getConstraints(component);
        if (lsConstr == null)
            return;

        DesignLayout.ConstraintsDescription dlConstr =
            Compat31LayoutFactory.createCompatibleConstraints(lsConstr);
        if (dlConstr == null) return;

        org.w3c.dom.Node constrNode = dlConstr.storeToXML(topDocument);
        if (constrNode != null) {
            buf.append(indent);
            addElementOpen(buf, XML_CONSTRAINTS);
            buf.append(indent + ONE_INDENT);
            addElementOpenAttr(
                buf,
                XML_CONSTRAINT,
                new String[] {
                    ATTR_CONSTRAINT_LAYOUT,
                    ATTR_CONSTRAINT_VALUE },
                new String[] {
                    Compat31LayoutFactory.getCompatibleDesignLayoutName(
                                                laySup.getClass().getName()),
                    PersistenceObjectRegistry.getPrimaryName(dlConstr),
                });

            saveNodeIntoText(buf, constrNode, indent + ONE_INDENT + ONE_INDENT);

            buf.append(indent + ONE_INDENT);
            addElementClose(buf, XML_CONSTRAINT);
            buf.append(indent);
            addElementClose(buf, XML_CONSTRAINTS);
        }
/** XXX        
        Map constraintsMap = component.getConstraintsMap();
        for (Iterator it = constraintsMap.keySet().iterator(); it.hasNext();) {
            String layoutName =(String)it.next();
            DesignLayout.ConstraintsDescription cd =
                (DesignLayout.ConstraintsDescription)constraintsMap.get(layoutName);

            org.w3c.dom.Node constrNode = cd.storeToXML(topDocument);
            if (constrNode != null) {
                buf.append(indent);
                addElementOpenAttr(
                    buf,
                    XML_CONSTRAINT,
                    new String[] {
                        ATTR_CONSTRAINT_LAYOUT,
                        ATTR_CONSTRAINT_VALUE
                    },
                    new String[] {
                        layoutName,
                        PersistenceObjectRegistry.getPrimaryName(cd),
                    }
                    );

                saveNodeIntoText(buf, constrNode, indent + ONE_INDENT);

                buf.append(indent);
                addElementClose(
                    buf,
                    XML_CONSTRAINT
                    );
            } else {
                buf.append(indent);
                addLeafElementOpenAttr(
                    buf,
                    XML_CONSTRAINT,
                    new String[] {
                        ATTR_CONSTRAINT_LAYOUT,
                        ATTR_CONSTRAINT_VALUE
                    },
                    new String[] {
                        layoutName,
                        PersistenceObjectRegistry.getPrimaryName(cd),
                    }
                    );
            }
        }
        ****/
    }

    // --------------------------------------------------------------------------------------
    // Value encoding methods

    /** Obtains value from given propertyNode for specified RADComponent.
     * @param propertyNode XML node where the property is stored
     * @param radComponent the RADComponent of which the property is to be loaded
     * @return the property value decoded from the node
     */
    private Object getEncodedPropertyValue(org.w3c.dom.Node propertyNode, RADComponent radComponent)
        throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        org.w3c.dom.NamedNodeMap attrs = propertyNode.getAttributes();
        if (attrs == null) {
            throw new IOException(); // [PENDING - explanation of problem]
        }
        org.w3c.dom.Node nameNode = attrs.getNamedItem(ATTR_PROPERTY_NAME);
        org.w3c.dom.Node typeNode = attrs.getNamedItem(ATTR_PROPERTY_TYPE);
        org.w3c.dom.Node editorNode = attrs.getNamedItem(ATTR_PROPERTY_EDITOR);
        org.w3c.dom.Node valueNode = attrs.getNamedItem(ATTR_PROPERTY_VALUE);
        org.w3c.dom.Node preCodeNode = attrs.getNamedItem(ATTR_PROPERTY_PRE_CODE);
        org.w3c.dom.Node postCodeNode = attrs.getNamedItem(ATTR_PROPERTY_POST_CODE);

        if (nameNode == null) {
            throw new IOException(); // [PENDING - explanation of problem]
        }

        RADProperty prop = null;
        if (radComponent != null) prop = radComponent.getPropertyByName(nameNode.getNodeValue());

        if (typeNode == null) {
            if (preCodeNode != null) {
                prop.setPreCode(preCodeNode.getNodeValue());
            }
            if (postCodeNode != null) {
                prop.setPostCode(postCodeNode.getNodeValue());
            }
            return NO_VALUE; // value is not stored for this property, just the pre/post code
        }

        Class propertyType = findPropertyType(typeNode.getNodeValue());
        PropertyEditor ed = null;
        if (editorNode != null) {
            Class editorClass =
                PersistenceObjectRegistry.loadClass(editorNode.getNodeValue());
            if (prop != null) {
                ed = FormEditor.createPropertyEditor(editorClass, propertyType, prop);
            } else {
                if (Boolean.getBoolean("netbeans.debug.form")) { // NOI18N
                    System.out.println("Property: "+nameNode.getNodeValue()+", of component: "+radComponent.getName()+"["+radComponent.getBeanClass().getName()+"] not found."); // NOI18N
                } // [PENDING better notification, localize]
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

    private Class  findPropertyType(String type) throws ClassNotFoundException {
        if ("int".equals(type)) return Integer.TYPE; // NOI18N
        else if ("short".equals(type)) return Short.TYPE; // NOI18N
        else if ("byte".equals(type)) return Byte.TYPE; // NOI18N
        else if ("long".equals(type)) return Long.TYPE; // NOI18N
        else if ("float".equals(type)) return Float.TYPE; // NOI18N
        else if ("double".equals(type)) return Double.TYPE; // NOI18N
        else if ("boolean".equals(type)) return Boolean.TYPE; // NOI18N
        else if ("char".equals(type)) return Character.TYPE; // NOI18N
        else {
            return PersistenceObjectRegistry.loadClass(type);
        }
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

    private void addElementOpen(StringBuffer buf, String elementName) {
        buf.append("<"); // NOI18N
        buf.append(elementName);
        buf.append(">\n"); // NOI18N
    }

    private void addElementOpenAttr(StringBuffer buf, String elementName, String[] attrNames, String[] attrValues) {
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

    private void addLeafElementOpenAttr(StringBuffer buf, String elementName, String[] attrNames, String[] attrValues) {
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

    private void addElementClose(StringBuffer buf, String elementName) {
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

    /*  private void walkTree(org.w3c.dom.Node node, String indent) {
        if (node.getNodeType() == org.w3c.dom.Node.TEXT_NODE) return; // ignore text nodes
        System.out.println(indent + node.getNodeName());
        org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
        if (attrs != null) {
        for (int i = 0; i < attrs.getLength(); i++) {
        org.w3c.dom.Node attr = attrs.item(i);
        System.out.println(indent + "  Attribute: "+ attr.getNodeName()+", value: "+attr.getNodeValue());
        }
        }

        org.w3c.dom.NodeList children = node.getChildNodes();
        if (children != null) {
        for (int i = 0; i < children.getLength(); i++) {
        walkTree(children.item(i), indent + "  ");
        }
        }
        }
    */

    private String encodeToProperXML(String text) {
        if (text.indexOf('&') != -1) text = Utilities.replaceString(text, "&", "&amp;"); // must be the first to prevent changes in the &XX; codes // NOI18N

        if (text.indexOf('<') != -1) text = Utilities.replaceString(text, "<", "&lt;"); // NOI18N
        if (text.indexOf('>') != -1) text = Utilities.replaceString(text, ">", "&gt;"); // NOI18N
        if (text.indexOf('\'') != -1) text = Utilities.replaceString(text, "\'", "&apos;"); // NOI18N
        if (text.indexOf('\"') != -1) text = Utilities.replaceString(text, "\"", "&quot;"); // NOI18N
        if (text.indexOf('\n') != -1) text = Utilities.replaceString(text, "\n", "&#xa;"); // NOI18N
        if (text.indexOf('\t') != -1) text = Utilities.replaceString(text, "\t", "&#x9;"); // NOI18N
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
    // FormInfo conversion methods

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
}
