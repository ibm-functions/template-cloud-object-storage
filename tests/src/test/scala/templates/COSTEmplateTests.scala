/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package packages


import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.junit.JUnitRunner
import common.{TestHelpers, Wsk, WskProps, WskTestHelpers}
import java.io._

// import spray.json.DefaultJsonProtocol.StringJsonFormat
// import spray.json.pimpAny

import common.TestUtils.RunResult
import common.rest.WskRest
import common.rest.RestResult
import com.jayway.restassured.RestAssured
import com.jayway.restassured.config.SSLConfig
// import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class CloudantBlueTests extends TestHelpers
    with WskTestHelpers
    with BeforeAndAfterAll {

    implicit val wskprops = WskProps()
    val wsk = new Wsk()
    val wskRest: common.rest.WskRest = new WskRest
    val allowedActionDuration = 120 seconds

    // statuses for deployWeb
    val successStatus =
      """"status":"success""""

    val deployTestRepo = "https://github.com/beemarie/template-cos"
    val packageTestRepo = "https://github.com/ibm-functions/package-cloud-object-storage"
    val action1 = "initial-html"
    val action2 = "handle-form"
    val action3 = "form-completed"
    val sequenceName = "upload-a-file-sequence"
    val packageName = "myPackage"
    val binding = "cloud-object-storage"
    val objectWriteAction = binding + "/" + "object-write"
    val objectReadAction = binding + "/" + "object-read"
    val objectDeleteAction = binding + "/" + "object-delete"
    val deployAction = "/whisk.system/deployWeb/wskdeploy"
    val deployActionURL = s"https://${wskprops.apihost}/api/v1/web${deployAction}.http"
    val namespace = wsk.namespace.whois()


    //set parameters for deploy tests
    val nodejs8RuntimePath = "runtimes/nodejs"
    val nodejs8folder = "../runtimes/nodejs/actions"
    val nodejs8kind = "nodejs:8"


    behavior of "Cloud Object Storage Template"

    // test to create the nodejs 8 cloud object storage template from github url.  Will use preinstalled folder.
    it should "create the nodejs 8 cloud object storage template from github url" in withAssetCleaner(wskprops) { (wp, assetHelper) =>

      // create unique asset names
      val timestamp: String = System.currentTimeMillis.toString
      val nodejs8Package = packageName + timestamp
      val nodejs8Action1 = nodejs8Package + "/" + action1
      val nodejs8Action2 = nodejs8Package + "/" + action2
      val nodejs8Action3 = nodejs8Package + "/" + action3
      val nodejs8Sequence = nodejs8Package + "/" + sequenceName


      // post call to deploy COS package
      makePostCallWithExpectedResult(JsObject(
        "gitUrl" -> JsString(packageTestRepo),
        "manifestPath" -> JsString(nodejs8RuntimePath),
        "wskApiHost" -> JsString(wskprops.apihost),
        "wskAuth" -> JsString(wskprops.authKey)
      ), successStatus, 200)

      // post call to deploy package to test deploy of manifest
      makePostCallWithExpectedResult(JsObject(
        "gitUrl" -> JsString(deployTestRepo),
        "manifestPath" -> JsString(nodejs8RuntimePath),
        "envData" -> JsObject(
          "PACKAGE_NAME" -> JsString(nodejs8Package)
        ),
        "wskApiHost" -> JsString(wskprops.apihost),
        "wskAuth" -> JsString(wskprops.authKey)
      ), successStatus, 200)

      // check that sequence was created and contains correct actions
      val compValue = JsArray(
        JsString("/" + namespace + "/" + nodejs8Action2),
        JsString("/" + namespace + "/" + objectWriteAction),
        JsString("/" + namespace + "/" + nodejs8Action3)
      )
      val sequence = wsk.action.get(nodejs8Sequence)
      verifyActionSequence(sequence, nodejs8Sequence, compValue, JsString("sequence"))

      // verify action exists as correct kind
      val testaction1 = wsk.action.get(nodejs8Action1)
      verifyAction(testaction1, nodejs8Action1, JsString(nodejs8kind))

      // verify action exists as correct kind
      val testaction2 = wsk.action.get(nodejs8Action2)
      verifyAction(testaction2, nodejs8Action2, JsString(nodejs8kind))

      // verify action exists as correct kind
      val testaction3 = wsk.action.get(nodejs8Action3)
      verifyAction(testaction3, nodejs8Action3, JsString(nodejs8kind))

      wsk.action.delete(nodejs8Action1)
      wsk.action.delete(nodejs8Action2)
      wsk.action.delete(nodejs8Action3)
      wsk.action.delete(nodejs8Sequence)
      wsk.pkg.delete(nodejs8Package)
      wsk.action.delete(objectWriteAction)
      wsk.action.delete(objectReadAction)
      wsk.action.delete(objectDeleteAction)
      wsk.pkg.delete(binding)
    }

    /**
      * Test the nodejs 8 "cloudant trigger" template
      */
    it should "invoke nodejs 8 initialHtml.js and get the result" in withAssetCleaner(wskprops) { (wp, assetHelper) =>
      val timestamp: String = System.currentTimeMillis.toString
      val name = "initialHtml" + timestamp
      val file = Some(new File(nodejs8folder, "initialHtml.js").toString())
      assetHelper.withCleaner(wsk.action, name) { (action, _) =>
        action.create(name, file, kind = Some(nodejs8kind))
      }

      withActivation(wsk.activation, wsk.action.invoke(name)) {
        _.response.result.get.toString should include("COS Bucket Name")
      }
    }

    it should "invoke nodejs 8 handleForm.js with no input and get the result" in withAssetCleaner(wskprops) { (wp, assetHelper) =>
      val timestamp: String = System.currentTimeMillis.toString
      val name = "handleForm" + timestamp
      val file = Some(new File(nodejs8folder, "handleForm.js").toString())


      assetHelper.withCleaner(wsk.action, name) { (action, _) =>
        action.create(name, file, kind = Some(nodejs8kind))
      }

      withActivation(wsk.activation, wsk.action.invoke(name)) {
        _.response.result.get.toString should include("Cannot read property 'content-type' of undefined")
      }
    }

    it should "invoke nodejs 8 formCompleteHtml.js with no input and get the result" in withAssetCleaner(wskprops) { (wp, assetHelper) =>
      val timestamp: String = System.currentTimeMillis.toString
      val name = "formCompleted" + timestamp
      val file = Some(new File(nodejs8folder, "formCompleteHtml.js").toString())
      assetHelper.withCleaner(wsk.action, name) { (action, _) =>
        action.create(name, file, kind = Some(nodejs8kind))
      }

      withActivation(wsk.activation, wsk.action.invoke(name)) {
        _.response.result.get.toString should include("Thank you for your input")
      }
    }

    private def verifyRule(ruleListResult: RunResult, ruleName: String, triggerName: String, actionName: String) = {
      val actionNameWithNoPackage = actionName.split("/").last
      val rule = wskRest.rule.get(ruleName)
      rule.getField("name") shouldBe ruleName
      RestResult.getField(rule.getFieldJsObject("trigger"), "name") shouldBe triggerName
      RestResult.getField(rule.getFieldJsObject("action"), "name") shouldBe actionNameWithNoPackage
    }

    private def verifyTriggerList(triggerListResult: RunResult, triggerName: String) = {
      val triggerList = triggerListResult.stdout
      val listOutput = triggerList.lines
      listOutput.find(_.contains(triggerName)).get should include(triggerName)
    }

    private def verifyAction(action: RunResult, name: String, kindValue: JsString): Unit = {
      val stdout = action.stdout
      assert(stdout.startsWith(s"ok: got action $name\n"))
      wsk.parseJsonString(stdout).fields("exec").asJsObject.fields("kind") shouldBe kindValue
    }

    def verifyActionSequence(action: RunResult, name: String, compValue: JsArray, kindValue: JsString): Unit = {
      val stdout = action.stdout
      assert(stdout.startsWith(s"ok: got action $name\n"))
      wsk.parseJsonString(stdout).fields("exec").asJsObject.fields("components") shouldBe compValue
      wsk.parseJsonString(stdout).fields("exec").asJsObject.fields("kind") shouldBe kindValue
    }

    private def makePostCallWithExpectedResult(params: JsObject, expectedResult: String, expectedCode: Int) = {
      val response = RestAssured.given()
        .contentType("application/json\r\n")
        .config(RestAssured.config().sslConfig(new SSLConfig().relaxedHTTPSValidation()))
        .body(params.toString())
        .post(deployActionURL)
      assert(response.statusCode() == expectedCode)
      response.body.asString should include(expectedResult)
      response.body.asString.parseJson.asJsObject.getFields("activationId") should have length 1
    }

}
