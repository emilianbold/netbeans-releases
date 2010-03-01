/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package dwarfvsmodel;

import java.io.PrintStream;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmFileImplementation;
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
	this(file.getName() + "  " + file.getAbsolutePath(), total, matched, 1); // NOI18N
	this.errors = (file instanceof  FileImpl) ? ((CsmFileImplementation) file).getErrorCount() : 0;
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
