package tk.romanaugusto.expense_tracker_app.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import tk.romanaugusto.expense_tracker_app.R
import tk.romanaugusto.expense_tracker_app.databinding.FragmentHistoryBinding
import java.io.File
import java.util.*

class HistoryFragment : Fragment() {
    private val urlString = "http://10.178.24.180:8080/expenses/all"
    private lateinit var userData:JSONObject
    private val requestScope = CoroutineScope(Job())
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private var _binding: FragmentHistoryBinding? = null
    private lateinit var historyListOfJSON:HttpResponse<String>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val historyViewModel =
            ViewModelProvider(this)[HistoryViewModel::class.java]

        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        userData = JSONObject(File(context!!.filesDir, "settings.json").readText())
        val layOut = this.binding.history.findViewById<LinearLayout>(R.id.history).apply {
            this.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        }
        println("start coroutine fetch")
        println(userData.toString())
        requestScope.launch {
           historyListOfJSON = getHistory(userData.optString("_id"))
        }.invokeOnCompletion {
            addRecordsToLayout(layOut)
        }

        return root
    }

    private fun addRecordsToLayout (outerView:LinearLayout) {
        val baseList = mutableListOf<JSONObject>()
        val list  = JSONObject(historyListOfJSON.body)
            .getJSONArray("expenses")
        for (json in 0 until list.length()) {
            baseList.add(list.getJSONObject(json))
        }

        var counter = 0
        baseList.forEach {
            val displayMetrics = resources.displayMetrics
            val layout = LinearLayout(this.context).apply {
                this.layoutParams = LinearLayout.LayoutParams(displayMetrics.widthPixels,LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    this.setMargins(12)
                }
            }

            layout.setPadding(50)
            layout.orientation = LinearLayout.HORIZONTAL
            layout.gravity = Gravity.START
            layout.setPadding(60,0,60,0)

            val titleView = TextView(this.context)
            titleView.text = it.getString("title")
                .replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            titleView.layoutParams =
                LinearLayout.LayoutParams((displayMetrics.widthPixels * .70).toInt(),LinearLayout.LayoutParams.WRAP_CONTENT)

            val priceView = TextView(this.context)
            val priceString = it.getString("integer_symbol") + it.getString("amount")
            priceView.text = priceString


            layout.addView(titleView)
            layout.addView(priceView)

            mainScope.launch {
                outerView.addView(layout)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun getHistory (userId:String): HttpResponse<String> = Unirest.post(urlString)
        .header("accept", "*/*")
        .header("Content-Type", "application/json")
        .body(JSONObject(mapOf("accId" to userId)).toString())
        .asString()
}