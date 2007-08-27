/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.loaders;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.text.DateFormat;
import java.util.Enumeration;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.FileEntry;
import org.openide.loaders.UniFileLoader;
import org.openide.modules.InstalledFileLocator;

import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.util.Utilities;

/**
 * DataLoader for recognising C/C++/Fortran (C-C-F) source files.
 * 
 * It also defines an innerclass, CndFormat, whose derived classes are
 * used to format template files (e.g. substitute values for parameters such as
 * __FILENAME__, __NAME__, __DATE__, __TIME__, __USER__, __GUARD_NAME etc.).
 */
public abstract class CndAbstractDataLoader extends UniFileLoader {

    /** Serial version number */
    static final long serialVersionUID = 6801389470714975682L;
    protected static final boolean CASE_INSENSITIVE =
            (Utilities.isWindows () || (Utilities.getOperatingSystem () == Utilities.OS_OS2)) || Utilities.getOperatingSystem() == Utilities.OS_VMS;

    protected CndAbstractDataLoader(String representationClassName) {
	super(representationClassName);
    }

    protected final void createExtentions(String [] extensions) {
	ExtensionList extensionList = new ExtensionList();
	for (int i = 0; i < extensions.length; i++) {
	    extensionList.addExtension(extensions[i]);
	}
	setExtensions(extensionList);
    }

    protected abstract String getMimeType();

    protected boolean resolveMimeType(String ext){
        ExtensionList extensions = getExtensions();
        for (Enumeration e = extensions.extensions(); e != null &&  e.hasMoreElements();) {
            String ex = (String) e.nextElement();
            if (ex != null && (!CASE_INSENSITIVE && ex.equals(ext) || CASE_INSENSITIVE && ex.equalsIgnoreCase(ext))) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    protected String actionsContext () {
        return "Loaders/text/x-cnd-sourcefile/Actions/"; // NOI18N
    }
    
    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
	// Never recognize folders...
	if (fo.isFolder()) {
	    return null;
	}
        
        String ext = fo.getExt();
        if (ext != null && ext.length() > 0) {
            Enumeration e = getExtensions().extensions();
            while (e.hasMoreElements()) {
                String ex = (String) e.nextElement();
                if (!CASE_INSENSITIVE && ext.equals(ex) || CASE_INSENSITIVE && ext.equalsIgnoreCase(ex)) {
                    return fo;
                }
            }
        }
        
	return findSecondaryFile(fo);
    }

    protected FileObject findSecondaryFile(FileObject fo){
        FileObject fb = null;
            
      	// Check for the secondary extension
	if (fo.hasExt("o")) { // NOI18N
            Enumeration e = getExtensions().extensions();
            while (e.hasMoreElements()) {
                String ex = (String) e.nextElement();
                fb = FileUtil.findBrother(fo, ex);
                if (fb != null) {
                    break;
		}
            }
	}
        return fb;
    }
 
    @Override
    protected MultiDataObject.Entry createPrimaryEntry(
			    MultiDataObject obj,
			    FileObject primaryFile) {
	// Entry for the important file: by default, is preserved
	// during all operations.
	return new CndFormat(obj, primaryFile);
    }
    
    @Override
    protected MultiDataObject.Entry createSecondaryEntry(
					MultiDataObject obj,
					FileObject secondaryFile) {
	return new FileEntry.Numb(obj, secondaryFile);
    }

    // Inner class: Substitute important template parameters...
    public static class CndFormat extends FileEntry.Format {
        
	public CndFormat(MultiDataObject obj, FileObject primaryFile) {
	    super(obj, primaryFile);
	}
	protected java.text.Format createFormat(FileObject target, String name, String ext) {
	    
	    Map map = (CppSettings.findObject(CppSettings.class, true)).getReplaceableStringsProps();
	    
	    String packageName = target.getPath().replace('/', '_');
            // add an underscore to the package name if it is not an empty string
	    if (!packageName.equals(""))  // NOI18N
		packageName = packageName + "_"; // NOI18N
            
	    map.put("PACKAGE_AND_NAME", packageName+name);  // NOI18N
	    map.put("NAME", name);	// NOI18N
	    map.put("EXTENSION", ext); // NOI18N
            String guardName = name.replace('-', '_').replace('.', '_'); // NOI18N
            map.put("GUARD_NAME", guardName.toUpperCase()); // NOI18N
	    /*
	      This is a ugly hack but I don't have a choice. That's because
	      NetBeans will not pass me the name the user typed in as the
	      "root" name; instead I get the substituted name for each
	      template file. In other words, suppose I use the parameter
	      __NAME__ in my template source files. The name passed to
	      createFormat is then the *filename* instead of just the
	      Name: field the user had entered. e.g. if I'm instantiating the
	      following files:
	        __sample___foo.cc
	        __sample___bar.cc
	      Then for the first file, __NAME__ becomes <myname>_foo and in
	      the second file, __NAME__ becomes <myname>_bar. But I really
	      need the Name itself, so that I can for example have have
                 #include "<myname>_externs.h"
	      in the templates!
	    */
	    
	    int crop = (name.lastIndexOf('_'));
	    if (crop != -1) {
		name = name.substring(0, crop);
	    }
	    map.put("CROPPEDNAME", name);  // NOI18N
	    map.put("DATE", DateFormat.getDateInstance	// NOI18N
		     (DateFormat.LONG).format(new Date()));
	    map.put("TIME", DateFormat.getTimeInstance	// NOI18N
		     (DateFormat.SHORT).format(new Date()));
	    //	    map.put("USER", System.getProperty("user.name"));	// NOI18N
            String nbHome = null;  //System.getProperty("netbeans.home");
            File file = InstalledFileLocator.getDefault().locate("lib", null, false); // NOI18N
            if (file != null){
                nbHome = file.getParent();
            }
            if (nbHome == null) {
                nbHome = "";
            }
	    map.put("NBDIR", nbHome); // NOI18N
            
	    map.put("QUOTES","\""); // NOI18N
 	  
	    org.openide.util.MapFormat format =
		new org.openide.util.MapFormat(map);
	    
	    // Use "%<%" and "%>%" instead of "__" (which most other templates
	    // use) since "__" is used for many C++ tokens and we don't want
	    // any conflicts with valid code. For example, __FILE__ is a valid
	    // construct in Sun C++ files and the compiler will replace the
	    // current file name during compilation.
	    format.setLeftBrace("%<%");	// NOI18N
	    format.setRightBrace("%>%");    // NOI18N
	    return format;
	}
    }
}
