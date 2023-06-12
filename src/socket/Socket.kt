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
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.util.Collections.singletonMap
import javax.swing.SwingUtilities
import org.json.JSONObject
import com.google.gson.*
import view.GUIgame

/**
 * Socket IO : connect & disconnect
 *  receive in Game / Tournament information with these methods:
 *  getCurrentTournaments()
 *  getPlayerInfo()
 *  getTournamentInfo()
 *  receiveMatchInvitation()
 *  getMatchInfo()
 *  AiPlayerMakeMove()
 *  getGameState()
 *  getGameStatus()
 *
 *  send information during the game:
 *  createTournament()
 *  joinTournament()
 *  leaveTournament()
 *  startTournament()
 *
 */

var tournamentList = ArrayList<Tournament>()

var currentTournament = Tournament(null,null,null,null,null,null, null, null,null)

var currentMatch = Match(null,null,null,null,null,null,null,null)

var currentGame = GameState(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null)

var aiPlayer = AILogic()


var mSocket : Socket? = null
var guiObject: GUIgame? = null
val gson = Gson()
var user = ""


fun socketinit(serverURL:String, access_token: Accesstoken, gui:GUIgame, username:String): Socket?{

    user = username
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
    getTournamentInfo()
    receiveMatchInvitation()
    getMatchInfo()
    AiPlayerMakeMove()
    getGameState()
    getGameStatus()
}
fun disconnect() {
    mSocket!!.disconnect()
}

/**
 * host sends a number of best of matches to start a Tournament
 */
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

/**
 * receive all created Tournaments with infos about players, status..
 */
fun getCurrentTournaments() {

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
                    val newP = Player(null, getPlayerData.getJSONObject(j).getJSONObject("map").getString("username"),null, null)
                    players.add(newP)
                }
                val newTournament = Tournament(id, createdAt, currentSize, players, status,null, null,null,null)
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
                guiObject?.updateTournamentLobby()
            }

        } else {
            println("No tournaments received")
        }
    })
}

/**
 * receive infos after joining a tournament
 * when a player joins or leaves the tournament- waitingroom
 */
fun getPlayerInfo() {
    mSocket?.off("tournament:playerInfo")
    mSocket?.on("tournament:playerInfo", Emitter.Listener { args ->
        if (args[0] != null) {
            val result = gson.toJson(args)
            val tournamentData = JSONArray(result).getJSONObject(0).getJSONObject("map")
            val tournamentId = tournamentData.getString("tournamentId")
            val currentSize = tournamentData.getInt("currentSize")
            val bestOf = tournamentData.getInt("bestOf")
            val message = tournamentData.getString("message")
            val playersArray = tournamentData.getJSONObject("players").getJSONArray("myArrayList")
            val players = ArrayList<Player>()
            for (i in 0 until playersArray.length()) {
                val playerData = playersArray.getJSONObject(i).getJSONObject("map")
                val newPlayer = Player(playerData.getString("id"), playerData.getString("username"),null, null)
                players.add(newPlayer)
            }
            currentTournament.id = tournamentId
            currentTournament.bestOf = bestOf
            currentTournament.currentSize = currentSize
            currentTournament.message = message
            currentTournament.players = players

            println("Received Tournament Data: ID: ${currentTournament.id} Message: ${currentTournament.message} Players: ${currentTournament.players} Best of: ${currentTournament.bestOf} Current Size: ${currentTournament.currentSize}")
            SwingUtilities.invokeLater {
                // update GUI components here
                guiObject?.updateWaitingRoom()
            }
        } else {
            println("No Player Data received")
        }
    })
}

/**
 * receive infos after joining a tournament - waitingroom
 */
fun getTournamentInfo() {
    mSocket?.off("tournament:info")
    mSocket?.on("tournament:info", Emitter.Listener { args ->
        if (args[0] != null) {
            val result = gson.toJson(args)
            val jsonArray = JSONArray(result)
            val jsonObject = jsonArray.getJSONObject(0).getJSONObject("map")
            val tournamentId = jsonObject.getString("tournamentId")
            val currentSize = jsonObject.getInt("currentSize")
            val message = jsonObject.getString("message")
            val status = jsonObject.getString("status")
            val hostObj = jsonObject.getJSONObject("host").getJSONObject("map")
            val hostId = hostObj.getString("id")
            val hostUsername = hostObj.getString("username")
            val playersArray = jsonObject.getJSONObject("players").getJSONArray("myArrayList")
            val players = ArrayList<Player>()
            for (i in 0 until playersArray.length()) {
                val playerObj = playersArray.getJSONObject(i).getJSONObject("map")
                val playerId = playerObj.getString("id")
                val playerUsername = playerObj.getString("username")
                val playerScore = playerObj.getInt("score")
                val player = Player(playerId, playerUsername, playerScore, null)
                players.add(player)
            }
            val host = Player(hostId, hostUsername, null, null)

            val winnerObject = jsonObject.optJSONObject("winner")?.optJSONObject("map")
            val winner = winnerObject?.let {
                Player(it.getString("id"), it.getString("username"),it.getInt("score"),null)
            }
            val tournament = Tournament(tournamentId, currentTournament.createdAt, currentSize, players, status, currentTournament.bestOf, message, host, winner)
            currentTournament = tournament
            println("Received Tournament Info Data: ID: ${currentTournament.id} Message: ${currentTournament.message} Players: ${currentTournament.players} Best of: ${currentTournament.bestOf} Current Size: ${currentTournament.currentSize} Current Host: ${currentTournament.host} Current Winner: ${currentTournament.winner}")
            println("Received Tournament info: ${result.toString()} \n")
            SwingUtilities.invokeLater {
                // update GUI components here
                guiObject?.updateWaitingRoom()
            }
        } else {
            println("No Tournament Data received")
        }
    })
}

/**
 * player sends id of tournament that he wants to join
 */
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

/**
 * if player wants to leave a tournament
 */
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

/**
 * host sends tournament start to start the Tournament
 */
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
                val player = Player(playerId, playerUsername,null, null)
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

            }

        } else {
            println("No MatchInvitation received")
        }
    })
}

/**
 * receive new round scores
 */
fun getMatchInfo() {
    mSocket?.off("match:info")
    mSocket?.on("match:info", Emitter.Listener { args ->
        if (args[0] != null) {
            val result = gson.toJson(args)
            println(result.toString())
            val jsonArray = JSONArray(result)
            val jsonObject = jsonArray.getJSONObject(0).getJSONObject("map")
            val message = jsonObject.getString("message")
            val tournamentId = jsonObject.getString("tournamentId")
            val matchObj = jsonObject.getJSONObject("match").getJSONObject("map")
            val round = matchObj.getInt("round")
            val id = matchObj.getString("id")
            val bestOf = matchObj.getInt("bestOf")
            val status = matchObj.getString("status")
            val opponentsArray = matchObj.getJSONObject("opponents").getJSONArray("myArrayList")

            val opponentList = opponentsArray?.let {
                val opponents = ArrayList<Player>()
                for (i in 0 until it.length()) {
                    val opponentObj = it.getJSONObject(i).getJSONObject("map")
                    val id = opponentObj.getString("id")
                    val username = opponentObj.getString("username")
                    val points = opponentObj.getInt("points")
                    val opponent = Player(id, username, points,null)
                    opponents.add(opponent)
                }
                opponents
            }
            val winnerObject = matchObj.optJSONObject("winner")?.optJSONObject("map")
            val winner = winnerObject?.let {
                Player(it.getString("id"), it.getString("username"),it.getInt("points"),null)
            }

            val match = Match(message, tournamentId, id, round, bestOf, status, opponentList, winner)
            currentMatch = match
            println("Received Match Info: ID: ${currentMatch.id} Message: ${currentMatch.message} Opponents: ${currentMatch.opponents} Best of: ${currentMatch.bestOf} Round: ${currentMatch.round} Status: ${currentMatch.status} Winner: ${currentMatch.winner}")
            SwingUtilities.invokeLater {
                // update GUI components here
                guiObject?.updateGameMenu(user)
            }
        } else {
            println("No Match Data received")
        }
    })
}

/**
 * receive infos for the ai players move and return the move in acknowledge
 */
fun AiPlayerMakeMove() {
    mSocket?.off("game:makeMove")
    mSocket?.on("game:makeMove", Emitter.Listener { args ->

        if (args[0] != null) {
            val result = gson.toJson(args)
            println(result.toString())

            // send back Move
            // Create a separate Gson instance for Card serialization
            val gsonCard = GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Card::class.java, object : TypeAdapter<Card>() {
                    override fun write(out: JsonWriter, value: Card?) {
                        out.beginObject()
                        out.name("type").value(value?.getType())
                        out.name("color").value(value?.getColor())
                        out.name("value").value(value?.value)
                        out.endObject()
                    }

                    override fun read(input: JsonReader): Card? {
                        // Implement this method if needed for deserialization
                        return null
                    }
                })
                .create()

            // send back Move
            val gsonSpecial = GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Move::class.java, object : TypeAdapter<Move>() {
                    override fun write(out: JsonWriter, value: Move?) {
                        out.beginObject()
                        out.name("type").value(value?.getMoveType())
                        out.name("card1").jsonValue(gsonCard.toJsonTree(value?.card1).toString())
                        out.name("card2").jsonValue(gsonCard.toJsonTree(value?.card2).toString())
                        out.name("card3").jsonValue(gsonCard.toJsonTree(value?.card3).toString())
                        out.name("reason").value(value?.reason)
                        out.endObject()
                    }

                    override fun read(input: JsonReader): Move? {
                        // Implement this method if needed for deserialization
                        return null
                    }
                })
                .create()

            val replyMove = aiPlayer.makeMove(currentGame, user)
            val replyMoveJSONString = gsonSpecial.toJson(replyMove)
            val myMove = JSONObject(replyMoveJSONString)
            //val replyMoveJSONObject = JSONObject(replyMoveJSONString)

            println("THIS is my move as a JSON String: ${replyMoveJSONString.toString()}")
            println()
            //println("THIS is my move as a JSON Object: ${replyMoveJSONObject.toString()}")

            if (args.size > 1 && args[1] is Ack) {
                (args[1] as Ack).call(myMove)
            }
            SwingUtilities.invokeLater {
                // update GUI components here
                guiObject?.updateMyMove(replyMove)
            }

        } else {
            println("No Notice For Make A Move received")
        }
    })
}

/**
 * receive information for the gameplay
 */
fun getGameState() {
    mSocket?.off("game:state")
    mSocket?.on("game:state", Emitter.Listener { args ->
        println("########################### GAME STATE ###############################")
        if (args[0] != null) {
            val result = gson.toJson(args)
            val jsonArray = JSONArray(result)

            val gameStateObject = jsonArray.getJSONObject(0).getJSONObject("map")
            val gameId = gameStateObject.getString("gameId")
            val matchId = gameStateObject.getString("matchId")
            val topCardObject = gameStateObject.getJSONObject("topCard").getJSONObject("map")
            val currCardType = topCardObject.getString("type")
            val type = Type.fromTypeString(currCardType)
            val currCardColor = topCardObject.getString("color")
            val color = Color.fromTypeString(currCardColor)
            val currCardValue: Int? = topCardObject?.let {
                when (val value = it.get("value")) {
                    is Int -> value
                    is String -> value.toIntOrNull()
                    else -> null
                }
            }
            val topCard = Card(
                type!!,
                color!!,
                currCardValue,null,null,null
            )
            val lastTopCardObject = gameStateObject.optJSONObject("lastTopCard")?.optJSONObject("map")
            val lastTopCard = lastTopCardObject?.let {
                val currCardType = it.getString("type")
                val type = Type.fromTypeString(currCardType)
                val currCardColor = it.getString("color")
                val color = Color.fromTypeString(currCardColor)
                // get Value of card (can be either a number or null)
                val currCardValue: Int? = it?.let {
                    when (val value = it.get("value")) {
                        is Int -> value
                        is String -> value.toIntOrNull()
                        else -> null
                    }
                }
                Card(type!!,color!! , currCardValue,null,null,null)
            }
            val drawPileSize = gameStateObject.getInt("drawPileSize")
            val handSize = gameStateObject.getInt("handSize")

            val currentPlayerObject = gameStateObject.getJSONObject("currentPlayer").getJSONObject("map")
            val currentPlayer = Player(
                currentPlayerObject.getString("id"),
                currentPlayerObject.getString("username"), null, null
            )
            val currentPlayerIdx = gameStateObject.getInt("currentPlayerIdx")

            val prevPlayerObject = gameStateObject.optJSONObject("prevPlayer")?.optJSONObject("map")
            val prevPlayer = prevPlayerObject?.let {
                Player(it.getString("id"), it.getString("username"),null, null)
            }
            val prevPlayerIdx = gameStateObject.opt("prevPlayerIdx") as? Int

            val prevTurnCardsArray = gameStateObject.getJSONObject("prevTurnCards").optJSONArray("myArrayList")
            val prevTurnCards = prevTurnCardsArray?.let {
                val cards = ArrayList<Card>()
                for (i in 0 until it.length()) {
                    val cardObject = it.getJSONObject(i).getJSONObject("map")
                    val currCardType = cardObject.getString("type")
                    val type = Type.fromTypeString(currCardType)
                    val currCardColor = cardObject.getString("color")
                    val color = Color.fromTypeString(currCardColor)
                    // get Value of card (can be either a number or null)
                    val currCardValue: Int? = cardObject?.let {
                        when (val value = cardObject.get("value")) {
                            is Int -> value
                            is String -> value.toIntOrNull()
                            else -> null
                        }
                    }

                    cards.add(Card(
                        type!!,
                        color!!,
                        currCardValue,null,null,null
                    ))
                }
                cards

            }

            val playersArray = gameStateObject.getJSONObject("players").getJSONArray("myArrayList")
            val players = ArrayList<Player>()
            for (i in 0 until playersArray.length()) {
                val playerObject = playersArray.getJSONObject(i).getJSONObject("map")
                val playerId = playerObject.getString("id")
                val playerUsername = playerObject.getString("username")
                val playerHandSize = playerObject.getInt("handSize")
                players.add(Player(playerId, playerUsername, null ,playerHandSize))
            }

            val handArray = gameStateObject.getJSONObject("hand").getJSONArray("myArrayList")
            val hand = ArrayList<Card>()
            for (i in 0 until handArray.length()) {
                val cardObject = handArray.getJSONObject(i).getJSONObject("map")
                val currCardType = cardObject.getString("type")
                val type = Type.fromTypeString(currCardType)
                val currCardColor = cardObject.getString("color")
                val color = Color.fromTypeString(currCardColor)
                // get Value of card (can be either a number or null)
                val currCardValue: Int? = cardObject?.let {
                    when (val value = it.get("value")) {
                        is Int -> value
                        is String -> value.toIntOrNull()
                        else -> null
                    }
                }

                hand.add(Card(
                    type!!,
                    color!!,
                    currCardValue,null,null,null
                ))
            }

            val lastMoveObject = gameStateObject.optJSONObject("lastMove")?.optJSONObject("map")
            val lastMove = lastMoveObject?.let {

                var card1Object = it.optJSONObject("card1").optJSONObject("map")
                val card1 = card1Object?.let{
                    val color = card1Object.getString("color")
                    val colorCard = Color.fromTypeString(color)
                    val type = card1Object.getString("type")
                    val typeCard = Type.fromTypeString(type)

                    // get Value of card (can be either a number or null)
                    val currCardValue: Int? = card1Object?.let {
                        when (val value = card1Object.get("value")) {
                            is Int -> value
                            is String -> value.toIntOrNull()
                            else -> null
                        }
                    }

                    Card(typeCard!!,colorCard!!,currCardValue,null,null,null)
                }
                var card2Object = it.optJSONObject("card2").optJSONObject("map")
                val card2 = card2Object?.let{
                    val color = card2Object.getString("color")
                    val colorCard = Color.fromTypeString(color)
                    val type = card2Object.getString("type")
                    val typeCard = Type.fromTypeString(type)

                    // get Value of card (can be either a number or null)
                    val currCardValue: Int? = card2Object?.let {
                        when (val value = card2Object.get("value")) {
                            is Int -> value
                            is String -> value.toIntOrNull()
                            else -> null
                        }
                    }

                    Card(typeCard!!,colorCard!!,currCardValue,null,null,null)
                }
                var card3Object = it.optJSONObject("card3").optJSONObject("map")
                val card3 = card3Object?.let{
                    val color = card3Object.getString("color")
                    val colorCard = Color.fromTypeString(color)
                    val type = card3Object.getString("type")
                    val typeCard = Type.fromTypeString(type)

                    // get Value of card (can be either a number or null)
                    val currCardValue: Int? = card3Object?.let {
                        when (val value = card3Object.get("value")) {
                            is Int -> value
                            is String -> value.toIntOrNull()
                            else -> null
                        }
                    }
                    Card(typeCard!!,colorCard!!,currCardValue,null,null,null)
                }

                val type = it.getString("type")
                val moveType = MoveType.fromTypeString(type)

                Move(moveType!!, card1,card2,card3,"Because I can!")
            }

            var secondTurn = false
            if(currentPlayerIdx == prevPlayerIdx){
                secondTurn = true
            }

            currentGame = GameState(matchId,gameId,topCard,lastTopCard,drawPileSize,players,hand,handSize,currentPlayer,currentPlayerIdx,prevPlayer,prevPlayerIdx,prevTurnCards, lastMove,secondTurn,null,null

            )
            println("This is my current game: $currentGame")
            println("Received Game State: ${result.toString()} \n")
            for(i in 0 until (currentGame.hand?.size ?: 0)){
                print("My handcards: \n")
                print("type: ${currentGame.hand?.get(i)?.type} color: ${currentGame.hand?.get(i)?.color}  value: ${currentGame.hand?.get(i)?.value} \n")
            }
            SwingUtilities.invokeLater {
                // update GUI components here
                guiObject?.updateGameMenu(user)
            }
        } else {
            println("No Game State received")
        }
    })
}

/**
 * receive messages during the gameplay
 */
fun getGameStatus() {
    mSocket?.off("game:status")
    mSocket?.on("game:status", Emitter.Listener { args ->
        if (args[0] != null) {
            val result = gson.toJson(args)
            val jsonArray = JSONArray(result)
            // get data about Game Status
            val gameStatus = jsonArray.getJSONObject(0).getJSONObject("map")
            val message = gameStatus.getString("message")
            val currentPlayerObject = gameStatus.getJSONObject("winner").getJSONObject("map")
            val winner = Player(
                currentPlayerObject.getString("id"),
                currentPlayerObject.getString("username"),
                currentPlayerObject.getInt("points"),
                null
            )
            // store values in current game
            currentGame.winner = winner
            currentGame.message = message

            println("Received Game Status: ${result.toString()} \n")
            SwingUtilities.invokeLater {
                // update GUI components here
                guiObject?.updateGameMenu(user)
            }
        } else {
            println("No Game Status received")
        }
    })
}
