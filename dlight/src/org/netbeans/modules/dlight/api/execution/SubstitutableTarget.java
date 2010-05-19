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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.api.execution;

/**
 * There are collectors that cannot be attachable to
 * target, for example to collect synchronization data using SunStudio
 * Performance Analyzer you should run your application as following:
 * <code>collect -s &lt;application&gt; &lt;args&gt; </code>.
 * If your target can be substituted by the collector please implement this interface
 * when creating your target.
 */
public interface SubstitutableTarget {

  /**
   * Returns <code>true</code> if this target can be substituted.
   * There are collectors that cannot be attachable to
   * target (method {@link org.netbeans.modules.dlight.spi.collector.DataCollector#isAttachable() }
   * returns false), for example to collect synchronization data using SunStudio
   * Performance Analyzer you should run your application as following:
   * <code>collect -s &lt;application&gt; &lt;args&gt; </code>.
   * If your target can be substituted by the collector this method should return <code>true</code>,
   * but it is obvious there are the cases it should return false as in case target is
   * the whole system.
   * @return <code>true</code> target can be substituted
   */
  public boolean canBeSubstituted();

  /**
   * Sunstitutes target with the new command line and args,
   * original command line and args are added at the end of common "substituted" command line
   * @param cmd command line to substitute original command line with
   * @param args args to run cmd
   */
  public void substitute(String cmd, String[] args);
}
