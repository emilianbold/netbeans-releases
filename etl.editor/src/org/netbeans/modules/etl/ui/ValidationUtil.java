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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.etl.ui;

import java.util.ArrayList;
import java.util.List;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import javax.swing.text.StyledDocument;
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

public final class ValidationUtil {
    
    private ValidationUtil() {}
    
    public static List<ResultItem> filterResultItems(List<ResultItem> validationResults) {
        List<ResultItem> resultItems = new ArrayList<ResultItem>();
        
        for(ResultItem resultItem: validationResults) {
            Component component = resultItem.getComponents();

            /*if(component instanceof EtlEntity) {
                ResultItem eTLResultItem = 
                    new ResultItem(resultItem.getValidator(),
                        resultItem.getType(), component, 
                        resultItem.getDescription());
                resultItems.add(eTLResultItem);
            }*/
        }
        return resultItems;
    }
    
    public static boolean equals(ResultItem item1, ResultItem item2){
        if (item1 == item2){
            return true;
        }
        if(!item1.getDescription().equals(item2.getDescription())) {
            return false;
        }
        
        if(!item1.getType().equals(item2.getType())) {
            return false;
        }
        
        Component components1 = item1.getComponents();
        Component components2 = item2.getComponents();
        
        if(components1 != components2) {
            return false;
        }
        return true;
    }

    private static boolean contains(List<ResultItem> list, ResultItem resultItem) {
        assert list!=null;
        for (ResultItem item: list) {
            if (equals(item, resultItem)){
                return true;
            }
        }
        return false;
    }

    public static Line getLine(ResultItem item) {
      int number;
      Component component = item.getComponents();

      if (component != null) {
        number = -1;

        if (component instanceof DocumentComponent) {
          number = getLineNumber((DocumentComponent) item.getComponents());
        } 
      }
      else {
        number = item.getLineNumber() - 1;
      }
  //System.out.println("  number: " + number);

      if (number < 1) {
        return null;
      }
      FileObject file = getFileObjectByModel(component == null ? item.getModel() : component.getModel());

      if (file == null) {
        return null;
      }
      LineCookie cookie = null;

      try {
        DataObject data = DataObject.find(file);
        cookie = (LineCookie) data.getCookie(LineCookie.class);
      }
      catch (DataObjectNotFoundException e) {
        e.printStackTrace();
      }
      if (cookie == null) {
        return null;
      }
      return cookie.getLineSet().getCurrent(number);
    }
    
    private static int getLineNumber(DocumentComponent entity) {
      if (entity == null) {
        return -1;
      }
      Model model = entity.getModel();

      if (model == null) {
        return -1;
      }
      ModelSource source = model.getModelSource();

      if (source == null) {
        return -1;
      }
      Lookup lookup = source.getLookup();

      if (lookup == null) {
        return -1;
      }
      StyledDocument document = (StyledDocument) lookup.lookup(StyledDocument.class);

      if (document == null) {
        return -1;
      }
      return NbDocument.findLineNumber(document, entity.findPosition());
    }

    private static FileObject getFileObjectByModel(Model model) {
      if (model == null) {
        return null;
      }
      ModelSource src = model.getModelSource();

      if (src == null) {
       return null;
      }
      Lookup lookup = src.getLookup();

      if (lookup == null) {
        return null;
      }
      return lookup.lookup(FileObject.class);
    }
}
