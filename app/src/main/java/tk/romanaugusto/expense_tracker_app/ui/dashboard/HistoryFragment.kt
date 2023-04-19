package tk.romanaugusto.expense_tracker_app.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import kotlinx.coroutines.*
import org.json.JSONObject
import tk.romanaugusto.expense_tracker_app.R
import tk.romanaugusto.expense_tracker_app.databinding.FragmentHistoryBinding
import java.io.File

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
            val baseList = mutableListOf<JSONObject>()
            val list  = JSONObject(historyListOfJSON.body)
                .getJSONArray("expenses")
            for (json in 0 until list.length()) {
                baseList.add(list.getJSONObject(json))
            }
            baseList.forEach {
                val textView = TextView(this.context).apply {
                    this.setPadding(5)
                }
                val textString = it.getString("title") + " " + it.getString("integer_symbol") + it.getString("amount")
                println(textString)
                textView.text = textString

                mainScope.launch {
                    layOut.addView(textView)
                }
            }
        }

        return root
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