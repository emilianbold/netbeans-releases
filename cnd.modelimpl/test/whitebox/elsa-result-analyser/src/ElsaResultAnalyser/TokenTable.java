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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nk220367
 */
public class TokenTable {

    String workDir;
    ArrayList<Declaration> variables = new ArrayList<Declaration>();
    ArrayList<Declaration> functions = new ArrayList<Declaration>();
    ArrayList<Declaration> aloneVariableDeclarations = new ArrayList<Declaration>();
    ArrayList<Declaration> aloneFunctionDeclarations = new ArrayList<Declaration>();
    boolean noContainCheck = false;

    TokenTable() {
        this.workDir = null;
    }

    TokenTable(String workDir) {
        this.workDir = workDir;
    }

    public void addVariable(Declaration var) {
        if (workDir != null) {
            if (!var.pos.file.startsWith(workDir)) {
                return;
            }
        }
        if (noContainCheck || !variables.contains(var)) {
            variables.add(var);
        }
    }

    public void addVariableDeclaration(Declaration var, Declaration decl) {
        if (workDir != null) {
            if (!decl.pos.file.startsWith(workDir)) {
                return;
            }
        }
        if (noContainCheck || !var.declarations.contains(decl)) {
            var.declarations.add(decl);
        }
    }

    public void addAloneVariableDeclaration(Declaration var) {
        if (workDir != null) {
            if (!var.pos.file.startsWith(workDir)) {
                return;
            }
        }
        if (noContainCheck || !aloneVariableDeclarations.contains(var)) {
            aloneVariableDeclarations.add(var);
        }
    }

    public void addVariableUsage(Declaration var, Offset usage) {
        if (workDir != null) {
            if (!usage.file.startsWith(workDir)) {
                return;
            }
        }
        if (noContainCheck || !var.usages.contains(usage)) {
            var.usages.add(usage);
        }
    }

    public void addFunction(Declaration fun) {

        if (workDir != null) {
            if (!fun.pos.file.startsWith(workDir)) {
                return;
            }
        }
        if (noContainCheck || !functions.contains(fun)) {
            functions.add(fun);
        }
//        } else {
//            System.out.println("Double function declaration");
//        }
    }

    public void addFunctionDeclaration(Declaration fun, Declaration decl) {
        if (workDir != null) {
            if (!decl.pos.file.startsWith(workDir)) {
                return;
            }
        }
        if (noContainCheck || !fun.declarations.contains(decl)) {
            fun.declarations.add(decl);
        }
    }

    public void addAloneFunctionDeclaration(Declaration fun) {
        if (workDir != null) {
            if (!fun.pos.file.startsWith(workDir)) {
                return;
            }
        }
        if (noContainCheck || !aloneFunctionDeclarations.contains(fun)) {
            aloneFunctionDeclarations.add(fun);
        }
    }

    public void addFunctionUsage(Declaration fun, Offset usage) {
        if (workDir != null) {
            if (!usage.file.startsWith(workDir)) {
                return;
            }
        }
        if (noContainCheck || !fun.usages.contains(usage)) {
            fun.usages.add(usage);
        }
    }

    public Declaration findFunctionDefinition(Offset qualifier, String fullName) {
        for (Declaration declaration : functions) {
            if (declaration.qualifierPos != null && declaration.fullName != null && declaration.qualifierPos.equals(qualifier) && declaration.fullName.equals(fullName)) {
                return declaration;
            }
        }
        return null;
    }

    public Declaration findFunctionDefinition(String fullName) {
        for (Declaration declaration : functions) {
            if (declaration.fullName != null && declaration.fullName.equals(fullName)) {
                return declaration;
            }
        }
        return null;
    }

    public Declaration findFunction(Offset pos, String name) {
        for (Declaration def : functions) {
            if (def.name.equals(name)) {
                if (def.pos.equals(pos)) {
                    return def;
                }
                for (Declaration decl : def.declarations) {
                    if (decl.pos.equals(pos)) {
                        return decl;
                    }
                }
            }
        }
        for (Declaration decl : aloneFunctionDeclarations) {
            if (decl.name.equals(name)) {
                if (decl.pos.equals(pos)) {
                    return decl;
                }
            }
        }

        return null;
    }

    public Declaration findVariableDefinition(Offset qualifier, String fullName) {
        for (Declaration declaration : variables) {
            if (declaration.qualifierPos != null && declaration.fullName != null && declaration.qualifierPos.equals(qualifier) && declaration.fullName.equals(fullName)) {
                return declaration;
            }
        }
        return null;
    }

    public Declaration findVariableDefinition(String fullName) {
        for (Declaration declaration : variables) {
            if (declaration.fullName != null && declaration.fullName.equals(fullName)) {
                return declaration;
            }
        }
        return null;
    }

    public Declaration findVariable(Offset pos, String name) {
        for (Declaration def : variables) {
            if (def.name.equals(name)) {
                if (def.pos.equals(pos)) {
                    return def;
                }
                for (Declaration decl : def.declarations) {
                    if (decl.pos.equals(pos)) {
                        return decl;
                    }
                }
            }
        }
        for (Declaration decl : aloneVariableDeclarations) {
            if (decl.name.equals(name)) {
                if (decl.pos.equals(pos)) {
                    return decl;
                }
            }
        }

        return null;
    }
    int faultsNumber = 0;

    public void verifyDeclaration(String name, Offset offset, int line) {

        if (workDir != null) {
            if (!offset.file.startsWith(workDir)) {
                return;
            }
        }

        for (Declaration var : variables) {
            if (var.name.equals(name)) {
                if (var.namePos.equals(offset) && var.namePos.elsaLine == line) {
                    return;
                }

                for (Offset o : var.usages) {
                    if (o.equals(offset) && o.elsaLine == line) {
                        return;
                    }
                }

                for (Declaration decl : var.declarations) {
                    if (decl.namePos.equals(offset) && decl.namePos.elsaLine == line) {
                        return;
                    }

                    for (Offset o : decl.usages) {
                        if (o.equals(offset) && o.elsaLine == line) {
                            return;
                        }
                    }
                }
            }
        }

        for (Declaration var : aloneVariableDeclarations) {
            if (var.name.equals(name)) {
                if (var.namePos.equals(offset) && var.namePos.elsaLine == line) {
                    return;
                }

                for (Offset o : var.usages) {
                    if (o.equals(offset) && o.elsaLine == line) {
                        return;
                    }
                }
            }
        }

        for (Declaration fun : functions) {
            if (fun.name.equals(name)) {
                if (fun.namePos.equals(offset) && fun.namePos.elsaLine == line) {
                    return;
                }

                for (Offset o : fun.usages) {
                    if (o.equals(offset) && o.elsaLine == line) {
                        return;
                    }
                }

                for (Declaration decl : fun.declarations) {
                    if (decl.namePos.equals(offset) && decl.namePos.elsaLine == line) {
                        return;
                    }

                    for (Offset o : decl.usages) {
                        if (o.equals(offset) && o.elsaLine == line) {
                            return;
                        }
                    }
                }
            }
        }

        for (Declaration fun : aloneFunctionDeclarations) {
            if (fun.name.equals(name)) {
                if (fun.namePos.equals(offset) && fun.namePos.elsaLine == line) {
                    return;
                }

                for (Offset o : fun.usages) {
                    if (o.equals(offset) && o.elsaLine == line) {
                        return;
                    }
                }
            }
        }

        System.out.println("verification fault on " + name + " " + line + " " + offset.file + ":" + offset.line + ":" + offset.row);
        faultsNumber++;
    }

    public void printNumbers() {
        int varNumber = variables.size();
        for (Declaration var : variables) {
            varNumber += var.usages.size();
            varNumber += var.declarations.size();
            for (Declaration decl : var.declarations) {
                varNumber += decl.usages.size();
            }
        }
        varNumber += aloneVariableDeclarations.size();
        for (Declaration var : aloneVariableDeclarations) {
            varNumber += var.usages.size();
        }
        System.out.println("variables: " + varNumber);

        int funNumber = functions.size();
        for (Declaration fun : functions) {
            funNumber += fun.usages.size();
            funNumber += fun.declarations.size();
            for (Declaration decl : fun.declarations) {
                funNumber += decl.usages.size();
            }
        }
        funNumber += aloneFunctionDeclarations.size();
        for (Declaration fun : aloneFunctionDeclarations) {
            funNumber += fun.usages.size();
        }
        System.out.println("functions: " + funNumber);
        if (noContainCheck) {
            System.out.println("faults: " + faultsNumber);
        }
    }

    public void printVariables() {
        System.out.println("Variables:");
        for (Declaration var : variables) {
            if (!var.pos.file.startsWith(workDir)) {
                continue;
            }

            ArrayList<String> offsets = new ArrayList<String>();
            for (Offset o : var.usages) {
                offsets.add(o.file + ":" + o.line + ":" + o.row + ":" + o.elsaLine);
            }

            for (Declaration decl : var.declarations) {
                offsets.add(decl.namePos.file + ":" + decl.namePos.line + ":" + decl.namePos.row + ":" + decl.namePos.elsaLine);
                for (Offset o : decl.usages) {
                    offsets.add(o.file + ":" + o.line + ":" + o.row + ":" + o.elsaLine);
                }
            }
            Collections.sort(offsets);

            System.out.println(var.namePos.file + ":" + var.namePos.line + ":" + var.namePos.row + ":" + var.namePos.elsaLine + " " + var.name.replaceAll("\"", ""));
            for (String string : offsets) {
                System.out.println(string);
            }
        }
        System.out.println("Variable alone declarations:");
        for (Declaration var : aloneVariableDeclarations) {
            if (!var.pos.file.startsWith(workDir)) {
                continue;
            }

            ArrayList<String> offsets = new ArrayList<String>();
            for (Offset o : var.usages) {
                offsets.add(o.file + ":" + o.line + ":" + o.row + ":" + o.elsaLine);
            }

            Collections.sort(offsets);

            System.out.println(var.namePos.file + ":" + var.namePos.line + ":" + var.namePos.row + ":" + var.namePos.elsaLine + " " + var.name.replaceAll("\"", ""));
            for (String string : offsets) {
                System.out.println(string);
            }
        }
    }

    public void dumpVariables(String dir) {
        for (Declaration var : variables) {
            if (!var.pos.file.startsWith(workDir)) {
                continue;
            }

            ArrayList<String> offsets = new ArrayList<String>();
            for (Offset o : var.usages) {
                offsets.add(o.file.substring(workDir.length() + 1) + ":" + o.line + ":" + o.row);
            }

            for (Declaration decl : var.declarations) {
                offsets.add(decl.namePos.file.substring(workDir.length() + 1) + ":" + decl.namePos.line + ":" + decl.namePos.row);
                for (Offset o : decl.usages) {
                    offsets.add(o.file.substring(workDir.length() + 1) + ":" + o.line + ":" + o.row);
                }
            }
            Collections.sort(offsets);

            String fileName = var.namePos.file.substring(workDir.length() + 1) + ":" + var.namePos.line + ":" + var.namePos.row + " " + var.name;

            fileName = fileName.replaceAll("/", "^");
            fileName = fileName.replaceAll("\"", "");

            File f = new File(dir + "/" + fileName);
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(TokenTable.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                PrintStream ps = new PrintStream(f);

                ps.println(var.namePos.file.substring(workDir.length() + 1) + ":" + var.namePos.line + ":" + var.namePos.row + " " + var.name.replaceAll("\"", ""));
                for (String string : offsets) {
                    ps.println(string);
                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(TokenTable.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        for (Declaration var : aloneVariableDeclarations) {
            if (!var.pos.file.startsWith(workDir)) {
                continue;
            }

            ArrayList<String> offsets = new ArrayList<String>();
            for (Offset o : var.usages) {
                offsets.add(o.file.substring(workDir.length() + 1) + ":" + o.line + ":" + o.row);
            }

            Collections.sort(offsets);

            String fileName = var.namePos.file.substring(workDir.length() + 1) + ":" + var.namePos.line + ":" + var.namePos.row + " " + var.name;

            fileName = fileName.replaceAll("/", "^");
            fileName = fileName.replaceAll("\"", "");

            File f = new File(dir + "/" + fileName);
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(TokenTable.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                PrintStream ps = new PrintStream(f);

                ps.println(var.namePos.file.substring(workDir.length() + 1) + ":" + var.namePos.line + ":" + var.namePos.row + " " + var.name.replaceAll("\"", ""));
                for (String string : offsets) {
                    ps.println(string);
                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(TokenTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void printFunctions() {
        System.out.println("Functions:");
        for (Declaration fun : functions) {
            if (!fun.pos.file.startsWith(workDir)) {
                continue;
            }

            ArrayList<String> offsets = new ArrayList<String>();
            for (Offset o : fun.usages) {
                offsets.add(o.file + ":" + o.line + ":" + o.row + ":" + o.elsaLine);
            }

            for (Declaration decl : fun.declarations) {
                offsets.add(decl.namePos.file + ":" + decl.namePos.line + ":" + decl.namePos.row + ":" + decl.namePos.elsaLine);
                for (Offset o : decl.usages) {
                    offsets.add(o.file + ":" + o.line + ":" + o.row + ":" + o.elsaLine);
                }
            }
            Collections.sort(offsets);

//            if (fun.qualifierPos != null && fun.fullName != null) {
//                System.out.println(fun.namePos.file + ":" + fun.namePos.line + ":" + fun.namePos.row + ":" + fun.namePos.elsaLine + " " + fun.name.replaceAll("\"", "") + " " + fun.fullName + " " + fun.qualifierPos.file + ":" + fun.qualifierPos.line + ":" + fun.qualifierPos.row);
//            } else {
            System.out.println(fun.namePos.file + ":" + fun.namePos.line + ":" + fun.namePos.row + ":" + fun.namePos.elsaLine + " " + fun.name.replaceAll("\"", ""));
//            }
            for (String string : offsets) {
                System.out.println(string);
            }
        }
        System.out.println("Function alone declarations:");
        for (Declaration fun : aloneFunctionDeclarations) {
            if (!fun.pos.file.startsWith(workDir)) {
                continue;
            }

            ArrayList<String> offsets = new ArrayList<String>();
            for (Offset o : fun.usages) {
                offsets.add(o.file + ":" + o.line + ":" + o.row + ":" + o.elsaLine);
            }

            Collections.sort(offsets);

            System.out.println(fun.namePos.file + ":" + fun.namePos.line + ":" + fun.namePos.row + ":" + fun.namePos.elsaLine + " " + fun.name.replaceAll("\"", ""));
            for (String string : offsets) {
                System.out.println(string);
            }
        }
    }

    public void dumpFunctions(String dir) {
        for (Declaration fun : functions) {
            if (!fun.pos.file.startsWith(workDir)) {
                continue;
            }

            ArrayList<String> offsets = new ArrayList<String>();
            for (Offset o : fun.usages) {
                offsets.add(o.file.substring(workDir.length() + 1) + ":" + o.line + ":" + o.row);
            }

            for (Declaration decl : fun.declarations) {
                offsets.add(decl.namePos.file.substring(workDir.length() + 1) + ":" + decl.namePos.line + ":" + decl.namePos.row);
                for (Offset o : decl.usages) {
                    offsets.add(o.file.substring(workDir.length() + 1) + ":" + o.line + ":" + o.row);
                }
            }
            Collections.sort(offsets);

            String fileName = fun.namePos.file.substring(workDir.length() + 1) + ":" + fun.namePos.line + ":" + fun.namePos.row + " " + fun.name;
            fileName = fileName.replace("operator/", "operator div");
            fileName = fileName.replace("*", " star");
            fileName = fileName.replace("|", " or");
            fileName = fileName.replace("?", " q");

            fileName = fileName.replaceAll("/", "^");
            fileName = fileName.replaceAll("\"", "");

            File f = new File(dir + "/" + fileName);
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(TokenTable.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                PrintStream ps = new PrintStream(f);

                ps.println(fun.namePos.file.substring(workDir.length() + 1) + ":" + fun.namePos.line + ":" + fun.namePos.row + " " + fun.name.replaceAll("\"", ""));
                for (String string : offsets) {
                    ps.println(string);
                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(TokenTable.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        for (Declaration fun : aloneFunctionDeclarations) {
            if (!fun.pos.file.startsWith(workDir)) {
                continue;
            }

            ArrayList<String> offsets = new ArrayList<String>();
            for (Offset o : fun.usages) {
                offsets.add(o.file.substring(workDir.length() + 1) + ":" + o.line + ":" + o.row);
            }

            Collections.sort(offsets);

            String fileName = fun.namePos.file.substring(workDir.length() + 1) + ":" + fun.namePos.line + ":" + fun.namePos.row + " " + fun.name;
            fileName = fileName.replace("operator/", "operator div");
            fileName = fileName.replace("*", " star");
            fileName = fileName.replace("|", " or");
            fileName = fileName.replace("?", " q");

            fileName = fileName.replaceAll("/", "^");
            fileName = fileName.replaceAll("\"", "");

            File f = new File(dir + "/" + fileName);
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(TokenTable.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                PrintStream ps = new PrintStream(f);

                ps.println(fun.namePos.file.substring(workDir.length() + 1) + ":" + fun.namePos.line + ":" + fun.namePos.row + " " + fun.name.replaceAll("\"", ""));
                for (String string : offsets) {
                    ps.println(string);
                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(TokenTable.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public void removeDuplicateAloneDeclarations() {
        ArrayList<Declaration> newAloneFunctionDeclarations = new ArrayList<Declaration>();
        f:
        for (Declaration declaration : aloneFunctionDeclarations) {
            for (Declaration def : functions) {
                if (def.name.equals(declaration.name)) {
                    if (def.pos.equals(declaration.pos)) {
                        continue;
                    }
                    for (Declaration decl : def.declarations) {
                        if (decl.pos.equals(declaration.pos)) {
                            continue f;
                        }
                    }
                }
            }
            newAloneFunctionDeclarations.add(declaration);
        }
        aloneFunctionDeclarations = newAloneFunctionDeclarations;

        ArrayList<Declaration> newAloneVariableDeclarations = new ArrayList<Declaration>();
        f:
        for (Declaration declaration : aloneVariableDeclarations) {
            for (Declaration def : variables) {
                if (def.name.equals(declaration.name)) {
                    if (def.pos.equals(declaration.pos)) {
                        continue;
                    }
                    for (Declaration decl : def.declarations) {
                        if (decl.pos.equals(declaration.pos)) {
                            continue f;
                        }
                    }
                }
            }
            newAloneVariableDeclarations.add(declaration);
        }
        aloneVariableDeclarations = newAloneVariableDeclarations;
    }

    class MyToken {

        String name;
        Offset offset;
        String description;

        MyToken(String s, Offset o, String d) {
            name = s;
            offset = o;
            description = d;
        }
    }

    class MyFile {

        String name;
        ArrayList<MyToken> tokens = new ArrayList<MyToken>();

        MyFile(String s) {
            name = s;
        }

        public boolean equals(Object o) {
            MyFile f = (MyFile) o;
            return name.equals(f.name);
        }
    }

    private class MyTokenOffsetComparator implements Comparator<MyToken> {

        public int compare(MyToken t1, MyToken t2) {
            int result = compareStrings(t1.offset.file, t2.offset.file);
            if (result == 0) {
                result = (new Integer(t1.offset.line)).compareTo(t2.offset.line);
                if (result == 0) {
                    result = (new Integer(t1.offset.row)).compareTo(t2.offset.row);
                }
            }
            return result;
        }

        private int compareStrings(String s1, String s2) {
            if (s1 == null) {
                s1 = ""; // NOI18N

            }
            if (s2 == null) {
                s2 = ""; // NOI18N

            }
            return s1.compareTo(s2);
        }
    }

    public void dumpIndex(String dir) {
        ArrayList<MyFile> files = new ArrayList<MyFile>();
        for (Declaration fun : functions) {
            MyFile f = new MyFile(fun.pos.file);
            if (files.contains(f)) {
                files.get(files.indexOf(f)).tokens.add(new MyToken(fun.name, fun.namePos, fun.decription));
            } else {
                f.tokens.add(new MyToken(fun.name, fun.namePos, fun.decription));
                files.add(f);
            }

            for (Offset o : fun.usages) {
                f = new MyFile(o.file);
                if (files.contains(f)) {
                    files.get(files.indexOf(f)).tokens.add(new MyToken(fun.name, o, fun.decription + "-usage"));
                } else {
                    f.tokens.add(new MyToken(fun.name, o, fun.decription + "-usage"));
                    files.add(f);
                }
            }

            for (Declaration decl : fun.declarations) {
                f = new MyFile(decl.pos.file);
                if (files.contains(f)) {
                    files.get(files.indexOf(f)).tokens.add(new MyToken(fun.name, decl.namePos, decl.decription));
                } else {
                    f.tokens.add(new MyToken(fun.name, decl.namePos, decl.decription));
                    files.add(f);
                }

                for (Offset o : decl.usages) {
                    f = new MyFile(o.file);
                    if (files.contains(f)) {
                        files.get(files.indexOf(f)).tokens.add(new MyToken(fun.name, o, decl.decription + "-usage"));
                    } else {
                        f.tokens.add(new MyToken(fun.name, o, decl.decription + "-usage"));
                        files.add(f);
                    }
                }
            }
        }

        for (Declaration fun : aloneFunctionDeclarations) {
            MyFile f = new MyFile(fun.pos.file);
            if (files.contains(f)) {
                files.get(files.indexOf(f)).tokens.add(new MyToken(fun.name, fun.namePos, fun.decription));
            } else {
                f.tokens.add(new MyToken(fun.name, fun.namePos, fun.decription));
                files.add(f);
            }

            for (Offset o : fun.usages) {
                f = new MyFile(o.file);
                if (files.contains(f)) {
                    files.get(files.indexOf(f)).tokens.add(new MyToken(fun.name, o, fun.decription + "-usage"));
                } else {
                    f.tokens.add(new MyToken(fun.name, o, fun.decription + "-usage"));
                    files.add(f);
                }
            }
        }

        for (Declaration var : variables) {
            MyFile f = new MyFile(var.pos.file);
            if (files.contains(f)) {
                files.get(files.indexOf(f)).tokens.add(new MyToken(var.name, var.namePos, var.decription));
            } else {
                f.tokens.add(new MyToken(var.name, var.namePos, var.decription));
                files.add(f);
            }

            for (Offset o : var.usages) {
                f = new MyFile(o.file);
                if (files.contains(f)) {
                    files.get(files.indexOf(f)).tokens.add(new MyToken(var.name, o, var.decription + "-usage"));
                } else {
                    f.tokens.add(new MyToken(var.name, o, var.decription + "-usage"));
                    files.add(f);
                }
            }

            for (Declaration decl : var.declarations) {
                f = new MyFile(decl.pos.file);
                if (files.contains(f)) {
                    files.get(files.indexOf(f)).tokens.add(new MyToken(var.name, decl.namePos, decl.decription));
                } else {
                    f.tokens.add(new MyToken(var.name, decl.namePos, decl.decription));
                    files.add(f);
                }

                for (Offset o : decl.usages) {
                    f = new MyFile(o.file);
                    if (files.contains(f)) {
                        files.get(files.indexOf(f)).tokens.add(new MyToken(var.name, o, decl.decription + "-usage"));
                    } else {
                        f.tokens.add(new MyToken(var.name, o, decl.decription + "-usage"));
                        files.add(f);
                    }
                }
            }
        }

        for (Declaration var : aloneVariableDeclarations) {
            MyFile f = new MyFile(var.pos.file);
            if (files.contains(f)) {
                files.get(files.indexOf(f)).tokens.add(new MyToken(var.name, var.namePos, var.decription));
            } else {
                f.tokens.add(new MyToken(var.name, var.namePos, var.decription));
                files.add(f);
            }

            for (Offset o : var.usages) {
                f = new MyFile(o.file);
                if (files.contains(f)) {
                    files.get(files.indexOf(f)).tokens.add(new MyToken(var.name, o, var.decription + "-usage"));
                } else {
                    f.tokens.add(new MyToken(var.name, o, var.decription + "-usage"));
                    files.add(f);
                }
            }
        }

        for (MyFile mf : files) {
            Collections.sort(mf.tokens, new MyTokenOffsetComparator());

            String fileName = mf.name.substring(workDir.length() + 1);
            fileName = fileName.replaceAll("/", ".");
            fileName = fileName.replaceAll("\"", "");

            // System.out.println(fileName);

            File f = new File(dir + "/" + fileName);
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(TokenTable.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                PrintStream ps = new PrintStream(f);

                for (MyToken t : mf.tokens) {
                    //    System.out.println(t.offset.file + ":" + t.offset.line + ":" + t.offset.row + " " + t.name.replaceAll("\"", ""));
                    ps.println(t.offset.line + ":" + t.offset.row + " " + t.name.replaceAll("\"", "") + " " + t.description);
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(TokenTable.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}

