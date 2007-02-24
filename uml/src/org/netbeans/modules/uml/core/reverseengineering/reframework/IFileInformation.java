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

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public interface IFileInformation
{
    public ETList<IDependencyEvent> getDependencies();
    public ETList<IErrorEvent> getErrors();
    public IErrorEvent getError(int index);
    public int getTotalErrors();
    public void addError(IErrorEvent e);
    public IREClass getClass(int index);
    public void addClass(IREClass newVal);
    public int getTotalClasses();
    public IDependencyEvent getDependency(int index);
    public void addDependency(IDependencyEvent newVal);
    public int getTotalDependencies();
    public int getTotalPackages();
    public void addPackage(IPackageEvent newVal);
    public IPackageEvent getPackage(int index);
}