package com.alexitc.playsonify.akka

trait JsonControllerComponents[+A] {

  def i18nService: SingleLangService

  def publicErrorRenderer: PublicErrorRenderer

  def authenticatorService: AbstractAuthenticatorService[A]
}
