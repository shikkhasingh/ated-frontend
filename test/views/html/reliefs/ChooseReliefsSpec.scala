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

package views.html.reliefs

import forms.ReliefForms
import models.Reliefs
import org.joda.time.LocalDate
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.twirl.api.Html
import utils.viewHelpers.AtedViewSpec

class ChooseReliefsSpec extends AtedViewSpec {

  val periodKey = 2017
  val periodStartDate = new LocalDate()

  "choose relief view" must {
    behave like pageWithTitle(messages("ated.choose-reliefs.title"))
    behave like pageWithHeader(messages("ated.choose-reliefs.header"))
    behave like pageWithPreHeading(messages("ated.choose-reliefs.subheader"))
    behave like pageWithBackLink
    behave like pageWithContinueButtonForm(s"/ated/reliefs/$periodKey/send")
  }

  "choose relief page" must {
    "display rental business checkbox" in {
      doc must haveElementWithId("rentalBusiness")
    }

    "display rental business start date" in {
      doc must haveElementWithId("rentalBusinessDate")
    }

    "display open to the public" in {
      doc must haveElementWithId("openToPublic")
    }

    "display open to the public start date" in {
      doc must haveElementWithId("openToPublicDate")
    }

    "display property developers" in {
      doc must haveElementWithId("propertyDeveloper")
    }

    "display property developer start date" in {
      doc must haveElementWithId("propertyDeveloperDate")
    }

    "display property trading" in {
      doc must haveElementWithId("propertyTrading")
    }

    "display property trading start date" in {
      doc must haveElementWithId("propertyTradingDate")
    }

    "display Lending" in {
      doc must haveElementWithId("lending")
    }

    "display Lending start date" in {
      doc must haveElementWithId("lendingDate")
    }

    "display employee occupation" in {
      doc must haveElementWithId("employeeOccupation")
    }

    "display employee occupation start date" in {
      doc must haveElementWithId("employeeOccupationDate")
    }

    "display farm houses" in {
      doc must haveElementWithId("farmHouses")
    }

    "display farm houses start date" in {
      doc must haveElementWithId("farmHousesDate")
    }

    "display social housing" in {
      doc must haveElementWithId("socialHousing")
    }

    "display social housing start date" in {
      doc must haveElementWithId("socialHousingDate")
    }

    "display equity release scheme" in {
      doc must haveElementWithId("equityRelease")
    }

    "display equity release scheme start date" in {
      doc must haveElementWithId("equityReleaseDate")
    }


    "display error" when {
      "rental business is selected but no date is populated" in {
        haveChooseReliefFormError("rentalBusiness", "rental businesses")
      }

      "rental business start date is invalid" in {
        haveChooseReliefStartDateFormError("rentalBusiness")
      }

      "open to public is selected but no date is populated" in {
        haveChooseReliefFormError("openToPublic", "open to the public")
      }

      "open to public start date is invalid" in {
        haveChooseReliefStartDateFormError("openToPublic")
      }

      "property developer is selected but no date is populated" in {
        haveChooseReliefFormError("propertyDeveloper", "property developers")
      }

      "property developer start date is invalid" in {
        haveChooseReliefStartDateFormError("propertyDeveloper")
      }

      "property trading is selected but no date is populated" in {
        haveChooseReliefFormError("propertyTrading", "property trading")
      }

      "property trading start date is invalid" in {
        haveChooseReliefStartDateFormError("propertyTrading")
      }

      "lending is selected but no date is populated" in {
        haveChooseReliefFormError("lending", "lending")
      }

      "lending start date is invalid" in {
        haveChooseReliefStartDateFormError("lending")
      }

      "employee occupation is selected but no date is populated" in {
        haveChooseReliefFormError("employeeOccupation", "employee occupation")
      }

      "employee occupation start date is invalid" in {
        haveChooseReliefStartDateFormError("employeeOccupation")
      }

      "farm houses is selected but no date is populated" in {
        haveChooseReliefFormError("farmHouses", "farmhouses")
      }

      "farm houses start date is invalid" in {
        haveChooseReliefStartDateFormError("farmHouses")
      }

      "social housing is selected but no date is populated" in {
        haveChooseReliefFormError("socialHousing", "social housing")
      }

      "social housing start date is invalid" in {
        haveChooseReliefStartDateFormError("socialHousing")
      }

      "equity release is selected but no date is populated" in {
        haveChooseReliefFormError("equityRelease", "equity release scheme")
      }

      "equity release start date is invalid" in {
        haveChooseReliefStartDateFormError("equityRelease")
      }
    }
  }

  def haveChooseReliefFormError(field: String, partialFieldError: String): Unit = {
    val fieldStartDate = field + "Date"
    val formWithErrors: Form[Reliefs] = ReliefForms.reliefsForm.bind(Json.obj("periodKey" -> periodKey, field -> true))

    def view: Html = views.html.reliefs.chooseReliefs(periodKey, formWithErrors, periodStartDate, Some("backLink"))

    val errorDoc = doc(view)
    errorDoc must haveErrorSummary(Messages(s"ated.choose-reliefs.error.general.$fieldStartDate"))
    errorDoc must haveErrorNotification(s"You must enter a $partialFieldError date")
  }

  def haveChooseReliefStartDateFormError(field: String): Unit = {
    val fieldStartDate = field + "Date"
    val formWithErrors: Form[Reliefs] = ReliefForms.reliefsForm.bind(Json.obj("periodKey" -> periodKey, field -> true,
      fieldStartDate -> Map("day" -> "1")))

    def view: Html = views.html.reliefs.chooseReliefs(periodKey, formWithErrors, periodStartDate, Some("backLink"))

    val errorDoc = doc(view)
    errorDoc must haveErrorSummary(Messages(s"ated.choose-reliefs.error.general.$fieldStartDate"))
    errorDoc must haveErrorNotification("You must enter a valid date")
  }

  val reliefsForm: Form[Reliefs] = ReliefForms.reliefsForm

  override def view: Html = views.html.reliefs.chooseReliefs(periodKey, reliefsForm, periodStartDate, Some("backLink"))

}
