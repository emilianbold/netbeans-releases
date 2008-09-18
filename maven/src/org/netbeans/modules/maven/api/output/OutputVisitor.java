/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.maven.api.output;

import javax.swing.Action;
import org.openide.windows.OutputListener;

/**
 * Is collecting line parsing information from all the registered Outputprocessors.
 * @author  Milos Kleint
 */
public final class OutputVisitor {
    
    private OutputListener outputListener;
    private Action successAction;
    private boolean important;
    private String line;
    private boolean skipLine = false;
    
    /**
     * property for success Action. Holds question text.
     */
    public static final String ACTION_QUESTION = "Question"; //NOI18N
    /**
     * property for success Action. Priority of the action.
     * From all collected actions one is used (the one with highest
     * priority).
     */
    public static final String ACTION_PRIORITY = "Priority"; //NOI18N
    
    /** Creates a new instance of OutputVisitor */
    public OutputVisitor() {
    }

    /**
     * not to be called by the OutputProcessors.
     */
    public void resetVisitor() {
        outputListener = null;
        successAction = null;
        important = false;
        line = null;
        skipLine = false;
    }
    

    public OutputListener getOutputListener() {
        return outputListener;
    }

    /**
     * add output line highlight and hyperlink via 
     * <code>org.openide.windows.OutputListener</code> instance.
     */
    public void setOutputListener(OutputListener listener) {
        outputListener = listener;
    }
    /**
     * add output line highlight and hyperlink via 
     * <code>org.openide.windows.OutputListener</code> instance.
     * @param isImportant mark the line as important (useful in Nb 4.1 only)
     */
    public void setOutputListener(OutputListener listener, boolean isImportant) {
        setOutputListener(listener);
        important = isImportant;
    }
    
    /**
     * at least one of the <code>OutputProcessor</code>s added a <code>OutputListener</code> and
     * marked it as important.
     */
    public boolean isImportant() {
        return important;
    }

    public Action getSuccessAction() {
        return successAction;
    }

    /**
     * add an action that should be performed when the build finishes.
     * Only one action will be performed, if more than one success actions are 
     * collected during processing, the one with highest value of property
     * ACTION_PRIORITY is performed. 
     * Another property used is ACTION_QUESTION which 
     * holds text for Yes/No question. If user confirms, it's performed.
     */
    public void setSuccessAction(Action sAction) {
        successAction = sAction;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public void skipLine() {
        skipLine = true;
    }
    
    public boolean isLineSkipped() {
        return skipLine;
    }
    
}
