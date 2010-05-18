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

package test.sfs;

import java.io.IOException;
import java.util.*;
import org.netbeans.modules.cnd.repository.api.RepositoryAccessor;
import org.netbeans.modules.cnd.repository.testbench.sfs.BaseTest;
import org.netbeans.modules.cnd.repository.testbench.sfs.TestIndexSize;
import org.netbeans.modules.cnd.repository.testbench.sfs.TestLongHashMap;
import org.netbeans.modules.cnd.repository.testbench.sfs.TestDataInputOutput;
import org.netbeans.modules.cnd.repository.testbench.sfs.TestSingleFileStorage;
import org.netbeans.modules.cnd.repository.testbench.sfs.ThreadingStress;

/**
 * Main for org.netbeans.modules.cnd.repository.sfs package testing
 * @author Vladimir Kvashin
 */
public class TestMain {
    
    public static void main(String[] args) {
	System.exit(new TestMain().run(args) ? 0 : 1);
    }
    
    private boolean run(String[] args) {
        boolean passed = true;
	
	BaseTest test = new TestSingleFileStorage();
	List<String> params = new ArrayList<String>();
	boolean wait = false;
	
	for (int i = 0; i < args.length; i++) {
	    if( "-s".equals(args[i]) ) { // NOI18N
		 test = new TestDataInputOutput();
	    }
	    else if( "-m".equals(args[i]) ) { // NOI18N
		test = new TestLongHashMap();
	    }
	    else if( "-t".equals(args[i]) ) { // NOI18N
		test = new ThreadingStress();
	    }
	    else if( "-i".equals(args[i]) ) { // NOI18N
		test = new TestIndexSize();
	    }
	    else if( "-w".equals(args[i]) ) { // NOI18N
		wait = true;
	    }
	    else {
		params.add(args[i]);
	    }
	}
	try {
            RepositoryAccessor.getRepository().cleanCaches();
	    passed &= test.test(params);
	}
	catch( Exception e ) {
	    e.printStackTrace(System.err);
	}
	if( wait ) {
	    System.out.printf("Press any key to continue\n"); // NOI18N
	    try {
		System.in.read();
	    } catch (IOException ex) {
	    }
	}
        return passed;
    }    
}
