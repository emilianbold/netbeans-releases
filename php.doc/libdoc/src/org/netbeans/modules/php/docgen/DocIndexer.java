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
package org.netbeans.modules.php.docgen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * @author ads
 *
 */
public class DocIndexer {

    public static void main( String[] args ) throws IOException, WriteException {
        if ( args.length != 2 ) {
            usage();
            System.exit( 1 );
        }
        else {
            File file = new File( args[0] );
            File folder = new File( args[1] );
            if ( !folder.exists() ) {
                folder.mkdirs();
            }
            try {
                CategoriesParser parser = new CategoriesParser( file , folder );
                parser.parse();
            }
            catch (FileNotFoundException e) {
                System.err.println("File " +args[0]+ " is not found");            // NOI18N
                throw e;
            }
            catch (IOException e) {
                System.err.println("Couldn't read file " + args[0]);              // NOI18N
                throw e;
            }
        }
    }
    
    private static void usage() {
        System.err.println("You should use html php doc file as first argument" +
                " and destination folder for output as second"); // NOI18N
    }

}
