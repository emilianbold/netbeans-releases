/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.lib.editor.util.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Random testing container allows to manage a random test which can be composed
 * from multiple independent resources (maintained as properties) and operations over them.
 *
 * @author mmetelka
 */
public final class RandomTestContainer {

    /** java.lang.Boolean whether operation description should be logged. */
    public static final String LOG_OP = "log-op";

    /** Whether progress in number of operations should be logged. */
    public static final String LOG_PROGRESS = "log-progress";

    private static final int PROGRESS_COUNT = 5;

    // -J-Dorg.netbeans.lib.editor.util.random.RandomTestContainer.level=FINE
    private static final Logger LOG = Logger.getLogger(RandomTestContainer.class.getName());

    private String name;

    private final Map<String,Op> name2Op;

    final List<Check> checks;

    private final List<Round> rounds;

    private Map<Object,Object> properties;

    private Random random;

    int totalOpCount;
    
    private Context context;

    public RandomTestContainer() {
        name2Op = new HashMap<String, Op>();
        checks = new ArrayList<Check>(3);
        rounds = new ArrayList<Round>(3);
        properties = new HashMap<Object,Object>();
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Run the whole testing.
     * <br/>
     * Take all rounds and process them one by one.
     */
    public void run(long seed) throws Exception {
        this.random = new Random();
        if (seed == 0) { // Use currentTimeMillis() (btw nanoTime() in 1.5 instead)
            seed = System.currentTimeMillis();
        }
        random.setSeed(seed);
        System.err.println(name() + " with SEED=" + seed + "L"); // NOI18N

        if (name2Op.size() == 0) {
            throw new IllegalStateException("No operations defined."); // NOI18N
        }
        Context context = new Context(this);
        for (Round round : rounds) {
            round.run(context);
        }
    }

    public Op op(String name) {
        return name2Op.get(name);
    }

    public void addOp(Op op) {
        if (name2Op.containsKey(op.name())) {
            throw new IllegalArgumentException("Operation " + op.name + " already defined.");
        }
        name2Op.put(op.name(), op);
    }

    public void addCheck(Check check) {
        checks.add(check);
    }

    public Random random() {
        return random;
    }

    /**
     * Create a new round.
     * <br/>
     * The caller should set opCount and additional ratios for ops into the round.
     *
     *
     * @return newly created round that is already added into list of rounds of this testing.
     */
    public Round addRound() {
        return addClonedRound(null);
    }

    /**
     * Create a new round to be a copy of existing round.
     * <br/>
     * The caller should set opCount and additional ratios for ops into the round.
     *
     * @return newly created round that is already added into list of rounds of this testing.
     */
    public Round addClonedRound(Round roundToClone) {
        Round round = new Round(this, roundToClone);
        rounds.add(round);
        totalOpCount += round.opCount();
        return round;
    }

    public Object getPropertyOrNull(Object key) {
        return properties.get(key);
    }

    public void putProperty(Object key, Object value) {
        properties.put(key, value);
    }

    /**
     * Get a special non-test context (global context) that has a null {@link Context#round()}.
     * It allows to run certain operations that operate over context for a custom
     * non-randomized operations that the supports may provide.
     *
     * @return non-null context.
     */
    public Context context() {
        if (context == null) {
            context = new Context(this);
        }
        return context;
    }

    public Logger logger() {
        return LOG;
    }

    public static abstract class Op {

        private final String name;

        public Op(String name) {
            this.name = name;
        }

        public final String name() {
            return name;
        }

        /**
         * Execute the operation.
         */
        protected abstract void run(Context context) throws Exception;

    }

    public static abstract class Check {

        protected abstract void check(Context context) throws Exception;

    }

    public static final class Round {

        private final RandomTestContainer container;

        private int opCount;

        final Map<String,Double> op2Ratio;

        private final Map<Object,Object> properties;

        Round(RandomTestContainer container, Round roundToClone) {
            this.container = container;
            if (roundToClone != null)
                this.opCount = roundToClone.opCount;
            this.op2Ratio = new HashMap<String, Double>((roundToClone != null)
                    ? roundToClone.op2Ratio
                    : Collections.<String,Double>emptyMap()
            );
            this.properties = new HashMap<Object, Object>((roundToClone != null)
                    ? roundToClone.properties
                    : Collections.<Object,Object>emptyMap()
            );
        }

        void run(Context context) throws Exception {
            context.round = this;
            double opRatioSum = computeOpRatioSum();
            for (int i = 0; i < opCount; i++) {
                Op op = findOp(context, opRatioSum);
                op.run(context);
                for (Check check : context.container().checks) {
                    check.check(context);
                }
                context.incrementOpCount();
            }
            LOG.info(container.name() + " finished successfully.");
        }

        public int opCount() {
            return opCount;
        }

        public void setOpCount(int opCount) {
            container.totalOpCount += (opCount - this.opCount); // Diff before assignment
            this.opCount = opCount;
        }

        public void setRatio(String opName, double ratio) {
            op2Ratio.put(opName, ratio);
        }

        public Object getPropertyOrNull(Object key) {
            Object value = properties.get(key);
            if (value == null)
                value = container.getPropertyOrNull(key);
            return value;
        }

        public void putProperty(Object key, Object value) {
            properties.put(key, value);
        }

        private double computeOpRatioSum() {
            double ratioSum = 0d;
            for (Double ratio : op2Ratio.values()) {
                ratioSum += ratio;
            }
            return ratioSum;
        }

        private Op findOp(Context context, double opRatioSum) {
            while (true) { // Prevent rounding errors problems
                double r = context.container().random().nextDouble() * opRatioSum;
                for (Map.Entry<String,Double> entry : op2Ratio.entrySet()) {
                    r -= entry.getValue();
                    if (r <= 0) {
                        Op op = context.container().op(entry.getKey());
                        if (op == null) {
                            throw new IllegalStateException("No op for name=" + entry.getKey()); // NOI18N
                        }
                        return op;
                    }
                }
            }
        }
    }

    /**
     * Context of the test being run.
     */
    public static final class Context {

        private final RandomTestContainer container;

        Round round;

        private int opCount;

        Context(RandomTestContainer container) {
            this.container = container;
        }

        public RandomTestContainer container() {
            return container;
        }

        public Round round() {
            return round;
        }

        public int opCount() {
            return opCount;
        }

        public Object getPropertyOrNull(Object key) {
            return (round != null)
                    ? round.getPropertyOrNull(key)
                    : container.getPropertyOrNull(key);
        }

        public Object getProperty(Object key) {
            Object value = getPropertyOrNull(key);
            if (value == null) {
                throw new IllegalStateException("No value for property " + key); // NOI18N
            }
            return value;
        }

        public <V> V getProperty(Object key, V defaultValue) {
            @SuppressWarnings("unchecked")
            V value = (V) getPropertyOrNull(key);
            if (value == null) {
                value = defaultValue;
            }
            return value;
        }

        public <C> C getInstance(Class<C> cls) {
            @SuppressWarnings("unchecked")
            C instance = (C) getPropertyOrNull(cls);
            if (instance == null) {
                throw new IllegalStateException("No value for property " + cls); // NOI18N
            }
            return instance;
        }

        void incrementOpCount() {
            opCount++;
            if (opCount % (container.totalOpCount / PROGRESS_COUNT) == 0) {
                LOG.info(container.name() + ": " + opCount + " operations finished."); // NOI18N
            }
        }

    }

}