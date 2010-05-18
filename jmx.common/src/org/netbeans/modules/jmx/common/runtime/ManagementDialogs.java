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
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.common.runtime;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import java.awt.*;

public class ManagementDialogs {
  private static DialogDisplayer standard = DialogDisplayer.getDefault();
  private static ManagementDialogs defaultInstance;

  private ManagementDialogs () { } // avoid direct instance creation

  public static ManagementDialogs getDefault () {
    if (defaultInstance == null)
      defaultInstance = new ManagementDialogs ();
    return defaultInstance;
  }

  public Object notify(NotifyDescriptor descriptor) {
    return standard.notify(descriptor);
  }

  public Dialog createDialog(DialogDescriptor descriptor) {
    return standard.createDialog(descriptor);
  }


  public static class DNSAMessage extends NotifyDescriptor.Message {
    /**
     * Create an informational report about the results of a command.
     *
     * @param message the message object
     * @see org.openide.NotifyDescriptor#NotifyDescriptor
     */
    public DNSAMessage(String key, Object message) {
      super(message);
    }

    /**
     * Create a report about the results of a command.
     *
     * @param message     the message object
     * @param messageType the type of message to be displayed
     * @see org.openide.NotifyDescriptor#NotifyDescriptor
     */
    public DNSAMessage(String key, Object message, int messageType) {
      super(message, messageType);
    }
  }

  public static class DNSAConfirmation extends NotifyDescriptor.Confirmation {

    /**
     * Create a yes/no/cancel question with default title.
     *
     * @param message the message object
     * @see org.openide.NotifyDescriptor#NotifyDescriptor
     */
    public DNSAConfirmation(String key, Object message) {
      super(message);
    }

    /**
     * Create a yes/no/cancel question.
     *
     * @param message the message object
     * @param title   the dialog title
     * @see org.openide.NotifyDescriptor#NotifyDescriptor
     */
    public DNSAConfirmation(String key, Object message, String title) {
      super(message, title);
    }

    /**
     * Create a question with default title.
     *
     * @param message    the message object
     * @param optionType the type of options to display to the user
     * @see org.openide.NotifyDescriptor#NotifyDescriptor
     */
    public DNSAConfirmation(String key, Object message, int optionType) {
      super(message, optionType);
    }

    /**
     * Create a question.
     *
     * @param message    the message object
     * @param title      the dialog title
     * @param optionType the type of options to display to the user
     * @see org.openide.NotifyDescriptor#NotifyDescriptor
     */
    public DNSAConfirmation(String key, Object message, String title, int optionType) {
      super(message, title, optionType);
    }

    /**
     * Create a confirmation with default title.
     *
     * @param message     the message object
     * @param optionType  the type of options to display to the user
     * @param messageType the type of message to use
     * @see org.openide.NotifyDescriptor#NotifyDescriptor
     */
    public DNSAConfirmation(String key, Object message, int optionType, int messageType) {
      super(message, optionType, messageType);
    }

    /**
     * Create a confirmation.
     *
     * @param message     the message object
     * @param title       the dialog title
     * @param optionType  the type of options to display to the user
     * @param messageType the type of message to use
     * @see org.openide.NotifyDescriptor#NotifyDescriptor
     */
    public DNSAConfirmation(String key, Object message, String title, int optionType, int messageType) {
      super(message, title, optionType, messageType);
    }
  }
}
