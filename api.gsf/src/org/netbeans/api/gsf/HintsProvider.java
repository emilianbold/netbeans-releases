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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.gsf;

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
