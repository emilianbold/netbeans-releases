/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.completion;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.spi.completion.CompletionContext;
import org.netbeans.modules.groovy.editor.spi.completion.DynamicCompletionProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.groovy.editor.spi.completion.DynamicCompletionProvider.class)
public class DomainCompletionProvider extends DynamicCompletionProvider {

    private static final Map<MethodSignature, String> BASIC_METHODS = new HashMap<MethodSignature, String>();

    private static final String FIND_BY_METHOD = "findBy"; // NOI18N

    private static final String FIND_ALL_BY_METHOD = "findAllBy"; // NOI18N

    private static final String COUNT_BY_METHOD = "countBy"; // NOI18N

    private static final Set<String> QUERY_OPERATOR = new HashSet<String>();

    private static final Set<String> QUERY_COMPARATOR = new HashSet<String>();

    // FIXME move it to some resource file, check the grails version - this is for 1.0.4
    static {
        Collections.addAll(QUERY_OPERATOR, "And", "Or");

        Collections.addAll(QUERY_COMPARATOR, "LessThan", "LessThanEquals",
                "GreaterThan", "GreaterThanEquals", "Like", "ILike",
                "Equal", "NotEqual", "Between", "IsNotNull", "IsNull");

        String[] noParams = new String[] {};

        BASIC_METHODS.put(new MethodSignature("save", new String[] {"java.lang.Boolean"}), "java.lang.Object"); // NOI18N
        BASIC_METHODS.put(new MethodSignature("save", new String[] {"java.util.Map"}), "java.lang.Object"); // NOI18N
        BASIC_METHODS.put(new MethodSignature("save", noParams), "java.lang.Object"); // NOI18N

        BASIC_METHODS.put(new MethodSignature("merge", new String[] {"java.lang.Object"}), "java.lang.Object"); // NOI18N
        BASIC_METHODS.put(new MethodSignature("merge", noParams), "java.lang.Object"); // NOI18N

        BASIC_METHODS.put(new MethodSignature("delete", noParams), "java.lang.Object"); // NOI18N
        BASIC_METHODS.put(new MethodSignature("delete", new String[] {"java.util.Map"}), "java.lang.Object"); // NOI18N

        BASIC_METHODS.put(new MethodSignature("refresh", noParams), "java.lang.Object"); // NOI18N

        BASIC_METHODS.put(new MethodSignature("discard", noParams), "java.lang.Object"); // NOI18N

        BASIC_METHODS.put(new MethodSignature("attach", noParams), "java.lang.Object"); // NOI18N
        BASIC_METHODS.put(new MethodSignature("isAttached", noParams), "java.lang.Boolean"); // NOI18N

        BASIC_METHODS.put(new MethodSignature("getErrors", noParams), "org.springframework.validation.Errors"); // NOI18N
        BASIC_METHODS.put(new MethodSignature("setErrors", new String[] {"org.springframework.validation.Errors"}), "org.springframework.validation.Errors"); // NOI18N
        BASIC_METHODS.put(new MethodSignature("clearErrors", noParams), "org.springframework.validation.Errors"); // NOI18N
        BASIC_METHODS.put(new MethodSignature("hasErrors", noParams), "java.lang.Boolean"); // NOI18N

        BASIC_METHODS.put(new MethodSignature("validate", noParams), "java.lang.Boolean"); // NOI18N

        BASIC_METHODS.put(new MethodSignature("delete", noParams), "java.lang.Object"); // NOI18N
        BASIC_METHODS.put(new MethodSignature("delete", new String[] {"java.util.Map"}), "java.lang.Object"); // NOI18N

        BASIC_METHODS.put(new MethodSignature("lock", noParams), "java.lang.Object"); // NOI18N
    }

    @Override
    public Map<FieldSignature, CompletionItem> getFields(CompletionContext context) {
        return Collections.emptyMap();
    }

    @Override
    public Map<MethodSignature, CompletionItem> getMethods(CompletionContext context) {
        if (context.getSourceFile() == null) {
            return Collections.emptyMap();
        }

        Project project = FileOwnerQuery.getOwner(context.getSourceFile());
        if (context.isLeaf() && project.getLookup().lookup(ControllerCompletionProvider.class) != null) {

            if (isDomain(context.getSourceFile(), project)) {
                Map<MethodSignature, CompletionItem> result = new HashMap<MethodSignature, CompletionItem>();
                result.putAll(getQueryMethods(context));
                for (Map.Entry<MethodSignature, String> entry : BASIC_METHODS.entrySet()) {
                    result.put(entry.getKey(), CompletionItem.forDynamicMethod(
                            context.getAnchor(), entry.getKey().getName(), entry.getKey().getParameters(), entry.getValue(), context.isNameOnly(), false));
                }
                return result;
            }
        }
        return Collections.emptyMap();
    }

    private Map<MethodSignature, CompletionItem> getQueryMethods(CompletionContext context) {
        Map<MethodSignature, CompletionItem> result = new HashMap<MethodSignature, CompletionItem>();

        Matcher matcher = getQueryMethodPattern(context).matcher(context.getPrefix());

        if (matcher.matches()) {
            String prefix = matcher.group(13);
            String name = context.getPrefix().substring(0, context.getPrefix().length() - prefix.length());

            if (prefix == null) {
                prefix = "";
            }

            Map<String, Integer> names = new HashMap<String, Integer>();
            Set<String> forbidden = new HashSet<String>();
            int paramCount = getUsedComparators(context, forbidden);

            // comparator
            if (matcher.group(10) != null) {
                // operator + property
                names.putAll(getSuffixForOperator(name, context, prefix, paramCount));
            // property
            } else if (matcher.group(9) != null) {
                // comparator or (operator + property)
                names.putAll(getSuffixForComparator(name, context, prefix, matcher.group(9), forbidden, paramCount));
                names.putAll(getSuffixForOperator(name, context, prefix, paramCount));
            // operator
            } else if (matcher.group(7) != null) {
                // property
                names.putAll(getSuffixForProperty(name, context, prefix, paramCount));
            // comparator
            } else if (matcher.group(4) != null) {
                // operator + property
                names.putAll(getSuffixForOperator(name, context, prefix, paramCount));
            // property
            } else if (matcher.group(3) != null) {
                // comparator or (operator + property)
                names.putAll(getSuffixForComparator(name, context, prefix, matcher.group(3), forbidden, paramCount));
                names.putAll(getSuffixForOperator(name, context, prefix, paramCount));
            }

            for (Map.Entry<String, Integer> entry : names.entrySet()) {

                addQueryEntries(result, context, matcher.group(1),
                        entry.getKey().substring(matcher.group(1).length()), entry.getValue().intValue());
            }
        } else {
            for (String property : context.getProperties()) {
                String tail = capitalise(property);

                addQueryEntries(result, context, FIND_ALL_BY_METHOD, tail, 1);
                addQueryEntries(result, context, FIND_BY_METHOD, tail, 1);
                addQueryEntries(result, context, COUNT_BY_METHOD, tail, 1);
            }
        }
        return result;
    }

    private Map<String, Integer> getSuffixForOperator(String prefix, CompletionContext context, String tail, int paramCount) {
        Map<String, Integer> result = new HashMap<String, Integer>();
        for (String property : context.getProperties()) {
            for (String operator : QUERY_OPERATOR) {
                String suffix = operator + capitalise(property);
                if (suffix.startsWith(tail)) {
                    result.put(prefix + suffix, paramCount + 1);
                }
            }
        }
        return result;
    }

    private Map<String, Integer> getSuffixForComparator(String prefix, CompletionContext context, String tail,
            String property, Set<String> forbidden, int paramCount) {

        Map<String, Integer> result = new HashMap<String, Integer>();

        for (String operator : QUERY_COMPARATOR) {
            int realCount = paramCount;
            String suffix = operator;
            if (suffix.startsWith(tail) && !forbidden.contains(property + suffix)) {
                if ("Between".equals(operator)) {
                    realCount++;
                } else if ("IsNotNull".equals(operator) || "IsNull".equals(operator)) {
                    realCount--;
                }
                result.put(prefix + suffix, realCount);
            }
        }
        return result;
    }

    private Map<String, Integer> getSuffixForProperty(String prefix, CompletionContext context, String tail, int paramCount) {
        Map<String, Integer> result = new HashMap<String, Integer>();
        for (String property : context.getProperties()) {
            String suffix = capitalise(property);
            if (suffix.startsWith(tail)) {
                result.put(prefix + suffix, paramCount + 1);
            }
        }
        return result;
    }

    private Pattern getQueryMethodPattern(CompletionContext context) {
        StringBuilder builder = new StringBuilder("(findBy|findAllBy|countBy)");
        builder.append("(");

        StringBuilder propertyBuilder = new StringBuilder();
        propertyBuilder.append("(");
        for (String property : context.getProperties()) {
            propertyBuilder.append(Pattern.quote(capitalise(property)));
            propertyBuilder.append('|');
        }
        propertyBuilder.setLength(propertyBuilder.length() - 1);
        propertyBuilder.append(")");

        builder.append(propertyBuilder);
        builder.append("(LessThan(Equals)?|GreaterThan(Equals)?|Like|ILike|Equal|NotEqual|Between|IsNotNull|IsNull)?");
        builder.append("(And|Or)");
        builder.append(")*");

        builder.append("(");
        builder.append(propertyBuilder);
        builder.append("(LessThan(Equals)?|GreaterThan(Equals)?|Like|ILike|Equal|NotEqual|Between|IsNotNull|IsNull)?");
        builder.append(")?");

        builder.append("(.*)");
        return Pattern.compile(builder.toString());
    }

    private int getUsedComparators(CompletionContext context, Set<String> result) {
        Matcher matcher = Pattern.compile("(findBy|findAllBy|countBy)(.*)").matcher(context.getPrefix());
        if (!matcher.matches()) {
            return 0;
        }

        String[] parts = matcher.group(2).split("(And|Or)");

        int paramCount = 0;
        Pattern pattern = Pattern.compile("(.*)(LessThan(Equals)?|GreaterThan(Equals)?|Like|ILike|Equal|NotEqual|Between|IsNotNull|IsNull)?");
        for (String part : parts) {
            //result.add(part);

            Matcher singleMatcher = pattern.matcher(part);
            if (singleMatcher.matches()) {
                String comparator = singleMatcher.group(2);
                if ("Between".equals(comparator)) {
                    paramCount += 2;
                } else if (!"IsNotNull".equals(comparator) && !"IsNull".equals(comparator)) {
                    paramCount += 1;
                } else if (comparator == null) {
                    paramCount += 1;
                    //result.add(part + "Equal");
                }
            }
        }

        return paramCount;
    }

    private void addQueryEntries(Map<MethodSignature, CompletionItem> result,
            CompletionContext context, String prefix, String tail, int params) {

        String returnType = "java.lang.Object";
        if (FIND_ALL_BY_METHOD.equals(prefix)) {
            returnType = "java.util.List";
        } else if (COUNT_BY_METHOD.equals(prefix)) {
            returnType = "int";
        }
        String name = prefix + tail;

        String[] shortParams = new String[params];
        Arrays.fill(shortParams, "java.lang.Object");
        result.put(new MethodSignature(name, shortParams),
                CompletionItem.forDynamicMethod(context.getAnchor(), name, shortParams,
                        returnType, context.isNameOnly(), false));

        String[] longParams = new String[params + 1];
        Arrays.fill(longParams, "java.lang.Object");
        longParams[params] = "java.util.Map";
        result.put(new MethodSignature(name, longParams),
                CompletionItem.forDynamicMethod(context.getAnchor(), name, longParams,
                        returnType, context.isNameOnly(), false));

        result.put(new MethodSignature(name + "_", new String[] {}),
                CompletionItem.forDynamicMethod(context.getAnchor(), name, new String[] {}, returnType, true, true));
    }

    private boolean isDomain(FileObject source, Project project) {
        return source != null
                    && source.getParent().getName().equals("domain") // NOI18N
                    && source.getParent().getParent().getName().equals("grails-app") // NOI18N
                    && source.getParent().getParent().getParent().equals(project.getProjectDirectory());
    }

    private String capitalise(String property) {
        StringBuilder builder = new StringBuilder();
        String[] parts = property.split("[^\\w\\d]");
        for (String part : parts) {
            builder.append(part.substring(0, 1).toUpperCase(Locale.ENGLISH)).append(part.substring(1));
        }

        return builder.toString();
    }
}
