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

package org.netbeans.modules.apisupport.project;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import junit.framework.Assert;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;

/**
 * NbTestCase logging error manager.
 * @author Jaroslav Tulach
 */
public class ErrorManagerImpl extends ErrorManager {

    static NbTestCase running;

    private String prefix;

    /** Creates a new instance of ErrorManagerImpl */
    public ErrorManagerImpl() {
        this("[em]");
    }

    private ErrorManagerImpl(String p) {
        this.prefix = p;
    }
    
    public static void registerCase(NbTestCase r) {
        running = r;
    }
    
    public Throwable attachAnnotations(Throwable t, Annotation[] arr) {
        return t;
    }
    
    public Annotation[] findAnnotations(Throwable t) {
        return null;
    }
    
    public Throwable annotate(Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, Date date) {
        return t;
    }
    
    public void notify(int severity, Throwable t) {
        StringWriter w = new StringWriter();
        w.write(prefix);
        w.write(' ');
        t.printStackTrace(new PrintWriter(w));
        
        System.err.println(w.toString());
        
        if (running == null) {
            return;
        }
        running.getLog().println(w.toString());
    }
    
    public void log(int severity, String s) {
        String msg = prefix + ' ' + s;
        if (severity != INFORMATIONAL) {
            System.err.println(msg);
        }
        
        if (running == null) {
            return;
        }
        running.getLog().println(msg);
    }
    
    public ErrorManager getInstance(String name) {
        return new ErrorManagerImpl(name);
    }
    
}
