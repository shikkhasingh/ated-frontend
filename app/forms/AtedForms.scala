/*
 * Copyright 2019 HM Revenue & Customs
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

import models._
import org.joda.time.LocalDate
import play.api.Logger
import play.api.Play.current
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationResult}
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import uk.gov.hmrc.play.mappers.DateTuple._
import utils.AtedUtils
import utils.PeriodUtils._

import scala.annotation.tailrec


object AtedForms {

  val ZERO = 0
  val countryUK = "GB"
  val ELEVEN = 11
  val SIXTY = 60
  val numRegex = """[0-9]{8}"""
  val addressLineLength = 35
  val PostcodeLength = 10
  val countryLength = 2
  val emailLength = 241
  val lengthZero = 0
  val nameLength = 35
  val phoneLength = 24
  val faxLength = 24
  val businessNameLength = 105
  val telephoneRegex = """^[A-Z0-9)\/(\-*#]+$""".r
  val emailRegex =
    """^(?!\.)("([^"\r\\]|\\["\r\\])*"|([-a-zA-Z0-9!#$%&'*+/=?^_`{|}~]|(?<!\.)\.)*)
      |(?<!\.)@[a-zA-Z0-9][\w\.-]*[a-zA-Z0-9]\.[a-zA-Z][a-zA-Z\.]*[a-zA-Z]$""".r
  val PostCodeRegex = "^[A-Z]{1,2}[0-9][0-9A-Z]?\\s?[0-9][A-Z]{2}|BFPO\\s?[0-9]{1,10}".r
  val addressLineRegex = """^([A-Za-z0-9\s\&\-\,\@\#\.\'])*$""".r

  val registeredDetailsForm = Form(
    mapping(
      "isEditable" -> boolean,
      "name" -> text.
        verifying(Messages("ated.error.mandatory", Messages("ated.registered-details.business-name")), x => checkBlankFieldLength(x))
        .verifying(Messages("ated.registered-details.businessName.length", businessNameLength),
          x => x.isEmpty || (x.nonEmpty && x.length <= businessNameLength)),
      "addressDetails" -> mapping(
        "addressLine1" -> text.
          verifying(Messages("ated.error.mandatory", Messages("ated.address.line-1")), x => checkBlankFieldLength(x))
          .verifying(Messages("ated.error.address.line-1", Messages("ated.address.line-1"), addressLineLength),
            x => x.isEmpty || (x.nonEmpty && x.length <= addressLineLength)),
        "addressLine2" -> text.
          verifying(Messages("ated.error.mandatory", Messages("ated.address.line-2")), x => checkBlankFieldLength(x))
          .verifying(Messages("ated.error.address.line-2", Messages("ated.address.line-2"), addressLineLength),
            x => x.isEmpty || (x.nonEmpty && x.length <= addressLineLength)),
        "addressLine3" -> optional(text)
          .verifying(Messages("ated.error.address.line-3", Messages("ated.address.line-3"), addressLineLength),
            x => checkFieldLengthIfPopulated(x, addressLineLength)),
        "addressLine4" -> optional(text)
          .verifying(Messages("ated.error.address.line-4", Messages("ated.address.line-4"), addressLineLength),
            x => checkFieldLengthIfPopulated(x, addressLineLength)),
        "postalCode" -> optional(text)
          .verifying(Messages("ated.error.address.postalcode", Messages("ated.address.postcode.field"), PostcodeLength),
            x => checkFieldLengthIfPopulated(x, PostcodeLength))
          .verifying(Messages("ated.error.address.postalcode.format", Messages("ated.address.postcode.field"), PostcodeLength),
            x => validatePostCodeFormat(AtedUtils.formatPostCode(x))),
        "countryCode" -> text.
          verifying(Messages("ated.error.mandatory", Messages("ated.address.country")), x => x.length > lengthZero).
          verifying(Messages("ated.error.address.country", Messages("ated.address.country"), addressLineLength),
            x => x.isEmpty || (x.nonEmpty && x.length <= addressLineLength)).
          verifying(Messages("ated.error.address.country.non-uk"), x => !x.matches(countryUK))

      )(RegisteredAddressDetails.apply)(RegisteredAddressDetails.unapply)

    )(RegisteredDetails.apply)(RegisteredDetails.unapply)
  )

  val correspondenceAddressForm = Form(
    mapping(
      "addressType" -> text,
      "addressLine1" -> text.
        verifying(Messages("ated.error.mandatory", Messages("ated.address.line-1")), x => checkBlankFieldLength(x))
        .verifying(Messages("ated.error.address.line-1", Messages("ated.address.line-1"), addressLineLength),
          x => x.isEmpty || (x.nonEmpty && x.length <= addressLineLength)),
      "addressLine2" -> text.
        verifying(Messages("ated.error.mandatory", Messages("ated.address.line-2")), x => checkBlankFieldLength(x))
        .verifying(Messages("ated.error.address.line-2", Messages("ated.address.line-2"), addressLineLength),
          x => x.isEmpty || (x.nonEmpty && x.length <= addressLineLength)),
      "addressLine3" -> optional(text)
        .verifying(Messages("ated.error.address.line-3", Messages("ated.address.line-3"), addressLineLength),
          x => checkFieldLengthIfPopulated(x, addressLineLength)),
      "addressLine4" -> optional(text)
        .verifying(Messages("ated.error.address.line-4", Messages("ated.address.line-4"), addressLineLength),
          x => checkFieldLengthIfPopulated(x, addressLineLength)),
      "postalCode" -> optional(text)
        .verifying(Messages("ated.error.address.postalcode", Messages("ated.address.postcode.field"), PostcodeLength),
          x => checkFieldLengthIfPopulated(AtedUtils.formatPostCode(x), PostcodeLength))
        .verifying(Messages("ated.error.address.postalcode.format", Messages("ated.address.postcode.field"), PostcodeLength),
          x => validatePostCodeFormat(AtedUtils.formatPostCode(x))),
      "countryCode" -> text.
        verifying(Messages("ated.error.mandatory", Messages("ated.address.country")), x => x.length > lengthZero)
        .verifying(Messages("ated.error.address.country", Messages("ated.address.country"), addressLineLength),
          x => x.isEmpty || (x.nonEmpty && x.length <= addressLineLength))

    )(AddressDetails.apply)(AddressDetails.unapply)
  )


  val editContactDetailsForm = Form(
    mapping(
      "firstName" -> text
        .verifying(Messages("ated.contact-details-first-name.error"), x => checkBlankFieldLength(x))
        .verifying(Messages("ated.contact-details-first-name.length"), x => x.isEmpty || (x.nonEmpty && x.length <= nameLength)),
      "lastName" -> text
        .verifying(Messages("ated.contact-details-last-name.error"), x => checkBlankFieldLength(x))
        .verifying(Messages("ated.contact-details-last-name.length"), x => x.isEmpty || (x.nonEmpty && x.length <= nameLength)),
      "phoneNumber" -> text
        .verifying(Messages("ated.contact-details-phoneNumber.error"), x => checkBlankFieldLength(x))
        .verifying(Messages("ated.contact-phoneNumber.length", phoneLength), x => x.isEmpty || (x.nonEmpty && x.length <= phoneLength))
        .verifying(Messages("ated.contact-phoneNumber.invalidText"), x => {
          val p = telephoneRegex.findFirstMatchIn(x.replaceAll(" ", "")).exists(_ => true)
          val y = x.length == lengthZero
          val z = x.length > phoneLength
          p || z || y
        })
    )(EditContactDetails.apply)(EditContactDetails.unapply)
  )


  val editContactDetailsEmailForm = Form(
    mapping(
      "emailAddress" -> text,
      "emailConsent" -> boolean
    )(EditContactDetailsEmail.apply)(EditContactDetailsEmail.unapply)
  )

  def validateEmail(f: Form[EditContactDetailsEmail]): Form[EditContactDetailsEmail] = {
    if (!f.hasErrors) {
      val emailConsent = f.data.get("emailConsent")
      val formErrors = emailConsent match {
        case Some("true") => {
          val email = f.data.get("emailAddress").getOrElse("")
          if (email.isEmpty || (email.nonEmpty && email.trim.length == lengthZero)) {
            Seq(FormError("emailAddress", Messages("ated.contact-details-emailAddress.error")))
          } else if (email.length > emailLength) {
            Seq(FormError("emailAddress", Messages("ated.contact-email.length")))
          } else {
            val x = emailRegex.findFirstMatchIn(email).exists(_ => true)
            val y = email.length == lengthZero
            if (x || y) {
              Nil
            } else {
              Seq(FormError("emailAddress", Messages("ated.contact-email.error")))
            }
          }
        }
        case _ => Nil
      }
      addErrorsToForm(f, formErrors)
    } else f
  }

  val editReliefForm = Form(
    mapping(
      "changeRelief" -> optional(text).verifying(Messages("ated.change-relief-return.error.empty"), returnType => returnType.isDefined)
    )(EditRelief.apply)(EditRelief.unapply)
  )

  def checkFieldLengthIfPopulated(optionValue: Option[String], fieldLength: Int): Boolean = {
    optionValue match {
      case Some(value) => value.isEmpty || (value.nonEmpty && value.trim.length <= fieldLength)
      case None => true
    }
  }


  def checkBlankFieldLength(value: String): Boolean = {
  value.trim.length > 0
  }


  def validatePostCodeFormat(optionValue: Option[String]): Boolean = {
    optionValue match {
      case Some(value) =>
        if (PostCodeRegex.findFirstMatchIn(value).exists(_ => true)) true
        else false
      case None => true
    }
  }

  def validateAddressLine(optionValue: Option[String]): Boolean = {
    optionValue match {
      case Some(value) =>
        if (addressLineRegex.findFirstMatchIn(value).exists(_ => true)) true
        else false
      case None => true
    }
  }

  def verifyUKPostCode(correspondenceForm: Form[AddressDetails]): Form[AddressDetails] = {
    if (!correspondenceForm.hasErrors) {
      val country = correspondenceForm.data.get("countryCode")
      val formErrors = country match {
        case Some("GB") =>
          val postCode = correspondenceForm.data.get("postalCode")
          postCode match {
            case Some(x) if (x == "") => Seq(FormError("postalCode", Messages("ated.correspondence-address.error.uk.postalCode")))
            case Some(x) => Nil
            case _ => Seq(FormError("postalCode", Messages("ated.correspondence-address.error.uk.postalCode")))
          }
        case _ => Nil
      }
      addErrorsToForm(correspondenceForm, formErrors)
    } else correspondenceForm

  }


  private def validataTA(ta: TaxAvoidance): Boolean = {
    ta.employeeOccupationScheme.isDefined ||
      ta.farmHousesScheme.isDefined ||
      ta.lendingScheme.isDefined ||
      ta.openToPublicScheme.isDefined ||
      ta.propertyDeveloperScheme.isDefined ||
      ta.propertyTradingScheme.isDefined ||
      ta.rentalBusinessScheme.isDefined ||
      ta.socialHousingScheme.isDefined
  }

  val selectPeriodForm = Form(mapping(
    "period" -> optional(text).verifying(Messages("ated.summary-return.return-type.error"), selectPeriod => selectPeriod.isDefined)
  )(SelectPeriod.apply)(SelectPeriod.unapply))

  val returnTypeForm = Form(mapping(
    "returnType" -> optional(text).verifying(Messages("ated.summary-return.return-type.error"), returnType => returnType.isDefined)
  )(ReturnType.apply)(ReturnType.unapply))

  val editLiabilityReturnTypeForm = Form(mapping(
    "editLiabilityType" -> optional(text).verifying(Messages("ated.edit-liability.edit-return-type.error"), editReturnType => editReturnType.isDefined)
  )(EditLiabilityReturnType.apply)(EditLiabilityReturnType.unapply))

  val disposalDateConstraint: Constraint[DisposeLiability] = Constraint("dateOfDisposal"){
    model => validateDisposedProperty(model.periodKey, model.dateOfDisposal)
  }

  val disposeLiabilityForm = {
    Form(
      mapping(
        "dateOfDisposal" -> dateTuple,
        "periodKey" -> number
      )(DisposeLiability.apply)(DisposeLiability.unapply)
        .verifying(disposalDateConstraint)
    )
  }

  def validateDisposedProperty(periodKey: Int, disposalDate: Option[LocalDate]): ValidationResult = {

    if (disposalDate.isEmpty) {
      Invalid(Messages("ated.dispose-property.dateOfDisposal.error.empty"), "dateOfDisposal")
    } else if (isPeriodTooEarly(periodKey, disposalDate)) {
      Invalid(Messages("ated.dispose-property.period.dateOfDisposal.date-before-period"), "dateOfDisposal")
    } else if (isPeriodTooLate(periodKey, disposalDate)) {
      Invalid(Messages("ated.dispose-property.period.dateOfDisposal.date-after-period"), "dateOfDisposal")
    } else if (isAfterPresentDay(disposalDate)) {
      Invalid(Messages("ated.dispose-property.period.dateOfDisposal.date-after-today"), "dateOfDisposal")
    }
    else {
      Valid
    }
  }


  private def addErrorsToForm[A](form: Form[A], formErrors: Seq[FormError]): Form[A] = {
    @tailrec
    def y(f: Form[A], fe: Seq[FormError]): Form[A] = {
      if (fe.isEmpty) f
      else y(f.withError(fe.head), fe.tail)
    }

    y(form, formErrors)
  }

  case class YesNoQuestion(yesNo: Option[Boolean] = None)

  object YesNoQuestion {
    implicit val formats = Json.format[YesNoQuestion]
  }

  class YesNoQuestionForm(_param: String) {

    private val param:String = Messages(_param)

    val yesNoQuestionForm =
      Form(
        mapping(
          "yesNo" -> optional(boolean).verifying(Messages("yes-no.error.mandatory", param), x => x.isDefined)
        )(YesNoQuestion.apply)(YesNoQuestion.unapply)
      )
  }

}
