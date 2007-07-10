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

package org.netbeans.modules.ruby.rubyproject.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import org.netbeans.modules.ruby.rubyproject.ui.customizer.RubyProjectProperties;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Zezula
 */
public class RubyProjectEncodingQueryImpl extends FileEncodingQueryImplementation implements PropertyChangeListener {
    
    
    private final PropertyEvaluator eval;
    private Charset cache;
    
    /** Creates a new instance of J2SEProjectEncodingQueryImpl */
    public RubyProjectEncodingQueryImpl(final PropertyEvaluator eval) {
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
        String enc = eval.getProperty(RubyProjectProperties.SOURCE_ENCODING);
        synchronized (this) {
            if (cache == null) {
                try {
                    //From discussion with K. Frank the project returns Charset.defaultCharset ()
                    //for older projects (no encoding property). The old project used system encoding => Charset.defaultCharset ()
                    //should work for most users.
                    cache = enc == null ? Charset.defaultCharset() : Charset.forName(enc);
                } catch (IllegalCharsetNameException exception) {
                    return null;
                }
            }
            return cache;
        }
    }
   
    public void propertyChange(PropertyChangeEvent event) {        
        String propName = event.getPropertyName();
        if (propName == null || propName.equals(RubyProjectProperties.SOURCE_ENCODING)) {
            synchronized (this) {
                cache = null;
            }
        }
    }
    
}
