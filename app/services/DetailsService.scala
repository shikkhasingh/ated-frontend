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

package services

import connectors.{AgentClientMandateFrontendConnector, AtedConnector, DataCacheConnector}
import models._
import play.api.Logger
import play.api.mvc.Request
import play.mvc.Http.Status._
import utils.AtedConstants

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.{ BadRequestException, HeaderCarrier, InternalServerException }

trait DetailsService {

  def atedConnector: AtedConnector
  def mandateFrontendConnector: AgentClientMandateFrontendConnector
  def dataCacheConnector: DataCacheConnector

  val delegatedClientAtedRefNumber = "delegatedClientAtedRefNumber"


  def getDetails(identifier: String, identifierType: String)(implicit atedContext: AtedContext, hc: HeaderCarrier): Future[Option[EtmpRegistrationDetails]] = {
    atedConnector.getDetails(identifier = identifier, identifierType = identifierType) map {
      response =>
        response.status match {
          case OK => response.json.asOpt[EtmpRegistrationDetails]
          case NOT_FOUND => response.json.asOpt[EtmpRegistrationDetails]
          case BAD_REQUEST =>
            Logger.warn(s"[DetailsService][getDetails] status = ${response.status} - body = ${response.body}")
            throw new BadRequestException(s"[DetailsService][getDetails] Bad Data, " +
              s"status = ${response.status} - body = ${response.body}")
          case _ =>
            Logger.warn(s"[DetailsService][getDetails] status = ${response.status} - body = ${response.body}")
            throw new InternalServerException(s"[DetailsService][getDetails] Internal server error, " +
              s"status = ${response.status} - body = ${response.body}")
        }
    }
  }

  def updateOrganisationRegisteredDetails(oldDetails: EtmpRegistrationDetails, updatedData: RegisteredDetails)
                                         (implicit atedContext: AtedContext, hc: HeaderCarrier): Future[Option[UpdateRegistrationDetailsRequest]] = {

    if (oldDetails.isAnIndividual) {
      Logger.warn(s"[DetailsService] [updateRegisteredDetails] tried to update an individual")
      Future.successful(None)
    } else if (oldDetails.organisation.flatMap(_.isAGroup).isEmpty) {
      Logger.warn(s"[DetailsService] [updateRegisteredDetails] tried to update an organisation with no isAGroup setting")
      Future.successful(None)
    } else {
      val updateData = UpdateRegistrationDetailsRequest(isAnIndividual = oldDetails.isAnIndividual,
        individual = None,
        organisation = Some(UpdateOrganisation(updatedData.name)),
        address = updatedData.addressDetails,
        contactDetails = oldDetails.contactDetails,
        isAnAgent = oldDetails.isAnAgent,
        isAGroup = oldDetails.organisation.flatMap(_.isAGroup).getOrElse(false),
        identification = oldDetails.nonUKIdentification)

      atedConnector.updateRegistrationDetails(oldDetails.safeId, updateData).map {
        response =>
          response.status match {
            case OK => Some(updateData)
            case status =>
              Logger.warn(s"[DetailsService] [updateRegisteredDetails] [status] = ${status} && [response.body] = ${response.body}")
              None
          }
      }
    }
  }

  def updateOverseasCompanyRegistration(oldDetails: EtmpRegistrationDetails, updatedData: Option[Identification])
                                         (implicit atedContext: AtedContext, hc: HeaderCarrier): Future[Option[UpdateRegistrationDetailsRequest]] = {

    if (oldDetails.isAnIndividual) {
      Logger.warn(s"[DetailsService] [updateRegisteredDetails] tried to update an individual")
      Future.successful(None)
    } else if (oldDetails.organisation.flatMap(_.isAGroup).isEmpty) {
      Logger.warn(s"[DetailsService] [updateRegisteredDetails] tried to update an organisation with no isAGroup setting")
      Future.successful(None)
    } else {
      val updateData = UpdateRegistrationDetailsRequest(isAnIndividual = oldDetails.isAnIndividual,
        individual = None,
        organisation = Some(UpdateOrganisation(oldDetails.organisation.get.organisationName)),
        address = oldDetails.addressDetails,
        contactDetails = oldDetails.contactDetails,
        isAnAgent = oldDetails.isAnAgent,
        isAGroup = oldDetails.organisation.flatMap(_.isAGroup).getOrElse(false),
        identification = updatedData)

      atedConnector.updateRegistrationDetails(oldDetails.safeId, updateData).map {
        response =>
          response.status match {
            case OK => Some(updateData)
            case status =>
              Logger.warn(s"[DetailsService] [updateRegisteredDetails] [status] = ${status} && [response.body] = ${response.body}")
              None
          }
      }
    }
  }

  def getRegisteredDetailsFromSafeId(safeId: String)(implicit atedContext: AtedContext, hc: HeaderCarrier): Future[Option[EtmpRegistrationDetails]] = {
    for {
      returnedJsValue <- getDetails(safeId, AtedConstants.IdentifierSafeId)
    } yield {
      returnedJsValue
    }
  }

  def getClientMandateDetails(clientId: String, service: String)(implicit atedContext: AtedContext, request: Request[_], hc: HeaderCarrier): Future[Option[ClientMandateDetails]] = {
    if (atedContext.user.authContext.attorney.isDefined) {
      Future.successful(None)
    } else {
      mandateFrontendConnector.getClientDetails(clientId, service).map { response =>
        response.status match {
          case OK => response.json.asOpt[ClientMandateDetails]
          case _ => None
        }
      }
    }
  }

  def cacheClientReference(atedRef: String)(implicit atedContext: AtedContext, request: Request[_], hc: HeaderCarrier): Future[String] = {
      dataCacheConnector.saveFormData[String](delegatedClientAtedRefNumber, atedRef)
  }


}

object DetailsService extends DetailsService {
  val atedConnector = AtedConnector
  val mandateFrontendConnector = AgentClientMandateFrontendConnector
  val dataCacheConnector= DataCacheConnector
}
