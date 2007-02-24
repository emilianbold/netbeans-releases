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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * File       : IREJavaParser.java
 * Created on : Nov 4, 2003
 * Author     : aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsers.javaparser;

import org.dom4j.Node;

import antlr.RecognitionException;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser;

/**
 * @author aztec
 */
public interface IREJavaParser extends ILanguageParser
{
    public void notifyPackageEvent(Node eventData);
    public void notifyDependencyEvent(Node eventData);
    public void notifyClassEvent(Node eventData);
    public void notifyActionEvent(Node eventData);
    public void notifyError(RecognitionException e);
}
