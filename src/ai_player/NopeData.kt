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
    var message: String?
)

data class Player(
    var id: String?,
    var username: String?

)

data class TurnInfo(
    var handCards: ArrayList<Card>,
    var lastCard: Card,
    var preLastCard: Card?,
    var secondTurn: Boolean?

)

data class Card(
    var type: Type,
    var color: Color,
    var value: Int?,
    var select: Int?,
    var selectValue: Int?,
    var selectedColor: String?
)

data class Move(
    var type: MoveType?,
    var card1: Card?,
    var card2: Card?,
    var card3: Card?,
    var reason: String
)

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
    MULTI("multi")
}
enum class Type(val type: String){
    NUMBER("number"),
    JOKER("joker"),
    REBOOT("reboot"),
    SEE_THROUGH("see-through"),
    SELECTION("selection")
}
enum class MoveType(val moveType: String){
    TAKE("take"),
    PUT("put"),
    NOPE("nope")
}
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