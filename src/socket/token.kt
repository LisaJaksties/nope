package socket
class Accesstoken(var accessToken: String?){}
class Register(var username: String, var password: String, var firstname: String, var lastname: String){}
class Login(var username: String, var password: String){}
class Token(var token: String){}
class TournamentCreate(var numBestOfMatches: Int){}