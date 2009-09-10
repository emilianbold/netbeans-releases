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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006-2007 Sun
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

package org.netbeans.modules.junit.plugin;

//import java.util.Collections;
//import java.util.EnumSet;
import java.awt.EventQueue;
import java.util.Map;
//import java.util.Set;
//import javax.lang.model.element.Element;
//import javax.lang.model.element.ElementKind;
//import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.junit.JUnitPluginTrampoline;
import org.openide.filesystems.FileObject;

/**
 * SPI for custom implementations of support for JUnit.
 * It declares methods for:
 * <ul>
 *     <li>navigation between source classes and corresponding test classes
 *         ({@link #getTestLocation getTestLocation},
 *          {@link #getTestedLocation getTestedLocation})</li>
 *     <li>creation of test class skeletons
 *         ({@link #createTests createTests})</li>
 * </ul>
 *
 * @author  Marian Petras
 */
public abstract class JUnitPlugin {
    
    static {
        JUnitPluginTrampoline.DEFAULT = new JUnitPluginTrampoline() {
            public boolean createTestActionCalled(JUnitPlugin plugin,
                                                  FileObject[] filesToTest) {
                return plugin.createTestActionCalled(filesToTest);
            }
            public FileObject[] createTests(
                    JUnitPlugin plugin,
                    FileObject[] filesToTest,
                    FileObject targetRoot,
                    Map<CreateTestParam,Object> params) {
                return plugin.createTests(filesToTest, targetRoot, params);
            }
            public Location getTestLocation(
                    JUnitPlugin plugin,
                    Location sourceLocation) {
                return plugin.getTestLocation(sourceLocation);
            }
            public Location getTestedLocation(
                    JUnitPlugin plugin,
                    Location testLocation) {
                return plugin.getTestedLocation(testLocation);
            }
            public boolean canCreateTests(
                    JUnitPlugin plugin,
                    FileObject... fileObjects) {
                return plugin.canCreateTests(fileObjects);
            }
        };
    }
    
    /**
     * Default constructor for use by subclasses.
     */
    protected JUnitPlugin() {}

    /**
     * Enumeration of test creation parameters.
     */
    public enum CreateTestParam {
        
        /**
         * key for the map of test creation parameters
         * - name of the test class
         */
        CLASS_NAME(99310),
        /**
         * key for the map of test creation parameters
         * - include tests for public methods?
         */
        INC_PUBLIC(99311),
        /**
         * key for the map of test creation parameters
         * - include tests for protected methods?
         */
        INC_PROTECTED(99312),
        /**
         * key for the map of test creation parameters
         * - include tests for package-private methods?
         */
        INC_PKG_PRIVATE(99313),
        /**
         * key for the map of test creation parameters
         * - generate test initializer method ({@code setup()}/{@code @Before})?
         */
        INC_SETUP(99314),
        /**
         * key for the map of test creation parameters
         * - generate test finalizer method ({@code tearDown()}/{@code @After})?
         */
        INC_TEAR_DOWN(99315),
        /**
         * key for the map of test creation parameters
         * - generate test class initializer method ({@code @BeforeClass})?
         */
        INC_CLASS_SETUP(99323),
        /**
         * key for the map of test creation parameters
         * - generate test class finalizer method ({@code @AfterClass})?
         */
        INC_CLASS_TEAR_DOWN(99324),
        /**
         * key for the map of test creation parameters
         * - generate default test method bodies?
         */
        INC_METHOD_BODIES(99316),
        /**
         * key for the map of test creation parameters
         * - generate Javadoc comments for test methods?
         */
        INC_JAVADOC(99317),
        /**
         * key for the map of test creation parameters
         * - generate source code hints?
         */
        INC_CODE_HINT(99318),
        /**
         * key for the map of test creation parameters
         * - generate test classes for package-private classes?
         */
        INC_PKG_PRIVATE_CLASS(99319),
        /**
         * key for the map of test creation parameters
         * - generate test classes for abstract classes?
         */
        INC_ABSTRACT_CLASS(99320),
        /**
         * key for the map of test creation parameters
         * - generate test classes for exception classes?
         */
        INC_EXCEPTION_CLASS(99321),
        /**
         * key for the map of test creation parameters
         * - generate test suites for packages?
         */
        INC_GENERATE_SUITE(99322);
        
        private final int idNumber;
        
        CreateTestParam(int idNumber) {
            this.idNumber = idNumber;
        }
        
        /**
         * Return a unique number of this enum element.
         *
         * @return  unique number of this enum element
         */
        public int getIdNumber() {
            return idNumber;
        }
        
    }
    
    /**
     * Data structure for storage of specification of a Java element or
     * a Java file.
     */
    public static final class Location {
        //** */
        //public static final Set<ElementKind> CLASS_LIKE_ELEM_TYPES;
        //** */
        //public static final Set<ElementKind> SUPPORTED_ELEM_TYPES;
        /**
         * holds specification of a Java file
         */
        private final FileObject fileObject;
//        /**
//         */
//        private final ElementHandle<Element> elementHandle;
//        
//        static {
//            CLASS_LIKE_ELEM_TYPES = EnumSet.of(ElementKind.CLASS,
//                                               ElementKind.INTERFACE,
//                                               ElementKind.ENUM);
//            EnumSet<ElementKind> elemTypes;
//            elemTypes = EnumSet.copyOf(CLASS_LIKE_ELEM_TYPES);
//            elemTypes.addAll(EnumSet.of(ElementKind.METHOD,
//                                        ElementKind.CONSTRUCTOR,
//                                        ElementKind.STATIC_INIT));
//            SUPPORTED_ELEM_TYPES = Collections.unmodifiableSet(elemTypes);
//        }
        
        /**
         * Creates a new instance.
         *
         * @param  fileObject  the {@code FileObject}
         * 
         * 
         * 
         */
        public Location(FileObject fileObject/*,
                        Element element*/) {
            if (fileObject == null) {
               throw new IllegalArgumentException("fileObject is null");//NOI18N
            }
            
//            while ((element != null)
//                    && !SUPPORTED_ELEM_TYPES.contains(element.getKind())) {
//                element = element.getEnclosingElement();
//            }
            
            this.fileObject = fileObject;
            //this.elementHandle = (element != null)
            //                     ? ElementHandle.create(element)
            //                     : null;
        }
        
        /**
         * Returns the {@code FileObject}.
         *
         * @return  the {@code FileObject} held in this instance
         */
        public FileObject getFileObject() {
            return fileObject;
        }
        
//        /**
//         */
//        public ElementHandle<Element> getElementHandle() {
//            return elementHandle;
//        }
        
    }
    
    /**
     * Returns a specification of a Java element or file representing test
     * for the given source Java element or file.
     *
     * @param  sourceLocation  specification of a Java element or file
     * @return  specification of a corresponding test Java element or file,
     *          or {@code null} if no corresponding test Java file is available
     */
    protected abstract Location getTestLocation(Location sourceLocation);
    
    /**
     * Returns a specification of a Java element or file that is tested
     * by the given test Java element or test file.
     *
     * @param  testLocation  specification of a Java element or file
     * @return  specification of a Java element or file that is tested
     *          by the given Java element or file.
     */
    protected abstract Location getTestedLocation(Location testLocation);
    
    /**
     * Informs whether the plugin is capable of creating tests at the moment.
     * The default implementation returns {@code true}.
     *
     * @return  {@code true} if the plugin is able of creating tests
     *          for the given {@code FileObject}s, {@code false} otherwise
     * @see  #createTests
     */
    protected boolean canCreateTests(FileObject... fileObjects) {
        return true;
    }
    
    /**
     * Creates test classes for given source classes.
     * If the plugin does not support creating tests, implementation of this
     * method should return {@code null}.
     *
     * @param  filesToTest  source files for which test classes should be
     *                      created
     * @param  targetRoot   root folder of the target source root
     * @param  params  parameters of creating test class
     *                 - each key is an {@code Integer} whose value is equal
     *                 to some of the constants defined in the class;
     *                 the value is either
     *                 a {@code String} (for key with value {@code CLASS_NAME})
     *                 or a {@code Boolean} (for other keys)
     * @return  created test files, or {@code null} if no test classes were
     *          created and/or updated
     * @see  #canCreateTests
     */
    protected abstract FileObject[] createTests(
            FileObject[] filesToTest,
            FileObject targetRoot,
            Map<CreateTestParam, Object> params);

//    /**
//     * Determines whether the &quot;create JUnit tests&quot; functionality
//     * should be enabled.
//     * Before this method is called, other common pre-requisites are checked
//     * (only Java classes or folders selected, all of them from the same source
//     * of a Java project, all of them being valid {@code DataObject}s).
//     * If some of the pre-requisites are not met, the functionality is disabled
//     * and this method is not called.
//     *
//     * @return  {@code true} if this action should be enabled,
//     *          {@code false} otherwise;
//     *          the default implementation returns always {@code true}
//     */
//    protected boolean canCreateTests() {
//        return true;
//    }

    /**
     * Called immediately after the <em>Create Test</em> action was called.
     * It can be used as a trigger for additional checks and/or for displaying
     * user dialogs etc. It is always called from the event-dispatching thread.
     *
     * @param  selectedFiles  files and folders/packages that were selected
     *                        when the action was called
     * @return  {@code true} if the action can continue,
     *          {@code false} if the action should not continue;
     *          the default implementation returns always {@code true}
     */
    protected boolean createTestActionCalled(FileObject[] selectedFiles) {
        // assert EventQueue.isDispatchThread(); #170707

        return true;
    }

}
