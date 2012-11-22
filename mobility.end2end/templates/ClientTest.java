/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
# import org.netbeans.mobility.end2end.core.model.*;
# import org.netbeans.mobility.end2end.core.model.classdata.*;
# import java.io.*;
# import java.util.*;
#
# ProtocolSupport support = new ProtocolSupport(data, this, true);
# String nameSuffix = "Test" + (data.isMIDlet() ? "" : "MIDlet");
# String outputDir = data.getClientOutputDirectory();
# setOut(support.getClientPath(data.getClientClassName() + nameSuffix));
# getOutput().addCreatedFile(support.getClientPath(data.getClientClassName() + nameSuffix));
${support.clientPackageLine()}
import java.util.*;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.midlet.*;

# String testClassName = data.getClientClassName() + nameSuffix;
# String extendsString = data.isMIDlet() ? "" : "extends MIDlet ";

public class ${testClassName} ${extendsString} {
#if(!data.isMIDlet()) {

    public void startApp() {
    }

    public void destroyApp(boolean b) {
    }

    public void pauseApp() {
    }
#}

# // generate constants for typesS
# ClassData[] types = data.getSupportedTypes();
# for (int i = 0; i < types.length; i++) {
#   String constantName = data.getConstantNameForType(types[i]);
#   short constantValue = data.getValueForType(types[i]);
public final static Short ${constantName} = new Short( (short) ${constantValue} );
#}

    public Displayable getTestDisplayable() {
        //List list = new List("Remote Methods");
    }

}
