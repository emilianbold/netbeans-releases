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
package org.netbeans.modules.gsf;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.gsfpath.classpath.ClassPath;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;


/**
 * Loader for recognizing languages handled by the generic scripting framework
 *
 * @author Tor Norbye
 */
public class GsfDataLoader extends UniFileLoader {
    boolean initialized;

    public GsfDataLoader() {
        super("org.netbeans.modules.gsf.GsfDataObject");
    }

    @Override
    protected void initialize() {
        super.initialize();

        ExtensionList list = getExtensions();

        for (Language language : LanguageRegistry.getInstance()) {
            list.addMimeType(language.getMimeType());

            String[] extensions = language.getExtensions();

            for (int i = 0; i < extensions.length; i++) {
                assert extensions[i].charAt(0) != '.' : "Extensions should exclude the .";
                list.addExtension(extensions[i]);
            }
        }

        initialized = true;
    }

    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile)
        throws DataObjectExistsException, IOException {
        Language language =
            LanguageRegistry.getInstance().getLanguageByMimeType(primaryFile.getMIMEType());

        return new GsfDataObject(primaryFile, this, language);
    }

    @Override
    protected String defaultDisplayName() {
        // Create a list of languages to include in the display
        StringBuilder sb = new StringBuilder();

        for (Language language : LanguageRegistry.getInstance()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }

            sb.append(language.getDisplayName());
        }

        return NbBundle.getMessage(GsfDataLoader.class, "GenericLoaderName", sb.toString());
    }

    @Override
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
        FileEntry.Format entry = new FileEntry.Format(obj, primaryFile) {
            protected java.text.Format createFormat (FileObject target, String n, String e) {
                ClassPath cp = ClassPath.getClassPath(target, ClassPath.SOURCE);
                String resourcePath = "";
                if (cp != null) {
                    resourcePath = cp.getResourceName(target);
                } else {
                    ErrorManager.getDefault().log(ErrorManager.WARNING, "No classpath was found for folder: "+target);
                }
                Map<String,String> m = new HashMap<String,String>();
                m.put("NAME", n ); //NOI18N
                String capitalizedName;
                if (n.length() > 1) {
                    capitalizedName = Character.toUpperCase(n.charAt(0))+n.substring(1);
                } else {
                    capitalizedName = ""+Character.toUpperCase(n.charAt(0));
                }
                m.put("CAPITALIZEDNAME", capitalizedName); //NOI18N
                m.put("LOWERNAME", n.toLowerCase()); //NOI18N
                m.put("UPPERNAME", n.toUpperCase()); //NOI18N

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < n.length(); i++) {
                    char c = n.charAt(i);
                    if (Character.isJavaIdentifierPart(c)) {
                        sb.append(c);
                    }
                }
                String identifier = sb.toString();
                m.put("IDENTIFIER", identifier); // NOI18N
                sb.setCharAt(0, Character.toUpperCase(identifier.charAt(0)));
                m.put("CAPITALIZEDIDENTIFIER", sb.toString()); // NOI18N
                m.put("LOWERIDENTIFIER", identifier.toLowerCase()); //NOI18N
                
                // Yes, this is package sans filename (target is a folder).
                String packageName = resourcePath.replace('/', '.');
                m.put("PACKAGE", packageName); // NOI18N
                String capitalizedPkgName;
                if (packageName == null || packageName.length() == 0) {
                    packageName = "";
                    capitalizedPkgName = "";
                } else if (packageName.length() > 1) {
                    capitalizedPkgName = Character.toUpperCase(packageName.charAt(0))+packageName.substring(1);
                } else {
                    capitalizedPkgName = ""+Character.toUpperCase(packageName.charAt(0));
                }
                m.put("CAPITALIZEDPACKAGE", capitalizedPkgName); // NOI18N
                m.put("PACKAGE_SLASHES", resourcePath); // NOI18N
                // Fully-qualified name:
                if (target.isRoot ()) {
                    m.put ("PACKAGE_AND_NAME", n); // NOI18N
                    m.put ("PACKAGE_AND_NAME_SLASHES", n); // NOI18N
                } else {
                    m.put ("PACKAGE_AND_NAME", resourcePath.replace('/', '.') + '.' + n); // NOI18N
                    m.put ("PACKAGE_AND_NAME_SLASHES", resourcePath + '/' + n); // NOI18N
                }
                m.put("DATE", DateFormat.getDateInstance(DateFormat.LONG).format(new Date())); // NOI18N
                m.put("TIME", DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date())); // NOI18N
                MapFormat f = new MapFormat(m);
                f.setLeftBrace( "__" ); //NOI18N
                f.setRightBrace( "__" ); //NOI18N

                return f;
            }
        };
        return entry;
    }
}
