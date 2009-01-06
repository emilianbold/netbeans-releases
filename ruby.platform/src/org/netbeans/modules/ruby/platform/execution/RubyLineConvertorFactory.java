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
package org.netbeans.modules.ruby.platform.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;

/**
 *
 * @author Erno Mononen
 */
public final class RubyLineConvertorFactory implements LineConvertorFactory {

    private static final String WINDOWS_DRIVE = "(?:\\S{1}:[\\\\/])"; // NOI18N
    private static final String FILE_CHAR = "[^\\s\\[\\]\\:\\\"]"; // NOI18N
    private static final String FILE = "((?:" + FILE_CHAR + "*))"; // NOI18N
    private static final String FILE_WIN = "(" + WINDOWS_DRIVE + "(?:" + FILE_CHAR + ".*))"; // NOI18N
    private static final String LINE = "([1-9][0-9]*)"; // NOI18N
    private static final String ROL = ".*\\s?"; // NOI18N
    private static final String SEP = "\\:"; // NOI18N
    private static final String STD_SUFFIX = FILE + SEP + LINE + ROL;
    static final Pattern RUBY_COMPILER = Pattern.compile(".*?" + STD_SUFFIX); // NOI18N
    static final Pattern RUBY_COMPILER_WIN_MY = Pattern.compile(".*?" + FILE_WIN + SEP + LINE + ROL); // NOI18N

    /* Keeping old one. Get rid of this with more specific recongizers? */
    static final Pattern RUBY_COMPILER_WIN =
            Pattern.compile("^(?:(?:\\[|\\]|\\-|\\:|[0-9]|\\s|\\,)*)(?:\\s*from )?" + FILE_WIN + SEP + LINE + ROL); // NOI18N
    public static final Pattern RAILS_RECOGNIZER =
            Pattern.compile(".*#\\{RAILS_ROOT\\}/" + STD_SUFFIX); // NOI18N
    public static final Pattern RUBY_TEST_OUTPUT = Pattern.compile("\\s*test.*\\[" + STD_SUFFIX); // NOI18N
    /** Regexp. for extensions. */
    public static final Pattern EXT_RE = Pattern.compile(".*\\.(rb|rake|mab|rjs|rxml|builder|erb)"); // NOI18N

    private final FileLocator locator;
    private final LineConvertor[] convertors;
    private final boolean stdConvertors;


    /**
     * Creates a new convertor factory.
     * 
     * @param locator the locator to use.
     * @param convertors the convertors to use (if more than one is passed, they will
     *  be chained in the given order, i.e. the first given convertor will get to handle
     *  lines first).
     * @return
     */
    public static RubyLineConvertorFactory create(FileLocator locator, LineConvertor... convertors) {
        return new RubyLineConvertorFactory(locator, false, convertors);
    }

    /**
     * Creates a new convertor factory with the standard Ruby line convertors. The
     * standard convertors will be chained after the given (if any) convertors.
     *
     * @param locator the locator to use.
     * @param convertors the convertors to use (if more than one is passed, they will
     *  be chained in the given order, i.e. the first given convertor will get to handle
     *  lines first).
     * @return
     */
    public static RubyLineConvertorFactory withStandardConvertors(FileLocator locator, LineConvertor... convertors) {
        return new RubyLineConvertorFactory(locator, true, convertors);
    }

    private RubyLineConvertorFactory(FileLocator locator, boolean stdConvertors, LineConvertor... convertors) {
        this.locator = locator;
        this.convertors = convertors;
        this.stdConvertors = stdConvertors;
    }

    /**
     * Gets the standard convertors.
     *
     * @param locator the locator for the convertors to use.
     * @return
     */
    public static List<LineConvertor> getStandardConvertors(FileLocator locator) {
        List<LineConvertor> result = new ArrayList<LineConvertor>(4);
        result.add(LineConvertors.filePattern(locator, RAILS_RECOGNIZER, null, 1, 2));
        result.add(LineConvertors.filePattern(locator, RUBY_COMPILER_WIN_MY, null, 1, 2));
        result.add(LineConvertors.filePattern(locator, RUBY_COMPILER, null, 1, 2));
        result.add(LineConvertors.filePattern(locator, RUBY_COMPILER_WIN, null, 1, 2));
        return result;
    }

    public LineConvertor newLineConvertor() {
        final List<LineConvertor> convertorList = new ArrayList<LineConvertor>();

        if (convertors != null) {
            for (LineConvertor each : convertors) {
                if (each != null) {
                    convertorList.add(each);
                }
            }
        }

        if (stdConvertors) {
            convertorList.addAll(getStandardConvertors(locator));
        }
        return LineConvertors.proxy(convertorList.toArray(new LineConvertor[convertorList.size()]));
    }
}
