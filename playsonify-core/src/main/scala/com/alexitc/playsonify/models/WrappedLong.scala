package com.alexitc.playsonify.models

trait WrappedLong extends Any {

  def long: Long

  override def toString: String = long.toString
}
