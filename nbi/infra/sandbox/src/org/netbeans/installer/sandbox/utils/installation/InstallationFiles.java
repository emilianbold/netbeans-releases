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
 * $Id$
 */

package org.netbeans.installer.sandbox.utils.installation;

import java.io.File;
import org.netbeans.installer.sandbox.utils.installation.conditions.FileCondition;

/**
 *
 * @author Dmitry Lipin
 */
public interface InstallationFiles {
    public void add(File file);
    public void add(File file, FileCondition cond);
    public void add(InstallationFileObject data);
    public void update(File file);
    public void updateConditions(File file,FileCondition conds);    
    public void delete(File file);
    public void registerCondition(FileCondition fc);
    public InstallationFileObject get(int index);    
    public int size();
    public void setDefaultCondition(FileCondition fc);
    public FileCondition getDefaultCondition();
}
