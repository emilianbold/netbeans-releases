/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTParseFileWalker;

/**
 * A class that tracks states of the preprocessor conditionals within file
 * @author Vladimir Voskresenskky
 */
public final class FilePreprocessorDeadConditionState implements APTParseFileWalker.EvalCallback {

    /** a SORTED array of blocks [start-end] for which conditionals were evaluated to false */
    private int[] offsets;

    /** amount of dead blocks (*2)  in the data array  */
    private int size;

    /** for debugging purposes */
    private final transient CharSequence fileName;

    public FilePreprocessorDeadConditionState(FileImpl file) {
        this(file.getBuffer().getAbsolutePath());
    }

    // for internal use and tests
    /*package*/FilePreprocessorDeadConditionState(CharSequence fileName) {
        offsets = new int[512];
        this.fileName = fileName;
    }

    public FilePreprocessorDeadConditionState(FilePreprocessorDeadConditionState state2copy) {
        offsets = new int[state2copy.size];
        System.arraycopy(state2copy.offsets, 0, offsets, 0, offsets.length);
        this.fileName = state2copy.fileName;
    }
    
    public FilePreprocessorDeadConditionState(DataInput input) throws IOException {
        size = input.readInt();
        if (size > 0) {
            offsets = new int[size];
            for (int i = 0; i < size; i++) {
                offsets[i] = input.readInt();
            }
        } else {
            offsets = new int[0];
        }
        fileName = null;
    }

    public void write(DataOutput output) throws IOException {
        output.writeInt(size);
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                output.writeInt(offsets[i]);
            }
        }
    }

    /**
     * Implements APTParseFileWalker.EvalCallback -
     * adds offset of dead branch to offsets array
     */
    public void onEval(APT apt, boolean result) {
        if (result) {
            // if condition was evaluated as 'true' check if we
            // need to mark siblings as dead blocks
            APT start = apt.getNextSibling();
            deadBlocks:
            while (start != null) {
                APT end = start.getNextSibling();
                if (end != null) {
                    switch (end.getType()) {
                        case APT.Type.ELIF:
                        case APT.Type.ELSE:
                            addDeadBlock(start, end);
                            // continue
                            start = end;
                            break;
                        case APT.Type.ENDIF:
                            addDeadBlock(start, end);
                            // stop
                            start = null;
                            break;
                        default:
                            // stop
                            start = null;
                            break;
                    }
                }
            }
        } else {
            // if condition was evaluated as 'false' mark it as dead block
            APT end = apt.getNextSibling();
            if (end != null) {
                switch (end.getType()) {
                    case APT.Type.ELIF:
                    case APT.Type.ELSE:
                    case APT.Type.ENDIF:
                        addDeadBlock(apt, end);
                        break;
                }
            }
        }
    }

//    /**
//     * check if "this" object is better than "other".
//     * If returns "false" it doens't mean that other is "better", only that "this" is not "better"
//     * @param other anotehr state to compare with
//     * @return returns "true" only if "this" is "better" than other.
//     *         returns "false" if "this" is not better or they are not comparable
//     */
//    public final boolean isBetter(FilePreprocessorDeadConditionState other) {
//        boolean result = false;
//        if (other == this) {
//            // same is not better
//            result = false;
//        } else if (other == null) {
//            // not comparable
//            result = false;
//        } else {
//            // "this" is better only if it can be used instead of "other"
//            result = isFirstCanReplaceSecond(this.offsets, this.size, other.offsets, other.size);
//        }
//        if (TraceFlags.TRACE_PC_STATE || TraceFlags.TRACE_PC_STATE_COMPARISION) {
//            traceComparison(other, result ? 1 : 0);
//        }
//        return result;
//    }
//
//    public final boolean isEqual(FilePreprocessorDeadConditionState other) {
//        if (this == other) {
//            return true;
//        }
//        // we assume that the array is ordered
//        if (this.size == other.size) {
//            for (int i = 0; i < size; i++) {
//                if (this.offsets[i] != other.offsets[i]) {
//                    return false;
//                }
//            }
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    private void traceComparison(FilePreprocessorDeadConditionState other, int result) {
//        System.err.printf("compareTo (%s): %s %s %s \n", fileName, toStringBrief(this),
//                (result < 0) ? "<" : ((result > 0) ? ">" : "="), // NOI18N
//                toStringBrief(other)); //NOI18N
//    }

    @Override
    public String toString() {
        return toStringBrief(this);
    }

    /*package*/ static String toStringBrief(FilePreprocessorDeadConditionState state) {
        if (state == null) {
            return "null"; // NOI18N
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");//NOI18N
        for (int i = 0; i < state.size; i+=2) {
            if (i > 0) {
                sb.append("][");//NOI18N
            }
            sb.append(state.offsets[i]);
            sb.append("-");//NOI18N
            sb.append(state.offsets[i+1]);
        }
        sb.append("]");//NOI18N
        return sb.toString();
    }

    public boolean isInActiveBlock(int startContext, int endContext) {
        if (size == 0 || startContext == 0) {
            return true;
        }
        // TODO: improve speed, if needed
        for (int i = 0; i < size; i+=2) {
            int start = offsets[i];
            int end = offsets[i+1];
            if (start <= startContext && startContext <= end) {
                return false;
            }
            if (start <= endContext && endContext <= end) {
                return false;
            }
        }
        return true;
    }

    /**
     * check if this state can be used in-place of other (it is superset or the same as other)
     * @param other
     * @return
     */
    public final boolean canReplaceOther(FilePreprocessorDeadConditionState other) {
        if (other == null) {
            return false;
        }
        if (this.size == 0) {
            // can replace only if not empty as well
            return other.size > 0;
        }
        if (other.size == 0) {
            // not empty can not replace empty
            return false;
        }
        // TODO: improve speed, if needed
        for (int i = 0; i < other.size; i += 2) {
            int secondStart = other.offsets[i];
            int secondEnd = other.offsets[i + 1];
            if (!canReplaceBlock(secondStart, secondEnd, true)) {
                return false;
            }
        }
        for (int i = 0; i < this.size; i += 2) {
            int firstStart = this.offsets[i];
            int firstEnd = this.offsets[i + 1];
            if (other.isInActiveBlock(firstStart, firstEnd)) {
                return false;
            }
        }
        return true;
    }

    /**
     * check if this state can be completely replaced by composition of other states (but not equal to them)
     * @param other
     * @return
     */
    public final boolean canBeReplacedByComposition(Collection<FilePreprocessorDeadConditionState> others) {
        if (this.size == 0) {
            // empty can be replaced only by composition containing empty as well
            for (FilePreprocessorDeadConditionState state : others) {
                if (state != null && state.size == 0) {
                    return true;
                }
            }
            return false;
        }
        for (int i = 0; i < size; i += 2) {
            // check each block of this state and detect if it can be replaced by at least one of other states
            int start = offsets[i];
            int end = offsets[i + 1];
            boolean isBlockReplaceable = false;
            for (FilePreprocessorDeadConditionState state : others) {
                if (state != null && state.canReplaceBlock(start, end, false)) {
                    isBlockReplaceable = true;
                    break;
                }
            }
            if (!isBlockReplaceable) {
                return false;
            }
        }
        return true;
    }

    /**
     * checks if other block is completely replaceable by this state:
     * it is either larger than existing dead block of this state
     * or checked block is placed in active area of this state
     * @param otherBlockStart
     * @param otherBlockEnd
     * @return
     */
    private boolean canReplaceBlock(int checkBlockStart, int checkBlockEnd, boolean allowEqual) {
        // empty blocks area can replace everything
        Boolean isBlockReplaceableCheck = this.size == 0 ? Boolean.TRUE : null;
        for (int i = 0; i < this.size; i += 2) {
            int blockStart = this.offsets[i];
            int blockEnd = this.offsets[i + 1];
            if (allowEqual) {
                if (checkBlockStart <= blockStart && blockEnd <= checkBlockEnd) {
                    // dead block area is inside or equal of the check block => the checked block is replaceable
                    isBlockReplaceableCheck = Boolean.TRUE;
                    break;
                }
            } else {
                if (checkBlockStart < blockStart && blockEnd < checkBlockEnd) {
                    // dead block area is inside the check block => the checked block is replaceable
                    isBlockReplaceableCheck = Boolean.TRUE;
                    break;
                }
            }
            if (blockStart < checkBlockStart && checkBlockEnd < blockEnd) {
                // the checked dead block is inside one of dead blocks area => the checked block is not replaceable
                isBlockReplaceableCheck = Boolean.FALSE;
                break;
            }
            // else it's still a candidate to be replaced because stay in active area (out of dead blocks)
        }
        if (isBlockReplaceableCheck == Boolean.FALSE) {
            return false;
        }
        return true;
    }

    /*package-local*/void trimSize() {
        int[] newOffsets = new int[size];
        System.arraycopy(this.offsets, 0, newOffsets, 0, size);
        this.offsets = newOffsets;
    }

    private void addDeadBlock(APT startNode, APT endNode) {
        if (startNode != null && endNode != null) {
            int startDeadBlock = startNode.getEndOffset();
            int endDeadBlock = endNode.getOffset() - 1;
            addBlockImpl(startDeadBlock, endDeadBlock);
        }
    }

    /*package*/final void addBlockImpl(int startDeadBlock, int endDeadBlock) {
        if (endDeadBlock > startDeadBlock) {
            if (size == this.offsets.length) {
                // expand
                int[] newOffsets = new int[2 * (this.offsets.length + 1)];
                System.arraycopy(this.offsets, 0, newOffsets, 0, size);
                this.offsets = newOffsets;
            }
            this.offsets[size++] = startDeadBlock;
            this.offsets[size++] = endDeadBlock;
        }
    }

}
