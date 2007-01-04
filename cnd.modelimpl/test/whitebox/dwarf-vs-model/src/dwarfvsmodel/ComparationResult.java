/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package dwarfvsmodel;

import java.io.PrintStream;

/**
 *
 * @author ak119685
 */
public class ComparationResult {
    private int files;
    private int total;
    private int matched;
    
    /** Creates a new instance of ComparationResult */
    public ComparationResult() {
        this(0, 0, 0);
    }
    
    public ComparationResult(int filesProcessed, int total, int matched) {
        if (total < matched || total < 0 || matched < 0) {
            throw new IllegalArgumentException("Wrong arguments for result initialization.");
        }
    
        this.total = total;
        this.matched = matched;
        this.files = filesProcessed;
    }
    
    public void add(ComparationResult result) {
        files += result.files;
        total += result.total;
        matched += result.matched;
    }
    
    public void dump(PrintStream out) {
        if (total == 0) {
            out.println("Pass rating undefined. Total == 0");
        } else {
            out.printf("\n%d files processed. Pass rating %.2f%% (%d/%d)\n\n", files, (((double)matched) / total * 100), matched, total);
        }
    }
}
