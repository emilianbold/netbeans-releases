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
import java.nio.charset.Charset;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Zezula
 */
public class J2SEProjectEncodingQueryImpl extends FileEncodingQueryImplementation implements PropertyChangeListener {
    
    
    private final PropertyEvaluator eval;
    private Charset cache;
    
    /** Creates a new instance of J2SEProjectEncodingQueryImpl */
    public J2SEProjectEncodingQueryImpl(final PropertyEvaluator eval) {
        assert eval != null;
        this.eval = eval;
        this.eval.addPropertyChangeListener(this);
    }
    
    public Charset getEncoding(FileObject file) {
        assert file != null;
        synchronized (this) {
            if (cache != null) {
                return cache;
            }
        }
        String enc = eval.getProperty(J2SEProjectProperties.PROJECT_ENCODING);
        synchronized (this) {
            if (cache == null) {
                cache = enc == null ? FileEncodingQuery.getDefaultEncoding() : Charset.forName(enc);
            }
            return cache;
        }
    }
   
    public void propertyChange(PropertyChangeEvent event) {        
        String propName = event.getPropertyName();
        if (propName == null || propName.equals(J2SEProjectProperties.PROJECT_ENCODING)) {
            synchronized (this) {
                cache = null;
            }
        }
    }
    
}
