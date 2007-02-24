/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.uml.reporting.wizard;

import java.awt.Dimension;
import java.text.MessageFormat;
import javax.swing.event.*;
import org.netbeans.modules.uml.reporting.StateChangeSupport;
import org.openide.*;
import org.openide.util.*;

public class WebReportWizardIterator implements WizardDescriptor.Iterator {
    /**
     *
     *
     */
    public WebReportWizardIterator(ReportWizardSettings settings) {
        super();

        panels = new WizardPanelBase[] {
			new ReportLocationPanel()
        };
        
    }
    
    
    /**
     *
     *
     */
    public String name() {
		return MessageFormat.format (NbBundle.getMessage(
				WebReportWizardIterator.class,"TITLE_WebReportWizardIterator_wizardTitle"),
            new Object[] {new Integer (index + 1), new Integer (panels.length) }); 
    }
    
    
    /**
     *
     *
     */
    public WizardDescriptor.Panel current() {
        updateCurrentPanel();
        return panels[index];
    }
    
    
    /**
     *
     *
     */
    public int getIndex() {
        return index;
    }
    
    
    /**
     *
     *
     */
    public void setIndex(int value) {
        index = value;
        changeSupport.fireStateChanged();
    }
    
    
    /**
     *
     *
     */
    protected void updateCurrentPanel() {
        panels[index].putClientProperty(
                "WizardPanel_contentSelectedIndex",new Integer(index)); // NOI18N
        panels[index].putClientProperty(
                "WizardPanel_contentData",getSteps()); // NOI18N
        
    }
    
    
    /**
     *
     *
     */
    public String[] getSteps() {
        String[]   steps=new String[panels.length];
        for (int i=0; i<steps.length; i++)
            steps[i]=panels[i].getName();
 
        return steps;
    }
    
    
    /**
     *
     *
     */
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    
    
    /**
     *
     *
     */
    public boolean hasPrevious() {
        return index > 0;
    }
    
    
    /**
     *
     *
     */
    public void nextPanel() {
        if ( hasNext() ) {
            index++;
            changeSupport.fireStateChanged();
        }
    }
    
    
    /**
     *
     *
     */
    public void previousPanel() {
        if ( hasPrevious() ) {   
            index--;         
            changeSupport.fireStateChanged();
        }
    }
    
    
    /**
     *
     *
     */
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addListener(listener);
    }
    
    
    /**
     *
     *
     */
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeListener(listener);
    }
    
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    
    private WizardPanelBase[] panels;

    
    Dimension preferredSize;
    
    private StateChangeSupport changeSupport = new StateChangeSupport( this );
    
    private int index;
}

