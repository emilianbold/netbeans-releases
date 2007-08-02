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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor;

import java.io.*;
import javax.swing.text.Document;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.SettingsDefaults;
import org.openide.text.IndentEngine;

/**
* Indent engine that delegates to formatter
*
* @author Miloslav Metelka
*/

public abstract class FormatterIndentEngine extends IndentEngine {

    public static final String EXPAND_TABS_PROP = "expandTabs"; //NOI18N

    public static final String SPACES_PER_TAB_PROP = "spacesPerTab"; //NOI18N

    static final long serialVersionUID = -3408217516931076216L;

    /** Formatter to delegate to. It's checked before use and if it's null
     * the createFormatter() is called to initialize it.
     */
    private transient ExtFormatter formatter;

    private String[] acceptedMimeTypes;

    /** Get the formatter to which this indentation engine delegates. */
    public ExtFormatter getFormatter() {
        if (formatter == null) {
            formatter = createFormatter();
            // Fallback if no formatter is registered (can happen with new formatting api)
            if (formatter == null) {
                formatter = new ExtFormatter(BaseKit.class);
            }
        }
        return formatter;
    }

    /** Create the formatter. */
    protected abstract ExtFormatter createFormatter();

    public Object getValue(String settingName) {
        return getFormatter().getSettingValue(settingName);
    }

    public void setValue(String settingName, Object newValue, String propertyName) {
        Object oldValue = getValue(settingName);
        if ((oldValue == null && newValue == null)
                || (oldValue != null && oldValue.equals(newValue))
           ) {
            return; // no change
        }

        getFormatter().setSettingValue(settingName, newValue);
        
        if (propertyName != null){
            firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * @deprecated use {@link #setValue(java.lang.String, java.lang.Object, java.lang.String)} instead 
     * with properly specified propertyName
     */
    public void setValue(String settingName, Object newValue) {
        setValue(settingName, newValue, null);
    }
    
    
    public int indentLine(Document doc, int offset) {
        return getFormatter().indentLine(doc, offset);
    }

    public int indentNewLine(Document doc, int offset) {
        return getFormatter().indentNewLine(doc, offset);
    }

    public Writer createWriter(Document doc, int offset, Writer writer) {
        return getFormatter().createWriter(doc, offset, writer);
    }

    protected boolean acceptMimeType(String mimeType) {
        if (acceptedMimeTypes != null) {
            for (int i = acceptedMimeTypes.length - 1; i >= 0; i--) {
                if (acceptedMimeTypes[i].equals(mimeType)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isExpandTabs() {
        return getFormatter().expandTabs();
    }

    public void setExpandTabs(boolean expandTabs) {
        boolean old = getFormatter().expandTabs();
        // Must call setter because of turning into custom property
        getFormatter().setExpandTabs(expandTabs);
        if (old != expandTabs) {
            setValue(SettingsNames.EXPAND_TABS,
                Boolean.valueOf(expandTabs), EXPAND_TABS_PROP);
            
            firePropertyChange(EXPAND_TABS_PROP,
                old ? Boolean.TRUE : Boolean.FALSE,
                expandTabs ? Boolean.TRUE : Boolean.FALSE
            );
        }
        
    }

    public int getSpacesPerTab() {
        return getFormatter().getSpacesPerTab();
    }

    public void setSpacesPerTab(int spacesPerTab) {
        if (spacesPerTab <= 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return; // value unchanged
        }

        int old = getFormatter().getSpacesPerTab();
        getFormatter().setSpacesPerTab(spacesPerTab);
        if (old != spacesPerTab) {
            setValue(SettingsNames.SPACES_PER_TAB,
                new Integer(spacesPerTab), SPACES_PER_TAB_PROP);
            
            firePropertyChange(SPACES_PER_TAB_PROP,
                new Integer(old),
                new Integer(spacesPerTab)
            );
        }
    }

    public void setAcceptedMimeTypes(String[] mimes) {
        this.acceptedMimeTypes = mimes;
    }

    public String[] getAcceptedMimeTypes() {
        return acceptedMimeTypes;
    }
    
    

    // Serialization ------------------------------------------------------------

    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField(EXPAND_TABS_PROP, Boolean.TYPE),
        new ObjectStreamField(SPACES_PER_TAB_PROP, Integer.TYPE)
    };
    
    private void readObject(java.io.ObjectInputStream ois)
    throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = ois.readFields();
        setExpandTabs(fields.get(EXPAND_TABS_PROP, SettingsDefaults.defaultExpandTabs.booleanValue()));
        setSpacesPerTab(fields.get(SPACES_PER_TAB_PROP, SettingsDefaults.defaultSpacesPerTab.intValue()));
    }

    private void writeObject(java.io.ObjectOutputStream oos)
    throws IOException, ClassNotFoundException {
        ObjectOutputStream.PutField fields = oos.putFields();
        fields.put(EXPAND_TABS_PROP, isExpandTabs());
        fields.put(SPACES_PER_TAB_PROP, getSpacesPerTab());
        oos.writeFields();
    }

}

