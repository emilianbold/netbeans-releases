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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby;

import java.util.List;
import org.jruby.ast.ArgsCatNode;
import org.jruby.ast.ArgsNode;
import org.jruby.ast.ArgumentNode;
import org.jruby.ast.ArrayNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.DefsNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.Node;
import org.jruby.ast.SplatNode;
import org.jruby.ast.VCallNode;


/**
 * The arity of a method is the number of arguments it takes.
 *
 * JRuby already has an Arity class (org.jruby.runtime.Arity), but
 * it doesn't have all we need - such as a maximum number of arguments.
 *
 * @todo I handle optional arguments and splats (*), but what about blocks?
 *      def foo(arg1, arg2 = deflt, *rest, &block)
 *
 * @author Tor Norbye
 */
public final class Arity {

    /**
     * Represents unknown arity, for example the arity of a method that
     * we're referring to as a symbol, such as "private :foo".
     */
    public static final Arity UNKNOWN = new Arity(-1, -1);

    /**
     * Minimum number of arguments required by this method
     */
    private int min;

    /**
     * Maximum number of arguments required by this method. Use {@link Integer#MAX_VALUE} to denote no
     * upper bound (e.g. *args).
     */
    private int max;

    private Arity(int min, int max) {
        // Examples:
        //  call:   foo(bar,baz)   ->  Arity(2,2)
        //  call:   foo(bar,*baz)  ->  Arity(1,Integer.MAX_VALUE)
        //  method: foo(bar,baz)   ->  Arity(2,2)
        //  method: foo(bar,baz=5) ->  Arity(1,2)
        this.min = min;
        this.max = max;
    }
    
    public int getMinArgs() {
        return min;
    }
    
    public int getMaxArgs() {
        return max;
    }

    public static Arity getCallArity(Node call) {
        assert call instanceof CallNode || call instanceof VCallNode || call instanceof FCallNode;

        Arity arity = new Arity(0, 0);
        arity.initializeFromCall(call);

        if (arity.min == -1) {
            return UNKNOWN;
        } else {
            return arity;
        }
    }

    public static boolean callHasArguments(Node call) {
        assert call instanceof CallNode || call instanceof VCallNode || call instanceof FCallNode;

        return getCallArity(call).min > 0;
    }
    
    @SuppressWarnings(value = "unchecked")
    private void initializeFromCall(Node node) {
        if (node instanceof FCallNode) {
            Node argsNode = ((FCallNode)node).getArgsNode();

            initializeFromCall(argsNode);
        } else if (node instanceof LocalAsgnNode) {
            if (max < Integer.MAX_VALUE) {
                max++;
            }
        } else if (node instanceof ArgsCatNode) {
            ArgsCatNode args = (ArgsCatNode)node;
            initializeFromCall(args.getFirstNode());
            // ArgsCatNode seems to be used only to append splats
            // but I don't get a splat node. So just pretend I did.
            //initializeFromCall(args.getSecondNode());
            max = Integer.MAX_VALUE;
        } else if (node instanceof ArgsNode) {
            ArgsNode args = (ArgsNode)node;

            if (args != null) {
                int value = args.getArity().getValue();

                if (value < 0) {
                    value = -(1 + value);
                }

                min = max = value;
            }
        } else if (node instanceof SplatNode) {
            // Flexible number of arguments
            max = Integer.MAX_VALUE;
        } else if (node instanceof CallNode) {
            Node argsNode = ((CallNode)node).getArgsNode();
            initializeFromCall(argsNode);
        } else if (node instanceof VCallNode) {
            List<Node> children = node.childNodes();

            for (Node child : children) {
                initializeFromCall(child);
            }
        } else if (node instanceof ListNode) {
            List<Node> children = node.childNodes();

            for (Node child : children) {
                if (child instanceof VCallNode) {
                    // The argument is a method call - just count it as a regular argument
                    min++;

                    if (max < Integer.MAX_VALUE) {
                        max++;
                    }
                } else if (child instanceof ArrayNode) {
                    // This is an array inside an array - this is a single argument
                    min++;

                    if (max < Integer.MAX_VALUE) {
                        // TODO: Consider walking through the elements here in case the array can be
                        // matched up
                        max++;
                    }
                } else {
                    initializeFromCall(child);
                }
            }
        } else {
            //assert (node instanceof LocalVarNode || node instanceof SymbolNode || node instanceof FixnumNode || ...
            min++;

            if (max < Integer.MAX_VALUE) {
                max++;
            }
        }
    }

    @SuppressWarnings(value = "unchecked")
    public static Arity getDefArity(Node method) {
        assert method instanceof DefsNode || method instanceof DefnNode;

        Arity arity = new Arity(0, 0);

        List<Node> nodes = (List<Node>)method.childNodes();

        for (Node c : nodes) {
            if (c instanceof ArgsNode) {
                arity.initializeFromDef(c);

                break;
            }
        }

        if (arity.min == -1) {
            return UNKNOWN;
        } else {
            return arity;
        }
    }

    @SuppressWarnings(value = "unchecked")
    private void initializeFromDef(Node node) {
        if (node instanceof ArgsNode) {
            ArgsNode argsNode = (ArgsNode)node;

            if (argsNode.getArgs() != null) {
                initializeFromDef(argsNode.getArgs());
            }

            if (argsNode.getOptArgs() != null) {
                initializeFromDef(argsNode.getOptArgs());
            }

            if (argsNode.getRestArg() > 0) {
                max = Integer.MAX_VALUE;
            }

            // TODO: Block arg node. Not sure how this should affect arity.
            //argsNode.getBlockArgNode()
        } else if (node instanceof ArgumentNode) {
            min++;
            max++;
        } else if (node instanceof LocalAsgnNode) {
            max++;
        } else if (node instanceof ListNode) {
            List<Node> children = node.childNodes();

            for (Node child : children) {
                initializeFromDef(child);
            }
        }
    }

    /**
     * Return true iff the given call arity matches the given method arity.
     *
     * This isn't fool-proof; if you're passing around "*args" I treat
     * these as unknown sizes that will match - but at runtime it can fail.
     * For example, def jump(*args) test(*args) end; def test(foo) end; jump
     * will look compatible but fail since you will wind up calling test with
     * 0 arguments...
     */
    public static boolean matches(Arity call, Arity method) {
        // If we don't know the arity for either side, consider it a match.
        // This may mean we get false positives (e.g. a method call doesn't
        // really a hit a method, so we may think it is used where it is not)
        if ((call == UNKNOWN) || (method == UNKNOWN)) {
            return true;
        }

        if (call.max < method.min) {
            return false;
        }

        if (call.max == Integer.MAX_VALUE) {
            // Unknown number of args - assume it's okay
            return true;
        }

        if (call.max > method.max) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "Arity(" + min + ":" + ((max == Integer.MAX_VALUE) ? "unlimited" : max) + ")";
    }

    public static Arity createTestArity(int min, int max) {
        return new Arity(min, max);
    }
}
