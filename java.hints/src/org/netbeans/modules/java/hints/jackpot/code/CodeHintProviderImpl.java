/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.code;

import com.sun.source.tree.Tree.Kind;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.java.hints.jackpot.code.spi.Constraint;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPatterns;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerTreeKind;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription.PatternDescription;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription.Worker;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescriptionFactory;
import org.netbeans.modules.java.hints.jackpot.spi.HintProvider;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbCollections;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=HintProvider.class)
public class CodeHintProviderImpl implements HintProvider {

    public Collection<? extends HintDescription> computeHints() {
        return computeHints(findLoader(), "META-INF/nb-hints/hints");
    }

    private Collection<? extends HintDescription> computeHints(ClassLoader l, String path) {
        List<HintDescription> result = new LinkedList<HintDescription>();
        
        try {
            Set<String> classes = new HashSet<String>();

            for (URL u : NbCollections.iterable(l.getResources(path))) {
                BufferedReader r = null;

                try {
                    r = new BufferedReader(new InputStreamReader(u.openStream(), "UTF-8"));
                    String line;

                    while ((line = r.readLine()) != null) {
                        classes.add(line);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    if (r != null) {
                        r.close();
                    }
                }
            }

            for (String c : classes) {
                try {
                    Class clazz = l.loadClass(c);
                    
                    processClass(clazz, result);
                } catch (ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result;
    }

    private static ClassLoader findLoader() {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);

        if (l == null) {
            return CodeHintProviderImpl.class.getClassLoader();
        }

        return l;
    }

    public static void processClass(Class<?> clazz, List<HintDescription> result) throws SecurityException {
        for (Method m : clazz.getDeclaredMethods()) {
            Hint hint = m.getAnnotation(Hint.class);
            if (hint != null) {
                processMethod(result, hint, m);
            }
        }
    }

    static void processMethod(List<HintDescription> hints, Hint hint, Method m) {
        //XXX: combinations of TriggerTreeKind and TriggerPattern?
        processTreeKindHint(hints, hint, m);
        processPatternHint(hints, hint, m);
    }
    
    private static void processTreeKindHint(List<HintDescription> hints, Hint hint, Method m) {
        TriggerTreeKind kindTrigger = m.getAnnotation(TriggerTreeKind.class);

        if (kindTrigger == null) {
            return ;
        }

        Worker w = new WorkerImpl(m);

        for (Kind k : new HashSet<Kind>(Arrays.asList(kindTrigger.value()))) {
            hints.add(HintDescriptionFactory.create()
                                            .setCategory(hint.category())
                                            .setTriggerKind(k)
                                            .setWorker(w)
                                            .produce());
        }
    }
    
    private static void processPatternHint(List<HintDescription> hints, Hint hint, Method m) {
        TriggerPattern patternTrigger = m.getAnnotation(TriggerPattern.class);

        if (patternTrigger != null) {
            processPatternHint(hints, patternTrigger, hint, m);
            return ;
        }

        TriggerPatterns patternTriggers = m.getAnnotation(TriggerPatterns.class);

        if (patternTriggers != null) {
            for (TriggerPattern pattern : patternTriggers.value()) {
                processPatternHint(hints, pattern, hint, m);
            }
            return ;
        }
    }

    private static void processPatternHint(List<HintDescription> hints, TriggerPattern patternTrigger, Hint hint, Method m) {
        String pattern = patternTrigger.value();
        Map<String, String> constraints = new HashMap<String, String>();

        for (Constraint c : patternTrigger.constraints()) {
            constraints.put(c.variable(), c.type());
        }

        PatternDescription pd = PatternDescription.create(pattern, constraints);

        hints.add(HintDescriptionFactory.create()
                                        .setCategory(hint.category())
                                        .setTriggerPattern(pd)
                                        .setWorker(new WorkerImpl(m))
                                        .produce());
    }

    //accessed by tests:
    static final class WorkerImpl implements Worker {

        private final Method method;

        public WorkerImpl(Method m) {
            this.method = m;
        }
        
        public Collection<? extends ErrorDescription> createErrors(org.netbeans.modules.java.hints.jackpot.spi.HintContext ctx) {
            try {
                Object result = method.invoke(null, ctx);

                if (result == null) {
                    return null;
                }

                if (result instanceof Iterable) {
                    List<ErrorDescription> out = new LinkedList<ErrorDescription>();

                    for (ErrorDescription ed : NbCollections.iterable(NbCollections.checkedIteratorByFilter(((Iterable) result).iterator(), ErrorDescription.class, false))) {
                        out.add(ed);
                    }

                    return out;
                }

                if (result instanceof ErrorDescription) {
                    return Collections.singletonList((ErrorDescription) result);
                }

                //XXX: log if result was ignored...
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }

            return null;
        }

        //used by tests:
        Method getMethod() {
            return method;
        }

    }

}
