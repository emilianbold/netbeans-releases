/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.codeviation.commons.patterns;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

/** Creates combination of filters
 *
 * @author Petr Hrebejk
 */
public class Filters {

    private static Filter<?> ALLWAYS_TRUE = new LogicalFilter<Object>(LogicalFilter.TRUE);
    private static Filter<?> ALLWAYS_FALSE = new LogicalFilter<Object>(LogicalFilter.FALSE);
    
    private Filters() {
    }
    
    public static <T> Filter<T> And( Filter<T>... filters ) {
        if ( filters.length < 2 ) {
            throw new IllegalArgumentException("At least two filters needed!");
        }
        return new LogicalFilter<T>( LogicalFilter.AND, filters );
    }
    
    public static <T> Filter<T> Or( Filter<T>... filters ) {
        if ( filters.length < 2 ) {
            throw new IllegalArgumentException("At least two filters needed!");
        }
        return new LogicalFilter<T>( LogicalFilter.OR, filters );
    }
    
    public static <T> Filter<T> Not( Filter<T> filter ) {
        if ( filter == null ) {
            throw new IllegalArgumentException("Filter must not be null!");
        }
        return new LogicalFilter<T>( LogicalFilter.NOT, filter );
    }
    
    public static <T> Filter<T> True() {        
        return (Filter<T>)ALLWAYS_TRUE;
    }
    
    public static <T> Filter<T> False() {        
        return (Filter<T>)ALLWAYS_FALSE;
    }
          
    public static <T> Filter<T> NotNull() {
        return new Filters.NotNullFilter<T>();
    }
    
    public static <T> Filter<T> IsIn(Collection<? extends T> container) {
        return new Filters.IsIn<T>(container);
    }
    
    public static <T> Filter<T> IsIn(T... container) {
        return new Filters.IsIn<T>(Arrays.asList(container));
    }
    
    public static Filter<String> Regexp(String regexp) {
        return new Regexp(regexp);
    }
    
    public static Filter<Object> InstanceOf(Class clazz) {
        return new InstanceOf(clazz);
    }
    
    /** Creates a filter equivalent to include AND !exclude
     * 
     * 
     * @param include
     * @param exclude
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Filter<T> IncludeExclude(Filter<T> include, Filter<T> exclude) {
        return new Filters.LogicalFilter<T>( LogicalFilter.INCLUDE_EXCLUDE, new Filter[] { include, exclude } );
    }
    
    // Private members ---------------------------------------------------------
    
    private static class LogicalFilter<T> implements Filter<T> {
        
        private static final int AND = 0;
        private static final int OR = 1;
        private static final int NOT = 2;
        private static final int INCLUDE_EXCLUDE = 3;
        private static final int TRUE = 4;
        private static final int FALSE = 5;
        
            
        private int op;
        private Filter<T> filters[];
        private Filter<T> filter;
                
        private LogicalFilter( int op ) {
            this.op = op;
        }
        
        private LogicalFilter( int op, Filter<T> filter ) {
            this.op = op;
            this.filter = filter;
        }
        
        private LogicalFilter( int op, Filter<T> filters[] ) {
            this.op = op;
            this.filters = filters;
        }

        public boolean accept(T object) {
            switch(op) {
                case AND:
                    for (Filter<T> f : filters) {
                        if( f != null && !f.accept(object) ) {
                            return false;
                        }
                    }
                    return true;
                case OR:
                    for (Filter<T> f : filters) {
                        if( f != null && f.accept(object) ) {
                            return true;
                        }
                    }
                    return false;
                case NOT:
                    return !filter.accept(object);
                case INCLUDE_EXCLUDE:
                    return filters[0].accept(object) && !filters[1].accept(object);
                case TRUE:
                    return true;
                case FALSE:
                    return false;
                default:
                    throw new IllegalStateException("Unknown filter operation!");
                }
         }
    }
    
    private static class NotNullFilter<T> implements Filter<T> {

        public boolean accept(T object) {
            return object != null;
        }
        
    }
    
    private static class IsIn<T> implements Filter<T> {

        private Collection container;

        public IsIn(Collection container) {
            this.container = container;
        }
        
        public boolean accept(T object) {
            return container.contains(object);
        }
        
    }
    
    private static class Regexp implements Filter<String> {

        
        private Pattern pattern;

        public Regexp(String regex) {
            pattern = Pattern.compile(regex);
        }
        
        public boolean accept(String text) {
            return pattern.matcher(text).matches();
        }
        
    }
    
    private static class InstanceOf implements Filter<Object> {

        private Class clazz;

        public InstanceOf(Class clazz) {
            this.clazz = clazz;
        }
        
        public boolean accept(Object object) {
            return clazz.isInstance(object);
        }
        
    }
    
}
