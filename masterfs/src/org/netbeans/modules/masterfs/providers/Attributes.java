/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.masterfs.providers;

import org.netbeans.modules.masterfs.ExLocalFileSystem;
import org.openide.ErrorManager;
import org.openide.util.Utilities;
import org.openide.filesystems.AbstractFileSystem;
import org.openide.filesystems.DefaultAttributes;
import org.openide.filesystems.FileUtil;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Implementation of DefaultAttributes that should be shared by all
 * filesystems that are mounted into MasterFileSystem.
 * 
 * Ensures that .nbattrs file is stored in netbeans.user/var/cache/attribs/.nbattrs.
 * There exist just one file for all attributes. This implemenation is supposed 
 * to provide backward compatibility. 
 *  
 */
public class Attributes extends DefaultAttributes {
    public static String ATTRNAME = "attributes.xml";
    private static final String USERDIR = "netbeans.user";//NOI18N
    private static final String LOCATION = "var";//NOI18N

    private static DefaultAttributes sharedUserAttributes;

    private final String attributePrefix;
    private AbstractFileSystem.List list;
    private static final boolean BACKWARD_COMPATIBILITY = true;
    private static File rootForAttributes;


    public Attributes(File mountPoint, AbstractFileSystem.Info info, AbstractFileSystem.Change change, AbstractFileSystem.List list) {
        super(info, change, list);
        this.list = list;
        this.attributePrefix = preparePrefix(mountPoint);
    }

    private String preparePrefix(File fileSystemRoot) {
        fileSystemRoot = FileUtil.normalizeFile(fileSystemRoot);
        String rootPath = fileSystemRoot.getAbsolutePath().replace('\\', '/');
        return (Utilities.isWindows()) ? rootPath.toLowerCase() : rootPath;
    }

    public static File getRootForAttributes() {
        synchronized (ExLocalFileSystem.class) {
            if (rootForAttributes == null) {
                String userDir = System.getProperty(USERDIR);
                                 
                if (userDir != null) {
                    rootForAttributes = new File(userDir, LOCATION);
                } else {
                    rootForAttributes = new File(System.getProperty("java.io.tmpdir"));//NOI18N
                    File tmpAttrs = new File (rootForAttributes, ATTRNAME);
                    if (tmpAttrs.exists()) {
                        tmpAttrs.delete();   
                    }
                    tmpAttrs.deleteOnExit();
                }
                
                
                if (!rootForAttributes.exists()) {
                    rootForAttributes.mkdirs();
                }
            }
        }
        return rootForAttributes;
    }

    /** isn't filtered anymore as it was in DefaultAttributes*/
    public String[] children(String f) {
        return list.children(f);
    }

    /* Get the file attribute with the specified name.
    * @param name the file
    * @param attrName name of the attribute
    * @return appropriate (serializable) value or <CODE>null</CODE> if the attribute is unset (or could not be properly restored for some reason)
    */
    public Object readAttribute(String name, String attrName) {
        final String translatedName = translateName(name);
        Object retVal = getPreferedAttributes().readAttribute(translatedName, attrName);
        if (retVal == null && isBackwardCompatible()) {
            retVal = super.readAttribute(name, attrName);
            if (retVal != null) {
                copyAllToUserDir(name, super.attributes(name));
                retVal = getPreferedAttributes().readAttribute(translatedName, attrName);
            }
        }
        return retVal;
    }

    /* Set the file attribute with the specified name.
    * @param name the file
    * @param attrName name of the attribute
    * @param value new value or <code>null</code> to clear the attribute. Must be serializable, although particular filesystems may or may not use serialization to store attribute values.
    * @exception IOException if the attribute cannot be set. If serialization is used to store it, this may in fact be a subclass such as {@link NotSerializableException}.
    */
    public void writeAttribute(String name, String attrName, Object value)
            throws IOException {
        getPreferedAttributes().writeAttribute(translateName(name), attrName, value);
    }

    /* Get all file attribute names for the file.
    * @param name the file
    * @return enumeration of keys (as strings)
    */
    public synchronized Enumeration attributes(String name) {
        Enumeration retVal = getPreferedAttributes().attributes(translateName(name));
        if ((retVal == null || !retVal.hasMoreElements()) && isBackwardCompatible()) {
            retVal = copyAllToUserDir(name, super.attributes(name));
        }
        return retVal;
    }

    private Enumeration copyAllToUserDir(String name, Enumeration attributeNames) {
        
        if (attributeNames != null && attributeNames.hasMoreElements() && isBackwardCompatible()) {
            final String translatedName = translateName(name);
            
            while (attributeNames.hasMoreElements()) {
                String attrName = (String) attributeNames.nextElement();
                Object value = super.readAttribute(name, attrName);
                try {
                    getPreferedAttributes().writeAttribute(translatedName, attrName, value);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
            super.deleteAttributes(name);
            attributeNames = getPreferedAttributes().attributes(translatedName);
        }
        return attributeNames;
    }

    /* Called when a file is renamed, to appropriatelly update its attributes.
    * <p>
    * @param oldName old name of the file
    * @param newName new name of the file
    */
    public synchronized void renameAttributes(String oldName, String newName) {
        if (isBackwardCompatible()) copyAllToUserDir(oldName, super.attributes(oldName));
        getPreferedAttributes().renameAttributes(translateName(oldName), translateName(newName));
    }

    /* Called when a file is deleted to also delete its attributes.
    *
    * @param name name of the file
    */
    public synchronized void deleteAttributes(String name) {
        if (isBackwardCompatible()) super.deleteAttributes(name);
        getPreferedAttributes().deleteAttributes(translateName(name));
    }

    /** adds prefix: systemName of FileSystem */
    private String translateName(String name) {
        return (attributePrefix.endsWith("/"))? attributePrefix+"/"+name: attributePrefix+name; // NOI18N
    }
    
    private DefaultAttributes getPreferedAttributes() {
        synchronized (Attributes.class) {
            if (sharedUserAttributes == null) {
                ExLocalFileSystem exLFs = null;
                try {
                    exLFs = ExLocalFileSystem.getInstance(getRootForAttributes());                    
                } catch (PropertyVetoException e) {
                    ErrorManager.getDefault().notify(e);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
                sharedUserAttributes = exLFs.getAttributes();
            }
        }

        assert sharedUserAttributes != null;
        return (sharedUserAttributes != null) ? sharedUserAttributes : this;
    }

    private boolean isBackwardCompatible() {
        return BACKWARD_COMPATIBILITY && (getPreferedAttributes() != this);
    }
}
