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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.etl.ui.view.wizards;

import java.awt.Dimension;
import java.text.MessageFormat;
import java.util.MissingResourceException;

import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

import com.sun.sql.framework.utils.Logger;

/**
 * Abstract subclass of NetBeans wizard descriptor. ETL wizard descriptors should be
 * concrete subclasses of this class.
 */
public abstract class ETLWizardDescriptor extends WizardDescriptor {
    /* Logging category string */
    private static final String LOG_CATEGORY = ETLWizardDescriptor.class.getName();

    /* Manages navigation of wizard panels */
    private WizardDescriptor.Iterator iterator;

    /**
     * Constructs an instance of ETLWizardDescriptor with the given iterator and context.
     * 
     * @param iter WizardDescriptor.Iterator instance to cycle through the panels defined
     *        by this descriptor
     * @param context ETLWizardContext to serve as intermediate storage for data collected
     *        by panels in this descriptor.
     */
    public ETLWizardDescriptor(WizardDescriptor.Iterator iter, Object context) {
        super(iter, context);
        this.iterator = iter;
    }

    /**
     * Overrides parent implementation to set customized look and feel settings.
     * 
     * @see org.openide.WizardDescriptor#initialize
     */
    protected void initialize() {
        initializeLookAndFeel();

        super.initialize();    	        
    }

    /**
     * Initializes look-and-feel of wizard panels.
     */
    protected void initializeLookAndFeel() {

        try {
            // Sets message format used for panel title; {0} indicates component
            // name, if any; {1} indicates step info as provided by iterator.
            setTitleFormat(new MessageFormat(
                NbBundle.getMessage(ETLWizardDescriptor.class, 
                    "MSG_titleformat_wiz_default")));
        } catch (MissingResourceException e) {
            Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, 
                "initializeLookAndFeel()", 
                "Could not locate key for title format.", e);
        }

        // Number the steps.
        putProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N

        // Optional: set the size of the left pane explicitly:
        putProperty("WizardPanel_leftDimension", new Dimension(184, 500)); // NOI18N

        // Optional: show a help tab with special info about the pane:
        // putProperty("WizardPanel_helpDisplayed", Boolean.TRUE); // NOI18N

        // Make the left pane appear:
        putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N

        // Make the left pane show list of steps etc.:
        putProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
    }

    // Called when user moves forward or backward etc.:
    /**
     * Called whenever user chooses a navigation button (back, next, etc.) on the wizard.
     * 
     * @see org.openide.WizardDescriptor#updateState
     */
    protected void updateState() {
        if (iterator instanceof ETLWizardIterator) {
            ETLWizardIterator myIterator = (ETLWizardIterator) iterator;

            // Make the left pane show list of steps etc.:
            putProperty("WizardPanel_contentData", myIterator.getSteps()); // NOI18N
            putProperty("WizardPanel_contentSelectedIndex", new Integer(myIterator.getIndex())); // NOI18N
        }

        super.updateState();
    }
}

