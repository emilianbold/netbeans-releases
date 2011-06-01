/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.core.stack.api.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.dlight.core.stack.api.CallStackEntry;
import org.netbeans.modules.dlight.core.stack.api.CallStackEntryParser;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;

/**
 * This is a default parser that can process stacks in the following format
 * 
 * [module[+module_offset]`]function_name[+offset_in_function][:source_file[:source_line]]
 * 
 * @author ak119685
 */
public class DefaultStackParserImpl implements CallStackEntryParser {

    public DefaultStackParserImpl() {
    }

    @Override
    public CallStackEntry parseEntry(final CharSequence entry) {
        int pos1, pos2;
        final EntryWithOffset modulePart;
        final EntryWithOffset functionPart;
        final SourceFileInfo srcFileInfo;

        pos1 = searchRight(entry, '`', 0);

        if (pos1 < 0) {
            modulePart = null;
        } else {
            modulePart = EntryWithOffset.parse(entry.subSequence(0, pos1));
        }

        pos2 = searchRight(entry, ':', pos1 + 1);

        if (pos2 < 0) {
            srcFileInfo = null;
            pos2 = entry.length();
        } else {
            srcFileInfo = parseSourceFileInfo(entry.subSequence(pos2 + 1, entry.length()));
        }

        functionPart = EntryWithOffset.parse(entry.subSequence(pos1 + 1, pos2));

        return new CallStackEntry() {

            @Override
            public CharSequence getModulePath() {
                return modulePart == null ? null : modulePart.entry;
            }

            @Override
            public long getOffsetInModule() {
                return modulePart == null ? -1 : modulePart.offset;
            }

            @Override
            public CharSequence getFunctionName() {
                return functionPart == null ? null : functionPart.entry;
            }

            @Override
            public long getOffsetInFunction() {
                return functionPart == null ? null : functionPart.offset;
            }

            @Override
            public SourceFileInfo getSourceFileInfo() {
                return srcFileInfo;
            }

            @Override
            public CharSequence getOriginalEntry() {
                return entry;
            }
        };
    }

    private static int searchRight(CharSequence entry, char c, int fromIndex) {
        for (int i = fromIndex; i < entry.length(); i++) {
            if (entry.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    private SourceFileInfo parseSourceFileInfo(CharSequence entry) {
        int pos1, pos2;
        int line = -1, column = -1;
        pos1 = searchRight(entry, ':', 0);

        if (pos1 < 0) {
            return new SourceFileInfo(entry, -1, -1);
        }

        pos2 = searchRight(entry, ':', pos1 + 1);

        try {
            if (pos2 < 0) {
                line = Integer.parseInt(entry.subSequence(pos1 + 1, entry.length()).toString());
                column = -1;
            } else {
                line = Integer.parseInt(entry.subSequence(pos1 + 1, pos2).toString());
                column = Integer.parseInt(entry.subSequence(pos2 + 1, entry.length()).toString());
            }
        } catch (NumberFormatException e) {
        }

        return new SourceFileInfo(entry.subSequence(0, pos1), line, column);
    }

    private static class EntryWithOffset {

        private static final Pattern pattern = Pattern.compile("^(.*)\\+([0-9]+)$"); // NOI18N
        private static final Pattern hexPattern = Pattern.compile("^(.*)\\+0x([0-9a-fA-F]+)$"); // NOI18N
        private final CharSequence entry;
        private final long offset;

        public EntryWithOffset(CharSequence entry, long offset) {
            this.entry = entry;
            this.offset = offset;
        }

        @Override
        public String toString() {
            return entry + "+0x" + Long.toHexString(offset); // NOI18N
        }

        private static EntryWithOffset parse(CharSequence part) {

            Matcher m = pattern.matcher(part);
            if (m.matches()) {
                return new EntryWithOffset(m.group(1), Long.parseLong(m.group(2)));
            }

            m = hexPattern.matcher(part);
            if (m.matches()) {
                return new EntryWithOffset(m.group(1), Long.parseLong(m.group(2), 16));
            }

            return new EntryWithOffset(part, -1);
        }
    }
}