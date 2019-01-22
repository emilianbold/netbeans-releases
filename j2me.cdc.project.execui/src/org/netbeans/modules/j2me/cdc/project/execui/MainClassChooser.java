/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2me.cdc.project.execui;

import java.util.Map;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileObject;

/**
 *
 * 
 */
public abstract class MainClassChooser extends JPanel {

    /**
     * Inicialize MainClassChooser to be able to search for main classes
     * @param sourceRoot
     * @param executionModes
     * @param bootcp
     */
    public abstract void inicialize(FileObject sourceRoot, Map<String, String> executionModes, String bootcp);
    
    /**
     * Sets the main class(-es). If more than one main class is selected (applies only for Xlets), then they are divided by ';'
     * @param mainClass which should be highlighted
     */
    public abstract void setSelectedMainClass(String mainClass);
    
    /**
     * @see MainClassChooser.setSelectedMainClass
     * @return selected main class(-es)
     */
    public abstract String getSelectedMainClass();
    
    /**
     * @see MainClassChooser.isAppletExecution. If both methods return false, execution using main(..) method will be used
     * @return true if the main class should be run by Xlet execution
     */
    public abstract boolean isXletExecution();
    
    /**
     * @see MainClassChooser.isXletExecution. If both methods return false, execution using main(..) method will be used
     * @return true if the main class should be run by Applet execution
     */
    public abstract boolean isAppletExecution();  
    
    /**
     * Add listener on changes in chooser
     * @param cl implementation of change listener
     */
    public abstract void addChangeListener(ChangeListener cl);
    
    /**
     * Add listener on changes in chooser
     * @param cl implementation of change listener
     */
    public abstract void removeChangeListener(ChangeListener cl);
}
