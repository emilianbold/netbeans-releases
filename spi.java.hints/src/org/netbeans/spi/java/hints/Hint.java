/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
 */

package org.netbeans.spi.java.hints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.swing.JComponent;

/**Description of a hint. When applied to a class, any enclosed method marked with a trigger
 * will be considered to be part of this hint. When applied to a method, only this specific
 * method will be considered to the part of the hint.
 *
 * Two keys in bundle are required: {@code DN_ <hint-id>}, which will be used the hint's display
 * name, and {@code DESC_<hint-id>}, which will be used as the hint's long description
 * @author lahvac
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface Hint {
    /**Manually specify the hint's id. Use only reorganizing code to keep compatibility with settings
     * from previous version. Id will be generated automatically is not specified.
     */
    public String id() default "";
    /**Category where the hint belongs.
     */
    public String category();
    /**Should the hint be enabled by default?*/
    public boolean enabled() default true;
    /**Default severity of the hint*/
    public HintSeverity severity() default HintSeverity.WARNING;
    /**Suppress warnings keys that should automatically suppress the hint.*/
    public String[] suppressWarnings() default {};
    public Class<? extends JComponent> customizerProvider() default JComponent.class;
    /**Whether the hint should be considered a {@link Kind#HINT hint}, e.g. it detects a code smell,
     * or otherwise leads to improving the code, or a {@link Kind#SUGGESTION}, which is simply
     * an offer to do automatically do something for the user.
     */
    public Kind hintKind() default Kind.HINT;
    /**Specify various options for the hint*/
    public Options[] options() default {};

   public enum Kind {
        HINT,
        SUGGESTION;
    }

    public enum Options {
        NON_GUI,
        QUERY,
        NO_BATCH;
    }
}
