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

package org.netbeans.modules.debugger.jpda.expr;

import com.sun.jdi.*;

/**
 * Helper class containing methods that evaluate various unary and binary Java operators.
 *
 * @author Maros Sandor
 */
class Operators implements JavaParserConstants {

    private VirtualMachine      vm;

    public Operators(VirtualMachine vm) {

        this.vm = vm;
    }

    public PrimitiveValue evaluate(Token operator, PrimitiveValue value) {
        switch (operator.kind) {
        case BANG:
            if (value instanceof BooleanValue) {
                return vm.mirrorOf(!value.booleanValue());
            }
            break;

        case TILDE:
            if (value instanceof BooleanValue || value instanceof DoubleValue || value instanceof FloatValue) break;
            if (value instanceof LongValue) {
                return vm.mirrorOf(~value.longValue());
            } else {
                return vm.mirrorOf(~value.intValue());
            }

        case PLUS:
            if (value instanceof BooleanValue) break;
            return value;

        case MINUS:
            if (value instanceof BooleanValue) break;
            if (value instanceof DoubleValue) {
                return vm.mirrorOf(-value.doubleValue());
            } else if (value instanceof FloatValue) {
                return vm.mirrorOf(-value.floatValue());
            } else if (value instanceof LongValue) {
                return vm.mirrorOf(-value.longValue());
            } else {
                return vm.mirrorOf(-value.intValue());
            }

        case DECR:
        case INCR:
            // NOTE: Values of variables are not changed by this operator !
            byte val = operator.kind == INCR ? 1 : (byte)-1;
            if (value instanceof BooleanValue) break;
            if (value instanceof DoubleValue) {
                return vm.mirrorOf(value.doubleValue() + val);
            } else if (value instanceof FloatValue) {
                return vm.mirrorOf(value.floatValue() + val);
            } else if (value instanceof LongValue) {
                return vm.mirrorOf(value.longValue() + val);
            } else {
                return vm.mirrorOf(value.intValue() + val);
            }

        }
        throw new IllegalArgumentException("badUnarySemantics");
    }

    public Value evaluate(Value left, Token operator, Value right) {

        if ((left instanceof BooleanValue) && (right instanceof BooleanValue)) {
            boolean op1 = ((BooleanValue)left).booleanValue();
            boolean op2 = ((BooleanValue)right).booleanValue();
            boolean res = false;
            switch (operator.kind) {
                case EQ: res = op1 == op2; break;
                case NE: res = op1 != op2; break;
                case BIT_OR: res = op1 | op2; break;
                case BIT_AND: res = op1 & op2; break;
                case XOR: res = op1 ^ op2; break;
                default:
                    throw new IllegalArgumentException("badBinarySemantics");
            }
            return vm.mirrorOf(res);
        } // (boolean, bin_op, boolean)

        boolean isLeftNumeric = left instanceof PrimitiveValue && !(left instanceof BooleanValue);
        boolean isRightNumeric = right instanceof PrimitiveValue && !(right instanceof BooleanValue);

        if (isLeftNumeric && isRightNumeric) {
            switch (operator.kind) {
                case PLUS: // +
                case MINUS: // -
                case REM: // %
                case STAR: // *
                case SLASH: // /
                    return evaluateAddOperator((PrimitiveValue) left, (PrimitiveValue) right, operator.kind);

                case LSHIFT: // <<
                case RSIGNEDSHIFT: // >>
                case RUNSIGNEDSHIFT: // >>>
                    return evaluateShiftOperator((PrimitiveValue) left, (PrimitiveValue) right, operator.kind);

                case XOR:
                case BIT_AND:
                case BIT_OR:
                    return evaluateBitOperator((PrimitiveValue) left, (PrimitiveValue) right, operator.kind);

                case LT: // <
                case GT: // >
                case LE: // <=
                case GE: // >=
                case EQ: // ==
                case NE: // !=
                    return evaluateComparisonOperator((PrimitiveValue) left, (PrimitiveValue) right, operator.kind);
            }
        } // (number, bin_op, number)

        if ((operator.kind == EQ) || (operator.kind == NE)) {
            if (left == null) {
                if (right == null)
                    return vm.mirrorOf(operator.kind == EQ);
                if (right instanceof ObjectReference)
                    return vm.mirrorOf(operator.kind == NE);
            }
            if (left instanceof ObjectReference) {
                if (right == null)
                    return vm.mirrorOf(operator.kind == NE);
                if (right instanceof ObjectReference)
                    if (operator.kind == EQ)
                        return vm.mirrorOf(right.equals(left));
                    else return vm.mirrorOf( ! right.equals(left));
            }
        }

        if ((left == null || left instanceof StringReference || right == null || right instanceof StringReference)
            && operator.kind == PLUS) {
            String s1 = null, s2 = null;
            if (left instanceof StringReference) s1 = ((StringReference)left).value();
            else if (left == null) s1 = "null";
            else s1 = left.toString();
            if (right instanceof StringReference) s2 = ((StringReference)right).value();
            else if (right == null) s2 = "null";
            else s2 = right.toString();
            return vm.mirrorOf(s1 + s2);
        }

        throw new IllegalArgumentException("Bad semantics for binary operator");
    }

    private PrimitiveValue evaluateAddOperator(PrimitiveValue op1, PrimitiveValue op2, int id) {
        if ((op1 instanceof DoubleValue) || (op2 instanceof DoubleValue)) {
            double d1 = op1.doubleValue ();
            double d2 = op2.doubleValue ();
            double res = 0;
            switch (id) {
                case PLUS: res = d1 + d2; break;
                case MINUS: res = d1 - d2; break;
                case REM: res = d1 % d2; break;
                case STAR: res = d1 * d2; break;
                case SLASH: res = d1 / d2; break;
            } // switch (id)
            return vm.mirrorOf(res);
        }
        if ((op1 instanceof FloatValue) || (op2 instanceof FloatValue)) {
            float f1 = op1.floatValue ();
            float f2 = op2.floatValue ();
            float res_f = 0;
            switch (id) {
                case PLUS: res_f = f1 + f2; break;
                case MINUS: res_f = f1 - f2; break;
                case REM: res_f = f1 % f2; break;
                case STAR: res_f = f1 * f2; break;
                case SLASH: res_f = f1 / f2; break;
            } // switch (id)
            return vm.mirrorOf(res_f);
        }
        if ((op1 instanceof LongValue) || (op2 instanceof LongValue)) {
            long long1 = op1.longValue ();
            long long2 = op2.longValue ();
            long res_long = 0;
            switch (id) {
                case PLUS: res_long = long1 + long2; break;
                case MINUS: res_long = long1 - long2; break;
                case REM: res_long = long1 % long2; break;
                case STAR: res_long = long1 * long2; break;
                case SLASH: res_long = long1 / long2; break;
            } // switch (id)
            return vm.mirrorOf(res_long);
        }
        int i1 = op1.intValue ();
        int i2 = op2.intValue ();
        int res_i = 0;
        switch (id) {
            case PLUS: res_i = i1 + i2; break;
            case MINUS: res_i = i1 - i2; break;
            case REM: res_i = i1 % i2; break;
            case STAR: res_i = i1 * i2; break;
            case SLASH: res_i = i1 / i2; break;
        } // switch (id)
        return vm.mirrorOf(res_i);
    }

    private PrimitiveValue evaluateShiftOperator(PrimitiveValue op1, PrimitiveValue op2, int id) {
        if ((op1 instanceof FloatValue) || (op1 instanceof DoubleValue) ||
            (op2 instanceof FloatValue) || (op2 instanceof DoubleValue)) {
                throw new IllegalArgumentException("Bad semantics for shift operator");
        }
        if (op1 instanceof LongValue) {
            long n1 = op1.longValue ();
            long n2 = op2.longValue ();
            long res = 0;
            switch (id) {
                case LSHIFT: res = n1 << n2; break;
                case RSIGNEDSHIFT: res = n1 >> n2; break;
                case RUNSIGNEDSHIFT: res = n1 >>> n2; break;
            }
            return vm.mirrorOf(res);
        } else {
            int i1 = op1.intValue ();
            long i2 = op2.longValue ();
            int res_i = 0;
            switch (id) {
                case LSHIFT: res_i = i1 << i2; break;
                case RSIGNEDSHIFT: res_i = i1 >> i2; break;
                case RUNSIGNEDSHIFT: res_i = i1 >>> i2; break;
            }
            return vm.mirrorOf(res_i);
        }
    }

    private PrimitiveValue evaluateBitOperator(PrimitiveValue op1, PrimitiveValue op2, int id)  {
        if ((op1 instanceof FloatValue) || (op1 instanceof DoubleValue) ||
            (op2 instanceof FloatValue) || (op2 instanceof DoubleValue)) {
                throw new IllegalArgumentException("Bad semantics for bit operator");
        }
        if ((op1 instanceof LongValue) || (op2 instanceof LongValue)) {
            long n1 = op1.longValue ();
            long n2 = op2.longValue ();
            long res = 0;
            switch (id) {
                case BIT_AND: res = n1 & n2; break;
                case BIT_OR: res = n1 | n2; break;
                case XOR: res = n1 ^ n2; break;
            }
            return vm.mirrorOf(res);
        } else {
            int i1 = op1.intValue ();
            int i2 = op2.intValue ();
            int res_i = 0;
            switch (id) {
                case BIT_AND: res_i = i1 & i2; break;
                case BIT_OR: res_i = i1 | i2; break;
                case XOR: res_i = i1 ^ i2; break;
            }
            return vm.mirrorOf(res_i);
        }
    }

    private BooleanValue evaluateComparisonOperator(PrimitiveValue op1, PrimitiveValue op2, int id) {
        boolean res = false;
        if ((op1 instanceof DoubleValue) || (op2 instanceof DoubleValue)) {
            double d1 = op1.doubleValue ();
            double d2 = op2.doubleValue ();
            switch (id) {
                case LT: res = d1 < d2; break;
                case GT: res = d1 > d2; break;
                case LE: res = d1 <= d2; break;
                case GE: res = d1 >= d2; break;
                case EQ: res = d1 == d2; break;
                case NE: res = d1 != d2; break;
            } // switch (id)
        } // if
        else if ((op1 instanceof FloatValue) || (op2 instanceof FloatValue)) {
            float f1 = op1.floatValue ();
            float f2 = op2.floatValue ();
            switch (id) {
                case LT: res = f1 < f2; break;
                case GT: res = f1 > f2; break;
                case LE: res = f1 <= f2; break;
                case GE: res = f1 >= f2; break;
                case EQ: res = f1 == f2; break;
                case NE: res = f1 != f2; break;
            } // switch (id)
        } // if
        else if ((op1 instanceof LongValue) || (op1 instanceof LongValue)) {
            long n1 = op1.longValue ();
            long n2 = op2.longValue ();
            switch (id) {
                case LT: res = n1 < n2; break;
                case GT: res = n1 > n2; break;
                case LE: res = n1 <= n2; break;
                case GE: res = n1 >= n2; break;
                case EQ: res = n1 == n2; break;
                case NE: res = n1 != n2; break;
            } // switch (id)
        } // if
          else
        {
        float i1 = op1.intValue ();
            float i2 = op2.intValue ();
            switch (id) {
                case LT: res = i1 < i2; break;
                case GT: res = i1 > i2; break;
                case LE: res = i1 <= i2; break;
                case GE: res = i1 >= i2; break;
                case EQ: res = i1 == i2; break;
                case NE: res = i1 != i2; break;
            } // switch (id)
        }
        return vm.mirrorOf(res);
    }
}
