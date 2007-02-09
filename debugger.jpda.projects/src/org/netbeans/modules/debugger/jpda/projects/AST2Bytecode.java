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

package org.netbeans.modules.debugger.jpda.projects;

import com.sun.corba.se.impl.protocol.InfoOnlyServantCacheLocalCRDImpl;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;

import org.netbeans.spi.debugger.jpda.EditorContext;

/**
 * This class tries to match the AST expression to bytecode. The result
 * of this match is provided as an
 * {@link org.netbeans.spi.debugger.jpda.EditorContext.ExpressionTree} object.
 * 
 * @author Martin Entlicher
 */
class AST2Bytecode {
    
    /** Creates a new instance of AST2Bytecode */
    private AST2Bytecode() {
    }
    
    static EditorContext.Operation[] matchSourceTree2Bytecode(
            CompilationUnitTree cu, CompilationController ci,
            List<Tree> treeNodes, ExpressionScanner.ExpressionsInfo info,
            byte[] bytecodes, int[] indexes, byte[] constantPool,
            OperationCreationDelegate opCreationDelegate,
            Map<Tree, EditorContext.Operation> nodeOperations) {
        
        Trees trees = ci.getTrees();
        Types types = ci.getTypes();
        SourcePositions sp = trees.getSourcePositions();
        //List<Tree> treeNodes = linearizeTree(expTrees);
        if (treeNodes == null) return null;
        int length = treeNodes.size();
        List<EditorContext.Operation> operations = new ArrayList<EditorContext.Operation>(length);
        LineMap lineMap = cu.getLineMap();
        int indexesIndex = 0;
        int from = indexes[indexesIndex];
        int to = indexes[indexesIndex + 1];
        for (int treeIndex = 0; treeIndex < length; treeIndex++) {
            Tree node = treeNodes.get(treeIndex);
            Tree.Kind kind = node.getKind();
            EditorContext.Operation op = null;
            if (kind.equals(Tree.Kind.METHOD_INVOCATION) ||
                kind.equals(Tree.Kind.NEW_CLASS)) {
                
                int opcode;
                do {
                    do {
                        opcode = bytecodes[from] & 0xFF;
                        if (isMethodCall(opcode)) {
                            break;
                        }
                        from += getInstrSize(opcode, bytecodes, from);
                    } while (from < to);
                    if (from < to) {
                        break;
                    }
                    if ((indexesIndex + 2) < indexes.length) {
                        indexesIndex += 2;
                        from = indexes[indexesIndex];
                        to = indexes[indexesIndex + 1];
                    } else {
                        break;
                    }
                } while (true);
                if (from < to) { // We have the method call
                    int pos = (int) sp.getStartPosition(cu, node);
                    EditorContext.Position startPosition =
                            opCreationDelegate.createPosition(
                                    pos,
                                    (int) lineMap.getLineNumber(pos),
                                    (int) lineMap.getColumnNumber(pos)
                            );
                    pos = (int) sp.getEndPosition(cu, node);
                    EditorContext.Position endPosition =
                            opCreationDelegate.createPosition(
                                    pos,
                                    (int) lineMap.getLineNumber(pos),
                                    (int) lineMap.getColumnNumber(pos)
                            );
                    Tree identifier;
                    String methodName;
                    String methodClassType;
                    if (kind.equals(Tree.Kind.NEW_CLASS)) {
                        identifier = ((NewClassTree) node).getIdentifier();
                        methodName = "<init>";
                        TreePath iPath = TreePath.getPath(cu, identifier);
                        TypeMirror type = trees.getTypeMirror(iPath);
                        assert type.getKind() == TypeKind.DECLARED;
                        TypeElement te = (TypeElement) types.asElement(type);
                        methodClassType = ElementUtilities.getBinaryName(te);
                    } else {
                        //identifier = ((MemberSelectTree) ((MethodInvocationTree) node).getMethodSelect()).getIdentifier();
                        identifier = ((MethodInvocationTree) node).getMethodSelect();
                        if (identifier.getKind() == Tree.Kind.IDENTIFIER) {
                            methodName = ((IdentifierTree) identifier).getName().toString();
                            TreePath iPath = TreePath.getPath(cu, identifier);
                            TypeElement te = trees.getScope(iPath).getEnclosingClass();
                            methodClassType = ElementUtilities.getBinaryName(te);
                        } else {
                            methodName = ((MemberSelectTree) identifier).getIdentifier().toString();
                            ExpressionTree exp = ((MemberSelectTree) identifier).getExpression();
                            TreePath expPath = TreePath.getPath(cu, exp);
                            TypeMirror type = trees.getTypeMirror(expPath);
                            assert type.getKind() == TypeKind.DECLARED;
                            TypeElement te = (TypeElement) types.asElement(type);
                            methodClassType = ElementUtilities.getBinaryName(te);
                        }
                    }
                    pos = (int) sp.getStartPosition(cu, identifier);
                    EditorContext.Position methodStartPosition =
                            opCreationDelegate.createPosition(
                                    pos,
                                    (int) lineMap.getLineNumber(pos),
                                    (int) lineMap.getColumnNumber(pos)
                            );
                    pos = (int) sp.getEndPosition(cu, identifier);
                    EditorContext.Position methodEndPosition =
                            opCreationDelegate.createPosition(
                                    pos,
                                    (int) lineMap.getLineNumber(pos),
                                    (int) lineMap.getColumnNumber(pos)
                            );
                    /*
                    EditorContext.Operation op =
                            opCreationDelegate.createOperation(
                                    startPosition,
                                    endPosition,
                                    from
                            );
                     */
                    op = opCreationDelegate.createMethodOperation(
                                    startPosition,
                                    endPosition,
                                    methodStartPosition,
                                    methodEndPosition,
                                    methodName,
                                    methodClassType,
                                    from
                            );
                    //treeNodes.get(treeIndex).setCodeIndex(from);
                    from += getInstrSize(opcode, bytecodes, from);
                    operations.add(op);
                } else {
                    return null; // Mismatch
                }
            }
            if (op != null) {
                nodeOperations.put(node, op);
            }
        }
        // Check the rest of the bytecode for method calls:
        do {
            while (from < to) {
                int opcode = bytecodes[(int) from] & 0xFF;
                if (isMethodCall(opcode)) {
                    return null; // Mismatch
                }
                from += getInstrSize(opcode, bytecodes, from);
            }
            if ((indexesIndex + 2) < indexes.length) {
                indexesIndex += 2;
                from = indexes[indexesIndex];
                to = indexes[indexesIndex + 1];
            } else {
                break;
            }
        } while (true);
        /*
        // Assign next operations:
        for (int treeIndex = 0; treeIndex < length; treeIndex++) {
            Tree node = treeNodes.get(treeIndex);
            Set<Tree> nextNodes = info.getNextExpressions(node);
            if (nextNodes != null) {
                EditorContext.Operation op = nodeOperations.get(node);
                if (op == null) {
                    for (int backIndex = treeIndex - 1; backIndex >= 0; backIndex--) {
                        node = treeNodes.get(backIndex);
                        op = nodeOperations.get(node);
                        if (op != null) break;
                    }
                }
                if (op != null) {
                    assignNext(op, opCreationDelegate, info, nodeOperations,
                               nextNodes, treeNodes);
                }
            }
        }
         */
        return operations.toArray(new EditorContext.Operation[] {});
    }
    
    /*private static void assignNext(EditorContext.Operation op,
                                   OperationCreationDelegate opCreationDelegate,
                                   ExpressionScanner.ExpressionsInfo info,
                                   Map<Tree, EditorContext.Operation> nodeOperations,
                                   Set<Tree> nextNodes,
                                   List<Tree> allNodes) {
        for (Tree t : nextNodes) {
            EditorContext.Operation nextOp = nodeOperations.get(t);
            if (nextOp != null) {
                opCreationDelegate.addNextOperationTo(op, nextOp);
            } else {
                Set<Tree> nextNextNodes = info.getNextExpressions(t);
                if (nextNextNodes == null) {
                    boolean check = false;
                    for (int treeIndex = 0; treeIndex < allNodes.size(); treeIndex++) {
                        Tree node = allNodes.get(treeIndex);
                        if (check) {
                            nextNextNodes = info.getNextExpressions(node);
                            if (nextNextNodes != null) break;
                        } else {
                            if (t == node) {
                                check = true;
                            }
                        }
                    }
                } else {
                    assignNext(op, opCreationDelegate, info, nodeOperations, nextNextNodes, allNodes);
                }
            }
        }
    }*/
    
    private static boolean isMethodCall(int opcode) {
        return opcode >= 182 && opcode <= 185;
    }
    
    private static int getInstrSize(int opcode, byte[] bytecodes, long codeIndex) {
        if (opcode <= 15) return 1; // nop - dconst_1
        if (opcode == 16) return 2; // bipush <byte>
        if (opcode == 17) return 3; // sipush <byte1> <byte2>
        if (opcode == 18) return 2; // ldc <index>
        if (opcode <= 20) return 3; // ldc_w <byte1> <byte2>, ldc2_w <byte1> <byte2>
        if (opcode <= 25) return 2; // iload <index>, lload <index>, fload <index>, dload <index>, aload <index>
        if (opcode <= 53) return 1; // <x>load_<n>, <x>aload
        if (opcode <= 58) return 2; // <x>store <index>
        if (opcode <= 86) return 1; // <x>store_<n>, <x>astore
        if (opcode <= 94) return 1; // pop*, dup*
        if (opcode <= 131) return 1; // swap, <x>add, <x>sub, <x>mul, <x>div, <x>rem, <x>neg, *sh*, <x>and, <x>or, <x>xor
        if (opcode <= 132) return 3; // iinc <index> <const>
        if (opcode <= 147) return 1; // <x>2<x>
        if (opcode <= 152) return 1; // <x>cmp<x>
        if (opcode <= 168) return 3; // if<cond> <branchbyte1> <branchbyte2>, goto <branchbyte1> <branchbyte2>, jsr <branchbyte1> <branchbyte2>
        if (opcode <= 169) return 2; // ret <index>
        if (opcode == 170) return tableswitchSize(bytecodes, codeIndex);
        if (opcode == 171) return lookupswitchSize(bytecodes, codeIndex);
        if (opcode <= 177) return 1; // <x>return
        if (opcode <= 184) return 3; // <get/put><static/field> <byte1> <byte2>, invokevirtual, invokespecial, invokestatic
        if (opcode == 185) return 5; // invokeinterface
        // 186 not used
        if (opcode == 187) return 3; // new <byte1> <byte2>
        if (opcode == 188) return 2; // newarray <atype>
        if (opcode == 189) return 3; // anewarray <byte1> <byte2>
        if (opcode <= 191) return 1; // arraylength, athrow
        if (opcode <= 193) return 3; // checkcast <byte1> <byte2>, instanceof <byte1> <byte2>
        if (opcode <= 195) return 1; // monitorenter, monitorexit
        if (opcode == 196) return wideSize(bytecodes, codeIndex);
        if (opcode == 197) return 4; // multianewarray
        if (opcode <= 199) return 3; // ifnull/ifnonnull <byte1> <byte2>
        if (opcode <= 201) return 5; // goto_w/jsr_w 4x<byte>
        return 1; // reserved opcodes
    }
    
    private static int tableswitchSize(byte[] bytecodes, long codeIndex) {
        int padding = 4 - ((int) codeIndex % 4); // Following byte begins at an address that is a multiple of 4
        int pos = (int) codeIndex + padding;
        pos += 4; // default
        int low = readInt(bytecodes, pos);
        pos += 4; // low
        int high = readInt(bytecodes, pos);
        pos += 4; // high
        pos += (high - low + 1) << 2; // high - low + 1 32-bit offsets
        return pos - (int) codeIndex;
    }
    
    private static int lookupswitchSize(byte[] bytecodes, long codeIndex) {
        int padding = 4 - ((int) codeIndex % 4); // Following byte begins at an address that is a multiple of 4
        int pos = (int) codeIndex + padding;
        pos += 4; // default
        int npairs = readInt(bytecodes, pos);
        pos += 4; // npairs
        pos += npairs << 3; // npairs 2x32-bit numbers (pairs)
        return pos - (int) codeIndex;
    }
    
    private static int wideSize(byte[] bytecodes, long codeIndex) {
        int opcode = bytecodes[(int) codeIndex + 1] & 0xFF;
        if (opcode == 132) { // iinc
            return 6;
        } else {
            return 4;
        }
    }
    
    private static int readUnsignedShort(byte[] bytecodes, int pos) {
        return ((bytecodes[pos] & 0xFF) << 8) | (bytecodes[pos + 1] & 0xFF);
    }
    
    private static int readInt(byte[] bytecodes, int pos) {
        return  (bytecodes[pos    ] & 0xFF) << 24 |
                (bytecodes[pos + 1] & 0xFF) << 16 |
                (bytecodes[pos + 2] & 0xFF) << 8  |
                (bytecodes[pos + 3] & 0xFF);
    }

    
    static interface OperationCreationDelegate {
        
        /*
            EditorContext.Operation createOperation(
                    EditorContext.Position startPosition,
                    EditorContext.Position endPosition,
                    int bytecodeIndex);
         */
            
            EditorContext.Operation createMethodOperation(
                    EditorContext.Position startPosition,
                    EditorContext.Position endPosition,
                    EditorContext.Position methodStartPosition,
                    EditorContext.Position methodEndPosition,
                    String methodName, String methodClassType,
                    int bytecodeIndex);
            
            EditorContext.Position createPosition(int offset, int line, int column);
            
            void addNextOperationTo(EditorContext.Operation operation,
                                    EditorContext.Operation next);

    }
}
