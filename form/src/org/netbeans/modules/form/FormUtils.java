/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

/* $Id$ */

package org.netbeans.modules.form;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import java.text.MessageFormat;

import org.openide.util.Utilities;
import org.openide.util.io.*;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.netbeans.modules.form.util.*;
import org.openide.util.NbBundle;
import org.openide.TopManager;

/**
 * A class that contains utility methods for the formeditor.
 * @author Ian Formanek
 */

public class FormUtils
{
    // Static variables

    private static String PROP_NAME = "PropertyName"; // NOI18N
    private static final boolean debug = System.getProperty("netbeans.debug.form") != null;

    /** The list of all well-known heavyweight components */
    private static Class[] heavyweightComponents;
    private static HashMap jComponentIgnored;
    private static HashMap valuesCache = new HashMap();

    /** The properties whose changes are ignored in JComponent subclasses */

    private static String[] jComponentIgnoredList = new String [] {
        "UI", // NOI18N
        "layout", // NOI18N
        "actionMap", // NOI18N
        "border", // NOI18N
        "model" // NOI18N
    };

    static {
        try {
            heavyweightComponents = new Class[] {
                java.awt.Button.class,
                java.awt.Canvas.class,
                java.awt.List.class,
                java.awt.Button.class,
                java.awt.Label.class,
                java.awt.TextField.class,
                java.awt.TextArea.class,
                java.awt.Checkbox.class,
                java.awt.Choice.class,
                java.awt.List.class,
                java.awt.Scrollbar.class,
                java.awt.ScrollPane.class,
                java.awt.Panel.class,
            };
        } catch (Exception e) {
            throw new InternalError("Cannot initialize AWT classes"); // NOI18N
        }

        jComponentIgnored = new HashMap(15);
        for (int i = 0; i < jComponentIgnoredList.length; i++)
            jComponentIgnored.put(jComponentIgnoredList[i], jComponentIgnoredList[i]);
    }

    // -----------------------------------------------------------------------------
    // Utility methods

    public static void notifyPropertyException(Class beanClass,
                                               String propertyName,
                                               String displayName,
                                               Throwable t,
                                               boolean reading) {
        boolean dontPrint = false;
        // if it is a subclass of Applet, we ignore InvocationTargetException on
        // codeBase, documentBase and appletContext properties

        if (java.applet.Applet.class.isAssignableFrom(beanClass))
            if ("codeBase".equals(propertyName) || // NOI18N
                "documentBase".equals(propertyName) || // NOI18N
                "appletContext".equals(propertyName)) // NOI18N
                dontPrint = true;
        if ("tearOff".equals(propertyName) || "helpMenu".equals(propertyName)) // NOI18N
            dontPrint = true;
        if (!dontPrint) {
            String fmt;
            if (reading)
                fmt = NbBundle.getBundle(FormUtils.class).getString("FMT_ERR_ReadingProperty");
            else
                fmt = NbBundle.getBundle(FormUtils.class).getString("FMT_ERR_WritingProperty");

            TopManager.getDefault().getStdOut().println(
                MessageFormat.format(fmt,
                                     new Object[] { t.getClass().getName(),
                                                    propertyName,
                                                    displayName }));
        }
    }


    /**
     * A utility method that returns the string that should be used for indenting
     * the generated text. It is a String that is a tabSize of spaces
     */
    public static String getIndentString() {
        return "  "; // EditorSettingsJava.getIndentString(); // NOI18N
    }

    //
    // JavaBeans helper mthods
    //

    public static Object cloneObject(Object o) throws CloneNotSupportedException {
        if (o == null)
            return null;
        else if ((o instanceof Byte) ||
                 (o instanceof Short) ||
                 (o instanceof Integer) ||
                 (o instanceof Long) ||
                 (o instanceof Float) ||
                 (o instanceof Double) ||
                 (o instanceof Boolean) ||
                 (o instanceof Character) ||
                 (o instanceof String)) {
            return o; // no need to change reference
        }
        else if (o instanceof Font)
            return Font.getFont(((Font)o).getAttributes());
        else if (o instanceof Color)
            return new Color(((Color)o).getRGB());
        else if (o instanceof Dimension)
            return new Dimension((Dimension)o);
        else if (o instanceof Point)
            return new Point((Point)o);
        else if (o instanceof Rectangle)
            return new Rectangle((Rectangle)o);
        else if (o instanceof Serializable) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(o);
                oos.close();
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bais);
                return ois.readObject();
            } catch (Exception e) {
                throw new CloneNotSupportedException();
            }
        }
        throw new CloneNotSupportedException();
    }

    /**
     * A utility method for checking whether specified component is heavyweight
     * or lightweight.
     * @param comp The component to check
     */

    public static boolean isHeavyweight(Component comp) {
        for (int i=0; i < heavyweightComponents.length; i++)
            if (heavyweightComponents[i].isAssignableFrom(comp.getClass()))
                return true;
        return false;
    }

    public static boolean isIgnoredProperty(Class beanClass, String propertyName) {
        if (JComponent.class.isAssignableFrom(beanClass)) {
            if (jComponentIgnored.get(propertyName) != null)
                return true;
        }
        if (javax.swing.JDesktopPane.class.isAssignableFrom(beanClass) && "desktopManager".equals(propertyName)) // NOI18N
            return true;
        if (javax.swing.JInternalFrame.class.isAssignableFrom(beanClass) && "menuBar".equals(propertyName)) // NOI18N
            return true;
        return false;
    }

    /** @return a default name for event handling method - it is a concatenation of
     * the component name and the name of the listener method(with first letter capital)
     *(e.g. button1MouseReleased).
     */
    public static String getDefaultEventName(RADComponent component, Method listenerMethod) {
        String componentName = component.getName();
        if (component instanceof FormContainer) {
            componentName = "form"; // NOI18N
        }
        return getDefaultEventName(componentName, listenerMethod);
    }

    static String getDefaultEventName(String name, Method listenerMethod) {
        StringBuffer sb = new StringBuffer(name);
        String lm = listenerMethod.getName();
        sb.append(lm.substring(0, 1).toUpperCase());
        sb.append(lm.substring(1));
        return sb.toString();
    }

    /** @return a formatted name of specified method
     */
    public static String getMethodName(MethodDescriptor desc) {
        StringBuffer sb = new StringBuffer(desc.getName());
        Class[] params = desc.getMethod().getParameterTypes();
        if ((params == null) ||(params.length == 0)) {
            sb.append("()"); // NOI18N
        } else {
            for (int i = 0; i < params.length; i++) {
                if (i == 0) sb.append("("); // NOI18N
                else sb.append(", "); // NOI18N
                sb.append(Utilities.getShortClassName(params[i]));
            }
            sb.append(")"); // NOI18N
        }

        return sb.toString();
    }

    // -----------------------------------------------------------------------------
    // Visual utility methods

    /**
     * This method is intended to be started from container's paint method to do all the
     * things concerning its grid. It is not necessary to use it, but it can hide some
     * low-level details to programmers who write their own containers and do not want to
     * go deep into implementation details. Moreover, by using this method it is sure that
     * the grid is done in a standard way.
     * @param xvc  The container which wants to have a grid painted
     * @param g    The Graphics givenas param. to paint method
     * @param gi    Xvc's gridInfo
     * @param offsX  x-offset to paint grid from
     * @param offsY  y-offset to paint grid from
     * @param imW  width of grid
     * @param imH  height of grid
     */
    public static void paintGrid(Component comp, Graphics g, GridInfo gi, int offsX, int offsY, int imW, int imH) {
        if (imW <= 0 || imH <=0) return;
        if (gi.getGridX() == 1 && gi.getGridY() == 1) return; // no grid
        if (gi.gridImage == null || gi.imWidth != imW || gi.imHeight != imH)
            new GridThread(comp, gi, imW, imH).run();

        if (gi.gridImage != null)
            g.drawImage(gi.gridImage, offsX, offsY, null);
    }

    /**
     * This method converts array of Rectangles(with compoment bounds) to
     * GridBagConstraints.
     *
     * Some bug(properly in GridBagLayout) appeares:
     *
     *  here will be bad size!!!!
     *               |
     *               V
     * mmmm  mmmm  mmmm  mmmm
     *         mmmmm
     */
    public static GridBagConstraints[] convertToConstraints(Rectangle[] r, Component[] com) {
        int i, k = r.length;
        GridBagConstraints[] c = new GridBagConstraints[k];
        for (i = 0; i < k; i++) {
            c [i] = new GridBagConstraints();
            int gx = 0, x1 = r [i].x;
            int gy = 0, y1 = r [i].y;
            int gw = 1, x2 = x1 + r [i].width;
            int gh = 1, y2 = y1 + r [i].height;
            int fromX = 0, fromY = 0;
            int j, l = r.length;
            for (j = 0; j < l; j++) {
                int xe = r [j].x + r [j].width;
                int ye = r [j].y + r [j].height;
                if (xe <= x1) {
                    gx++;
                    fromX = Math.max(fromX, xe);
                }
                if (ye <= y1) {
                    gy++;
                    fromY = Math.max(fromY, ye);
                }
                if ((xe > x1) &&(xe < x2)) gw++;
                if ((ye > y1) &&(ye < y2)) gh++;
            }
            c [i].gridx = gx;
            c [i].gridy = gy;
            c [i].gridwidth = gw;
            c [i].gridheight = gh;
            c [i].insets = new Insets(y1 - fromY, x1 - fromX, 0, 0);
            c [i].fill = GridBagConstraints.BOTH;
            c [i].ipadx =(r [i].width - com [i].getPreferredSize().width);
            c [i].ipady =(r [i].height - com [i].getPreferredSize().height);
        }
        return c;
    }

    /**
     * Translates specified event so that the source component of the new event
     * is the original event's source component's parent and the coordinates of
     * the event are translated appropriately to the parent's coordinate space.
     *
     * @param evt the MouseEvent to be translated
     * @param parent the parent component
     * @exception IllegalArgumentException is thrown if the source component
     *    of the MouseEvent is not a subcomponent of given parent component
     */
    static MouseEvent translateMouseEventToParent(MouseEvent evt, Component parent) {
        if (parent == null)
            return evt;

        Component comp = evt.getComponent();
        while (!parent.equals(comp)) {
            if (comp instanceof JComponent)
                evt.translatePoint(((JComponent)comp).getX(),
                                   ((JComponent)comp).getY());
            else {
                Rectangle bounds = comp.getBounds();
                evt.translatePoint(bounds.x, bounds.y);
            }
            comp = comp.getParent();
            if (comp == null) {
                System.err.println("Component: "+evt.getSource()+" is not under its parent's container: "+parent); // NOI18N
                break;
            }
        }

        return new MouseEvent(parent,
                              evt.getID(),
                              evt.getWhen(),
                              evt.getModifiers(),
                              evt.getX(),
                              evt.getY(),
                              evt.getClickCount(),
                              evt.isPopupTrigger()
                              );
    }

    // -----------------------------------------------------------------------------
    // DEBUG utilities

    public static void DEBUG() {
        if (debug) {
            Thread.dumpStack();
        }
    }

    public static void DEBUG(String s) {
        if (debug) {
            System.out.println(s);
        }
    }

    /** A utility method which looks for the first common ancestor of the
     * classes specified. The ancestor is either one of the two classes, if one of them extends the other
     * or their first superclass which is common to both.
     * @param cl1 the first class
     * @param cl2 the first class
     * @return class which is superclass of both classes provided, or null if the first common superclass is java.lang.Object]
     */
    public Class findCommonAncestor(Class cl1, Class cl2) {
        // handle direct inheritance
        if (cl1.isAssignableFrom(cl2)) return cl1; // cl2 is subclass of cl1
        if (cl2.isAssignableFrom(cl1)) return cl2; // cl1 is subclass of cl2

        ArrayList cl1Ancestors = new ArrayList(8);
        ArrayList cl2Ancestors = new ArrayList(8);
        Class cl1An = cl1.getSuperclass();
        Class cl2An = cl2.getSuperclass();
        while (cl1An != null) {
            cl1Ancestors.add(cl1An);
            cl1An = cl1An.getSuperclass();
        }
        while (cl2An != null) {
            cl2Ancestors.add(cl2An);
            cl2An = cl2An.getSuperclass();
        }
        if (cl2Ancestors.size() > cl1Ancestors.size()) {
            ArrayList temp = cl1Ancestors;
            cl1Ancestors = cl2Ancestors;
            cl2Ancestors = temp;
        }
        // cl1Ancestors is now the longer stack of classes,
        // i.e. it must contain the first common superclass
        Class foundClass = null;
        for (Iterator it = cl1Ancestors.iterator(); it.hasNext();) {
            Object o = it.next();
            if (cl2Ancestors.contains(o)) {
                foundClass =(Class)o;
                break;
            }
        }
        if (foundClass.equals(Object.class)) {
            foundClass = null; // if Object is the first common superclass, null is returned
        }
        return foundClass;
    }

    /** A utility method which looks for the first common ancestor of the
     * classes specified. The ancestor is either one of the two classes, if one of them extends the other
     * or their first superclass which is common to both.
     * The stopClass parameter can be used to limit the superclass to be found to be instance
     * @param cl1 the first class
     * @param cl2 the first class
     * @param stopClass a class to limit the results to, i.e. a result returned is either subclass of the stopClass or null. 
     *                  If the stopClass is null, the result is not limited to any particular class
     * @return class which is superclass of both classes provided, or null if the first common superclass is not inmstance of stopClass]
     */
    public Class findCommonAncestor(Class cl1, Class cl2, Class stopClass) {
        Class cl = findCommonAncestor(cl1, cl2);
        if (stopClass == null) return cl;
        if ((cl == null) ||(!(stopClass.isAssignableFrom(cl)))) return null;
        return cl;
    }

    // -----------------------------------------------------------------------------
    // XML utilities

    /** Read property from XML node.
     * @param propName name of property
     * @param propClass class of property(to find editor)
     * @param element XML element representing the property
     * @return value of property created from XML element
     */
    public static Object readProperty(String propName, Class propClass, org.w3c.dom.Node element) throws java.io.IOException {
        org.w3c.dom.NodeList items = element.getChildNodes();
        Object result = null;
        if (items.getLength() >0) {
            PropertyEditor propEdit = FormPropertyEditorManager.findEditor(propClass);
            for (int i=0, n=items.getLength();i<n; i++){
                if (items.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE &&
                    ((org.w3c.dom.Element) items.item(i)).getAttribute(PROP_NAME).equals(propName)) {
                    ((XMLPropertyEditor) propEdit).readFromXML(items.item(i));
                    result = propEdit.getValue();
                }
            }
        }
        if (result == null) {
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
            if (attributes != null) {
                org.w3c.dom.Node attr = attributes.getNamedItem(propName);
                if (attr!=null) {
                    String valueText = attr.getNodeValue();
                    if (valueText != null){
                        result = GandalfPersistenceManager.decodeValue(valueText);
                    }
                }
            }
        }
        return result;
    }

    /** Write information about Color into XML element.
     * @param propName name of property
     * @param value value of property
     * @param propClass class of property(to find editor)
     * @param element XML element to write to
     * @param doc the whole XML document
     */
    public static void writeProperty(String propName, Object value, Class propClass, org.w3c.dom.Element el,org.w3c.dom.Document doc) {
        boolean written = false;
        PropertyEditor propEdit = FormPropertyEditorManager.findEditor(propClass);
        org.w3c.dom.Node valueNode = null;
        if (propEdit instanceof XMLPropertyEditor) {
            propEdit.setValue(value);
            valueNode =((XMLPropertyEditor) propEdit).storeToXML(doc);
            if (valueNode != null) {
                el.appendChild(valueNode);
                if (valueNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    ((org.w3c.dom.Element) valueNode).setAttribute(PROP_NAME, propName);
                }
                written = true;
            }
        }
        if (!written) {
            String encodedSerializeValue = GandalfPersistenceManager.encodeValue(value);
            if (encodedSerializeValue != null) {
                el.setAttribute(propName, encodedSerializeValue);
            } else {
                // [PENDING - notify problem?]
            }
        }
    }
}
