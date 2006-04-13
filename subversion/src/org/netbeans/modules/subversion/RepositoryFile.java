/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion;

import java.net.MalformedURLException;
import org.openide.ErrorManager;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class RepositoryFile {
        
    public static final String[] ROOT_FOLDER_PATH = new String[0];    
    
    private final SVNUrl repositoryUrl;    
    private final SVNRevision revision;
    private SVNUrl fileUrl;    
    private String[] pathSegments;    
    private String path;    
    private String toString = null;    
    private boolean repositoryRoot;

    private String name;
    
    public RepositoryFile(SVNUrl repositoryUrl, SVNRevision revision) {
        assert repositoryUrl != null;        
        assert revision != null;
        
        this.repositoryUrl = repositoryUrl;
        this.revision = revision;
        repositoryRoot = true;
    }
        
    public RepositoryFile(SVNUrl repositoryUrl, SVNUrl fileUrl, SVNRevision revision) {       
        this(repositoryUrl, revision);
        this.fileUrl = fileUrl;        
        repositoryRoot = fileUrl == null;   
        
        if(!repositoryRoot) {            
            String[] fileUrlSegments = fileUrl.getPathSegments();
            int fileSegmentsLength = fileUrlSegments.length;
            int repositorySegmentsLength = repositoryUrl.getPathSegments().length;
            pathSegments = new String[fileSegmentsLength - repositorySegmentsLength];
            StringBuffer sb = new StringBuffer();
            for (int i = repositorySegmentsLength; i < fileSegmentsLength; i++) {
                pathSegments[i-repositorySegmentsLength] = fileUrlSegments[i];
                sb.append(fileUrlSegments[i]);
                if(i-repositorySegmentsLength < pathSegments.length-1) {
                    sb.append("/"); // NOI18N
                }
            }    
            path = sb.toString();
        }                
    }

    public RepositoryFile(SVNUrl repositoryUrl, String[] pathSegments, SVNRevision revision) throws MalformedURLException {
        this(repositoryUrl, revision);
        this.pathSegments = pathSegments;    
        repositoryRoot = pathSegments == null;        
        
        if(!repositoryRoot) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < pathSegments.length; i++) {
                sb.append(pathSegments[i]);
                if(i<pathSegments.length-1) {
                sb.append("/"); // NOI18N
                }            
            }
            path = sb.toString();        
            fileUrl = repositoryUrl.appendPath(path);        
        }
    }
    
    public RepositoryFile(SVNUrl repositoryUrl, String path, SVNRevision revision) throws MalformedURLException {
        this(repositoryUrl, revision);
        this.path = path;
        repositoryRoot = path == null;        
        
        if(!repositoryRoot) {                        
            fileUrl = repositoryUrl.appendPath(path);        
            pathSegments = path.split("/");
        }
    }
    
    public SVNUrl getRepositoryUrl() {
        return repositoryUrl;
    }

    public SVNRevision getRevision() {
        return revision;
    }

    public SVNUrl getFileUrl() {
        if(isRepositoryRoot()) {
            return getRepositoryUrl();
        }
        return fileUrl;
    }

    public String[] getPathSegments() {
        if(isRepositoryRoot()) {
            return ROOT_FOLDER_PATH;
        }        
        return pathSegments;
    }

    public String getPath() {
        if(isRepositoryRoot()) { 
            return ""; // NOI18N
        }        
        return path;
    }
    
    public boolean isRepositoryRoot() {
        return repositoryRoot;
    }

    public String getName() {
        if(name == null) {
            if(isRepositoryRoot()) {
                String url = getRepositoryUrl().toString();
                int idx = url.indexOf("://"); // NOI18N
                if(idx >= 0) {
                    url = url.substring(idx+3);
                }
                return  url;
            } else {
                return  getFileUrl().getLastPathSegment();
            }    
        }
        return name;
    }
    
    public String toString() {
        if(toString == null) {
            StringBuffer sb = new StringBuffer();
            sb.append(fileUrl);
            sb.append("@"); // NOI18N
            sb.append(revision);    
            toString = sb.toString();
        }
        
        return toString;
    }

    public RepositoryFile appendPath(String path) {
        return new RepositoryFile(repositoryUrl, getFileUrl().appendPath(path), revision);
    }       

    public RepositoryFile replaceLastSegment(String segment, int level) {
        assert segment != null && !segment.equals("");
        assert level > -1 && level < fileUrl.getPathSegments().length;
        assert !isRepositoryRoot(); // can't do this 

        String fileUrlString = fileUrl.toString();
        int fromIdx = fileUrlString.lastIndexOf('/');
        int toIdx = fileUrlString.length() - 1;
        for (int i = 0; i < level; i++) {
            toIdx = fromIdx - 1;
            fromIdx = fileUrlString.lastIndexOf('/', fromIdx - 1);
        }        
        
        assert !(fromIdx < repositoryUrl.toString().length());
        assert toIdx >= fromIdx && toIdx < fileUrlString.length();

        SVNUrl newUrl = null;
        try {
            newUrl = new SVNUrl(fileUrlString.substring(0, fromIdx + 1) + segment + fileUrlString.substring(toIdx + 1));
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);  // should not happen
        }                        
        return new RepositoryFile(repositoryUrl, newUrl, revision);
    }

    public RepositoryFile  removeLastSegment() {                
        assert !isRepositoryRoot(); // can't do this 
        
        String fileUrlStrint = fileUrl.toString();
        int idx = fileUrlStrint.lastIndexOf('/');

        assert !(idx < repositoryUrl.toString().length());

        SVNUrl newUrl = null;
        try {
            newUrl = new SVNUrl(fileUrlStrint.substring(0, idx));
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);  // should not happen
        }                        
        return new RepositoryFile(repositoryUrl, newUrl, revision);
    }         
    
}
