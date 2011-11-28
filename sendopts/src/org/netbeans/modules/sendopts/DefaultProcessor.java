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

package org.netbeans.modules.sendopts;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.netbeans.spi.sendopts.annotations.PostProcess;

/** Processor that is configured from a map, usually from a layer.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class DefaultProcessor extends OptionProcessor {
    private final String clazz;
    private final String field;
    private final Option option;
    private final Type type;

    private DefaultProcessor(
        String type, String f, String c,
        Character shortName, String longName, 
        String displayName, String description
    ) {
        this.field = f;
        this.clazz = c;
        this.type = Type.valueOf(type);
        Option o = null;
        switch (this.type) {
            case withoutArgument: o = Option.withoutArgument(shortName, longName); break;
            case requiredArgument: o = Option.requiredArgument(shortName, longName); break;
            case additionalArguments: o = Option.additionalArguments(shortName, longName); break;
            default: assert false;
        }
        if (displayName != null) {
            String[] arr = displayName.split("#"); // NOI18N
            o = Option.displayName(o, arr[0], arr[1]);
        }
        if (description != null) {
            String[] arr = description.split("#"); // NOI18N
            o = Option.shortDescription(o, arr[0], arr[1]);
        }
        this.option = o;
    }
    
    static DefaultProcessor create(Map<?,?> map) {
        String f = (String) map.get("field");
        String c = (String) map.get("class");
        Character shortName = (Character) map.get("shortName");
        String longName = (String) map.get("longName");
        String type = (String) map.get("type");
        String displayName = (String)map.get("displayName");
        String description = (String)map.get("shortDescription");
        return new DefaultProcessor(type, f, c, shortName, longName, displayName, description);
    }
    

    @Override
    protected Set<Option> getOptions() {
        Set<Option> set = new HashSet<Option>();
        set.add(option);
        return set;
    }

    @Override
    protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
        try {
            Class<?> realClazz = Class.forName(clazz);
            Field realField = realClazz.getDeclaredField(field);
            realField.setAccessible(true);
            switch (type) {
                case withoutArgument:
                    realField.setBoolean(null, true); break;
                case requiredArgument:
                    realField.set(null, optionValues.values().iterator().next()[0]); break;
                case additionalArguments:
                    realField.set(null, optionValues.values().iterator().next()); break;
            }
            for (Method method : realClazz.getDeclaredMethods()) {
                if (method.getAnnotation(PostProcess.class) != null) {
                    method.setAccessible(true);
                    method.invoke(null);
                    break;
                }
            }
            
        } catch (Exception exception) {
            throw (CommandException)new CommandException(10, exception.getLocalizedMessage()).initCause(exception);
        }
    }

    private static enum Type {
        withoutArgument, requiredArgument, additionalArguments;
    }
}
