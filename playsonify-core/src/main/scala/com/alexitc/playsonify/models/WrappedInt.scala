package com.alexitc.playsonify.models

trait WrappedInt extends Any {

  def int: Int

  override def toString: String = int.toString
}
