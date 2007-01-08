/*
 * ReportNote.java
 *
 * Created on November 9, 2006, 11:18 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl.validator;

/**
 *
 * @author Michal Skvor
 */
public final class ReportNote {
    
    public static final int SEVERITY_LOG        = 1;
    public static final int SEVERITY_WARNING    = 2;
    public static final int SEVERITY_ERROR      = 3;

    private int severity;
    private String desctiption;

    public ReportNote( int severity, String desctiption ) {
        this.severity = severity;
        this.desctiption = desctiption;
    }

    public int getSeverity() {
        return severity;
    }

    public String getDescription() {
        return desctiption;
    }
}
