package com.winnyking.wincalendar

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.winnyking.wincalendar.databinding.ActivityMainBinding // MODIFIED
// import com.example.taskerpluginsample.tasker.TaskerHelper concreto.getTaskerHelper // REMOVED - Specific to old sample structure
// import com.example.taskerpluginsample.tasker.WriteSettingActivityTasker // REMOVED - Specific to old sample structure
import com.joaomgcd.taskerpluginlibrary.TaskerPluginConstants
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.extensions.getTaskerInput // Assuming this is from the library
// import com.joaomgcd.taskerpluginlibrary.extensions.requestQuery // REMOVED - Not used in this simplified version
// import com.joaomgcd.taskerpluginlibrary.input.TaskerInput // REMOVED - getTaskerInput is more specific
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
        // This part is for a generic setting, not specifically the HelloWorld plugin.
        // It can be adapted later.
        if (intent?.action == "com.twofortyfouram.locale.intent.action.EDIT_SETTING") {
            // This is a generic way to handle an edit request.
            // Specific plugins like HelloWorldActivity will handle their own EDIT_SETTING.
            // For ActivityMain, we might just show some default data or placeholder.
            val taskerInput = intent.getTaskerInput<String>()
            binding.editTextData.setText(taskerInput?.input ?: "Main Activity Edit Mode")
            binding.editTextData.visibility = View.VISIBLE
            binding.buttonSaveToTasker.visibility = View.VISIBLE
        }

        binding.buttonSaveToTasker.setOnClickListener {
            val resultIntent = Intent()
            val resultBundle = Bundle()
            // This should be structured according to a specific plugin's input if this were a real config.
            resultBundle.putString(TaskerPluginConstants.BUNDLE_KEY_INPUT_JSON, "{ \"main_data\": \"${binding.editTextData.text}\" }")
            resultIntent.putExtra(TaskerPluginConstants.EXTRA_BUNDLE, resultBundle)
            resultIntent.putExtra(TaskerPluginConstants.EXTRA_STRING_BLURB, "Main Activity Data: ${binding.editTextData.text}")
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        // Links to original sample repositories
        binding.buttonOpenGithub.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/joaomgcd/TaskerPluginSample"))
            startActivity(intent)
        }
        binding.buttonOpenPluginPage.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/joaomgcd/TaskerPluginKotlin"))
            startActivity(intent)
        }

        // Placeholder for testing a condition - this part would need a real condition to be implemented.
        // The original sample had a WriteSettingActivityTasker which is not part of this minimal setup.
        binding.buttonTestCondition.setOnClickListener {
            Toast.makeText(this@ActivityMain, "Condition test placeholder", Toast.LENGTH_SHORT).show()
            // GlobalScope.launch(Dispatchers.Main) {
            //     val result = withContext(Dispatchers.IO) {
            //         // Replace with actual condition helper and input if available
            //         // getTaskerHelper(this@ActivityMain, YourConditionConfig::class.java).requestQuery()
            //     }
            //     val message = when ((result as? TaskerPluginResultCondition)?.state) {
            //         TaskerPluginResultConditionState.SATISFIED -> "Condition is SATISFIED"
            //         TaskerPluginResultConditionState.NOT_SATISFIED -> "Condition is NOT SATISFIED"
            //         else -> "Error querying condition or unknown state" //: ${result?.errorMessage}"
            //     }
            //     Toast.makeText(this@ActivityMain, message, Toast.LENGTH_LONG).show()
            // }
        }
    }
}
