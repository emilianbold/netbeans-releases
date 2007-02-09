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

package org.netbeans.modules.java.j2seproject.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.BinaryForSourceQuery.Result;
import org.netbeans.modules.java.j2seproject.SourceRoots;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public class BinaryForSourceQueryImpl implements BinaryForSourceQueryImplementation {        
    
    
    private final Map<URL,BinaryForSourceQuery.Result>  cache = new HashMap<URL,BinaryForSourceQuery.Result>();
    private final SourceRoots src;
    private final SourceRoots test;
    private final PropertyEvaluator eval;
    private final AntProjectHelper helper;
    
    /** Creates a new instance of BinaryForSourceQueryImpl */
    public BinaryForSourceQueryImpl(SourceRoots src, SourceRoots test, AntProjectHelper helper, PropertyEvaluator eval) {
        assert src != null;
        assert test != null;
        assert helper != null;
        assert eval != null;        
        this.src = src;
        this.test = test;
        this.eval = eval;
        this.helper = helper;
    }
    
    public Result findBinaryRoots(URL sourceRoot) {
        assert sourceRoot != null;
        BinaryForSourceQuery.Result result = cache.get(sourceRoot);
        if (result == null) {
            for (URL root : this.src.getRootURLs()) {
                if (root.equals(sourceRoot)) {
                    result = new R (J2SEProjectProperties.BUILD_CLASSES_DIR);
                    cache.put (sourceRoot,result);
                    break;
                }
            }
            for (URL root : this.test.getRootURLs()) {
                if (root.equals(sourceRoot)) {
                    result = new R (J2SEProjectProperties.BUILD_TEST_CLASSES_DIR);
                    cache.put (sourceRoot,result);
                    break;
                }
            }
        }
        return result;
    }
    
    class R implements BinaryForSourceQuery.Result, PropertyChangeListener {
        
        private final String propName;
        private final List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();
        
        R (final String propName) {
            assert propName != null;
            this.propName = propName;
            eval.addPropertyChangeListener(this);
        }
        
        public URL[] getRoots() {
            String val = eval.getProperty(propName);
            if (val != null) {                
                File f = helper.resolveFile(val);
                if (f != null) {
                    try {
                        return new URL[] {f.toURI().toURL()};
                    } catch (MalformedURLException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            }
            return new URL[0];
        }

        public void addChangeListener(ChangeListener l) {
            assert l != null;
            this.listeners.add (l);
        }

        public void removeChangeListener(ChangeListener l) {
            assert l != null;
            this.listeners.remove (l);
        }

        public void propertyChange(PropertyChangeEvent event) {
            ChangeEvent ce = new ChangeEvent (this);
            for (ChangeListener l : listeners) {
                l.stateChanged(ce);
            }
        }
}

}
