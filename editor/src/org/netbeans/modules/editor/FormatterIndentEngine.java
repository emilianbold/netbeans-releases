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

package org.netbeans.modules.editor;

import java.io.Writer;
import javax.swing.text.Document;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Settings;
import org.openide.text.IndentEngine;
import org.openide.util.HelpCtx;

/**
* Indent engine that delegates to formatter
*
* @author Miloslav Metelka
*/

public abstract class FormatterIndentEngine extends IndentEngine {

    /** Formatter to delegate to. It's checked before use and if it's null
     * the createFormatter() is called to initialize it.
     */
    private transient Formatter formatter;

    private String[] acceptedMimeTypes;

    /** Get the formatter to which this indentation engine delegates. */
    public Formatter getFormatter() {
        checkFormatter();
        return formatter;
    }

    private void checkFormatter() {
        if (formatter == null) {
            formatter = createFormatter();
        }
    }

    public Object getSettingValue(String settingName) {
        return Settings.getValue(getFormatter().getKitClass(), settingName);
    }

    public void setSettingValue(String settingName, Object newValue) {
        Object oldValue = getSettingValue(settingName);
        if ((oldValue == null && newValue == null)
                || (oldValue != null && oldValue.equals(newValue))
           ) {
            return; // no change
        }

        Settings.setValue(getFormatter().getKitClass(), settingName, newValue);
        firePropertyChange(settingName, oldValue, newValue);
    }

    /** Create the formatter. */
    protected abstract Formatter createFormatter();

    public int indentLine(Document doc, int offset) {
        checkFormatter();
        return formatter.indentLine(doc, offset);
    }

    public int indentNewLine(Document doc, int offset) {
        checkFormatter();
        return formatter.indentNewLine(doc, offset);
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

    public void setAcceptedMimeTypes(String[] mimes) {
        this.acceptedMimeTypes = mimes;
    }

    public String[] getAcceptedMimeTypes() {
        return acceptedMimeTypes;
    }

    public Writer createWriter(Document doc, int offset, Writer writer) {
        checkFormatter();
        return formatter.createWriter(doc, offset, writer);
    }

    private void readObject(java.io.ObjectInputStream ois)
    throws java.io.IOException, ClassNotFoundException {
        ois.defaultReadObject();
    }

}

