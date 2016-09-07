/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.antlr.preprocessor;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

import java.io.*;
import org.netbeans.modules.cnd.antlr.collections.impl.Vector;
import java.util.Enumeration;

/** Tester for the preprocessor */
public class Tool {
    protected Hierarchy theHierarchy;
    protected String grammarFileName;
    protected String[] args;
    protected int nargs;		// how many args in new args list
    protected Vector grammars;
    protected org.netbeans.modules.cnd.antlr.Tool antlrTool;

    public Tool(org.netbeans.modules.cnd.antlr.Tool t, String[] args) {
        antlrTool = t;
        processArguments(args);
    }

    public static void main(String[] args) {
        org.netbeans.modules.cnd.antlr.Tool antlrTool = new org.netbeans.modules.cnd.antlr.Tool();
        Tool theTool = new Tool(antlrTool, args);
        theTool.preprocess();
        String[] a = theTool.preprocessedArgList();
        for (int i = 0; i < a.length; i++) {
            System.out.print(" " + a[i]);
        }
        System.out.println();
    }

    public boolean preprocess() {
        if (grammarFileName == null) {
            antlrTool.toolError("no grammar file specified");
            return false;
        }
        if (grammars != null) {
            theHierarchy = new Hierarchy(antlrTool);
            for (Enumeration e = grammars.elements(); e.hasMoreElements();) {
                String f = (String)e.nextElement();
                try {
                    theHierarchy.readGrammarFile(f);
                }
                catch (FileNotFoundException fe) {
                    antlrTool.toolError("file " + f + " not found");
                    return false;
                }
            }
        }

        // do the actual inheritance stuff
        boolean complete = theHierarchy.verifyThatHierarchyIsComplete();
        if (!complete)
            return false;
        theHierarchy.expandGrammarsInFile(grammarFileName);
        GrammarFile gf = theHierarchy.getFile(grammarFileName);
        String expandedFileName = gf.nameForExpandedGrammarFile(grammarFileName);

        // generate the output file if necessary
        if (expandedFileName.equals(grammarFileName)) {
            args[nargs++] = grammarFileName;			// add to argument list
        }
        else {
            try {
                gf.generateExpandedFile(); 				// generate file to feed ANTLR
                args[nargs++] = antlrTool.getOutputDirectory() +
                    System.getProperty("file.separator") +
                    expandedFileName;		// add to argument list
            }
            catch (IOException io) {
                antlrTool.toolError("cannot write expanded grammar file " + expandedFileName);
                return false;
            }
        }
        return true;
    }

    /** create new arg list with correct length to pass to ANTLR */
    public String[] preprocessedArgList() {
        String[] a = new String[nargs];
        System.arraycopy(args, 0, a, 0, nargs);
        args = a;
        return args;
    }

    /** Process -glib options and grammar file.  Create a new args list
     *  that does not contain the -glib option.  The grammar file name
     *  might be modified and, hence, is not added yet to args list.
     */
    private void processArguments(String[] incomingArgs) {
		 this.nargs = 0;
		 this.args = new String[incomingArgs.length];
		 for (int i = 0; i < incomingArgs.length; i++) {
			 if ( incomingArgs[i].length() == 0 )
			 {
				 antlrTool.warning("Zero length argument ignoring...");
				 continue;
			 }
			 if (incomingArgs[i].equals("-glib")) {
				 // if on a pc and they use a '/', warn them
				 if (File.separator.equals("\\") &&
					  incomingArgs[i].indexOf('/') != -1) {
					 antlrTool.warning("-glib cannot deal with '/' on a PC: use '\\'; ignoring...");
				 }
				 else {
					 grammars = antlrTool.parseSeparatedList(incomingArgs[i + 1], ';');
					 i++;
				 }
			 }
			 else if (incomingArgs[i].equals("-o")) {
				 args[this.nargs++] = incomingArgs[i];
				 if (i + 1 >= incomingArgs.length) {
					 antlrTool.error("missing output directory with -o option; ignoring");
				 }
				 else {
					 i++;
					 args[this.nargs++] = incomingArgs[i];
					 antlrTool.setOutputDirectory(incomingArgs[i]);
				 }
			 }
			 else if (incomingArgs[i].charAt(0) == '-') {
				 args[this.nargs++] = incomingArgs[i];
			 }
			 else {
				 // Must be the grammar file
				 grammarFileName = incomingArgs[i];
				 if (grammars == null) {
					 grammars = new Vector(10);
				 }
				 grammars.appendElement(grammarFileName);	// process it too
				 if ((i + 1) < incomingArgs.length) {
					 antlrTool.warning("grammar file must be last; ignoring other arguments...");
					 break;
				 }
			 }
		 }
    }
}
