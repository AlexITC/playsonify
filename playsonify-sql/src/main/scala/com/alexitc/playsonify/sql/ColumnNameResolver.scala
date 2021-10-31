package com.alexitc.playsonify.sql

import scala.annotation.implicitNotFound

@implicitNotFound(
  "No column name resolver found for type ${A}. Try to implement an implicit ColumnNameResolver for this type."
)
trait ColumnNameResolver[A] {

  /** Maps a field to the column name on the sql schema.
    */
  def getColumnName(field: A): String

  /** This is used to break ties while sorting by non-unique fields,
    */
  def getUniqueColumnName: String
}
