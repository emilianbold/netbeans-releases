// Copyright (c) Corporation for National Research Initiatives
package org.netbeans.modules.python.editor.scopes;

import java.util.ArrayList;

import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Suite;
import org.python.antlr.ast.Tuple;
import org.python.antlr.ast.argumentsType;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.ast.exprType;
import org.python.antlr.ast.stmtType;

/** Based on org.python.compiler.ArgListCompiler */
public class ArgListCompiler extends Visitor {
    public boolean arglist,  keywordlist;
    public exprType[] defaults;
    public ArrayList<String> names;
    public ArrayList<PythonTree> nodes;
    public ArrayList<String> fpnames;
    public ArrayList<stmtType> init_code;
    private SymbolTable symbolTable;

    public ArgListCompiler(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        arglist = keywordlist = false;
        defaults = null;
        names = new ArrayList<String>();
        nodes = new ArrayList<PythonTree>();
        fpnames = new ArrayList<String>();
        init_code = new ArrayList<stmtType>();
    }

    public void reset() {
        arglist = keywordlist = false;
        defaults = null;
        names.clear();
        nodes.clear();
        init_code.clear();
    }

    public void appendInitCode(Suite node) {
        int n = node.body.length;
        stmtType[] newtree = new stmtType[init_code.size() + n];
        init_code.toArray(newtree);
        System.arraycopy(node.body, 0, newtree, init_code.size(), n);
        node.body = newtree;
    }

    public exprType[] getDefaults() {
        return defaults;
    }

    public void visitArgs(argumentsType args) throws Exception {
        for (int i = 0; i < args.args.length; i++) {
            String name = (String)visit(args.args[i]);
            names.add(name);
            nodes.add(args.args[i]);
            if (args.args[i] instanceof Tuple) {
                Assign ass = new Assign(args.args[i],
                        new exprType[]{args.args[i]},
                        new Name(args.args[i], name, expr_contextType.Load));
                init_code.add(ass);
            }
        }
        if (args.vararg != null) {
            arglist = true;
            names.add(args.vararg);
            //nodes.add(null); // no corresponding node?
            nodes.add(args); // just use the corresponding args node instead
        }
        if (args.kwarg != null) {
            keywordlist = true;
            names.add(args.kwarg);
            //nodes.add(null); // no corresponding node?
            nodes.add(args); // just use the corresponding args node instead
        }

        defaults = args.defaults;
        for (int i = 0; i < defaults.length; i++) {
            if (defaults[i] == null) {
                symbolTable.error("non-default argument follows default argument", true,
                        args.args[args.args.length - defaults.length + i]);
            }
        }
    }

    @Override
    public Object visitName(Name node) throws Exception {
        //FIXME: do we need Store and Param, or just Param?
        if (node.ctx != expr_contextType.Store && node.ctx != expr_contextType.Param) {
            return null;
        }

        if (fpnames.contains(node.id)) {
            symbolTable.error("duplicate argument name found: " +
                    node.id, true, node);
        }
        fpnames.add(node.id);
        return node.id;
    }

    @Override
    public Object visitTuple(Tuple node) throws Exception {
        StringBuffer name = new StringBuffer("(");
        int n = node.elts.length;
        for (int i = 0; i < n - 1; i++) {
            name.append(visit(node.elts[i]));
            name.append(", ");
        }
        name.append(visit(node.elts[n - 1]));
        name.append(")");
        return name.toString();
    }
}
