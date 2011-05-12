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

package org.netbeans.modules.apisupport.hints;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.CheckReturnValue;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.apisupport.project.api.LayerHandle;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import static org.netbeans.modules.apisupport.hints.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 * One category of hint.
 * Register implementation into global lookup.
 */
public interface Hinter {

    /**
     * Check for hints.
     * @param ctx context of a single layer entry
     * @throws Exception in case of problem
     */
    void process(Context ctx) throws Exception;

    /**
     * Context supplied to a {@link Hinter}.
     */
    class Context {

        private static final Logger LOG = Logger.getLogger(Hinter.class.getName());

        private final Document doc;
        private final LayerHandle layer;
        private final FileObject file;
        private final RunnableFuture<Map<String,Integer>> lines;
        private final List<? super ErrorDescription> errors;

        Context(Document doc, LayerHandle layer, FileObject file, RunnableFuture<Map<String,Integer>> lines, List<? super ErrorDescription> errors) {
            this.doc = doc;
            this.layer = layer;
            this.file = file;
            this.lines = lines;
            this.errors = errors;
        }

        /**
         * Gets the layer entry you may offer hints for.
         * File attribute names like {@code literal:instanceCreate} may return values like {@code new:pkg.Clazz} or {@code method:pkg.Clazz.factory}.
         * @return a file (or folder) in the project's layer
         */
        public FileObject file() {
            return file;
        }

        /**
         * @return standard description to pass to {@link #addHint}
         */
        @Messages("Hinter.description=Use of layer entry where annotation is available")
        public String standardAnnotationDescription() {
            return Hinter_description();
        }

        /**
         * @return standard fix description to pass to {@link #addHint}
         */
        @Messages("Hinter.fix.description=Convert registration to Java annotation")
        public String standardAnnotationFixDescription() {
            return Hinter_fix_description();
        }

        /**
         * Add a hint.
         * @param severity whether to treat as a warning, etc.
         * @param description description of hint
         * @param fixes any fixes to offer
         * @see #addStandardAnnotationHint
         */
        public void addHint(Severity severity, String description, Fix... fixes) {
            Integer line = null;
            try {
                lines.run();
                line = lines.get().get(file.getPath());
            } catch (Exception x) {
                LOG.log(Level.INFO, null, x);
            }
            if (line != null) {
                errors.add(ErrorDescriptionFactory.createErrorDescription(severity, description, Arrays.asList(fixes), doc, line));
            } else {
                LOG.log(Level.WARNING, "no line found for {0}", file);
            }
        }

        /**
         * Add an annotation-oriented warning hint following the standard pattern.
         * @param fix what to do for a fix (see e.g. {@link #findAndModifyDeclaration}); no change info
         * @see #addHint
         * @see #standardAnnotationDescription
         * @see #standardAnnotationFixDescription
         */
        public void addStandardAnnotationHint(final Callable<Void> fix) {
            addHint(Severity.WARNING, standardAnnotationDescription(), new Fix() {
                public @Override String getText() {
                    return standardAnnotationFixDescription();
                }
                public @Override ChangeInfo implement() throws Exception {
                    fix.call();
                    return null;
                }
            });
        }

        /**
         * Checks whether a given API class can be accessed from the current classpath.
         * This can be used to control whether or not a given hint or fix is enabled.
         * @param api binary name of an API class
         * @return true if it is visible
         */
        public boolean canAccess(String api) {
            ClassPath cp = ClassPath.getClassPath(layer.getLayerFile(), ClassPath.COMPILE);
            if (cp == null) {
                return false;
            }
            return cp.findResource(api.replace('.', '/') + ".class") != null;
        }

        /**
         * Locate the declaration of an object declared as a newvalue or methodvalue attribute.
         * @param instanceAttribute the result of {@link FileObject#getAttribute} on a {@code literal:*} key
         * @return the source file containing the corresponding declaration, or null if not found
         */
        private @CheckForNull FileObject findDeclaringSource(@NullAllowed Object instanceAttribute) {
            if (!(instanceAttribute instanceof String)) {
                return null;
            }
            // XXX this will not find classes in a sister module; maybe look in ClassPath.EXECUTE, then use SFBQ to find it?
            ClassPath src = ClassPath.getClassPath(layer.getLayerFile(), ClassPath.SOURCE); // should work even for Maven src/main/resources/.../layer.xml
            if (src == null) {
                return null;
            }
            String attr = (String) instanceAttribute;
            if (attr.startsWith("new:")) {
                return src.findResource(attr.substring(4).replaceFirst("[$][^.]+$", "").replace('.', '/') + ".java");
            } else if (attr.startsWith("method:")) {
                return src.findResource(attr.substring(7, attr.lastIndexOf('.')).replaceFirst("[$][^.]+$", "").replace('.', '/') + ".java");
            } else {
                return null;
            }
        }

        /**
         * Locate the declaration of an object declared as a newvalue or methodvalue attribute.
         * @param wc context of a modification task
         * @param instanceAttribute the result of {@link FileObject#getAttribute} on a {@code literal:*} key
         * @return the corresponding declaration, or null if not found
         */
        private @CheckForNull Element findDeclaration(WorkingCopy wc, @NullAllowed Object instanceAttribute) {
            if (!(instanceAttribute instanceof String)) {
                return null;
            }
            String attr = (String) instanceAttribute;
            if (attr.startsWith("new:")) {
                return wc.getElements().getTypeElement(attr.substring(4).replace('$', '.'));
            } else if (attr.startsWith("method:")) {
                int dot = attr.lastIndexOf('.');
                TypeElement type = wc.getElements().getTypeElement(attr.substring(7, dot).replace('$', '.'));
                if (type != null) {
                    String meth = attr.substring(dot + 1);
                    for (Element check : type.getEnclosedElements()) {
                        if (check.getKind() == ElementKind.METHOD && check.getSimpleName().contentEquals(meth) && ((ExecutableElement) check).getParameters().isEmpty()) {
                            return check;
                        }
                    }
                }
            }
            return null;
        }

        /**
         * Saves the layer after some modifications to {@link #file()}.
         * @throws IOException if the layer could not be saved
         */
        public void saveLayer() throws IOException {
            layer.save();
        }

        /**
         * Task to be used from {@link #findAndModifyDeclaration}.
         */
        interface ModifyDeclarationTask {
            /**
             * Modify the original declaration.
             * @param wc Java source information
             * @param declaration the {@link TypeElement} or {@link ExecutableElement} that the instance attribute corresponds to
             * @param modifiers modifiers of the declaration that you might wish to add annotations to
             * @throws Exception in case of problem
             */
            void run(WorkingCopy wc, Element declaration, ModifiersTree modifiers) throws Exception;
        }

        /**
         * Tries to find the Java declaration of an instance attribute, and if successful, runs a task to modify it.
         * @param instanceAttribute the result of {@link FileObject#getAttribute} on a {@code literal:*} key
         * @param task a task to run (may modify Java sources and layer objects; all will be saved for you)
         * @throws IOException in case of problem (will instead show a message and return early if the type could not be found)
         */
        @Messages({"# {0} - layer attribute", "Hinter.missing_instance_class=Could not find Java source corresponding to {0}."})
        public void findAndModifyDeclaration(@NullAllowed final Object instanceAttribute, final ModifyDeclarationTask task) throws IOException {
            FileObject java = findDeclaringSource(instanceAttribute);
            if (java == null) {
                DialogDisplayer.getDefault().notify(new Message(Hinter_missing_instance_class(instanceAttribute), NotifyDescriptor.WARNING_MESSAGE));
                return;
            }
            JavaSource js = JavaSource.forFileObject(java);
            if (js == null) {
                throw new IOException("No source info for " + java);
            }
            js.runModificationTask(new Task<WorkingCopy>() {
                public @Override void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(JavaSource.Phase.RESOLVED);
                    Element decl = findDeclaration(wc, instanceAttribute);
                    if (decl == null) {
                        DialogDisplayer.getDefault().notify(new Message(Hinter_missing_instance_class(instanceAttribute), NotifyDescriptor.WARNING_MESSAGE));
                        return;
                    }
                    ModifiersTree mods;
                    if (decl.getKind() == ElementKind.CLASS) {
                        mods = wc.getTrees().getTree((TypeElement) decl).getModifiers();
                    } else {
                        mods = wc.getTrees().getTree((ExecutableElement) decl).getModifiers();
                    }
                    task.run(wc, decl, mods);
                    saveLayer();
                }
            }).commit();
            SaveCookie sc = DataObject.find(java).getLookup().lookup(SaveCookie.class);
            if (sc != null) {
                sc.save();
            }
        }

        /**
         * Converts a (possibly) localized string attribute in a layer into a value suitable for {@link org.openide.filesystems.annotations.LayerBuilder.File#bundlevalue(String,String)}.
         * @param attribute the result of {@link FileObject#getAttribute} on a {@code literal:*} key
         * @param declaration the declaring element (used to calculate package)
         * @return a string referring to the same (possibly) localized value (may be null)
         */
        public @CheckForNull String bundlevalue(@NullAllowed Object attribute, Element declaration) {
            if (attribute instanceof String) {
                String val = (String) attribute;
                if (val.startsWith("bundle:")) {
                    PackageElement pkg = findPackage(declaration);
                    if (pkg != null) {
                        String expected = "bundle:" + pkg.getQualifiedName() + ".Bundle#";
                        if (val.startsWith(expected)) {
                            return val.substring(expected.length() - 1); // keep '#'
                        }
                    }
                    return val.substring(7);
                }
                return val;
            } else {
                return null;
            }
        }
        private PackageElement findPackage(Element e) {
            if (e.getKind() == ElementKind.PACKAGE) {
                return ((PackageElement) e);
            }
            Element parent = e.getEnclosingElement();
            if (parent == null) {
                return null;
            }
            return findPackage(parent);
        }

        /**
         * Convenience method to add an annotation to an element.
         * @param wc Java source context
         * @param modifiers the element's modifiers to append to
         * @param type canonical name of the annotation type
         * @param parameters simple parameters of String or primitive type (null values are skipped)
         * @return the expanded modifiers tree
         */
        public @CheckReturnValue ModifiersTree addAnnotation(WorkingCopy wc, ModifiersTree modifiers, String type, Map<String,Object> parameters) {
            TreeMaker make = wc.getTreeMaker();
            TypeElement ann = wc.getElements().getTypeElement(type);
            if (ann == null) {
                // XXX does this deserve a localized message? generally hint should have been disabled already if missing
                throw new IllegalArgumentException("Could not find " + type + " in classpath");
            }
            List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();
            for (Map.Entry<String,Object> entry : parameters.entrySet()) {
                if (entry.getValue() != null) {
                    arguments.add(make.Assignment(make.Identifier(entry.getKey()), make.Literal(entry.getValue())));
                }
            }
            return make.addModifiersAnnotation(modifiers, make.Annotation(make.QualIdent(ann), arguments));
        }

        /**
         * Deletes an obsolete layer entry.
         * Also deletes empty parent directories.
         * @param entry a file to delete
         * @throws IOException in case of problem
         */
        public void delete(FileObject entry) throws IOException {
            entry.delete();
            FileObject parent = entry.getParent();
            if (parent.getChildren().length == 0 && !parent.getAttributes().hasMoreElements()) {
                if (parent.isRoot()) {
                    // XXX maybe delete the whole layer file! (and its reference in manifest.mf)
                } else {
                    delete(parent);
                }
            }
        }

    }

}
