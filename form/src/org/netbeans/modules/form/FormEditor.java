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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.lang.reflect.*;
import java.util.*;
import java.text.MessageFormat;
import javax.swing.*;

import org.openide.*;
import org.openide.awt.*;
import org.openide.explorer.*;
import org.openide.windows.*;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.explorer.propertysheet.editors.NodePropertyEditor;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.*;
import org.openide.util.*;
import org.netbeans.modules.form.util2.*;
import org.netbeans.modules.form.actions.*;
import org.netbeans.modules.form.palette.*;
import org.netbeans.modules.form.compat2.layouts.*;
import org.netbeans.modules.form.compat2.layouts.support.*;
import org.netbeans.beaninfo.editors.TableModelEditor;

import org.netbeans.modules.form.layoutsupport.*;

/**
 * A static class that manages global FormEditor issues.
 * @author   Ian Formanek
 */

final public class FormEditor extends Object
{
    // -----------------------------------------------------------------------------
    // Static variables

    /** The global version number of the FormEditor serialized format */
    public static final NbVersion FORM_EDITOR_VERSION = new NbVersion(1, 0);

    public static final String GUI_EDITING_WORKSPACE_NAME = "Visual"; // NOI18N
    
    
    /** The prefix for event properties. The name of an event property
     * is a concatenation of this string and the event name.
     * E.g. for mousePressed event, the property is named "__EVENT__mousePressed"
     */
    public static final String EVENT_PREFIX = "__EVENT__"; // NOI18N
    /** The prefix for component's layout properties. The name of such property
     * is a concatenation of this string and the component layout property's name.
     * E.g. for Direction layout property, the property is named "__LAYOUT__mousePressed"
     */
    public static final String LAYOUT_PREFIX = "__LAYOUT__"; // NOI18N

    /** The resource bundle for the form editor */
    private static ResourceBundle formBundle = NbBundle.getBundle(FormEditor.class);
    /** Settings of FormEditor */
    private static FormLoaderSettings formSettings = (FormLoaderSettings)
                   SharedClassObject.findObject(FormLoaderSettings.class, true);

    /** The default width of the form window */
    public static final int DEFAULT_FORM_WIDTH = 300;
    /** The default height of the form window */
    public static final int DEFAULT_FORM_HEIGHT = 200;

    static ExplorerActions actions = new ExplorerActions();

    // ---------------------------------------------------
    // Private static variables

    private static ArrayList errorLog = new ArrayList();

    // -----------------------------------------------------------------------------
    // Static methods

    /** Provides the resource bundle for FormEditor */
    public static ResourceBundle getFormBundle() {
        return formBundle;
    }

    /** Provides the settings for the FormEditor */
    public static FormLoaderSettings getFormSettings() {
        return formSettings;
    }

    // used by GandalfPersistenceManager only
    // why is this in FormEditor class??
    public static PropertyEditor createPropertyEditor(
        Class editorClass, Class propertyType, FormProperty property)
        throws InstantiationException, IllegalAccessException
    {
        PropertyEditor ed;
        if (editorClass.equals(RADConnectionPropertyEditor.class)) {
            ed = new RADConnectionPropertyEditor(propertyType);
        } else {
            ed = (PropertyEditor)editorClass.newInstance();
        }
        property.getPropertyContext().initPropertyEditor(ed);
        return ed;
    }

    public static Image getGridImage(Container gridCont) {
        Image gridImage = gridCont.createImage(100, 100);
        Graphics ig = gridImage.getGraphics();
        ig.setColor(gridCont.getBackground());
        ig.fillRect(0, 0, 100, 100);
        ig.setColor(gridCont.getForeground());
        for (int j=0; j< 100; j+= 10)
            for (int i=0; i< 100; i+= 10)
                ig.drawLine(i,j,i,j);
        return gridImage;
    }

    public static String getSerializedBeanName(RADComponent comp) {
        StringBuffer name =
            new StringBuffer(comp.getFormModel().getFormDataObject().getName());
        name.append("$"); // NOI18N
        name.append(comp.getName());
        name.append(".ser"); // NOI18N
        return name.toString();
    }

    public static boolean isNonReflectedProperty(Class clazz, PropertyDescriptor desc) {
        if ("visible".equals(desc.getName())) return true; // NOI18N
        else {
            if (Window.class.isAssignableFrom(clazz)) {
                if ("enabled".equals(desc.getName())) return true; // NOI18N
                else if ("modal".equals(desc.getName())) return true; // NOI18N
            }
        }
        return false;
    }

    /** A method that returns the supporting layout for some containers, which
     * have a special design-time support in the FormEditor.
     * @param itemClass The class of the component the layout is requested for
     * @return the DesignLayout that should be used in the container.
     */
    public static DesignSupportLayout getSupportLayout(Class itemClass) {
        if (JTabbedPane.class.isAssignableFrom(itemClass))
            return new JTabbedPaneSupportLayout();
        else if (JScrollPane.class.isAssignableFrom(itemClass))
            return new JScrollPaneSupportLayout();
        else if (ScrollPane.class.isAssignableFrom(itemClass))
            return new ScrollPaneSupportLayout();
        else if (JSplitPane.class.isAssignableFrom(itemClass))
            return new JSplitPaneSupportLayout();
        else if (JLayeredPane.class.isAssignableFrom(itemClass))
            return new JLayeredPaneSupportLayout();
        return null;
    }

    static RADProperty[] sortProperties(
        java.util.List properties, Class beanClass) {
        return (RADProperty[]) properties.toArray(
            new RADProperty[properties.size()]); // noop so far [PENDING]
    }

    // ---------------------------------------------------
    // inner classes


    final static class ErrorLogItem {
        public static final int WARNING = 0;
        public static final int ERROR = 1;

        public ErrorLogItem(String desc, Throwable t) {
            this(desc, t, ERROR);
        }

        public ErrorLogItem(String desc, Throwable t, int type) {
            thr = t;
            this.type = type;
            this.desc = desc;
        }

        String getDescription() {
            return desc;
        }

        Throwable getThrowable() {
            return thr;
        }

        int getType() {
            return type;
        }

        private String desc;
        private int type;
        private Throwable thr;
    }


    static void clearLog() {
        errorLog.clear();
    }

    public static void fileError(String desc, Throwable t) {
        errorLog.add(new ErrorLogItem(desc, t, ErrorLogItem.ERROR));
    }

    public static void fileWarning(String desc, Throwable t) {
        errorLog.add(new ErrorLogItem(desc, t, ErrorLogItem.WARNING));
    }

    public static void displayErrorLog() {
        if (errorLog.size() == 0) return;

        ErrorLogDialog eld = new ErrorLogDialog((ErrorLogItem[])errorLog.toArray(new ErrorLogItem[errorLog.size()]));
        errDlg = TopManager.getDefault().createDialog(new DialogDescriptor(
            eld,
            FormEditor.getFormBundle().getString("CTL_ErrorsNotificationTitle"),
            true,
            new Object[] { FormEditor.getFormBundle().getString("CTL_CLOSE") },
            FormEditor.getFormBundle().getString("CTL_CLOSE"),
            DialogDescriptor.BOTTOM_ALIGN,
            null,
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    errDlg.setVisible(false);
                }
            }
            )
                                                      );
        errDlg.show();

        clearLog();
    }
    private static java.awt.Dialog errDlg;

    // ---------------

    /** For correct project deserialization from NB 3.1 - ResolvableHelper
     * for ComponentInspector used to be in FormEditor class.
     */
    final public static class ResolvableHelper implements java.io.Serializable {
        static final long serialVersionUID =7424646018839457544L;
        public Object readResolve() {
            return ComponentInspector.getInstance();
        }
    }
}
