/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.css.gsf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author marek.fukala@sun.com
 */
public class CssParserResultCslWrapper extends ParserResult {

    private CssParserResult wrappedCssParserResult;
    
    private final List<Error> errors = new ArrayList<Error>();
    private AtomicBoolean analyzerErrorsComputed = new AtomicBoolean(false);

    private boolean invalidated = false;

    public CssParserResultCslWrapper(Snapshot snapshot) {
        super(snapshot);
    }
    
    public CssParserResultCslWrapper(CssParserResult cssParserResult) {
        super(null);
        this.wrappedCssParserResult = cssParserResult;
    }

    public CssParserResult getWrappedCssParserResult() {
        return wrappedCssParserResult;
    }
    
    @Override
    public Snapshot getSnapshot() {
        return wrappedCssParserResult.getSnapshot();
    }
    
    public Node getParseTree() {
        return wrappedCssParserResult.getParseTree();
    }
    
    @Override
    public List<? extends Error> getDiagnostics() {
        if(invalidated) {
            throw new IllegalStateException("The CssParserResult already invalidated!"); //NOI18N
        }
        
        if(!analyzerErrorsComputed.getAndSet(true)) {
            //convert all problem descriptions to the CSL Error-s
            errors.addAll(Css3Utils.getCslErrorForCss3ProblemDescription(
                        wrappedCssParserResult.getSnapshot().getSource().getFileObject(), wrappedCssParserResult.getDiagnostics()));
            
            //and add the "advanced" errors
            //XXX this should be possibly done via hints!
            
//            errors.addAll(CssAnalyser.checkForErrors(getSnapshot(), root));
        }
        
        return errors;
    }
    
    @Override
    protected void invalidate() {
        //the result invalidation must be disabled since some GSF features uses the result outside 
        //the parsing task! This should be fixed.
        
//        invalidated = true; 
    }
    
}
