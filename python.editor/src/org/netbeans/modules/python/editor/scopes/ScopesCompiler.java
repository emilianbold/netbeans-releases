// (C) Copyright 2001 Samuele Pedroni
package org.netbeans.modules.python.editor.scopes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.netbeans.modules.python.editor.AstPath;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Delete;
import org.python.antlr.ast.Exec;
import org.python.antlr.ast.Expression;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.GeneratorExp;
import org.python.antlr.ast.Global;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Interactive;
import org.python.antlr.ast.Lambda;
import org.python.antlr.ast.ListComp;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.With;
import org.python.antlr.ast.Yield;
import org.python.antlr.ast.argumentsType;
import org.python.antlr.ast.exprType;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.ast.stmtType;

/** 
 * Based on org.python.compiler.ScopesCompiler in Jython
 *
 * Modifications I've made:
 * - Methods for finding all the free variables
 * - Methods for identifying unused bound variables
 * - Track whether symbols are referenced as calls or not
 *   (so I can determine whether to look in the index for
 *    functions or data etc. when trying to resolve imports)
 * - Track variable reads/writes
 * - Track imports etc.
 * - Add nodes to each SymInfo
 * - Replace old style Java (Hashtable, Vector, implements ScopeConstants) with
 *   modern Java (HashMap, ArrayList, import static)
 * 
 */
@SuppressWarnings("unchecked")
public class ScopesCompiler extends Visitor implements ScopeConstants {
    private SymbolTable symbolTable;
    private Stack scopes;
    private ScopeInfo cur = null;
    private Map<PythonTree, ScopeInfo> nodeScopes;
    private int level = 0;
    private int func_level = 0;
    private List<Import> imports;
    private List<PythonTree> mainImports;
    private List<ImportFrom> importsFrom;
    private Set<PythonTree> topLevelImports;
    private PythonTree root;
    private PythonTree parent;
    private AstPath path = new AstPath();
    /** List of symbols registered via __all__ = [ "foo", "bar" ] or __all__.extend() or __all__.append() */
    private List<Str> publicSymbols;
    /** Set to true if we encountered manipulation on __all__ that I don't understand */
    private boolean invalidPublicSymbols;

    public ScopesCompiler(SymbolTable symbolTable, Map<PythonTree, ScopeInfo> nodeScopes, PythonTree root,
            List<Import> imports, List<ImportFrom> importsFrom, List<PythonTree> mainImports, Set<PythonTree> topLevelImports) {
        this.symbolTable = symbolTable;
        this.nodeScopes = nodeScopes;
        scopes = new Stack();
        this.root = root;

        this.imports = imports;
        this.importsFrom = importsFrom;
        this.mainImports = mainImports;
        this.topLevelImports = topLevelImports;
    }

    @Override
    public void traverse(PythonTree node) throws Exception {
        // Jython's parser often doesn't set the parent references correctly
        // so try to fix that here
        node.parent = parent;

        PythonTree oldParent = parent;
        parent = node;

        path.descend(node);
        super.traverse(node);
        parent = oldParent;
        path.ascend();
    }

    public void beginScope(String name, int kind, PythonTree node,
            ArgListCompiler ac) {
        if (cur != null) {
            scopes.push(cur);
        }
        if (kind == FUNCSCOPE) {
            func_level++;
        }
        cur = new ScopeInfo(name, node, level++, kind, func_level, ac);
        nodeScopes.put(node, cur);
    }

    public void endScope() throws Exception {
        if (cur.kind == FUNCSCOPE) {
            func_level--;
        }
        level--;
        ScopeInfo up = null;
        if (!scopes.empty()) {
            up = (ScopeInfo)scopes.pop();
        }
        //Go into the stack to find a non class containing scope to use making the closure
        //See PEP 227
        int dist = 1;
        ScopeInfo referenceable = up;
        for (int i = scopes.size() - 1; i >= 0 && referenceable.kind == CLASSSCOPE; i--, dist++) {
            referenceable = ((ScopeInfo)scopes.get(i));
        }
        cur.cook(referenceable, dist, symbolTable);
//        cur.dump(); // debug
        cur = up;
    }

    public void parse() throws Exception {
        try {
            visit(root);
        } catch (Throwable t) {
            Exceptions.printStackTrace(t);
            //throw org.python.core.ParserFacade.fixParseError(null, t,
            //        code_compiler.getFilename());
        }
    }

    @Override
    public Object visitInteractive(Interactive node) throws Exception {
        beginScope("<single-top>", TOPSCOPE, node, null);
        PythonTree oldParent = parent;
        parent = node;
        suite(node.body);
        parent = oldParent;
        endScope();
        return null;
    }

    @Override
    public Object visitModule(org.python.antlr.ast.Module node)
            throws Exception {
        if (node.body != null && node.body.length > 0) {
            stmtType[] body = node.body;
            boolean foundFirst = false;
            for (int i = 0; i < body.length; i++) {
                stmtType stmt = body[i];
                if (stmt != null) {
                    if (stmt instanceof Import || stmt instanceof ImportFrom) {
                        if (!foundFirst) {
                            foundFirst = true;
                        }
                        mainImports.add(stmt);
                    } else if (foundFirst) {
                        break;
                    }
                }
            }
        }

        beginScope("<file-top>", TOPSCOPE, node, null);

        PythonTree oldParent = parent;
        parent = node;
        suite(node.body);
        parent = oldParent;

        endScope();
        return null;
    }

    @Override
    public Object visitExpression(Expression node) throws Exception {
        beginScope("<eval-top>", TOPSCOPE, node, null);
        visit(new Return(node, node.body));
        endScope();
        return null;
    }

    private void def(String name, int extraFlags, PythonTree node) {
        SymInfo info = cur.addBound(name, node);
        // <netbeans>
        info.flags |= (DEF | extraFlags);
        info.node = node;
        // </netbeans>
    }

    @Override
    public Object visitAssign(Assign node) throws Exception {
        if (node.targets != null && node.targets.length == 1 &&
                node.targets[0] instanceof Name) {
            Name lhs = (Name)node.targets[0];
            if ("__all__".equals(lhs.id)) { // NOI18N
                if (!invalidPublicSymbols && node.value instanceof org.python.antlr.ast.List) {
                    org.python.antlr.ast.List allList = (org.python.antlr.ast.List)node.value;
                    if (allList != null && allList.elts != null && allList.elts.length > 0) {
                        for (exprType expr : allList.elts) {
                            if (expr instanceof Str) {
                                Str str = (Str)expr;
                                if (publicSymbols == null) {
                                    publicSymbols = new ArrayList<Str>();
                                }
                                publicSymbols.add(str);
                            } else {
                                invalidPublicSymbols = true;
                            }
                        }
                    }
                } else {
                    invalidPublicSymbols = true;
                }
            }
        }

        if (node.targets != null && node.targets.length > 0) {
            Name[] names = new Name[node.targets.length];
            boolean valid = true;
            for (int i = 0, n = node.targets.length; i < n; i++) {
                exprType et = node.targets[i];
                if (et instanceof Name) {
                    Name name = (Name)et;
                    names[i] = name;
                } else {
                    valid = false;
                }
            }
            if (valid) {
                if (node.value instanceof Name) {
                    Name value = (Name)node.value;

                    SymInfo rhsSym = cur.tbl.get(value.id);
                    if (rhsSym != null && rhsSym.isDef()) {
                        for (Name name : names) {
                            visitName(name);
                            SymInfo sym = cur.tbl.get(name.id);
                            if (sym != null) {
                                sym.flags |= ALIAS;
                                sym.flags |= (rhsSym.flags & (CLASS | FUNCTION));
                                sym.node = rhsSym.node;
                            }
                        }
                    }
                }
            }
        }

        return super.visitAssign(node);
    }

    @Override
    public Object visitFunctionDef(FunctionDef node) throws Exception {
        def(node.name, FUNCTION, node);
        ArgListCompiler ac = new ArgListCompiler(symbolTable);
        ac.visitArgs(node.args);

        exprType[] defaults = ac.getDefaults();
        for (int i = 0; i < defaults.length; i++) {
            visit(defaults[i]);
        }

        exprType[] decs = node.decorators;
        for (int i = decs.length - 1; i >= 0; i--) {
            visit(decs[i]);
        }
        ScopeInfo parentScope = cur;
        beginScope(node.name, FUNCSCOPE, node, ac);
        cur.nested = parentScope;

        int n = ac.names.size();
        for (int i = 0; i < n; i++) {
            cur.addParam(ac.names.get(i), ac.nodes.get(i));
        }
        for (int i = 0; i < ac.init_code.size(); i++) {
            visit(ac.init_code.get(i));
        }
        cur.markFromParam();

        PythonTree oldParent = parent;
        parent = node;
        suite(node.body);
        parent = oldParent;

        endScope();
        return null;
    }

    @Override
    public Object visitLambda(Lambda node) throws Exception {
        ArgListCompiler ac = new ArgListCompiler(symbolTable);
        ac.visitArgs(node.args);

        PythonTree[] defaults = ac.getDefaults();
        for (int i = 0; i < defaults.length; i++) {
            visit(defaults[i]);
        }

        beginScope("<lambda>", FUNCSCOPE, node, ac);
        assert ac.names.size() == ac.nodes.size();
        for (int i = 0; i < ac.names.size(); i++) {
            cur.addParam(ac.names.get(i), ac.nodes.get(i));
        }
        for (Object o : ac.init_code) {
            visit((stmtType)o);
        }
        cur.markFromParam();
        visit(node.body);
        endScope();
        return null;
    }

    public void suite(stmtType[] stmts) throws Exception {
        for (int i = 0; i < stmts.length; i++) {
            path.descend(stmts[i]);
            visit(stmts[i]);
            path.ascend();
        }
    }

    @Override
    public Object visitImport(Import node) throws Exception {
        if (parent == root) {
            topLevelImports.add(node);
        }
        imports.add(node);

        for (int i = 0; i < node.names.length; i++) {
            if (node.names[i].asname != null) {
                SymInfo entry = cur.addBound(node.names[i].asname, node);
                entry.flags |= IMPORTED;
            } else {
                String name = node.names[i].name;
                if (name.indexOf('.') > 0) {
                    name = name.substring(0, name.indexOf('.'));
                }
                SymInfo entry = cur.addBound(name, node);
                entry.flags |= IMPORTED;
            }
        }
        return null;
    }

    @Override
    public Object visitImportFrom(ImportFrom node) throws Exception {
        if (parent == root) {
            topLevelImports.add(node);
        }
        importsFrom.add(node);

        //Future.checkFromFuture(node); // future stmt support
        int n = node.names.length;
        if (n == 0) {
            cur.from_import_star = true;
            return null;
        }
        for (int i = 0; i < n; i++) {
            if (node.names[i].asname != null) {
                SymInfo entry = cur.addBound(node.names[i].asname, node);
                entry.flags |= IMPORTED;
            } else {
                SymInfo entry = cur.addBound(node.names[i].name, node);
                entry.flags |= IMPORTED;
            }
        }
        return null;
    }

    @Override
    public Object visitGlobal(Global node) throws Exception {
        int n = node.names.length;
        for (int i = 0; i < n; i++) {
            String name = node.names[i];
            int prev = cur.addGlobal(name, node);
            if (prev >= 0) {
                if ((prev & FROM_PARAM) != 0) {
                    symbolTable.error("name '" + name + "' is local and global", true, node);
                }
                if ((prev & GLOBAL) != 0) {
                    continue;
                }
                String what;
                if ((prev & BOUND) != 0) {
                    what = "assignment";
                } else {
                    what = "use";
                }
                symbolTable.error("name '" + name + "' declared global after " + what, false, node);
            }
        }
        return null;
    }

    @Override
    public Object visitExec(Exec node) throws Exception {
        cur.exec = true;
        if (node.globals == null && node.locals == null) {
            cur.unqual_exec = true;
        }
        traverse(node);
        return null;
    }

    @Override
    public Object visitClassDef(ClassDef node) throws Exception {
        def(node.name, CLASS, node);
        int n = node.bases.length;
        for (int i = 0; i < n; i++) {
            visit(node.bases[i]);
        }
        ScopeInfo parentScope = cur;
        beginScope(node.name, CLASSSCOPE, node, null);
        cur.nested = parentScope;
        PythonTree oldParent = parent;
        parent = node;
        suite(node.body);
        parent = oldParent;
        endScope();
        return null;
    }

    @Override
    public Object visitName(Name node) throws Exception {
        // Jython's parser doesn't always initialize the parent references correctly;
        // try to correct that here.
        node.parent = parent;

        String name = node.id;
        if (node.ctx != expr_contextType.Load) {
            if (name.equals("__debug__")) {
                symbolTable.error("can not assign to __debug__", true, node);
            }
            cur.addBound(name, node);
        } else {
            cur.addUsed(name, node);
        }
        return null;
    }

    // <netbeans>
    @Override
    public Object visitCall(Call node) throws Exception {
        Object ret = super.visitCall(node);

        if (node.func instanceof Name) {
            Name name = (Name)node.func;
            cur.markCall(name.id);
        } else if (node.func instanceof Attribute) {
            Attribute func = (Attribute)node.func;
            if (cur.attributes != null) {
                SymInfo funcSymbol = cur.attributes.get(func.attr);
                if (funcSymbol != null) {
                    funcSymbol.flags |= FUNCTION | CALLED; // mark as func/method call
                }
            }

        }

        return ret;
    }

    @Override
    public Object visitDelete(Delete node) throws Exception {
        for (exprType et : node.targets) {
            if (et instanceof Name) {
                String name = ((Name)et).id;
                cur.addUsed(name, node);
            }
        }

        return super.visitDelete(node);
    }

    @Override
    public Object visitAttribute(Attribute node) throws Exception {
        if (parent instanceof Call && node.value instanceof Name &&
                ("__all__".equals(((Name)node.value).id))) {
            // If you for example call
            //    __all__.extend("foo")
            // or
            //    __all__.append("bar")
            // then I don't want to try to analyze __all__
            if ("extend".equals(node.attr) || "append".equals(node.attr)) { // NOI18N
                Call call = (Call)parent;
                if (call.args != null) {
                    for (exprType expr : call.args) {
                        if (expr instanceof Str) {
                            if (publicSymbols == null) {
                                publicSymbols = new ArrayList<Str>();
                            }
                            publicSymbols.add((Str)expr);
                        } else if (expr instanceof org.python.antlr.ast.List) {
                            org.python.antlr.ast.List list = (org.python.antlr.ast.List)expr;
                            if (list != null && list.elts != null && list.elts.length > 0) {
                                for (exprType ex : list.elts) {
                                    if (ex instanceof Str) {
                                        Str str = (Str)ex;
                                        if (publicSymbols == null) {
                                            publicSymbols = new ArrayList<Str>();
                                        }
                                        publicSymbols.add(str);
                                    } else {
                                        invalidPublicSymbols = true;
                                    }
                                }
                            }
                        } else {
                            invalidPublicSymbols = true;
                            break;
                        }
                    }
                }
            } else {
                invalidPublicSymbols = true;
            }
        } else {
            if (node.attr != null) {
                cur.addAttribute(path, node.attr, node);
            }
        }
        return super.visitAttribute(node);
    }
    // </netbeans>

    @Override
    public Object visitListComp(ListComp node) throws Exception {
        String tmp = "_[" + node.getLine() + "_" + node.getCharPositionInLine() + "]";
        cur.addBound(tmp, node);
        traverse(node);
        return null;
    }

    @Override
    public Object visitYield(Yield node) throws Exception {
        cur.defineAsGenerator(node);
        cur.yield_count++;
        traverse(node);
        return null;
    }

    @Override
    public Object visitReturn(Return node) throws Exception {
        if (node.value != null) {
            cur.noteReturnValue(node);
        }
        traverse(node);
        return null;
    }

    @Override
    public Object visitGeneratorExp(GeneratorExp node) throws Exception {
        // The first iterator is evaluated in the outer scope
        if (node.generators != null && node.generators.length > 0) {
            visit(node.generators[0].iter);
        }
        String bound_exp = "_(x)";
        String tmp = "_(" + node.getLine() + "_" + node.getCharPositionInLine() + ")";
        def(tmp, GENERATOR, node);
        ArgListCompiler ac = new ArgListCompiler(symbolTable);
        Name argsName = new Name(node.token, bound_exp, expr_contextType.Param);
        ac.visitArgs(new argumentsType(node, new exprType[]{argsName}, null, null,
                new exprType[0]));
        beginScope(tmp, FUNCSCOPE, node, ac);
        cur.addParam(bound_exp, argsName);
        cur.markFromParam();

        cur.defineAsGenerator(node);
        cur.yield_count++;
        // The reset of the iterators are evaluated in the inner scope
        if (node.elt != null) {
            visit(node.elt);
        }
        if (node.generators != null) {
            for (int i = 0; i < node.generators.length; i++) {
                if (node.generators[i] != null) {
                    if (i == 0) {
                        visit(node.generators[i].target);
                        if (node.generators[i].ifs != null) {
                            for (exprType cond : node.generators[i].ifs) {
                                if (cond != null) {
                                    visit(cond);
                                }
                            }
                        }
                    } else {
                        visit(node.generators[i]);
                    }
                }
            }
        }

        endScope();
        return null;
    }

    @Override
    public Object visitWith(With node) throws Exception {
        cur.max_with_count++;
        traverse(node);

        return null;
    }

    public List<Str> getPublicSymbols() {
        return invalidPublicSymbols ? null : publicSymbols;
    }
}
