package io.github.domi04151309.home.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import io.github.domi04151309.home.R
import io.github.domi04151309.home.activities.HueLampActivity
import io.github.domi04151309.home.adapters.HueLampListAdapter
import io.github.domi04151309.home.data.ListViewItem
import io.github.domi04151309.home.data.RequestCallbackObject
import io.github.domi04151309.home.helpers.Global
import io.github.domi04151309.home.api.HueAPI
import io.github.domi04151309.home.data.DeviceItem
import io.github.domi04151309.home.data.SceneListItem
import io.github.domi04151309.home.helpers.HueUtils
import io.github.domi04151309.home.helpers.UpdateHandler
import io.github.domi04151309.home.interfaces.HueAdvancedLampInterface
import io.github.domi04151309.home.interfaces.HueRoomInterface
import io.github.domi04151309.home.interfaces.RecyclerViewHelperInterface
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class HueLampsFragment : Fragment(R.layout.fragment_hue_lamps), RecyclerViewHelperInterface,
    HueAdvancedLampInterface {

    private lateinit var c: Context
    private lateinit var lampData: HueRoomInterface
    private lateinit var hueAPI: HueAPI
    private lateinit var queue: RequestQueue
    private lateinit var requestCallBack: HueAPI.RequestCallback
    private val updateHandler: UpdateHandler = UpdateHandler()

    override var id: String = ""
    override var canReceiveRequest: Boolean = true
    override lateinit var device: DeviceItem
    override lateinit var addressPrefix: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        c = context ?: throw IllegalStateException()
        lampData = context as HueRoomInterface
        hueAPI = HueAPI(c, lampData.device.id)
        queue = Volley.newRequestQueue(context)

        device = lampData.device
        addressPrefix = lampData.addressPrefix

        val recyclerView = (super.onCreateView(inflater, container, savedInstanceState)
            ?: throw IllegalStateException()) as RecyclerView

        val hueLampStateListener = CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (compoundButton.isPressed) {
                val hidden =
                    (compoundButton.parent as ViewGroup).findViewById<TextView>(R.id.hidden).text.toString()
                hueAPI.switchLightByID(hidden, b)
            }
        }

        val adapter = HueLampListAdapter(hueLampStateListener, this@HueLampsFragment)
        recyclerView.layoutManager = LinearLayoutManager(c)
        recyclerView.adapter = adapter

        requestCallBack = object : HueAPI.RequestCallback {
            override fun onLightsLoaded(holder: RequestCallbackObject<JSONObject>) {
                if (holder.response != null) {
                    try {
                        var currentObjectName: String
                        var currentObject: JSONObject
                        var currentState: JSONObject
                        val listItems: ArrayList<ListViewItem> = arrayListOf()
                        val colorArray: ArrayList<Int> = arrayListOf()
                        for (i in 0 until (holder.response.length())) {
                            try {
                                currentObjectName = holder.response.names()?.getString(i) ?: ""
                                currentObject = holder.response.getJSONObject(currentObjectName)

                                currentState = currentObject.getJSONObject("state")
                                colorArray += if (currentState.has("hue") && currentState.has("sat")) {
                                    HueUtils.hueSatToRGB(
                                        currentState.getInt("hue"),
                                        currentState.getInt("sat")
                                    )
                                } else if (currentState.has("ct")) {
                                    HueUtils.ctToRGB(currentState.getInt("ct"))
                                } else {
                                    Color.parseColor("#FFFFFF")
                                }

                                listItems += ListViewItem(
                                    title = currentObject.getString("name"),
                                    summary =
                                    if (currentState.getBoolean("reachable")) resources.getString(R.string.hue_tap)
                                    else resources.getString(R.string.str_unreachable),
                                    hidden = currentObjectName,
                                    state = currentState.getBoolean("on")
                                )
                            } catch (e: JSONException) {
                                Log.e(Global.LOG_TAG, e.toString())
                            }
                        }
                        adapter.updateData(recyclerView, listItems, colorArray)
                    } catch (e: Exception) {
                        Log.e(Global.LOG_TAG, e.toString())
                    }
                }
            }
        }

        return recyclerView
    }

    override fun onStart() {
        super.onStart()
        updateHandler.setUpdateFunction {
            if (lampData.canReceiveRequest && hueAPI.readyForRequest) {
                hueAPI.loadLightsByIDs(lampData.lights ?: JSONArray(), requestCallBack)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        updateHandler.stop()
    }

    override fun onItemClicked(view: View, position: Int) {
        id = view.findViewById<TextView>(R.id.hidden).text.toString()
        HueColorSheet(this).show(
            (c as AppCompatActivity).supportFragmentManager,
            HueColorSheet::class.simpleName
        )
    }

    override fun onColorChanged(color: Int) {}
    override fun onBrightnessChanged(brightness: Int) {}
    override fun onHueSatChanged(hue: Int, sat: Int) {}
    override fun onCtChanged(ct: Int) {}
}