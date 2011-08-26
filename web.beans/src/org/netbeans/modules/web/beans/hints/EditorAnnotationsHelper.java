/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
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
package org.netbeans.modules.web.beans.hints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.web.beans.analysis.CdiEditorAnalysisFactory;
import org.netbeans.modules.web.beans.hints.CDIAnnotation.CDIAnnotaitonType;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.Line.Part;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;

import com.sun.source.tree.Tree;


/**
 * @author ads
 *
 */
public final class EditorAnnotationsHelper implements PropertyChangeListener {
    
    private static ConcurrentHashMap<DataObject, EditorAnnotationsHelper> HELPERS 
        = new ConcurrentHashMap<DataObject, EditorAnnotationsHelper>();
    
    private static final RequestProcessor PROCESSOR = new RequestProcessor(
            EditorAnnotationsHelper.class.getName(), 1, false, false);
    
    private EditorAnnotationsHelper( DataObject dataObject , 
            EditorCookie.Observable observable )
    {
        myDataObject = dataObject;
        myObservable = observable;
        myAnnotations = new AtomicReference<List<CDIAnnotation>>(
                new LinkedList<CDIAnnotation>());
        myCollectedAnnotations = new AtomicReference<List<CDIAnnotation>>();
        myCollectedAnnotations.set( new LinkedList<CDIAnnotation>());
        
        observable.addPropertyChangeListener(this );
    }
    
    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange( PropertyChangeEvent evt ) {
        if (EditorCookie.Observable.PROP_OPENED_PANES.endsWith(evt.getPropertyName()) 
                || evt.getPropertyName() == null)
        {
            if (myObservable.getOpenedPanes() == null) {
                myObservable.removePropertyChangeListener(this);

                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        HELPERS.remove(myDataObject);
                        myCollectedAnnotations.set(new LinkedList<CDIAnnotation>());
                        List<CDIAnnotation> annotations = myAnnotations.get();
                        for (CDIAnnotation annotation : annotations) {
                            annotation.detach();
                        }
                    }
                };
                PROCESSOR.submit(runnable);
            }
        }        
    }
    
    public static EditorAnnotationsHelper getInstance( CdiAnalysisResult result ){
        return getInstance( result.getInfo().getFileObject());
    }
    
    public static EditorAnnotationsHelper getInstance( FileObject fileObject ){
        try {
            DataObject dataObject = DataObject.find(fileObject);
            EditorAnnotationsHelper helper = HELPERS.get(dataObject);

            if (helper != null) {
                return helper;
            }

            EditorCookie.Observable observable = dataObject.getLookup().lookup(
                    EditorCookie.Observable.class);

            if (observable == null) {
                return null;
            }

            helper = new EditorAnnotationsHelper( dataObject , observable );
            HELPERS.put(dataObject, helper );

            return helper;
        } catch (IOException ex) {
            Logger.getLogger( EditorAnnotationsHelper.class.getName() ).
                log(Level.INFO, null, ex);
            return null;
        }
    }

    public void addInjectionPoint( CdiAnalysisResult result, VariableElement element )
    {
        addAnnotation(result, element, CDIAnnotaitonType.INJECTION_POINT );
    }

    public void addDelegate( CdiAnalysisResult result ,  VariableElement element ) {
        addAnnotation(result, element, CDIAnnotaitonType.DELEGATE_POINT );
    }

    public void publish( final CdiAnalysisResult result ) {
        Runnable runnable = new Runnable() {
            
            @Override
            public void run() {
                List<CDIAnnotation> annotations = myAnnotations.get();
                for (CDIAnnotation annotation : annotations) {
                    annotation.detach();
                }
                List<CDIAnnotation> collected = myCollectedAnnotations.get();
                if ( HELPERS.get( myDataObject ) == null ){
                    collected = Collections.emptyList();
                }
                else {
                    collected = myCollectedAnnotations.get();
                }
                
                for (CDIAnnotation annotation : collected) {
                    annotation.attach( annotation.getPart() );
                }
                myAnnotations.set(collected);
                myCollectedAnnotations.set(new LinkedList<CDIAnnotation>());
            }
        };
        PROCESSOR.submit(runnable);
    }
    
    public List<CDIAnnotation> getAnnotations(){
        return myAnnotations.get();
    }

    private void addAnnotation( CdiAnalysisResult result, Element element , 
            CDIAnnotaitonType type) 
    {
        Tree var = result.getInfo().getTrees().getTree( element );
        List<Integer> position = CdiEditorAnalysisFactory.getElementPosition( 
                result.getInfo(),  var );
        Document document;
        try {
            document = result.getInfo().getDocument();
            if ( !( document instanceof StyledDocument) ){
                return;
            }
        }
        catch (IOException e) {
            return;
        }
        int start = position.get(0);
        Line line = NbEditorUtilities.getLine( document , start, true);
        Part part = line.createPart( NbDocument.findLineColumn((StyledDocument) document,
                start),  position.get( 1 ) -start);
        myCollectedAnnotations.get().add( new CDIAnnotation( type, part));
    }
    
    private DataObject myDataObject;
    private EditorCookie.Observable myObservable;
    private AtomicReference<List<CDIAnnotation>> myAnnotations;
    private AtomicReference<List<CDIAnnotation>> myCollectedAnnotations;

}
