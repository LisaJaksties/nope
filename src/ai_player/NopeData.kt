package ai_player


data class TournamentInfo(
    var tournamentId: String?,
    var bestOf: Int?,
    var currentSize: Int?,
    var success: Boolean,
    var error: Any?
)
data class Tournament(
    var id: String?,
    var createdAt: String?,
    var currentSize: Int?,
    var players: ArrayList<Player>?,
    var status: String?,
    var bestOf: Int?,
    var message: String?,
    var host: Player?,
    var winner: Player?
)

data class Player(
    var id: String?,
    var username: String?,
    var score: Int?,
    var handcards: Int?

)
data class MatchInvitation(
    var invitationTimeout: Long,
    var players: ArrayList<Player>,
    var message: String,
    var matchId: String
)
data class InvitationReply (
    var accepted: Boolean,
    var id: String
)

data class Match(
    var message: String?,
    var tournamentId: String?,
    var id: String?,
    var round: Int?,
    var bestOf: Int?,
    var status: String?,
    var opponents: ArrayList<Player>?,
    var winner: Player?

)
data class GameMoveNotice(
    var message: String?,
    var timeout: Int?
)
data class GameState(
    var matchId: String?,
    var gameId: String?,
    var topCard: Card?,
    var lastTopCard: Card?,
    var drawPileSize: Int?,
    var players: ArrayList<Player>?,
    var hand: ArrayList<Card>?,
    var handSize: Int?,
    var currentPlayer:Player?,
    var currentPlayerIdx: Int?,
    var prevPlayer: Player?,
    var prevPlayerIdx: Int?,
    var prevTurnCards: ArrayList<Card>?,
    var lastMove: Move?,
    var secondTurn: Boolean?,
    var message: String?,
    var winner: Player?
)

data class Card(
    var type: Type,
    var color: Color,
    var value: Int?,
    var select: Int?,
    var selectValue: Int?,
    var selectedColor: Color?
){
    fun getType(): String {
        return type.type.toLowerCase()
    }

    fun getColor(): String {
        return color.color.toLowerCase()
    }
}

data class Move(
    var type: MoveType,
    var card1: Card?,
    var card2: Card?,
    var card3: Card?,
    var reason: String
){
    fun getMoveType(): String {
        return type.moveType.toLowerCase()
    }
}

enum class Color(val color: String){
    RED("red"),
    BLUE("blue"),
    GREEN("green"),
    YELLOW("yellow"),
    RED_YELLOW("red-yellow"),
    BLUE_GREEN("blue-green"),
    YELLOW_BLUE("yellow-blue"),
    RED_BLUE("red-blue"),
    RED_GREEN("red-green"),
    YELLOW_GREEN("yellow-green"),
    MULTI("multi"),
    NULL("null");
    companion object {
        fun fromTypeString(colorString: String): Color? {
            return values().find { it.color == colorString }
        }
    }
    override fun toString(): String {
        return color.lowercase()
    }
}
enum class Type(val type: String){
    NUMBER("number"),
    JOKER("joker"),
    REBOOT("reboot"),
    SEE_THROUGH("see-through"),
    SELECTION("selection");
    companion object {
        fun fromTypeString(typeString: String): Type? {
            return values().find { it.type == typeString }
        }
    }
    override fun toString(): String {
        return type.lowercase()
    }
}
enum class MoveType(val moveType: String){
    TAKE("take"),
    PUT("put"),
    NOPE("nope");

    override fun toString(): String {
        return moveType.lowercase()
    }
}

