package za.co.varsitycollege.st10215473.pank

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_NAME)
            param2 = it.getString(ARG_EMAIL)
        }

    }

    private lateinit var nameText: TextView
    private lateinit var emailText: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Find the EditText or TextView fields where the user's info will be displayed
        nameText = view.findViewById(R.id.lblName)
        emailText = view.findViewById(R.id.lblEmail)

        // Retrieve the data passed through arguments (using Bundle)
        val name = arguments?.getString(param1)
        val email = arguments?.getString(param2)

        // Set the data to the UI components
        nameText.text = name
        emailText.text = email


        // Find the Buttons in the fragment layout
        val openSettingsButton = view.findViewById<Button>(R.id.btnSettings)
        val openAboutDevsButton = view.findViewById<Button>(R.id.btnAboutDevs)
        val openReportBugsButton = view.findViewById<Button>(R.id.btnReportBugs)
        val openLogoutButton = view.findViewById<Button>(R.id.btnlogout)
        val openProfileActivity = view.findViewById<Button>(R.id.btnGoToProfilePage)

        // Set the onClickListener for the Buttons
        openSettingsButton.setOnClickListener {
            // Create an Intent to open the new activity
            val intent = Intent(requireContext(), SettingsPage::class.java)
            startActivity(intent)
        }

        openAboutDevsButton.setOnClickListener {
            Toast.makeText(requireContext(), "Feature Coming Soon", Toast.LENGTH_SHORT).show()
        }
        openReportBugsButton.setOnClickListener{
            Toast.makeText(requireContext(), "Feature Coming Soon", Toast.LENGTH_SHORT).show()
        }
        openLogoutButton.setOnClickListener {
            Toast.makeText(requireContext(), "Feature Coming Soon", Toast.LENGTH_SHORT).show()
        }

        openProfileActivity.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        return view
    }



    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_EMAIL = "email"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param name User's name.
         * @param email User's email.
         * @return A new instance of fragment ProfileFragment.
         */
        @JvmStatic
        fun newInstance(name: String, email: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NAME, name)
                    putString(ARG_EMAIL, email)
                }
            }
    }
}
