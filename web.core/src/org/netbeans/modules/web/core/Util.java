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

package org.netbeans.modules.web.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/** Utility class
* @author  Petr Jiricka
* @version 1.00, Jun 03, 1999
*/
public class Util {

    /** Waits for startup of a server, waits until the connection has
     * been established. */ 

    public static boolean waitForURLConnection(URL url, int timeout, int retryTime) { 
        Connect connect = new Connect(url, retryTime); 
        Thread t = new Thread(connect);
        t.start();
        try {
            t.join(timeout);
        } catch(InterruptedException ie) {
        }
        if (t.isAlive()) {
            connect.finishLoop();
            t.interrupt();//for thread deadlock
        }
        return connect.getStatus();
    }

    public static String issueGetRequest(URL url) {
        BufferedReader in = null;
        StringBuffer input = new StringBuffer();
        try {
            in = new BufferedReader(new InputStreamReader(
                                        url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                input.append(inputLine);
                input.append("\n"); // NOI18N
            }  
            return input.toString();
        }
        catch (Exception e) {
	    //e.printStackTrace();
            return null;
        }
        finally {
            if (in != null)
                try {
                    in.close();
                }
                catch(IOException e) {
                    //e.printStackTrace();
                }
        }
    }

    /** Returns string for localhost */
/*    public static String getLocalHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            return "127.0.0.1"; // NOI18N
        }
    }
*/
    private static class Connect implements Runnable  {

        URL url = null;
        int retryTime;
        boolean status = false;
        boolean loop = true;

        public Connect(URL url, int retryTime) {
            this.url = url;
            this.retryTime = retryTime; 
        } 

        public void finishLoop() {
            loop = false;
        }

        public void run() {
            try {
                InetAddress.getByName(url.getHost());
            } catch (UnknownHostException e) {
                return;
            }
            while (loop) {
                try {
                    Socket socket = new Socket(url.getHost(), url.getPort());
                    socket.close();
                    status = true;
                    break;
                } catch (UnknownHostException e) {//nothing to do
                } catch (IOException e) {//nothing to do
                }
                try {
                    Thread.currentThread().sleep(retryTime);
                } catch(InterruptedException ie) {
                }
            }
        }

        boolean getStatus() {
            return status;
        }
    }

    // following block is copy/pasted code from 
    // org.netbeans.modules.j2ee.ejbjarproject.Utils
    
    /**
     * Returns Java source groups for all source packages in given project.<br>
     * Doesn't include test packages.
     *
     * @param project Project to search
     * @return Array of SourceGroup. It is empty if any probelm occurs.
     */
    public static SourceGroup[] getJavaSourceGroups(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                                    JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set testGroups = getTestSourceGroups(project, sourceGroups);
        List result = new ArrayList();
        for (int i = 0; i < sourceGroups.length; i++) {
            if (!testGroups.contains(sourceGroups[i])) {
                result.add(sourceGroups[i]);
            }
        }
        return (SourceGroup[]) result.toArray(new SourceGroup[result.size()]);
    }

    private static Set/*<SourceGroup>*/ getTestSourceGroups(Project project, SourceGroup[] sourceGroups) {
        Map foldersToSourceGroupsMap = createFoldersToSourceGroupsMap(sourceGroups);
        Set testGroups = new HashSet();
        for (int i = 0; i < sourceGroups.length; i++) {
            testGroups.addAll(getTestTargets(sourceGroups[i], foldersToSourceGroupsMap));
        }
        return testGroups;
    }
    
    private static Map createFoldersToSourceGroupsMap(final SourceGroup[] sourceGroups) {
        Map result;
        if (sourceGroups.length == 0) {
            result = Collections.EMPTY_MAP;
        } else {
            result = new HashMap(2 * sourceGroups.length, .5f);
            for (int i = 0; i < sourceGroups.length; i++) {
                SourceGroup sourceGroup = sourceGroups[i];
                result.put(sourceGroup.getRootFolder(), sourceGroup);
            }
        }
        return result;
    }

    private static List/*<FileObject>*/ getFileObjects(URL[] urls) {
        List result = new ArrayList();
        for (int i = 0; i < urls.length; i++) {
            FileObject sourceRoot = URLMapper.findFileObject(urls[i]);
            if (sourceRoot != null) {
                result.add(sourceRoot);
            } else {
                int severity = ErrorManager.INFORMATIONAL;
                if (ErrorManager.getDefault().isNotifiable(severity)) {
                    ErrorManager.getDefault().notify(severity, new IllegalStateException(
                       "No FileObject found for the following URL: " + urls[i])); //NOI18N
                }
            }
        }
        return result;
    }
    
    private static List/*<SourceGroup>*/ getTestTargets(SourceGroup sourceGroup, Map foldersToSourceGroupsMap) {
        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
        if (rootURLs.length == 0) {
            return new ArrayList();
        }
        List result = new ArrayList();
        List sourceRoots = getFileObjects(rootURLs);
        for (int i = 0; i < sourceRoots.size(); i++) {
            FileObject sourceRoot = (FileObject) sourceRoots.get(i);
            SourceGroup srcGroup = (SourceGroup) foldersToSourceGroupsMap.get(sourceRoot);
            if (srcGroup != null) {
                result.add(srcGroup);
            }
        }
        return result;
    }

    // end of copy/paste block
    
}
