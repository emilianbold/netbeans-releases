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

package org.netbeans.modules.javadoc.search;

import java.io.*;
import java.util.*;
import javax.swing.text.html.parser.*;

import org.openide.ErrorManager;
import org.openide.filesystems.*;

import org.openide.util.RequestProcessor;

/**
 * @author Svata Dedic
 */
public class IndexBuilder implements Runnable, RepositoryListener {
    /**
     * Refresh delay v ms
     */
    static final int REFRESH_DELAY = 1000;

    static final String[] INDEX_FILE_NAMES = {
        "overview-summary.html", // NOI18N
        "api/overview-summary.html", // NOI18N
        "index.html", // NOI18N
        "api/index.html", // NOI18N
        "index.htm", // NOI18N
        "api/index.htm", // NOI18N
    };

    static IndexBuilder INSTANCE;

    static RequestProcessor.Task    task;

    /**
     * WeakMap<FileSystem : info> of information extracted from filesystems.
     */
    Map     filesystemInfo = Collections.EMPTY_MAP;

    static class Info {
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
        Repository.getDefault().addRepositoryListener(this);
    }

    public static void initialize() {
        getDefault();
    }

    synchronized static IndexBuilder getDefault() {
        if (INSTANCE != null)
            return INSTANCE;
        INSTANCE = new IndexBuilder();
        scheduleTask();
        return INSTANCE;
    }

    public void run() {
        refreshIndex();
    }

    public List[] getIndices() {
        Map m = this.filesystemInfo;
        List display = new ArrayList(m.size());
        List fos = new ArrayList(m.size());
        Iterator it = m.entrySet().iterator();
        for (int i = 0; i < m.size(); i++) {
            Map.Entry e = (Map.Entry)it.next();
            FileSystem fs = (FileSystem)e.getKey();
            Info info = (Info)e.getValue();
            FileObject fo = fs.findResource(info.indexFileName);
            if (fo == null)
                continue;
            display.add(info.title);
            fos.add(fo);
        }
        return new List[] { display, fos };
    }

    public void refreshIndex() {
        Enumeration e = FileSystemCapability.DOC.fileSystems();
        Collection c = new LinkedList();
        Map m = new WeakHashMap();

        while (e.hasMoreElements()) {
            FileSystem fs = (FileSystem)e.nextElement();
            FileObject index = null;
            for (int i = 0; i < INDEX_FILE_NAMES.length; i++) {
                if ((index = fs.findResource(INDEX_FILE_NAMES[i])) != null) {
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
                FileObject packageList = fs.findResource("package-list"); // NOI18N
                if (packageList == null) {
                    packageList = fs.findResource("api/package-list"); // NOI18N
                }
                if (packageList != null) {
                    try {
                        InputStream is = packageList.getInputStream();
                        try {
                            BufferedReader r = new BufferedReader(new InputStreamReader(is));
                            String line = r.readLine();
                            if (line != null && r.readLine() == null) {
                                // Good, exactly one line as expected. A package name.
                                String resName = line.replace('.', '/') + "/package-summary.html"; // NOI18N
                                FileObject pindex = fs.findResource(resName);
                                if (pindex == null) {
                                    pindex = fs.findResource("api/" + resName); // NOI18N
                                }
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
                final String[] title = new String[1];
                try {
                    Reader r = new InputStreamReader(index.getInputStream());
                    try {
                        class TitleParser extends Parser {
                            public TitleParser() throws IOException {
                                super(DTD.getDTD("html32")); // NOI18N
                            }
                            protected void handleTitle(char[] text) {
                                title[0] = new String(text);
                            }
                        }
                        new TitleParser().parse(r);
                    } finally {
                        r.close();
                    }
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                }
                JavaDocFSSettings fss = JavaDocFSSettings.getSettingForFS(fs);
                if (title[0] != null && fss != null) {
                    JavadocSearchType st = fss.getSearchTypeEngine();
                    if (st == null)
                        continue;
                    title[0] = st.getOverviewTitleBase(title[0]);
                }
                c.add(fs);
                Info info = new Info();
                info.title = title[0] == null ? fs.getDisplayName() : title[0];
                info.indexFileName = index.getPath();
                m.put(fs, info);
            }
            synchronized (this) {
                this.filesystemInfo = m;
            }
        }
    }

    synchronized static void scheduleTask() {
        if (task == null)
            task = RequestProcessor.getDefault().create(getDefault());
        task.schedule(REFRESH_DELAY);
    }

    public void fileSystemAdded(RepositoryEvent ev) {
        scheduleTask();
    }

    public void fileSystemRemoved(RepositoryEvent ev) {
        scheduleTask();
    }

    public void fileSystemPoolReordered(RepositoryReorderedEvent ev) {

    }
}
