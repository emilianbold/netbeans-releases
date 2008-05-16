/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ElsaResultAnalyser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
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
//        if (!variables.contains(var)) {
        variables.add(var);
//        } else {
//            System.out.println("Double variable declaration");
//        }
    }

    public void addFunction(Declaration fun) {

        if (workDir != null) {
            if (!fun.pos.file.startsWith(workDir)) {
                return;
            }
        }
//        if (!functions.contains(fun)) {
        functions.add(fun);
//        } else {
//            System.out.println("Double function declaration");
//        }
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
            if (decl.pos.equals(pos)) {
                return decl;
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
            if (decl.pos.equals(pos)) {
                return decl;
            }
        }

        return null;
    }

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

            fileName = fileName.replaceAll("/", ".");
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

            System.out.println(fun.namePos.file + ":" + fun.namePos.line + ":" + fun.namePos.row + fun.namePos.elsaLine + " " + fun.name.replaceAll("\"", ""));
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

            System.out.println(fun.namePos.file + ":" + fun.namePos.line + ":" + fun.namePos.row + fun.namePos.elsaLine + " " + fun.name.replaceAll("\"", ""));
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

            fileName = fileName.replaceAll("/", ".");
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
}

