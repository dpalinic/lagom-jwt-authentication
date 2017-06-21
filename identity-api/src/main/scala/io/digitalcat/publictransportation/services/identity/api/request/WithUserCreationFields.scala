package io.digitalcat.publictransportation.services.identity.api.request

trait WithUserCreationFields {
  val firstName: String
  val lastName: String
  val email: String
  val username: String
  val password: String
}