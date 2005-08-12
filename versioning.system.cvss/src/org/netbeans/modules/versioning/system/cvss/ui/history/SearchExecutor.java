/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.log.LogCommand;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.RLogExecutor;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.LogExecutor;
import org.netbeans.modules.versioning.system.cvss.ExecutorSupport;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Executes searches in Search History panel.
 * 
 * @author Maros Sandor
 */
class SearchExecutor implements Runnable {

    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    private static final SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    private static final DateFormat [] dateFormats = new DateFormat[] {
        fullDateFormat,
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
        simpleDateFormat,
        new SimpleDateFormat("yyyy-MM-dd"),
    };
    
    private final SearchHistoryPanel    master;
    private final File[]                folders;
    private final File[]                files;
    private final SearchCriteriaPanel   criteria;
    
    private List                        results;

    public SearchExecutor(SearchHistoryPanel master) {
        this.master = master;
        File [] roots = master.getRoots();
        
        Set foldersSet = new HashSet(roots.length); 
        Set filesSet = new HashSet(roots.length); 
        for (int i = 0; i < roots.length; i++) {
            File root = roots[i];
            if (root.isFile()) {
                filesSet.add(root);
            } else {
                foldersSet.add(root);
            }
        }
        files = (File[]) filesSet.toArray(new File[filesSet.size()]);
        folders = (File[]) foldersSet.toArray(new File[foldersSet.size()]);
        criteria = master.getCriteria();
    }

    public void run() {
        String from = criteria.getFrom();
        String to = criteria.getTo();
        Date fromDate = parseDate(from);
        Date toDate = parseDate(to);

        RlogCommand rcmd = new RlogCommand();
        LogCommand lcmd = new LogCommand();

        if (fromDate != null || toDate != null) {
            String dateFilter = "";
            if (fromDate != null) {
                dateFilter = fullDateFormat.format(fromDate);
            }
            dateFilter += "<=";
            if (toDate != null) {
                dateFilter += fullDateFormat.format(toDate);
            }
            rcmd.setDateFilter(dateFilter);
            lcmd.setDateFilter(dateFilter);
        } else if (from != null || to != null) {
            String revFilter = "";
            if (from != null) {
                revFilter = from;
            }
            revFilter += ":";
            if (to != null) {
                revFilter += to;
            }
            rcmd.setRevisionFilter(revFilter);
            lcmd.setRevisionFilter(revFilter);
        }
        
        rcmd.setUserFilter(criteria.getUsername());
        lcmd.setUserFilter(criteria.getUsername());
        
        RLogExecutor [] rexecutors;
        if (folders.length > 0) {
            rexecutors = RLogExecutor.executeCommand(rcmd, folders, null);
            ExecutorSupport.wait(rexecutors);
        } else {
            rexecutors = new RLogExecutor[0];
        }
        
        LogExecutor [] lexecutors;
        if (files.length > 0) {
            lcmd.setFiles(files);
            lexecutors = LogExecutor.executeCommand(lcmd, null);
            ExecutorSupport.wait(lexecutors);
        } else {
            lexecutors = new LogExecutor[0];
        }
        
        results = processResults(rexecutors, lexecutors);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                master.setResults(results);
            }
        });
    }

    private List processResults(RLogExecutor[] rexecutors, LogExecutor[] lexecutors) {
        List log = new ArrayList(200);
        for (int i = 0; i < rexecutors.length; i++) {
            RLogExecutor executor = rexecutors[i];
            log.addAll(executor.getLogEntries());
        }
        for (int i = 0; i < lexecutors.length; i++) {
            LogExecutor executor = lexecutors[i];
            log.addAll(executor.getLogEntries());
        }
        String commitMessage = criteria.getCommitMessage();

        List newResults = new ArrayList(log.size());
        for (Iterator i = log.iterator(); i.hasNext();) {
            LogInformation info = (LogInformation) i.next();
            newResults.addAll(info.getRevisionList());
        }

        if (commitMessage != null) {
            for (Iterator i = newResults.iterator(); i.hasNext();) {
                LogInformation.Revision revision = (LogInformation.Revision) i.next();
                String msg = revision.getMessage();
                if (msg.indexOf(commitMessage) == -1) {
                    i.remove();
                }
            }
        }

        return newResults;
    }
    
    private Date parseDate(String s) {
        if (s == null) return null;
        for (int i = 0; i < dateFormats.length; i++) {
            DateFormat dateformat = dateFormats[i];
            try {
                return dateformat.parse(s);
            } catch (ParseException e) {
                // try the next one
            }
        }
        return null;
    }

}
