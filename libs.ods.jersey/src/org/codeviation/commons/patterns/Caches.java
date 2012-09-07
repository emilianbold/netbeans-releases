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

import java.util.HashMap;
import java.util.LinkedHashSet;

/** Creates caches of various kind.
 *
 * @author Petr Hrebejk
 */
public class Caches {

    public static final Factory<Statistics,Cache> STATISTICS_FACTORY  = new StatsFactory();
    
    /** Holds all entries until explicitely cleared by call into clear() method
     */
    public static <T,P> Cache<T,P> permanent(Factory<T,P> producer) {
        return new PermanentCache<T,P>(producer);
    }
    
    
    // XXX maybe add 
    // public static <T,P> Cache<T,P> synchro( Cache<T,P> original) {}
    
    
    // XXX unfinished
    
//    public static <T,P> Factory<T,P> lru( Factory<T,P> producer, int maxCount ) {
//        return new LruCache<T,P>( producer, maxCount );
//    }
    
    // Public inneclasses ------------------------------------------------------
    
    public static final class Statistics {
        
        public long queries;
        public long misses;

        private static Statistics create(Statistics stats) {
            Statistics s = new Statistics();
            s.queries = stats.queries;
            s.misses = stats.misses;
            return s;
        }
        
    }
        
    // Private innerclasses ----------------------------------------------------
    
    
    private static class StatsFactory implements Factory<Statistics, Cache> {

        public Statistics create(Cache cache) {
            if (!(cache instanceof StatsProvider) ) {
                throw new IllegalArgumentException( "Unknown cache " + cache);
            }
            else {
                return Statistics.create(((StatsProvider)cache).getStats());
            }
        }        
    }
    
    private interface StatsProvider {
        
        Statistics getStats();
        
    }
    
    private static class PermanentCache<T,P> implements Cache<T,P>, StatsProvider {

        private Factory<T,P> producer;
        private HashMap<P,T> cache;
        
        private Statistics stats;
                        
        public PermanentCache(Factory<T, P> producer) {
            this.producer = producer;
            clear();
        }        
        
        public synchronized T create(P param) {
            stats.queries++;
            T r = cache.get(param);
            if ( r == null ) {
                r = producer.create(param);
                cache.put(param, r);
                stats.misses++;
            }
            return r;
        }

        public synchronized void clear() {
            this.stats = new Statistics();
            this.cache = new HashMap<P, T>();
        }

        public Statistics getStats() {
            return stats;
        }
    }
    
    private static class LruCache<T,P> implements Cache<T,P>, StatsProvider {

        private Factory<T,P> producer;
        private int maxCount;

        private HashMap<P,T> cache;
        private LinkedHashSet<P> usages;

        
        private Statistics stats;
        
        public LruCache(Factory<T, P> producer, int maxCount) {
            this.producer = producer;
            this.maxCount = maxCount;
            clear();
        }        
        
        public T create(P param) {
            stats.queries++;
            T r = cache.get(param);
            if ( r == null ) {
                r = producer.create(param);
                cache.put(param, r);
                stats.misses++;
            }            
            //used(param);
            return r;
        }
        
        public void clear() {
            stats = new Statistics();
        }

        public Statistics getStats() {
            return stats;
        }
      
                
//        private void used(P p) {
//            if ( ( usages.size() + 1) > maxCount ) {
//                usages.
//                
//            }
//            if ( usages ) {
//                usages.contains(p);
//            }
//        }
//        
        
        
    }
    
    
}
