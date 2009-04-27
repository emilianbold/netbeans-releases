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
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTParseFileWalker;

/**
 * A class that tracks states of the preprocessor conditionals within file
 * @author Vladimir Kvashin
 */
public final class FilePreprocessorConditionState
        implements APTParseFileWalker.EvalCallback {

    /** a SORTED array of offsets for which conditionals were evaluated to true */
    private int[] offsets;

    /** amount of states in the data array  */
    private int size;

    /** for debugging purposes */
    private transient CharSequence fileName;

    private static final int MIN_SIZE = 16;

    //private boolean isCpp;

    public FilePreprocessorConditionState(FileImpl file/*, APTPreprocHandler preprocHandler*/) {
        offsets = new int[MIN_SIZE];
        fileName = file.getBuffer().getAbsolutePath();
        //this.isCpp = preprocHandler.getMacroMap().isDefined("__cplusplus");
    }

    public FilePreprocessorConditionState(FilePreprocessorConditionState state2copy) {
        offsets = new int[state2copy.size];
        System.arraycopy(state2copy.offsets, 0, offsets, 0, offsets.length);
        this.fileName = state2copy.fileName;
    }
    
    public FilePreprocessorConditionState(DataInput input) throws IOException {
        size = input.readInt();
        if (size > 0) {
            offsets = new int[Math.max(size, MIN_SIZE)];
            for (int i = 0; i < size; i++) {
                offsets[i] = input.readInt();
            }
        } else {
            offsets = new int[MIN_SIZE];
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

    public void clear() {
        size = 0;
    }

    /**
     * Implements APTParseFileWalker.EvalCallback -
     * adds offset of active branch to offsets array
     */
    public void onEval(APT apt, boolean result) {
        boolean proceed = false;
        int offset = -1;
        if (result) {
            proceed = true;
            offset = apt.getOffset();
        } else {
            APT  sibling = apt.getNextSibling();
            if (sibling != null && sibling.getType() == APT.Type.ELSE) {
                proceed = true;
                offset = sibling.getOffset();
            }
        }
        if (proceed) {
            assert offset >= 0;
            addOffset(offset);
        }
    }

    private boolean addOffset(int offset) {
        if (size == 0) {
            offsets[0] = offset;
            size = 1;
        } else {
            int last = size - 1;
            if (offsets[last] < offset) {
                insert(last + 1, offset);
            } else {
                for (int i = last - 1; i > 0; i--) {
                    if (offset > offsets[i]) {
                        insert(i + 1, offset);
                        return true;
                    }
                }
                insert(0, offset);
            }
        }
        return false;
    }

    private void insert(int index, int value) {
        if (index > size) {
            throw new IllegalArgumentException("Index: " + index + " shouldn't be greater than " + size); //NOI18N
        }
        // ensure the array is capable of storing a new value
        if (size >= offsets.length) {
            int newSize = (size * 3)/2 + 1;
            int[] newData = new int[newSize];
            System.arraycopy(offsets, 0, newData, 0, offsets.length);
            offsets = newData;
        }
        // store the value
        if (index < size) { // inserting
            System.arraycopy(offsets, index, offsets, index+1, offsets.length-index+1);
        }
        offsets[index] = value;
        size++;
    }

    public final boolean isBetter(FilePreprocessorConditionState other) {
        int result = compareToImpl(other);
        if (TraceFlags.TRACE_PC_STATE || TraceFlags.TRACE_PC_STATE_COMPARISION) {
            traceComparison(other, result);
        }
        return result > 0;
    }
    
    public final boolean isEqual(FilePreprocessorConditionState other) {
        if (this == other) {
            return true;
        }
        // we assume that the array is ordered
        if (this.size == other.size) {
            for (int i = 0; i < size; i++) {
                if (this.offsets[i] != other.offsets[i]) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public final boolean isSubset(Collection<FilePreprocessorConditionState> others) {
        SortedSet<Integer> sorted = new TreeSet<Integer>();
        for (FilePreprocessorConditionState state : others) {
            if (state == null) {
                return false;
            }
            for (int i = 0; i < state.size; i++) {
                sorted.add(state.offsets[i]);
            }
        }
        int[] arr = new int[sorted.size()];
        int pos = 0;
        for (int offset : sorted) {
            arr[pos++] = offset;
        }
        return isSubset(arr, arr.length);
    }

    public final boolean isSubset(FilePreprocessorConditionState other) {
        return (other == null) ? false : isSubset(other.offsets, other.size);
    }

    public final boolean isSubset(int[] otherOffsets, int otherSize) {
        // we assume that the array is ordered
        if (this.size <= otherSize) {
            int thisPos = 0;
            int otherPos = 0;
            outer:
            while (thisPos < size && otherPos < otherSize) {
                // on each iteration we assume
                // that all on the left of the current position
                // this is a subset of other
                if (this.offsets[thisPos] == otherOffsets[otherPos]) {
                    thisPos++;
                    otherPos++;
                    continue;
                } else if (this.offsets[thisPos] < otherOffsets[otherPos]) {
                    return false;
                } else { // this.offsets[thisPos] > other.offsets[thisPos]
                    while (++otherPos < otherSize) {
                        if (this.offsets[thisPos] == otherOffsets[otherPos]) {
                            thisPos++;
                            otherPos++;
                            continue outer;
                        }
                    }
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void traceComparison(FilePreprocessorConditionState other, int result) {
        System.err.printf("compareTo (%s): %s %s %s \n", fileName, toStringBrief(this),
                (result < 0) ? "<" : ((result > 0) ? ">" : "="), // NOI18N
                toStringBrief(other)); //NOI18N
    }

    private int compareToImpl(FilePreprocessorConditionState other) {
        if (other == this) {
            return 0;
        } else if (other == null) {
            return (size > 0) ? 1 : 0; // null and empty are the same
        } else {
            if (this.size != other.size) {
                // longer is the array, more branches were active
                return this.size - other.size;
            } else {
                // for now we don't care if counts are equal;
                // the code below is witten just for the sake of stability
                for (int i = 0; i < size; i++) {
                    if (this.offsets[i] != other.offsets[i]) {
                        return other.offsets[i] - this.offsets[i];
                    }
                }
                return 0;
            }
        }
    }

    @Override
    public String toString() {
//        StringBuilder sb = new StringBuilder(); // "FilePreprocessorConditionState "
//        sb.append(fileName);
//        sb.append(' ');
//        sb.append(toStringBrief(this));
//        sb.append(" size=" + size); //NOI18N
//        return sb.toString();
        return toStringBrief(this);
    }

    /*package*/ static String toStringBrief(FilePreprocessorConditionState state) {
        if (state == null) {
            return "null"; // NOI18N
        }
        StringBuilder sb = new StringBuilder(/*state.isCpp ? "c++ " : "c   "*/); // NOI18N
        sb.append("["); // NOI18N
        for (int i = 0; i < state.size; i++) {
            if (i > 0) {
                sb.append(", "); //NOI18N
            }
            sb.append(state.offsets[i]);
        }
        sb.append("]"); //NOI18N
        return sb.toString();
    }

    void trimSize() {
        int[] newOffsets = new int[size];
        System.arraycopy(this.offsets, 0, newOffsets, 0, size);
        this.offsets = newOffsets;
    }

    public boolean isInActiveBlock(int startContext, int endContext) {
        return true;
    }
}
