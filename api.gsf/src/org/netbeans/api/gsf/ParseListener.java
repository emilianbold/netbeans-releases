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
package org.netbeans.api.gsf;

import org.netbeans.api.gsf.Error;


/**
 * Based on the javac one
 * 
 * Provides a listener to monitor the activity of the Sun Java Compiler, javac.
 *
 * @author Jonathan Gibbons
 * @since 1.6
 */
public interface ParseListener
{
    public void started(ParseEvent e);

    public void finished(ParseEvent e);
    
    public void error(Error e);
    
    public void exception(Exception e);
}
