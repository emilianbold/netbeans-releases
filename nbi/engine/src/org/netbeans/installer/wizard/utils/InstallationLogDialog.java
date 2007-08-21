/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.wizard.utils;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.helper.swing.NbiDialog;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiScrollPane;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;

public class InstallationLogDialog extends NbiDialog {
    private NbiTextPane   logPane;
    private NbiPanel      logPanel;
    private NbiScrollPane logScrollPane;
    
    private NbiLabel errorLabel;
    
    private File logFile;
    private static final String MSG_LOADING_LOGFILE_KEY =
            "ILD.loading.logfile";//NOI18N
    private static final String ERROR_READING_LOGFILE_KEY =
            "ILD.error.reading.log";//NOI18N
    private static final String ERROR_LOG_CONTENTS =
            "ILD.error.log.contents";//NOI18N
    public InstallationLogDialog() {
        super();
        
        initComponents();
        initialize();
    }
    
    private void initialize() {
        logFile = LogManager.getLogFile();
        
        setTitle(logFile.getAbsolutePath());
    }
    
    private void initComponents() {
        setLayout(new GridBagLayout());
        
        logPane = new NbiTextPane();
        logPane.setFont(new Font("Monospaced", 
                logPane.getFont().getStyle(), logPane.getFont().getSize()));
        
        logPanel = new NbiPanel();
        logPanel.setLayout(new BorderLayout());
        logPanel.add(logPane, BorderLayout.CENTER);
        
        logScrollPane = new NbiScrollPane(logPanel);
        logScrollPane.setViewportBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        errorLabel = new NbiLabel();
        
        add(logScrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, 
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, 
                new Insets(11, 11, 11, 11), 0, 0));
        add(errorLabel,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, 
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(11, 11, 11, 11), 0, 0));
    }
    
    public void loadLogFile() {
        try {
            logScrollPane.setVisible(false);
            errorLabel.setVisible(true);
            
            errorLabel.setText(ResourceUtils.getString(
                    InstallationLogDialog.class,
                    MSG_LOADING_LOGFILE_KEY));
            logPane.setText(FileUtils.readFile(logFile));
            logPane.setCaretPosition(0);
            
            logScrollPane.setVisible(true);
            errorLabel.setVisible(false);
        }  catch (IOException e) {
            ErrorManager.notify(ErrorLevel.WARNING,
                    ResourceUtils.getString(InstallationLogDialog.class,
                    ERROR_READING_LOGFILE_KEY,
                    logFile),
                    e);
            
            errorLabel.setText(ResourceUtils.getString(
                    InstallationLogDialog.class, ERROR_LOG_CONTENTS));
            
            logScrollPane.setVisible(false);
            errorLabel.setVisible(true);
        }
    }
}
