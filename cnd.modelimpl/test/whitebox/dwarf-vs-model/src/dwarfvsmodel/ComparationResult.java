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
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;

/**
 *
 * @author ak119685
 */
public class ComparationResult {
    private int files;
    private int total;
    private int matched;
    private String label;
    private int errors;

    public ComparationResult(CsmFile file, int total, int matched) {
	this(file.getName() + "  " + file.getAbsolutePath(), total, matched, 1);
	this.errors = (file instanceof  FileImpl) ? ((FileImpl) file).getErrorCount() : 0;
    }

    public ComparationResult(String label, int total, int matched, int files) {
        if (total < matched || total < 0 || matched < 0) {
            throw new IllegalArgumentException("Wrong arguments for result initialization."); // NOI18N
        }
	this.label = label;
        this.total = total;
        this.matched = matched;
        this.files = files;
    }
    
    public void add(ComparationResult result) {
        files += result.files;
        total += result.total;
        matched += result.matched;
	errors += result.errors;
    }
    
//    public void dump(PrintStream out) {
//        if (total == 0) {
//            out.println("Pass rating undefined. Total == 0"); // NOI18N
//        } else {
//            out.printf("\n%d files processed. Pass rating %.2f%% (%d/%d)\n\n", files, (((double)matched) / total * 100), matched, total); // NOI18N
//        }
//    }
    
    public void dump(PrintStream out) {
        if (total == 0) {
            out.printf("%s   n/a  %d of %d  delta %d errors %d\n", label, matched, total, total - matched, errors); // NOI18N
	} else {
            out.printf("%s   %.2f%%  %d of %d  delta %d errors %d\n", label, (((double)matched) / total * 100), matched, total, total - matched, errors); // NOI18N
	}
        if( files > 1 ) {
            out.printf("Files processed: %d\n\n", files); // NOI18N
        }
    }

}
