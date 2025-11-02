package com.winnyking.wincalendar.tasker.htmlviewer

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.winnyking.wincalendar.databinding.ActivityHtmlViewerBinding

class HtmlViewerActivity : AppCompatActivity(), TaskerPluginConfig<HtmlViewerInput> {
    override val context: Context get() = applicationContext
    private val taskerHelper by lazy { HtmlViewerHelper(this) }
    private lateinit var binding: ActivityHtmlViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHtmlViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        taskerHelper.onCreate()
        binding.buttonSave.setOnClickListener { taskerHelper.finishForTasker() }
    }

    override fun assignFromInput(input: TaskerInput<HtmlViewerInput>) = input.regular.run {
        binding.editTextCode.setText(htmlContent ?: code)
        binding.editTextOutputVariables.setText(outputVariables)
    }

    override val inputForTasker: TaskerInput<HtmlViewerInput>
        get() = TaskerInput(
            HtmlViewerInput(
                htmlContent = binding.editTextCode.text.toString(),
                outputVariables = binding.editTextOutputVariables.text.toString()
            )
        )
}
