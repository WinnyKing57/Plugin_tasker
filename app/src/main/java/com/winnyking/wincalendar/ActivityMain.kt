package com.winnyking.wincalendar

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.winnyking.wincalendar.databinding.ActivityMainBinding
import com.example.taskerpluginsample.tasker.TaskerHelper concreto.getTaskerHelper
import com.example.taskerpluginsample.tasker.WriteSettingActivityTasker
import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.extensions.getTaskerInput
import com.joaomgcd.taskerpluginlibrary.extensions.requestQuery
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultCondition
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultConditionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivityMain : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example: Check if this activity was launched from Tasker to configure a setting
        if (intent?.action == "com.twofortyfouram.locale.intent.action.EDIT_SETTING") {
            val taskerInput = intent.getTaskerInput<String>() // Assuming input is a simple String for this example
            binding.editTextData.setText(taskerInput?.input ?: "Default Value")
            binding.buttonSaveToTasker.visibility = View.VISIBLE
        }

        binding.buttonSaveToTasker.setOnClickListener {
            // This is where you would save the configuration back to Tasker
            // For this example, we'll just create a dummy result
            val resultIntent = Intent()
            val resultBundle = Bundle()
            resultBundle.putString(TaskerPluginConstants.BUNDLE_KEY_INPUT_JSON, "{ \"data\": \"${binding.editTextData.text}\" }") // Example JSON
            resultIntent.putExtra(TaskerPluginConstants.EXTRA_BUNDLE, resultBundle)
            resultIntent.putExtra(TaskerPluginConstants.EXTRA_STRING_BLURB, "Set to: ${binding.editTextData.text}")
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
        binding.buttonOpenGithub.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/joaomgcd/TaskerPluginSample"))
            startActivity(intent)
        }
        binding.buttonOpenPluginPage.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/joaomgcd/TaskerPluginKotlin"))
            startActivity(intent)
        }

        binding.buttonTestCondition.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val result = withContext(Dispatchers.IO) {
                    getTaskerHelper(this@ActivityMain, WriteSettingActivityTasker::class.java).requestQuery()
                }
                val message = when ((result as? TaskerPluginResultCondition)?.state) {
                    TaskerPluginResultConditionState.SATISFIED -> "Condition is SATISFIED"
                    TaskerPluginResultConditionState.NOT_SATISFIED -> "Condition is NOT SATISFIED"
                    else -> "Error querying condition or unknown state: ${result?.errorMessage}"
                }
                Toast.makeText(this@ActivityMain, message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
