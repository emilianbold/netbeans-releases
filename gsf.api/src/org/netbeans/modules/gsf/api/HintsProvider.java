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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.gsf.api;

import java.util.List;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 * Interface implemented by plugins that wish to provide quickfixes and hints.
 *
 * @author Tor Norbye
 */
public interface HintsProvider {

    /**
     * Compute hints applicable to the given compilation info and add to the given result list.
     */
    void computeHints(CompilationInfo info, List<ErrorDescription> hints);
    
    /**
     * Compute any suggestions applicable to the given caret offset, and add to
     * the given suggestion list.
     */
    void computeSuggestions(CompilationInfo info, List<ErrorDescription> suggestions, int caretOffset);

    /**
     * Compute any suggestions applicable to the given caret offset, and add to
     * the given suggestion list.
     */
    void computeSelectionHints(CompilationInfo info, List<ErrorDescription> suggestions, int start, int end);
    
    /** 
     * Process the errors for the given compilation info, and add errors and
     * warning descriptions into the provided hint list. Return any errors
     * that were not added as error descriptions (e.g. had no applicable error rule)
     */
    List<Error> computeErrors(CompilationInfo info, List<ErrorDescription> hints);

    /**
     * Cancel in-progress processing of hints.
     */
    void cancel();
}
