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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.spi.java.project.support;

import java.net.URL;
import java.util.Collection;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.LookupMerger;
import org.openide.util.Lookup;


/**
 * Factory class for creation of {@link org.netbeans.spi.project.LookupMerger} instances.
 * @author mkleint
 * @since org.netbeans.modules.java.project 1.14
 */
public final class LookupMergerSupport {

    /**
     * Create a simple instance of LookupMerger for SourceForBinaryQueryImplementation. It takes
     * all implemntations it finds inthe provided lookup and iterates them until a result
     * is found.
     * @return
     */
    public static LookupMerger<SourceForBinaryQueryImplementation> createSFBLookupMerger() {
        return new SFBLookupMerger();
    }

    /**
     * Create a simple instance of LookupMerger for JavadocForBinaryQueryImplementation. It takes
     * all implemntations it finds inthe provided lookup and iterates them until a result
     * is found.
     * @return
     */
    public static LookupMerger<JavadocForBinaryQueryImplementation> createJFBLookupMerger() {
        return new JFBLookupMerger();
    }
    
    private static class SFBLookupMerger implements LookupMerger<SourceForBinaryQueryImplementation> {

        public Class<SourceForBinaryQueryImplementation> getMergeableClass() {
            return SourceForBinaryQueryImplementation.class;
        }

        public SourceForBinaryQueryImplementation merge(Lookup lookup) {
            return new SFBIMerged(lookup);
        }
        
    }
    
    private static class SFBIMerged implements SourceForBinaryQueryImplementation {
        private Lookup lookup;
        
        public SFBIMerged(Lookup lkp) {
            lookup = lkp;
        }
        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            Collection<? extends SourceForBinaryQueryImplementation> col = lookup.lookupAll(SourceForBinaryQueryImplementation.class);
            for (SourceForBinaryQueryImplementation impl : col) {
                SourceForBinaryQuery.Result res = impl.findSourceRoots(binaryRoot);
                if (res != null) {
                    return res;
                }
            }
            return null;
        }
        
    }
    
    private static class JFBLookupMerger implements LookupMerger<JavadocForBinaryQueryImplementation> {

        public Class<JavadocForBinaryQueryImplementation> getMergeableClass() {
            return JavadocForBinaryQueryImplementation.class;
        }

        public JavadocForBinaryQueryImplementation merge(Lookup lookup) {
            return new JFBIMerged(lookup);
        }
        
    }
    
    private static class JFBIMerged implements JavadocForBinaryQueryImplementation {
        private Lookup lookup;
        
        public JFBIMerged(Lookup lkp) {
            lookup = lkp;
        }
        
        public JavadocForBinaryQuery.Result findJavadoc(URL binaryRoot) {
            Collection<? extends JavadocForBinaryQueryImplementation> col = lookup.lookupAll(JavadocForBinaryQueryImplementation.class);
            for (JavadocForBinaryQueryImplementation impl : col) {
                JavadocForBinaryQuery.Result res = impl.findJavadoc(binaryRoot);
                if (res != null) {
                    return res;
                }
            }
            return null;
        }
        
    }
    
}
