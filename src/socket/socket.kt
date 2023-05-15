package socket
import ai_player.*
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URI
import java.net.URISyntaxException
import io.socket.emitter.Emitter
import io.socket.client.Ack
import com.google.gson.Gson
import org.json.JSONArray
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.util.Collections.singletonMap
import javax.swing.SwingUtilities
import org.json.JSONObject;
import view.GUIMain

/*
 *
 *  game relevant data
 */

// Create an empty ArrayList
var tournamentList = ArrayList<Tournament>()

var currentTournament = Tournament(null,null,null,null,null,null, null)

/*
 * Socket code
 */


var mSocket : Socket? = null
var guiObject: GUIMain? = null
var emitter = Emitter.Listener {}
val gson = Gson()


fun socketinit(serverURL:String, access_token: Accesstoken, gui:GUIMain): Socket?{


    var token = access_token
    val gson = Gson()
    val tokenjwt = gson.toJson(token)
    guiObject = gui


    try {
        val opts = IO.Options.builder()
            .setTransports(arrayOf(io.socket.engineio.client.transports.WebSocket.NAME))
            //.setQuery("token=$tokenjwt")
            .setAuth(singletonMap("token", token.accessToken))
            .build()
        mSocket = IO.socket(URI.create("https://nope-server.azurewebsites.net/"), opts)
    } catch (e: URISyntaxException) {
        e.printStackTrace()
    }

    return mSocket

}

fun connect() {
    val socket = mSocket!!.connect()
    socket.on(Socket.EVENT_CONNECT,{println("Connected")}).on(Socket.EVENT_CONNECT_ERROR, {println(it.joinToString())})
    // put Socket.On functions here
    getCurrentTournaments()
    getPlayerInfo()
    receiveMatchInvitation()
}
fun disconnect() {
    mSocket!!.disconnect()
}

// TODO braucht der Emitter einen Acknowledge? : Ack acknowl
fun emit(event: String, data: JsonObject): Emitter?{
    return mSocket ?.emit(event, data)
}
fun emit(event: String, data: JsonArray): Emitter?{
    return mSocket ?.emit(event, data)
}
fun emit(event: String, data: String): Emitter?{
    return mSocket ?.emit(event, data)
}

fun createTournament(number: Int): TournamentInfo {

    val tournamentInfo = TournamentInfo(null,null,null,false,null)

    mSocket?.emit("tournament:create", number, Ack { ackData ->
        // Handle acknowledgement data or failure

        if (ackData != null) {
            val result = gson.toJson(ackData)
            val jsonArray = JSONArray(result)

            // get data about tournament creation
            val getTournamentInfo = jsonArray.getJSONObject(0).getJSONObject("map")
            val success = getTournamentInfo.getBoolean("success")
            val error = getTournamentInfo.get("error")
            tournamentInfo.error = error
            tournamentInfo.success = success
            if(success){
                // get tournament data
                val getTournamentData = jsonArray.getJSONObject(0).getJSONObject("map").getJSONObject("data").getJSONObject("map")
                val tournamentId = getTournamentData.getString("tournamentId")
                val currentSize = getTournamentData.getInt("currentSize")
                val bestOf = getTournamentData.getInt("bestOf")
                tournamentInfo.tournamentId = tournamentId
                tournamentInfo.bestOf = bestOf
                tournamentInfo.currentSize = currentSize

                if (tournamentInfo.tournamentId != null) {
                    println("Tournament created with ID: ${tournamentInfo.tournamentId}, current size: ${tournamentInfo.currentSize}, best of: ${tournamentInfo.bestOf}")

                    currentTournament.id = tournamentInfo.tournamentId
                    currentTournament.bestOf = tournamentInfo.bestOf
                    currentTournament.currentSize = tournamentInfo.currentSize

                }else {
                    println("Error: tournament data is missing or incomplete")
                }
            }else{
                val errorMessage = getTournamentInfo.getJSONObject("error").getJSONObject("map").getString("message")
                tournamentInfo.error =errorMessage
                println("Error: tournament creation failed : $errorMessage")
            }
            println("Acknowledgement received - Success: CREATE Tournament ${result.toString()}")

        } else {
            // Handle acknowledgement failure
            println("Acknowledgement not received")
        }
    })
    Thread.sleep(1000)
    return tournamentInfo
}


fun getCurrentTournaments() {

    println("Hallo Ich habe etwas empfangen JEEE JEEEE")
    mSocket?.off("list:tournaments")
    mSocket?.on("list:tournaments", Emitter.Listener { args ->

        if (args[0] != null) {
            tournamentList.clear()

            val result = gson.toJson(args)
            val jsonTemp = JSONArray(result)
            val tourData = jsonTemp.getJSONObject(0)
            val myArrayList = tourData.getJSONArray("myArrayList")
            println(myArrayList.toString())
            val length = myArrayList.length()
            for (i in 0 until length) {
                val tournamentData = myArrayList.getJSONObject(i).getJSONObject("map")
                val id = tournamentData.getString("id")
                val createdAt = tournamentData.getString("createdAt")
                val currentSize = tournamentData.getInt("currentSize")
                val status = tournamentData.getString("status")
                val players = ArrayList<Player>()
                val getPlayerData = tournamentData.getJSONObject("players").getJSONArray("myArrayList")
                for (j in 0 until getPlayerData.length()) {
                    val newP = Player(null, getPlayerData.getJSONObject(j).getJSONObject("map").getString("username"))
                    players.add(newP)
                }
                val newTournament = Tournament(id, createdAt, currentSize, players, status,null, null)
                if (newTournament.id != null) {
                    println("New Entry found: ${tournamentList.size} TournamentID: ${newTournament.id}, current size: ${newTournament.currentSize}, createdAt:  ${newTournament.createdAt}, status:  ${newTournament.status}, players:  ${newTournament.players}")
                } else {
                    println("Error: tournament data is missing or incomplete")
                }

                tournamentList.add(newTournament)
            }
            //println("Received tournaments: ${result.toString()} \n")
            SwingUtilities.invokeLater {
                // update GUI components here
                guiObject?.updateTournamentList()
            }

        } else {
            println("No tournaments received")
        }
    })

}


fun getPlayerInfo() {

    mSocket?.off("tournament:playerInfo")
    mSocket?.on("tournament:playerInfo", Emitter.Listener { args ->

        if (args[0] != null) {

            val result = gson.toJson(args)
            val jsonTemp = JSONArray(result)
            val tourData = jsonTemp.getJSONObject(0)
            val myArrayList = tourData.getJSONArray("myArrayList")
            println(myArrayList.toString())
            val length = myArrayList.length()
            for (i in 0 until length) {
                val tournamentData = myArrayList.getJSONObject(i).getJSONObject("map")
                val id = tournamentData.getString("id")
                val createdAt = tournamentData.getString("createdAt")
                val currentSize = tournamentData.getInt("currentSize")
                val status = tournamentData.getString("status")
                val players = ArrayList<Player>()
                val getPlayerData = tournamentData.getJSONObject("players").getJSONArray("myArrayList")
                for (j in 0 until getPlayerData.length()) {
                    val newP = Player(null, getPlayerData.getJSONObject(j).getJSONObject("map").getString("username"))
                    players.add(newP)
                }
                val newTournament = Tournament(id, createdAt, currentSize, players, status,null, null)
                if (newTournament.id != null) {
//            currentTournament.id = tournamentInfo.tournamentId
//            currentTournament.bestOf = tournamentInfo.bestOf
//            currentTournament.currentSize = tournamentInfo.currentSize
                    println("TournamentID: ${newTournament.id}, current size: ${newTournament.currentSize}, createdAt:  ${newTournament.createdAt}, status:  ${newTournament.status}, players:  ${newTournament.players}")
                } else {
                    println("Error: tournament data is missing or incomplete")
                }

                tournamentList.add(newTournament)
            }
            println("Received PlayerData: ${result.toString()} \n")
//            SwingUtilities.invokeLater {
//                // update GUI components here
//                guiObject?.updateTournamentList()
//            }

        } else {
            println("No Player Data received")
        }
    })

}


fun joinTournament(id:String): TournamentInfo{
    val tournamentInfo = TournamentInfo(null,null,null,false,null)
    mSocket?.emit("tournament:join", id, Ack { ackData ->
        if (ackData != null) {
            val result = gson.toJson(ackData)
            val jsonArray = JSONArray(result)

            // get data about tournament that you join
            val getTournamentInfo = jsonArray.getJSONObject(0).getJSONObject("map")
            val success = getTournamentInfo.getBoolean("success")
            val error = getTournamentInfo.get("error")
            tournamentInfo.success = success
            if(success){

                tournamentInfo.error = error
                // get tournament data
//                val getTournamentData = jsonArray.getJSONObject(0).getJSONObject("map").getJSONObject("data").getJSONObject("map")
//                val tournamentId = getTournamentData.getString("tournamentId")
//                val currentSize = getTournamentData.getInt("currentSize")
//                val bestOf = getTournamentData.getInt("bestOf")


//                //if (tournamentInfo.tournamentId != null) {
//                    //println("Tournament created with ID: ${tournamentInfo.tournamentId}, current size: ${tournamentInfo.currentSize}, best of: ${tournamentInfo.bestOf}")
//                }else {
//                println("Error: tournament data is missing or incomplete")
//                }
            }else{
                val errorMessage = getTournamentInfo.getJSONObject("error").getJSONObject("map").getString("message")
                tournamentInfo.error = errorMessage
                println("Error: tournament join  failed : $errorMessage")
            }
            println("Acknowledgement received - Success: JOIN Tournament  ${jsonArray.toString()}")
            getCurrentTournaments()
        } else {
            // Handle acknowledgement failure
            println("Acknowledgement not received")
        }
    })
    Thread.sleep(1000)
    return tournamentInfo

}

fun leaveTournament():TournamentInfo{
    val tournamentInfo = TournamentInfo(null,null,null,false,null)
    mSocket?.emit("tournament:leave",  Ack { ackData ->
        if (ackData != null) {
            val result = gson.toJson(ackData)
            val jsonArray = JSONArray(result)

            // get data about tournament that you leave
            val getTournamentInfo = jsonArray.getJSONObject(0).getJSONObject("map")
            val success = getTournamentInfo.getBoolean("success")
            val error = getTournamentInfo.get("error")
            tournamentInfo.success = success
            if(success){

                tournamentInfo.error = error
                // get tournament data
//                val getTournamentData = jsonArray.getJSONObject(0).getJSONObject("map").getJSONObject("data").getJSONObject("map")
//                val tournamentId = getTournamentData.getString("tournamentId")
//                val currentSize = getTournamentData.getInt("currentSize")
//                val bestOf = getTournamentData.getInt("bestOf")


//                //if (tournamentInfo.tournamentId != null) {
//                    //println("Tournament created with ID: ${tournamentInfo.tournamentId}, current size: ${tournamentInfo.currentSize}, best of: ${tournamentInfo.bestOf}")
//                }else {
//                println("Error: tournament data is missing or incomplete")
//                }
            }else{
                val errorMessage = getTournamentInfo.getJSONObject("error").getJSONObject("map").getString("message")
                tournamentInfo.error = errorMessage
                println("Error: tournament leave  failed : $errorMessage")
            }
            println("Acknowledgement received - Success: LEAVE Tournament ${result.toString()}")
            getCurrentTournaments()
        } else {
            // Handle acknowledgement failure
            println("Acknowledgement not received")
        }
    })
    Thread.sleep(1000)
    return tournamentInfo
}

fun startTournament(): TournamentInfo {
    val tournamentInfo = TournamentInfo(null,null,null,false,null)
    mSocket?.emit("tournament:start",  Ack { ackData ->
        if (ackData != null) {
            val result = gson.toJson(ackData)
            val jsonArray = JSONArray(result)

            // get data about tournament that you start
            val getTournamentInfo = jsonArray.getJSONObject(0).getJSONObject("map")
            val success = getTournamentInfo.getBoolean("success")
            val error = getTournamentInfo.get("error")
            tournamentInfo.success = success
            if(success){

                tournamentInfo.error = error
                // get tournament data
//                val getTournamentData = jsonArray.getJSONObject(0).getJSONObject("map").getJSONObject("data").getJSONObject("map")
//                val tournamentId = getTournamentData.getString("tournamentId")
//                val currentSize = getTournamentData.getInt("currentSize")
//                val bestOf = getTournamentData.getInt("bestOf")


//                //if (tournamentInfo.tournamentId != null) {
//                    //println("Tournament created with ID: ${tournamentInfo.tournamentId}, current size: ${tournamentInfo.currentSize}, best of: ${tournamentInfo.bestOf}")
//                }else {
//                println("Error: tournament data is missing or incomplete")
//                }
            }else{
                val errorMessage = getTournamentInfo.getJSONObject("error").getJSONObject("map").getString("message")
                tournamentInfo.error = errorMessage
                println("Error: tournament start  failed : $errorMessage")
            }
            println("Acknowledgement received - Success: START Tournament ${result.toString()}")
            getCurrentTournaments()
        } else {
            // Handle acknowledgement failure
            println("Acknowledgement not received")
        }
    })
    Thread.sleep(1000)
    return tournamentInfo
}
fun receiveMatchInvitation() {
    mSocket?.off("match:invite")
    mSocket?.on("match:invite", Emitter.Listener { args ->
//        val playerId = get
//        val sendAck = Ack { true,currentGame. }
        if (args[0] != null) {
            val result = gson.toJson(args)
            val jsonTemp = JSONArray(result)
            val jsonObject = jsonTemp.getJSONObject(0)
            val mapObject = jsonObject.getJSONObject("map")
            val invitationTimeout = mapObject.getLong("invitationTimeout")
            val playersArray = mapObject.getJSONObject("players").getJSONArray("myArrayList")
            val players = ArrayList<Player>()
            var myId: String? = null
            for (i in 0 until playersArray.length()) {
                val playerObject = playersArray.getJSONObject(i).getJSONObject("map")
                val playerId = playerObject.getString("id")
                val playerUsername = playerObject.getString("username")
                val player = Player(playerId, playerUsername)
                players.add(player)
                if (playerUsername == "koala") {
                    myId = playerId
                }
            }
            val message = mapObject.getString("message")
            val matchId = mapObject.getString("matchId")
            val matchInformation = MatchInvitation(invitationTimeout, players, message, matchId)
            println("Received MatchInvitation: ${matchInformation.toString()} \n")

            println("Received MatchInvitation: ${result.toString()} \n")
            // Send acknowledgement with true and my id
            if (myId != null) {
                val reply = InvitationReply(true,myId)
                val message = gson.toJson(reply)
                val message2 = JSONObject(message)
                println("THIS is what I send: ${message2.toString()}")

                if (args.size > 1 && args[1] is Ack) {
                    (args[1] as Ack).call(message2)
                }


//                val ack = Ack{ response ->
//                    // Your code to handle the response received from the server
//                    if (response != null) {
//                        // Do something with the response
//                        println("INVITATION REPLY send ${response.toString()}")
//                    }
//                }
//                ack.call(message2)
//                mSocket?.emit("match:invite", message2, Ack { ackData ->
//                    if (ackData != null) {
//                        val result = gson.toJson(ackData)
//                        println("INVITATION REPLY RECEIVED: ${result.toString()}")
//                    }
//                })
            }

        } else {
            println("No MatchInvitation received")
        }
    })
}


class Socket_events(){
    // TODO events:
    // Events return a String like this: public static final String EVENT_MESSAGE
    // object Connect : SocketEvent<Unit>
    // object Connecting : SocketEvent<Unit>
    // object Disconnect : SocketEvent<Unit>
    // object Error : SocketEvent<Throwable>
    // object Message : SocketEvent<Any>
    // object Reconnect : SocketEvent<Unit>
    // object ReconnectAttempt : SocketEvent<Int>
    // object Ping : SocketEvent<Unit>
    // object Pong : SocketEvent<Unit>
}