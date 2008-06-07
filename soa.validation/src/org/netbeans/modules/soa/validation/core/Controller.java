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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.soa.validation.core;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;
import java.util.prefs.Preferences;

import org.openide.text.Line;
import org.openide.util.NbPreferences;
import org.netbeans.modules.xml.validation.ValidateAction;
import org.netbeans.modules.xml.validation.ValidateAction.RunAction;
import org.netbeans.modules.xml.validation.ValidationOutputWindowController;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;

import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.netbeans.modules.soa.validation.util.LineUtil;
import static org.netbeans.modules.xml.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.01.17
 */
public final class Controller implements ComponentListener {
    
  public Controller(Model model) {
    myModel = model;
    myTimer = new Timer();
    myResult = new LinkedList<ResultItem>();
    myListeners = new WeakHashMap<Listener, Object>();
    myAnnotations = new LinkedList<Annotation>();
  }

  public Model getModel() {
    return myModel;
  }

  public void attach() {
    myModel.addComponentListener(this);
  }

  public void detach() {
    if (myModel != null) {
      myModel.removeComponentListener(this);
    }
  }

  public void addListener(Listener listener) {
    synchronized (myListeners) {
      myListeners.put(listener, null);
    }
  }
  
  public void removeListener(Listener listener) {
    synchronized (myListeners) {
      myListeners.remove(listener);
    }
  }
  
  public List<ResultItem> getResult() {
    return myResult;
  }

  public void startValidation() {
    log();
    log("START ..."); // NOI18N
    doValidation(true, true);
  }

  public void runValidation() {
    log();
    log("RUN ..."); // NOI18N
    doValidation(true, false);
  }

  public void triggerValidation() {
//stackTrace();
    log();
    log("ALLOW Background Validation: " + isAllowBackgroundValidation()); // NOI18N
    log();

    if ( !isAllowBackgroundValidation()) {
      return;
    }
    log();
    log("TIMER-TRIGGER"); // NOI18N
    log();

    cancelValidation();

    myTimer.schedule(new TimerTask() {
      public void run() {
        doValidation(false, false);
      }
    },
    DELAY);
  }

  public boolean cliValidate(File file) {
    return validate(file, true);
  }

  public boolean ideValidate(File file) {
    return validate(file, false);
  }

  private boolean validate(File file, boolean isCommandLine) {
    PrintStream stream;
    List<ResultItem> result;

    if (isCommandLine) {
      stream = System.out;
      result = validate(ValidationType.PARTIAL);
    }
    else {
      stream = System.err;
      result = validate(ValidationType.COMPLETE);
    }
    boolean isError = false;

    for (ResultItem item : result) {
      if (item.getType() == Validator.ResultType.ERROR) {
        isError = true;
      }
      else {
        if (isCommandLine) {
          continue;
        }
      }
      stream.println(LineUtil.getValidationError(file, item));
      stream.println();
    }
    return isError;
  }

  public List<ResultItem> validate(ValidationType type) {
    Validation validation = new Validation();
    validation.validate(myModel, type);
    return validation.getValidationResult();
  }

  private void doValidation(boolean isComplete, boolean isOutput) {
    cancelValidation();

    List<ResultItem> items;
    ValidationType type;
    int priority;

    if (isComplete) {
      type = ValidationType.COMPLETE;
      priority = Thread.MAX_PRIORITY;
    }
    else {
      type = ValidationType.PARTIAL;
      priority = Thread.MIN_PRIORITY;
    }
    Thread.currentThread().setPriority(priority);

    log();
    log("VALIDATION: " + type); // NOI18N
    startTimeln();

    if (isOutput) {
      RunAction action = new ValidateAction(myModel).new RunAction();
      action.run();
      items = action.getValidationResults();
    }
    else {
      if (isComplete) {
        items = new ValidationOutputWindowController().validate(myModel);
      }
      else {
        items = validate(type);
      }
    }
    endTime("validation"); // NOI18N
    log("end: " + type); // NOI18N

    notifyListeners(items);
  }

  private void cancelValidation() {
    myTimer.cancel();
    myTimer = new Timer();
    Validation.stop();
  }

  private Preferences getPreferences() {
    return NbPreferences.forModule(Controller.class);
  } 

  private boolean get(String name, boolean defaultValue) {
    return getPreferences().getBoolean(name, defaultValue);
  }

  private boolean isAllowBackgroundValidation() {
    return get(ALLOW_BACKGROUND_VALIDATION, true);
  }

  private void notifyListeners(List<ResultItem> items) {
    if (items == null) {
      return;
    }
    myResult = items;
    log("+++: Notify listeners"); // NOI18N

    synchronized (myListeners) {
      for (Listener listener : myListeners.keySet()) {
        if (listener != null) {
          listener.validationUpdated(myResult);
        }
      }
    }
    showAnnotations();
  }
  
  private void showAnnotations() {
    synchronized (myAnnotations) {
      for (Annotation annotation : myAnnotations) {
        annotation.detach();
      }
//out();
//out("SHOW ANNOTATION IN EDITOR");
      myAnnotations.clear();
      Map<Line.Part, List<ResultItem>> map = new HashMap<Line.Part, List<ResultItem>>();
  
      for (ResultItem item : myResult) {
        if (item.getType() != ResultType.ERROR) {
          continue;
        }
        Line.Part part = LineUtil.getLinePart(item);

        if (part == null) {
          continue;
        }
        List<ResultItem> list = map.get(part);

        if (list == null) {
          list = new LinkedList<ResultItem>();
          map.put(part, list);
        }
        list.add(item);
      }
      for (Line.Part part : map.keySet()) {
        StringBuilder description = new StringBuilder();
        List<ResultItem> list = map.get(part);

        for (int i=0; i < list.size(); i++) {
          description.append(list.get(i).getDescription());
          
          if (i < list.size() - 1) {
            description.append("\n\n"); // NOI18N
          }
        }
        myAnnotations.add(new Annotation(part, description.toString()));
      }
    }
  }

  public void valueChanged(ComponentEvent event) {
//out("CHANGED");
    triggerValidation();
  }
  
  public void childrenAdded(ComponentEvent event) {
//out("ADDED");
    triggerValidation();
  }
  
  public void childrenDeleted(ComponentEvent event) {
//out("DELETED");
    triggerValidation();
  }
  
  private Model myModel;
  private Timer myTimer;
  private List<ResultItem> myResult;
  private List<Annotation> myAnnotations;
  private Map<Listener, Object> myListeners;

  private static final long DELAY = 5432L;
  private static final String ALLOW_BACKGROUND_VALIDATION = "allow.background.validation"; // NOI18N
}
