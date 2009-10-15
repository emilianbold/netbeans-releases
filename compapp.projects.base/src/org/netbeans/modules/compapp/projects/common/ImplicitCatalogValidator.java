/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.compapp.projects.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.projects.common.CatalogWSDL.Entry;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * This class can be used to validate the implicit catalog entries. It reads
 * the catalog.wsdl and checks if the wsdl/xsd imports in the catalog.wsdl have a 
 * valid uri reference for their system id (location/schemaLocation). It first
 * will locate the file object corresponding to the uri reference and then
 * optionally load xdm model also to validate the contents.
 * Report error, warning and info level messages to the print output (defaults
 * to the system.out). This print output can be redirected by setting the ResultPrinter
 * on the validator.
 * <p><blockquote><pre>
 *     Project prj = ... // get the project
 *     ImplicitCatalogValidator validator = new ImplicitCatalogValidator(prj);
 *     StringResult out = new StringResult(); // results printed to a string.
 *     validator.setResultPrinter(out);
 *     boolean validated = validator.validate();
 *     if (validated) {
 *          // continue.
 *     } else {
 *          System.out.println("Validation Failed:" + out.toString());
 *     }
 *     
 * 
 * </blockquote></pre><p>
 * 
 * @see ResultPrinter
 * @author chikkala
 */
public class ImplicitCatalogValidator {

    /** logger */
    private static final Logger sLogger = Logger.getLogger(ImplicitCatalogValidator.class.getName());
    /** Project in which the implicit catalog is validated*/
    private Project mPrj;
    /** catalog wsdl file model */
    private CatalogWSDL mCatalogWSDL;
    /** implicit catalog support reference */
    private ImplicitCatalogSupport mCatalogSupport;
    /** printer to print results */
    private ResultPrinter mPrn;

    /**
     * create constructor that validates the implicit catalog.
     * @param catSupport
     * @param catWSDL
     */
    public ImplicitCatalogValidator(ImplicitCatalogSupport catSupport, CatalogWSDL catWSDL) {
        this.mPrn = new ResultPrinter();
        this.mCatalogSupport = catSupport;
        this.mCatalogWSDL = catWSDL;
    }

    /**
     * This method creates a new ImplicitCatalogValidator using the 
     * ImplicitCatalogSupport and the Catalog wsdl file loaded from the project.
     * @param prj project on which the validation should be performed.
     * @return validator
     * @throws java.io.IOException on error
     */
    public static ImplicitCatalogValidator newInstance(Project prj) throws IOException {
        ImplicitCatalogValidator newInstance = null;
        ImplicitCatalogSupport catSupport = ImplicitCatalogSupport.getInstance(prj);
        CatalogWSDL catWSDL = CatalogWSDL.loadCatalogWSDL(prj);
        newInstance = new ImplicitCatalogValidator(catSupport, catWSDL);
        return newInstance;
    }

    /**
     * sets the result printer. By default a printer is initialized that prints
     * the results to stdout.
     * @param out
     */
    public void setResultPrinter(ResultPrinter out) {
        this.mPrn = out;
    }

    /**
     * return printer
     * @return
     */
    protected ResultPrinter getResultPrinter() {
        return this.mPrn;
    }

    /**
     * 
     * @return
     */
    protected CatalogWSDL getCatalogWSDL() {
        return this.mCatalogWSDL;
    }

    /**
     * 
     * @return
     */
    protected ImplicitCatalogSupport getImplicitCatalogSupport() {
        return this.mCatalogSupport;
    }

    /**
     * validates the catalog.wsdl entries by checking the correpsonding entries
     * in the catalog.xml and also existance of the actual file and optionally 
     * its valid contents (checks if the xam model is valid or not).
     * @return true if the validation is successful or false if any of the validation
     * is failed with errors.
     */
    public boolean validate() {
        String startedMsg = 
                NbBundle.getMessage(ImplicitCatalogValidator.class, "icat.validator.msg.started");
        String finishedMsg = 
                NbBundle.getMessage(ImplicitCatalogValidator.class, "icat.validator.msg.finished");
        this.mPrn.println(startedMsg);
        List<Entry> entries = this.mCatalogWSDL.getEntries();
        for (Entry entry : entries) {
            FileObject fo = null;
            try {
                fo = this.mCatalogSupport.resolveImplicitReference(entry.getLocation());
                printResult(entry, fo);
            } catch (Exception ex) {
                printResult(entry, ex);
                sLogger.log(Level.FINE, ex.getMessage(), ex);
            }
        }
        this.mPrn.println(finishedMsg);
        return true;
    }

    /**     
     * prints out the result of validating an entry in catalog.wsdl
     * @param entry entry in the catalog.wsdl
     * @param fo resolved file corresponding to that entry.
     */
    protected void printResult(Entry entry, FileObject fo) {
        String systemIdMsg =
                NbBundle.getMessage(this.getClass(), "icat.validator.msg.entry.systemid", entry.getLocation());
        String typeMsg =
                NbBundle.getMessage(this.getClass(), "icat.validator.msg.entry.type", entry.getType());
        if (fo == null) {
            String errMsg =
                    NbBundle.getMessage(this.getClass(), "icat.validator.msg.error", entry.getNamesapce());
            String notFoundMsg =
                    NbBundle.getMessage(this.getClass(), "icat.validator.msg.entry.result.file.not.found");
            String resultMsg =
                    NbBundle.getMessage(this.getClass(), "icat.validator.msg.entry.result", notFoundMsg);

            this.mPrn.println(errMsg);
            this.mPrn.println(systemIdMsg);
            this.mPrn.println(typeMsg);
            this.mPrn.println(resultMsg);
        } else {
            String infoMsg =
                    NbBundle.getMessage(this.getClass(), "icat.validator.msg.info", entry.getNamesapce());
            String fileMsg =
                    NbBundle.getMessage(this.getClass(), "icat.validator.msg.entry.result.file", fo.getPath());
            String resultMsg =
                    NbBundle.getMessage(this.getClass(), "icat.validator.msg.entry.result", fileMsg);
            this.mPrn.println(infoMsg);
            this.mPrn.println(systemIdMsg);
            this.mPrn.println(typeMsg);
            this.mPrn.println(resultMsg);
        }
    }

    /**
     * prints any exception results in validating the entry
     * @param entry
     * @param ex
     */
    protected void printResult(Entry entry, Exception ex) {
        String errMsg =
                NbBundle.getMessage(this.getClass(), "icat.validator.msg.error", entry.getNamesapce());        
        String systemIdMsg =
                NbBundle.getMessage(this.getClass(), "icat.validator.msg.entry.systemid", entry.getLocation());
        String typeMsg =
                NbBundle.getMessage(this.getClass(), "icat.validator.msg.entry.type", entry.getType());
        String resultMsg =
                NbBundle.getMessage(this.getClass(), "icat.validator.msg.entry.result", ex.getMessage());
        this.mPrn.println(errMsg);
        this.mPrn.println(systemIdMsg);
        this.mPrn.println(typeMsg);
        this.mPrn.println(resultMsg);        
        
    // TODO: print statcktrace to log 
    }

    /**
     * This class is a print interface for the validation results. By default it
     * prints the result output to stdout. Extended classes
     * from this class can override this behaviour by setting different output 
     * writers which may redirect the printing to various outputs (like special
     * output window, or logger etc).
     */
    public static class ResultPrinter {

        /** standard output writer*/
        private PrintWriter mOut;
        /** error output writer */
        private PrintWriter mErr;

        /** constructor */
        public ResultPrinter() {
            initWriters();
        }

        /**
         * initializes the writers. By default it initializes writes to system.out and err.
         * Called from the constructor to initialize the writers. extended classes
         * can overwrite this method to initilize the error and output writers to 
         * different writers. 
         */
        protected void initWriters() {
            PrintWriter out = new PrintWriter(System.out, true);
            PrintWriter err = new PrintWriter(System.err, true);
            setOutWriter(out);
            setErrorWriter(err);
        }

        /**
         * 
         * @param out
         */
        protected final void setOutWriter(PrintWriter out) {
            this.mOut = out;
        }

        /**
         * 
         * @return
         */
        protected final PrintWriter getOutWriter() {
            return this.mOut;
        }

        /**
         * 
         * @param err
         */
        protected final void setErrorWriter(PrintWriter err) {
            this.mErr = err;
        }

        /**
         * 
         * @return
         */
        protected final PrintWriter getErrorWriter() {
            return this.mErr;
        }

        /**
         * 
         * @param obj
         */
        public void println(Object obj) {
            this.mOut.println(obj);
        }

        /**
         * 
         * @param obj
         */
        public void error(Object obj) {
            this.mErr.print(obj);
        }

        /**
         * 
         * @param obj
         */
        public void warning(Object obj) {
            this.mErr.print(obj);
        }
    }

    /**
     *  ResultPrinter that prints results to a string.
     */
    public static class StringResult extends ResultPrinter {

        private StringWriter mStrWriter;

        /**
         * initializes both error and outout writers to a string writer.
         */
        @Override
        protected void initWriters() {
            this.mStrWriter = new StringWriter();
            PrintWriter out = new PrintWriter(mStrWriter, true);
            setOutWriter(out);
            setErrorWriter(out); // both err and out to same file.
        }

        @Override
        public String toString() {
            return this.mStrWriter.getBuffer().toString();
        }
    }
}
