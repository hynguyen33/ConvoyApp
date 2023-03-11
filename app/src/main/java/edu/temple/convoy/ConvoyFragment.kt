package edu.temple.convoy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.navigation.Navigation

class ConvoyFragment : Fragment() {

    var joining = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        joining = arguments?.getBoolean("JOIN_ACTION")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_convoy, container, false)
        val groupIdEditText = layout.findViewById<EditText>(R.id.groupIdEditText)
        val joinButton = layout.findViewById<Button>(R.id.joinButton)
        val leaveButton = layout.findViewById<Button>(R.id.leaveGroupButton)

        groupIdEditText.visibility = if (joining) View.VISIBLE else View.INVISIBLE
        joinButton.visibility = if (joining) View.VISIBLE else View.INVISIBLE
        leaveButton.visibility = if (joining) View.INVISIBLE else View.VISIBLE

        joinButton.setOnClickListener{
            (activity as ConvoyControlInterface).joinConvoyFlow(groupIdEditText.text.toString())
            Navigation.findNavController(layout).popBackStack()
        }

        leaveButton.setOnClickListener{
            (activity as ConvoyControlInterface).leaveConvoyFlow(Helper.user.getConvoyId(requireContext())!!)
            Navigation.findNavController(layout).popBackStack()
        }

        return layout
    }

    interface ConvoyControlInterface {
        fun joinConvoyFlow(groupId: String)
        fun leaveConvoyFlow(groupId: String)
    }

}