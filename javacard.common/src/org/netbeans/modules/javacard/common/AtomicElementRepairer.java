/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javacard.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Iterates an list of Strings to produce a new list of Strings.  Whenever the
 * sequence {{{ is encountered, everything following is concatenated until the
 * closing }}}.  Any text preceding the {{{ or following the }}} is
 * separated into a unique element in the new list.
 * <p/>
 * Used for escaping items that need to be atomic inside an Ant-templated
 * command-line provided by a properties file in the Java Card SDK, to handle
 * space-in-path issues and to uniquify arguments which do not follow the pattern
 * <code>-lineswitch arg -lineswitch -lineswitch arg</code> but follow each
 * other with no -.  See Utils.shellSplit().
 *
 * @author Tim Boudreau
 */
final class AtomicElementRepairer {

    private final List<String> l;
    private final List<String> nue;

    AtomicElementRepairer(List<String> l) {
        this.l = l;
        this.nue = new ArrayList<String>(l == null ? 0 : l.size());
    }

    List<String> restoreAtomicItems() {
        StringBuilder sb = null;
        for (String s : l) {
            boolean inQuote = sb != null;
            OneStringHandler h = new OneStringHandler(s, inQuote);
            while (h != null) {
                if ("".equals(h.getString().trim())) {
                    break;
                }
                if (!h.involvesQuoting()) {
                    if (inQuote) {
                        sb.append (s);
                    } else {
                        nue.add (h.getString());
                    }
                    h = null;
                } else {
                    if (h.isOpen()) {
                        if (h.hasLeader()) {
                            nue.add (h.getLeader());
                        }
                        String content = h.getContents();
                        if (h.isClose()) {
                            if (content != null) {
                                nue.add (content);
                            }
                            if (h.hasTrailer()) {
                                h = new OneStringHandler (h.getTrailer(), false);
                            } else {
                                h = null;
                            }
                        } else {
                            sb = new StringBuilder();
                            inQuote = true;
                            if (content != null) {
                                sb.append (content);
                            }
                            h = null;
                        }
                    } else if (h.isClose()) {
                        sb.append(h.getToClose());
                        nue.add (sb.toString());
                        sb = null;
                        inQuote = false;
                        if (h.hasTrailer()) {
                            h = new OneStringHandler(h.getTrailer(), false);
                        } else {
                            h = null;
                        }
                    }
                }
            }
        }
        return nue;
    }

    OneStringHandler createHandler (String s, boolean inQuote) {
        return new OneStringHandler(s, inQuote);
    }

    static final String END_DELIMITER = "}}}";
    static final String START_DELIMITER = "{{{";
    final class OneStringHandler {
        private final String s;
        int startIx;
        int endIx;
        private final boolean inQuote;
        OneStringHandler(String s, boolean inQuote) {
            this.s = s;
            startIx = s.indexOf (START_DELIMITER); //NOI18N
            endIx = s.indexOf (END_DELIMITER); //NOI18N
            this.inQuote = inQuote;
        }

        String getString() {
            return s;
        }

        boolean involvesQuoting() {
            return isOpen() || isClose();
        }

        boolean isOpen() {
            return !inQuote && startIx >= 0;
        }

        boolean isClose() {
            return (inQuote || isOpen()) && endIx >= 0 && endIx > startIx;
        }

        boolean hasLeader() {
            return !inQuote && isOpen() && startIx > 0 && getLeader().trim().length() > 0;
        }

        String getLeader() {
            String result = inQuote ? null : startIx > 0 ? s.substring (0, startIx) : null;
            return result;
        }

        boolean hasTrailer() {
            return isClose() && endIx < s.length() - 3 && getTrailer().trim().length() > 0;
        }

        String getTrailer() {
            String result = endIx >= 0 && endIx < s.length() - 3 ? s.substring (endIx + 3) : null;
            return result;
        }

        String getToClose() {
            String result = s.substring (0, endIx);
            return result;
        }

        boolean hasContents() {
            boolean result = s.length() > 0;
            if (result) {
                boolean isPureContent = (!inQuote || endIx < 0) && startIx < 0;
                boolean openIsNotAtEnd = isOpen() && s.length() > startIx + 3;
                boolean closeIsNotAtStart = isClose() && endIx > 0;
                boolean pureQuote = inQuote && !isClose();
                result = s.length() > 0 && (isPureContent || openIsNotAtEnd || closeIsNotAtStart || pureQuote);
            }
            return result;
        }

        String getContents() {
            if (!hasContents()) {
                return null;
            }
            String result;
            if (isOpen() && !isClose()) {
                result = s.substring (startIx + 3);
            } else if (isOpen() && isClose()) {
                result = s.substring (startIx + 3, endIx);
            } else if (!isOpen() && isClose()) {
                result = s.substring (0, endIx);
            } else {
                return s;
            }
            return result;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append (s);
            sb.append (':'); //NOI18N
            sb.append(" open:").append(isOpen()).append (" "); //NOI18N
            sb.append(" close:").append(isClose()).append (" "); //NOI18N
            sb.append(" hasLeader:").append(hasLeader()).append (" "); //NOI18N
            sb.append(" hasTrailer:").append(hasTrailer()).append (" "); //NOI18N
            sb.append(" hasContent:").append(hasContents()).append (" "); //NOI18N
            sb.append(" involvesQuoting:").append(involvesQuoting()).append (" "); //NOI18N
            sb.append(" inQuote:").append(inQuote).append (" "); //NOI18N
            return sb.toString();
        }
    }


}
