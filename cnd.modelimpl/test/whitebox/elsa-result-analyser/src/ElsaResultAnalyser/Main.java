/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package ElsaResultAnalyser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nk220367
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            return;
        }


        String workDir = args[0];

        TokenTable table = new TokenTable(workDir);

        for (int i = 1; i < args.length; i++) {
            try {
                BufferedReader in = new BufferedReader(new FileReader(args[i]));
                Parser parser = new Parser(in);
                AstNode tree = parser.parse();

                System.out.println("find variable definitions for " + args[i]);
                tree.findVariableDefinitions(table);
                System.out.println("find function definitions for " + args[i]);
                tree.findFunctionDefinitions(table);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        for (int i = 1; i < args.length; i++) {
            try {
                BufferedReader in = new BufferedReader(new FileReader(args[i]));
                Parser parser = new Parser(in);
                AstNode tree = parser.parse();

                System.out.println("find variable declarations for " + args[i]);
//                for (Declaration decl : table.variables) {
//                    tree.findVariableDeclarations(decl);
//                }
//                System.out.println("find function declarations for " + args[i]);
//                for (Declaration decl : table.functions) {
//                    tree.findFunctionDeclarations(decl);
//                }
                tree.findVariableDeclarations(table, null);
                tree.findFunctionDeclarations(table, null);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        for (int i = 1; i < args.length; i++) {
            try {
                BufferedReader in = new BufferedReader(new FileReader(args[i]));
                Parser parser = new Parser(in);
                AstNode tree = parser.parse();

                System.out.println("find variable usages for " + args[i]);
//                for (Declaration decl : table.variables) {
//                    tree.findVariableUssages(decl);
//                    for (Declaration decl2 : decl.declarations) {
//                        tree.findVariableUssages(decl2);
//                    }
//                }
//
//                System.out.println("find function usages for " + args[i]);
//                for (Declaration decl : table.functions) {
//                    tree.findFunctionUssages(decl);
//                    for (Declaration decl2 : decl.declarations) {
//                        tree.findFunctionUssages(decl2);
//                    }
//                }
                tree.findVariableUssages(table, null);
                tree.findFunctionUssages(table, null);

            } catch (FileNotFoundException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        for (int i = 1; i < args.length; i++) {
            try {
                BufferedReader in = new BufferedReader(new FileReader(args[i]));
                Parser parser = new Parser(in);
                AstNode tree = parser.parse();
                
                tree.verifyUsages(table);

            } catch (FileNotFoundException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        table.printVariables();
        table.printFunctions();
        table.printNumbers();

        table.dumpVariables("variableusages");
        table.dumpFunctions("functionusages");
    }
}
