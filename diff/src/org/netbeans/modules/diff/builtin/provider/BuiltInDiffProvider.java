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

package org.netbeans.modules.diff.builtin.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.openide.util.NbBundle;

import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.DiffProvider;

/**
 *
 * @author  Martin Entlicher
 */
public class BuiltInDiffProvider extends DiffProvider implements java.io.Serializable {

    /**
     * Holds value of property trimLines.
     */
    private boolean trimLines = true;

    static final long serialVersionUID = 1L;
    
    /** Creates a new instance of BuiltInDiffProvider */
    public BuiltInDiffProvider() {
    }
    
    /**
     * Get the display name of this diff provider.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(BuiltInDiffProvider.class, "BuiltInDiffProvider.displayName");
    }
    
    /**
     * Get a short description of this diff provider.
     */
    public String getShortDescription() {
        return NbBundle.getMessage(BuiltInDiffProvider.class, "BuiltInDiffProvider.shortDescription");
    }
    
    /**
     * Create the differences of the content two streams.
     * @param r1 the first source
     * @param r2 the second source to be compared with the first one.
     * @return the list of differences found, instances of {@link Difference};
     *        or <code>null</code> when some error occured.
     */
    public Difference[] computeDiff(Reader r1, Reader r2) throws IOException {
        return HuntDiff.diff(getLines(r1), getLines(r2), trimLines);   
    }
    
    private String[] getLines(Reader r) throws IOException {
        BufferedReader br = new BufferedReader(r);
        String line;
        List<String> lines = new ArrayList<String>();
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
        return lines.toArray(new String[0]);
    }


    /** On true all lines are trimmed before passing to diff engine. */
    public boolean isTrimLines() {
        return this.trimLines;
    }

    /**
     * Setter for property trimLines.
     * @param trimLines New value of property trimLines.
     */
    public void setTrimLines(boolean trimLines) {
        this.trimLines = trimLines;
    }


    
}
