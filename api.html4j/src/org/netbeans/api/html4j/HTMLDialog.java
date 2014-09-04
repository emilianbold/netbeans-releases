/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * License.  When distributing the software, include this License Header/*

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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.api.html4j;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;
import org.netbeans.modules.html4j.HTMLDialogImpl;

/** Generates method that opens an HTML based modal dialog.
 * The method is generated into <code>Pages</code> class in the same package
 * (unless one changes the name via {@link #className()}) and has the same name,
 * and parameters as the method annotated by this annotation. When the method
 * is invoked, it opens a dialog, loads an HTML page into it. When the page is 
 * loaded, it calls back the method annotated by this annotation and passes it
 * its own arguments. The method is supposed to make the page live, preferrably 
 * by using {@link net.java.html.json.Model} generated class and calling 
 * <code>applyBindings()</code> on it.
 * <p>
 * The HTML page may contain invisible <code>&lt;button&gt;</code> elements. If it does so, 
 * those buttons are copied to the dialog frame and displayed underneath the page.
 * Their enabled/disabled state reflects the state of the buttons in the page.
 * When one of the buttons is selected, the dialog closes and the generated
 * method returns with 'id' of the selected button (or <code>null</code> if
 * the dialog was closed).
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface HTMLDialog {
    /** URL of the page to display. Usually relative to the annotated class.
     * Will be resolved by the annotation processor and converted into
     * <code>nbresloc</code> protocol - as such the HTML page can be L10Ned
     * later by adding classical L10N suffixes. E.g. <code>index_cs.html</code>
     * will take preceedence over <code>index.html</code> if the user is 
     * running in Czech {@link Locale}.
     * 
     * @return relative path the HTML page
     */
    String url();
    
    /** Name of the file to generate the method that opens the dialog
     * into. Class of such name will be generated into the same
     * package. 
     * 
     * @return name of class to generate
     */
    String className() default "Pages";
    
    /** Rather than using this class directly, consider 
     * {@link HTMLDialog}. The {@link HTMLDialog} annotation 
     * generates boilderplate code for you
     * and can do some compile times checks helping you to warnings
     * as soon as possible.
     */
    public static final class Builder {
        private final HTMLDialogImpl impl;
        
        private Builder(String u) {
            impl = new HTMLDialogImpl();
            impl.setUrl(u);
        }

        /** Starts creation of a new HTML dialog. The page
         * can contain hidden buttons as described at
         * {@link HTMLDialog}.
         * 
         * @param url URL (usually using <code>nbresloc</code> protocol)
         *   of the page to display in the dialog.
         * @return instance of the builder
         */
        public static Builder newDialog(String url) {
            return new Builder(url);
        }
        
        /** Registers a runnable to be executed when the page
         * becomes ready.
         * 
         * @param run runnable to run
         * @return this builder
         */
        public Builder loadFinished(Runnable run) {
            impl.setOnPageLoad(run);
            return this;
        }

        /** Displays the dialog. This method blocks waiting for the
         * dialog to be shown and closed by the user. 
         * 
         * @return 'id' of a selected button element or <code>null</code>
         *   if the dialog was closed without selecting a button
         */
        public String showAndWait() {
            return impl.showAndWait();
        }
    }
}
