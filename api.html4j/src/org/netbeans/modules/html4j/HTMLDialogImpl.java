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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html4j;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javax.swing.JButton;
import javax.swing.JComponent;
import net.java.html.boot.fx.FXBrowsers;
import org.openide.*;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public final class HTMLDialogImpl implements Runnable {
    private volatile int state;
    private NbFxPanel p;
    private DialogDescriptor dd;
    private WebView webView;
    
    private String url;
    private Runnable onPageLoad;
    private boolean nestedLoop;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setOnPageLoad(Runnable onPageLoad) {
        this.onPageLoad = onPageLoad;
    }
    
    @Override
    public void run() {
        switch (state) {
            case 0:
                initPanel();
                break;
            case 1:
                initFX();
                break;
            case 2:
                initPage();
                if (nestedLoop) {
                    state = 3;
                    EventQueue.invokeLater(this);
                } else {
                    state = -1;
                }
                break;
            case 3:
                showDialog();
                state = 4;
                Platform.runLater(this);
                break;
            case 4:
                state = -1;
                com.sun.javafx.tk.Toolkit.getToolkit().exitNestedEventLoop(this, null);
                break;
            default:
                throw new IllegalStateException("State: " + state);
        }
    }
    
    public String showAndWait() {
        if (EventQueue.isDispatchThread()) {
            run();
            showDialog();
        } else {
            if (Platform.isFxApplicationThread()) {
                nestedLoop = true;
                EventQueue.invokeLater(this);
                com.sun.javafx.tk.Toolkit.getToolkit().enterNestedEventLoop(this);
            } else {
                try {
                    EventQueue.invokeAndWait(this);
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                } catch (InvocationTargetException ex) {
                    throw new IllegalStateException(ex);
                }
                showDialog();
            }
        }
        Object val = dd.getValue();
        return val instanceof JButton ? ((JButton)val).getName() : null;
    }

    private void showDialog() {
        p.setPreferredSize(new Dimension(600, 400));
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
    }
    
    private void initPanel() {
        p = new NbFxPanel();
        dd = new DialogDescriptor(p, "");
        dd.setOptions(new Object[0]);
        state = 1;
        Platform.runLater(this);
    }
    
    private void initFX() {
        Platform.setImplicitExit(false);
        webView = new WebView();
        BorderPane bp = new BorderPane();
        Scene scene = new Scene(bp, Color.ALICEBLUE);

        class X implements ChangeListener<String>, Runnable {

            private String title;

            public X() {
                super();
            }

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                title = webView.getEngine().getTitle();
                EventQueue.invokeLater(this);
            }

            @Override
            public void run() {
                if (title != null) {
                    dd.setTitle(title);
                }
            }
        }
        final X x = new X();
        webView.getEngine().titleProperty().addListener(x);
        Platform.runLater(x);
        bp.setCenter(webView);
        p.setScene(scene);

        ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
        if (loader == null) {
            loader = HTMLDialogImpl.class.getClassLoader();
        }
        URL pageUrl;
        try {
            pageUrl = new URL(url);
        } catch (MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
        state = 2;
        FXBrowsers.load(webView, pageUrl, this, loader);
    }

    private void initPage() {
        try {
            onPageLoad.run();
        } catch (Throwable t) {
            Exceptions.printStackTrace(t);
        }
        final JButton[] buttons = Buttons.buttons();
        dd.setOptions(buttons);
    }

    public <C> C component(Class<C> type) {
        state = -1;
        ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
        if (loader == null) {
            loader = HTMLDialogImpl.class.getClassLoader();
        }
        final URL pageUrl;
        try {
            pageUrl = new URL(url);
        } catch (MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
        if (type == Node.class) {
            WebView wv = new WebView();
            FXBrowsers.load(wv, pageUrl, onPageLoad, loader);
            return type.cast(wv);
        } else if (type == JComponent.class) {
            final JFXPanel p = new JFXPanel();
            final ClassLoader l = loader;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    WebView wv = new WebView();
                    FXBrowsers.load(wv, pageUrl, onPageLoad, l);
                    Scene s = new Scene(wv);
                    p.setScene(s);
                }
            });
            return type.cast(p);
        } else {
            throw new IllegalStateException("Unsupported type: " + type);
        }
    }
}
