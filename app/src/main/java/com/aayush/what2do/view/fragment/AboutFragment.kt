package com.aayush.what2do.view.fragment


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.aayush.what2do.R
import kotlinx.android.synthetic.main.fragment_about.*


class AboutFragment: Fragment() {
    private lateinit var emailTextView: TextView
    private lateinit var githubTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailTextView = text_email
        githubTextView = text_github

        emailTextView.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:aayushjn11@gmail.com")
            intent.putExtra(Intent.EXTRA_SUBJECT, "")
            intent.putExtra(Intent.EXTRA_TEXT, "")

            if (intent.resolveActivity(activity?.packageManager!!) != null) {
                startActivity(intent)
            }
            else {
                Toast.makeText(context!!, "Unable to find email apps!", Toast.LENGTH_SHORT).show()
            }
        }

        githubTextView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://www.github.com/Aayushjn/What2Do")

            startActivity(intent)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AboutFragment()
    }
}

