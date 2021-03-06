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

package models

import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.twirl.api.Html


case class EditRelief(changeRelief: Option[String] = None)

object EditRelief {
  implicit val formats = Json.format[EditRelief]
}

case class ReturnType(returnType: Option[String] = None)// CR = chargeable-return && RR = relief-return

object ReturnType {
  implicit val formats = Json.format[ReturnType]
}

case class SelectPeriod(period: Option[String] = None)
object SelectPeriod {
  implicit val formats = Json.format[SelectPeriod]
}

case class EditLiabilityReturnType(editLiabilityType: Option[String] = None)// ER = edit-return, DP = dispose-property && MP = move-property

object EditLiabilityReturnType {
  implicit val formats = Json.format[EditLiabilityReturnType]
}

case class DisposeLiability(dateOfDisposal: Option[LocalDate] = None, periodKey: Int)

object DisposeLiability {
  implicit val formats = Json.format[DisposeLiability]
}

case class DisposeCalculated(liabilityAmount: BigDecimal, amountDueOrRefund: BigDecimal)

object DisposeCalculated {
  implicit val formats = Json.format[DisposeCalculated]
}

case class DisposeLiabilityReturn(id: String,
                                  formBundleReturn: FormBundleReturn,
                                  disposeLiability: Option[DisposeLiability] = None,
                                  calculated: Option[DisposeCalculated] = None,
                                  bankDetails: Option[BankDetailsModel] = None)

object DisposeLiabilityReturn {
  implicit val formats = Json.format[DisposeLiabilityReturn]
}

case class CyaRow(
                   cyaQuestion: String,
                   cyaQuestionId: String,
                   cyaAnswer: Html,
                   cyaAnswerId: String,
                   cyaChange: Option[Html]
                 )
