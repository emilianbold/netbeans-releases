/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
                WizardDescriptor.PROP_CONTENT_SELECTED_INDEX,new Integer(index)); // NOI18N
        panels[index].putClientProperty(
                WizardDescriptor.PROP_CONTENT_DATA,getSteps()); // NOI18N
        
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

