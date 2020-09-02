/* Doc.java -- Model of an item to document.
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

/**
  * This interface is the super-interface of all items that can have
  * Javadoc comments associated with them.
  */
public interface Doc extends java.io.Serializable, Comparable
{

/**
  * This method returns the text of the comment for this item, with all
  * tags stripped.
  *
  * @return The comment text for this item.
  */
String
commentText();

/*************************************************************************/

/**
  * This method returns an array of all the tags in this item.
  *
  * @return An array of tags for this item.
  */
Tag[]
tags();

/*************************************************************************/

/**
  * This method returns an array of all the tags of the specified type
  * in this item.
  *
  * @param tagtype The name of the tag type to return.
  *
  * @return A list of all tags of the specified type.
  */
Tag[]
tags(String tagtype);

/*************************************************************************/

/**
  * This method returns an array of all tags of the "@see" type.
  *
  * @return An array of tags of the "@see" type
  */
SeeTag[]
seeTags();

/*************************************************************************/

/**
  * This method returns the comment text as an array of tags.  This will
  * include any inline tags, but no regular tags.  Regular text is returned
  * as a type of <code>Text</code>.  Inline "@see" tags are returned as
  * type <code>SeeTag</code>.
  *
  * @return The comment text as tags.
  */
Tag[]
inlineTags();

/*************************************************************************/

/**
  * This method returns the first sentence of the comment text as an array
  * of tags.  This will include any inline tags, but no regular tags.
  * Regular text is returned as a type of <code>Text</code>. Inline "@see"
  * tags are returned as type <code>SeeTag</code>.
  *
  * @return An array of tags representing the first sentence of the comment
  * text.
  */
Tag[]
firstSentenceTags();

/*************************************************************************/

/**
  * This method returns the text of the comment in an unprocessed format.
  * Any Javadoc tags will remain as written in the text.
  *
  * @return The unprocessed comment text.
  */
String
getRawCommentText();

/*************************************************************************/

/**
  * This method sets the unprocessed comment text for this item.
  *
  * @param rawtext The unprocessed comment text for this itme.
  */
void
setRawCommentText(String rawtext);

/*************************************************************************/

/**
  * This method returns the name of this item.
  *
  * @return The name of this item.
  */
String
name();

/*************************************************************************/

/**
  * This method tests whether or not this item is a field.
  *
  * @return <code>true</code> if this item is a field, <code>false</code>
  * otherwise.
  */
boolean
isField();

/*************************************************************************/

/**
  * This method tests whether or not this item is a method.
  *
  * @return <code>true</code> if this item is a method, <code>false</code>
  * otherwise.
  */
boolean
isMethod();

/*************************************************************************/

/**
  * This method tests whether or not this item is a constructor.
  *
  * @return <code>true</code> if this item is a constructor,
  * <code>false</code> otherwise.
  */
boolean
isConstructor();

/*************************************************************************/

/**
  * This method tests whether or not this item is an interface.
  *
  * @return <code>true</code> if this item is an interface,
  * <code>false</code> otherwise.
  */
boolean
isInterface();

/*************************************************************************/

/**
  * This method tests whether or not this item is an exception.
  *
  * @return <code>true</code> if this item is an exception,
  * <code>false</code> otherwise.
  */
boolean
isException();

/*************************************************************************/

/**
  * This method tests whether or not this item is an error.
  *
  * @return <code>true</code> if this item is an error,
  * <code>false</code> otherwise.
  */
boolean
isError();

/*************************************************************************/

/**
  * This method tests whether or not this item is a class.  Interfaces
  * do not count as classes.
  *
  * @return <code>true</code> if this item is a class,
  * <code>false</code> otherwise.
  */
boolean
isClass();

/*************************************************************************/

/**
  * This method tests whether or not this item is an ordinary class.  An
  * ordinary class is a class that is not an exception or an error.
  * Interfaces also do not count because they are not considered classes at
  * all.
  *
  * @return <code>true</code> if this item is an ordinary class,
  * <code>false</code> otherwise.
  */
boolean
isOrdinaryClass();

/*************************************************************************/

/**
  * This method tests whether or not this item is part of the active set,
  * whatever that is.
  *
  * @return <code>true</code> if this item is part of the active set,
  * <code>false</code> otherwise.
  */
boolean
isIncluded();

/*************************************************************************/

/**
  * This method returns the location of the item within the Java
  * source code.
  *
  * @return an object describing the file, line and column where this
  * item is defined.
  */
SourcePosition
position();

} // interface Doc
