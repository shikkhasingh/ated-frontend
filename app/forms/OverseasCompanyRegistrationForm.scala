/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms

import models.OverseasCompanyRegistration
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import uk.gov.hmrc.play.mappers.StopOnFirstFail.constraint

object OverseasCompanyRegistrationForm {

  private val length60 = 60
  private val length40 = 40
  private val idNumberRegex = "^[a-zA-Z0-9 '&\\-]{1,60}$"
  private val issuingInstitutionRegex = "^[a-zA-Z0-9 '&\\-\\/]{1,40}$"
  private val issuingCountryRegex = "(?!^GB$)^[A-Z]{2}$"

  val overseasCompanyRegistrationForm = Form(
    mapping(
      "businessUniqueId" ->  optional(text).verifying(StopOnFirstFail(
        constraint[Option[String]](Messages("ated.non-uk-reg.businessUniqueId.length", length60), x => x.isEmpty || (x.nonEmpty && x.get.length <= length60)),
        constraint[Option[String]](Messages("ated.non-uk-reg.businessUniqueId.invalid"), x => x.isEmpty
          || x.fold(false)(_.trim.matches(idNumberRegex))))
      ),
      "issuingInstitution" -> optional(text).verifying(StopOnFirstFail(
        constraint[Option[String]](Messages("ated.non-uk-reg.issuingInstitution.length", length40), x => x.isEmpty || (x.nonEmpty && x.get.length <= length40)),
        constraint[Option[String]](Messages("ated.non-uk-reg.issuingInstitution.invalid"), x => x.isEmpty
          || x.fold(false)(_.trim.matches(issuingInstitutionRegex))))
      ),
      "countryCode" -> optional(text).verifying(Messages("ated.non-uk-reg.countryCode.invalid"), x => x.isEmpty
        || x.fold(false)(_.trim.matches(issuingCountryRegex)))
    )(OverseasCompanyRegistration.apply)(OverseasCompanyRegistration.unapply)
  )
}
