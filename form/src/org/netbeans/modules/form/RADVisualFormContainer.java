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

/* $Id$ */

package org.netbeans.modules.form;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.explorer.propertysheet.editors.*;
import org.netbeans.modules.form.compat2.layouts.*;
import org.netbeans.modules.form.forminfo.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JMenuBar;
import javax.swing.JComboBox;
import java.util.Hashtable;

/**
 * RADVisualFormContainer represents the top-level container of the form and
 * the form itself during design time.
 *
 * @author Ian Formanek
 */
public class RADVisualFormContainer extends RADVisualContainer implements FormContainer
{
    public static final String PROP_MENU_BAR = "menuBar"; // NOI18N
    public static final String PROP_FORM_SIZE_POLICY = "formSizePolicy"; // NOI18N
    public static final String PROP_FORM_SIZE = "formSize"; // NOI18N
    public static final String PROP_FORM_POSITION = "formPosition"; // NOI18N
    public static final String PROP_GENERATE_POSITION = "generatePosition"; // NOI18N
    public static final String PROP_GENERATE_SIZE = "generateSize"; // NOI18N
    public static final String PROP_GENERATE_CENTER = "generateCenter"; // NOI18N

    public static final Hashtable encodingList = new Hashtable();
    static {
        encodingList.put("US-ASCII(English)", "US-ASCII"); // NOI18N
        encodingList.put("UTF-8(Compressed Unicode)", "UTF-8"); // NOI18N
        encodingList.put("UTF-16(Compressed UCS)", "UTF-16"); // NOI18N
        encodingList.put("ISO-10646-UCS-2(Raw Unicode)", "ISO-10646-UCS-2"); // NOI18N
        encodingList.put("ISO-10646-UCS-4(Raw UCS)", "ISO-10646-UCS-4"); // NOTE: no support for ISO-10646-UCS-4 yet. [from XmlReader] // NOI18N
        encodingList.put("ISO-8859-1(Latin-1, western Europe)", "ISO-8859-1"); // NOI18N
        encodingList.put("ISO-8859-2(Latin-2, eastern Europe)", "ISO-8859-2"); // NOI18N
        encodingList.put("ISO-8859-3(Latin-3, southern Europe)", "ISO-8859-3"); // NOI18N
        encodingList.put("ISO-8859-4(Latin-4, northern Europe)", "ISO-8859-4"); // NOI18N
        encodingList.put("ISO-8859-5(ASCII plus Cyrillic)", "ISO-8859-5"); // NOI18N
        encodingList.put("ISO-8859-6(ASCII plus Arabic)", "ISO-8859-6"); // NOI18N
        encodingList.put("ISO-8859-7(ASCII plus Greek)", "ISO-8859-7"); // NOI18N
        encodingList.put("ISO-8859-8(ASCII plus Hebrew)", "ISO-8859-8"); // NOI18N
        encodingList.put("ISO-8859-9(Latin-5, Turkish)", "ISO-8859-9"); // NOI18N
        encodingList.put("ISO-2022-JP(Japanese)", "ISO-2022-JP"); // NOI18N
        encodingList.put("Shift_JIS(Japanese, Windows)", "Shift_JIS"); // NOI18N
        encodingList.put("EUC-JP(Japanese, UNIX)", "EUC-JP"); // NOI18N
        encodingList.put("Big5(Chinese, Taiwan)", "Big5"); // NOI18N
        encodingList.put("GB2312(Chinese, mainland China)", "GB2312"); // NOI18N
        encodingList.put("KOI8-R(Russian)", "KOI8-R"); // NOI18N
        encodingList.put("ISO-2022-KR(Korea)", "ISO-2022-KR"); // NOI18N
        encodingList.put("EUC-KR(Korean, UNIX)", "EUC-KR"); // NOI18N
        encodingList.put("ISO-2022-CN(Chinese)", "ISO-2022-CN"); // NOI18N

        encodingList.put("EBCDIC-CP-US(EBCDIC: US)", "EBCDIC-CP-US"); // NOI18N
        encodingList.put("EBCDIC-CP-CA(EBCDIC: Canada)", "EBCDIC-CP-CA"); // NOI18N
        encodingList.put("EBCDIC-CP-NL(EBCDIC: Netherlands)", "EBCDIC-CP-NL"); // NOI18N
        encodingList.put("EBCDIC-CP-WT(like EBCDIC-CP-US)", "EBCDIC-CP-WT"); // NOI18N
        encodingList.put("EBCDIC-CP-DK(EBCDIC: Denmark)", "EBCDIC-CP-DK"); // NOI18N
        encodingList.put("EBCDIC-CP-NO(EBCDIC: Norway)", "EBCDIC-CP-NO"); // NOI18N
        encodingList.put("EBCDIC-CP-FI(EBCDIC: Finland)", "EBCDIC-CP-FI"); // NOI18N
        encodingList.put("EBCDIC-CP-SE(EBCDIC: Sweden)", "EBCDIC-CP-SE"); // NOI18N
        encodingList.put("EBCDIC-CP-IT(EBCDIC: Italy)", "EBCDIC-CP-IT"); // NOI18N
        encodingList.put("EBCDIC-CP-ES(EBCDIC: Spain, Latin America)", "EBCDIC-CP-ES"); // NOI18N
        encodingList.put("EBCDIC-CP-GB(EBCDIC: Great Britain)", "EBCDIC-CP-GB"); // NOI18N
        encodingList.put("EBCDIC-CP-FR(EBCDIC: France)", "EBCDIC-CP-FR"); // NOI18N
        encodingList.put("EBCDIC-CP-AR1(EBCDIC: Arabic)", "EBCDIC-CP-AR1"); // NOI18N
        encodingList.put("EBCDIC-CP-HE(EBCDIC: Hebrew)", "EBCDIC-CP-HE"); // NOI18N
        encodingList.put("EBCDIC-CP-BE(like EBCDIC-CP-CH)", "EBCDIC-CP-BE"); // NOI18N
        encodingList.put("EBCDIC-CP-CH(EBCDIC: Switzerland)", "EBCDIC-CP-CH"); // NOI18N
        encodingList.put("EBCDIC-CP-ROECE(EBCDIC: Roece)", "EBCDIC-CP-ROECE"); // NOI18N
        encodingList.put("EBCDIC-CP-YU(EBCDIC: Yogoslavia)", "EBCDIC-CP-YU"); // NOI18N
        encodingList.put("EBCDIC-CP-IS(EBCDIC: Iceland)", "EBCDIC-CP-IS"); // NOI18N
        encodingList.put("EBCDIC-CP-AR2(EBCDIC: Urdu)", "EBCDIC-CP-AR2"); // NOI18N
    }
    protected static final String AUX_MENU_COMPONENT = "RADVisualFormContainer_MenuComponent"; // NOI18N

    public static final int GEN_BOUNDS = 0;
    public static final int GEN_PACK = 1;
    public static final int GEN_NOTHING = 2;

    /** Localized string for no menu. */
    static final String NO_MENU = FormEditor.getFormBundle().getString("CTL_NoMenu");

    private FormInfo formInfo;
    private Container topContainer;
    private Container topAddContainer;

    // Synthetic properties of form
    private RADComponent menu;
    private boolean menuInitialized = false;
    private Dimension formSize = new Dimension(FormEditor.DEFAULT_FORM_WIDTH, FormEditor.DEFAULT_FORM_HEIGHT);
    private Point formPosition;
    private boolean generatePosition = true;
    private boolean generateSize = true;
    private boolean generateCenter = true;
    private int formSizePolicy = GEN_NOTHING;

    public RADVisualFormContainer(FormInfo formInfo) {
        super();
        this.formInfo = formInfo;
        topContainer = formInfo.getTopContainer();
        topAddContainer = formInfo.getTopAddContainer();
    }

    /** @return The JavaBean visual container represented by this RADVisualComponent */
    public Container getContainer() {
        return topAddContainer;
    }

    public String getJavaContainerDelegateString() {
        String delegateGetter = getContainerDelegateGetterName();
        if (delegateGetter != null) {
            return delegateGetter + "()"; // NOI18N
        }
        else
            return "";          // NOI18N
    }
    
    /**
     * Called to create the instance of the bean. Default implementation
     * simply creates instance of the bean's class using the default
     * constructor.  Top-level container(the form object itself) will redefine
     * this to use FormInfo to create the instance, as e.g. Dialogs cannot be
     * created using the default constructor
     * @return the instance of the bean that will be used during design time
     */
    protected Object createBeanInstance() {
        return formInfo.getFormInstance();
    }

    /** Called to obtain a Java code to be used to generate code to access the
     * container for adding subcomponents.  It is expected that the returned
     * code is either ""(in which case the form is the container) or is a name
     * of variable or method call ending with
     * "."(e.g. "container.getContentPane().").  This implementation simply
     * delegates to FormInfo.getContainerGenName().
     * @return the prefix code for generating code to add subcomponents to this
     * container
     */
    public String getContainerGenName() {
        String delegateGetter = getContainerDelegateGetterName();
        if (delegateGetter != null) {
            return delegateGetter + "()."; // NOI18N
        }
        else
            return "";          // NOI18N
//        return formInfo.getContainerGenName();
    }

    // ------------------------------------------------------------------------------
    // Form synthetic properties

    public FormInfo getFormInfo() {
        return formInfo;
    }

    /**
     * Getter for the Name property of the component - overriden to provide
     * non-null value, as the top-level component does not have a variable
     * @return current value of the Name property
     */
    public String getName() {
        return FormEditor.getFormBundle().getString("CTL_FormTopContainerName");
    }

    /**
     * Setter for the Name property of the component - usually maps to
     * variable declaration for holding the instance of the component
     * @param value new value of the Name property
     */
    public void setName(String value) {
        // noop in forms
    }

    public String getFormMenu() {
        if (!menuInitialized) {
            String menuName =(String)getAuxValue(AUX_MENU_COMPONENT);
            if (menuName != null) {
                ArrayList list = getAvailableMenus();
                for (Iterator it = list.iterator(); it.hasNext();) {
                    RADComponent comp =(RADComponent)it.next();
                    if (comp.getName().equals(menuName)) {
                        menu = comp;
                        break;
                    }
                }
            }
            menuInitialized = true;
        }
        if (menu == null) return null;
        else return menu.getName();
    }

    public void setFormMenu(String value) {
        setAuxValue(AUX_MENU_COMPONENT, value);
        FormDesigner designer = getFormModel().getFormDesigner();
        
        if (value == null) {
            menu = null;
            if (formInfo instanceof JMenuBarContainer) {
                designer.setFormJMenuBar(null);
            } else if (formInfo instanceof MenuBarContainer) {
                designer.setFormMenuBar(null);
            }
        }
        else {
            ArrayList list = getAvailableMenus();
            for (Iterator it = list.iterator(); it.hasNext();) {
                RADComponent comp =(RADComponent)it.next();
                if (comp.getName().equals(value)) {
                    menu = comp;
                }
            }

            if (menu == null)
                return;
            
            if (!getFormModel().isFormLoaded())
                return;

            Object menubar = menu.getBeanInstance();
            
            if (formInfo instanceof JMenuBarContainer
                && menubar instanceof JMenuBar) {
                designer.setFormJMenuBar((JMenuBar) menubar);
            }
            else if (formInfo instanceof MenuBarContainer
                     && menu.getBeanInstance() instanceof MenuBar) {
                designer.setFormMenuBar((MenuBar)menubar);
            }
        }
//        getFormModel().fireFormChanged();
    }

    public Point getFormPosition() {
        if (formPosition == null) {
            formPosition = topContainer.getLocation();
        }
        return formPosition;
    }

    public void setFormPosition(Point value) {
        formPosition = value;
        // [PENDING - set on form window if in single mode]
        getFormModel().fireFormChanged();
    }

    public Dimension getFormSize() {
        if (formSize == null) {
            formSize = topContainer.getSize();
        }
        return formSize;
    }

    public void setFormSize(Dimension value) {
        formSize = value;
        // [PENDING - set on form window if in single mode]
        getFormModel().fireFormChanged();
    }

    public boolean getGeneratePosition() {
        return generatePosition;
    }

    public void setGeneratePosition(boolean value) {
        // [PENDING - set as aux value]
        generatePosition = value;
        getFormModel().fireFormChanged();
    }

    public boolean getGenerateSize() {
        return generateSize;
    }

    public void setGenerateSize(boolean value) {
        // [PENDING - set as aux value]
        generateSize = value;
        getFormModel().fireFormChanged();
    }

    public boolean getGenerateCenter() {
        return generateCenter;
    }

    public void setGenerateCenter(boolean value) {
        // [PENDING - set as aux value]
        generateCenter = value;
        getFormModel().fireFormChanged();
    }

    public int getFormSizePolicy() {
        if (formInfo instanceof JAppletFormInfo
            || formInfo instanceof AppletFormInfo
            || formInfo instanceof JPanelFormInfo
            || formInfo instanceof PanelFormInfo) {
            return GEN_NOTHING;
        }

        return formSizePolicy;
    }

    public void setFormSizePolicy(int value) {
        // [PENDING - set as aux value]
        formSizePolicy = value;
        getFormModel().fireFormChanged();
    }

    // ------------------------------------------------------------------------------
    // End of form synthetic properties

    protected Node.Property[] createSyntheticProperties() {
        if (!getFormModel().getFormEditorSupport().supportsAdvancedFeatures()) {
            if ((formInfo instanceof JMenuBarContainer) ||(formInfo instanceof MenuBarContainer)) {
                return new Node.Property[] { createMenuProperty() } ;
            } else {
                return new Node.Property[0];
            }
        }

        Node.Property policyProperty = new PropertySupport.ReadWrite(PROP_FORM_SIZE_POLICY, Integer.TYPE,
                                                                     FormEditor.getFormBundle().getString("MSG_FormSizePolicy"),
                                                                     FormEditor.getFormBundle().getString("MSG_FormSizePolicy")) {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return new Integer(getFormSizePolicy());
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Integer)) throw new IllegalArgumentException();
                setFormSizePolicy(((Integer)val).intValue());
            }

            public boolean canWrite() {
                return !isReadOnly();
            }

            /** Editor for alignment */
            public java.beans.PropertyEditor getPropertyEditor() {
                return new SizePolicyEditor();
            }

        };


        Node.Property sizeProperty = new PropertySupport.ReadWrite(PROP_FORM_SIZE, Dimension.class,
                                                                   FormEditor.getFormBundle().getString("MSG_FormSize"),
                                                                   FormEditor.getFormBundle().getString("MSG_FormSize")) {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return getFormSize();
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Dimension)) throw new IllegalArgumentException();
                setFormSize((Dimension)val);
            }

            public boolean canWrite() {
                return !isReadOnly();
            }
        };

        Node.Property positionProperty = new PropertySupport.ReadWrite(PROP_FORM_POSITION, Point.class,
                                                                       FormEditor.getFormBundle().getString("MSG_FormPosition"),
                                                                       FormEditor.getFormBundle().getString("MSG_FormPosition")) {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return getFormPosition();
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Point)) throw new IllegalArgumentException();
                setFormPosition((Point)val);
            }

            public boolean canWrite() {
                return !isReadOnly();
            }
        };

        Node.Property genPositionProperty = new PropertySupport.ReadWrite(PROP_GENERATE_POSITION, Boolean.TYPE,
                                                                          FormEditor.getFormBundle().getString("MSG_GeneratePosition"),
                                                                          FormEditor.getFormBundle().getString("MSG_GeneratePosition")) {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return new Boolean(getGeneratePosition());
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Boolean)) throw new IllegalArgumentException();
                setGeneratePosition(((Boolean)val).booleanValue());
            }

            public boolean canWrite() {
                return !isReadOnly();
            }
        };

        Node.Property genSizeProperty = new PropertySupport.ReadWrite(PROP_GENERATE_SIZE, Boolean.TYPE,
                                                                      FormEditor.getFormBundle().getString("MSG_GenerateSize"),
                                                                      FormEditor.getFormBundle().getString("MSG_GenerateSize")) {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return new Boolean(getGenerateSize());
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Boolean)) throw new IllegalArgumentException();
                setGenerateSize(((Boolean)val).booleanValue());
            }

            public boolean canWrite() {
                return !isReadOnly();
            }
        };

        Node.Property genCenterProperty = new PropertySupport.ReadWrite(PROP_GENERATE_CENTER, Boolean.TYPE,
                                                                        FormEditor.getFormBundle().getString("MSG_GenerateCenter"),
                                                                        FormEditor.getFormBundle().getString("MSG_GenerateCenter")) {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return new Boolean(getGenerateCenter());
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Boolean)) throw new IllegalArgumentException();
                setGenerateCenter(((Boolean)val).booleanValue());
            }

            public boolean canWrite() {
                return !isReadOnly();
            }
        };

        Node.Property genEncodingProperty = new EncodingProperty("encoding", String.class, // NOI18N
                                                                 FormEditor.getFormBundle().getString("MSG_FormEncoding"),
                                                                 FormEditor.getFormBundle().getString("MSG_FormEncodingDesc")) {
            public void setValue(Object value) {
                if (!(value instanceof String)) {
                    throw new IllegalArgumentException();
                }
                //XXX getFormModel().setEncoding((String) value);
            }


            public Object getValue() {
                Object value = "UTF-8";//XXX getFormModel().getEncoding();
                if (value == null) {
                    value = ""; // NOI18N
                }
                return value;
            }

            public boolean canWrite() {
                return !isReadOnly();
            }
        };

        // the order of if's is important, JAppletFormInfo implements
        // JMenuBarContainer

        if (formInfo instanceof JAppletFormInfo) {
            return new Node.Property[] { createMenuProperty(),
                                         genEncodingProperty, };
        }
        else if (formInfo instanceof AppletFormInfo
                 || formInfo instanceof PanelFormInfo
                 || formInfo instanceof JPanelFormInfo) {
            return new Node.Property[] { genEncodingProperty, };
        }
        else if (formInfo instanceof JMenuBarContainer
                 || formInfo instanceof MenuBarContainer) {
            return new Node.Property[] { createMenuProperty(),
                                         sizeProperty,
                                         positionProperty,
                                         policyProperty,
                                         genPositionProperty,
                                         genSizeProperty,
                                         genCenterProperty,
                                         genEncodingProperty,
            };
        }
        else {
            return new Node.Property[] { sizeProperty,
                                         positionProperty,
                                         policyProperty,
                                         genPositionProperty,
                                         genSizeProperty,
                                         genCenterProperty,
                                         genEncodingProperty,
            };
        }
    }

    private Node.Property createMenuProperty() {
        return new PropertySupport.ReadWrite(PROP_MENU_BAR, String.class,
                                             FormEditor.getFormBundle().getString("MSG_MenuBar"),
                                             FormEditor.getFormBundle().getString("MSG_MenuBarDesc")) {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                String s = getFormMenu();
                return(s == null) ? NO_MENU : s;
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof String)) throw new IllegalArgumentException();
                String s =(String) val;
                setFormMenu(s.equals(NO_MENU) ? null : s);
                getFormModel().fireFormChanged();
            }

            public boolean canWrite() {
                return !isReadOnly();
            }

            /** Editor for alignment */
            public java.beans.PropertyEditor getPropertyEditor() {
                return new FormMenuEditor();
            }
        };
    }

    public ArrayList getAvailableMenus() {
        ArrayList list = new ArrayList();
        RADComponent[] comps = getFormModel().getNonVisualComponents();
        int size = comps.length;
        boolean swing =(formInfo instanceof JMenuBarContainer);

        for (int i = 0; i < size; i++) {
            if (comps[i] instanceof RADMenuComponent) {
                RADMenuComponent n =(RADMenuComponent) comps[i];
                if ((swing &&(n.getMenuItemType() == RADMenuComponent.T_JMENUBAR)) ||
                    (!swing &&(n.getMenuItemType() == RADMenuComponent.T_MENUBAR)))
                    list.add(n);
            }
        }
        return list;
    }

    // ------------------------------------------------------------------------------------------
    // Innerclasses

    final public static class SizePolicyEditor extends java.beans.PropertyEditorSupport {
        /** Display Names for alignment. */
        private static final String[] names = {
            FormEditor.getFormBundle().getString("VALUE_sizepolicy_full"),
            FormEditor.getFormBundle().getString("VALUE_sizepolicy_pack"),
            FormEditor.getFormBundle().getString("VALUE_sizepolicy_none"),
        };

        /** @return names of the possible directions */
        public String[] getTags() {
            return names;
        }

        /** @return text for the current value */
        public String getAsText() {
            int value =((Integer)getValue()).intValue();
            return names[value];
        }

        /** Setter.
         * @param str string equal to one value from directions array
         */
        public void setAsText(String str) {
            if (names[0].equals(str))
                setValue(new Integer(0));
            else if (names[1].equals(str))
                setValue(new Integer(1));
            else if (names[2].equals(str))
                setValue(new Integer(2));
        }
    }

    final public class FormMenuEditor extends java.beans.PropertyEditorSupport {

        /** @return names of the possible directions */
        public String[] getTags() {
            ArrayList list = getAvailableMenus();
            RADComponent[] comps = new RADComponent [list.size()];
            list.toArray(comps);
            String[] names = new String[comps.length + 1];
            names[0] = NO_MENU; // No Menu
            for (int i = 0; i < comps.length; i++) {
                names[i+1] = comps[i].getName();
            }
            return names;
        }

        /** @return text for the current value */
        public String getAsText() {
            return(String)getValue();
        }

        /** Setter.
         * @param str string equal to one value from directions array
         */
        public void setAsText(String str) {
            setValue(str);
        }
    }

    abstract class EncodingProperty extends PropertySupport.ReadWrite {
        EncodingProperty(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        /** Editor with list of encodings */
        public java.beans.PropertyEditor getPropertyEditor() {
            return new EncodingEditor();
        }

        class EncodingEditor extends java.beans.PropertyEditorSupport implements EnhancedPropertyEditor {
            /**
             * @return true if this PropertyEditor provides a enhanced in-place custom 
             *              property editor, false otherwise
             */
            public boolean hasInPlaceCustomEditor() {
                return true;
            }

            public void setAsText(String value) {
                String newValue =(String) encodingList.get(value);
                if (newValue == null)
                    newValue = value;

                // XXX(-tdt) test if the encoding is supported by the JDK

                try {
                    String x = new String(new byte[0], 0, 0, newValue);
                }
                catch (java.io.UnsupportedEncodingException ex) {
                    throw new IllegalArgumentException(
                        FormEditor.getFormBundle().getString("ERR_UnsupportedEncoding"));
                }

                this.setValue(newValue);
                if (!getFormModel().getFormDataObject().isModified()) {
                    getFormModel().getFormDataObject().setModified(true);
                }
            }
            public String getAsText() {
                return this.getValue().toString();
            }

            public java.awt.Component getInPlaceCustomEditor() {
                final JComboBox eventBox = new JComboBox();
                eventBox.setEditable(true);

                java.util.Iterator iter = encodingList.keySet().iterator();
                while (iter.hasNext())
                    eventBox.addItem(iter.next());

                eventBox.setSelectedItem(getAsText());
                eventBox.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        try {
                            setAsText((String) eventBox.getEditor().getItem());
                        }
                        catch (IllegalArgumentException ex) {
                            TopManager.getDefault().notify(new NotifyDescriptor.Message(
                                ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                        }
                    }
                }
                                           );
                return eventBox;
            }

            /**
             * @return true if this property editor provides tagged values and
             * a custom strings in the choice should be accepted too, false otherwise
             */
            public boolean supportsEditingTaggedValues() {
                return false;
            }
        }
    }
}
