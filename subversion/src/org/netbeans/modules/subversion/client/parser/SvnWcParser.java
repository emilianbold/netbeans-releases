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
package org.netbeans.modules.subversion.client.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNScheduleKind;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.xml.sax.SAXException;

/**
 *
 * @author Ed Hillmann
 */
public class SvnWcParser {
    
    /** Creates a new instance of SvnWcParser */ 
    public SvnWcParser() {
    }

    private WorkingCopyDetails getWCDetails(File file) throws IOException, SAXException {   
        Map<String, String> attributes = EntriesCache.getInstance().getFileAttributes(file);
        return WorkingCopyDetails.createWorkingCopy(file, attributes);            
    }

   /**
     * 
     */ 
    public ISVNStatus[] getStatus(File path, boolean descend, boolean getAll) throws LocalSubversionException {        
        List<ISVNStatus> l = getStatus(path, descend);
//        List<ISVNStatus> ret = new ArrayList<ISVNStatus>(l.size());        
//        for(ISVNStatus status : l) {
//            if(!getAll) {
//                    if(!status.getRepositoryTextStatus().equals(SVNStatusKind.NORMAL)) { // XXX does this mean !getAll
//                       ret.add(status);
//                    }     
//            } else {
//                ret.add(status);
//            }
//        }
        return l.toArray(new ISVNStatus[l.size()]);
    }

    private List<ISVNStatus> getStatus(File path, boolean descend) throws LocalSubversionException {
        List<ISVNStatus> ret = new ArrayList<ISVNStatus>(20);
                        
        File[] children = path.listFiles();
        if(children != null && children.length > 0) {        
            for (int i = 0; i < children.length; i++) {
                ret.add(getSingleStatus(children[i]));            
                if(descend && children[i].isDirectory()) {                
                    ret.addAll(getStatus(children[i], descend));                
                }                    
            }        
        }
        ret.add(getSingleStatus(path));
        return ret;
    }    

    public ISVNStatus getSingleStatus(File file) throws LocalSubversionException {
        String finalTextStatus = SVNStatusKind.NORMAL.toString();
        String finalPropStatus = SVNStatusKind.NONE.toString();

        try {
            WorkingCopyDetails wcDetails = getWCDetails(file);
            if (wcDetails.isHandled()) {

                if (wcDetails.propertiesExist()) {
                    finalPropStatus = SVNStatusKind.NORMAL.toString();
                    //See if props have been modified
                    if (wcDetails.propertiesModified()) {
                        finalPropStatus = SVNStatusKind.MODIFIED.toString();
                    }
                }                
                if (wcDetails.isFile()) {
                    //Find Text Status
                    // XXX what if already added
                    if (wcDetails.textModified()) {
                        finalTextStatus = SVNStatusKind.MODIFIED.toString();
                    }
                } 

                String value = wcDetails.getValue("schedule");  // NOI18N
                if (value != null) {
                    if (value.equals("add")) {  // NOI18N
                        finalTextStatus = SVNStatusKind.ADDED.toString();
                        finalPropStatus = SVNStatusKind.NONE.toString();
                    } else if (value.equals("delete")) {  // NOI18N
                        finalTextStatus = SVNStatusKind.DELETED.toString();
                        finalPropStatus = SVNStatusKind.NONE.toString();
                    }
                    //status.c had a schedule="replace", but TSVN
                    //simply did a copy
                }
                value = wcDetails.getValue("deleted");  // NOI18N
                if (value != null) {
                    if (value.equals("true")) {  // NOI18N
                        finalTextStatus = SVNStatusKind.UNVERSIONED.toString();
                        finalPropStatus = SVNStatusKind.NONE.toString();
                    }
                }    

                String fileUrl = wcDetails.getValue("url");          // NOI18N        
                long revision = wcDetails.getLongValue("revision");             // NOI18N                         
                String nodeKind = wcDetails.getValue("kind", "normal");                  // NOI18N
                String lastCommitAuthor = wcDetails.getValue("last-author");  // NOI18N
                long lastChangedRevision = wcDetails.getLongValue("committed-rev");  // NOI18N
                Date lastCommittedDate = wcDetails.getDateValue("committed-date");              // NOI18N    

                boolean isCopied = wcDetails.getBooleanValue("copied");  // NOI18N
                String urlCopiedFrom = null;
                if (isCopied) {                                        
                    urlCopiedFrom = wcDetails.getValue("copyfrom-url");  // NOI18N  
                }

                File conflictNew = null;
                File conflictOld = null;
                File conflictWorking = null;
                value = wcDetails.getValue("conflict-wrk");  // NOI18N
                if (value != null && ((String)value).length() > 0) {
                    conflictWorking = new File(file.getParentFile(), value);
                }

                value = wcDetails.getValue("conflict-new");  // NOI18N
                if (value != null && ((String)value).length() > 0) {
                    conflictNew = new File(file.getParentFile(), value);
                }
                value = wcDetails.getValue("conflict-old");  // NOI18N
                if (value != null && ((String)value).length() > 0) {
                    conflictOld = new File(file.getParentFile(), value);
                }
                if ((conflictNew != null) || (conflictOld != null)) {
                    finalTextStatus = SVNStatusKind.CONFLICTED.toString();                
                }

                Date lockCreationDate = wcDetails.getDateValue("lock-creation-date");  // NOI18N
                String lockComment = null;
                String lockOwner = null;
                if (lockCreationDate != null) {                        
                    lockComment = wcDetails.getValue("lock-comment");  // NOI18N
                    lockOwner = wcDetails.getValue("lock-owner");      // NOI18N
                }

                return new ParserSvnStatus(
                        file,
                        fileUrl,
                        revision,
                        nodeKind,
                        finalTextStatus,
                        finalPropStatus,
                        lastCommitAuthor,
                        lastChangedRevision,
                        lastCommittedDate,
                        isCopied,
                        urlCopiedFrom,
                        conflictNew,
                        conflictOld,
                        conflictWorking,
                        lockCreationDate,
                        lockComment,
                        lockOwner);
            } else {
                //File isn't handled.
                return new ParserSvnStatus(
                        file,                                
                        wcDetails.getValue("url"),            // NOI18N
                        0,
                        "unknown",                            // NOI18N   
                        SVNStatusKind.UNVERSIONED.toString(),
                        SVNStatusKind.UNVERSIONED.toString(),
                        null,
                        0,
                        null,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
            }

        } catch (IOException ex) {
            throw new LocalSubversionException(ex);
        } catch (SAXException ex) {
            throw new LocalSubversionException(ex);
        } catch (IllegalArgumentException ex) {
            throw new LocalSubversionException(ex);
        }
    }

    public ISVNInfo getInfoFromWorkingCopy(File file) throws LocalSubversionException {

        ISVNInfo returnValue = null;
        try {
            WorkingCopyDetails wcDetails = getWCDetails(file);  // NOI18N
            if (wcDetails.isHandled()) {
                String fileUrl = wcDetails.getValue("url");               // NOI18N   
                String reposUrl = wcDetails.getValue("repos");  // NOI18N
                String reposUuid = wcDetails.getValue("uuid");  // NOI18N
                String schedule = wcDetails.getValue("schedule");  // NOI18N
                if (schedule == null) {
                    schedule = SVNScheduleKind.NORMAL.toString();
                }

                long revision = wcDetails.getLongValue("revision");     // NOI18N                
                boolean isCopied = wcDetails.getBooleanValue("copied");  // NOI18N
                String urlCopiedFrom = null;
                long revisionCopiedFrom = 0;
                if (isCopied) {
                    urlCopiedFrom = wcDetails.getValue("copyfrom-url");  // NOI18N
                    revisionCopiedFrom = wcDetails.getLongValue("copyfrom-rev");      // NOI18N               
                } 

                Date lastCommittedDate = wcDetails.getDateValue("committed-date");  // NOI18N
                long lastChangedRevision = wcDetails.getLongValue("committed-rev");     // NOI18N             
                String lastCommitAuthor = wcDetails.getValue("last-author");        // NOI18N          
                Date lastDatePropsUpdate = wcDetails.getDateValue("prop-time");     // NOI18N                          
                Date lastDateTextUpdate = wcDetails.getDateValue("text-time");  // NOI18N

                Date lockCreationDate = wcDetails.getDateValue("lock-creation-date");  // NOI18N
                String lockComment = null;
                String lockOwner = null;                
                if (lockCreationDate != null) {                    
                    lockComment = wcDetails.getValue("lock-comment");  // NOI18N
                    lockOwner = wcDetails.getValue("lock-owner");  // NOI18N
                }

                String nodeKind = wcDetails.getValue("kind", "normal");     // NOI18N             
                returnValue = new ParserSvnInfo(file, fileUrl, reposUrl, reposUuid,
                    schedule, revision, isCopied, urlCopiedFrom, revisionCopiedFrom,
                    lastCommittedDate, lastChangedRevision, lastCommitAuthor,
                    lastDatePropsUpdate, lastDateTextUpdate, lockCreationDate,
                    lockOwner, lockComment, nodeKind, wcDetails.getPropertiesFile(), wcDetails.getBasePropertiesFile());
            } else {
                String fileUrl = wcDetails.getValue("url");  // NOI18N
                String reposUrl = wcDetails.getValue("repos");  // NOI18N
                String reposUuid = wcDetails.getValue("uuid");  // NOI18N
                returnValue = new ParserSvnInfo(file, fileUrl, reposUrl, reposUuid,
                    SVNScheduleKind.NORMAL.toString(), 0, false, null, 0, null, 0, null,
                    null, null, null, null, null, SVNNodeKind.UNKNOWN.toString(), null, null);
            }
        } catch (IOException ex) {
            throw new LocalSubversionException(ex);
        } catch (SAXException ex) {
            throw new LocalSubversionException(ex);
        }
        return returnValue;
    }
    
}

