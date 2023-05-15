package view

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.Image
import javax.swing.*

fun showMessage(fr: JFrame, content: String, time: Int) {
    val dialog = JDialog()
    dialog.setSize(900, 50)

    val x = fr.locationOnScreen.x + fr.width / 2 - dialog.width / 2
    val y = fr.locationOnScreen.y + fr.height / 8 - dialog.height / 2
    dialog.setLocation(x,y)
    dialog.isUndecorated = true

    val panel = JPanel()
    panel.layout = BorderLayout()
    panel.background = Color(229, 226, 248)

    val iconImage = ImageIcon("bin/info2.png").image
    val scaledIcon = ImageIcon(iconImage.getScaledInstance(32, 32, Image.SCALE_SMOOTH))
    val iconLabel = JLabel(scaledIcon)
    iconLabel.border = BorderFactory.createEmptyBorder(0, 10, 0, 10)
    panel.add(iconLabel, BorderLayout.WEST)

    val message = JLabel(content)
    message.font= Font("Arial", Font.BOLD, 16)
    message.horizontalAlignment = SwingConstants.CENTER
    message.verticalAlignment = SwingConstants.CENTER
    message.border = BorderFactory.createEmptyBorder(0, 10, 0, 10)
    panel.add(message, BorderLayout.CENTER)

    dialog.contentPane.add(panel)

    // 1500
    val timer = Timer(time) { e ->

        dialog.dispose()
    }
    timer.isRepeats = false
    timer.start()

    dialog.isVisible = true
}