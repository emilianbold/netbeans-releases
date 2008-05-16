/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
