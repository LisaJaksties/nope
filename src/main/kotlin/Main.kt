import socket.*
import view.GUIgame
import java.util.*

/*
 * Main
 */
fun main() {

    val scanner = Scanner(System.`in`)
    //Server URL
    val serverURL = "https://nope-server.azurewebsites.net/"
    val restapi = RestApi()

    var token: Accesstoken? = null
    var loggedIn = false
    var username = ""

    // menu for register and login
    while (!loggedIn) {
        println("Nope card game Registration/ Login Menu:")
        println("1: Register \n2: Login")
        val input = scanner.next()

        if (!input.matches(Regex("\\d+"))) {
            println("Invalid choice. Please enter a number")
            continue
        }
        val choice = input.toInt()
        if (choice == 1) {
            println("Registration:")
            print("Enter username: ")
            username = scanner.next()
            print("Enter password: ")
            val password = scanner.next()
            print("Enter first name: ")
            val firstName = scanner.next()
            print("Enter last name: ")
            val lastName = scanner.next()

            val registerSuccess = restapi.registerUser(username, password, firstName, lastName)

            if (registerSuccess) {
                println("Registration was successful.")
            } else {
                println("Registration failed.")
            }
        } else if (choice == 2) {
            println("Login:")
            print("Enter username: ")
            username = scanner.next()
            print("Enter password: ")
            val password = scanner.next()

            token = restapi.userLogin(username, password)

            if (token != null) {
                println("Login successful.")
                loggedIn = true
            } else {
                println("Login failed.")
            }
        } else {
            println("Login didn't work.")
        }
    }

    // build Gui window
    val menu = GUIgame()
    // Socket init and connect with token
    if (token != null) {
        socketinit(serverURL, token, menu, username)
        connect()
        restapi.connect(token)

    } else {
        println("Fail: You couldn't log in")
    }

}