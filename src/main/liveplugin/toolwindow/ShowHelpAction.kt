package liveplugin.toolwindow

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class ShowHelpAction: AnAction("Show help on GitHub"), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        BrowserUtil.open("https://github.com/dkandalov/live-plugin#liveplugin")
    }
}
