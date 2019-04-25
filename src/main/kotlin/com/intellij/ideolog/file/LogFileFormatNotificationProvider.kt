// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.ideolog.file

import com.intellij.ide.util.PropertiesComponent
import com.intellij.ideolog.highlighting.settings.LogHighlightingConfigurable
import com.intellij.ideolog.util.ideologContext
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications

class LogFileFormatNotificationProvider : EditorNotifications.Provider<EditorNotificationPanel>(), DumbAware {
  companion object {
    private val KEY = Key.create<EditorNotificationPanel>("log.file.format.editor.notification")
    private val HIDDEN_KEY = Key.create<Any>("log.file.format.editor.notification.hidden")
    private val DONT_SHOW_AGAIN_KEY = "log.file.format.editor.notification.disabled"

    private fun update(file: VirtualFile, project: Project) = EditorNotifications.getInstance(project).updateNotifications(file)
  }

  override fun getKey(): Key<EditorNotificationPanel> = KEY

  override fun createNotificationPanel(file: VirtualFile, fileEditor: FileEditor): EditorNotificationPanel? {
    if (fileEditor !is LogFileEditor) return null
    val editor = (fileEditor as TextEditor).editor
    val project = editor.project ?: return null

    val propertiesComponent = PropertiesComponent.getInstance()
    if (editor.document.ideologContext.detectLogFileFormat().myRegexLogParser != null || propertiesComponent.getBoolean(DONT_SHOW_AGAIN_KEY) || editor.getUserData(HIDDEN_KEY) != null)
      return null

    val panel = EditorNotificationPanel().apply {
      createActionLabel("Configure log formats") {
        ShowSettingsUtil.getInstance().editConfigurable(project, LogHighlightingConfigurable())

        update(file, project)
      }
      createActionLabel("Hide this notification") {
        editor.putUserData(HIDDEN_KEY, HIDDEN_KEY)

        update(file, project)
      }
      createActionLabel("Don't show again") {
        propertiesComponent.setValue(DONT_SHOW_AGAIN_KEY, true)

        update(file, project)
      }
    }

    return panel.text("Log format not recognized")
  }
}
