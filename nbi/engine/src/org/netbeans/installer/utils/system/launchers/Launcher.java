/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */


package org.netbeans.installer.utils.system.launchers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.utils.helper.JavaCompatibleProperties;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 * @author Dmitry Lipin
 */
public abstract class Launcher extends LauncherProperties {
    
    protected Launcher(LauncherProperties pr) {
        super(pr);
    }
    public abstract File create(Progress progress) throws IOException;
    public abstract void initialize() throws IOException;
    public abstract String [] getExecutionCommand();
    public abstract List <JavaCompatibleProperties> getDefaultCompatibleJava();    
    public abstract String getExtension();
    protected abstract String getI18NResourcePrefix();
    
}
