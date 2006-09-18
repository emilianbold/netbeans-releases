/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.sendopts;

import java.util.Map;
import java.util.Set;
import org.netbeans.api.sendopts.CommandException;

/** A subclass of this class shall be registered in 
 * <code>META-INF/services/org.netbeans.spi.sendopts.OptionProcessor</code>
 * file (see <a href="@org-openide-util@/org/openide/util/Lookup.html">Lookup</a> 
 * for description of how to do it and why) 
 * in order to register it for participation on handling
 * and processing of command line options initiated by
 * {@link org.netbeans.api.sendopts.CommandLine#getDefault}'s
 * {@link org.netbeans.api.sendopts.CommandLine#process}.
 * When the {@link Option}s provided by this processor are found
 * on the command line and are consistent, this processor's {@link #process}
 * method is going to be called to handle their values and invoke an action.
 * <p>
 * The usual pattern for writing a subclass of processor is:
 * <pre>
 * public class MyProcessor extends OptionProcessor {
 *   private Option option1 = ...;
 *   private Option option2 = ...;
 *   private Option option3 = ...;
 * 
 *   protected Set<Option> getOptions() {
 *      Set<Option> set = new HashSet<Option>();
 *      set.add(option1);
 *      set.add(option2);
 *      set.add(option3);
 *      return set;
 *   }
 * 
 *   protected void process(<a href="Env.html">Env</a> env, Map&lt;<a href="Option.html">Option</a>,String[]&gt; values) 
 *   throws {@link CommandException} {
 *     if (values.containKey(option1) { ... }
 *     if (values.containKey(option2) { ... }
 *     if (values.containKey(option3) { ... }
 *   }
 * }
 * </pre>
 * 
 * @author Jaroslav Tulach
 */
public abstract class OptionProcessor {
    /** Constructor for subclasses.
     */
    protected OptionProcessor() {
    }
    
    /** Method to override in subclasses to create 
     * the right set of {@link Option}s.
     * See the factory methods that are part of the {@link Option}'s javadoc
     * or read the <a href="@TOP@/architecture-summary.html#answer-usecases">
     * usecases</a> for the sendopts API.
     * <p>
     * 
     * @return a set of options this processor is interested in, if during
     *   processing at least on of the options appears on command line
     *   the {@link OptionProcessor#process} method will be invoked to
     *   handle such option and its values
     */
    protected abstract Set<Option> getOptions();
    
    
    /** Called by the sendopts parsing infrastructure as a result of
     * {@link org.netbeans.api.sendopts.CommandLine#process}. The method shall read the values
     * associated with the option(s) this {@link OptionProcessor} defines
     * and invoke an action to handle them. While doing this it can 
     * communicate with external world using its environment (see {@link Env}).
     * Such environment provides access to current user directory, standard
     * output and error streams, as well standard input stream. In case
     * the processing of options fails, the code shall thrown {@link CommandException}.
     * 
     * @param env the environment to communicate with
     * @param optionValues map of all options that appeared on command line with their values
     * @exception CommandException in case the processing fails1
     */
    protected abstract void process(Env env, Map<Option,String[]> optionValues)
    throws CommandException;
}
