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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.languages.features;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.Language;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.languages.features.DatabaseContext;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;


/**
 *
 * @author Jan Jancura
 */
public class Index {
    
    private static Map<Project,ProjectCache> projectToCache = new WeakHashMap<Project,ProjectCache> ();

    
    public static Map<FileObject,List<DatabaseDefinition>> getGlobalItems (
        FileObject fo, 
        boolean parse
    ) throws FileNotParsedException {
        Map<FileObject,List<DatabaseDefinition>> result = new HashMap<FileObject,List<DatabaseDefinition>> ();
        Project project = FileOwnerQuery.getOwner (fo);
        if (project == null) return result;
        getProjectCache (project, parse).add (result, null);
        return result;
    }
    
    public static Map<FileObject,List<DatabaseDefinition>> getGlobalItem (
        FileObject  fo,
        String      name,
        boolean     parse
    ) throws FileNotParsedException {
        Map<FileObject,List<DatabaseDefinition>> result = new HashMap<FileObject,List<DatabaseDefinition>> ();
        Project project = FileOwnerQuery.getOwner (fo);
        if (project == null) return result;
        getProjectCache (project, parse).add (result, name);
        return result;
    }
    
    private static ProjectCache getProjectCache (
        Project project, 
        boolean parse
    ) throws FileNotParsedException {
        ProjectCache cache = projectToCache.get (project);
        if (cache == null)
            cache = readProjectCache (project);
        if (cache == null) {
            if (!parse) throw new FileNotParsedException ();
            cache = new ProjectCache (project);
            projectToCache.put (project, cache);
        }
        return cache;
    }
    
    private static ProjectCache readProjectCache (Project project) {
        try {
            File f = getProjectCacheFile (project);
            if (f == null) return null;
            return ProjectCache.load (project, f);
        } catch (IOException e) {
            e.printStackTrace ();
            return null;
        }
    }
    
    private static List<FileObject> roots;
    
    private static File getProjectCacheFile (Project project) {
        FileObject projectDir = project.getProjectDirectory ();
        File cacheFolder = getCacheFolder ();
        if (roots != null) {
            int i = roots.indexOf (projectDir);
            if (i < 0) return null;
            return new File (cacheFolder, "s" + (i + 1));
        }
        roots = new ArrayList<FileObject> ();
        File segments = new File (cacheFolder, "segments");
        if (!segments.exists ()) return null;
        try {
            BufferedReader reader = new BufferedReader (new FileReader (segments));
            try {
                File result = null;
                int i = 1;
                String path = reader.readLine ();
                while (path != null) {
                    File file = new File (path);
                    file = FileUtil.normalizeFile (file);
                    FileObject fo = FileUtil.toFileObject (file);
                    if (fo == null) {
                        System.out.println (Index.class.getName () + " File not found: " + file);
                    } else {
                        roots.add (fo);
                        if (fo.equals (projectDir))
                            result = new File (cacheFolder, "s" + i);
                    }
                    path = reader.readLine ();
                    i++;
                }
                return result;
            } finally {
                reader.close ();
            }
        } catch (IOException ex) {
            ex.printStackTrace ();
            return null;
        }
    }
    
    public static void save () throws IOException {
        File dir = getCacheFolder ();
        File segments = new File (dir, "segments");
        BufferedWriter writer = new BufferedWriter (new FileWriter (segments));
        try {
            int i = 1;
            Iterator<Project> it = projectToCache.keySet ().iterator ();
            while (it.hasNext ()) {
                Project p = it.next ();
                ProjectCache cache = projectToCache.get (p);
                File s = new File (dir, "s" + i);
                cache.save (s);
                writer.write (p.getProjectDirectory ().getPath ());
                writer.newLine ();
            }
        } finally {
            writer.close ();
        }
    }

    private static File cacheFolder;
    
    private static synchronized File getCacheFolder () {
        if (cacheFolder == null) {
            String userDir = System.getProperty ("netbeans.user");
            assert userDir != null;
            String cacheDir = userDir + File.separatorChar + 
                "var" + File.separatorChar + "cache" + File.separatorChar + 
                "sindex" + File.separatorChar + "1.0";
            cacheFolder = FileUtil.normalizeFile (new File (cacheDir));
            if (!cacheFolder.exists ()) {
                boolean created = cacheFolder.mkdirs ();                
                assert created : "Cannot create cache folder";  //NOI18N
            } else
                assert cacheFolder.isDirectory () && cacheFolder.canRead () && cacheFolder.canWrite ();
        }
        return cacheFolder;
    }

    
    // innerclasses ............................................................
    
    private static class ProjectCache {
        
        private FileObject                  root;
        private Map<FileObject,FileCache>   cache;
        
        ProjectCache (Project project) {
            root = project.getProjectDirectory ();
        }
        
        ProjectCache (Project project, Map<FileObject,FileCache> cache) {
            root = project.getProjectDirectory ();
            this.cache = cache;
        }

        private void add (
            Map<FileObject,List<DatabaseDefinition>>    result,
            String                                      name
        ) {
            if (cache == null) {
                cache = new HashMap<FileObject,FileCache> ();
                init (root);
            }
            Iterator<FileCache> it = cache.values ().iterator ();
            while (it.hasNext ()) {
                FileCache fileCache =  it.next();
                fileCache.add (result, name);
            }
        }

        private void init (FileObject root) {
            FileObject[] ch = root.getChildren ();
            int i, k = ch.length;
            for (i = 0; i < k; i++) {
                FileObject fo = ch[i];
                if (fo.isFolder ()) {
                    init (fo);
                    continue;
                }
                if (!"js".equals (fo.getExt ()))
                    continue;
                FileCache fc = new FileCache (fo);
                cache.put (fo, fc);
            }
        }
        
       private static ProjectCache load (Project project, File f) throws IOException {
            DataInputStream is = new DataInputStream (new FileInputStream (f));
            try {
                Map<FileObject,FileCache> cache = new HashMap<FileObject, Index.FileCache> ();
                int i = is.readInt ();
                while (i > 0) {
                    String path = is.readUTF ();
                    File file = new File (path);
                    file = FileUtil.normalizeFile (file);
                    FileObject fo = FileUtil.toFileObject (file);
                    FileCache fc = FileCache.load (fo, is);
                    cache.put (fo, fc);
                    i--;
                }
                return new ProjectCache (project, cache);
            } finally {
                is.close ();
            }
        }
        
        private void save (File f) throws IOException {
            DataOutputStream os = new DataOutputStream (new FileOutputStream (f));
            try {
                os.writeInt (cache.size ());
                Iterator<FileObject> it = cache.keySet ().iterator ();
                while (it.hasNext ()) {
                    FileObject fileObject =  it.next();
                    os.writeUTF (fileObject.getPath ());
                    FileCache fc = cache.get (fileObject);
                    fc.save (os);
                }
            } finally {
                os.close ();
            }
        }
    }
    
    private static class FileCache {
    
        private FileObject                  fileObject;
        private List<DatabaseDefinition>    definitions;
        
        FileCache (FileObject fileObject) {
            this.fileObject = fileObject;
        }
        
        private FileCache (FileObject fileObject, List<DatabaseDefinition> definitions) {
            this.fileObject = fileObject;
            this.definitions = definitions;
        }
        
        private DatabaseContext getRoot (
            FileObject fo
        ) throws LanguageDefinitionNotFoundException, IOException, ParseException {
            Language l = LanguagesManager.get().getLanguage (fo.getMIMEType ());
            ASTNode root = l.parse (fo.getInputStream ());
            return DatabaseManager.parse (root, null, null);
        }

        private void add (
            Map<FileObject,List<DatabaseDefinition>>    result,
            String                                      name
        ) {
            if (definitions == null) {
                definitions = new ArrayList<DatabaseDefinition> ();
                try {
                    //long time = System.currentTimeMillis();
                    DatabaseContext r = getRoot (fileObject);
                    //S ystem.out.println ("parse " + fileObject.getNameExt () + " : " + (System.currentTimeMillis () - time));
                    definitions = r.getDefinitions ();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (name == null) {
                if (!definitions.isEmpty ()) {
                    List<DatabaseDefinition> l = result.get (fileObject);
                    if (l == null) {
                        l = new ArrayList<DatabaseDefinition> ();
                        result.put (fileObject, l);
                    }
                    l.addAll (definitions);
                }
            } else {
                List<DatabaseDefinition> l = result.get (fileObject);
                Iterator<DatabaseDefinition> it = definitions.iterator ();
                while (it.hasNext()) {
                    DatabaseDefinition definition =  it.next ();
                    if (definition.getName ().equals (name)) {
                        if (l == null) {
                            l = new ArrayList<DatabaseDefinition> ();
                            result.put (fileObject, l);
                        }
                        l.add (definition);
                    }
                }
            }
        }
        
        static FileCache load (FileObject fo, DataInputStream is) throws IOException {
            List<DatabaseDefinition> definitions = new ArrayList<DatabaseDefinition> ();
            int i = is.readInt ();
            while (i > 0) {
                definitions.add (DatabaseDefinition.load (is));
                i--;
            }
            return new FileCache (fo, definitions);
        }
        
        private void save (DataOutputStream os) throws IOException {
            os.writeInt (definitions.size ());
            Iterator<DatabaseDefinition> it = definitions.iterator ();
            while (it.hasNext ())
                it.next ().save (os);
        }
    }
}
