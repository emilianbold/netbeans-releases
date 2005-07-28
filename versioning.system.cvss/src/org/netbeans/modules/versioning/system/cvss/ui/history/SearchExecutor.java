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
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.RLogExecutor;
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
    private final File[]                roots;
    private final SearchCriteriaPanel   criteria;
    
    private List                        results;

    public SearchExecutor(SearchHistoryPanel master) {
        this.master = master;
        roots = master.getRoots();
        criteria = master.getCriteria();
    }

    public void run() {
        String from = criteria.getFrom();
        String to = criteria.getTo();
        Date fromDate = parseDate(from);
        Date toDate = parseDate(to);

        RlogCommand cmd = new RlogCommand();

        if (fromDate != null || toDate != null) {
            String dateFilter = "";
            if (fromDate != null) {
                dateFilter = fullDateFormat.format(fromDate);
            }
            dateFilter += "<=";
            if (toDate != null) {
                dateFilter += fullDateFormat.format(toDate);
            }
            cmd.setDateFilter(dateFilter);
        } else if (from != null || to != null) {
            String revFilter = "";
            if (from != null) {
                revFilter = from;
            }
            revFilter += ":";
            if (to != null) {
                revFilter += to;
            }
            cmd.setRevisionFilter(revFilter);
        }
        
        cmd.setUserFilter(criteria.getUsername());
        
        RLogExecutor [] executors = RLogExecutor.executeCommand(cmd, roots, null);
        ExecutorSupport.wait(executors);
        results = processResults(executors);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showResults();
            }
        });
    }

    private void showResults() {
        JPanel resultsPanel = master.getResultsPanel();
        resultsPanel.removeAll();
        SummaryView summary = new SummaryView(results);
        resultsPanel.add(summary.getComponent());
        resultsPanel.revalidate();
    }

    private List processResults(RLogExecutor[] executors) {
        List log = new ArrayList(200);
        for (int i = 0; i < executors.length; i++) {
            RLogExecutor executor = executors[i];
            log.addAll(executor.getLogEntries());
        }
        String commitMessage = criteria.getCommitMessage();

        List results = new ArrayList(log.size());
        for (Iterator i = log.iterator(); i.hasNext();) {
            LogInformation info = (LogInformation) i.next();
            results.addAll(info.getRevisionList());
        }

        if (commitMessage != null) {
            for (Iterator i = results.iterator(); i.hasNext();) {
                LogInformation.Revision revision = (LogInformation.Revision) i.next();
                String msg = revision.getMessage();
                if (msg.indexOf(commitMessage) == -1) {
                    i.remove();
                }
            }
        }

        return results;
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
