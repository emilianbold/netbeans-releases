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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
