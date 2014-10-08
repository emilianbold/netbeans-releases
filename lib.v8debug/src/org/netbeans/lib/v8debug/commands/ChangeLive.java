/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.lib.v8debug.commands;

import org.netbeans.lib.v8debug.PropertyBoolean;
import org.netbeans.lib.v8debug.V8Arguments;
import org.netbeans.lib.v8debug.V8Body;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;

/**
 *
 * @author Martin Entlicher
 */
public final class ChangeLive {
    
    private ChangeLive() {}
    
    public static V8Request createRequest(long sequence, long scriptId, String newSource) {
        return createRequest(sequence, scriptId, newSource, null);
    }
    
    public static V8Request createRequest(long sequence, long scriptId, String newSource, Boolean previewOnly) {
        return new V8Request(sequence, V8Command.Changelive, new Arguments(scriptId, newSource, previewOnly));
    }
    
    public static final class Arguments extends V8Arguments {
        
        private final long scriptId;
        private final String newSource;
        private final PropertyBoolean previewOnly;
        
        public Arguments(long scriptId, String newSource, Boolean previewOnly) {
            this.scriptId = scriptId;
            this.newSource = newSource;
            this.previewOnly = new PropertyBoolean(previewOnly);
        }

        public long getScriptId() {
            return scriptId;
        }

        public PropertyBoolean isPreviewOnly() {
            return previewOnly;
        }

        public String getNewSource() {
            return newSource;
        }
    }
    
    public static final class ResponseBody extends V8Body {
        
        private final ChangeLog changeLog;
        private final Result result;
        private final PropertyBoolean stepInRecommended;
        
        public ResponseBody(ChangeLog changeLog, Result result, Boolean stepInRecommended) {
            this.changeLog = changeLog;
            this.result = result;
            this.stepInRecommended = new PropertyBoolean(stepInRecommended);
        }

        public ChangeLog getChangeLog() {
            return changeLog;
        }

        public Result getResult() {
            return result;
        }

        public PropertyBoolean getStepInRecommended() {
            return stepInRecommended;
        }
        
    }
    
    public static final class Result {
        
        private final ChangeTree changeTree;
        private final TextualDiff diff;
        private final boolean updated;
        private final PropertyBoolean stackModified;
        private final PropertyBoolean stackUpdateNeedsStepIn;
        private final String createdScriptName;
        
        public Result(ChangeTree changeTree, TextualDiff diff, boolean updated,
                      Boolean stackModified, Boolean stackUpdateNeedsStepIn,
                      String createdScriptName) {
            this.changeTree = changeTree;
            this.diff = diff;
            this.updated = updated;
            this.stackModified = new PropertyBoolean(stackModified);
            this.stackUpdateNeedsStepIn = new PropertyBoolean(stackUpdateNeedsStepIn);
            this.createdScriptName = createdScriptName;
        }

        public ChangeTree getChangeTree() {
            return changeTree;
        }

        public TextualDiff getDiff() {
            return diff;
        }

        public boolean isUpdated() {
            return updated;
        }
        
        public PropertyBoolean getStackModified() {
            return stackModified;
        }

        public PropertyBoolean getStackUpdateNeedsStepIn() {
            return stackUpdateNeedsStepIn;
        }

        public String getCreatedScriptName() {
            return createdScriptName;
        }

        public static final class ChangeTree {
            
            private final String name;
            private final Positions positions;
            private final Positions newPositions;
            private final FunctionStatus status;
            private final String statusExplanation;
            private final ChangeTree[] children;
            private final ChangeTree[] newChildren;
            
            public static enum FunctionStatus {
                Unchanged,
                SourceChanged,
                Changed,
                Damaged;
                
                public static FunctionStatus fromString(String statusName) {
                    statusName = Character.toUpperCase(statusName.charAt(0)) + statusName.substring(1);
                    return FunctionStatus.valueOf(statusName);
                }
            }
            
            public ChangeTree(String name,
                              Positions positions, Positions newPositions,
                              FunctionStatus status, String statusExplanation,
                              ChangeTree[] children, ChangeTree[] newChildren) {
                this.name = name;
                this.positions = positions;
                this.newPositions = newPositions;
                this.status = status;
                this.statusExplanation = statusExplanation;
                this.children = children;
                this.newChildren = newChildren;
            }

            public String getName() {
                return name;
            }

            public Positions getPositions() {
                return positions;
            }

            public Positions getNewPositions() {
                return newPositions;
            }

            public FunctionStatus getStatus() {
                return status;
            }

            public String getStatusExplanation() {
                return statusExplanation;
            }

            public ChangeTree[] getChildren() {
                return children;
            }

            public ChangeTree[] getNewChildren() {
                return newChildren;
            }
            
            public static final class Positions {
                
                private final long startPosition;
                private final long endPosition;
                
                public Positions(long startPosition, long endPosition) {
                    this.startPosition = startPosition;
                    this.endPosition = endPosition;
                }

                public long getStartPosition() {
                    return startPosition;
                }

                public long getEndPosition() {
                    return endPosition;
                }
            }
            
        }
        
        public static final class TextualDiff {
            
            private final long oldLength;
            private final long newLength;
            private final long[] chunks;
            
            public TextualDiff(long oldLength, long newLength, long[] chunks) {
                this.oldLength = oldLength;
                this.newLength = newLength;
                this.chunks = chunks;
            }

            public long getOldLength() {
                return oldLength;
            }

            public long getNewLength() {
                return newLength;
            }

            public long[] getChunks() {
                return chunks;
            }
        }
    }
    
    public static final class ChangeLog {
        
        private final long[] breakpointsUpdate;
        private final String[] namesLinkedToOldScript;
        // TODO: function_patched, function_info_not_found
        // TODO: position_patched
        
        public ChangeLog(long[] breakpointsUpdate, String[] namesLinkedToOldScript) {
            this.breakpointsUpdate = breakpointsUpdate;
            this.namesLinkedToOldScript = namesLinkedToOldScript;
        }

        public long[] getBreakpointsUpdate() {
            return breakpointsUpdate;
        }

        public String[] getNamesLinkedToOldScript() {
            return namesLinkedToOldScript;
        }
    }
    
}
