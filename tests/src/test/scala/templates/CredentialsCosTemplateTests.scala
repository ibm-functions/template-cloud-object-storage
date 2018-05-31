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


import com.google.gson.JsonObject
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.junit.JUnitRunner
import common._
import common.TestUtils.RunResult
import common.rest.WskRest
import com.jayway.restassured.RestAssured
import com.jayway.restassured.config.SSLConfig
import spray.json._

import scala.collection.JavaConversions._
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class CredentialsCosTemplateTests extends TestHelpers
  with WskTestHelpers
  with BeforeAndAfterAll {

  implicit val wskprops = WskProps()
  val wsk = new Wsk()
  val wskRest: common.rest.WskRest = new WskRest
  val allowedActionDuration = 120 seconds

  // statuses for deployWeb
  val successStatus =
    """"status":"success""""

  val deployTestRepo = "https://github.com/ibm-functions/template-cloud-object-storage"
  val packageTestRepo = "https://github.com/ibm-functions/package-cloud-object-storage"
  val htmlAction = "initial-html"
  val packageName = "myPackage"
  val binding = "cloud-object-storage"
  val objectWriteAction = s"${binding}/object-write"
  val objectReadAction = s"${binding}/object-read"
  val objectDeleteAction = s"${binding}/object-delete"
  val bucketCorsGetAction = s"${binding}/bucket-cors-get"
  val bucketCorsPutAction = s"${binding}/bucket-cors-put"
  val bucketCorsDeleteAction = s"${binding}/bucket-cors-delete"
  val getSignedUrlAction = s"${binding}/client-get-signed-url"
  val deployAction = "/whisk.system/deployWeb/wskdeploy"
  val deployActionURL = s"https://${wskprops.apihost}/api/v1/web${deployAction}.http"

  //set parameters for deploy tests
  val nodejs8RuntimePath = "runtimes/nodejs"
  val nodejs8folder = "../runtimes/nodejs/actions"
  val nodejs8kind = "nodejs:8"

  val creds = TestUtils.getCredentials("cloud-object-storage")
  val __bx_creds = parseCredentials(creds)
  val testBucket: String = "ibm-functions-cos-package-testing"

  behavior of "Cloud Object Storage Template"

    // test to create the nodejs 8 cloud object storage template from github url.  Will use preinstalled folder.
    it should "create the nodejs 8 cloud object storage template from github url" in withAssetCleaner(wskprops) { (wp, assetHelper) =>

      // create unique asset names
      val timestamp: String = System.currentTimeMillis.toString
      val nodejs8Package = packageName + timestamp
      val nodejs8htmlAction = nodejs8Package + "/" + htmlAction

      // post call to deploy COS package
      deployCOSPackage()
      // post call to deploy package to test deploy of manifest
      deployTemplate(JsString(nodejs8Package))

      // verify action exists as correct kind
      val testhtmlAction = wsk.action.get(nodejs8htmlAction)
      verifyAction(testhtmlAction, nodejs8htmlAction, JsString(nodejs8kind))

      // update COS and template packages to have required parameters
      wsk.pkg.create(nodejs8Package, Map(
        "bucket" -> JsString(testBucket),
        "ignore_certs" -> JsBoolean(true)
      ), update = true, expectedExitCode = 0).stdout should include("updated package")

      wsk.pkg.create("cloud-object-storage", Map(
        "__bx_creds" -> __bx_creds
      ), update = true, expectedExitCode = 0).stdout should include("updated package")

      // Invoke web action through CURL so we can get the html
      val webActionUrl = s"https://${wskprops.apihost}/api/v1/web/${wsk.namespace.whois()}/$nodejs8htmlAction"
      val res = RestAssured.given()
        .config(RestAssured.config().sslConfig(new SSLConfig().relaxedHTTPSValidation()))
        .get(webActionUrl)

      res.statusCode shouldBe 200
      val htmlDom = res.body.asString
      val urlParsingPattern = """(?<=fetch\(\').*(?=\'\,)""".r
      val urls = urlParsingPattern.findAllIn(htmlDom).toArray

      val (strippedGetUrl, queryParamsGet) = parseUrl(urls(0))
      val (strippedPutUrl, queryParamsPut) = parseUrl(urls(1))

      // Run the PUT Action
      verifyOptions(strippedPutUrl, queryParamsPut, "PUT")
      val putResponse =  RestAssured.given()
        .urlEncodingEnabled(false)
        .queryParameters(queryParamsPut)
        .body(s"test text ${timestamp}")
        .put(strippedPutUrl)
      assert(putResponse.statusCode() == 200)

      // Run the GET Action
      verifyOptions(strippedGetUrl, queryParamsGet, "GET")
      val getResponse = RestAssured.given()
        .urlEncodingEnabled(false)
        .queryParameters(queryParamsGet)
        .get(strippedGetUrl)
      assert(getResponse.statusCode() == 200)
      getResponse.getBody.asString() should include(s"test text ${timestamp}")

      wsk.action.delete(nodejs8htmlAction)
      wsk.action.delete(objectWriteAction)
      wsk.action.delete(objectReadAction)
      wsk.action.delete(objectDeleteAction)
      wsk.action.delete(bucketCorsDeleteAction)
      wsk.action.delete(bucketCorsGetAction)
      wsk.action.delete(bucketCorsPutAction)
      wsk.action.delete(getSignedUrlAction)
      wsk.pkg.delete(nodejs8Package)
      wsk.pkg.delete(binding)
    }

    private def verifyAction(action: RunResult, name: String, kindValue: JsString): Unit = {
      val stdout = action.stdout
      assert(stdout.startsWith(s"ok: got action $name\n"))
      wsk.parseJsonString(stdout).fields("exec").asJsObject.fields("kind") shouldBe kindValue
    }

    private def verifyOptions(url: String, queryParams : Map[String, _], methodType : String) = {
      val response =  RestAssured.given()
        .urlEncodingEnabled(false)
        .queryParameters(queryParams)
        .header("Origin", s"https://${wskprops.apihost}")
        .header("Access-Control-Request-Method", methodType)
        .options(url)
      print(s"STATUS: ${response.statusLine()}")
      assert(response.statusCode() == 200)
      response.getHeader("Access-Control-Allow-Origin") should include ("*")
      response.getHeader("Access-Control-Allow-Methods") should include ("PUT, GET, DELETE")
    }

    private def parseUrl(url: String) : (String, Map[String, _]) = {
      val baseUrl = url.substring(0, url.indexOf('?'))
      val queryString = url.substring(url.indexOf('?') + 1)
      val pairs = queryString.split("&|=").grouped(2)
      val queryMap = pairs.map { case Array(k, v) => k -> v }.toMap

      return (baseUrl, queryMap)
    }

    private def parseCredentials(credentials: JsonObject) : JsObject = {
      val apikey = creds.get("apikey").getAsString()
      val resource_instance_id = creds.get("resource_instance_id").getAsString()
      val access_key_id = creds
        .get("cos_hmac_keys")
        .getAsJsonObject()
        .get("access_key_id")
        .getAsString()
      val secret_access_key = creds
        .get("cos_hmac_keys")
        .getAsJsonObject()
        .get("secret_access_key")
        .getAsString()

      return JsObject(
        "cloud-object-storage" -> JsObject(
          "cos_hmac_keys" -> JsObject(
            "access_key_id" -> JsString(access_key_id),
            "secret_access_key" -> JsString(secret_access_key)
          ),
          "apikey" -> JsString(apikey),
          "resource_instance_id" -> JsString(resource_instance_id)
        ))
    }

    private def deployCOSPackage() = {
      makePostCallWithExpectedResult(JsObject(
        "gitUrl" -> JsString(packageTestRepo),
        "manifestPath" -> JsString(nodejs8RuntimePath),
        "wskApiHost" -> JsString(wskprops.apihost),
        "wskAuth" -> JsString(wskprops.authKey)
      ), successStatus, 200)
    }

    private def deployTemplate(packageName : JsString) = {
      makePostCallWithExpectedResult(JsObject(
        "gitUrl" -> JsString(deployTestRepo),
        "manifestPath" -> JsString(nodejs8RuntimePath),
        "envData" -> JsObject(
          "PACKAGE_NAME" -> packageName
        ),
        "wskApiHost" -> JsString(wskprops.apihost),
        "wskAuth" -> JsString(wskprops.authKey)
      ), successStatus, 200)
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
