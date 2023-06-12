import socket.*
import view.GUIgame

/*
 * Main
 */
fun main() {
    //Server URL
    val serverURL = "https://nope-server.azurewebsites.net/"

    val restapi = RestApi()
    //Register
    //restapi.registerUser()
    val username = "koala"
    //val username = "LisaJaksties"
    val password = "fukuoka24"
    //Login
    val token = restapi.userLogin(username, password)
    // Create an instance of the StartMenu class
    val menu = GUIgame()
    //Socket init
    if(token != null){
        val mSocket = socketinit(serverURL, token, menu, username)
        connect()
        restapi.connect(token)

    }else{
        println("Fail: You couldn't log in")
    }

}