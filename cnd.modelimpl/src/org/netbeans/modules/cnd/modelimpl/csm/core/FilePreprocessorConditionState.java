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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTParseFileWalker;

/**
 * A class that tracks states of the preprocessor conditionals within file
 * @author Vladimir Voskresenskky
 */
public final class FilePreprocessorConditionState implements APTParseFileWalker.EvalCallback {

    /** a SORTED array of blocks [start-end] for which conditionals were evaluated to false */
    private int[] offsets;

    /** amount of dead blocks (*2)  in the data array  */
    private int size;

    /** for debugging purposes */
    private final transient CharSequence fileName;

    public FilePreprocessorConditionState(FileImpl file) {
        this(file.getBuffer().getAbsolutePath());
    }

    // for internal use and tests
    /*package*/FilePreprocessorConditionState(CharSequence fileName) {
        offsets = new int[512];
        this.fileName = fileName;
    }

    public FilePreprocessorConditionState(FilePreprocessorConditionState state2copy) {
        offsets = new int[state2copy.size];
        System.arraycopy(state2copy.offsets, 0, offsets, 0, offsets.length);
        this.fileName = state2copy.fileName;
    }
    
    public FilePreprocessorConditionState(DataInput input) throws IOException {
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
                } else {
                    break;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FilePreprocessorConditionState other = (FilePreprocessorConditionState) obj;
        if (this.size != other.size) {
            return false;
        }
        if (!Arrays.equals(this.offsets, other.offsets)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + this.size;
        hash = 67 * hash + Arrays.hashCode(this.offsets);
        return hash;
    }

    @Override
    public String toString() {
        return toStringBrief(this);
    }

    /*package*/ static String toStringBrief(FilePreprocessorConditionState state) {
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
        // TODO: improve speed, if needed, offsets are ordered
        for (int i = 0; i < size; i += 2) {
            int start = offsets[i];
            int end = offsets[i + 1];
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
     * check if this state can be used to replace another (it is better or equal to another)
     * @param other
     * @return
     */
    public final boolean isBetterOrEqual(FilePreprocessorConditionState other) {
        if (other == null) {
            return false;
        }
        if (this.size == 0) {
            // can replace only if not empty as well
            return other.size > 0;
        }
        if (other.size == 0) {
            // this not empty can not replace empty
            return false;
        }
        // check if all my blocks are inactive in terms of other (but not equal to them)
        for (int i = 0; i < size; i += 2) {
            int start = offsets[i];
            int end = offsets[i + 1];
            boolean active = true;
            for (int j = 0; j < other.size; j += 2) {
                int secondStart = other.offsets[j];
                int secondEnd = other.offsets[j + 1];
                if (secondStart <= start && end <= secondEnd) {
                    // not in active
                    active = false;
                } else if (start < secondStart && secondEnd < end) {
                    // our dead block is bigger
                    return false;
                }
                if (!active || (end < secondStart)) {
                    // we can stop, because blocks are sorted
                    break;
                }
            }
            // our block is active => we can't be the best
            if (active) {
                return false;
            }
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
