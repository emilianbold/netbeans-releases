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
 * @author ks152834
 */
public class CompositeProgress extends Progress implements ProgressListener {
    Map<Progress, Integer> children;
    
    public CompositeProgress() {
        children = new HashMap<Progress, Integer>();
    }
    
    public CompositeProgress(ProgressListener initialListener) {
        this();
        addProgressListener(initialListener);
    }
    
    public CompositeProgress(final Map<Progress, Integer> children) {
        this.children = children;
        
        if (!evaluateChildren()) {
            throw new IllegalArgumentException("The sum of percentages for children cannot exceed" + COMPLETE);
        }
        
        for (Progress child: children.keySet()) {
            child.addProgressListener(this);
        }
    }
    
    public CompositeProgress(final Map<Progress, Integer> children, final ProgressListener initialListener) {
        this(children);
        addProgressListener(initialListener);
    }
    
    public void addChild(Progress progress, int percentage) {
        children.put(progress, percentage);
        
        progress.addProgressListener(this);
        
        if (!evaluateChildren()) {
            throw new IllegalArgumentException("The sum of percentages for children cannot exceed" + COMPLETE);
        }
    }
    
    private boolean evaluateChildren() {
        int percentageSum = 0;
        
        for (Integer value: children.values()) {
            percentageSum += value;
        }
        
        if (percentageSum > COMPLETE) {
            return false;
        }
        
        return true;
    }
    
    public int getPercentage() {
        double percentage = 0.;
        
        for (Progress child: children.keySet()) {
            percentage += (double) child.getPercentage() * children.get(child) / COMPLETE;
        }
        
        return (int) percentage;
    }
    
    public void progressUpdated(Progress progress) {
        notifyListeners();
    }
}
