/*
 * ElementScanningTask.java
 *
 * Created on November 9, 2006, 6:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.beans;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;

/** XXX Remove the ElementScanner class from here it should be wenough to
 * consult the Elements class. It should also permit for showing inherited members.
 *
 * @author phrebejk
 */
public class BeanScanningTask implements CancellableTask<CompilationInfo>{
    
    private BeanPanelUI ui;
    private volatile boolean canceled;
    
    public BeanScanningTask( BeanPanelUI ui ) {
        this.ui = ui;
    }
    
    public void cancel() {
        //System.out.println("Element task canceled");
        canceled = true;        
        }

    public void run(CompilationInfo info) throws Exception {
        
        canceled = false; // Task shared for one file needs reset first
        
        // XXX Scan for all classes and interfaces and run the
        // Pattern Analyser on them.

        //System.out.println("The task is running" + info.getFileObject().getNameExt() + "=====================================" ) ;
        
//        Description rootDescription = new Description( ui );
//        rootDescription.fileObject = info.getFileObject();
//        rootDescription.subs = new ArrayList<Description>();

        // Get all outerclasses in the Compilation unit
        CompilationUnitTree cuTree = info.getCompilationUnit();
        List<? extends Tree> typeDecls = cuTree.getTypeDecls();
        List<Element> elements = new ArrayList<Element>( typeDecls.size() );
        TreePath cuPath = new TreePath( cuTree );
        for( Tree t : typeDecls ) {
            TreePath p = new TreePath( cuPath, t );
            Element e = info.getTrees().getElement( p );
            if ( e != null ) {
                elements.add( e );
            }
        }
        
        ArrayList<ClassPattern> classPatterns = new ArrayList<ClassPattern>(elements.size());
        for( Element e : elements ) {
            if ( e.getKind() == ElementKind.CLASS || e.getKind() == ElementKind.INTERFACE ) {
                PatternAnalyser pa = new PatternAnalyser( info.getFileObject(), ui );
                pa.analyzeAll(info, (TypeElement)e);
                ClassPattern cp = new ClassPattern(pa, e.asType(), 
                                                   BeanUtils.nameAsString(e));
                classPatterns.add(cp);
            }
        }
        
        PatternAnalyser pa = new PatternAnalyser( info.getFileObject(), ui, classPatterns );
        
        // Refresh the GUI
        if ( !canceled ) {
            ui.refresh( new ClassPattern(pa));            
        }
        
    }
        
}
