/* PackageDoc.java -- Document a package
   Copyright (C) 1999 Free Software Foundation, Inc.

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
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

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


package com.sun.javadoc;

public interface PackageDoc extends Doc
{

/**
  * This method returns a list of all the classes and interfaces in
  * this package.  This list will included exceptions and errors.
  *
  * @return The list of classes and interfaces for this package.
  */
ClassDoc[]
allClasses();

/*************************************************************************/

/**
  * This method returns the list of ordinary classes in this package.  This
  * list will not include any interface, exceptions or errors.
  *
  * @return The list of ordinary classes in this package.
  */
ClassDoc[]
ordinaryClasses();

/*************************************************************************/

/**
  * This method returns the list of exceptions in this package.
  *
  * @return The list of exceptions in this package.
  */
ClassDoc[]
exceptions();

/*************************************************************************/

/**
  * This method returns the list of errors in this package.
  *
  * @return The list of errors in this package.
  */
ClassDoc[]
errors();

/*************************************************************************/

/**
  * This method returns the list of interfaces in this package.
  *
  * @return The list of interfaces in this package.
  */
ClassDoc[]
interfaces();

/*************************************************************************/

/**
  * This method returns a <code>ClassDoc</code> instance for the specified
  * class.
  *
  * @param name The name of the class to return.
  *
  * @return The requested <code>ClassDoc</code> or <code>null</code> if
  * this class not part of this javadoc run.
  */
ClassDoc
findClass(String cls);

} // interface PackageDoc
