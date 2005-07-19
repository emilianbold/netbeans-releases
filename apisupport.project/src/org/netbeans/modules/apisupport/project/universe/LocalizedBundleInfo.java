/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Represents localized information for a NetBeans module usually loaded from a
 * <em>Bundle.properties</em> specified in a module's manifest. It is actaully
 * back up by {@link EditableProperties} so any changes to this instance will
 * behave exactly as specified in {@link EditableProperties} javadoc during
 * storing.
 *
 * @author Martin Krauskopf
 */
public final class LocalizedBundleInfo {
    
    public static final String NAME = "OpenIDE-Module-Name"; // NOI18N
    public static final String DISPLAY_CATEGORY = "OpenIDE-Module-Display-Category"; // NOI18N
    public static final String SHORT_DESCRIPTION = "OpenIDE-Module-Short-Description"; // NOI18N
    public static final String LONG_DESCRIPTION = "OpenIDE-Module-Long-Description"; // NOI18N
    
    static final LocalizedBundleInfo EMPTY = new LocalizedBundleInfo(new EditableProperties(true));
    
    private EditableProperties props;
    private String path;
    
    /** Simple factory method. */
    public static LocalizedBundleInfo load(FileObject bundleFO) throws IOException {
        if (bundleFO == null) {
            return null;
        }
        InputStream bundleIS = bundleFO.getInputStream();
        try {
            LocalizedBundleInfo info = load(bundleIS);
            File f = FileUtil.toFile(bundleFO);
            if (f != null) {
                info.setPath(f.getAbsolutePath());
            }
            return info;
        } finally {
            bundleIS.close();
        }
    }
    
    /** Simple factory method. */
    public static LocalizedBundleInfo load(InputStream bundleIS) throws IOException {
        EditableProperties props = new EditableProperties();
        props.load(bundleIS);
        return new LocalizedBundleInfo(props);
    }
    
    /** Use factory method instead. */
    private LocalizedBundleInfo(EditableProperties props) {
        this.props = props;
    }
    
    public void store() throws IOException {
        if (getPath() == null) {
            throw new IllegalStateException("First you must call " // NOI18N
                    + getClass().getName() + ".setPath()"); // NOI18N
        }
        FileObject bundleFO = FileUtil.toFileObject(new File(getPath()));
        FileLock lock = bundleFO.lock();
        try {
            OutputStream os = bundleFO.getOutputStream(lock);
            try {
                props.store(os);
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    /**
     * Converts entries this instance represents into {@link
     * EditableProperties}.
     */
    public EditableProperties toEditableProperties() {
        return props;
    }
    
    public String getDisplayName() {
        return props.getProperty(NAME);
    }
    
    public void setDisplayName(String name) {
        this.setProperty(NAME, name, false);
    }
    
    public String getCategory() {
        return props.getProperty(DISPLAY_CATEGORY);
    }
    
    public void setCategory(String category) {
        this.setProperty(DISPLAY_CATEGORY, category, false);
    }
    
    public String getShortDescription() {
        return props.getProperty(SHORT_DESCRIPTION);
    }
    
    public void setShortDescription(String shortDescription) {
        this.setProperty(SHORT_DESCRIPTION, shortDescription, false);
    }
    
    public String getLongDescription() {
        return props.getProperty(LONG_DESCRIPTION);
    }
    
    public void setLongDescription(String longDescription) {
        this.setProperty(LONG_DESCRIPTION, longDescription, true);
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    private void setProperty(String name, String value, boolean split) {
        if (Utilities.compareObjects(value, props.getProperty(name))) {
            return;
        }
        if (value != null) {
            value = value.trim();
        }
        if (value != null && value.length() > 0) {
            if (split) {
                props.setProperty(name, splitBySentence(value));
            } else {
                props.setProperty(name, value);
            }
        } else {
            props.remove(name);
        }
    }
    
    private static String[] splitBySentence(String text) {
        List/*<String>*/ sentences = new ArrayList();
        // Use Locale.US since the customizer is setting the default (US) locale text only:
        BreakIterator it = BreakIterator.getSentenceInstance(Locale.US);
        it.setText(text);
        int start = it.first();
        int end;
        while ((end = it.next()) != BreakIterator.DONE) {
            sentences.add(text.substring(start, end));
            start = end;
        }
        return (String[]) sentences.toArray(new String[sentences.size()]);
    }

    public String toString() {
        return "LocalizedBundleInfo[" + getDisplayName() + "; " + // NOI18N
            getCategory() + "; " + // NOI18N
            getShortDescription() + "; " + // NOI18N
            getLongDescription() + "]"; // NOI18N
    }
}
