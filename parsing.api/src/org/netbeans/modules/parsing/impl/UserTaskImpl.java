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

package org.netbeans.modules.parsing.impl;

import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.MultiLanguageUserTask;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;


/**
 * This {@link MultiLanguageUserTask} implementation implements 
 * {@link UserTask} call based on {@link MultiLanguageUserTask} call.
 * 
 * @author Jan Jancura
 */
public class UserTaskImpl extends MultiLanguageUserTask {

    private Source          source;
    private UserTask        userTask;
    private int             offset;
    
    public UserTaskImpl (
        Source              source,
        UserTask            userTask, 
        int                 offset
    ) {
        this.source =       source;
        this.userTask =     userTask;
        this.offset =       offset;
    }
    
    @Override
    public void run (ResultIterator resultIterator) throws Exception {
        run (resultIterator, source);
    }
    
    private void run (ResultIterator resultIterator, Source source) throws Exception {
        for (Embedding embedding : resultIterator.getEmbeddings ()) {
            if (embedding.containsOriginalOffset (offset)) {
                run (resultIterator.getResultIterator (embedding), source);
                return;
            }
        }
        userTask.run (resultIterator.getParserResult (), resultIterator.getSnapshot ());
    }
}
