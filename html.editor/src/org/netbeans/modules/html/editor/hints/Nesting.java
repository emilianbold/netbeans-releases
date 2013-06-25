/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.hints;

import java.util.regex.Pattern;
import org.netbeans.modules.csl.api.HintSeverity;

/**
 *
 * @author marekfukala
 */
public class Nesting extends PatternRule {

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }

    private static final String[] PATTERNS_SOURCES = new String[]{
        "Heading cannot be a child of another heading.",
        ".*? start tag found but the .*? element is already open.",
        "Unclosed elements inside a list.",
        "An .*? start tag seen with already an active .*? element.",
        ".*? start tag seen when there was an open .*? element in scope.",
        "Start tag for .*? seen when .*? was already open.",
        "Bad start tag in .*? in",
        ".*? start tag with .*? open",
        ".*? start tag where end tag expected.",
        ".*? start tag seen in",
        ".*? element outside",
        ".*? element between",
        "Unclosed elements on stack.",
        "No .*? element in scope but a .*? end tag seen.",
        "No .*? element in list scope",
        "Saw an end tag after .*? had been closed.",
        "No element .*? to close.",
        "End tag .*? violates nesting rules.",
        "Element .*? not allowed as child of element .*? in this context.",
        "XHTML element .*? is missing a required instance of child element .*?"
        
        
    }; //NOI18N
    
    private final static Pattern[] PATTERNS = buildPatterns(PATTERNS_SOURCES);

    @Override
    public Pattern[] getPatterns() {
        return PATTERNS;
    }
    
}
