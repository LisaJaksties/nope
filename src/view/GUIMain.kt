package view

import ai_player.Tournament
import socket.*
import java.awt.*
import javax.swing.*
import javax.swing.table.DefaultTableModel


class GUIMain : JFrame("Nope Card Game") {

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
    private var gameCard = JButton()
    private var gamePile = JLabel()
    private var gameMyMove = JTextArea()
    private var gameOpponent = JTextArea()

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


        val icon = ImageIcon("bin/icon.jpg")
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
            button.setForeground(Color.BLACK)                    // Vordergrundfarbe auf "rot" setzen
            button.background = Color(154,199,220) // Hintergrundfarbe auf "weiß" setzen
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

        gamePanel.layout= BorderLayout()
        gamePanel.maximumSize = Dimension(1200, 1000)
        val gameBorder = JPanel()
        gameBorder.layout = GridBagLayout()


        gameBorder.background = Color(242, 242, 242)

        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH

        gbc.gridx = 0
        gbc.gridy = 0
        gbc.gridwidth = 5

        gameHeader.background = Color(234, 226, 211)
        gameHeader.font = Font("Arial", Font.BOLD, 14)
        gameHeader.text = "Game Menu "
        gameHeader.maximumSize = Dimension(700, 100)
        gameBorder.add(gameHeader, gbc)

        gbc.gridx = 2
        gbc.gridy = 1
        gbc.gridwidth = 1


        gameCard.font = Font("Arial", Font.BOLD, 25)
        gameCard.background = Color(242, 242, 242)
        gameBorder.add(gameCard, gbc)
        gameCard.maximumSize = Dimension(140, 180)

        gbc.gridx = 2
        gbc.gridy = 2
        gbc.gridwidth = 1

        gamePile.font = Font("Arial", Font.BOLD, 25)
        gamePile.background = Color(242, 242, 242)
        gameBorder.add(gamePile, gbc)
        gamePile.maximumSize = Dimension(140, 10)
        gamePile.text = "size: 44"

        gbc.gridx = 0
        gbc.gridy = 3
        gbc.gridwidth = 3

        gameMyMove.font = Font("Arial", Font.BOLD, 25)
        gameMyMove.background = Color(132, 204, 204)
        gameBorder.add(gameMyMove, gbc)
        gameMyMove.maximumSize = Dimension(400, 200)

        gbc.gridx = 3
        gbc.gridy = 3
        gbc.gridwidth = 2

        gameOpponent.font = Font("Arial", Font.BOLD, 25)
        gameOpponent.background = Color(132, 204, 204)
        gameBorder.add(gameOpponent, gbc)
        gameOpponent.maximumSize = Dimension(260, 200)

        updateGameMenu()
        gamePanel.add(gameBorder,BorderLayout.CENTER)
    }

    public fun updateGameMenu(){
        gameHeader.text = ""
        gameHeader.append("Nope Card Game \n")
        gameHeader.append("Player1:  ${currentGame.players?.get(0)?.username} \n")
        gameHeader.append("Player2:  ${currentGame.players?.get(1)?.username} \n")
        gameHeader.append("Round:  ${currentMatch.round} \n")
        gameHeader.append(" ${currentMatch.opponents?.get(0)?.score} :  ${currentMatch.opponents?.get(1)?.score}")
        gameHeader.font = Font("Arial", Font.BOLD, 25)

        gameCard.text = "Card: ${currentGame.topCard?.type} ${currentGame.topCard?.color} ${currentGame.topCard?.value}"
        gameCard.icon = ImageIcon("bin/icon.jpg")

        gameMyMove.text = ""
        gameMyMove.append("My Hand: \n")
        gameMyMove.append("cards: \n")

        for(i in 0 until (currentGame.hand?.size?:0)){

            gameMyMove.append("Card ${i+1}: ${currentGame.hand?.get(i)?.getType()?:""}  ${currentGame.hand?.get(i)?.getColor()?:""}  ${currentGame.hand?.get(i)?.value?:""}  ${currentGame.hand?.get(i)?.selectValue?:""}  ${currentGame.hand?.get(i)?.selectedColor?:""}\n")
        }

        gameOpponent.text = ""
        gameOpponent.append("Number of Cards on Hand: ${currentGame.players?.get(1)?.handcards} \n")
    }

    private fun initWaitingRoomPanel(){
        waitingRoomPanel.layout = BorderLayout()

        // divide main Panel into two by adding a new panel
        val tempPanel = JPanel()
        tempPanel.layout = BorderLayout()
        tempPanel.background = Color(198, 226, 255)


        val leaveTournament = JButton("Leave Tournament")
        leaveTournament.font = Font("Arial", Font.BOLD, 14)
        leaveTournament.preferredSize = Dimension(150, 40)
        leaveTournament.background = Color(190,190,190)
        leaveTournament.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        leaveTournament.isBorderPainted = true
        leaveTournament.addActionListener {
            if (inTournament) {
                val tournamentInfoStatus = leaveTournament()
                if(tournamentInfoStatus.success){
                    showMessage(this, "You left the tournament", 2000)
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
                    showMessage(this, tournamentInfoStatus.error.toString(),2000)
                }

            } else {
                showMessage(this, "You cannot leave a tournament as you ar not in one", 2000)
            }

        }

        val startTournament = JButton("Start Tournament")
        startTournament.font = Font("Arial", Font.BOLD, 14)
        startTournament.preferredSize = Dimension(150, 40)
        startTournament.background = Color(190,190,190)
        startTournament.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        startTournament.isBorderPainted = true
        startTournament.addActionListener {
            if (tournamentCreator) {
                val tournamentInfoStatus = startTournament()
                if(tournamentInfoStatus.success){
                    showMessage(this, "The Tournament starts now -> go to game room", 2000)
                    cardLayout.show(contentPane, "game")
                }else{
                    showMessage(this,tournamentInfoStatus.error.toString(),2000)
                }
            } else {
                showMessage(this, "Sorry but only the admin can start a game", 2000)
            }
        }

        val messageBoard = JScrollPane(waitingRoomBoard)
        waitingRoomPanel.add(messageBoard, BorderLayout.CENTER)
        waitingRoomBoard.append("Here is the info then...")
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
        tournamentTable.gridColor = Color(190,190,190)



        val scrollPane = JScrollPane(tournamentTable)
        scrollPane.background = Color(198, 226, 255)
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        showTournPanel.add(scrollPane, BorderLayout.CENTER)

        val joinTournament = JButton("Join Tournament")
        joinTournament.font = Font("Arial", Font.BOLD, 14)
        joinTournament.preferredSize = Dimension(150, 40)
        joinTournament.background = Color(190,190,190)
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
                    showMessage(this, "You joined game number: $tournamentId ", 4000)
                    //updateTournamentList()

                    currentTournament.status = tournamentList[selectedRowIndex].status
                    currentTournament.createdAt =tournamentList[selectedRowIndex].createdAt
                    updateCurrentTournamentList()
                    cardLayout.show(contentPane, "game lobby")
                }
                else{
                    showMessage(this, tournamentInfoStatus.error.toString(), 2000)
                }

            } else {
                showMessage(this, "Please choose the tournament you want to join", 2000)
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
        createButton.background = Color(190,190,190)
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

                    showMessage(this,"Tournament created with ID: ${tournamentInfoStatus.tournamentId}, current size: ${tournamentInfoStatus.currentSize}, best of: ${tournamentInfoStatus.bestOf}",4000)
                    inTournament = true
                    tournamentCreator = true
                    //updateTournamentList()
                    currentTournament.id = tournamentInfoStatus.tournamentId
                    currentTournament.currentSize = tournamentInfoStatus.currentSize
                    currentTournament.bestOf = tournamentInfoStatus.bestOf

                    updateCurrentTournamentList()
                    cardLayout.show(contentPane, "game lobby")
                    tfName.text = "" // clear textfield
                } else{
                    showMessage(this,tournamentInfoStatus.error.toString(),4000)
                }

            } else {
                showMessage(this, "Please enter a valid integer value between 3 and 7.",2000)


            }
        }
        centerPanel.add(label)
        centerPanel.add(tfName)
//        crTournamentPanel.add(label, BorderLayout.LINE_START)
//        crTournamentPanel.add(tfName, BorderLayout.CENTER)

        createTournPanel.add(createButton, BorderLayout.SOUTH)
        createTournPanel.add(returnButton2, BorderLayout.NORTH)
        createTournPanel.add(centerPanel, BorderLayout.CENTER)
    }
    /*    private fun updateScoreTable() {
            val jsonString = """
                [
                    {"name": "John", "score": 100},
                    {"name": "Mary", "score": 90},
                    {"name": "Bob", "score": 80},
                    {"name": "Alice", "score": 70},
                    {"name": "David", "score": 60}
                ]
            """.trimIndent()

            val gson = Gson()
            val typeToken = object : TypeToken<List<Score>>() {}.type
            val scores = gson.fromJson<List<Score>>(jsonString, typeToken)

            scores.sortedByDescending { it.score }
                .forEach { score ->
                    val model = scoreTable.model as DefaultTableModel
                    model.addRow(arrayOf(score.name, score.score))
                }
        }*/
    private fun updateTournament(){
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

    }
    fun updateTournamentList(){

        tournamentTable.model= DefaultTableModel(arrayOf("Number", "ID", "Current Size", "Date", "status", "Players"), 0 )
        updateTournament()
        if(inTournament == true){
            tournamentTable.setEnabled(false)
        }else{
            tournamentTable.isEnabled = true
        }
    }
    private fun updateCurrentTournament(){
        if (currentTournament.id != null){
            if(currentTournament.message!=null){
                showMessage(this,currentTournament.message.toString(),2000)
            }
            var players = ""
            if(currentTournament.players != null){
                for(j in 0 until currentTournament.players!!.size){
                    val currP = currentTournament.players!![j]
                    players  += " ${currP.username} "
                }
            }
            if(currentTournament.winner != null){
                //TODO make prettier
            }
            if(currentTournament.host != null){
                //TODO make prettier
            }
            val model = currentTournamentTable.model as DefaultTableModel
            model.addRow(arrayOf( currentTournament.id, currentTournament.currentSize, currentTournament.createdAt, currentTournament.status, players, currentTournament.bestOf, currentTournament.message,currentTournament.host,currentTournament.winner))
        }
    }
    fun updateCurrentTournamentList(){

        currentTournamentTable.model= DefaultTableModel(arrayOf("ID", "Current Size", "Date", "status", "Players", "Best Of", "Last Message","Host", "Winner"), 0 )
        updateCurrentTournament()


    }

}