package socket

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import com.google.gson.Gson

/*
 * Http Requests - REST API code
 * Klassen mit Infos für die Json Objekte
 */

class RestApi(){
    fun registerUser() {

        val registerOb = Register("LisaJaksties","fukuoka24","Lisa","Jaksties")

        println("hi")
        val client = HttpClient.newHttpClient()

        val gson = Gson()
        val jsonRequest = gson.toJson(registerOb)

        val request = HttpRequest.newBuilder()
            .uri(URI("https://nope-server.azurewebsites.net/api/auth/register"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
            .build()

        val postResponse = client.send(request, BodyHandlers.ofString())
        println("Response - POST register")
        println(postResponse.body())
        if (postResponse.statusCode() == 200) {
            print("lauft alles super")
        }
    }

    fun userLogin(username: String, password:String): Accesstoken?{
        // create Login Object with data and convert to Json with gson
        val loginOb = Login(username, password)
        val gson = Gson()
        val jsonLogRequest = gson.toJson(loginOb)
        val client = HttpClient.newHttpClient()

        //Send a Http Request with jsonLogRequest to server
        val request = HttpRequest.newBuilder()
            .uri(URI("https://nope-server.azurewebsites.net/api/auth/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonLogRequest))
            .build()

        // store the access token if Login was valid and return accessToken
        val loginResponse = client.send(request, BodyHandlers.ofString())
        if (loginResponse.statusCode() == 200) {
            val responseBody = loginResponse.body()
            val accessToken = extractAccessToken(responseBody)
            //store the token into class Token
            val token = Accesstoken(accessToken)
            println("Login Response: ")
            println(loginResponse.body())
            println(accessToken)
            return token

        }
        return null

    }

    fun connect(access_token: Accesstoken){
        // create Login Object with data and convert to Json with gson
        val token = Token(access_token.accessToken.toString())
        val gson = Gson()
        val jsonAuthentication = gson.toJson(token)
        val client = HttpClient.newHttpClient()

        //Send a Http Request with jsonLogRequest to server
        val request = HttpRequest.newBuilder()
            .uri(URI("https://nope-server.azurewebsites.net/api/verify-token"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token.token)
            .POST(HttpRequest.BodyPublishers.ofString(jsonAuthentication))
            .build()

        // store the access token if Login was valid and return accessToken
        val authenticationResponse = client.send(request, BodyHandlers.ofString())
        if (authenticationResponse.statusCode() == 200) {
            val responseBody = authenticationResponse.body()
            println("Authentication response: ")
            println(authenticationResponse.body())
         }

    }

    /**
     * extract the Tokenvalue from the original response body
     */
    fun extractAccessToken(responseBody: String): String? {
        val regex = "\"accessToken\":\"(.*?)\"".toRegex()
        val matchResult = regex.find(responseBody)
        return matchResult?.groupValues?.getOrNull(1)
    }

}
