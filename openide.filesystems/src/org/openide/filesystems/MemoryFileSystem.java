/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.filesystems;

import org.openide.filesystems.*;
import org.openide.filesystems.AbstractFileSystem.*;

import java.beans.*;

import java.io.*;

import java.util.*;


/**
 * Simple implementation of memory file system.
 * @author Jaroslav Tulach
 */
final class MemoryFileSystem extends AbstractFileSystem implements Info, Change, AbstractFileSystem.List, Attr {
    /** time when the filesystem was created. It is supposed to be the default
     * time of modification for all resources that has not been modified yet
     */
    private java.util.Date created = new java.util.Date();

    /** maps String to Entry */
    private Hashtable entries = new Hashtable();

    /** Creates new MemoryFS */
    public MemoryFileSystem() {
        attr = this;
        list = this;
        change = this;
        info = this;

        try {
            setSystemName("MemoryFileSystem");
        } catch (PropertyVetoException ex) {
            ex.printStackTrace();
        }
    }

    /** Creates MemoryFS with data */
    public MemoryFileSystem(String[] resources) {
        this();

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < resources.length; i++) {
            sb.append(resources[i]);

            if (resources[i].endsWith("/")) {
                // folder
                e(resources[i]).data = null;
            } else {
                e(resources[i]).data = new byte[0];
            }
        }
    }

    /** finds entry for given name */
    private Entry e(String n) {
        if ((n.length() > 0) && (n.charAt(0) == '/')) {
            n = n.substring(1);
        }

        Entry x = (Entry) entries.get(n);

        if (x == null) {
            x = new Entry();
            entries.put(n, x);
        }

        return x;
    }

    /** finds whether there already is this name */
    private boolean is(String n) {
        if ((n.length() > 0) && (n.charAt(0) == '/')) {
            n = n.substring(1);
        }

        Entry x = (Entry) entries.get(n);

        return x != null;
    }

    public String getDisplayName() {
        return "MemoryFileSystem";
    }

    public boolean isReadOnly() {
        return false;
    }

    public Enumeration attributes(String name) {
        return is(name) ? Collections.enumeration(e(name).attrs.keySet()) : org.openide.util.Enumerations.empty();
    }

    public String[] children(String f) {
        if ((f.length() > 0) && (f.charAt(0) == '/')) {
            f = f.substring(1);
        }

        if ((f.length() > 0) && !f.endsWith("/")) {
            f = f + "/";
        }

        HashSet l = new HashSet();

        //System.out.println("Folder: " + f);
        Iterator it = entries.keySet().iterator();

        while (it.hasNext()) {
            String name = (String) it.next();

            if (name.startsWith(f) || (f.trim().length() == 0)) {
                int i = name.indexOf('/', f.length());
                String child = null;

                if (i > 0) {
                    child = name.substring(f.length(), i);
                } else {
                    child = name.substring(f.length());
                }

                if (child.trim().length() > 0) {
                    l.add(child);
                }
            }
        }

        return (String[]) l.toArray(new String[0]);
    }

    public void createData(String name) throws IOException {
        if (is(name)) {
            throw new IOException("File already exists");
        }

        e(name).data = new byte[0];
    }

    public void createFolder(String name) throws java.io.IOException {
        if (is(name)) {
            throw new IOException("File already exists");
        }

        e(name).data = null;
    }

    public void delete(String name) throws IOException {
        if (entries.remove(name) == null) {
            throw new IOException("No file to delete: " + name); // NOI18N
        }
    }

    public void deleteAttributes(String name) {
    }

    public boolean folder(String name) {
        return e(name).data == null;
    }

    public InputStream inputStream(String name) throws java.io.FileNotFoundException {
        byte[] arr = e(name).data;

        if (arr == null) {
            arr = new byte[0];
        }

        return new ByteArrayInputStream(arr);
    }

    public java.util.Date lastModified(String name) {
        java.util.Date d = e(name).last;

        return (d == null) ? created : d;
    }

    public void lock(String name) throws IOException {
    }

    public void markUnimportant(String name) {
    }

    public String mimeType(String name) {
        return (String) e(name).attrs.get("mimeType");
    }

    public OutputStream outputStream(final String name)
    throws java.io.IOException {
        class Out extends ByteArrayOutputStream {
            public void close() throws IOException {
                super.close();

                e(name).data = toByteArray();
                e(name).last = new Date();
            }
        }

        return new Out();
    }

    public Object readAttribute(String name, String attrName) {
        return is(name) ? e(name).attrs.get(attrName) : null;
    }

    public boolean readOnly(String name) {
        return false;
    }

    public void rename(String oldName, String newName)
    throws IOException {
        if (!is(oldName)) {
            throw new IOException("The file to rename does not exist.");
        }

        if (is(newName)) {
            throw new IOException("Cannot rename to existing file");
        }

        if ((newName.length() > 0) && (newName.charAt(0) == '/')) {
            newName = newName.substring(1);
        }

        Entry e = e(oldName);
        entries.remove(oldName);
        entries.put(newName, e);
    }

    public void renameAttributes(String oldName, String newName) {
    }

    public long size(String name) {
        byte[] d = e(name).data;

        return (d == null) ? 0 : d.length;
    }

    public void unlock(String name) {
    }

    public void writeAttribute(String name, String attrName, Object value)
    throws IOException {
        e(name).attrs.put(attrName, value);
    }

    static final class Entry {
        /** String, Object */
        public HashMap attrs = new HashMap();
        public byte[] data;
        public java.util.Date last;
    }
}
