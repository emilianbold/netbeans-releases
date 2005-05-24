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

package org.netbeans.modules.javadoc.search;

import java.io.*;
import java.lang.ref.*;
import java.text.Collator;
import java.util.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import javax.swing.text.html.parser.*;

import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.util.NbBundle;

import org.openide.util.RequestProcessor;


/**
 * Builds index of Javadoc filesystems.
 * @author Svata Dedic, Jesse Glick
 */
public class IndexBuilder implements Runnable, ChangeListener {

    private static final String[] INDEX_FILE_NAMES = {
        "overview-summary.html", // NOI18N
        "index.html", // NOI18N
        "index.htm", // NOI18N
    };

    private static IndexBuilder INSTANCE;

    private static RequestProcessor.Task    task;
    
    private final ErrorManager err;
    
    private Reference cachedData;
    
    private JavadocRegistry jdocRegs;

    /**
     * WeakMap<FileSystem : info> of information extracted from filesystems.
     */
    Map     filesystemInfo = Collections.EMPTY_MAP;

    private static class Info {
        /**
         * Display name / title of the helpset
         */
        String      title;

        /**
         * Name of the index/overview file
         */
        String      indexFileName;
    }

    private IndexBuilder() {
        this.jdocRegs = JavadocRegistry.getDefault();
        this.jdocRegs.addChangeListener(this);
        err = ErrorManager.getDefault().getInstance("org.netbeans.modules.javadoc.search.IndexBuilder"); // NOI18N
        if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
            err.log("new IndexBuilder");
        }
    }

    /**
     * Get the default index builder instance.
     * It will start parsing asynch.
     */
    public synchronized static IndexBuilder getDefault() {
        if (INSTANCE != null)
            return INSTANCE;
        INSTANCE = new IndexBuilder();
        scheduleTask();
        return INSTANCE;
    }
    
    public void run() {
        cachedData = null;
        refreshIndex();
    }
    
    public void stateChanged (ChangeEvent event) {
        scheduleTask ();
    }

    /**
     * Get the important information from the index builder.
     * Waits for parsing to complete first, if necessary.
     * @return two lists, one of String display names, the other of FileObject indices
     */
    public List[] getIndices() {
        task.waitFinished();
        if (cachedData != null) {
            List[] data = (List[])cachedData.get();
            if (data != null) {
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log("getIndices (cached)");
                }
                return data;
            }
        }
        
        if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
            err.log("getIndices");
        }
        Map m = this.filesystemInfo;
        Iterator it = m.entrySet().iterator();
        final Collator c = Collator.getInstance();
        class Pair implements Comparable {
            public String display;
            public FileObject fo;
            public int compareTo(Object o) {
                return c.compare(display, ((Pair)o).display);
            }
        }
        SortedSet pairs = new TreeSet(); // SortedSet<Pair>
        for (int i = 0; i < m.size(); i++) {
            Map.Entry e = (Map.Entry)it.next();
            FileObject f = (FileObject)e.getKey();
            Info info = (Info)e.getValue();
            FileObject fo = f.getFileObject(info.indexFileName);
            if (fo == null)
                continue;
            Pair p = new Pair();
            p.display = info.title;
            p.fo = fo;
            pairs.add(p);
        }
        List display = new ArrayList(pairs.size());
        List fos = new ArrayList(pairs.size());
        it = pairs.iterator();
        while (it.hasNext()) {
            Pair p = (Pair)it.next();
            display.add(p.display);
            fos.add(p.fo);
        }
        List[] data = new List[] {display, fos};
        cachedData = new WeakReference(data);
        return data;
    }

    private void refreshIndex() {
        if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
            err.log("refreshIndex");
        }
        Map oldMap;
        synchronized (this) {
            oldMap = this.filesystemInfo;
        }
        //Enumeration e = FileSystemCapability.DOC.fileSystems();
        FileObject docRoots[] = jdocRegs.getDocRoots();
        // XXX needs to be able to listen to result; when it changes, call scheduleTask()
        Map m = new WeakHashMap();

        for ( int ifCount = 0; ifCount < docRoots.length; ifCount++ ) {
            FileObject fo = docRoots[ifCount];
            Info oldInfo = (Info)oldMap.get(fo);
            if (oldInfo != null) {
                // No need to reparse.
                m.put(fo, oldInfo);
                continue;
            }
            
            FileObject index = null;
            for (int i = 0; i < INDEX_FILE_NAMES.length; i++) {
                if ((index = fo.getFileObject(INDEX_FILE_NAMES[i])) != null) {
                    break;
                }
            }
            if (index == null || index.getName().equals("index")) { // NOI18N
                // For single-package doc sets, overview-summary.html is not present,
                // and index.html is less suitable (it is framed). Look for a package
                // summary.
                // [PENDING] Display name is not ideal, e.g. "org.openide.windows (NetBeans Input/Output API)"
                // where simply "NetBeans Input/Output API" is preferable... but standard title filter
                // regexps are not so powerful (to avoid matching e.g. "Servlets (Main Documentation)").
                FileObject packageList = fo.getFileObject("package-list"); // NOI18N
                if (packageList != null) {
                    try {
                        InputStream is = packageList.getInputStream();
                        try {
                            BufferedReader r = new BufferedReader(new InputStreamReader(is));
                            String line = r.readLine();
                            if (line != null && r.readLine() == null) {
                                // Good, exactly one line as expected. A package name.
                                String resName = line.replace('.', '/') + "/package-summary.html"; // NOI18N
                                FileObject pindex = fo.getFileObject(resName);
                                if (pindex != null) {
                                    index = pindex;
                                }
                                // else fall back to index.html if available
                            }
                        } finally {
                            is.close();
                        }
                    } catch (IOException ioe) {
                            // Oh well, skip this one.
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                    }
                }
            }
            if (index != null) {
                // Try to find a title.
                String title = parseTitle(index);
                if (title != null) {
                    JavadocSearchType st = jdocRegs.findSearchType( fo );
                    if (st == null)
                        continue;
                    title = st.getOverviewTitleBase(title);
                }
                if ("".equals(title)) { // NOI18N
                    title = NbBundle.getMessage(IndexBuilder.class,
                            "FMT_NoOverviewTitle", new Object[] { index.getPath(), // NOI18N
                                                                   fo.getName(),
                                                                   fo.getName() });
                }
                Info info = new Info();
                info.title = title == null ? fo.getName() : title;
                info.indexFileName = FileUtil.getRelativePath(fo, index);
                m.put(fo, info);
            }
            synchronized (this) {
                this.filesystemInfo = m;
            }
        }
    }
    
    /**
     * Attempt to find the title of an HTML file object.
     * May return null if there is no title tag, or "" if it is empty.
     */
    private String parseTitle(FileObject html) {
        try {
            // #32551: first try to find title the easy way, only then fall back.
            // XXX character set may be an issue here...
            BufferedReader b = new BufferedReader(new InputStreamReader(html.getInputStream()));
            String line;
            while ((line = b.readLine()) != null) {
                if (line.equalsIgnoreCase("<title>")) { // NOI18N
                    String title = b.readLine();
                    if (title != null) {
                        String next = b.readLine();
                        if ("</title>".equalsIgnoreCase(next)) { // NOI18N
                            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                                err.log("Fast parsing of " + title);
                            }
                            return title;
                        }
                    }
                }
            }
            // OK, fall back to slower parsing mode.
            final String[] title = new String[1];
            // XXX technically this should use an encoding according to the HTML character
            // set specification...
            Reader r = new InputStreamReader(html.getInputStream());
            try {
                class TitleParser extends Parser {
                    public TitleParser() throws IOException {
                        super(DTD.getDTD("html32")); // NOI18N
                    }
                    protected void handleTitle(char[] text) {
                        title[0] = new String(text);
                        if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                            err.log("Slow parsing of " + title[0]);
                        }
                    }
                }
                new TitleParser().parse(r);
                return title[0];
            } finally {
                r.close();
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            return null;
        }
    }

    
    private synchronized static void scheduleTask() {
        if (task == null)
            task = RequestProcessor.getDefault().create(getDefault());
        // Give it a small delay to avoid restarting too many times e.g. during
        // project switch:
        task.schedule(100);
    }

}
