package za.co.varsitycollege.st10215473.pank

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

class ReportFragment : Fragment() {

    private lateinit var title: TextView
    private lateinit var description: EditText
    private lateinit var location: EditText
    private lateinit var locationCheckBox: CheckBox
    private lateinit var pictureCheckBox: CheckBox
    private lateinit var image: ImageView
    private lateinit var reportButton: Button
    private lateinit var wildfireButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_report, container, false)
        val reportForm = layoutInflater.inflate(R.layout.activity_report, null)

        title = reportForm.findViewById(R.id.txtReportTitle)
        description = reportForm.findViewById(R.id.edtDescription)
        location = reportForm.findViewById(R.id.edtLocation)

        wildfireButton = view.findViewById(R.id.btnWildfire)

        wildfireButton.setOnClickListener {
            showDialog(wildfireButton, reportForm)
        }

        return view
    }

    fun showDialog(button: Button, reportForm: View){
        val formTitle = button.text.toString()
        title.text = formTitle
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(reportForm)
        val dialog = dialogBuilder.create()
        dialog.show()
    }

}