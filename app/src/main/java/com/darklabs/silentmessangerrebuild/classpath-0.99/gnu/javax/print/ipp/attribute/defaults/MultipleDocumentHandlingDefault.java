/* MultipleDocumentHandlingDefault.java --
   Copyright (C) 2006 Free Software Foundation, Inc.

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

package gnu.javax.print.ipp.attribute.defaults;

import javax.print.attribute.Attribute;
import javax.print.attribute.EnumSyntax;

import gnu.javax.print.ipp.IppUtilities;
import gnu.javax.print.ipp.attribute.DefaultValueAttribute;


/**
 * <code>MultipleDocumentHandlingDefault</code> provides the
 * default value for the MultipleDocumentHandling attribute.
 *
 * @author Wolfgang Baer (WBaer@gmx.de)
 */
public final class MultipleDocumentHandlingDefault extends EnumSyntax
  implements DefaultValueAttribute
{

  //a keyword based attribute in IPP - int values just starting at 0

  /**
   * Supports only multiple documents treated as a single document. This
   * applies to attributes which specify treatment of multiple document jobs.
   */
  public static final MultipleDocumentHandlingDefault SINGLE_DOCUMENT =
    new MultipleDocumentHandlingDefault(0);

  /** Supports multiple documents as uncollated copies */
  public static final MultipleDocumentHandlingDefault SEPARATE_DOCUMENTS_UNCOLLATED_COPIES =
    new MultipleDocumentHandlingDefault(1);

  /** Supports multiple documents as collated copies */
  public static final MultipleDocumentHandlingDefault SEPARATE_DOCUMENTS_COLLATED_COPIES =
    new MultipleDocumentHandlingDefault(2);

  /**
   * Supports multiple documents where every single document starts
   * with a new sheet.
   */
  public static final MultipleDocumentHandlingDefault SINGLE_DOCUMENT_NEW_SHEET =
    new MultipleDocumentHandlingDefault(3);

  private static final String[] stringTable = { "single-document",
                                                "separate-documents-uncollated-copies",
                                                "separate-documents-collated-copies",
                                                "single-document-new-sheet" };

  private static final MultipleDocumentHandlingDefault[] enumValueTable =
    { SINGLE_DOCUMENT, SEPARATE_DOCUMENTS_UNCOLLATED_COPIES,
      SEPARATE_DOCUMENTS_COLLATED_COPIES, SINGLE_DOCUMENT_NEW_SHEET};

  /**
   * Constructs a <code>MultipleDocumentHandlingDefault</code> object.
   *
   * @param value the enum value
   */
  protected MultipleDocumentHandlingDefault(int value)
  {
    super(value);
  }

  /**
   * Returns category of this class.
   *
   * @return The class <code>MultipleDocumentHandlingDefault</code> itself.
   */
  public Class<? extends Attribute> getCategory()
  {
    return MultipleDocumentHandlingDefault.class;
  }

  /**
   * Returns the name of this attribute.
   *
   * @return The name "multiple-document-handling-default".
   */
  public String getName()
  {
    return "multiple-document-handling-default";
  }

  /**
   * Returns a table with the enumeration values represented as strings
   * for this object.
   *
   * @return The enumeration values as strings.
   */
  protected String[] getStringTable()
  {
    return stringTable;
  }

  /**
   * Returns a table with the enumeration values for this object.
   *
   * @return The enumeration values.
   */
  protected EnumSyntax[] getEnumValueTable()
  {
    return enumValueTable;
  }

  /**
   * Returns the equally enum of the standard attribute class
   * of this DefaultValuesAttribute enum.
   *
   * @return The enum of the standard attribute class.
   */
  public Attribute getAssociatedAttribute()
  {
    return IppUtilities.getEnumAttribute("multiple-document-handling",
                                         new Integer(getValue()));
  }
}
