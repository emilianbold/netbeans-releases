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
package org.netbeans.modules.gsf.api;

import java.util.List;
import org.netbeans.modules.gsf.api.annotations.CheckForNull;
import org.netbeans.modules.gsf.api.annotations.NonNull;


/**
 * Interface for a Parser registered with GSF. A parser takes a parse request
 * and fires parsing events including the parse result at the end of parsing
 * each file.
 * 
 * @author Tor Norbye
 */
public interface Parser {
    /** Parse the given set of files, and notify the parse listener for each transition 
     * (compilation results are attached to the events). The SourceFileReader can be used
     * to get the contents of the files to be parsed.
     */
    void parseFiles(@NonNull Job request);
    
    public final class Job {
        @NonNull public final List<ParserFile> files;
        @NonNull public final ParseListener listener;
        @NonNull public final SourceFileReader reader;
        @CheckForNull public final TranslatedSource translatedSource;

        public Job(@NonNull List<ParserFile> files, 
                @NonNull ParseListener listener,
                @NonNull SourceFileReader reader, 
                @NonNull TranslatedSource translatedSource) {
            this.files = files;
            this.listener = listener;
            this.reader = reader;
            this.translatedSource = translatedSource;
        }
    }
    
    /**
     * Return an object capable of providing source offsets for objects produced by the parser
     */
    @NonNull PositionManager getPositionManager();
}
