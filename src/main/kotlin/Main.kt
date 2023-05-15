import view.MainWindow
import javax.swing.*
import io.socket.client.IO
import io.socket.client.Socket
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.socket.client.Ack
import io.socket.emitter.Emitter
import socket.*
import org.json.JSONObject
import view.GUIMain

/*
 * Main
 */




fun main() {
    //Server URL
    val serverURL = "https://nope-server.azurewebsites.net/"

    val restapi = RestApi()
    //Register
    //restapi.registerUser()

    //Login
    // TODO : catch a nullpointer if access token is null
    val token = restapi.userLogin()
    // Create an instance of the StartMenu class
    val menu = GUIMain()
    //Socket init
    if(token != null){
        val mSocket = socketinit(serverURL, token, menu)
        connect()
        restapi.connect(token)

    }else{
        println("Your login/ was not valid")
    }


    // MainWindow
    //val window = MainWindow()


    //val jsonTournamentCreate = gson.toJson(tc)


    // socket.on -> empfangen der events + json datein
    // je nach event wird andere socket.on ausgeführt (Listener)
    //val socket = mSocket!!.connect()
    //socket.on("game:state" ){

       // println("Please play a card within 10 seconds")

        // Listen for player movepayload and send acknowledgement to server
        //val movepayload = calculateMove()
        //ack(movepayload)
    //}

    // Exit when the window is closed
    //window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE



}