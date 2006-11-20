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
import java.util.Map;

/**
 *
 * @author Kirill Sorokin
 */
public class CompositeProgress extends Progress implements ProgressListener {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private Map<Progress, Integer> children = new HashMap<Progress, Integer>();
    
    private boolean synchronizeDetails = false;
    
    // constructors /////////////////////////////////////////////////////////////////
    public CompositeProgress() {
        // does nothing
    }
    
    public CompositeProgress(ProgressListener initialListener) {
        this();
        
        addProgressListener(initialListener);
    }
    
    // progress overrides ///////////////////////////////////////////////////////////
    public int getPercentage() {
        int totalPercentage = 0;
        
        for (Progress child: children.keySet()) {
            totalPercentage += child.getPercentage() * children.get(child);
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
        
        for (Progress child: children.keySet()) {
            child.setCanceled(true);
        }
    }
    
    // composite-specific methods ///////////////////////////////////////////////////
    public void addChild(Progress progress, int percentageChunk) {
        children.put(progress, percentageChunk);
        
        progress.addProgressListener(this);
        
        if (!evaluateChildren(percentage)) {
            throw new IllegalArgumentException("The sum of percentages for children cannot exceed" + COMPLETE);
        }
    }
    
    public void synchronizeDetails(boolean synchronize) {
        synchronizeDetails = synchronize;
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
        
        for (Integer value: children.values()) {
            totalPercentage += value;
        }
        
        if (totalPercentage > COMPLETE) {
            return false;
        }
        
        return true;
    }
}
