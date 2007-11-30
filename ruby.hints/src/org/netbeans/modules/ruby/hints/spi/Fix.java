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
package org.netbeans.modules.ruby.hints.spi;

/**
 * Wrapper around org.netbeans.spi.editor.hints.Fix
 *
 * @author Tor Norbye
 */
public interface Fix {
    /**
     * Return the text that is shown in the pop up list for a hint describing each
     * of the available hints.
     * 
     * @return a short (one line) description of the fix
     */
    String getDescription();

    /**
     * Perform the actual hint. Invoked when the user chooses to perform the
     * fix.
     * 
     * @throws java.lang.Exception
     */
    void implement() throws Exception;

    /**
     * Return true if this hint is considered safe (will not change program
     * semantics.)
     * 
     * @return true iff the hint is safe
     */
    boolean isSafe();
    
    /**
     * Return true if and only if this hint requires user interaction when applied.
     * For example, the hint may enter synchronized-editing mode to rename a symbol. 
     * (A command-line driver for the hints will for example not offer this hint
     * as one it can possibly apply automatically.)
     * 
     * @return true iff this hint requires user interaction.
     */
    boolean isInteractive();
}
