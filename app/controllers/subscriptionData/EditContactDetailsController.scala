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

package controllers.subscriptionData

import config.FrontendDelegationConnector
import connectors.DataCacheConnector
import controllers.AtedBaseController
import controllers.auth.{AtedFrontendAuthHelpers, AtedRegime}
import forms.AtedForms._
import models.EditContactDetails
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import services.SubscriptionDataService
import uk.gov.hmrc.play.frontend.auth.DelegationAwareActions

import scala.concurrent.Future

trait EditContactDetailsController extends AtedBaseController with AtedFrontendAuthHelpers with DelegationAwareActions {

  def subscriptionDataService: SubscriptionDataService

  def edit = AuthAction(AtedRegime) {
    implicit atedContext =>
      for {
        contactAddress <- subscriptionDataService.getCorrespondenceAddress
      } yield {
        val populatedForm = contactAddress.fold(editContactDetailsForm) { x =>
          val editContactDetails = EditContactDetails(firstName = x.name1.getOrElse(""),
            lastName = x.name2.getOrElse(""),
            phoneNumber = x.contactDetails.fold("")(a => a.phoneNumber.getOrElse("")))
          editContactDetailsForm.fill(editContactDetails)
        }
        Ok(views.html.subcriptionData.editContactDetails(populatedForm, getBackLink))
      }
  }

  def submit = AuthAction(AtedRegime) {
    implicit atedContext =>
      editContactDetailsForm.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(views.html.subcriptionData.editContactDetails(formWithErrors, getBackLink))),
        editedClientData => {
          for {
            editedContact <- subscriptionDataService.editContactDetails(editedClientData)
          } yield {
            editedContact match {
              case Some(x) => Redirect(controllers.subscriptionData.routes.CompanyDetailsController.view())
              case None =>
                val errorMsg = Messages("ated.contact-details.error.general.addressType")
                val errorForm = editContactDetailsForm.withError(key = "addressType", message = errorMsg).fill(editedClientData)
                BadRequest(views.html.subcriptionData.editContactDetails(errorForm, getBackLink))
            }
          }
        }
      )
  }

  private def getBackLink = {
    Some(controllers.subscriptionData.routes.CompanyDetailsController.view.url)
  }
}

object EditContactDetailsController extends EditContactDetailsController {
  val delegationConnector = FrontendDelegationConnector
  val dataCacheConnector = DataCacheConnector
  val subscriptionDataService = SubscriptionDataService
}
