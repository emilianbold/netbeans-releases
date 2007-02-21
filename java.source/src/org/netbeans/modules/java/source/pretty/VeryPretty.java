/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.source.pretty;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.query.CommentHandler;
import org.netbeans.api.java.source.query.CommentSet;
import org.netbeans.api.java.source.query.Query;

import com.sun.tools.javac.util.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeInfo;

import java.io.*;
import java.util.Set;

import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.TypeTags.*;
import org.netbeans.modules.java.source.engine.JavaFormatOptions;

/** Prints out a tree as an indented Java source program.
 */
public class VeryPretty extends JCTree.Visitor {
    public JavaFormatOptions options;
    private CharBuffer out;
    boolean cuddleElse;

    private final Name.Table names;
    private CommentHandler commentHandler;
    private final Symtab symbols;
    private final Types types;
    private final TreeInfo treeinfo;
    private boolean packagePrinted;
    private boolean importsPrinted;
    
    // track a selected tree within a pretty printing
    private JCTree selection;
    private int selectionPos;     // the offset of the selection in the output stream.
    private int selectionEndPos;  // the end of the selection in the output stream.

    public VeryPretty(Context context) {
        this(context, JavaFormatOptions.getDefault());
    }

    public VeryPretty(Context context, JavaFormatOptions options) {
        this.options = options;
        out = new CharBuffer(options.rightMargin);
        cuddleElse = options.cuddleElse && !options.cuddleCloseBrace;
	names = Name.Table.instance(context);
	enclClassName = names.empty;
        commentHandler = CommentHandlerService.instance(context);
	symbols = Symtab.instance(context);
        types = Types.instance(context);
	treeinfo = TreeInfo.instance(context);
        prec = TreeInfo.notExpression;
	widthEstimator = new WidthEstimator(context);
        selectionPos = Position.NOPOS;
        selectionEndPos = Position.NOPOS;
    }

    /** The enclosing class name.
     */
    public Name enclClassName;

    public String toString() {
	return out.toString();
    }
    public void writeTo(Writer w)
	throws IOException
    {
	out.writeTo(w);
    }
    protected void toLeftMargin() {
	out.toLeftMargin();
    }
    public void reset(int margin) {
	out.setLength(0);
	out.leftMargin = margin;
        //out.col = margin;
    }

    /** Increase left margin by indentation width.
     */
    public int indent() {
	int old = out.leftMargin;
	out.leftMargin = old + options.indentSize;
	return old;
    }
    public void undent(int old) {
	out.leftMargin = old;
    }

    public void setPrec(int prec) {
        this.prec = prec;
    }
    
    public void setSelection(JCTree tree) {
        selection = tree;
    }
    public int getSelectionPos() {
        return selectionPos;
    }
    public int getSelectionEndPos() {
        return selectionEndPos;
    }

    /** Enter a new precedence level. Emit a `(' if new precedence level
     *  is less than precedence level so far.
     *  @param contextPrec    The precedence level in force so far.
     *  @param ownPrec        The new precedence level.
     */
    void open(int contextPrec, int ownPrec) {
	if (options.excessParensAroundConditionals
	    && (ownPrec==treeinfo.ordPrec || ownPrec==treeinfo.eqPrec)
	    && contextPrec>treeinfo.condPrec
	    || ownPrec < contextPrec)
	    print('(');
    }

    /** Leave precedence level. Emit a `(' if inner precedence level
     *  is less than precedence level we revert to.
     *  @param contextPrec    The precedence level we revert to.
     *  @param ownPrec        The inner precedence level.
     */
    void close(int contextPrec, int ownPrec) {
	//	if (ownPrec < contextPrec)
	if (options.excessParensAroundConditionals
	    && (ownPrec==treeinfo.ordPrec || ownPrec==treeinfo.eqPrec)
	    && contextPrec>treeinfo.condPrec
	    || ownPrec < contextPrec)
	    print(')');
    }

    private static char[] hex = "0123456789ABCDEF".toCharArray();
    /** Print string, replacing all non-ascii character with unicode escapes.
     */
    public void print(String s) {
	if (s == null)
	    return;
	int limit = s.length();
	for (int i = 0; i < limit; i++) {
	    char c = s.charAt(i);
	    if (c <= 255)
		out.append(c);
	    else {
		out.append("\\u");
		out.append(hex[(c >> (3 * 4)) & 0xF]);
		out.append(hex[(c >> (2 * 4)) & 0xF]);
		out.append(hex[(c >> (1 * 4)) & 0xF]);
		out.append(hex[(c >> (0 * 4)) & 0xF]);
	    }
	}
    }
    public final void print(char c) {
	out.append(c);
    }
    public final void needSpace() {
	out.needSpace();
    }
    public final void print(Name n) {
	out.appendUtf8(n.table.names, n.index, n.len);
    }

    public void printQualified(Symbol t) {
	if (t.owner != null && t.owner.name.len > 0
		&& !(t.type instanceof Type.TypeVar)
		&& (imports == null || !imports.imported(t)) && !(t.owner instanceof MethodSymbol)) {
	    if (t.owner instanceof Symbol.PackageSymbol)
		printAllQualified(t.owner);
	    else
		printQualified(t.owner);
	    print('.');
	}
	print(t.name);
    }
    public void printAllQualified(Symbol t) {
	if (t.owner != null && t.owner.name.len > 0) {
	    printAllQualified(t.owner);
	    print('.');
	}
	print(t.name);
    }
    public void printImports() {
	printImports(true);
    }
    public void printImports(boolean printPackageStatement) {
	if (imports == null)
	    return;
	imports.decideImports();
	int nout = 0;
	if (printPackageStatement && imports.containingPackage != null && 
	    imports.containingPackage != symbols.unnamedPackage) {
	    print("package ");
	    printAllQualified(imports.containingPackage);
	    print(";\n\n");
            packagePrinted = true;
	}
	for (ImportAnalysis.SymRefStats p = imports.usedClassOwners; p != null; p = p.next)
	    if (p.imported && !p.implicitlyImported()) {
                printImport(p.clazz, true);
		nout++;
	    }
	for (ImportAnalysis.SymRefStats p = imports.usedClasses; p != null; p = p.next)
	    if (p.imported && !p.implicitlyImported() && !imports.starred(p.clazz.owner)) {
		printImport(p.clazz, false);
		nout++;
	    }
	if (nout > 0 || printPackageStatement)
	    blankline();
        importsPrinted = true;
    }
    public void printImports(Set<Symbol> clazzes) {
        for (Symbol clazz : clazzes)
            printImport(clazz, false);
    }
    private void printImport(Symbol clazz, boolean wildcard) {
        print("import ");
        printAllQualified(clazz);
        if (wildcard)
            print(".*");
        print(";\n");
    }
    private ImportAnalysis imports;
    public void setImports(ImportAnalysis i) {
	imports = i;
	widthEstimator.setImports(i);
    }
    public ImportAnalysis getImports() { return imports; }
    public void print(JCTree t, Type ty) {
	if (ty == null || ty == Type.noType) {
	    print(t);
	} else {
	    int arrCnt = 0;
	    while (ty instanceof Type.ArrayType) {
		ty = ((Type.ArrayType) ty).elemtype;
		arrCnt++;
	    }
	    printQualified(ty.tsym);
	    if (ty instanceof Type.ClassType) {
		List < Type > typarams = ((Type.ClassType) ty).typarams_field;
		if (typarams != null && typarams.nonEmpty()) {
		    char prec = '<';
		    for (; typarams.nonEmpty(); typarams = typarams.tail) {
			print(prec);
			prec = ',';
			print(null, typarams.head);
		    }
		    print('>');
		}
	    }
	    while (--arrCnt >= 0)
		print("[]");
	}
    }
    public void print(JCTree t) {
        CommentSet comment = commentHandler.getComments(t);
	printPrecedingComments(comment);
        if (t == selection)
            selectionPos = out.used;
	t.accept(this);
        if (t == selection)
            selectionEndPos = out.used;
        printTrailingComments(comment);
    }

    /**************************************************************************
     * Traversal methods
     *************************************************************************/

    /** Visitor argument: the current precedence level.
     */
    int prec;

    /** Visitor method: print expression tree.
     *  @param prec  The current precedence level.
     */
    public void printExpr(JCTree tree, int prec) {
	if (tree == null) {
	    print("/*missing*/");
	} else {
	    int prevPrec = this.prec;
	    this.prec = prec;
            if (tree == selection)
                selectionPos = out.used;
	    tree.accept(this);
            if (tree == selection)
                selectionEndPos = out.used;
	    this.prec = prevPrec;
	}
    }

    /** Derived visitor method: print expression tree at minimum precedence level
     *  for expression.
     */
    public void printExpr(JCTree tree) {
	printExpr(tree, treeinfo.noPrec);
    }
    public void printNoParenExpr(JCTree tree) {
	while (tree instanceof JCParens)
	    tree = ((JCParens) tree).expr;
	printExpr(tree, treeinfo.noPrec);
    }

    /** Derived visitor method: print statement tree.
     */
    public void printStat(JCTree tree) {
	if(tree==null) print(';');
	else {
            CommentSet comment = commentHandler.getComments(tree);
	    printPrecedingComments(comment);
	    printExpr(tree, treeinfo.notExpression);
	    int tag = tree.tag;
	    if(JCTree.APPLY<=tag && tag<=JCTree.MOD_ASG) print(';');
            printTrailingComments(comment);
	}
    }

    /** Derived visitor method: print statement tree.
     */
    public void printIndentedStat(JCTree tree) {
	switch(options.redundantBraces.value) {
	case 0: // Leave alone
	    break;
	case 1: // Eliminate
	    while(tree instanceof JCBlock) {
		List<JCStatement> t = ((JCBlock) tree).stats;
		if(t.isEmpty() || !t.tail.isEmpty()) break;
		if (t.head instanceof JCVariableDecl)
		    // bogus code has a variable declaration -- leave alone.
		    break;
		printPrecedingComments(tree);
		tree = t.head;
	    }
	    break;
	case 2: //Add
	case 3:
	    printBlock(tree);
	    return;
	}
	int old = out.leftMargin;
	if (!(tree instanceof JCBlock))
	    indent();
	if (options.sameLineIfFit) {
	    int oldhm = out.harden();
	    int oldc = out.col;
	    int oldu = out.used;
	    int oldm = out.leftMargin;
	    try {
		needSpace();
		printStat(tree);
		undent(old);
		out.restore(oldhm);
		return;
	    } catch(Throwable t) {
		out.restore(oldhm);
		out.col = oldc;
		out.used = oldu;
		out.leftMargin = oldm;
	    }
	}
	if (out.hasMargin() || tree instanceof JCBlock && options.cuddleOpenBrace)
	    needSpace();
	else {
	    if (out.col > 0)
		newline();
	    out.toLeftMargin();
	}
	printStat(tree);
	undent(old);
    }

    /** Derived visitor method: print list of expression trees, separated by commas.
     */
    public <T extends JCTree >void printExprs(List < T > trees, String sep) {
	if (trees.nonEmpty()) {
	    printNoParenExpr(trees.head);
	    for (List < T > l = trees.tail; l.nonEmpty(); l = l.tail) {
		print(sep);
		printNoParenExpr(l.head);
	    }
	}
    }
    public <T extends JCTree >void printExprs(List < T > trees) {
	printExprs(trees, ", ");
    }

    private final WidthEstimator widthEstimator;

    /** Derived visitor method: print list of expression trees, separated by commas.
	If entries would overflow the line width, they get wrapped to the next
	line.
     */
    public <T extends JCTree >void wrapExprs(List < T > trees, String sep, int wrapIndent) {
	if (trees.nonEmpty()) {
	    boolean first = true;
	    int oldleft = out.leftMargin;
	    out.leftMargin = wrapIndent;
	    int rm = options.rightMargin;
	    for (List < T > l = trees; l.nonEmpty(); l = l.tail) {
		if (!first) {
		    print(sep);
		    int col = out.col;
		    if (col + widthEstimator.estimateWidth(l.head, rm - col + 1) > rm)
			toColExactly(wrapIndent);
		}
		first = false;
		printNoParenExpr(l.head);
	    }
	    out.leftMargin = oldleft;
	}
    }
    public <T extends JCTree >void wrapExprs(List < T > trees, String sep) {
	wrapExprs(trees, sep, out.leftMargin + options.continuationIndent);
    }
    public <T extends JCTree >void wrapExprs(List < T > trees) {
	wrapExprs(trees, ", ");
    }
    public <T extends JCTree >void wrapExprs(List < T > trees, int indent) {
	wrapExprs(trees, ", ", indent);
    }

    /** Derived visitor method: print list of statements, each on a separate line.
     */
    public <T extends JCTree >void printStats(List < T > trees) {
	printStats(trees, Query.NOPOS);
    }

    /**
     * Print a list of trees, unless a tree has the same position
     * as the specified parent tree's position.  This hack is used
     * to mark trees that were added by the compiler, rather than
     * being in the original source code.
     */
    public <T extends JCTree >void printStats(List < T > trees, int parentPos) {
	boolean prevDecl = false;
	boolean first = true;
	for (List < T > l = trees; l.nonEmpty(); l = l.tail) {
	    T t = l.head;
	    if (isSyntheticStatement(t.pos, parentPos))
		continue;
	    boolean isDecl = t instanceof JCVariableDecl || t instanceof JCMethodDecl || t instanceof JCClassDecl;
	    if (!first)
		if (options.blankLineBeforeInlineDeclarations && isDecl && !prevDecl
		|| options.blankLineAfterDeclarations && !isDecl && prevDecl)
		    blankline();
	    toColExactly(out.leftMargin);
	    printStat(t);
	    first = false;
	    prevDecl = isDecl;
	}
    }
    
    private boolean isSyntheticStatement(int pos, int parentPos) {
	return pos == parentPos && pos != Query.NOPOS;
    }
    
    /** Print a set of annotations.
     */
    public void printAnnotations(List<JCAnnotation> annotations) {
        while (!annotations.isEmpty()) {
	    printNoParenExpr(annotations.head);
            if (annotations.tail != null) {
                newline();
                toColExactly(out.leftMargin);
            }
            else 
                needSpace();
            annotations = annotations.tail;
        }
    }

    /** Print a set of modifiers.
     */
    public void printFlags(long flags) {
	print(treeinfo.flagNames(flags));
	if ((flags & StandardFlags) != 0)
	    needSpace();
    }

    private Comment pendingAppendComment = null;
    private JCTree lastCommentCheck = null;
    
    protected void printPrecedingComments(CommentSet commentSet) {
        if (!commentSet.hasComments())
            return;
        for (Comment c : commentSet.getPrecedingComments())
            printComment(c, false, options.moveAppendedComments);
    }

    protected void printTrailingComments(CommentSet commentSet) {
        if (!commentSet.hasComments())
            return;
        for (Comment c : commentSet.getTrailingComments())
            printComment(c, true, false);
    }

    /** Print documentation and other preceding comments, if any exist
     *  @param tree    The tree for which a documentation comment should be printed.
     */
    public void printPrecedingComments(JCTree tree) {
	if(tree==lastCommentCheck) return;
	lastCommentCheck = tree;
	if(pendingAppendComment!=null) {
	    printComment(pendingAppendComment, true, false);
	    pendingAppendComment = null;
	}
	if (commentHandler != null) {
	    CommentSet pc = commentHandler.getComments(tree);
            printPrecedingComments(pc);
	}
    }

    public void newline() {
	if(pendingAppendComment != null) {
	    printComment(pendingAppendComment, true, false);
	    pendingAppendComment = null;
	}
	out.nlTerm();
    }
    public void blankline() {
	newline();
	out.blankline();
    }
    public void toColExactly(int n) {
	if(n<out.col) newline();
	out.toCol(n);
    }

    private String body;
    class CommentLine {
	int startColumn;
	int startPos;
	int length;
	CommentLine next;
	CommentLine(int sc, int sp, int l) {
	    if((length = l)==0) {
		startColumn = 0;
		startPos = 0;
	    } else {
		startColumn = sc;
		startPos = sp;
	    }
	}
	public void print(int col) {
	    if(length>0) {
		out.toCol(col/*+startColumn*/);
		int limit = startPos+length;
		for(int i = startPos; i<limit; i++)
		    out.append(body.charAt(i));
	    }
	}
    }
    public void printComment(Comment comment, boolean appendOnly, boolean makePrepend) {
	body = comment.getText();
	int col = comment.indent();
	int stpos = -1;
        int endpos = 0;
	CommentLine root = null;
	CommentLine tail = null;
	int limit = body.length();
	for(int i = 0; i<limit; i++) {
	    char c = body.charAt(i);
	    switch(c) {
	    default:
		if(stpos<0) stpos = i;
                endpos = i + 1;
		break;
	    case '\t':
		if(stpos<0) col = (col+8)&~7;
		break;
	    case ' ':
	    case '*':
	    case '/':
		if(stpos<0) col++;
		break;
	    case '\n':
		int tlen = stpos<0 ? 0 : i-stpos;
		if(tlen>0||root!=null) {
		    CommentLine cl = new CommentLine(col,stpos,tlen);
		    if(tail==null) root = cl;
		    else tail.next = cl;
		    tail = cl;
		}
		stpos = -1;
		col = 0;
		break;
	    }
	}
	if(stpos>=0 && stpos<limit) {
	    CommentLine cl = new CommentLine(col,stpos,endpos-stpos);
	    if(tail==null) root = cl;
	    else tail.next = cl;
	}
	if(root==null) return;
	int minStartColumn = 99999;
	for(CommentLine cl = root; cl!=null; cl = cl.next)
	    if(cl.length>0 && cl.startColumn<minStartColumn) minStartColumn = cl.startColumn;
	for(CommentLine cl = root; cl!=null; cl = cl.next)
	    if(cl.length>0) cl.startColumn -= minStartColumn;

	boolean docComment = comment.isDocComment();

	int style = (docComment ? options.docCommentStyle
		     : root.next==null ? options.smallCommentStyle
		     : options.blockCommentStyle).value;
	int col0 = out.col;
	boolean start = true;
	int leftMargin = out.leftMargin;
	if (/*dc.isAppendPrevious()*/false && !makePrepend) {
	    if(!appendOnly) {
		if(pendingAppendComment==null)
		    pendingAppendComment = comment;
		return;
	    }
	    if (style != 3)
		style = 0;
	    leftMargin += options.appendCommentCol;
	    if (leftMargin < col0 + 1)
		leftMargin = col0 + 1;
	} else if(appendOnly) return;
	else {
	    if (options.blankLineBeforeAllComments ||
		options.blankLineBeforeDocComments && docComment)
		out.blankline();
	    if(!docComment) leftMargin -= options.unindentDisplace;
	}
	switch (style) {
	case 0:	// compact
	    for (CommentLine cl = root; cl!=null; cl = cl.next) {
		out.toColExactly(leftMargin);
		out.append(start ? docComment ? "/**" : "/*" : " *");
		start = false;
		cl.print(leftMargin+3);
		if (cl.next==null)
		    out.append(" */");
		out.nlTerm();
	    }
	    break;
	case 1:	// K&R
	    out.toColExactly(leftMargin);
	    out.append(docComment ? "/**" : "/*");
	    for (CommentLine cl = root; cl!=null; cl = cl.next) {
		out.toColExactly(leftMargin+1);
		out.append("*");
		cl.print(leftMargin+3);
		out.nlTerm();
	    }
	    out.toColExactly(leftMargin+1);
	    out.append("*/");
	    out.nlTerm();
	    break;
	case 2:	// boxed
	    int w = 0;
	    for (CommentLine cl = root; cl!=null; cl = cl.next) {
		int tw = cl.length+cl.startColumn;
		if (tw > w)
		    w = tw;
	    }
	    out.toColExactly(leftMargin);
	    out.append(docComment ? "/**" : "/* ");
	    for (int i = w + 2; --i >= 0;)
		out.append('*');
	    out.nlTerm();
	    for (CommentLine cl = root; cl!=null; cl = cl.next) {
		out.toColExactly(leftMargin);
		out.append(" *");
		cl.print(leftMargin+3);
		out.toCol(leftMargin + w + 4);
		out.append('*');
		out.nlTerm();
	    }
	    out.toColExactly(leftMargin + 1);
	    for (int i = w + 4; --i >= 0;)
		out.append('*');
	    out.append('/');
	    out.nlTerm();
	    break;
	case 3:	// double slash
	    for (CommentLine cl = root; cl!=null; cl = cl.next) {
		out.toColExactly(leftMargin);
		out.append(start && docComment ? "//*" : "//");
		start = false;
		cl.print(leftMargin+3);
		out.nlTerm();
	    }
	    break;
	}
	out.nlTerm();
	out.toLeftMargin();
    }

    /** If type parameter list is non-empty, print it enclosed in "<...>" brackets.
     */
    public void printTypeParameters(List < JCTypeParameter > trees) {

	if (trees.nonEmpty()) {
	    print('<');
	    printExprs(trees);
	    print('>');
	}
    }

    /** Print a block.
     */
    public void printBlock(List<? extends JCTree>stats) {
	printBlock(stats, Query.NOPOS);
    }

    /**
     * Print a block, unless a statement has the same position as
     * the specified parent tree's position.  This hack is used
     * to mark trees that were added by the compiler, rather than
     * being in the original source code.
     */
    public void printBlock(List<? extends JCTree> stats, int parentPos) {
	int old = indent();
	int bcol = old;
	if (options.indBracesHalfway)
	    bcol += (options.indentSize >> 1);
	else if (options.indBracesInner)
	    bcol = out.leftMargin;
	out.toCol(bcol);
	needSpace();
	print('{');
	if (!stats.isEmpty())
	    newline();
	printStats(stats, parentPos);
	undent(old);
	if (options.cuddleCloseBrace)
	    print(" }");
	else {
	    toColExactly(bcol);
	    print('}');
	}
    }
    public void printBlock(JCTree t) {
	List<? extends JCTree> stats;
	if (t instanceof JCBlock)
	    stats = ((JCBlock) t).stats;
	else
	    stats = List.of(t);
	printBlock(stats);
    }
    /** Print unit consisting of package clause and import statements in toplevel,
     *  followed by class definition. if class definition == null,
     *  print all definitions in toplevel.
     *  @param tree     The toplevel tree
     */
    public void printUnit(JCCompilationUnit tree) {
        if (!packagePrinted) {
            printPrecedingComments(tree);
            if (tree.pid != null) {
                print("package ");
                printExpr(tree.pid);
                print(';');
                newline();
            }
            packagePrinted = true;
        }
        List<JCTree> l = tree.defs;
        while (l.nonEmpty() && l.head.tag == JCTree.IMPORT){ 
            if (!importsPrinted) {
                printStat(l.head);
                newline();
            }
            l = l.tail;
        }
        importsPrinted = true;
	while (l.nonEmpty()) {
            printStat(l.head);
            newline();
            l = l.tail;
	}
    }

    private Name fullName(JCTree tree) {
	switch (tree.tag) {
	case JCTree.IDENT:
	    return ((JCIdent) tree).name;
	case JCTree.SELECT:
            JCFieldAccess sel = (JCFieldAccess)tree;
	    Name sname = fullName(sel.selected);
	    return sname != null && sname.len > 0 ? sname.append('.', sel.name) : sel.name;
	default:
	    return null;
	}
    }

    /**************************************************************************
     * Visitor methods
     *************************************************************************/

    public void visitTopLevel(JCCompilationUnit tree) {
	printUnit(tree);
    }

    public void visitImport(JCImport tree) {
        if (!importsPrinted) {
            print("import ");
            if (tree.staticImport)
                print("static ");
            print(fullName(tree.qualid));
            print(';');
        }
    }

    public void visitClassDef(JCClassDecl tree) {
	out.toLeftMargin();
	Name enclClassNamePrev = enclClassName;
	enclClassName = tree.name;
	printClassHeader(tree);
	needSpace();
	printClassBody(tree);
	enclClassName = enclClassNamePrev;
    }
    
    protected void printClassHeader(JCClassDecl tree) {
        printAnnotations(tree.mods.annotations);
	long flags = tree.sym != null ? tree.sym.flags() : tree.mods.flags;
	if ((flags & ENUM) != 0)
	    printFlags(flags & ~(INTERFACE | STATIC | FINAL));
	else
	    printFlags(flags & ~(INTERFACE | ABSTRACT));
	if ((flags & INTERFACE) != 0 || (flags & ANNOTATION) != 0) {
            if ((flags & ANNOTATION) != 0) print('@');
	    print("interface ");
	    print(tree.name);
	    printTypeParameters(tree.typarams);
	    if (tree.implementing.nonEmpty()) {
		print(" extends ");
		wrapExprs(tree.implementing);
	    }
	} else {
	    if ((flags & ENUM) != 0)
		print("enum ");
	    else {
		if ((flags & ABSTRACT) != 0)
		    print("abstract ");
		print("class ");
	    }
	    print(tree.name);
	    printTypeParameters(tree.typarams);
	    if (tree.extending != null) {
		print(" extends ");
		print(tree.extending, tree.sym != null 
                      ? types.supertype(tree.sym.type) : null);
	    }
	    if (tree.implementing.nonEmpty()) {
		print(" implements ");
		wrapExprs(tree.implementing, out.col);
	    }
	}
    }
    
    protected void printClassBody(JCClassDecl tree) {
	if ((tree.mods.flags & ENUM) != 0)
	    printEnumBody(tree.defs, tree.pos);
	else
	    printBlock(tree.defs, tree.pos);
    }
    
    public void printEnumBody(List<JCTree> stats, int parentPos) {
	int old = indent();
	int bcol = old;
	if (options.indBracesHalfway)
	    bcol += (options.indentSize >> 1);
	else if (options.indBracesInner)
	    bcol = out.leftMargin;
	out.toCol(bcol);
	needSpace();
        print("{");
	if (!stats.isEmpty())
	    newline();
        boolean first = true;
	boolean hasNonEnumerator = false;
        for (List<JCTree> l = stats; l.nonEmpty(); l = l.tail) {
            if (isEnumerator(l.head)) {
                if (!first) {
                    print(", ");
		    if (options.blankLineBeforeInlineDeclarations
		        || options.blankLineAfterDeclarations)
			blankline();
		}
		toColExactly(out.leftMargin);
                printStat(l.head);
                first = false;
            }
	    else if (!isSyntheticStatement(l.head.pos, parentPos))
		hasNonEnumerator = true;
        }
	if (hasNonEnumerator) {
	    print(";");
	    newline();
	}
        for (List<JCTree> l = stats; l.nonEmpty(); l = l.tail) {
	    JCTree t = l.head;
            if (!isEnumerator(t)) {
		if (isSyntheticStatement(t.pos, parentPos))
		    continue;
                toColExactly(out.leftMargin);
                printStat(t);
                newline();
            }
        }
	undent(old);
	if (options.cuddleCloseBrace)
	    print(" }");
	else {
	    toColExactly(bcol);
	    print('}');
	}
    }

    /** Is the given tree an enumerator definition? */
    protected boolean isEnumerator(JCTree t) {
        return t.tag == JCTree.VARDEF && (((JCVariableDecl) t).mods.flags & ENUM) != 0;
    }

    protected final boolean isMethodPrintable(JCMethodDecl tree) {
	// suppress printing of constructors of anonymous methods
	// since we have no name for them.
	return ((tree.mods.flags & Flags.SYNTHETIC)==0 && 
		tree.name != names.init || 
		enclClassName != null);
    }

    public void visitMethodDef(JCMethodDecl tree) {
	if (isMethodPrintable(tree)) {
	    if (options.blankLineBeforeMethods)
		blankline();
	    out.toLeftMargin();
	    printMethodHeader(tree);
	    needSpace();
	    printMethodBody(tree);
	}
    }

    private String replace(String a,String b) {
        a = a.replace(b, out.toString());
        out.clear();
        return a;
    }
    
    private static final String REPLACEMENT = "%[a-z]*%";
    
    public String getMethodHeader(MethodTree t, String s) {
        JCMethodDecl tree = (JCMethodDecl) t;
        printAnnotations(tree.mods.annotations);
        s = replace(s, UiUtils.PrintPart.ANNOTATIONS);
        printFlags(tree.mods.flags);
        s = replace(s, UiUtils.PrintPart.FLAGS);
        if (tree.name == names.init) {
            print(enclClassName);
            s = replace(s, UiUtils.PrintPart.NAME);
        } else {
            if (tree.typarams != null) {
                printTypeParameters(tree.typarams);
                needSpace();
                s = replace(s, UiUtils.PrintPart.TYPEPARAMETERS);
            }
            print(tree.restype, tree.sym != null && tree.sym.type!=null ? tree.sym.type.getReturnType() : null);
            s = replace(s, UiUtils.PrintPart.TYPE);
            if(options.methodNamesStartLine) { newline(); out.toLeftMargin(); } else needSpace();
            out.clear();
            print(tree.name);
            s = replace(s, UiUtils.PrintPart.NAME);
        }
        print('(');
        wrapExprs(tree.params, out.col);
        print(')');
        s = replace(s, UiUtils.PrintPart.PARAMETERS);
        if (tree.thrown.nonEmpty()) {
            print(" throws ");
            wrapExprs(tree.thrown, out.col);
            s = replace(s, UiUtils.PrintPart.THROWS);
        }
        return s.replaceAll(REPLACEMENT,"");
    }
    
    public String getClassHeader(ClassTree t, String s) {
        JCClassDecl tree = (JCClassDecl) t;
        printAnnotations(tree.mods.annotations);
        s = replace(s, UiUtils.PrintPart.ANNOTATIONS);
        long flags = tree.sym != null ? tree.sym.flags() : tree.mods.flags;
        if ((flags & ENUM) != 0)
            printFlags(flags & ~(INTERFACE | STATIC | FINAL));
        else
            printFlags(flags & ~(INTERFACE | ABSTRACT));
        s = replace(s, UiUtils.PrintPart.FLAGS);
        if ((flags & INTERFACE) != 0) {
            print("interface ");
            print(tree.name);
            s = replace(s, UiUtils.PrintPart.NAME);
            printTypeParameters(tree.typarams);
            s = replace(s, UiUtils.PrintPart.TYPEPARAMETERS);
            if (tree.implementing.nonEmpty()) {
                print(" extends ");
                wrapExprs(tree.implementing);
                s = replace(s, UiUtils.PrintPart.EXTENDS);
            }
        } else {
            if ((flags & ENUM) != 0)
                print("enum ");
            else {
                if ((flags & ABSTRACT) != 0)
                    print("abstract ");
                print("class ");
            }
            print(tree.name);
            s = replace(s, UiUtils.PrintPart.NAME);
            printTypeParameters(tree.typarams);
            s = replace(s, UiUtils.PrintPart.TYPEPARAMETERS);
            if (tree.extending != null) {
                print(" extends ");
                print(tree.extending, tree.sym != null
                        ? types.supertype(tree.sym.type) : null);
                s = replace(s, UiUtils.PrintPart.EXTENDS);
            }
            if (tree.implementing.nonEmpty()) {
                print(" implements ");
                wrapExprs(tree.implementing, out.col);
                s = replace(s, UiUtils.PrintPart.IMPLEMENTS);
            }
        }
        return s.replaceAll(REPLACEMENT,"");
    }

    public String getVariableHeader(VariableTree t, String s) {
        JCVariableDecl tree = (JCVariableDecl) t;
        printAnnotations(tree.mods.annotations);
        s = replace(s, UiUtils.PrintPart.ANNOTATIONS);
	printFlags(tree.mods.flags);
        s = replace(s, UiUtils.PrintPart.FLAGS);
        Type type = tree.type != null ? tree.type : tree.vartype.type;
	print(tree.vartype, type);
        s = replace(s, UiUtils.PrintPart.TYPE);
	needSpace();
	print(tree.name);
        s = replace(s, UiUtils.PrintPart.NAME);
        return s.replaceAll(REPLACEMENT,"");
    }
    
    protected void printMethodHeader(JCMethodDecl tree) {
        printAnnotations(tree.mods.annotations);
	printFlags(tree.mods.flags);
	if (tree.name == names.init) {
	    print(enclClassName);
	} else {
	    if (tree.typarams != null) {
		printTypeParameters(tree.typarams);
		needSpace();
	    }
	    print(tree.restype, tree.sym != null && tree.sym.type!=null ? tree.sym.type.getReturnType() : null);
	    if(options.methodNamesStartLine) { newline(); out.toLeftMargin(); }
	    else needSpace();
	    print(tree.name);
	}
	print('(');
	wrapExprs(tree.params, out.col);
	print(')');
	if (tree.thrown.nonEmpty()) {
	    print(" throws ");
	    wrapExprs(tree.thrown, out.col);
	}
    }

    protected void printMethodBody(JCMethodDecl tree) {
	if (tree.body != null) {
	    boolean constructor = (tree.name == names.init);
	    List<JCStatement> stats = tree.body.stats;
	    JCTree head = stats.head;
	    if(head instanceof JCExpressionStatement) head = ((JCExpressionStatement)head).expr;
	    if(constructor && head instanceof JCMethodInvocation) {
		JCMethodInvocation ap = (JCMethodInvocation) head;
		if(ap.args.isEmpty() && ap.meth instanceof JCIdent) {
		    JCIdent id = (JCIdent) ap.meth;
		    Name n = id.sym==null ? id.name : id.sym.name;
		    if(n == names.init) {
			/* We have an invocation of the null constructor
			   at the beginning of a constructor: eliminate it */
			stats = stats.tail;
		    }
		}
	    }
	    printBlock(stats);
	} else {
	    print(';');
	}
    }

    public void visitVarDef(JCVariableDecl tree) {
	if (commentHandler != null && commentHandler.hasComments(tree)) {
            if (prec == TreeInfo.notExpression) { // ignore for parameters.
                newline();
                out.toLeftMargin();
            }
	}
	if ((tree.mods.flags & ENUM) != 0)
	    print(tree.name);
	else {
	    printVarHeader(tree);
	    printVarBody(tree);
	}
    }

    protected void printVarHeader(JCVariableDecl tree) {
        printAnnotations(tree.mods.annotations);
	printFlags(tree.mods.flags);
        Type type = tree.type != null ? tree.type : tree.vartype.type;
        if ((tree.mods.flags & VARARGS) != 0) {
            // Variable arity method. Expecting  ArrayType, print ... instead of [].
            // todo  (#pf): should we check the array type to prevent CCE?
            printExpr(((JCArrayTypeTree) tree.vartype).elemtype);
            print("...");
        } else {
            print(tree.vartype, type);
        }
	needSpace();
	print(tree.name);
    }

    protected void printVarBody(JCVariableDecl tree) {
	if (tree.init != null) {
	    print(" = ");
	    printNoParenExpr(tree.init);
	}
	if (prec == treeinfo.notExpression)
	    print(';');
    }

    public void visitSkip(JCSkip tree) {
	print(';');
    }

    public void visitBlock(JCBlock tree) {
	printFlags(tree.flags);
	printBlock(tree.stats);
    }

    public void visitDoLoop(JCDoWhileLoop tree) {
	print("do ");
	printIndentedStat(tree.body);
	out.toLeftMargin();
	needSpace();
	print("while (");
	printNoParenExpr(tree.cond);
	print(");");
    }

    public void visitWhileLoop(JCWhileLoop tree) {
	print("while (");
	printNoParenExpr(tree.cond);
	print(") ");
	printIndentedStat(tree.body);
    }

    public void visitForLoop(JCForLoop tree) {
	print("for (");
	if (tree.init.nonEmpty()) {
	    if (tree.init.head.tag == JCTree.VARDEF) {
		printNoParenExpr(tree.init.head);
		for (List<? extends JCTree> l = tree.init.tail; l.nonEmpty(); l = l.tail) {
		    JCVariableDecl vdef = (JCVariableDecl) l.head;
		    print(", " + vdef.name + " = ");
		    printNoParenExpr(vdef.init);
		}
	    } else {
		printExprs(tree.init);
	    }
	}
	print(';');
	if (tree.cond != null) {
            print(' ');
	    printNoParenExpr(tree.cond);
        }
	print(';');
        if (tree.step.nonEmpty()) {
            print(' ');
            printExprs(tree.step);
        }
	print(") ");
	printIndentedStat(tree.body);
    }

    public void visitLabelled(JCLabeledStatement tree) {
	print(tree.label);
	print(": ");
	printIndentedStat(tree.body);
    }

    public void visitSwitch(JCSwitch tree) {
	print("switch (");
	printNoParenExpr(tree.selector);
	print(") {\n");
	printStats(tree.cases);
	toColExactly(out.leftMargin);
	print('}');
    }

    public void visitCase(JCCase tree) {
	toColExactly(out.leftMargin + options.caseInd);
	if (tree.pat == null) {
	    print("default");
	} else {
	    print("case ");
	    printNoParenExpr(tree.pat);
	}
	print(':');
	newline();
	int old = indent();
	printStats(tree.stats);
	undent(old);
    }

    public void visitSynchronized(JCSynchronized tree) {
	print("synchronized (");
	printNoParenExpr(tree.lock);
	print(") ");
	printBlock(tree.body);
    }

    public void visitTry(JCTry tree) {
	print("try ");
	printBlock(tree.body);
	for (List < JCCatch > l = tree.catchers; l.nonEmpty(); l = l.tail)
	    printStat(l.head);
	if (tree.finalizer != null) {
	    toColExactly(out.leftMargin);
	    print("finally ");
	    printBlock(tree.finalizer);
	}
    }

    public void visitCatch(JCCatch tree) {
	toColExactly(out.leftMargin);
	print("catch (");
	printNoParenExpr(tree.param);
	print(") ");
	printBlock(tree.body);
    }

    public void visitConditional(JCConditional tree) {
	int condWidth = 0;
	final int maxCondWidth = 40;
	if (options.forceCondExprWrap) {
	    JCConditional t = tree;
	    while (true) {
		int thisWidth = widthEstimator.estimateWidth(t.cond, maxCondWidth);
		if (thisWidth > condWidth)
		    condWidth = thisWidth;
		if (!(t.falsepart instanceof JCConditional))
		    break;
		t = (JCConditional) t.falsepart;
	    }
	    if (condWidth >= maxCondWidth)
		condWidth = options.continuationIndent;
	}
	open(prec, treeinfo.condPrec);
	int col0 = out.col;
	while (true) {
	    printExpr(tree.cond, treeinfo.condPrec - 1);
	    out.toColExactly(col0 + condWidth + 1);
	    print("? ");
	    printExpr(tree.truepart, treeinfo.condPrec);
	    if (options.forceCondExprWrap)
		if (tree.falsepart instanceof JCConditional) {
		    tree = (JCConditional) tree.falsepart;
		    toColExactly(col0 - 3);
		    print(" : ");
		    continue;
		} else
		    toColExactly(col0 + condWidth+1);
	    else needSpace();
	    print(": ");
	    printExpr(tree.falsepart, treeinfo.condPrec);
	    break;
	}
	close(prec, treeinfo.condPrec);
    }

    private final DanglingElseChecker danglingElseChecker = new DanglingElseChecker();
    public void visitIf(JCIf tree) {
	print("if (");
	printNoParenExpr(tree.cond);
	print(")");
	boolean prevblock = tree.thenpart instanceof JCBlock || options.redundantBraces.value>=2;
	if (tree.elsepart != null && danglingElseChecker.hasDanglingElse(tree.thenpart)) {
	    printBlock(tree.thenpart);
	    prevblock = true;
	} else
	    printIndentedStat(tree.thenpart);
	if (tree.elsepart != null) {
	    if (prevblock && cuddleElse)
		needSpace();
	    else
		toColExactly(out.leftMargin);
	    needSpace();
	    print("else");
	    if (tree.elsepart instanceof JCIf && options.cuddleElseIf) {
		needSpace();
		printStat(tree.elsepart);
	    } else
		printIndentedStat(tree.elsepart);
	}
    }

    public void visitExec(JCExpressionStatement tree) {
	printNoParenExpr(tree.expr);
	if (prec == treeinfo.notExpression)
	    print(';');
    }

    public void visitBreak(JCBreak tree) {
	print("break");
	if (tree.label != null) {
	    needSpace();
	    print(tree.label);
	}
	print(';');
    }

    public void visitContinue(JCContinue tree) {
	print("continue");
	if (tree.label != null) {
	    needSpace();
	    print(tree.label);
	}
	print(';');
    }

    public void visitReturn(JCReturn tree) {
	print("return");
	if (tree.expr != null) {
	    needSpace();
	    printNoParenExpr(tree.expr);
	}
	print(';');
    }

    public void visitThrow(JCThrow tree) {
	print("throw ");
	printNoParenExpr(tree.expr);
	print(';');
    }

    public void visitAssert(JCAssert tree) {
	print("assert ");
	printExpr(tree.cond);
	if (tree.detail != null) {
	    print(" : ");
	    printExpr(tree.detail);
	}
	print(';');
    }
    
    private void printTypeArguments(List<? extends JCExpression> typeargs) {
        if (typeargs.size() > 0) {
            print('<');
            printExprs(typeargs);
            print('>');
        }
    }

    public void visitApply(JCMethodInvocation tree) {
	if (!tree.typeargs.isEmpty()) {
	    int prevPrec = prec;
	    this.prec = treeinfo.postfixPrec;
	    if (tree.meth.tag == JCTree.SELECT) {
		JCFieldAccess left = (JCFieldAccess)tree.meth;
		printExpr(left.selected);
                print('.');
                printTypeArguments(tree.typeargs);
                print(left.name.toString());
	    } else {
                printTypeArguments(tree.typeargs);
		printExpr(tree.meth);
	    }
	    this.prec = prevPrec;
	} else {
	    printExpr(tree.meth, treeinfo.postfixPrec);
	}
	print('(');
	wrapExprs(tree.args, out.col);
	print(')');
    }

    public void visitNewClass(JCNewClass tree) {
	if (tree.encl != null) {
	    printExpr(tree.encl);
	    print('.');
	}
	print("new ");
        if (!tree.typeargs.isEmpty()) {
            print("<");
            printExprs(tree.typeargs);
            print(">");
        }
	if (tree.encl == null)
	    print(tree.clazz, tree.clazz.type);
	else if (tree.clazz.type != null)
	    print(tree.clazz.type.tsym.name);
	else
	    print(tree.clazz);
	print('(');
	wrapExprs(tree.args, out.col);
	print(')');
	if (tree.def != null) {
	    Name enclClassNamePrev = enclClassName;
	    enclClassName = null;
	    printBlock(((JCClassDecl) tree.def).defs);
	    enclClassName = enclClassNamePrev;
	}
    }

    public void visitNewArray(JCNewArray tree) {
	if (tree.elemtype != null) {
	    print("new ");
	    int n = tree.elems != null ? 1 : 0;
	    JCTree elemtype = tree.elemtype;
	    while (elemtype.tag == JCTree.TYPEARRAY) {
		n++;
		elemtype = ((JCArrayTypeTree) elemtype).elemtype;
	    }
	    printExpr(elemtype);
	    for (List<? extends JCTree> l = tree.dims; l.nonEmpty(); l = l.tail) {
		print('[');
		printNoParenExpr(l.head);
		print(']');
		n--;
	    }
	    while(--n >= 0) 
                print("[]");
	}
	if (tree.elems != null) {
	    print("{");
	    wrapExprs(tree.elems);
	    print('}');
	}
    }

    public void visitParens(JCParens tree) {
	print('(');
	printExpr(tree.expr);
	print(')');
    }

    public void visitAssign(JCAssign tree) {
	open(prec, treeinfo.assignPrec);
	printExpr(tree.lhs, treeinfo.assignPrec + 1);
	print(" = ");
	printExpr(tree.rhs, treeinfo.assignPrec);
	close(prec, treeinfo.assignPrec);
    }

    public void visitAssignop(JCAssignOp tree) {
	open(prec, treeinfo.assignopPrec);
	printExpr(tree.lhs, treeinfo.assignopPrec + 1);
	print(" ");
	print(treeinfo.operatorName(tree.tag - JCTree.ASGOffset));
	print("= ");
	printExpr(tree.rhs, treeinfo.assignopPrec);
	close(prec, treeinfo.assignopPrec);
    }

    public void visitUnary(JCUnary tree) {
	int ownprec = treeinfo.opPrec(tree.tag);
	Name opname = treeinfo.operatorName(tree.tag);
	open(prec, ownprec);
	if (tree.tag <= JCTree.PREDEC) {
	    print(opname);
	    printExpr(tree.arg, ownprec);
	} else {
	    printExpr(tree.arg, ownprec);
	    print(opname);
	}
	close(prec, ownprec);
    }

    public void visitBinary(JCBinary tree) {
	int ownprec = treeinfo.opPrec(tree.tag);
	Name opname = treeinfo.operatorName(tree.tag);
	open(prec, ownprec);
	int stcol = out.col;
	printExpr(tree.lhs, ownprec);
	int spLimit = options.spaceOperatorsBelow.value;
	if(ownprec<=spLimit) needSpace();
	print(opname);
	if(ownprec<=spLimit) needSpace();
	int rm = options.rightMargin;
	if(out.col+widthEstimator.estimateWidth(tree.rhs,rm-out.col)>rm)
	    out.toColExactly(stcol);
	printExpr(tree.rhs, ownprec + 1);
	close(prec, ownprec);
    }

    public void visitTypeCast(JCTypeCast tree) {
	open(prec, treeinfo.prefixPrec);
	print('(');
	print(tree.clazz, tree.clazz.type);
	print(") ");
	printExpr(tree.expr, treeinfo.prefixPrec);
	close(prec, treeinfo.prefixPrec);
    }

    public void visitTypeTest(JCInstanceOf tree) {
	open(prec, treeinfo.ordPrec);
	printExpr(tree.expr, treeinfo.ordPrec);
	print(" instanceof ");
	print(tree.clazz, tree.clazz.type);
	close(prec, treeinfo.ordPrec);
    }

    public void visitIndexed(JCArrayAccess tree) {
	printExpr(tree.indexed, treeinfo.postfixPrec);
	print('[');
	printExpr(tree.index);
	print(']');
    }

    public void visitSelect(JCFieldAccess tree) {
	if (tree.sym instanceof Symbol.ClassSymbol) {
	    print(null, tree.type);
	} else {
	    printExpr(tree.selected, treeinfo.postfixPrec);
	    print('.');
	    print(tree.sym==null ? tree.name : tree.sym.name);
	}
    }

    public void visitIdent(JCIdent tree) {
	if (tree.sym instanceof Symbol.ClassSymbol)
	    print(null, tree.type);
	else {
	    Name n = tree.sym==null ? tree.name : tree.sym.name;
	    if(n==names.init) print(tree.name);
	    else print(n);
	}
    }

    public void visitLiteral(JCLiteral tree) {
	switch (tree.typetag) {
	  case INT:
	    print(tree.value.toString());
	    break;
	  case LONG:
	    print(tree.value.toString() + "L");
	    break;
	  case FLOAT:
	    print(tree.value.toString() + "F");
	    break;
	  case DOUBLE:
	    print(tree.value.toString());
	    break;
	  case CHAR:
	    print("\'" +
		  Convert.quote(
		  String.valueOf((char) ((Number) tree.value).intValue())) +
		  "\'");
	    break;
	   case CLASS:
	    print("\"" + Convert.quote((String) tree.value) + "\"");
	    break;
          case BOOLEAN:
            print(tree.getValue().toString());
            break;
          case BOT:
            print("null");
            break;
	  default:
	    print(tree.value.toString());
	}
    }

    public void visitTypeIdent(JCPrimitiveTypeTree tree) {
	print(symbols.typeOfTag[tree.typetag].tsym.name);
    }

    public void visitTypeArray(JCArrayTypeTree tree) {
	printExpr(tree.elemtype);
	print("[]");
    }

    public void visitTypeApply(JCTypeApply tree) {
	printExpr(tree.clazz);
	print('<');
	printExprs(tree.arguments);
	print('>');
    }

    public void visitTypeParameter(JCTypeParameter tree) {
	print(tree.name);
	if (tree.bounds.nonEmpty()) {
	    print(" extends ");
	    printExprs(tree.bounds, " & ");
	}
    }
    
    public void visitWildcard(JCWildcard tree) {
	print("" + tree.kind);
	if (tree.kind != BoundKind.UNBOUND)
	    printExpr(tree.inner);
    }
    
    public void visitModifiers(JCModifiers tree) {
	printAnnotations(tree.annotations);
	printFlags(tree.flags);
    }
    
    public void visitAnnotation(JCAnnotation tree) {
	print("@");
	printExpr(tree.annotationType);
        if (tree.args.nonEmpty()) {
            print("(");
            printExprs(tree.args);
            print(")");
        }
    }

    public void visitForeachLoop(JCEnhancedForLoop tree) {
	print("for (");
        printExpr(tree.getVariable());
        print(" : ");
        printExpr(tree.getExpression());
        print(") ");
	printIndentedStat(tree.getStatement());
    }
    
    public void visitLetExpr(LetExpr tree) {
	print("(let " + tree.defs + " in " + tree.expr + ")");
    }

    public void visitErroneous(JCErroneous tree) {
	print("(ERROR)");
    }

    public void visitTree(JCTree tree) {
	print("(UNKNOWN: " + tree + ")");
	newline();
    }
}
