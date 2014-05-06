/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.implspi;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.parsing.api.Source;

/**
 * Allows to control Source state within the Parsing subsystem based on
 * external events.
 * The {@link SourceEnvironment} gets this instance to forward interesting
 * events to the parser. It is possible to invalidate the source, region of source,
 * 
 * <p/>
 * <b>DO NOT</b> implement this interface; if it is ever promoted to an API,
 * it will become a final class.
 * <p/>
 * @author sdedic
 */
public interface SourceControl {
    /**
     * Provides reference to the Source. The reference is provided for convenience
     * to help proper garbage collection of the Source object. If the client keeps
     * a Source instance, it should use WeakReference to store it.
     * @return Source instance or {@code null}
     */
    public @CheckForNull Source getSource();

    /**
     * Informs that the source was changed in an unspecified way, and possibly
     * its mime was changed.
     * The source will be reparsed and if {@code mimeChanged} is true, the MIME
     * type will be re-read and the appropriate parser will be used for parsing.
     * <p/>
     * The {@code mimeChanged} flag is used for optimization; set it aggressively,
     * so proper parser is looked up.
     * 
     * @param mimeChanged true, if mime type might have changed.
     */
    public void sourceChanged(boolean mimeChanged);

    
    /**
     * Informs that part of the source was edited. The parser implementation
     * may reparse just a portion of the text or a whole (depends on the 
     * implementation). Setting {@code startOffset} or {@code endOffset}
     * to -1 will execute an equivalent of {@code sourceChanged(false)}.
     * 
     * @param startOffset start of the change
     * @param endOffset end of the change
     */
    public void regionChanged(int startOffset, int endOffset);

    /**
     * Resets the parsing state and interrupts the parser.
     */
    public void cancelParsing();

    /**
     * Informs about a non-text change in the Source, such as caret movement
     * or focus. Does not invalidate the parsing result, but may re-execute certain
     * tasks.
     */
    public void stateChanged();
    
    /**
     * Marks the source for reparsing after the specified delay.
     * 
     * @param delay time in milliseconds.
     */
    public void revalidate(int delay);
}
