/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.apt.support;

import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.impl.support.clank.ClankDriverImpl;
import org.netbeans.modules.cnd.apt.impl.support.clank.ClankPPCallback;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.support.Interrupter;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class ClankDriver {


    private ClankDriver() {
    }

    public static final class APTTokenStreamCache {
      private final APTToken[] tokens;
      private final int[] skippedRanges;
      private final int fileIndex;

      public APTTokenStreamCache(ClankFileInfo fileInfo) {
        assert fileInfo instanceof ClankPPCallback.ClankFileInfoImpl;
        ClankPPCallback.ClankFileInfoImpl fileInfoImpl = (ClankPPCallback.ClankFileInfoImpl) fileInfo;
        this.skippedRanges = fileInfoImpl.getSkippedRanges();
        this.fileIndex = fileInfoImpl.getIncludeIndex();
        this.tokens = fileInfoImpl.getStolenTokens();
      }

      public APTTokenStreamCache(int[] skippedRanges, APTToken[] tokens, int fileIndex) {
        this.skippedRanges = skippedRanges;
        this.tokens = tokens;
        this.fileIndex = fileIndex;
      }

      public int getFileIndex() {
        return fileIndex;
      }

      public int[] getSkippedRanges() {
          return skippedRanges;
      }

      public TokenStream getTokenStream() {
          return new ClankDriverImpl.ArrayBasedAPTTokenStream(tokens);
      }

      public boolean hasTokenStream() {
          return tokens != null;
      }

      @Override
      public String toString() {
        return "APTTokenStreamCache{" + "tokens=" + hasTokenStream() + "\nskippedRanges=" + skippedRanges + "\nfileIndex=" + fileIndex + '}';
      }
    }

    public static void cacheTokenStream(PreprocHandler ppHandler, ClankDriver.APTTokenStreamCache toCache) {
      ClankDriverImpl.cacheTokenStream(ppHandler, toCache);
    }

    public static APTTokenStreamCache extractTokenStream(PreprocHandler ppHandler) {
      return ClankDriverImpl.extractTokenStream(ppHandler);
    }

    public static boolean preprocess(APTFileBuffer buffer,
            PreprocHandler ppHandler, 
            ClankPreprocessorCallback callback, Interrupter interrupter) {
        return ClankDriverImpl.preprocessImpl(buffer, ppHandler, callback, interrupter);
    }
    
    public interface ClankPreprocessorCallback {
      boolean onEnter(ClankFileInfo include);
      /**
       * return true to continue or false to stop preprocessing and exit
       * @param include
       * @return
       */
      boolean onExit(ClankFileInfo include);
    }

    public interface ClankFileInfo {
      CharSequence getFilePath();
      PreprocHandler getHandler();
      APTTokenStream getTokenStream();
      int getIncludeIndex();
      ResolvedPath getResolvedPath();
      int[] getSkippedRanges();
    }
}
