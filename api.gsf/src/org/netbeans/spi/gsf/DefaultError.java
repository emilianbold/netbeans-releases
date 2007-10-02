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
package org.netbeans.spi.gsf;

import org.netbeans.api.gsf.Error;
import org.netbeans.api.gsf.Position;
import org.netbeans.api.gsf.Severity;
import org.netbeans.api.gsf.annotations.NonNull;
import org.netbeans.api.gsf.annotations.Nullable;
import org.openide.filesystems.FileObject;


/**
 * Simple implementation of the Error interface, which can be used for convenience
 * when generating errors during (for example) program parsing.
 *
 * @author Tor Norbye
 */
public class DefaultError implements Error {
    private String displayName;
    private String description;

    //private List<Fix> fixes;
    private FileObject file;
    private Position start;
    private Position end;
    private String key;
    private Severity severity;
    private Object[] parameters;

    /** Creates a new instance of DefaultError */
    public DefaultError(@Nullable
    String key, @NonNull
    String displayName, @Nullable
    String description, @NonNull
    FileObject file, @NonNull
    Position start, @NonNull
    Position end, @NonNull
    Severity severity) {
        this.key = key;
        this.displayName = displayName;
        this.description = description;
        this.file = file;
        this.start = start;
        this.end = end;
        this.severity = severity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    //public void addFix(Fix fix) {
    //    if (fixes == null) {
    //        fixes = new ArrayList<Fix>(5);
    //    }
    //    fixes.add(fix);
    //}
    //
    //public List<Fix> getFixes() {
    //    return Collections.unmodifiableList(fixes);
    //}

    public Position getStartPosition() {
        return start;
    }

    public Position getEndPosition() {
        return end;
    }

    public String toString() {
        return "DefaultError[" + displayName + ", " + description + ", " + severity + "]";
    }

    public String getKey() {
        return key;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(final Object[] parameters) {
        this.parameters = parameters;
    }

    public Severity getSeverity() {
        return severity;
    }

    public FileObject getFile() {
        return file;
    }
}
