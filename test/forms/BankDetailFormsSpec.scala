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

import models.{BicSwiftCode, Iban, SortCode}
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}

class BankDetailFormsSpec extends PlaySpec with OneServerPerSuite {

  val validUkData: Map[String, String] = Map("hasUKBankAccount" -> "true",
      "accountName" -> "Account Name",
      "accountNumber" -> "12345678",
      "sortCode.firstElement" -> "11",
      "sortCode.secondElement" -> "22",
      "sortCode.thirdElement" -> "33"
    )

  val validNonUkData: Map[String, String] = Map("hasUKBankAccount" -> "false",
    "bicSwiftCode" -> "123654789654",
    "iban" -> "GADGSDGADSGF"
  )


  val maxLengthUkData: Map[String, String] = Map("hasUKBankAccount" -> "true",
    "accountName" -> "Account Name"*20,
    "accountNumber" -> "12345678"*10,
    "sortCode.firstElement" -> "11"*10,
    "sortCode.secondElement" -> "22"*10,
    "sortCode.thirdElement" -> "33"*10
  )

  val maxLengthNonUkData: Map[String, String] = Map("hasUKBankAccount" -> "false",
    "accountName" -> "Account Name"*20,
    "bicSwiftCode" -> "123654789654"*20,
    "iban" -> "GADGSDGADSGF"*10
  )

  val invalidUkData: Map[String, String] = Map("hasUKBankAccount" -> "true",
    "accountName" -> "Account Name/ TTTT %£**",
    "accountNumber" -> "12345678**",
    "sortCode.firstElement" -> "11 **",
    "sortCode.secondElement" -> "22",
    "sortCode.thirdElement" -> "33"
  )

  val invalidNonUkData: Map[String, String] = Map("hasUKBankAccount" -> "false",
    "accountName" -> "Account Name***",
    "bicSwiftCode" -> "12364***",
    "iban" -> "%$***"
  )

  "bankDetailsForm" must {
    "pass through validation" when {
      "supplied with valid data for uk accounts" in {
        BankDetailForms.bankDetailsForm.bind(validUkData).fold(
          formWithErrors => {
            formWithErrors.errors.isEmpty must be(true)

          },
          success => {
            success.accountName must be(Some("Account Name"))
            success.accountNumber must be(Some("12345678"))
            success.sortCode must be(Some(SortCode("11","22","33")))
          }
        )
      }

      "supplied with valid data for non uk accounts" in {
        BankDetailForms.bankDetailsForm.bind(validNonUkData).fold(
          formWithErrors => {
            formWithErrors.errors.isEmpty must be(true)

          },
          success => {
            success.bicSwiftCode must be(Some(BicSwiftCode("123654789654")))
            success.iban must be(Some(Iban("GADGSDGADSGF")))
          }
        )
      }
    }

    "fail validation" when {
      "supplied with uk account form data which exceeds max length" in {
        BankDetailForms.validateBankDetails(BankDetailForms.bankDetailsForm.bind(maxLengthUkData)).fold (
          formWithErrors => {
            formWithErrors.errors(0).message must be("The account holder name cannot be more than 60 characters")
            formWithErrors.errors(1).message must be("The account number cannot be more than 18 characters")
            formWithErrors.errors(2).message must be("You must enter a valid sort code")
            formWithErrors.errors.length must be(3)

          },
          success => {
            fail("Form should give an error")
          }
        )
      }

      "supplied with non uk form data which exceeds  max length" in {
        BankDetailForms.validateBankDetails(BankDetailForms.bankDetailsForm.bind(maxLengthNonUkData)).fold (
          formWithErrors => {
            formWithErrors.errors.head.message must be("The account holder name cannot be more than 60 characters")
            formWithErrors.errors(1).message must be("The IBAN cannot be more than 34 characters")
            formWithErrors.errors(2).message must be("The SWIFT code must be 8 or 11 characters")
            formWithErrors.errors.length must be(3)
          },
          success => {
            fail("Form should give an error")
          }
        )

      }

      "supplied with invalid uk account form data" in {
        BankDetailForms.validateBankDetails(BankDetailForms.bankDetailsForm.bind(invalidUkData)).fold (
          formWithErrors => {
            formWithErrors.errors.head.message must be("The account holder name must only include letters a to z, ampersands (&), apostrophes (‘), forward slashes (/)  and full stops (.)")
            formWithErrors.errors(1).message must be("You must enter a valid account number")
            formWithErrors.errors(2).message must be("You must enter a valid sort code")
            formWithErrors.errors.length must be(3)

          },
          success => {
            fail("Form should give an error")
          }
        )

      }

      "supplied with invalid non uk bank form data" in {
        BankDetailForms.validateBankDetails(BankDetailForms.bankDetailsForm.bind(invalidNonUkData)).fold (
          formWithErrors => {
            formWithErrors.errors.head.message must be("The account holder name must only include letters a to z, ampersands (&), apostrophes (‘), forward slashes (/)  and full stops (.)")
            formWithErrors.errors(1).message must be("The IBAN must only include letters a to z, numbers 0 to 9 and hyphens (-)")
            formWithErrors.errors(2).message must be("The SWIFT code must only include letters a to z, numbers 0 to 9 and hyphens (-)")
            formWithErrors.errors.length must be(3)
          },
          success => {
            fail("Form should give an error")
          }
        )
      }
    }
  }

}