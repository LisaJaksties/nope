package view

import ai_player.Move
import ai_player.Tournament
import socket.*
import java.awt.*
import javax.swing.*
import javax.swing.table.DefaultTableModel


class GUIgame : JFrame("Nope Card Game") {

    private val scoreTable = JTable()
    private var tournamentTable = JTable()
    private var currentTournamentTable = JTable()

    private var inTournament = false
    private var tournamentCreator = false

    private val menuPanel = JPanel()
    private val createTournPanel = JPanel()
    private val showTournPanel = JPanel()
    private val waitingRoomPanel = JPanel()
    private val gamePanel = JPanel()

    private var gameHeader = JTextArea()
    private var gameCard = JLabel()
    private var gameCardPic = JButton()
    private var gamePile = JLabel()
    private var gameMyMove = JTextArea()
    private var gameOpponent = JTextArea()
    private var spaceButton = JButton()

    private var waitingRoomBoard = JTextArea(20,20)
    private val cardLayout = CardLayout()


    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(1200, 1000)

        val img = ImageIcon("bin/cover.png")
        setIconImage(img.getImage())

        // Show the window
        isVisible = true

        // Create a JPanel to hold the menu items
        menuPanel.layout = BoxLayout(menuPanel, BoxLayout.Y_AXIS)
        menuPanel.setBackground(Color(242, 242, 242))

        // Create a JLabel for the title
        val titleLabel = JLabel("Nope!™")


        val icon = ImageIcon("bin/Nope.png")
        val titleIcon = JLabel()
        titleIcon.icon = icon
        titleIcon.alignmentX = CENTER_ALIGNMENT

        titleLabel.alignmentX = CENTER_ALIGNMENT
        titleLabel.font = titleLabel.font.deriveFont(24f)

        menuPanel.add(titleIcon)

        menuPanel.add(Box.createRigidArea(Dimension(0,20)))
        // Create a JButton for each menu item
        val items = listOf("Create Tournament", "Join Tournament")
        for (item in items) {
            val button = JButton(item)
            button.alignmentX = CENTER_ALIGNMENT
            button.maximumSize = Dimension(600, 80)
            button.setForeground(Color.BLACK)
            button.background = Color(154,199,220)
            button.isBorderPainted = false

            if(item == "Create Tournament"){
                button.addActionListener{
                    cardLayout.show(contentPane, "Create a new Tournament")
                }
            }
            if(item == "Join Tournament"){
                button.addActionListener{
                    cardLayout.show(contentPane, "tournamentLobby")
                }

            } else {
                button.addActionListener {
                    cardLayout.show(contentPane, "menu")
                }
            }
            menuPanel.add(button)
            menuPanel.add(Box.createRigidArea(Dimension(0,60)))

        }

        // Create a JButton to exit the application
        val exitButton = JButton("Exit")
        exitButton.alignmentX = CENTER_ALIGNMENT
        exitButton.background = Color(154,199,220)
        exitButton.maximumSize = Dimension(600, 80)
        exitButton.addActionListener {
            dispose()
        }
        menuPanel.add(exitButton)



        initTournamentPanel()

        initTournamentListPanel()

        initWaitingRoomPanel()

        initGamePanel()

        // Add the menu and score panels to the frame
        contentPane.layout = cardLayout
        contentPane.add(menuPanel, "menu")
        contentPane.add(createTournPanel, "Create a new Tournament")
        contentPane.add(showTournPanel, "tournamentLobby")
        contentPane.add(waitingRoomPanel, "game lobby")
        contentPane.add(gamePanel, "game")

    }

    private fun initGamePanel(){

        gamePanel.layout= BoxLayout(gamePanel, BoxLayout.Y_AXIS)
        val gameBorder = JPanel()
        gameBorder.layout = BorderLayout()
        val leftRight = JPanel()
        leftRight.layout = BoxLayout(leftRight, BoxLayout.X_AXIS)

        gameBorder.background = Color(132, 204, 204)

        val nopeGameHeader = JLabel("Nope Card Game ")
        nopeGameHeader.alignmentX = CENTER_ALIGNMENT
        nopeGameHeader.font = Font("Times New Roman", Font.BOLD, 40)
        nopeGameHeader.background = Color(132, 204, 204)
        gamePanel.add(nopeGameHeader)

        gameHeader.background = Color(234, 226, 211)
        gameHeader.font = Font("Arial", Font.BOLD, 14)
        gameHeader.text = "Game Menu "
        gameHeader.alignmentX = CENTER_ALIGNMENT

        gamePanel.add(gameHeader)

        gameCardPic.alignmentX = CENTER_ALIGNMENT
        gamePanel.add(gameCardPic)

        gameCard.font = Font("Arial", Font.BOLD, 25)
        gameCard.background = Color(234, 226, 211)
        gamePanel.add(gameCard)
        gameCard.alignmentX = CENTER_ALIGNMENT

        gamePile.font = Font("Arial", Font.BOLD, 25)
        gamePile.background = Color(154,199,220)
        gamePile.text = "size: 44"
        gamePanel.add(gamePile)
        gamePile.alignmentX = CENTER_ALIGNMENT

        gameMyMove.font = Font("Arial", Font.BOLD, 25)
        gameMyMove.background = Color(242, 242, 242)
        leftRight.add(gameMyMove)


        gameOpponent.font = Font("Arial", Font.BOLD, 25)
        gameOpponent.background = Color(242, 242, 242)
        leftRight.add(gameOpponent)


        updateGameMenu("opponentname")
        gameBorder.add(leftRight,BorderLayout.CENTER)
        gameBorder.alignmentX = CENTER_ALIGNMENT
        gamePanel.add(gameBorder)
    }


    private fun initWaitingRoomPanel(){
        waitingRoomPanel.layout = BorderLayout()

        // divide main Panel into two by adding a new panel
        val tempPanel = JPanel()
        tempPanel.layout = BorderLayout()
        tempPanel.background = Color(154,199,220)


        val leaveTournament = JButton("Leave Tournament")
        leaveTournament.font = Font("Arial", Font.BOLD, 14)
        leaveTournament.preferredSize = Dimension(150, 40)
        leaveTournament.background = Color(234, 226, 211)
        leaveTournament.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        leaveTournament.isBorderPainted = true
        leaveTournament.addActionListener {
            if (inTournament) {
                val tournamentInfoStatus = leaveTournament()
                if(tournamentInfoStatus.success){
                    infoBoard(this, "You left the tournament", 2000)
                    inTournament = false
                    tournamentCreator = false
                    // reset current tournament to nothing
                    currentTournament.bestOf = null
                    currentTournament.id = null
                    currentTournament.currentSize = null
                    currentTournament.status = null
                    currentTournament.players = null
                    currentTournament.createdAt = null

                    cardLayout.show(contentPane, "tournamentLobby")
                }else{
                    infoBoard(this, tournamentInfoStatus.error.toString(),2000)
                }

            } else {
                infoBoard(this, "You cannot leave a tournament as you ar not in one", 2000)
            }

        }

        val startTournament = JButton("Start Tournament")
        startTournament.font = Font("Arial", Font.BOLD, 14)
        startTournament.preferredSize = Dimension(150, 40)
        startTournament.background = Color(234, 226, 211)
        startTournament.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        startTournament.isBorderPainted = true
        startTournament.addActionListener {
            if (tournamentCreator) {
                val tournamentInfoStatus = startTournament()
                if(tournamentInfoStatus.success){
                    infoBoard(this, "The Tournament starts now -> go to game room", 2000)
                    cardLayout.show(contentPane, "game")
                }else{
                    infoBoard(this,tournamentInfoStatus.error.toString(),2000)
                }
            } else {
                infoBoard(this, "Sorry but only the admin can start a game", 2000)
            }
        }

        val messageBoard = JScrollPane(waitingRoomBoard)
        waitingRoomPanel.add(messageBoard, BorderLayout.CENTER)

        tempPanel.add(leaveTournament, BorderLayout.WEST)
        tempPanel.add(startTournament, BorderLayout.EAST)
        waitingRoomPanel.add(tempPanel, BorderLayout.SOUTH)

    }
    private fun initTournamentListPanel() {
        showTournPanel.layout = BorderLayout()
        // Create a JButton to return to the menu
        val returnButton3 = JButton("leave Room")
        returnButton3.font = Font("Arial", Font.BOLD, 14)
        returnButton3.preferredSize = Dimension(150, 40)
        returnButton3.background = Color(198, 226, 255)
        returnButton3.border = BorderFactory.createEmptyBorder(0, 10, 0, 10)

        returnButton3.addActionListener {
            cardLayout.show(contentPane, "menu")
        }


        tournamentTable.model =
            DefaultTableModel(arrayOf("Number", "ID", "Current Size", "Date", "status", "Players"), 0)
        tournamentTable.fillsViewportHeight = true
        tournamentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        tournamentTable.rowSelectionAllowed = true
        tournamentTable.columnSelectionAllowed = false
        tournamentTable.font = Font("Arial", Font.ITALIC, 16)
        tournamentTable.gridColor = Color(234, 226, 211)



        val scrollPane = JScrollPane(tournamentTable)
        scrollPane.background = Color(198, 226, 255)
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        showTournPanel.add(scrollPane, BorderLayout.CENTER)

        val joinTournament = JButton("Join Tournament")
        joinTournament.font = Font("Arial", Font.BOLD, 14)
        joinTournament.preferredSize = Dimension(150, 40)
        joinTournament.background = Color(234, 226, 211)
        joinTournament.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        joinTournament.isBorderPainted = true
        joinTournament.addActionListener {

            val selectedRowIndex = tournamentTable.selectedRow
            if (selectedRowIndex != -1) {

                val tournamentId = tournamentList[selectedRowIndex].id
                // join tournament
                val tournamentInfoStatus = joinTournament(tournamentId!!)
                if(tournamentInfoStatus.success){
                    inTournament = true
                    infoBoard(this, "You joined game number: $tournamentId ", 4000)
                    //updateTournamentList()

                    currentTournament.status = tournamentList[selectedRowIndex].status
                    currentTournament.createdAt =tournamentList[selectedRowIndex].createdAt
                    updateWaitingRoom()
                    cardLayout.show(contentPane, "game lobby")
                }
                else{
                    infoBoard(this, tournamentInfoStatus.error.toString(), 2000)
                }

            } else {
                infoBoard(this, "Please choose the tournament you want to join", 2000)
            }
        }


        showTournPanel.add(returnButton3, BorderLayout.NORTH)
        showTournPanel.add(joinTournament, BorderLayout.SOUTH)



    }
    private fun initTournamentPanel() {
        createTournPanel.layout = BorderLayout()
        createTournPanel.background = Color(198, 226, 255)


        // Create a JButton to create a Tournament
        val createButton = JButton("Create Tournament")
        createButton.isEnabled = false
        createButton.font = Font("Arial", Font.BOLD, 14)
        createButton.preferredSize = Dimension(150, 40)
        createButton.background = Color(234, 226, 211)
        createButton.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        createButton.isBorderPainted = true

        // Create a JButton to return to the menu
        val returnButton2 = JButton("Back to Main Menu")
        returnButton2.font = Font("Arial", Font.BOLD, 14)
        returnButton2.preferredSize = Dimension(150, 40)
        returnButton2.background = Color(198, 226,255)
        returnButton2.border = BorderFactory.createEmptyBorder(0, 10, 0, 10)

        returnButton2.addActionListener {
            cardLayout.show(contentPane, "menu")
        }

        // divide main Panel into two by adding a new panel
        val centerPanel = JPanel()
        centerPanel.layout = BoxLayout(centerPanel, BoxLayout.Y_AXIS)
        centerPanel.background = Color(198, 226, 255)

        // create Tournament Field
        val label = JLabel("Insert the number for Best of matches :")
        label.font = Font("Times New Roman", Font.PLAIN, 50)
        label.preferredSize = Dimension(300, 50)
        label.alignmentX = CENTER_ALIGNMENT
        //label.horizontalAlignment = SwingConstants.CENTER
        //label.verticalAlignment = SwingConstants.CENTER
        //label.border = BorderFactory.createEmptyBorder(0, 10, 0, 10)

        val tfName = JTextField(5)
        tfName.font = Font("Arial", Font.PLAIN, 20)
        tfName.preferredSize = Dimension(100, 30)
        tfName.horizontalAlignment = SwingConstants.CENTER
        tfName.border = BorderFactory.createEmptyBorder(0, 10, 0, 10)
        tfName.addActionListener {
            // check if there is an input of integer
            if (tfName.text.matches(Regex("-?\\d+"))) {
                createButton.isEnabled = true
            } else {
                createButton.isEnabled = false
            }
        }
        createButton.addActionListener {
            val number = tfName.text.toIntOrNull()
            if (number != null) {

                val tournamentInfoStatus = createTournament(number)
                if(tournamentInfoStatus.success && tournamentInfoStatus.tournamentId != null){

                    infoBoard(this,"Tournament created with ID: ${tournamentInfoStatus.tournamentId}, current size: ${tournamentInfoStatus.currentSize}, best of: ${tournamentInfoStatus.bestOf}",4000)
                    inTournament = true
                    tournamentCreator = true
                    //updateTournamentList()
                    currentTournament.id = tournamentInfoStatus.tournamentId
                    currentTournament.currentSize = tournamentInfoStatus.currentSize
                    currentTournament.bestOf = tournamentInfoStatus.bestOf

                    updateWaitingRoom()
                    cardLayout.show(contentPane, "game lobby")
                    tfName.text = "" // clear textfield
                } else{
                    infoBoard(this,tournamentInfoStatus.error.toString(),4000)
                }

            } else {
                infoBoard(this, "Please enter a valid integer value between 3 and 7.",2000)


            }
        }
        centerPanel.add(label)
        centerPanel.add(tfName)

        createTournPanel.add(createButton, BorderLayout.SOUTH)
        createTournPanel.add(returnButton2, BorderLayout.NORTH)
        createTournPanel.add(centerPanel, BorderLayout.CENTER)
    }

    fun updateTournamentLobby(){

        tournamentTable.model= DefaultTableModel(arrayOf("Number", "ID", "Current Size", "Date", "status", "Players"), 0 )
        val model = tournamentTable.model as DefaultTableModel
        val tempTournamentList = mutableListOf<Tournament>()
        tempTournamentList.addAll(tournamentList.map { it.copy() })


        for(i in 0 until tempTournamentList.size ){
            val currTour = tempTournamentList[i]
            //println(currTour)


            var players = ""
            for(j in 0 until currTour.players!!.size){
                val currP = currTour.players!![j]
                players  += " ${currP.username} "
            }
            model.addRow(arrayOf(i, currTour.id, currTour.currentSize, currTour.createdAt, currTour.status, players))
        }
        model.fireTableDataChanged()
        model.fireTableRowsInserted(0, tournamentList.size)

        // set not selectable if already joined a tournament
        tournamentTable.isEnabled = !inTournament
    }

    fun updateWaitingRoom(){
        infoBoard(this, currentTournament.message.toString(), 2000)

        waitingRoomBoard.font = Font("Arial", Font.PLAIN, 50)
        waitingRoomBoard
        waitingRoomBoard.text = ""
        waitingRoomBoard.append("Tournament ID : ${currentTournament.id} \n")
        waitingRoomBoard.append("Current Tournament Size: ${currentTournament.currentSize} \n")
        waitingRoomBoard.append("Date: ${currentTournament.createdAt.toString()} \n")
        waitingRoomBoard.append("Status: ${currentTournament.status} \n")
        for(j in 0 until (currentTournament.players?.size ?: 0)){
            val player = currentTournament.players?.get(j)
            if (player != null) {
                waitingRoomBoard.append("Player: ${player.username} score: ${player.score} \n")
            }
        }
        waitingRoomBoard.append("Rounds to play (Best of): ${currentTournament.bestOf} \n")
        waitingRoomBoard.append("Status message: ${currentTournament.message} \n")
        waitingRoomBoard.append("Host: ${currentTournament.host?.username}\n")
        waitingRoomBoard.append("Winner: ${currentTournament.winner?.username}")

        //switch between game lobby and game main window
        if(currentTournament.status == "IN_PROGRESS"){
            cardLayout.show(contentPane, "game")
        }else if(currentTournament.status == "FINISHED"){
            cardLayout.show(contentPane, "game lobby")
        }

    }

    public fun updateGameMenu(user: String){

        gameHeader.text = ""
        gameHeader.append("Player1:  ${currentGame.players?.get(0)?.username} \n")
        gameHeader.append("Player2:  ${currentGame.players?.get(1)?.username} \n")
        gameHeader.append("Round:  ${currentMatch.round} \n")
        gameHeader.append(" ${currentMatch.opponents?.get(0)?.score} :  ${currentMatch.opponents?.get(1)?.score}")
        gameHeader.font = Font("Arial", Font.BOLD, 25)

        gameCardPic.background = Color(234, 226, 211)
        gameCard.text = "Card: ${currentGame.topCard?.type?:""} ${currentGame.topCard?.color?:""} ${currentGame.topCard?.value?:""}"
        // change card icon - color
        if (currentGame.topCard?.color == ai_player.Color.RED) {
            gameCardPic.icon = ImageIcon("bin/card_red.png")
        } else if (currentGame.topCard?.color == ai_player.Color.GREEN) {
            gameCardPic.icon = ImageIcon("bin/card_green.png")
        } else if (currentGame.topCard?.color == ai_player.Color.YELLOW) {
            gameCardPic.icon = ImageIcon("bin/card_yellow.png")
        } else if (currentGame.topCard?.color == ai_player.Color.BLUE) {
            gameCardPic.icon = ImageIcon("bin/card_blue.png")
        } else if (currentGame.topCard?.color == ai_player.Color.RED_GREEN) {
            gameCardPic.icon = ImageIcon("bin/card_red_green.png")
        } else if (currentGame.topCard?.color == ai_player.Color.RED_BLUE) {
            gameCardPic.icon = ImageIcon("bin/card_blue_red.png")
        } else if (currentGame.topCard?.color == ai_player.Color.RED_YELLOW) {
            gameCardPic.icon = ImageIcon("bin/card_yellow_red.png")
        } else if (currentGame.topCard?.color == ai_player.Color.BLUE_GREEN) {
            gameCardPic.icon = ImageIcon("bin/card_blue_green.png")
        } else if (currentGame.topCard?.color == ai_player.Color.YELLOW_BLUE) {
            gameCardPic.icon = ImageIcon("bin/card_yellow_blue.png")
        } else if (currentGame.topCard?.color == ai_player.Color.YELLOW_GREEN) {
            gameCardPic.icon = ImageIcon("bin/card_yellow_green.png")
        } else if (currentGame.topCard?.color == ai_player.Color.MULTI) {
            gameCardPic.icon = ImageIcon("bin/card_multi.png")
        } else {
            gameCardPic.icon = ImageIcon("bin/card_empty.png")
        }

        gameMyMove.text = ""
        gameMyMove.append("My AI Player: \n")
        gameMyMove.append("cards: \n")

        for(i in 0 until (currentGame.hand?.size?:0)){

            gameMyMove.append("Card ${i+1}: ${currentGame.hand?.get(i)?.getType()?:""}  ${currentGame.hand?.get(i)?.getColor()?:""}  ${currentGame.hand?.get(i)?.value?:""}  ${currentGame.hand?.get(i)?.selectValue?:""}  ${currentGame.hand?.get(i)?.selectedColor?:""}\n")
        }

        gameOpponent.text = ""
        gameOpponent.append("Opponent: \n")

        var handcardOpponent = 0
        if(currentGame.players != null){
            for(p in currentGame.players!!){
                if(p.username != user){
                    handcardOpponent = p.handcards!!
                }
            }
        }

        gameOpponent.append("Number of Cards on Hand: ${handcardOpponent} \n")
    }

    public fun updateMyMove(myMove: Move){

        val myMoveString= "My current Move: Move Type: ${myMove.type}, 1. Card: ${myMove.card1?.type} ${myMove.card1?.color} ${myMove.card1?.value}, 2. Card:  ${myMove.card2?.type} ${myMove.card2?.color} ${myMove.card2?.value}, 3. Card:  ${myMove.card3?.type} ${myMove.card3?.color} ${myMove.card3?.value}"
        infoBoard(this, myMoveString, 6000)
    }

}