/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.api.templates;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.script.ScriptEngineFactory;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.loaders.TemplateWizard;

/**
 * Registers a template the user can select.
 * May be placed on a class (with a default constructor) or static method (with no arguments)
 * to register an {@link InstantiatingIterator} for a custom template;
 * or on a package to register a plain-file template with no custom behavior.
 * @since 7.29
 * @see TemplateWizard
 * @see TemplateRegistrations
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE})
@Retention(RetentionPolicy.SOURCE)
public @interface TemplateRegistration {
    
    /**
     * Subfolder in which to place the template, such as {@code Other} or {@code Project/Standard}.
     */
    String folder();
    
    /**
     * Optional position within {@link #folder}.
     */
    int position() default Integer.MAX_VALUE;

    /**
     * Special file basename to use rather than inferring one from the declaring element,
     * when {@link #content} is empty.
     * Useful for pure templates referenced from {@code PrivilegedTemplates}.
     */
    String id() default "";

    /**
     * File contents, as resources relative to the package of this declaration.
     * A nonempty list is mandatory for a template registered on a package.
     * For a template with a custom iterator, the content may be omitted, though it may still be specified.
     * <p>Normally only a single file is specified, but for a multifile data object, list the primary entry first.
     * <p>The file basenames (incl. extension) of the actual template files (as in {@link TemplateWizard#getTemplate})
     * will be taken from the basename of the content resources, though a {@code .template} suffix
     * may be appended to prevent template resources in a source project from being misinterpreted.
     * For a "pure" custom iterator with no specified content, the template basename
     * defaults to the FQN of the class or method defining it but with {@code -} for {@code .} characters,
     * e.g. {@code pkg-Class-method}, but may be overridden with {@link #id}.
     * <p>Example usage for a simple, single-file template (with or without custom iterator):
     * <pre>content="resources/empty.php"</pre>
     * <p>For a form template:
     * <pre>content={"Login.java.template", "Login.form.template"}</pre>
     */
    String[] content() default {};

    /**
     * Localized label for the template.
     * Mandatory unless {@link #content} is specified, in which case it would be defaulted by the data node.
     * May use the usual {@code #key} syntax.
     */
    String displayName() default "";

    /**
     * Icon to use for the template.
     * Should be an absolute resource path (no initial slash).
     * Mandatory unless {@link #content} is specified, in which case it would be defaulted by the data node.
     */
    String iconBase() default "";

    /**
     * Optional but recommended relative resource path to an HTML description of the template.
     * @see TemplateWizard#getDescription
     */
    String description() default "";

    /**
     * Optional name of a script engine to use when processing file content, such as {@code freemarker}.
     * @see ScriptEngineFactory#getNames
     */
    String scriptEngine() default "";

    /**
     * Optional list of categories interpreted by the project system.
     */
    String[] category() default {};

}
