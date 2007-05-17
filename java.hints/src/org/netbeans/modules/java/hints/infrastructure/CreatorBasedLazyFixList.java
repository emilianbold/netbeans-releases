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
package org.netbeans.modules.java.hints.infrastructure;

import org.netbeans.modules.java.hints.infrastructure.LazyHintComputationFactory;
import com.sun.source.util.TreePath;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
public class CreatorBasedLazyFixList implements LazyFixList {
    
    private PropertyChangeSupport pcs;
    private boolean computed;
    private boolean computing;
    private boolean cancelled;
    private List<Fix> fixes;
    
    private FileObject file;
    private String diagnosticKey;
    private int offset;
    private final Collection<ErrorRule> c;
    private final Map<Class, Data> class2Data;
    
    /** Creates a new instance of CreatorBasedLazyFixList */
    public CreatorBasedLazyFixList(FileObject file, String diagnosticKey, int offset, Collection<ErrorRule> c, Map<Class, Data> class2Data) {
        this.pcs = new PropertyChangeSupport(this);
        this.file = file;
        this.diagnosticKey = diagnosticKey;
        this.offset = offset;
        this.c = c;
        this.class2Data = class2Data;
        this.fixes = Collections.<Fix>emptyList();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    public boolean probablyContainsFixes() {
        return true;
    }
    
    public synchronized List<Fix> getFixes() {
        if (!computed && !computing) {
            LazyHintComputationFactory.addToCompute(file, this);
            computing = true;
        }
        return fixes;
    }
    
    public synchronized boolean isComputed() {
        return computed;
    }
    
    private ErrorRule<?> currentRule;
    
    private synchronized void setCurrentRule(ErrorRule currentRule) {
        this.currentRule = currentRule;
    }
    
    public void compute(CompilationInfo info) {
        synchronized (this) {
            //resume:
            this.cancelled = false;
            
            if (this.computed) {
                return ; //already done.
            }
        }
        
        List<Fix> fixes = new ArrayList<Fix>();
        TreePath path = info.getTreeUtilities().pathFor(offset + 1);
        
        for (ErrorRule rule : c) {
            synchronized (this) {
                if (this.cancelled) {
                    //has been canceled, the computation was not finished:
                    return ;
                }
            }
            
            setCurrentRule(rule);
            
            try {
                Data data = class2Data.get(rule.getClass());
                
                if (data == null) {
                    class2Data.put(rule.getClass(), data = new Data());
                }
                
                List<Fix> currentRuleFixes = rule.run(info, diagnosticKey, offset, path, data);
                
                if (currentRuleFixes != null) {
                    fixes.addAll(currentRuleFixes);
                }
            } finally {
                setCurrentRule(null);
            }
        }
        
        synchronized (this) {
            if (this.cancelled) {
                //has been canceled, the computation was not finished:
                return ;
            }
            this.fixes    = fixes;
            this.computed = true;
        }
        
        pcs.firePropertyChange(PROP_FIXES, null, null);
        pcs.firePropertyChange(PROP_COMPUTED, null, null);
    }
    
    public void cancel() {
        synchronized (this) {
            this.cancelled = true;
            
            if (currentRule != null) {
                currentRule.cancel();
            }
        }
    }
    
}
