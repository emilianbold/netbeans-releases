/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.test.xslt.lib.sequential;

import org.netbeans.jellytools.JellyTestCase;

/**
 *
 * @author ca@netbeans.org
 */

public class SequentialTest extends JellyTestCase {
    private boolean m_completed = false;
    
    public SequentialTest() {
        super("dummy");
    }
    
    public void setupOnce() {
    }
    
    public void setup() {
    }
    
    public boolean needsExecution() {
        return true;
    }
    
    public void execute() {
    }
    
    public void cleanup() {
        setCompleted();
    }
    
    public void cleanupOnce() {
    }
    
    public void finalCleanup() {
    }
    
    protected String getTestName() {
        return "";
    }
    
    public boolean isCompleted() {
        return m_completed;
    }
    
    protected void setCompleted() {
        m_completed = true;
    }
    
    protected void clearCompleted() {
        m_completed = false;
    }
    
}
