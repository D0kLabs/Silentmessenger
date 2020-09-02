/* gnu.classpath.tools.doclets.xmldoclet.doctranslet.JarClassLoader
   Copyright (C) 2001 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package gnu.classpath.tools.doclets.xmldoclet.doctranslet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarClassLoader extends ClassLoader {

   private JarFile jarFile;

   public JarClassLoader(JarFile jarFile) {
      this.jarFile = jarFile;
   }

   public Class findClass(String name)
      throws ClassNotFoundException {

      byte[] b = loadClassData(name);
      return defineClass(name, b, 0, b.length);
   }

   private byte[] loadClassData(String className)
      throws ClassNotFoundException
   {
      String classFileName = className.replace('.', File.separatorChar) + ".class";

      try {
         JarEntry jarEntry = jarFile.getJarEntry(classFileName);
         if (null != jarEntry) {
            return readFromStream(jarFile.getInputStream(jarEntry),
                                  jarEntry.getSize());
         }
      }
      catch (IOException ignore_) {
      }
      throw new ClassNotFoundException(className);
   }

   private byte[] readFromStream(InputStream in, long size)
      throws IOException
   {
      byte[] result = new byte[(int)size];
      int nread = 0;
      int offset = 0;
      while (offset < size && (nread = in.read(result, offset, (int)(size - offset))) >= 0) {
         offset += nread;
      }
      in.close();
      return result;
   }
}