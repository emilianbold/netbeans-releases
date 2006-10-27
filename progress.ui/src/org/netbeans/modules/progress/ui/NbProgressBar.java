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

package org.netbeans.modules.progress.ui;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import org.netbeans.progress.spi.ExtractedProgressUIWorker;
import org.netbeans.progress.spi.InternalHandle;
import org.netbeans.progress.spi.ProgressEvent;


/**
 * progress component, let just put the UI related issues here, update the state from outside

 * @author mkleint
 */
public class NbProgressBar extends JProgressBar implements ExtractedProgressUIWorker {
    
    static final String SLEEPY = "sleepy"; //NOI18N
    boolean isSetup = false;
    boolean usedInStatusBar = false;
    //TODO these two ought to be created only when the the bar is used externally..
    private JLabel detailLabel = new JLabel();
    private JLabel mainLabel = new JLabel();
    
    /** Creates a new instance of NbProgressBar */
    public NbProgressBar() {
        super();
        setOrientation(JProgressBar.HORIZONTAL);
        setAlignmentX(0.5f);
        setAlignmentY(0.5f);
        Color fg = UIManager.getColor ("nbProgressBar.Foreground");
        if (fg != null) {
            setForeground(fg);
        }
        Color bg = UIManager.getColor ("nbProgressBar.Background");
        if (bg != null) {
            setBackground(bg);
        }
    }
    
    public void setUseInStatusBar(boolean use) {
        usedInStatusBar = use;
    }
    
    public Dimension getPreferredSize() {
        Dimension supers = super.getPreferredSize();
        if (usedInStatusBar) {
            supers.width = ListComponent.ITEM_WIDTH / 3;
        }
        return supers;
    }

    
//--- these are used only when dealing with extracted component, when in status bar this is not used.    
//------------------------------------
    
    public void processProgressEvent(ProgressEvent event) {
        if (event.getType() == ProgressEvent.TYPE_START || !isSetup  || event.isSwitched()) {
            setupBar(event.getSource(), this);
            mainLabel.setText(event.getSource().getDisplayName());
            isSetup = true;
        } 
        if (event.getType() == ProgressEvent.TYPE_PROGRESS) {
            if (event.getWorkunitsDone() > 0) {
                setValue(event.getWorkunitsDone());
            }
            setString(StatusLineComponent.getBarString(event.getPercentageDone(), event.getEstimatedCompletion()));
            if (event.getDisplayName() != null) {
                mainLabel.setText(event.getDisplayName());
            }
            if (event.getMessage() != null) {
                detailLabel.setText(event.getMessage());
            }
            
        } else if (event.getType() == ProgressEvent.TYPE_FINISH) {
            boolean wasIndetermenite = isIndeterminate();
            setIndeterminate(false);
            setMaximum(event.getSource().getTotalUnits());
            setValue(event.getSource().getTotalUnits());
            if (wasIndetermenite) {
                setStringPainted(false);
            } else {
                setString(StatusLineComponent.getBarString(100, -1));
            }
        }
    }

    public void processSelectedProgressEvent(ProgressEvent event) {
        // ignore we'return always processing just one selected component
    }
    
    
    static void setupBar(InternalHandle handle, NbProgressBar bar) {
        bar.putClientProperty(SLEEPY, null); //NIO18N
        int total = handle.getTotalUnits();
        if (handle.isInSleepMode()) {
            bar.setStringPainted(true);
            bar.setIndeterminate(false);
            bar.setMaximum(1);
            bar.setMinimum(0);
            bar.setValue(0);
            bar.putClientProperty(SLEEPY, new Object()); //NIO18N
        } else if (total < 1) {
            // macosx workaround..            
            bar.setValue(bar.getMaximum());
            bar.setIndeterminate(true);
            bar.setStringPainted(false);
        } else {
            bar.setStringPainted(true);
            bar.setIndeterminate(false);
            bar.setMaximum(total);
            bar.setMinimum(0);
            bar.setValue(0);
        }
        bar.setString(" ");
    }    

    public JComponent getProgressComponent() {
        return this;
    }

    public JLabel getMainLabelComponent() {
        return mainLabel;
    }

    public JLabel getDetailLabelComponent() {
        return detailLabel;
    }
}