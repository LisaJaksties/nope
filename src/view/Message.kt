package view

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.Image
import javax.swing.*

fun infoBoard(fr: JFrame, content: String, time: Int) {
    val infoMessage = JDialog()
    infoMessage.setSize(900, 50)

    val x = fr.locationOnScreen.x + fr.width / 2 - infoMessage.width / 2
    val y = fr.locationOnScreen.y + fr.height / 8 - infoMessage.height / 2
    infoMessage.setLocation(x, y)
    infoMessage.isUndecorated = true

    val panel = JPanel()
    panel.layout = BorderLayout()
    panel.background = Color(154, 199, 220)

    val message = JLabel(content)
    message.font = Font("Arial", Font.BOLD, 16)
    message.horizontalAlignment = SwingConstants.CENTER
    message.verticalAlignment = SwingConstants.CENTER
    message.border = BorderFactory.createEmptyBorder(0, 10, 0, 10)
    panel.add(message, BorderLayout.CENTER)

    infoMessage.contentPane.add(panel)

    // 1500
    val timer = Timer(time) { e ->

        infoMessage.dispose()
    }
    timer.isRepeats = false
    timer.start()

    infoMessage.isVisible = true
}