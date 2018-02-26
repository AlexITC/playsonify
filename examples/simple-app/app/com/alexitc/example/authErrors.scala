package com.alexitc.example

import com.alexitc.playsonify.models.AuthenticationError

sealed trait SimpleAuthError
case object InvalidAuthorizationHeaderError extends SimpleAuthError with AuthenticationError

