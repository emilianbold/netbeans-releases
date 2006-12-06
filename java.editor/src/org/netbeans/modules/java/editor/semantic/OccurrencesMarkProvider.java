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
package org.netbeans.modules.java.editor.semantic;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.Document;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.netbeans.modules.editor.highlights.spi.Highlight;

/**
 *
 * @author Jan Lahoda
 */
public class OccurrencesMarkProvider extends MarkProvider {
    
    private static Map<Document, Reference<OccurrencesMarkProvider>> providers = new WeakHashMap<Document, Reference<OccurrencesMarkProvider>>();
    
    public static synchronized OccurrencesMarkProvider get(Document doc) {
        Reference<OccurrencesMarkProvider> ref = providers.get(doc);
        OccurrencesMarkProvider p = ref != null ? ref.get() : null;
        
        if (p == null) {
            providers.put(doc, new WeakReference(p = new OccurrencesMarkProvider()));
        }
        
        return p;
    }
    
    private List<Mark> semantic;
    private List<Mark> occurrences;
    private List<Mark> joint;
    
    /** Creates a new instance of OccurrencesMarkProvider */
    private OccurrencesMarkProvider() {
        semantic = Collections.emptyList();
        occurrences = Collections.emptyList();
        joint = Collections.emptyList();
    }
    
    public synchronized List getMarks() {
        return joint;
    }
    
    public void setSematic(Collection<Highlight> s) {
        List<Mark> old;
        List<Mark> nue;
        
        synchronized (this) {
            semantic = new ArrayList<Mark>();
            
            for (Highlight h : s) {
                if (h != null && h instanceof Mark && ((Mark) h).getEnhancedColor() != null)
                    semantic.add((Mark) h);
            }
            
            old = joint;
            
            nue = joint = new ArrayList<Mark>();
            
            joint.addAll(semantic);
            joint.addAll(occurrences);
        }
        
        //#85919: fire outside the lock:
        firePropertyChange(PROP_MARKS, old, nue);
    }
    
    public void setOccurrences(Collection<Highlight> s) {
        List<Mark> old;
        List<Mark> nue;
        
        synchronized (this) {
            occurrences = new ArrayList<Mark>();
            
            for (Highlight h : s) {
                if (h != null && h instanceof Mark && ((Mark) h).getEnhancedColor() != null)
                    occurrences.add((Mark) h);
            }
            
            old = joint;
            
            nue = joint = new ArrayList<Mark>();
            
            joint.addAll(semantic);
            joint.addAll(occurrences);
        }
        
        //#85919: fire outside the lock:
        firePropertyChange(PROP_MARKS, old, nue);
    }
    
}
