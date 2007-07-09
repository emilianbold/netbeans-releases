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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.editor.hints.common;

import com.sun.source.tree.Tree;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;

/**
 * @author Tomasz.Slota@Sun.COM
 * @author Ajit.Bhate@Sun.COM
 */
public abstract class Rule<E extends Element> {
    
    public final ErrorDescription[] execute(E subject, ProblemContext ctx){
        if (isApplicable(subject, ctx)){
            return apply(subject, ctx);
        }
        
        return null;
    }
    
    /**
     * A rule is applied to an individual element, called subject.
     *
     * @param subject the element where the rule will be applied.
     * @param ctx     additional information passed onto this test.
     * @return a problem object which represents a violation of a rule. It
     *         returns null, if no violation was detected.
     */
    protected abstract ErrorDescription[] apply(E subject, ProblemContext ctx);
    
    protected abstract boolean isApplicable(E subject, ProblemContext ctx);
    
    public static ErrorDescription createProblem(Element subject, ProblemContext ctx,
            String description){
        return createProblem(subject, ctx, description, Severity.ERROR, Collections.<Fix>emptyList());
    }
    
    public static ErrorDescription createProblem(Element subject, ProblemContext ctx,
            String description, Severity severity){
        return createProblem(subject, ctx, description, severity, Collections.<Fix>emptyList());
    }
    
    public static ErrorDescription createProblem(Element subject, ProblemContext ctx, String description,
            Severity severity, Fix fix){
        return createProblem(subject, ctx, description, severity, Collections.singletonList(fix));
    }
    
    public static ErrorDescription createProblem(Element subject, ProblemContext ctx, String description, Fix fix){
        return createProblem(subject, ctx, description, Severity.ERROR, Collections.singletonList(fix));
    }
    
    public static ErrorDescription createProblem(Element subject, ProblemContext ctx,
            String description, Severity severity, List<Fix> fixes){
        ErrorDescription err = null;
        List<Fix> fixList = fixes == null ? Collections.<Fix>emptyList() : fixes;
        
        // by default place error annotation on the element being checked
        Tree elementTree = ctx.getElementToAnnotate() == null ?
            ctx.getCompilationInfo().getTrees().getTree(subject) : ctx.getElementToAnnotate();
        
        if (elementTree != null){
            Utilities.TextSpan underlineSpan = Utilities.getUnderlineSpan(
                    ctx.getCompilationInfo(), elementTree);
            
            err = ErrorDescriptionFactory.createErrorDescription(
                    severity, description, fixList, ctx.getFileObject(),
                    underlineSpan.getStartOffset(), underlineSpan.getEndOffset());
        }
        return err;
    }
}
