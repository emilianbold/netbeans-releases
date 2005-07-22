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
    
    /**
     * Returns instances initialized by data in the given {@link FileObject}.
     * Note that instances created by this factory method are automatically
     * storable (i.e. {@link #store} and {@link #reload} can be called) if the
     * given object represents a regular {@link File}.
     * @param bundleFO {@link FileObject} representing localizing bundle.
     *        Usually <em>bundle.properties</em> or its branded version.
     * @return instance representing data in the given bundle
     */
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
    
    /**
     * Returns instances initialized by data in the given {@link FileObject}.
     * Instances created by this factory method are not storable (i.e. {@link
     * #store} and {@link #reload} cannot be called) until the {@link #setPath}
     * is called upon this object.
     * @param bundleIS input stream representing localizing bundle. Usually
     *        <em>bundle.properties</em> or its branded version.
     * @return instance representing data in the given bundle
     */
    public static LocalizedBundleInfo load(InputStream bundleIS) throws IOException {
        EditableProperties props = new EditableProperties();
        props.load(bundleIS);
        return new LocalizedBundleInfo(props);
    }
    
    /** Use factory method instead. */
    private LocalizedBundleInfo(EditableProperties props) {
        this.props = props;
    }
    
    /**
     * Reload data of this localizing bundle info from the file represented by
     * previously set path. If the {@link #setPath} hasn't been called before
     * an {@link IllegalStateException} will be thrown.
     */
    public void reload() throws IOException {
        if (getPath() == null) {
            throw new IllegalStateException("First you must call " // NOI18N
                    + getClass().getName() + ".setPath()"); // NOI18N
        }
        FileObject bundleFO = FileUtil.toFileObject(new File(getPath()));
        InputStream bundleIS = bundleFO.getInputStream();
        try {
            EditableProperties ep = new EditableProperties();
            ep.load(bundleIS);
            this.props = ep;
        } finally {
            bundleIS.close();
        }
    }
    
    /**
     * Reload this localizing bundle from the file specified by previously set
     * path. If the {@link #setPath} hasn't been called before an {@link
     * IllegalStateException} will be thrown.
     */
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
    
    /**
     * After calling this methods instance become <em>storable</em>. So methods
     * {@link #store} and {@link #reload} can be called.
     */
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
