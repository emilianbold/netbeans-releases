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
 *
 * $Id$
 */
package org.netbeans.installer.utils.progress;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Kirill Sorokin
 */
public class CompositeProgress extends Progress implements ProgressListener {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private List<Progress> childProgresses;
    private List<Integer>  childPercentages;
    
    private boolean synchronizeDetails = false;
    
    // constructors /////////////////////////////////////////////////////////////////
    public CompositeProgress() {
        super();
        
        childProgresses = new LinkedList<Progress>();
        childPercentages = new LinkedList<Integer>();
    }
    
    public CompositeProgress(ProgressListener initialListener) {
        this();
        
        addProgressListener(initialListener);
    }
    
    // progress overrides ///////////////////////////////////////////////////////////
    public int getPercentage() {
        int totalPercentage = 0;
        
        for (int i = 0; i < childProgresses.size(); i++) {
            totalPercentage += childProgresses.get(i).getPercentage() * childPercentages.get(i);
        }
        
        totalPercentage = (totalPercentage / COMPLETE) + percentage;
        
        return totalPercentage;
    }
    
    public void setPercentage(final int percentage) {
        if (!evaluateChildren(percentage)) {
            throw new IllegalArgumentException("The sum of percentages for children cannot exceed" + COMPLETE);
        }
        
        this.percentage = percentage;
    }
    
    public void addPercentage(final int addition) {
        if (!evaluateChildren(percentage + addition)) {
            throw new IllegalArgumentException("The sum of percentages for children cannot exceed" + COMPLETE);
        }
        
        percentage += addition;
    }
    
    public void setCanceled(final boolean canceled) {
        super.setCanceled(canceled);
        
        for (Progress child: childProgresses) {
            child.setCanceled(true);
        }
    }
    
    // composite-specific methods ///////////////////////////////////////////////////
    public void addChild(Progress progress, int percentageChunk) {
        childProgresses.add(progress);
        childPercentages.add(percentageChunk);
        
        progress.addProgressListener(this);
        
        if (!evaluateChildren(percentage)) {
            throw new IllegalArgumentException("The sum of percentages for children cannot exceed" + COMPLETE);
        }
    }
    
    public void synchronizeDetails(boolean synchronizeDetails) {
        this.synchronizeDetails = synchronizeDetails;
    }
    
    // progress listener implementation /////////////////////////////////////////////
    public void progressUpdated(Progress progress) {
        if (synchronizeDetails) {
            setDetail(progress.getDetail());
        }
        
        notifyListeners();
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private boolean evaluateChildren(final int percentage) {
        int totalPercentage = percentage;
        
        for (Integer value: childPercentages) {
            totalPercentage += value;
        }
        
        if (totalPercentage > COMPLETE) {
            return false;
        }
        
        return true;
    }
}
