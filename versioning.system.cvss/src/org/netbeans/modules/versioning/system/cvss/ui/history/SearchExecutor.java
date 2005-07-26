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
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.RLogExecutor;
import org.netbeans.modules.versioning.system.cvss.ExecutorSupport;

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

    private static final SimpleDateFormat defaultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    private static final DateFormat [] dateFormats = new DateFormat[] {
        defaultDateFormat,
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
        new SimpleDateFormat("yyyy-MM-dd HH:mm"),
        new SimpleDateFormat("yyyy-MM-dd"),
    };
    
    private final File[]                roots;
    private final SearchCriteriaPanel   criteria;

    public SearchExecutor(File [] roots, SearchCriteriaPanel criteria) {
        this.roots = roots;
        this.criteria = criteria;
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
                dateFilter = defaultDateFormat.format(fromDate);
            }
            dateFilter += "<=";
            if (toDate != null) {
                dateFilter += defaultDateFormat.format(toDate);
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
        processResults(executors);
    }

    private void processResults(RLogExecutor[] executors) {
        List log = new ArrayList(200);
        for (int i = 0; i < executors.length; i++) {
            RLogExecutor executor = executors[i];
            log.addAll(executor.getLogEntries());
        }
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
