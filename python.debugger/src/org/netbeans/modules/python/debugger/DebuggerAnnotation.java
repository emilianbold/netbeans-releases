/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.python.debugger;

import org.openide.text.Annotation;
import org.openide.text.Annotatable;

/**
 *
 * @author jean-yves Mengant
 */
public class DebuggerAnnotation
        extends Annotation {

  /** Annotation type constant. */
  public static final String BREAKPOINT_ANNOTATION_TYPE =
          new String("Breakpoint");
  /** Annotation type constant. */
  public static final String DISABLED_BREAKPOINT_ANNOTATION_TYPE =
          new String("DisabledBreakpoint");
  /** Annotation type constant. */
  public static final String CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE =
          new String("CondBreakpoint");
  /** Annotation type constant. */
  public static final String DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE =
          new String("DisabledCondBreakpoint");
  /** Annotation type constant. */
  public static final String CURRENT_LINE_ANNOTATION_TYPE =
          new String("CurrentPC");
  /** Annotation type constant. */
  public static final String CURRENT_LINE_ANNOTATION_TYPE2 =
          new String("CurrentPC2");
  /** Annotation type constant. */
  public static final String CURRENT_LINE_PART_ANNOTATION_TYPE =
          new String("CurrentPCLinePart");
  /** Annotation type constant. */
  public static final String NEXT_TARGET_NAME =
          new String("NextTargetName");
  /** Annotation type constant. */
  public static final String CURRENT_LINE_PART_ANNOTATION_TYPE2 =
          new String("CurrentPC2LinePart");
  /** Annotation type constant. */
  public static final String CALL_STACK_FRAME_ANNOTATION_TYPE =
          new String("CallSite");
  private Annotatable _annotatable;
  private String _type;

  /** Creates a new instance of DebuggerAnnotation */
  public DebuggerAnnotation(String type, Annotatable annotatable) {
    _type = type;
    _annotatable = annotatable;
    if (_annotatable != null) {
      attach(annotatable);
    }
  }

  public String getShortDescription() {
    if (_type == BREAKPOINT_ANNOTATION_TYPE) {
      return "Breakpoint";
    } else if (_type == DISABLED_BREAKPOINT_ANNOTATION_TYPE) {
      return "Disabled Breakpoint";
    } else if (_type == CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE) {
      return "Disabled Conditional Breakpoint";
    } else if (_type == DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE) {
      return "Disabled Conditional Breakpoint";
    } else if (_type == CURRENT_LINE_ANNOTATION_TYPE) {
      return "Current Program Counter";
    } else if (_type == CALL_STACK_FRAME_ANNOTATION_TYPE) {
      return "Call Stack Line";
    }
    return "TOOLTIP_ANNOTATION";
  }

  public String getAnnotationType() {
    return _type;
  }
}
