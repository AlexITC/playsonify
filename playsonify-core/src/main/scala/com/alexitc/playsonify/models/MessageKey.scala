package com.alexitc.playsonify.models

/** Represents a message key to use with play i18n.
  *
  * The use of this model allow us to differentiate the messages that are not adequate to display.
  */
case class MessageKey(string: String) extends AnyVal
