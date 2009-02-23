/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.editor.indent.spi;

import java.util.List;
import javax.swing.text.BadLocationException;

/**
 * Indent task performs indentation on a single or multiple lines.
 * <br/>
 * Typically it is used to fix indentation after newline was inserted
 * or to fix indentation for a selected block of code.
 *
 * @author Miloslav Metelka
 */

public interface IndentTask {

    /**
     * Perform reindentation of the line(s) of {@link Context#document()}
     * between {@link Context#startOffset()} and {@link Context#endOffset()}.
     * <br/>
     * It is called from AWT thread and it should process synchronously. It is used
     * after a newline is inserted after the user presses Enter
     * or when a current line must be reindented e.g. when Tab is pressed in emacs mode.
     * <br/>
     * The method should use information from the context and modify
     * indentation at the given offset in the document.
     * 
     * @throws BadLocationException in case the indent task attempted to insert/remove
     *  at an invalid offset or e.g. into a guarded section.
     */
    void reindent() throws BadLocationException;
    
    /**
     * Get an extra locking or null if no extra locking is necessary.
     */
    ExtraLock indentLock();


    /**
     * Enhanced IndentTask capable of creating a formatting context and
     * sharing that context with other ContextAwareIndentTask.
     */
    public interface ContextAwareIndentTask extends IndentTask {

        /**
         * Create formatting context.
         */
        FormattingContext createFormattingContext();

        /**
         * This method is called before indentation starts with formatting
         * contexts from all other ContextAwareIndentTasks which will be called
         * on given document. It allows IndentTasks to discover and communicate
         * with other IndentTask before they are executed on given document.
         */
        void beforeReindent(List<FormattingContext> contexts);
    }
    

    /**
     * Marker interface describing formatting context. It is up to subclasses
     * to define behaviour.
     */
    public interface FormattingContext {

    }

    /**
     * Indent task factory produces indent tasks for the given context.
     * <br/>
     * It should be registered in MimeLookup via xml layer in "/Editors/&lt;mime-type&gt;"
     * folder.
     */
    public interface Factory {

        /**
         * Create indenting task.
         *
         * @param context non-null indentation context.
         * @return indenting task or null if the factory cannot handle the given context.
         */
        IndentTask createTask(Context context);

    }

}
